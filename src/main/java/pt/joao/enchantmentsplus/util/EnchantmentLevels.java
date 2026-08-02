package pt.joao.enchantmentsplus.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Answers "is this enchantment acting on this item, and how strongly?".
 *
 * <p>Every enchantment asks the same question and would otherwise repeat the
 * same three checks, so they live here once: is it switched on, is it on the
 * stack at all, and is its level within the configured cap.
 *
 * <p>Each method comes in two shapes, one taking a {@link World} and one not.
 * They answer identically; the difference is only how the enchantment is
 * recognised. Gameplay code should take the world, which is the ordinary way to
 * reach a data-driven registry. The world-free pair exists for the callers that
 * have no world to hand or are asked often enough that a registry lookup per
 * call would be felt &mdash; rendering, most of all.
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
	 * The raw level on the stack, ignoring configuration.
	 *
	 * <p>For the rare caller that has no business reading the server's config,
	 * such as a client deciding whether an input is worth reporting at all.
	 * Anything that acts on the answer should use {@link #effective} instead.
	 *
	 * <p>Enchantments are data-driven, so the entry has to come from the world's
	 * registries rather than from a constant.
	 *
	 * @param world       the world whose registries define the enchantment
	 * @param stack       the item to inspect; may be empty
	 * @param enchantment the enchantment to look for
	 * @return the level on the stack, or {@code 0}
	 */
	public static int level(World world, ItemStack stack, RegistryKey<Enchantment> enchantment) {
		return world.getRegistryManager()
				.get(RegistryKeys.ENCHANTMENT)
				.getEntry(enchantment)
				.map(entry -> EnchantmentHelper.getLevel(entry, stack))
				.orElse(0);
	}

	/**
	 * The level to act at, without a world.
	 *
	 * <p>The world-free twin of {@link #effective(World, ItemStack, RegistryKey,
	 * EnchantmentConfig)}, and subject to the same caveat as {@link
	 * #level(ItemStack, RegistryKey)}: the configuration read is whichever one
	 * this side of the connection has. Safe for anything cosmetic, wrong for
	 * anything a server should decide.
	 *
	 * @param stack       the item to inspect; may be empty
	 * @param enchantment the enchantment to look for
	 * @param config      that enchantment's live configuration
	 * @return the level to act at, or {@code 0} to do nothing
	 */
	public static int effective(ItemStack stack, RegistryKey<Enchantment> enchantment,
			EnchantmentConfig config) {
		if (!config.isEnabled()) {
			return 0;
		}
		return Math.min(level(stack, enchantment), config.getMaxLevel());
	}

	/**
	 * The raw level on the stack, read from the item's own components.
	 *
	 * <p>Identical in meaning to {@link #level(World, ItemStack, RegistryKey)}
	 * &mdash; vanilla's own lookup does nothing more than read this component
	 * &mdash; but it recognises the enchantment by its key instead of resolving
	 * it through the registry first. That makes it usable where there is no
	 * world, and cheap enough to ask once per item slot per frame.
	 *
	 * <p>The components travel with the stack, so this is as true on a client as
	 * it is on the server. The configuration is not: anything reading this on the
	 * client is reading that client's own settings, which is why only cosmetic
	 * decisions belong here.
	 *
	 * @param stack       the item to inspect; may be empty
	 * @param enchantment the enchantment to look for
	 * @return the level on the stack, or {@code 0}
	 */
	public static int level(ItemStack stack, RegistryKey<Enchantment> enchantment) {
		if (stack.isEmpty()) {
			return 0;
		}

		ItemEnchantmentsComponent enchantments =
				stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
		// The overwhelmingly common case, and the reason this is affordable to
		// ask about every stack being drawn.
		if (enchantments.isEmpty()) {
			return 0;
		}

		for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
			if (entry.matchesKey(enchantment)) {
				return enchantments.getLevel(entry);
			}
		}
		return 0;
	}
}
