package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link JumpEnchantment}.
 *
 * <p>Height comes from the level, timing from the other two: a full charge at
 * level three clears nine blocks after three seconds of holding, and the fall
 * that follows costs nothing. The gap between levels is deliberately wide
 * enough to feel without reading a number.
 */
public final class JumpConfig extends EnchantmentConfig {

	/** Blocks of height a fully charged jump gains per level. */
	public double maxHeightPerLevel = 3.0;

	/** How long a charge takes to fill, in ticks, before {@link #chargeSpeed}. */
	public int maxChargeTicks = 60;

	/**
	 * Multiplier on how quickly the charge fills.
	 *
	 * <p>Kept apart from the tick count so the two can be tuned for different
	 * reasons: the ticks set the feel of a full charge, this scales it without
	 * having to recompute it.
	 */
	public double chargeSpeed = 1.0;

	public JumpConfig() {
		super(3);
	}
}
