package pt.joao.enchantmentsplus.client.health;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import pt.joao.enchantmentsplus.networking.HealthSync;

/**
 * Remembers, for a moment, that the server announced a drop in maximum health.
 *
 * <p>The announcement and the health update that follows it are separate
 * packets, so the notice opens a short window rather than applying to one exact
 * message. The window is only a few ticks wide and only ever opens when a
 * maximum actually changed, which is far more reliable than trying to infer the
 * same thing from the order the packets happen to arrive in.
 */
public final class HealthAdjustment {

	/**
	 * How long the notice stays valid. The health update follows within a tick
	 * or two on the same connection; anything longer would start swallowing
	 * real damage.
	 */
	private static final int WINDOW_TICKS = 4;

	private static int remaining;

	private HealthAdjustment() {
	}

	/** Registers the listeners. Call once from client init. */
	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(HealthSync.MaxHealthLowered.ID,
				(payload, context) -> remaining = WINDOW_TICKS);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (remaining > 0) {
				remaining--;
			}
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> remaining = 0);
	}

	/** @return {@code true} while an announced drop is still expected */
	public static boolean isPending() {
		return remaining > 0;
	}
}
