package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link SpeedEnchantment}.
 */
public final class SpeedConfig extends EnchantmentConfig {

	/**
	 * How much faster each level makes the wearer, as a fraction of their
	 * ordinary walking speed.
	 *
	 * <p>A fraction rather than a flat amount, so the bonus keeps its meaning
	 * whatever else is acting on the player: sprinting, Soul Speed and a
	 * potion of Swiftness all scale with it instead of being drowned out by it.
	 * At the default, three levels are worth about a potion of Swiftness.
	 */
	public double speedPerLevel = 0.08;

	public SpeedConfig() {
		super(3);
	}
}
