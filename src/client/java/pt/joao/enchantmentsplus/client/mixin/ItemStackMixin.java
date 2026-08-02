package pt.joao.enchantmentsplus.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemStack;
import pt.joao.enchantmentsplus.enchantment.EternalEnchantment;

/**
 * Keeps the durability bar off items that cannot wear out.
 *
 * <p>Nothing in the Fabric API reaches the bar: there are events for tooltips
 * and for whole-item rendering, but the overlay a slot draws on top of a stack
 * is vanilla's alone. {@code isItemBarVisible} is nonetheless the exact
 * question being asked, and {@code DrawContext} is its only caller, so
 * answering it settles the bar everywhere it is drawn &mdash; hotbar, inventory,
 * chests, hoppers and every screen a mod builds out of the same slots.
 *
 * <p>Deliberately <em>not</em> a patch of {@code isDamaged}, which would be one
 * line shorter and would quietly break the game: Mending picks its item by it,
 * crafting matches ingredients by it, and the inventory stacks by it. The bar is
 * the only thing that should change.
 *
 * <p>Purely cosmetic and purely local. The damage is still on the stack, still
 * sent by the server and still read by everything else; only the drawing of it
 * is declined.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@Inject(method = "isItemBarVisible", at = @At("HEAD"), cancellable = true)
	private void enchantmentsPlus$hideDurabilityBar(CallbackInfoReturnable<Boolean> cir) {
		if (EternalEnchantment.hidesDurability((ItemStack) (Object) this)) {
			cir.setReturnValue(false);
		}
	}
}
