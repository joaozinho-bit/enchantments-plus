package pt.joao.enchantmentsplus.enchantment;

import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Tunable values of {@link ExcavatorEnchantment}.
 *
 * <p>Nothing to tune beyond being switched on and how far it can be levelled.
 * How much the area is worth is not a number a pack should be setting freely:
 * the outline the player sees is derived from the same level the server acts on,
 * and the two only stay honest while there is a single rule turning one into the
 * other.
 */
public final class ExcavatorConfig extends EnchantmentConfig {

	public ExcavatorConfig() {
		super(3);
	}
}
