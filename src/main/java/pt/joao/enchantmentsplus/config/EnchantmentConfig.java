package pt.joao.enchantmentsplus.config;

/**
 * Base class for every enchantment's configuration.
 *
 * <p>It holds the options that <em>all</em> enchantments share, so those never
 * have to be redeclared. Each enchantment defines a small subclass that adds
 * only its own specific options (cooldown, chance, damage, ...) and passes its
 * defaults through the constructor:
 *
 * <pre>{@code
 * public final class StormConfig extends EnchantmentConfig {
 *     public double chance = 0.25;
 *     public StormConfig() { super(1); } // max level 1
 * }
 * }</pre>
 *
 * <p>Instances are plain data: fields are read via reflection by Gson and are
 * populated once at load time (see {@link ConfigManager}).
 */
public abstract class EnchantmentConfig {

	private boolean enabled = true;
	private int maxLevel;

	/**
	 * @param maxLevel the default maximum level of the enchantment
	 */
	protected EnchantmentConfig(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	/** @return whether the enchantment is active at all */
	public boolean isEnabled() {
		return enabled;
	}

	/** @return the highest level the enchantment can reach */
	public int getMaxLevel() {
		return maxLevel;
	}
}
