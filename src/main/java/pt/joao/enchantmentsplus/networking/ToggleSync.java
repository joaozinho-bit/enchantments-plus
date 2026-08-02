package pt.joao.enchantmentsplus.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import pt.joao.enchantmentsplus.EnchantmentsPlus;
import pt.joao.enchantmentsplus.effect.ToggleManager;

/**
 * The one channel every manually switched effect travels on.
 *
 * <p>Like {@link JumpSync} it reports an input and never a decision: the payload
 * says which key was pressed and nothing else. Whether the player is wearing the
 * right piece, whether the enchantment is switched on, what level it is and what
 * the effect actually does are all settled on the server, so there is nothing
 * here a client could lie about to gain anything &mdash; the worst a forged
 * packet can do is flip a toggle the sender was entitled to flip anyway.
 *
 * <p>Because the payloads carry a plain {@link Identifier}, a new toggleable
 * enchantment reuses this channel as-is instead of adding a packet of its own.
 *
 * <p>The answer travels back for one reason only: an effect the player can
 * <em>see</em> before it happens. An attribute or a status effect needs nothing,
 * because vanilla already syncs those, but a client cannot draw the outline of
 * an area it does not know is armed. So the server states the preference it just
 * recorded, and the client keeps a copy purely to decide what to draw &mdash; it
 * is never asked, and never allowed, to act on it.
 */
public final class ToggleSync {

	private ToggleSync() {
	}

	/** Registers the payload types and the server-side receiver. Call once from mod init. */
	public static void init() {
		PayloadTypeRegistry.playC2S().register(Toggle.ID, Toggle.CODEC);
		PayloadTypeRegistry.playS2C().register(State.ID, State.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(Toggle.ID,
				(payload, context) -> ToggleManager.toggle(context.player(), payload.effect()));
	}

	/**
	 * Tells a player what their preference now is, so their client can draw it.
	 *
	 * <p>Sent only when it changes, which is the only time it can: nothing else
	 * on the server ever writes a preference.
	 *
	 * @param player  the player whose preference changed
	 * @param effect  the effect that changed
	 * @param enabled what it changed to
	 * @param mode    the mode it is on, or empty for effects that have none
	 */
	public static void publish(ServerPlayerEntity player, Identifier effect, boolean enabled, String mode) {
		if (ServerPlayNetworking.canSend(player, State.ID)) {
			ServerPlayNetworking.send(player, new State(effect, enabled, mode));
		}
	}

	/**
	 * A toggle key was pressed.
	 *
	 * @param effect the id of the effect the key belongs to
	 */
	public record Toggle(Identifier effect) implements CustomPayload {

		public static final CustomPayload.Id<Toggle> ID =
				new CustomPayload.Id<>(EnchantmentsPlus.id("toggle"));

		public static final PacketCodec<PacketByteBuf, Toggle> CODEC = CustomPayload.codecOf(
				(payload, buf) -> buf.writeIdentifier(payload.effect()),
				buf -> new Toggle(buf.readIdentifier()));

		@Override
		public CustomPayload.Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/**
	 * The server's record of what the player has switched on.
	 *
	 * @param effect  the id of the effect
	 * @param enabled whether it is now switched on
	 * @param mode    the mode it is on, or empty for effects that have none
	 */
	public record State(Identifier effect, boolean enabled, String mode) implements CustomPayload {

		/** Long enough for any mode name, short enough that a forged one is harmless. */
		private static final int MAX_MODE_LENGTH = 64;

		public static final CustomPayload.Id<State> ID =
				new CustomPayload.Id<>(EnchantmentsPlus.id("toggle_state"));

		public static final PacketCodec<PacketByteBuf, State> CODEC = CustomPayload.codecOf(
				(payload, buf) -> {
					buf.writeIdentifier(payload.effect());
					buf.writeBoolean(payload.enabled());
					buf.writeString(payload.mode(), MAX_MODE_LENGTH);
				},
				buf -> new State(buf.readIdentifier(), buf.readBoolean(),
						buf.readString(MAX_MODE_LENGTH)));

		@Override
		public CustomPayload.Id<? extends CustomPayload> getId() {
			return ID;
		}
	}
}
