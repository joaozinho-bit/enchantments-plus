package pt.joao.enchantmentsplus.util;

import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * The shapes a widened swing can take, and what each one costs to unlock.
 *
 * <p>One catalogue, read by the server to decide what to break and by the client
 * to decide what to outline, so the two cannot disagree about either the shape
 * or whether the player is allowed it.
 *
 * <p>The order below is the order they cycle in, deliberately smallest first: a
 * player stepping through the modes passes the careful ones before the greedy
 * ones, and stops at the first that suits the job.
 *
 * <p>Levels buy reach and nothing else. Everything is available at the first
 * level except the larger squares, so a level one tool is already useful and a
 * level three one is simply capable of more.
 */
public enum MiningShape {

	/** Just the block that was hit, exactly as an unenchanted tool behaves. */
	SINGLE("single", 1),

	/** The block hit and the one above it: a corridor tall enough to walk down. */
	CORRIDOR("corridor", 1),

	/** The 3&times;3 facing the player. */
	SQUARE_3("square_3", 1),

	/** The 5&times;5 facing the player. */
	SQUARE_5("square_5", 2),

	/** The 7&times;7 facing the player. */
	SQUARE_7("square_7", 3);

	/** What a player gets before they have chosen anything. */
	public static final MiningShape DEFAULT = SQUARE_3;

	private final String modeName;
	private final int requiredLevel;

	MiningShape(String modeName, int requiredLevel) {
		this.modeName = modeName;
		this.requiredLevel = requiredLevel;
	}

	/** @return the name this shape travels and is translated under */
	public String modeName() {
		return modeName;
	}

	/** @return the lowest enchantment level that unlocks it */
	public int requiredLevel() {
		return requiredLevel;
	}

	/**
	 * The blocks this shape reaches around the one that was hit.
	 *
	 * <p>Never includes the block that was hit: the server has already broken it
	 * and the client already has vanilla's outline on it.
	 *
	 * @param centre the block that was hit
	 * @param face   the face of it that was struck
	 * @return the surrounding blocks, possibly none
	 */
	public List<BlockPos> around(BlockPos centre, Direction face) {
		return switch (this) {
			case SINGLE -> List.of();
			// Always upwards, whichever face was struck. Digging into a wall at
			// foot height therefore opens a walkable corridor, and the rule stays
			// one a player can hold in their head.
			case CORRIDOR -> List.of(centre.up());
			case SQUARE_3 -> MiningArea.around(centre, face, 1);
			case SQUARE_5 -> MiningArea.around(centre, face, 2);
			case SQUARE_7 -> MiningArea.around(centre, face, 3);
		};
	}

	/**
	 * @param modeName a name from {@link #modeName()}, possibly stale or empty
	 * @return the shape it names, or {@link #DEFAULT} for anything unrecognised
	 */
	public static MiningShape byName(String modeName) {
		for (MiningShape shape : values()) {
			if (shape.modeName.equals(modeName)) {
				return shape;
			}
		}
		return DEFAULT;
	}

	/**
	 * Holds a shape to what the tool in hand can actually manage.
	 *
	 * <p>Chosen with a level three pickaxe and then carried on a level one
	 * shovel, a shape has to give way rather than be honoured, or the outline
	 * would promise a reach the swing does not have.
	 *
	 * @param shape the shape the player has chosen
	 * @param level the level the tool grants
	 * @return the chosen shape, or the default when it is out of reach
	 */
	public static MiningShape clamp(MiningShape shape, int level) {
		return shape.requiredLevel <= level ? shape : DEFAULT;
	}

	/**
	 * The next shape the player can reach, wrapping around.
	 *
	 * @param current the shape they are on
	 * @param level   the level the tool grants
	 * @return the next unlocked shape, or {@code current} if it is the only one
	 */
	public static MiningShape next(MiningShape current, int level) {
		MiningShape[] all = values();
		for (int step = 1; step <= all.length; step++) {
			MiningShape candidate = all[(current.ordinal() + step) % all.length];
			if (candidate.requiredLevel <= level) {
				return candidate;
			}
		}
		return current;
	}
}
