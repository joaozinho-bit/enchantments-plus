package pt.joao.enchantmentsplus.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registerable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import pt.joao.enchantmentsplus.EnchantmentsPlus;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.networking.HealthSync;
import pt.joao.enchantmentsplus.registry.ModEnchantments;
import pt.joao.enchantmentsplus.util.EnchantmentLevels;

/**
 * Hearty: armour that makes its wearer harder to finish off.
 *
 * <p>It has a single level and pays out per slot, so the reward is completing
 * the set rather than re-rolling one piece: a full set is worth an extra row of
 * hearts, split between the four pieces in proportion to how much armour each
 * one normally carries.
 *
 * <p>The bonus is an attribute modifier, never a direct write to the health
 * value: vanilla owns maximum health, so it also owns clamping the current
 * health when the maximum drops, and the value reaches the client through the
 * normal attribute sync without the mod sending anything itself.
 *
 * <p>The modifier is persistent so that it is already in place when the
 * player's saved health is read back on login &mdash; a temporary one would let
 * vanilla clamp a fully-healed player down to twenty on every join.
 *
 * <p>Newly gained hearts arrive already full, which keeps the enchantment
 * completely separate from hunger. Losing them is announced to the client so
 * that the clamp that follows is not drawn as an injury.
 */
public final class HeartyEnchantment {

	/**
	 * The single id every Hearty bonus is stored under.
	 *
	 * <p>An attribute holds at most one modifier per id, so reusing one id for
	 * the whole set is what makes duplicates structurally impossible.
	 */
	private static final Identifier MODIFIER_ID = EnchantmentsPlus.id("hearty");

	/** Health points in one heart. */
	private static final double HEALTH_PER_HEART = 2.0;

	private static ConfigHolder<HeartyConfig> config;

	private HeartyEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration. Call once from mod init,
	 * before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("hearty", new HeartyConfig());
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime. Single level, rare and expensive: extra health is the strongest
	 * thing a piece of armour can offer.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		EnchantmentDefinitions.armor(registry, ModEnchantments.HEARTY, 2, 1, 20, 70, 4);
	}

	/**
	 * Recomputes the wearer's bonus from the armour they are currently wearing.
	 *
	 * <p>Called from {@link pt.joao.enchantmentsplus.event.EntityEvents} whenever
	 * a piece of equipment changes and once when the entity enters the world.
	 * Nothing is ever adjusted incrementally: the previous modifier is dropped
	 * and a single new one is derived from the whole set, so no sequence of
	 * equipping and unequipping can make the bonus drift or stack. Vanilla
	 * applies the change once at the end of the tick, so the intermediate state
	 * inside this method is never observed.
	 *
	 * @param entity the wearer
	 */
	public static void refresh(LivingEntity entity) {
		EntityAttributeInstance maxHealth = entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (maxHealth == null) {
			return;
		}

		EntityAttributeModifier existing = maxHealth.getModifier(MODIFIER_ID);
		double previous = existing == null ? 0.0 : existing.value();
		maxHealth.removeModifier(MODIFIER_ID);

		HeartyConfig settings = config.get();
		double bonus = bonusFor(entity, settings);
		if (bonus > 0.0) {
			maxHealth.addPersistentModifier(
					new EntityAttributeModifier(MODIFIER_ID, bonus, EntityAttributeModifier.Operation.ADD_VALUE));
		}

		if (bonus > previous) {
			// The new hearts arrive full. Leaving them empty would put the
			// wearer below their maximum, and that is precisely what starts
			// vanilla's natural regeneration: it would close the gap slowly,
			// only above 9 drumsticks, and charge the food bar for every half
			// heart. Granting the health outright sidesteps all three.
			entity.setHealth(entity.getHealth() + (float) (bonus - previous));
		} else if (bonus < previous && entity instanceof ServerPlayerEntity player) {
			// The clamp that vanilla is about to apply would otherwise be drawn
			// as damage on the client.
			HealthSync.notifyLowered(player);
		}
	}

	/** @return the total bonus the worn armour is worth, in health points */
	private static double bonusFor(LivingEntity entity, HeartyConfig settings) {
		double hearts = 0.0;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
				continue;
			}
			if (EnchantmentLevels.effective(
					entity.getWorld(), entity.getEquippedStack(slot), ModEnchantments.HEARTY, settings) > 0) {
				hearts += heartsFor(slot, settings);
			}
		}
		return hearts * HEALTH_PER_HEART;
	}

	/** @return what an enchanted piece in this slot is worth, in hearts */
	private static double heartsFor(EquipmentSlot slot, HeartyConfig settings) {
		return switch (slot) {
			case HEAD -> settings.helmetHearts;
			case CHEST -> settings.chestplateHearts;
			case LEGS -> settings.leggingsHearts;
			case FEET -> settings.bootsHearts;
			default -> 0.0;
		};
	}
}
