package pt.joao.enchantmentsplus.util;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;

/**
 * Breaks extra blocks as though the player had mined each one themselves.
 *
 * <p>The whole point is that it does almost nothing. Every block goes through
 * {@code ServerPlayerInteractionManager#tryBreakBlock}, which is the same method
 * vanilla calls when a player finishes mining, so protection checks, harvest
 * checks, block entities, experience, tool wear, Fortune, Silk Touch and every
 * event the mod already listens to all happen exactly as they would have. There
 * is no drop logic here, and there must never be: the moment an area miner
 * starts deciding what an ore drops, every enchantment that touches drops has to
 * be taught about it.
 *
 * <p>Because it goes through the real path, breaking a block reports a block
 * being broken, which would come straight back here. The guard is what stops
 * that: an area break is one flat pass, never a cascade. The server tick is a
 * single thread and the whole pass is synchronous, so one flag is enough to say
 * "already inside one".
 *
 * <p>Which blocks are worth trying is not decided here either. The caller hands
 * over a list; this only refuses what nothing could break at all.
 */
public final class AreaBreaker {

	private static boolean breaking;

	private AreaBreaker() {
	}

	/**
	 * @return whether an area break is already running, in which case the block
	 *         being reported is one of ours and must not widen again
	 */
	public static boolean isBreaking() {
		return breaking;
	}

	/**
	 * Breaks each position that the player's current tool still qualifies for.
	 *
	 * @param player    the miner, whose main hand pays for all of it
	 * @param positions the blocks to try, typically from {@link MiningArea}
	 */
	public static void breakAll(ServerPlayerEntity player, List<BlockPos> positions) {
		if (breaking) {
			return;
		}

		breaking = true;
		try {
			ServerWorld world = player.getServerWorld();
			// What they started the swing with. A tool that snaps mid-area must
			// end it: vanilla only checks whether a block can be harvested, so
			// carrying on bare-handed would shatter the rest for nothing.
			Item started = player.getMainHandStack().getItem();

			for (BlockPos pos : positions) {
				ItemStack tool = player.getMainHandStack();
				if (tool.isEmpty() || !tool.isOf(started)) {
					return;
				}
				if (!MiningArea.isBreakable(world, pos)) {
					continue;
				}

				// Read before the block is gone; the effect needs to know what it
				// was made of.
				BlockState state = world.getBlockState(pos);
				if (player.interactionManager.tryBreakBlock(pos)) {
					// vanilla's own break effect, the one World#breakBlock plays.
					// tryBreakBlock leaves it to the client's prediction, which
					// only ever covers the block actually aimed at, so without
					// this the rest of the area would vanish in silence.
					world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
				}
			}
		} finally {
			breaking = false;
		}
	}
}
