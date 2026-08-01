package pt.joao.enchantmentsplus.enchantment;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;

/**
 * The definition shape shared by the mod's sword enchantments.
 *
 * <p>All of them are swords-only, act from the main hand and widen their
 * enchanting cost window at the same rate; only the weight, the level cap and
 * the cost range differ. Keeping the common half here means a new sword
 * enchantment states its numbers and nothing else, and that they can never
 * drift apart by accident.
 */
final class SwordEnchantments {

	/** How much the enchanting cost window moves per level above the first. */
	private static final int COST_PER_LEVEL = 20;

	private SwordEnchantments() {
	}

	/**
	 * Registers one sword enchantment definition.
	 *
	 * @param registry  the registry being bootstrapped
	 * @param key       the enchantment to define
	 * @param weight    how often it shows up in the enchanting table
	 * @param maxLevel  the highest level obtainable in-game
	 * @param minCost   enchantability needed at level one
	 * @param maxCost   upper end of the enchantability window at level one
	 * @param anvilCost cost multiplier when combining in an anvil
	 */
	static void register(Registerable<Enchantment> registry, RegistryKey<Enchantment> key,
			int weight, int maxLevel, int minCost, int maxCost, int anvilCost) {
		registry.register(key, Enchantment.builder(Enchantment.definition(
						registry.getRegistryLookup(RegistryKeys.ITEM).getOrThrow(ItemTags.SWORD_ENCHANTABLE),
						weight,
						maxLevel,
						Enchantment.leveledCost(minCost, COST_PER_LEVEL),
						Enchantment.leveledCost(maxCost, COST_PER_LEVEL),
						anvilCost,
						AttributeModifierSlot.MAINHAND))
				.build(key.getValue()));
	}
}
