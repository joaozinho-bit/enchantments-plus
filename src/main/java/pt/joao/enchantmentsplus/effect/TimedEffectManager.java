package pt.joao.enchantmentsplus.effect;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Server-side owner of every {@link TimedEffect}, one bucket per player.
 *
 * <p>The state lives on the server so it behaves identically in singleplayer
 * (integrated server) and on a dedicated server. Effects are advanced from the
 * end-of-tick event and only players that actually have running effects are
 * visited, so there is no per-player polling. A finished effect is dropped
 * immediately, which also means "is this effect running?" is simply "is it
 * present?".
 *
 * <p>The manager is generic: effects are keyed by an arbitrary
 * {@link Identifier} chosen by the enchantment, so this class never refers to a
 * specific enchantment, to the registry or to the HUD.
 */
public final class TimedEffectManager {

	private static final Map<UUID, Map<Identifier, TimedEffect>> EFFECTS = new HashMap<>();

	private TimedEffectManager() {
	}

	/** Registers the tick and cleanup listeners. Call once from mod init. */
	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(TimedEffectManager::tick);
		ServerPlayConnectionEvents.DISCONNECT.register(
				(handler, server) -> EFFECTS.remove(handler.player.getUuid()));
	}

	/**
	 * Starts (or restarts) an effect for a player.
	 *
	 * @param player       the player the effect runs for
	 * @param id           the effect key, unique per enchantment
	 * @param activeTicks  length of the active phase, in ticks ({@code 0} for a
	 *                     cooldown-only effect)
	 * @param cooldownTicks length of the cooldown afterwards, in ticks
	 *                      ({@code 0} for no cooldown)
	 * @param onTick       run on every tick of the active phase, e.g. to watch
	 *                      for a condition; may be {@code null}
	 * @param onEnd        run when the active phase ends, e.g. to undo a
	 *                      modifier; may be {@code null}
	 * @return the created effect
	 */
	public static TimedEffect start(ServerPlayerEntity player, Identifier id,
			int activeTicks, int cooldownTicks,
			Consumer<ServerPlayerEntity> onTick, Consumer<ServerPlayerEntity> onEnd) {
		TimedEffect effect = new TimedEffect(activeTicks, cooldownTicks, onTick, onEnd);
		EFFECTS.computeIfAbsent(player.getUuid(), uuid -> new HashMap<>()).put(id, effect);
		return effect;
	}

	/** Starts an effect that only runs an end callback. */
	public static TimedEffect start(ServerPlayerEntity player, Identifier id,
			int activeTicks, int cooldownTicks, Consumer<ServerPlayerEntity> onEnd) {
		return start(player, id, activeTicks, cooldownTicks, null, onEnd);
	}

	/**
	 * @return the running effect for this player, or empty if it is neither
	 *         active nor on cooldown (i.e. ready to be triggered again)
	 */
	public static Optional<TimedEffect> get(ServerPlayerEntity player, Identifier id) {
		Map<Identifier, TimedEffect> effects = EFFECTS.get(player.getUuid());
		return effects == null ? Optional.empty() : Optional.ofNullable(effects.get(id));
	}

	/** Removes an effect immediately, without running its end callback. */
	public static void cancel(ServerPlayerEntity player, Identifier id) {
		Map<Identifier, TimedEffect> effects = EFFECTS.get(player.getUuid());
		if (effects != null && effects.remove(id) != null && effects.isEmpty()) {
			EFFECTS.remove(player.getUuid());
		}
	}

	private static void tick(MinecraftServer server) {
		if (EFFECTS.isEmpty()) {
			return;
		}
		Iterator<Map.Entry<UUID, Map<Identifier, TimedEffect>>> players = EFFECTS.entrySet().iterator();
		while (players.hasNext()) {
			Map.Entry<UUID, Map<Identifier, TimedEffect>> entry = players.next();
			ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
			if (player == null) {
				players.remove();
				continue;
			}
			entry.getValue().values().removeIf(effect -> !effect.tick(player));
			if (entry.getValue().isEmpty()) {
				players.remove();
			}
		}
	}
}
