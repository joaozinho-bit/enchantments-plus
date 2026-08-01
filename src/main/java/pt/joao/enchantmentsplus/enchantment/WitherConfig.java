package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link WitherEnchantment}.
 *
 * <p>Only the duration scales with the level; the amplifier is a flat option so
 * a server that wants a harsher Wither can raise it without the enchantment
 * turning into a per-level power curve.
 */
public final class WitherConfig extends EnchantmentConfig {

	/** Ticks of Wither granted per enchantment level; 60 ticks is 3 seconds. */
	public int durationTicksPerLevel = 60;

	/** Amplifier of the applied effect, where {@code 0} is Wither I. */
	public int amplifier = 0;

	public WitherConfig() {
		super(3);
	}
}
