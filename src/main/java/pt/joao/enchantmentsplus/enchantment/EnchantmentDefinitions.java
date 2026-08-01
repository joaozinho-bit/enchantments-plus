package pt.joao.enchantmentsplus.enchantment;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;

/**
 * The definition shapes shared by the mod's enchantments.
 *
 * <p>An enchantment differs from its siblings in its weight, level cap and cost
 * range; which items it goes on and which slot it acts from follow from the
 * kind of gear it is for. Naming those kinds here means a new enchantment
 * states only its own numbers, and that two enchantments of the same kind can
 * never drift apart by accident.
 */
final class EnchantmentDefinitions {

	/** How much the enchanting cost window moves per level above the first. */
	private static final int COST_PER_LEVEL = 20;

	private EnchantmentDefinitions() {
	}

	/** Registers a swords-only enchantment that acts from the main hand. */
	static void sword(Registerable<Enchantment> registry, RegistryKey<Enchantment> key,
			int weight, int maxLevel, int minCost, int maxCost, int anvilCost) {
		register(registry, key, ItemTags.SWORD_ENCHANTABLE, AttributeModifierSlot.MAINHAND,
				weight, maxLevel, minCost, maxCost, anvilCost);
	}

	/** Registers an armour enchantment that acts from any of the four pieces. */
	static void armor(Registerable<Enchantment> registry, RegistryKey<Enchantment> key,
			int weight, int maxLevel, int minCost, int maxCost, int anvilCost) {
		register(registry, key, ItemTags.ARMOR_ENCHANTABLE, AttributeModifierSlot.ARMOR,
				weight, maxLevel, minCost, maxCost, anvilCost);
	}

	private static void register(Registerable<Enchantment> registry, RegistryKey<Enchantment> key,
			TagKey<Item> supportedItems, AttributeModifierSlot slot,
			int weight, int maxLevel, int minCost, int maxCost, int anvilCost) {
		registry.register(key, Enchantment.builder(Enchantment.definition(
						registry.getRegistryLookup(RegistryKeys.ITEM).getOrThrow(supportedItems),
						weight,
						maxLevel,
						Enchantment.leveledCost(minCost, COST_PER_LEVEL),
						Enchantment.leveledCost(maxCost, COST_PER_LEVEL),
						anvilCost,
						slot))
				.build(key.getValue()));
	}
}
