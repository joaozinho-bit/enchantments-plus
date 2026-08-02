package pt.joao.enchantmentsplus.event;

import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import pt.joao.enchantmentsplus.config.EnchantmentConfig;
import pt.joao.enchantmentsplus.util.EnchantmentLevels;

/**
 * A block a player just broke, caught while its drops are still ours to change.
 *
 * <p>The list is the loot table's own output, so everything that decides
 * <em>what</em> and <em>how much</em> has already run: Fortune has multiplied,
 * Silk Touch has substituted, any mod's loot modifier has applied. An
 * enchantment here only rewrites or redirects the result, which is why several
 * of them can act on the same block without fighting each other.
 *
 * @param player the player who broke it
 * @param tool   the stack they broke it with
 * @param world  the world it was broken in
 * @param drops  the stacks about to be dropped; mutable on purpose
 */
public record MinedBlock(ServerPlayerEntity player, ItemStack tool, ServerWorld world, List<ItemStack> drops) {

	/**
	 * Resolves the level an enchantment is effectively acting at, for the tool
	 * behind this break.
	 *
	 * @param enchantment the enchantment to look for
	 * @param config      that enchantment's live configuration
	 * @return the level to act at, or {@code 0} to do nothing
	 */
	public int levelOf(RegistryKey<Enchantment> enchantment, EnchantmentConfig config) {
		return EnchantmentLevels.effective(world, tool, enchantment, config);
	}
}
