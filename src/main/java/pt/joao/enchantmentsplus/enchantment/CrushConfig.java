package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link CrushEnchantment}.
 *
 * <p>Height and radius are related: the higher the anvil starts, the longer the
 * target has to walk away, so a server that raises {@link #fallHeight} for the
 * spectacle will usually want a slightly larger {@link #impactRadius} too.
 */
public final class CrushConfig extends EnchantmentConfig {

	/** Chance per level that a hit summons an anvil; {@code 0.15} is 15%. */
	public double chancePerLevel = 0.15;

	/** How many blocks above the target the anvil appears. */
	public int fallHeight = 6;

	/** Damage dealt to every entity caught by the impact. */
	public float damage = 8.0F;

	/**
	 * Blocks around the impact point that still take damage.
	 *
	 * <p>Keeps the enchantment fair despite the fall time: the target can move
	 * while the anvil is on its way. Meant to stay small &mdash; a large radius
	 * turns a duel into an area attack.
	 */
	public double impactRadius = 1.5;

	public CrushConfig() {
		super(3);
	}
}
