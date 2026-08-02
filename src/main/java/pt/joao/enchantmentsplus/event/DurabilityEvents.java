package pt.joao.enchantmentsplus.event;

import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import pt.joao.enchantmentsplus.enchantment.EternalEnchantment;

/**
 * Items about to wear out, and the enchantments that can spare them.
 *
 * <p>Vanilla funnels every point of durability loss through a single method, so
 * this is the one place the whole game asks the question &mdash; a hit, a block
 * broken, an arrow fired, a fall in an elytra, a dispenser using shears. An
 * enchantment that answers here is exempt from all of it without knowing any of
 * it.
 *
 * <p>Only the raising of damage passes through here. Repairs do not: the anvil,
 * the grindstone, Mending and crafting all set the damage directly, and stay
 * exactly as vanilla wrote them.
 *
 * <p>This fires for every damaged item on the server, so anything asked here
 * must be cheap and give up early.
 */
public final class DurabilityEvents {

	private DurabilityEvents() {
	}

	/**
	 * An item is about to take durability damage.
	 *
	 * <p>Called from {@code ItemStackMixin}, before vanilla has decided anything
	 * at all about the amount. Answering {@code true} means the wear never
	 * happens: the damage is not applied, the item cannot break from it, and
	 * nothing is told that it changed.
	 *
	 * @param world the server world the item is being used in
	 * @param stack the item about to be damaged; may be empty
	 * @return {@code true} when the damage should be ignored entirely
	 */
	public static boolean allowWear(ServerWorld world, ItemStack stack) {
		return !EternalEnchantment.preventsWear(world, stack);
	}
}
