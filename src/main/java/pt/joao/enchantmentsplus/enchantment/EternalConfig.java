package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link EternalEnchantment}.
 *
 * <p>Nothing to tune beyond being switched on. Wear either happens or it does
 * not, so there is no strength to scale and no level worth having a second of:
 * a partial exemption from durability would be a worse Unbreaking, which the
 * game already has.
 *
 * <p>The enchanting and anvil costs live in the generated definition rather than
 * here, because they are read when the datapack loads and not when the
 * enchantment acts; the same is true of the maximum level, which this file can
 * only ever lower.
 */
public final class EternalConfig extends EnchantmentConfig {

	public EternalConfig() {
		super(1);
	}
}
