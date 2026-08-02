package pt.joao.enchantmentsplus.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import pt.joao.enchantmentsplus.event.DropEvents;
import pt.joao.enchantmentsplus.event.MinedBlock;

/**
 * Hands a broken block's drops to the mod before they hit the ground.
 *
 * <p>Nothing in the Fabric API offers this: the break events fire either too
 * early to know the drops or too late to change them, and rebuilding the drop
 * list from scratch would mean reimplementing loot tables, Fortune and Silk
 * Touch. {@code getDroppedStacks} is instead the exact seam &mdash; the loot
 * table has just finished, the list has not been turned into entities yet, and
 * both the breaker and the tool are right there.
 *
 * <p>Injecting on the overload that takes an entity keeps this to blocks that
 * something actually broke; explosions and pistons go through the other one.
 */
@Mixin(Block.class)
public abstract class BlockMixin {

	@Inject(
			method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;"
					+ "Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;"
					+ "Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;",
			at = @At("RETURN"),
			cancellable = true)
	private static void enchantmentsPlus$modifyDrops(BlockState state, ServerWorld world, BlockPos pos,
			BlockEntity blockEntity, Entity breaker, ItemStack tool,
			CallbackInfoReturnable<List<ItemStack>> cir) {
		if (!(breaker instanceof ServerPlayerEntity player)) {
			return;
		}

		// Copied rather than edited in place: the list vanilla returns is its
		// own, and nothing promises it is safe to modify.
		List<ItemStack> drops = new ArrayList<>(cir.getReturnValue());
		DropEvents.onBlockMined(new MinedBlock(player, tool, world, drops));
		cir.setReturnValue(drops);
	}
}
