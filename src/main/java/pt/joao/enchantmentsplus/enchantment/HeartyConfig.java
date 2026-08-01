package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link HeartyEnchantment}.
 *
 * <p>There is one value per slot rather than one value per level: the reward
 * comes from completing the set, and each piece is worth roughly what it is
 * worth as armour, so the chestplate carries the most and the boots the least.
 * The defaults add up to a full extra row of ten hearts.
 *
 * <p>Values are in hearts, the unit the numbers were designed in; one heart is
 * two health points.
 */
public final class HeartyConfig extends EnchantmentConfig {

	/** Extra hearts from an enchanted helmet. */
	public double helmetHearts = 2.0;

	/** Extra hearts from an enchanted chestplate. */
	public double chestplateHearts = 4.0;

	/** Extra hearts from enchanted leggings. */
	public double leggingsHearts = 3.0;

	/** Extra hearts from enchanted boots. */
	public double bootsHearts = 1.0;

	public HeartyConfig() {
		super(1);
	}
}
