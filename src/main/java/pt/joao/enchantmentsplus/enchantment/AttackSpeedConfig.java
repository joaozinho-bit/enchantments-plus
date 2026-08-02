package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link AttackSpeedEnchantment}.
 *
 * <p>The two durations are what keep it a burst rather than a buff: a short
 * frenzy against a long cooldown means the enchantment is felt in moments, and
 * the ratio between them is the single knob that decides how often those
 * moments come.
 */
public final class AttackSpeedConfig extends EnchantmentConfig {

	/** Chance per level that a hit triggers a frenzy; {@code 0.1} is 10%. */
	public double chancePerLevel = 0.1;

	/** How long a frenzy lasts, in ticks. */
	public int frenzyTicks = 100;

	/** How long after a frenzy before another can start, in ticks. */
	public int cooldownTicks = 300;

	/**
	 * Share added to attack speed during a frenzy; {@code 0.5} is 50% faster.
	 *
	 * <p>Applied to the total, so it scales whatever weapon is held instead of
	 * flattening every blade to the same rate.
	 */
	public double attackSpeedMultiplier = 0.5;

	public AttackSpeedConfig() {
		super(3);
	}
}
