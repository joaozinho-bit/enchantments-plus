package pt.joao.enchantmentsplus.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Answers "is this enchantment acting on this item, and how strongly?".
 *
 * <p>Every enchantment asks the same question and would otherwise repeat the
 * same three checks, so they live here once: is it switched on, is it on the
 * stack at all, and is its level within the configured cap.
 */
public final class EnchantmentLevels {

	private EnchantmentLevels() {
	}

	/**
	 * @param world       the world whose registries define the enchantment
	 * @param stack       the item to inspect; may be empty
	 * @param enchantment the enchantment to look for
	 * @param config      that enchantment's live configuration
	 * @return the level to act at, or {@code 0} to do nothing
	 */
	public static int effective(World world, ItemStack stack, RegistryKey<Enchantment> enchantment,
			EnchantmentConfig config) {
		if (!config.isEnabled() || stack.isEmpty()) {
			return 0;
		}
		// The definition's max level is baked into the datapack file at build
		// time, so lowering it in the config can only take effect here.
		return Math.min(level(world, stack, enchantment), config.getMaxLevel());
	}

	/**
	 * Enchantments are data-driven, so the entry has to come from the world's
	 * registries rather than from a constant.
	 */
	private static int level(World world, ItemStack stack, RegistryKey<Enchantment> enchantment) {
		return world.getRegistryManager()
				.get(RegistryKeys.ENCHANTMENT)
				.getEntry(enchantment)
				.map(entry -> EnchantmentHelper.getLevel(entry, stack))
				.orElse(0);
	}
}
