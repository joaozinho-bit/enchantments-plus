package pt.joao.enchantmentsplus.event;

import java.util.function.Consumer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import pt.joao.enchantmentsplus.enchantment.AutoSmeltEnchantment;
import pt.joao.enchantmentsplus.enchantment.TelekinesisEnchantment;

/**
 * Items on their way out of a block or a corpse, and the enchantments that
 * rewrite or redirect them.
 *
 * <p>Every enchantment that touches drops plugs in here instead of hooking the
 * game itself, so the awkward parts &mdash; where vanilla can be intercepted,
 * which breaks belong to a player, keeping the loot table's own output intact
 * &mdash; are solved once.
 *
 * <p>The order below is the contract, and it is what makes the enchantments
 * compose: everything that <em>changes what the item is</em> runs before
 * anything that <em>decides where it goes</em>. Auto Smelt therefore always
 * sees the raw drop, and Telekinesis always sees the final one.
 */
public final class DropEvents {

	private DropEvents() {
	}

	/**
	 * A player broke a block and the loot table has produced its drops.
	 *
	 * <p>Called from {@code BlockMixin}. Whatever is left in
	 * {@link MinedBlock#drops()} afterwards is what actually falls on the
	 * ground, so removing a stack silently keeps it out of the world.
	 *
	 * @param mined the break, with its drops still mutable
	 */
	public static void onBlockMined(MinedBlock mined) {
		AutoSmeltEnchantment.onMined(mined);
		TelekinesisEnchantment.onMined(mined);
	}

	/**
	 * Something died and is about to hand its loot to the world.
	 *
	 * <p>Called from {@code LivingEntityMixin} to wrap the sink vanilla drops
	 * into, rather than to replace the loot itself &mdash; the table, the seed
	 * and Looting all still run untouched, and anything an enchantment does not
	 * claim falls through to the original behaviour.
	 *
	 * @param victim   the entity that died
	 * @param fallback what vanilla would have done with each stack
	 * @return the sink to use instead
	 */
	public static Consumer<ItemStack> onLootDropped(LivingEntity victim, Consumer<ItemStack> fallback) {
		if (!(victim.getAttacker() instanceof ServerPlayerEntity killer)) {
			return fallback;
		}

		ItemStack weapon = killer.getMainHandStack();
		return stack -> {
			if (!TelekinesisEnchantment.collect(killer, weapon, stack)) {
				fallback.accept(stack);
			}
		};
	}
}
