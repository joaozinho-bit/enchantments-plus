package pt.joao.enchantmentsplus.client.jump;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import pt.joao.enchantmentsplus.networking.JumpSync;
import pt.joao.enchantmentsplus.registry.ModEnchantments;
import pt.joao.enchantmentsplus.util.EnchantmentLevels;

/**
 * Turns crouching with the jump key held into a charge.
 *
 * <p>The jump key alone stays exactly as vanilla left it. That matters more
 * than it sounds: telling a tap from a hold on one key means either withholding
 * the jump until the key comes up, which everyone feels as input lag, or
 * letting it jump first and charging afterwards, which throws in a hop nobody
 * asked for. Asking for the crouch as well sidesteps the question entirely
 * &mdash; the gesture is unambiguous the moment it starts, so nothing has to be
 * guessed, delayed or undone.
 *
 * <p>The cost is that a player wearing these boots cannot crouch-jump; that is
 * the whole of it. Every other use of the jump key, including swimming up,
 * gliding and flying, is untouched.
 */
public final class JumpInput {

	private static boolean charging;

	private JumpInput() {
	}

	/** Registers the listeners. Call once from client init. */
	public static void init() {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> setCharging(false));
	}

	/**
	 * Reads the crouch-and-jump gesture out of the movement input.
	 *
	 * <p>Called from {@code KeyboardInputMixin} straight after vanilla reads the
	 * keys, the only moment the jump can still be turned into something else.
	 *
	 * @param input the movement input to correct
	 */
	public static void filterJump(Input input) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null || !input.jumping || !input.sneaking || !appliesTo(player)) {
			setCharging(false);
			return;
		}

		// Crouched and pressing jump: a wind-up, not a jump.
		input.jumping = false;
		setCharging(true);
	}

	private static void setCharging(boolean value) {
		if (value == charging) {
			return;
		}
		charging = value;

		if (ClientPlayNetworking.canSend(JumpSync.Charging.ID)) {
			ClientPlayNetworking.send(new JumpSync.Charging(charging));
		}
	}

	/**
	 * Whether this player's jump key is ours to interpret at all.
	 *
	 * <p>Reads the enchantment level only, never the configuration, which
	 * belongs to the server; the server checks properly before acting. The rest
	 * are states where the jump key already means something &mdash; rising
	 * through water, gliding, riding, and flying, where it is the only way up
	 * and must never be taken away.
	 */
	private static boolean appliesTo(ClientPlayerEntity player) {
		return !player.isTouchingWater()
				&& !player.isFallFlying()
				&& !player.hasVehicle()
				&& !player.getAbilities().flying
				&& EnchantmentLevels.level(player.getWorld(),
						player.getEquippedStack(EquipmentSlot.FEET), ModEnchantments.JUMP) > 0;
	}
}
