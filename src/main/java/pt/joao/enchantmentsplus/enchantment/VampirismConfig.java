package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link VampirismEnchantment}.
 *
 * <p>The healing is a fraction of the damage actually dealt, so it stays in
 * proportion to the weapon and to whatever the target's armour absorbed instead
 * of being a flat bonus.
 */
public final class VampirismConfig extends EnchantmentConfig {

	/** Share of the damage dealt healed back per level; {@code 0.1} is 10%. */
	public double healFractionPerLevel = 0.1;

	public VampirismConfig() {
		super(3);
	}
}
