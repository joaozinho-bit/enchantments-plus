package pt.joao.enchantmentsplus.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import pt.joao.enchantmentsplus.enchantment.ExcavatorEnchantment;
import pt.joao.enchantmentsplus.enchantment.MomentumEnchantment;

/**
 * The act of mining, and the enchantments that care about it.
 *
 * <p>Separate from {@link DropEvents}, which is about the items a break
 * produces: this one is about the break itself, and answers for enchantments
 * that reward the digging rather than change what comes out of it.
 *
 * <p>Both ends of a break are reported, because they mean different things. A
 * block finishing is an achievement worth rewarding; a block being started is
 * merely proof the player is still at it, which matters when a single swing can
 * last longer than a streak would otherwise survive.
 *
 * <p>The start is also the only moment the <em>face</em> is known. Vanilla
 * carries it in the packet that begins a break and never mentions it again, and
 * by the time a block has finished it is gone and cannot be re-derived by
 * looking. Anything that needs to know which way a player was digging &mdash; an
 * area, a direction, a tunnel &mdash; is therefore served by remembering it
 * here, once, for everyone.
 */
public final class MiningEvents {

	/** The face each player last struck; the only record of which way they are digging. */
	private static final Map<UUID, Direction> STRUCK_FACE = new HashMap<>();

	private MiningEvents() {
	}

	/** Registers the listeners. Call once from mod init. */
	public static void init() {
		PlayerBlockBreakEvents.AFTER.register(MiningEvents::onBlockBroken);
		AttackBlockCallback.EVENT.register(MiningEvents::onBlockStruck);
	}

	/**
	 * Forgets a player, for when they are already on their way out.
	 *
	 * @param player the player leaving
	 */
	public static void forget(ServerPlayerEntity player) {
		STRUCK_FACE.remove(player.getUuid());
	}

	private static void onBlockBroken(World world, PlayerEntity player, BlockPos pos,
			BlockState state, BlockEntity blockEntity) {
		if (!(player instanceof ServerPlayerEntity miner)) {
			return;
		}

		MomentumEnchantment.onMined(miner, miner.getMainHandStack());

		// No face means the break did not come from a player swinging at it: a
		// command, another mod, something scripted. Widening on a guess could
		// take out anything, so it simply does not.
		Direction face = STRUCK_FACE.get(miner.getUuid());
		if (face != null) {
			ExcavatorEnchantment.onMined(miner, pos, face);
		}
	}

	private static ActionResult onBlockStruck(PlayerEntity player, World world, Hand hand,
			BlockPos pos, Direction direction) {
		if (hand == Hand.MAIN_HAND && player instanceof ServerPlayerEntity miner) {
			STRUCK_FACE.put(miner.getUuid(), direction);
			MomentumEnchantment.onToolUsed(miner, miner.getMainHandStack());
		}
		// Never interferes with the break itself.
		return ActionResult.PASS;
	}
}
