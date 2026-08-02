package pt.joao.enchantmentsplus.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registerable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import pt.joao.enchantmentsplus.EnchantmentsPlus;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.effect.ToggleManager;
import pt.joao.enchantmentsplus.effect.ToggleableEffect;
import pt.joao.enchantmentsplus.registry.ModEnchantments;

/**
 * Speed: boots that can be told to hurry, and told to stop.
 *
 * <p>Always-on movement speed would be a strictly better pair of boots and
 * nothing else; being able to switch it off is what makes it a choice. Sneaking
 * up on something, lining up a jump and walking a one-block ledge all want the
 * ordinary speed back, and the player is the only one who knows which.
 *
 * <p>The speed itself is an attribute modifier rather than a status effect, so
 * it composes with everything already acting on the player instead of competing
 * with it, and vanilla syncs the value to the client with nothing sent from
 * here. Temporary rather than persistent: a preference is meant to die with the
 * session, and the modifier is rewritten from scratch on every refresh anyway.
 *
 * <p>Everything about when it runs belongs to
 * {@link pt.joao.enchantmentsplus.effect.ToggleManager}; this class only says
 * what "on" means.
 */
public final class SpeedEnchantment {

	/** The toggle, the key binding and the attribute modifier are all this one thing. */
	public static final Identifier ID = EnchantmentsPlus.id("speed");

	private static ConfigHolder<SpeedConfig> config;

	private SpeedEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration and its toggle. Call once from
	 * mod init, before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("speed", new SpeedConfig());
		ToggleManager.register(ToggleableEffect.applied(ID, ModEnchantments.SPEED, EquipmentSlot.FEET,
				config::get, SpeedEnchantment::enable, SpeedEnchantment::disable));
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime. Boots only, so it can never be rolled onto another piece.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		EnchantmentDefinitions.boots(registry, ModEnchantments.SPEED, 2, 3, 15, 65, 4);
	}

	private static void enable(ServerPlayerEntity player, int level) {
		applyBonus(player, level * config.get().speedPerLevel);
	}

	private static void disable(ServerPlayerEntity player) {
		applyBonus(player, 0.0);
	}

	/**
	 * Rewrites the bonus from scratch under one fixed id, so no sequence of
	 * toggling, dying and swapping boots can make it drift or stack up.
	 */
	private static void applyBonus(ServerPlayerEntity player, double bonus) {
		EntityAttributeInstance speed =
				player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}

		speed.removeModifier(ID);
		if (bonus > 0.0) {
			speed.addTemporaryModifier(new EntityAttributeModifier(
					ID, bonus, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		}
	}
}
