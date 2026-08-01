package pt.joao.enchantmentsplus.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import pt.joao.enchantmentsplus.EnchantmentsPlus;

/**
 * Tells a client that the health drop it is about to see is not an injury.
 *
 * <p>When maximum health falls, vanilla clamps the current health down to it
 * and the client reads any drop the server reports as damage: it flashes the
 * screen red and tilts the camera. There is no way to send a smaller health
 * value that a vanilla client will not react to that way, and the client cannot
 * tell the two cases apart on its own without guessing at packet ordering.
 *
 * <p>So the server, which knows exactly when it lowered a maximum, simply says
 * so. The notice is sent the moment the maximum changes, which is always before
 * the health update that follows it on the same connection, so the client is
 * never left guessing.
 */
public final class HealthSync {

	private HealthSync() {
	}

	/** Registers the payload type. Call once from mod init, on both sides. */
	public static void init() {
		PayloadTypeRegistry.playS2C().register(MaxHealthLowered.ID, MaxHealthLowered.CODEC);
	}

	/**
	 * Warns a player that their maximum health just went down.
	 *
	 * @param player the player whose maximum changed
	 */
	public static void notifyLowered(ServerPlayerEntity player) {
		if (ServerPlayNetworking.canSend(player, MaxHealthLowered.ID)) {
			ServerPlayNetworking.send(player, MaxHealthLowered.INSTANCE);
		}
	}

	/** A bare signal: it carries no data beyond having been sent. */
	public record MaxHealthLowered() implements CustomPayload {

		public static final MaxHealthLowered INSTANCE = new MaxHealthLowered();

		public static final CustomPayload.Id<MaxHealthLowered> ID =
				new CustomPayload.Id<>(EnchantmentsPlus.id("max_health_lowered"));

		public static final PacketCodec<PacketByteBuf, MaxHealthLowered> CODEC =
				PacketCodec.unit(INSTANCE);

		@Override
		public CustomPayload.Id<? extends CustomPayload> getId() {
			return ID;
		}
	}
}
