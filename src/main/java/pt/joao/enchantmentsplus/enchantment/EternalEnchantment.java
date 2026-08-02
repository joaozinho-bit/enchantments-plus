package pt.joao.enchantmentsplus.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registerable;
import net.minecraft.server.world.ServerWorld;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.registry.ModEnchantments;
import pt.joao.enchantmentsplus.util.EnchantmentLevels;

/**
 * Eternal: the item simply stops wearing out.
 *
 * <p>Deliberately not vanilla's {@code Unbreakable}. That flag is a property of
 * the stack, so it survives the enchantment being ground off, it cannot be
 * switched off by configuration, and it leaves an item that reads as a creative
 * one. Refusing the wear at the moment it would be applied instead leaves an
 * ordinary stack that happens never to be damaged &mdash; take the enchantment
 * away and it is exactly the item it was.
 *
 * <p>Damage already on the item is kept, not healed. Repairing on top of an
 * enchantment would make Eternal a free anvil, and would throw away a value the
 * player gets back the moment they remove it at a grindstone. Only <em>new</em>
 * wear is refused.
 *
 * <p>Everything else about durability is left alone on purpose: Mending still
 * repairs, Unbreaking still applies, and the anvil and grindstone still do their
 * arithmetic. Those all read or lower the damage; this only ever declines to
 * raise it.
 */
public final class EternalEnchantment {

	private static ConfigHolder<EternalConfig> config;

	private EternalEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration. Call once from mod init,
	 * before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("eternal", new EternalConfig());
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime. As rare and as costly as the roster gets: retiring an entire
	 * vanilla system for one item should be the last thing a table ever offers.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		EnchantmentDefinitions.durability(registry, ModEnchantments.ETERNAL, 1, 1, 30, 80, 8);
	}

	/**
	 * Decides whether this item should shrug off the wear it is about to take.
	 *
	 * <p>Called from {@link pt.joao.enchantmentsplus.event.DurabilityEvents} for
	 * every point of damage any item would take, so it answers from the stack in
	 * hand and keeps no state at all: an item that gains or loses the enchantment
	 * is protected, or stops being protected, from that instant.
	 *
	 * @param world the server world the item is being used in
	 * @param stack the item about to be damaged; may be empty
	 * @return {@code true} when the damage should be ignored entirely
	 */
	public static boolean preventsWear(ServerWorld world, ItemStack stack) {
		return EnchantmentLevels.effective(world, stack, ModEnchantments.ETERNAL, config.get()) > 0;
	}

	/**
	 * Whether this item should keep its durability to itself.
	 *
	 * <p>An item that cannot wear out has no business drawing a bar that says how
	 * close it is to breaking: the number is real, but what it implies is not,
	 * and a player reading it is being told something false about their gear.
	 *
	 * <p>Deliberately says nothing about the damage stored on the item. Hiding
	 * only while the value happens to be zero would put the bar back the moment
	 * an enchantment is applied to something already worn &mdash; which is
	 * precisely when the reassurance is wanted. The damage is kept, untouched and
	 * unhidden the instant the enchantment comes off.
	 *
	 * <p>Called while drawing, so it is asked about every stack on screen, every
	 * frame; it reads the stack's own components and never the registry.
	 *
	 * @param stack the item being drawn; may be empty
	 * @return {@code true} when nothing about its durability should be shown
	 */
	public static boolean hidesDurability(ItemStack stack) {
		return EnchantmentLevels.effective(stack, ModEnchantments.ETERNAL, config.get()) > 0;
	}
}
