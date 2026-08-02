package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link MomentumEnchantment}.
 *
 * <p>The three numbers set the whole shape of it: how much each block is worth,
 * how far it can build up, and how long the streak survives a pause. At the
 * defaults a full streak doubles mining speed and half a minute away from the
 * rock loses it all.
 */
public final class MomentumConfig extends EnchantmentConfig {

	/** Share of the base mining speed added per stack; {@code 0.05} is 5%. */
	public double speedPerStack = 0.05;

	/** How many stacks can be held at once. */
	public int maxStacks = 20;

	/**
	 * Ticks without using the tool on a block before the whole streak is lost.
	 *
	 * <p>Measured from the last time the player <em>started</em> or finished a
	 * block, not from the last one that broke, so a long swing at obsidian does
	 * not cost the streak that earned it. Six hundred ticks is thirty seconds.
	 */
	public int decayTicks = 600;

	public MomentumConfig() {
		super(1);
	}
}
