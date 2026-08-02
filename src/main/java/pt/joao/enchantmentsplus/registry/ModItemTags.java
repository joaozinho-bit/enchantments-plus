package pt.joao.enchantmentsplus.registry;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import pt.joao.enchantmentsplus.EnchantmentsPlus;

/**
 * Item tags the mod defines for itself.
 *
 * <p>Only for the cases vanilla has no tag for. A tag rather than a hardcoded
 * item list is what keeps the mod open: the generated file is built from
 * vanilla tags, so a modded pickaxe that is already {@code enchantable/mining}
 * is covered without anyone doing anything, and a pack can widen or narrow the
 * tag without touching code.
 */
public final class ModItemTags {

	/**
	 * What Telekinesis can be put on: mining tools and weapons.
	 *
	 * <p>Vanilla splits these into two tags and has none that spans both, which
	 * is the only reason this one exists.
	 */
	public static final TagKey<Item> TELEKINESIS_ENCHANTABLE =
			TagKey.of(RegistryKeys.ITEM, EnchantmentsPlus.id("telekinesis_enchantable"));

	/**
	 * What Momentum can be put on: pickaxes and shovels.
	 *
	 * <p>Narrower than {@code enchantable/mining}, which also takes axes, hoes
	 * and shears &mdash; sustained digging is the thing being rewarded.
	 */
	public static final TagKey<Item> MOMENTUM_ENCHANTABLE =
			TagKey.of(RegistryKeys.ITEM, EnchantmentsPlus.id("momentum_enchantable"));

	/**
	 * What Attack Speed can be put on: swords, and anything a pack decides
	 * belongs with them.
	 *
	 * <p>Ships as vanilla's sword tag, which is also where weapon mods such as
	 * Simply Swords register their blades, so they are covered without either
	 * side knowing about the other. Anything that is not sits one datapack line
	 * away from being added here.
	 */
	public static final TagKey<Item> ATTACK_SPEED_ENCHANTABLE =
			TagKey.of(RegistryKeys.ITEM, EnchantmentsPlus.id("attack_speed_enchantable"));

	/**
	 * What Excavator can be put on: pickaxes and shovels.
	 *
	 * <p>The same two families as {@link #MOMENTUM_ENCHANTABLE} and for the same
	 * reason &mdash; these are the tools that clear ground rather than fell,
	 * till or fight &mdash; but named separately so a pack can widen one without
	 * the other. Sharing the tag would quietly tie a swing's reach to a mining
	 * speed streak.
	 */
	public static final TagKey<Item> EXCAVATOR_ENCHANTABLE =
			TagKey.of(RegistryKeys.ITEM, EnchantmentsPlus.id("excavator_enchantable"));

	private ModItemTags() {
	}
}
