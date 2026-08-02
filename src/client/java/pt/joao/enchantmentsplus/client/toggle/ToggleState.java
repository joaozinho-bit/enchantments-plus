package pt.joao.enchantmentsplus.client.toggle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import pt.joao.enchantmentsplus.networking.ToggleSync;

/**
 * The client's copy of what the server says this player has switched on.
 *
 * <p>Kept for one purpose: drawing. An effect the player can see the reach of
 * before they commit to it &mdash; an outline, a highlight &mdash; has to be
 * drawn every frame, long before the server is told anything, so the client
 * needs to know the mode is armed. Nothing here is ever acted on: it decides
 * what is shown and never what happens.
 *
 * <p>The copy only changes when the server says so, never when a key is pressed,
 * which is what keeps it honest. A press the server refuses &mdash; the wrong
 * tool, the enchantment switched off in the config &mdash; produces no reply and
 * therefore no change, so the outline never appears for something that would not
 * work.
 */
public final class ToggleState {

	private static final Set<Identifier> ENABLED = new HashSet<>();

	/** The mode each effect is on, for the ones that have modes. */
	private static final Map<Identifier, String> MODES = new HashMap<>();

	private ToggleState() {
	}

	/** Registers the listeners. Call once from client init. */
	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(ToggleSync.State.ID, (payload, context) -> {
			if (payload.enabled()) {
				ENABLED.add(payload.effect());
			} else {
				ENABLED.remove(payload.effect());
			}
			MODES.put(payload.effect(), payload.mode());
		});

		// Preferences live only as long as the connection, on both sides.
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			ENABLED.clear();
			MODES.clear();
		});
	}

	/**
	 * @param effect the effect to ask about
	 * @return whether the server has this player's preference switched on
	 */
	public static boolean isEnabled(Identifier effect) {
		return ENABLED.contains(effect);
	}

	/**
	 * @param effect the effect to ask about
	 * @return the mode the server last said it was on, or empty if it has never
	 *         said &mdash; in which case the effect's own default applies
	 */
	public static String mode(Identifier effect) {
		return MODES.getOrDefault(effect, "");
	}
}
