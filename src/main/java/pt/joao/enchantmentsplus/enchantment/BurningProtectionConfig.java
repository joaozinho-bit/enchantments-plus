package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link BurningProtectionEnchantment}.
 *
 * <p>There is deliberately no strength to tune: the enchantment either applies
 * or it does not, which is what makes the full-set requirement the whole cost
 * of it. A single level, for the same reason.
 */
public final class BurningProtectionConfig extends EnchantmentConfig {

	/**
	 * Whether all four pieces must carry the enchantment.
	 *
	 * <p>On by default, and what makes this an end-game reward rather than
	 * something a single lucky helmet hands out. Turning it off makes any one
	 * enchanted piece enough.
	 */
	public boolean requireFullSet = true;

	public BurningProtectionConfig() {
		super(1);
	}
}
