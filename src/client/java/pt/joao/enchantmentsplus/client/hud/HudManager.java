package pt.joao.enchantmentsplus.client.hud;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.gui.DrawContext;
import pt.joao.enchantmentsplus.hud.HudValue;
import pt.joao.enchantmentsplus.networking.HudSync;

/**
 * Wires the HUD together and decides what actually reaches the screen.
 *
 * <p>It owns the lifecycle (events and packet handlers) and the selection step:
 * {@link HudState} says what exists and in which order, this class applies the
 * player's preferences and the visible-count limit, and {@link HudRenderer}
 * draws whatever survives. Splitting it this way is what keeps "how many, and
 * which ones" out of the drawing code.
 *
 * <p>Everything here reacts to events &mdash; no enchantment is ever polled,
 * and a frame with an empty HUD costs a single emptiness check.
 */
public final class HudManager {

	/** Reused between frames so a frame never allocates a selection list. */
	private static final List<HudState.Entry> VISIBLE = new ArrayList<>();

	private HudManager() {
	}

	/** Registers the render, tick and networking listeners. Call once from client init. */
	public static void init() {
		HudRenderCallback.EVENT.register(HudManager::render);
		ClientTickEvents.END_CLIENT_TICK.register(client -> HudState.tick());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> HudState.clear());

		ClientPlayNetworking.registerGlobalReceiver(HudSync.Show.ID,
				(payload, context) -> HudState.show(payload.indicator()));
		ClientPlayNetworking.registerGlobalReceiver(HudSync.Hide.ID,
				(payload, context) -> HudState.hide(payload.id()));
	}

	private static void render(DrawContext context, RenderTickCounter tickCounter) {
		List<HudState.Entry> entries = HudState.sorted();
		if (entries.isEmpty()) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden) {
			return;
		}

		HudConfig config = HudConfig.INSTANCE;
		VISIBLE.clear();
		for (HudState.Entry entry : entries) {
			if (VISIBLE.size() >= config.maxVisible) {
				break;
			}
			if (!config.showCooldowns && entry.indicator().value() instanceof HudValue.Cooldown) {
				continue;
			}
			VISIBLE.add(entry);
		}

		if (!VISIBLE.isEmpty()) {
			HudRenderer.draw(context, client.textRenderer, VISIBLE);
		}
	}
}
