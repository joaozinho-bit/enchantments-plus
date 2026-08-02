package pt.joao.enchantmentsplus.enchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registerable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import pt.joao.enchantmentsplus.EnchantmentsPlus;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.effect.ToggleManager;
import pt.joao.enchantmentsplus.effect.ToggleableEffect;
import pt.joao.enchantmentsplus.registry.ModEnchantments;
import pt.joao.enchantmentsplus.registry.ModItemTags;
import pt.joao.enchantmentsplus.util.AreaBreaker;
import pt.joao.enchantmentsplus.util.MiningShape;

/**
 * Excavator: a swing that can be told to take the whole face at once.
 *
 * <p>Always-on area mining would make careful digging impossible, and careful
 * digging is most of what a pickaxe is for &mdash; tunnelling a two-wide
 * corridor, cutting a staircase, taking one ore out of a wall. So it is armed by
 * hand, and while it is armed the player is shown exactly what a swing will
 * cost them before they take it.
 *
 * <p>There is deliberately almost nothing in this class. Whether the mode is on
 * belongs to {@link ToggleManager}, which blocks are reached belongs to
 * {@link MiningShape}, and breaking them belongs to {@link AreaBreaker}, which
 * does it through the same vanilla method a player's own swing goes through.
 * That last part is the whole design: nothing here knows what a block drops, so
 * Fortune, Silk Touch, Auto Smelt and Telekinesis are not accounted for
 * anywhere, and neither will be whatever is added next.
 *
 * <p>Levels buy reach. Every shape in {@link MiningShape} is available at the
 * first level except the larger squares, so a level one tool is already worth
 * having and a level three one is simply capable of more. Which shape is wanted
 * changes from minute to minute &mdash; a corridor to travel, a wide face to
 * clear a room &mdash; so it is chosen by double-tapping the same key rather
 * than by a second binding.
 *
 * <p>The choice belongs to the player and not to the tool, so swapping a pickaxe
 * for a shovel keeps both the mode and the switch. All that changes is whether
 * the thing in hand can act on them.
 */
public final class ExcavatorEnchantment {

	/** The toggle, the key binding and the client's outline are all this one thing. */
	public static final Identifier ID = EnchantmentsPlus.id("excavator");

	/** The shape each player has chosen; absent for everyone still on the default. */
	private static final Map<UUID, MiningShape> SHAPES = new HashMap<>();

	private static ConfigHolder<ExcavatorConfig> config;

	private ExcavatorEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration and its toggle. Call once from
	 * mod init, before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("excavator", new ExcavatorConfig());
		// Nothing to switch on in advance: the mode is a standing answer to a
		// question the next swing asks.
		ToggleManager.register(ToggleableEffect.cycled(
				ID, ModEnchantments.EXCAVATOR, EquipmentSlot.MAINHAND, config::get, new Shapes()));
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime. Pickaxes and shovels only, so it can never be rolled onto an axe,
	 * a hoe or a weapon.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		EnchantmentDefinitions.mainHand(registry, ModEnchantments.EXCAVATOR,
				ModItemTags.EXCAVATOR_ENCHANTABLE, 2, 3, 15, 65, 4);
	}

	/**
	 * A block just came out of the ground; widen the swing if the mode is armed.
	 *
	 * <p>Called from {@link pt.joao.enchantmentsplus.event.MiningEvents} for
	 * every block a player breaks, so it gives up on the first cheap check.
	 * Runs after the block that was hit is already gone, which is exactly right:
	 * that one has had its own turn through the full pipeline, and the rest are
	 * about to have theirs.
	 *
	 * @param player the miner
	 * @param pos    the block they broke
	 * @param face   the face of it they struck
	 */
	public static void onMined(ServerPlayerEntity player, BlockPos pos, Direction face) {
		// The blocks this widening is itself breaking arrive here too. One flat
		// pass is the whole feature; a cascade would be a world eater.
		if (AreaBreaker.isBreaking()) {
			return;
		}

		int level = ToggleManager.activeLevel(player, ID);
		if (level <= 0) {
			return;
		}

		AreaBreaker.breakAll(player, shapeOf(player, level).around(pos, face));
	}

	/**
	 * Forgets a player's chosen shape, for when they are already on their way
	 * out.
	 *
	 * @param player the player leaving
	 */
	public static void forget(ServerPlayerEntity player) {
		SHAPES.remove(player.getUuid());
	}

	/**
	 * @param player the player to ask about
	 * @param level  the level the tool in their hand grants
	 * @return the shape they have chosen, held to what that tool can manage
	 */
	private static MiningShape shapeOf(ServerPlayerEntity player, int level) {
		return MiningShape.clamp(SHAPES.getOrDefault(player.getUuid(), MiningShape.DEFAULT), level);
	}

	/**
	 * The player's choice of shape, exposed to
	 * {@link pt.joao.enchantmentsplus.effect.ToggleManager} as a mode.
	 *
	 * <p>The stored choice is deliberately never clamped on the way in, only on
	 * the way out: a 7&times;7 chosen with a good pickaxe is still remembered
	 * while a worse shovel is in hand, and comes back the moment the pickaxe
	 * does.
	 */
	private static final class Shapes implements ToggleableEffect.Modes {

		@Override
		public String current(ServerPlayerEntity player, int level) {
			return shapeOf(player, level).modeName();
		}

		@Override
		public String next(ServerPlayerEntity player, int level) {
			MiningShape next = MiningShape.next(shapeOf(player, level), level);
			SHAPES.put(player.getUuid(), next);
			return next.modeName();
		}
	}
}
