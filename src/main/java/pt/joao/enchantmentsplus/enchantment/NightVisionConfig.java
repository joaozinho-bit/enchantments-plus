package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link NightVisionEnchantment}.
 *
 * <p>Nothing to tune beyond being switched on: seeing in the dark is something
 * you either can or cannot do, so there is no number here that could change the
 * outcome. A single level, for the same reason.
 */
public final class NightVisionConfig extends EnchantmentConfig {

	public NightVisionConfig() {
		super(1);
	}
}
