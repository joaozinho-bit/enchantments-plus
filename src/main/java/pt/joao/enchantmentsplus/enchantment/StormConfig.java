package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link StormEnchantment}.
 *
 * <p>The chance is per level and adds up, so the default lets level three fire
 * on roughly half of all critical hits. Reaching or passing {@code 1.0} makes
 * every critical hit call lightning, which is left possible on purpose.
 */
public final class StormConfig extends EnchantmentConfig {

	/** Chance per level that a critical hit calls lightning; {@code 0.15} is 15%. */
	public double chancePerLevel = 0.15;

	/**
	 * Damage the bolt deals to the entity that was hit.
	 *
	 * <p>The bolt is summoned harmless so it cannot start fires, which means
	 * this value replaces the damage a natural lightning strike would have
	 * dealt rather than adding to it. The default matches vanilla.
	 */
	public float damage = 5.0F;

	public StormConfig() {
		super(3);
	}
}
