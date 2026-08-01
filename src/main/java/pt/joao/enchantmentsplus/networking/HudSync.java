package pt.joao.enchantmentsplus.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import pt.joao.enchantmentsplus.EnchantmentsPlus;
import pt.joao.enchantmentsplus.hud.HudIndicator;

/**
 * The one channel every HUD indicator travels on.
 *
 * <p>Temporary states live on the server (see
 * {@link pt.joao.enchantmentsplus.effect.TimedEffectManager}) while the HUD is
 * client-only, so an enchantment that wants to show one calls {@link #show} when
 * the state changes and {@link #hide} when there is nothing left to show. There
 * is no request/response and no per-tick stream: a snapshot carries the ticks
 * remaining and the client counts them down by itself, so a whole effect
 * normally costs two packets.
 *
 * <p>Because the payloads carry a generic
 * {@link pt.joao.enchantmentsplus.hud.HudIndicator}, a new enchantment reuses
 * this channel as-is instead of adding a packet of its own.
 */
public final class HudSync {

	private HudSync() {
	}

	/** Registers the payload types. Call once from mod init, on both sides. */
	public static void init() {
		PayloadTypeRegistry.playS2C().register(Show.ID, Show.CODEC);
		PayloadTypeRegistry.playS2C().register(Hide.ID, Hide.CODEC);
	}

	/**
	 * Shows (or replaces) an indicator on one player's HUD.
	 *
	 * @param player    the player to send it to
	 * @param indicator the snapshot to display
	 */
	public static void show(ServerPlayerEntity player, HudIndicator indicator) {
		if (ServerPlayNetworking.canSend(player, Show.ID)) {
			ServerPlayNetworking.send(player, new Show(indicator));
		}
	}

	/**
	 * Removes an indicator before its lifetime runs out. Not needed for a
	 * snapshot that expires on its own.
	 *
	 * @param player the player to send it to
	 * @param id     the indicator id used when it was shown
	 */
	public static void hide(ServerPlayerEntity player, Identifier id) {
		if (ServerPlayNetworking.canSend(player, Hide.ID)) {
			ServerPlayNetworking.send(player, new Hide(id));
		}
	}

	/** Publishes a snapshot on the receiving client's HUD. */
	public record Show(HudIndicator indicator) implements CustomPayload {

		public static final CustomPayload.Id<Show> ID =
				new CustomPayload.Id<>(EnchantmentsPlus.id("hud_show"));

		public static final PacketCodec<PacketByteBuf, Show> CODEC = CustomPayload.codecOf(
				(payload, buf) -> HudIndicator.write(buf, payload.indicator()),
				buf -> new Show(HudIndicator.read(buf)));

		@Override
		public CustomPayload.Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** Drops an indicator from the receiving client's HUD. */
	public record Hide(Identifier id) implements CustomPayload {

		public static final CustomPayload.Id<Hide> ID =
				new CustomPayload.Id<>(EnchantmentsPlus.id("hud_hide"));

		public static final PacketCodec<PacketByteBuf, Hide> CODEC = CustomPayload.codecOf(
				(payload, buf) -> buf.writeIdentifier(payload.id()),
				buf -> new Hide(buf.readIdentifier()));

		@Override
		public CustomPayload.Id<? extends CustomPayload> getId() {
			return ID;
		}
	}
}
