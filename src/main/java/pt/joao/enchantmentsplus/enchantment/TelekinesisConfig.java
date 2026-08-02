package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link TelekinesisEnchantment}.
 *
 * <p>Nothing to tune beyond being switched on: the enchantment changes only
 * where items end up, never how many there are, so there is no number that
 * could sensibly be turned up.
 */
public final class TelekinesisConfig extends EnchantmentConfig {

	public TelekinesisConfig() {
		super(1);
	}
}
