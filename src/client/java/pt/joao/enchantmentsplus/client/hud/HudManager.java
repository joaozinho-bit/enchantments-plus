package pt.joao.enchantmentsplus.client.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

/**
 * Owns the whole HUD: enchantments register a {@link HudIndicator} once and this
 * class takes care of collecting, ordering, capping and drawing them.
 *
 * <p>The manager subscribes to Fabric's {@link HudRenderCallback}, so no mixin
 * is needed. Adding a new indicator never requires touching the drawing code.
 */
public final class HudManager {

	/** Upper bound on how many indicators are drawn at once, to keep the screen clean. */
	private static final int MAX_VISIBLE = 4;

	private static final int MARGIN = 4;
	private static final int LINE_GAP = 2;
	private static final int TEXT_COLOR = 0xFFFFFFFF;

	private static final List<HudIndicator> INDICATORS = new ArrayList<>();

	private HudManager() {
	}

	/** Registers the {@link HudRenderCallback} listener. Call once from client init. */
	public static void init() {
		HudRenderCallback.EVENT.register(HudManager::render);
	}

	/**
	 * Registers an indicator. Meant to be called at start-up; the instance is
	 * then queried every frame for its current state.
	 */
	public static void register(HudIndicator indicator) {
		INDICATORS.add(indicator);
	}

	private static void render(DrawContext context, RenderTickCounter tickCounter) {
		if (INDICATORS.isEmpty()) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden) {
			return;
		}

		List<HudIndicator> visible = new ArrayList<>();
		for (HudIndicator indicator : INDICATORS) {
			if (indicator.shouldRender()) {
				visible.add(indicator);
			}
		}
		if (visible.isEmpty()) {
			return;
		}

		visible.sort(Comparator.comparingInt(indicator -> indicator.priority().ordinal()));

		TextRenderer textRenderer = client.textRenderer;
		int lineHeight = textRenderer.fontHeight;
		int screenWidth = context.getScaledWindowWidth();
		int shown = Math.min(visible.size(), MAX_VISIBLE);

		for (int i = 0; i < shown; i++) {
			Text text = visible.get(i).text();
			int x = screenWidth - textRenderer.getWidth(text) - MARGIN;
			int y = MARGIN + i * (lineHeight + LINE_GAP);
			context.drawTextWithShadow(textRenderer, text, x, y, TEXT_COLOR);
		}
	}
}
