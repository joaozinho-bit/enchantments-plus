package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link AutoSmeltEnchantment}.
 *
 * <p>Nothing to tune beyond being switched on: what a block smelts into comes
 * from the game's own recipes, so there is no number here that could change the
 * outcome without contradicting them.
 */
public final class AutoSmeltConfig extends EnchantmentConfig {

	public AutoSmeltConfig() {
		super(1);
	}
}
