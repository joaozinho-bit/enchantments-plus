package pt.joao.enchantmentsplus.enchantment;

import java.util.Iterator;
import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registerable;
import net.minecraft.server.network.ServerPlayerEntity;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.event.MinedBlock;
import pt.joao.enchantmentsplus.registry.ModEnchantments;
import pt.joao.enchantmentsplus.registry.ModItemTags;
import pt.joao.enchantmentsplus.util.EnchantmentLevels;

/**
 * Telekinesis: what you earn goes straight into your pockets.
 *
 * <p>It changes one thing only &mdash; where the items end up. Quantities,
 * experience, loot tables, Fortune, Silk Touch and Looting have all finished
 * deciding before this ever runs, and it never adds, removes or rerolls a
 * single item.
 *
 * <p>A full inventory is not an error: whatever does not fit is handed back and
 * falls on the ground exactly as it would have, so nothing is ever lost.
 */
public final class TelekinesisEnchantment {

	private static ConfigHolder<TelekinesisConfig> config;

	private TelekinesisEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration. Call once from mod init,
	 * before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("telekinesis", new TelekinesisConfig());
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime. Convenience rather than power, so it is priced modestly.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		EnchantmentDefinitions.mainHand(registry, ModEnchantments.TELEKINESIS,
				ModItemTags.TELEKINESIS_ENCHANTABLE, 2, 1, 15, 65, 4);
	}

	/**
	 * Takes what fits from a mined block, leaving the rest to drop.
	 *
	 * @param mined the break, with its drops still mutable
	 */
	public static void onMined(MinedBlock mined) {
		if (mined.levelOf(ModEnchantments.TELEKINESIS, config.get()) <= 0) {
			return;
		}

		List<ItemStack> drops = mined.drops();
		Iterator<ItemStack> remaining = drops.iterator();
		while (remaining.hasNext()) {
			ItemStack stack = remaining.next();
			mined.player().getInventory().insertStack(stack);
			// insertStack shrinks the stack as it goes, so an empty one means
			// the whole thing was taken and there is nothing left to drop.
			if (stack.isEmpty()) {
				remaining.remove();
			}
		}
	}

	/**
	 * Takes what fits from a single stack of loot.
	 *
	 * @param player the player the loot belongs to
	 * @param tool   the stack they were holding
	 * @param stack  the loot, shrunk by however much was taken
	 * @return {@code true} when nothing is left and the caller should not drop it
	 */
	public static boolean collect(ServerPlayerEntity player, ItemStack tool, ItemStack stack) {
		if (EnchantmentLevels.effective(
				player.getWorld(), tool, ModEnchantments.TELEKINESIS, config.get()) <= 0) {
			return false;
		}

		player.getInventory().insertStack(stack);
		return stack.isEmpty();
	}
}
