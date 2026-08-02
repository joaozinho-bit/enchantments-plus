package pt.joao.enchantmentsplus.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import pt.joao.enchantmentsplus.event.DurabilityEvents;

/**
 * Lets the mod refuse a point of durability before vanilla spends it.
 *
 * <p>Nothing in the Fabric API reaches this: its own {@code CustomDamageHandler}
 * is chosen per item, so it cannot be given to a vanilla pickaxe, and it is only
 * consulted when an entity is holding the item &mdash; a dispenser wearing out
 * shears never sees it.
 *
 * <p>This overload is the seam because everything else already funnels into it.
 * The two sibling {@code damage} methods delegate here, and every other way the
 * game touches the damage value is a repair rather than wear: the anvil, the
 * grindstone, crafting and Mending all set it directly, and mobs randomise it
 * when they spawn. Patching this one method therefore covers all wear and only
 * wear.
 *
 * <p>Injecting at the head is what keeps it honest. Vanilla itself returns from
 * this point when Unbreaking cancels out the damage, so declining here is the
 * path the game already has for "this hit costs nothing": no advancement is
 * told the item changed, and no break can follow.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@Inject(
			method = "damage(ILnet/minecraft/server/world/ServerWorld;"
					+ "Lnet/minecraft/server/network/ServerPlayerEntity;"
					+ "Ljava/util/function/Consumer;)V",
			at = @At("HEAD"),
			cancellable = true)
	private void enchantmentsPlus$preventWear(int amount, ServerWorld world, ServerPlayerEntity player,
			Consumer<Item> breakCallback, CallbackInfo ci) {
		if (!DurabilityEvents.allowWear(world, (ItemStack) (Object) this)) {
			ci.cancel();
		}
	}
}
