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
		mainHand(registry, key, ItemTags.SWORD_ENCHANTABLE, weight, maxLevel, minCost, maxCost, anvilCost);
	}

	/** Registers an enchantment for mining tools. */
	static void mining(Registerable<Enchantment> registry, RegistryKey<Enchantment> key,
			int weight, int maxLevel, int minCost, int maxCost, int anvilCost) {
		mainHand(registry, key, ItemTags.MINING_ENCHANTABLE, weight, maxLevel, minCost, maxCost, anvilCost);
	}

	/**
	 * Registers an enchantment that acts from whatever is held, for any set of
	 * items. Takes the tag directly, for the cases vanilla has no single tag
	 * that fits.
	 */
	static void mainHand(Registerable<Enchantment> registry, RegistryKey<Enchantment> key,
			TagKey<Item> supportedItems, int weight, int maxLevel, int minCost, int maxCost, int anvilCost) {
		register(registry, key, supportedItems, AttributeModifierSlot.MAINHAND,
				weight, maxLevel, minCost, maxCost, anvilCost);
	}

	/**
	 * Registers an enchantment for anything that wears out, acting from wherever
	 * the item happens to be.
	 *
	 * <p>Vanilla already names this set of items for Unbreaking and Mending, and
	 * it is the same question being asked &mdash; does this item have durability
	 * at all &mdash; so the tag is reused rather than restated. That covers tools,
	 * weapons, armour, elytra, shields and the odd ones out such as flint and
	 * steel, and it grows on its own as mods add to it.
	 *
	 * <p>Any slot, for the same reason: what is being changed belongs to the item
	 * rather than to the wearer, so where it is held cannot matter.
	 */
	static void durability(Registerable<Enchantment> registry, RegistryKey<Enchantment> key,
			int weight, int maxLevel, int minCost, int maxCost, int anvilCost) {
		register(registry, key, ItemTags.DURABILITY_ENCHANTABLE, AttributeModifierSlot.ANY,
				weight, maxLevel, minCost, maxCost, anvilCost);
	}

	/** Registers a boots-only enchantment. */
	static void boots(Registerable<Enchantment> registry, RegistryKey<Enchantment> key,
			int weight, int maxLevel, int minCost, int maxCost, int anvilCost) {
		register(registry, key, ItemTags.FOOT_ARMOR_ENCHANTABLE, AttributeModifierSlot.FEET,
				weight, maxLevel, minCost, maxCost, anvilCost);
	}

	/** Registers a helmet-only enchantment. */
	static void helmet(Registerable<Enchantment> registry, RegistryKey<Enchantment> key,
			int weight, int maxLevel, int minCost, int maxCost, int anvilCost) {
		register(registry, key, ItemTags.HEAD_ARMOR_ENCHANTABLE, AttributeModifierSlot.HEAD,
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
