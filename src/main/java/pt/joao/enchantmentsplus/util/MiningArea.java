package pt.joao.enchantmentsplus.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

/**
 * Which blocks an area-mining enchantment reaches, and which of them count.
 *
 * <p>Pure geometry and a single question about one block: nothing here breaks
 * anything, knows about an enchantment or has any state. That is deliberate,
 * because both sides of the connection need exactly these answers and must
 * agree on them &mdash; the server to decide what to break, the client to decide
 * what to outline. Sharing the code is what makes it impossible for the outline
 * to promise a block that will not fall, or to miss one that will.
 *
 * <p>The shape is a flat square facing the player rather than a cube, which is
 * what makes it behave the way a shovel or a pickaxe should: dig into a wall and
 * it widens the wall, dig at your feet and it widens the floor.
 */
public final class MiningArea {

	private MiningArea() {
	}

	/**
	 * The blocks around the one that was hit, on the plane facing the player.
	 *
	 * <p>The plane is the one perpendicular to the face that was struck, so the
	 * two axes that vary are simply the two the face's own axis is not. Reading
	 * it off the face rather than off where the player is standing is what makes
	 * it predictable: a ceiling always widens sideways, never downwards.
	 *
	 * <p>The block that was hit is <em>not</em> included &mdash; it has already
	 * been broken by the time the server asks, and already has vanilla's own
	 * outline by the time the client asks.
	 *
	 * @param centre the block that was hit
	 * @param face   the face of it that was struck
	 * @param radius how far the area reaches, so {@code 1} is a 3&times;3
	 * @return the surrounding blocks, in no particular order
	 */
	public static List<BlockPos> around(BlockPos centre, Direction face, int radius) {
		int side = radius * 2 + 1;
		List<BlockPos> area = new ArrayList<>(side * side - 1);

		for (int a = -radius; a <= radius; a++) {
			for (int b = -radius; b <= radius; b++) {
				if (a == 0 && b == 0) {
					continue;
				}
				area.add(switch (face.getAxis()) {
					case X -> centre.add(0, a, b);
					case Y -> centre.add(a, 0, b);
					case Z -> centre.add(a, b, 0);
				});
			}
		}
		return area;
	}

	/**
	 * Whether an extra block belongs in the area at all.
	 *
	 * <p>Almost everything does. A swing takes the whole face it is aimed at, so
	 * a seam of ore in a stone wall or a band of dirt in a gravel bank comes out
	 * with the rest rather than being picked around &mdash; digging a hole and
	 * finding it full of leftovers is exactly what the enchantment is for
	 * avoiding.
	 *
	 * <p>The one thing kept out is what nothing can break: bedrock, barriers,
	 * portal frames and anything else a pack gives an unbreakable hardness to.
	 * Those are excluded here so the outline never draws them, but vanilla would
	 * refuse them anyway.
	 *
	 * <p>Says nothing about permission, protection or whether the tool is the
	 * right one. All of those are vanilla's to answer, and it still does, once
	 * per block.
	 *
	 * @param world the world the block is in
	 * @param pos   the block to judge
	 * @return whether the area should take it
	 */
	public static boolean isBreakable(BlockView world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		return !state.isAir() && state.getHardness(world, pos) >= 0.0F;
	}
}
