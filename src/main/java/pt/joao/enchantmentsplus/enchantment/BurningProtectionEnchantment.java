package pt.joao.enchantmentsplus.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.tag.DamageTypeTags;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.registry.ModEnchantments;
import pt.joao.enchantmentsplus.util.ArmorSet;

/**
 * Burning Protection: a full set that simply stops burning.
 *
 * <p>Enchanting one piece does nothing at all. Only when all four carry it does
 * the wearer become immune to fire, to lava and to burning &mdash; the whole
 * point being that it costs four enchants to get anything, which is what makes
 * it an end-game reward rather than a lucky helmet.
 *
 * <p>The set is read at the instant damage would be dealt rather than tracked
 * in a cache. That is not just fast enough, it is what makes the effect exact:
 * there is no stored state that could still say "complete" a tick after a
 * piece came off, so swapping gear can never leave a stale immunity behind and
 * there is nothing to invalidate.
 *
 * <p>Blocking the damage is only half of it &mdash; the wearer is also put out,
 * so a complete set means not burning at all rather than burning harmlessly the
 * way a fire resistance potion leaves you.
 */
public final class BurningProtectionEnchantment {

	private static ConfigHolder<BurningProtectionConfig> config;

	private BurningProtectionEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration. Call once from mod init,
	 * before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("burning_protection", new BurningProtectionConfig());
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime. The rarest and priciest of the roster: needing four of them is
	 * already the cost, so the table should not hand them out easily either.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		EnchantmentDefinitions.armor(registry, ModEnchantments.BURNING_PROTECTION, 1, 1, 25, 75, 8);
	}

	/**
	 * Decides whether the wearer shrugs this damage off.
	 *
	 * <p>Called from {@link pt.joao.enchantmentsplus.event.DamageEvents} for
	 * every incoming hit, so it answers cheaply and gives up on the first check
	 * for anything that is not fire.
	 *
	 * @param entity the entity being damaged
	 * @param source what is damaging it
	 * @return {@code true} when the damage should be ignored entirely
	 */
	public static boolean absorbs(LivingEntity entity, DamageSource source) {
		// The same family of damage a fire resistance potion protects against:
		// standing in fire, burning, lava and hot floors.
		if (!source.isIn(DamageTypeTags.IS_FIRE)) {
			return false;
		}

		BurningProtectionConfig settings = config.get();
		// The full-set path stops at the first piece without the enchantment,
		// so an unenchanted wearer is turned away after a single lookup.
		boolean active = settings.requireFullSet
				? ArmorSet.isComplete(entity, ModEnchantments.BURNING_PROTECTION, settings)
				: ArmorSet.count(entity, ModEnchantments.BURNING_PROTECTION, settings) > 0;
		if (!active) {
			return false;
		}

		// Vanilla lights the wearer up just before it damages them, so putting
		// them out here also clears the flames they just caught.
		entity.extinguish();
		return true;
	}
}
