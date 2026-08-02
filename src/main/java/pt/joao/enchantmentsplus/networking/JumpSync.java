package pt.joao.enchantmentsplus.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import pt.joao.enchantmentsplus.EnchantmentsPlus;
import pt.joao.enchantmentsplus.enchantment.JumpEnchantment;

/**
 * Carries the one thing only the client knows: whether a key is being held.
 *
 * <p>The mod's only client-to-server channel, and deliberately the thinnest
 * possible. It reports an input, never a decision &mdash; how long the charge
 * has run, how strong the jump is and whether it is allowed at all are all
 * worked out on the server, which then has nothing to trust the client about
 * beyond a key going down and back up.
 *
 * <p>Sent only on a change, so a whole charge costs two packets.
 */
public final class JumpSync {

	private JumpSync() {
	}

	/** Registers the payload type and the server-side receiver. Call once from mod init. */
	public static void init() {
		PayloadTypeRegistry.playC2S().register(Charging.ID, Charging.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(Charging.ID,
				(payload, context) -> JumpEnchantment.onChargeInput(context.player(), payload.charging()));
	}

	/**
	 * The charge key went down or came up.
	 *
	 * @param charging {@code true} when the key was pressed, {@code false} when released
	 */
	public record Charging(boolean charging) implements CustomPayload {

		public static final CustomPayload.Id<Charging> ID =
				new CustomPayload.Id<>(EnchantmentsPlus.id("jump_charging"));

		public static final PacketCodec<PacketByteBuf, Charging> CODEC = CustomPayload.codecOf(
				(payload, buf) -> buf.writeBoolean(payload.charging()),
				buf -> new Charging(buf.readBoolean()));

		@Override
		public CustomPayload.Id<? extends CustomPayload> getId() {
			return ID;
		}
	}
}
