package pt.joao.enchantmentsplus.client.hud;

import java.util.List;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Puts the chosen indicators on screen, and nothing else.
 *
 * <p>Deliberately plain: one shadowed line per indicator, stacked from the
 * configured corner. Keeping the drawing this small is what lets the rest of
 * the HUD stay about <em>what</em> to show; anything richer (fades, scaling,
 * textured icons) can be added here without touching a single enchantment.
 */
final class HudRenderer {

	private HudRenderer() {
	}

	/**
	 * @param context      the frame being drawn
	 * @param textRenderer the client font
	 * @param entries      the already selected and ordered indicators
	 */
	static void draw(DrawContext context, TextRenderer textRenderer, List<HudState.Entry> entries) {
		HudConfig config = HudConfig.INSTANCE;
		int color = color(config);
		int lineHeight = textRenderer.fontHeight;
		int step = lineHeight + config.lineSpacing;

		for (int i = 0; i < entries.size(); i++) {
			HudState.Entry entry = entries.get(i);
			Text text = HudFormatter.format(entry.indicator(), entry.elapsedTicks());

			int x = config.anchor.isRight()
					? context.getScaledWindowWidth() - config.marginX - textRenderer.getWidth(text)
					: config.marginX;
			int y = config.anchor.isBottom()
					? context.getScaledWindowHeight() - config.marginY - lineHeight - i * step
					: config.marginY + i * step;

			context.drawTextWithShadow(textRenderer, text, x, y, color);
		}
	}

	/** Folds the configured opacity into the colour as its alpha channel. */
	private static int color(HudConfig config) {
		int alpha = Math.round(Math.clamp(config.opacity, 0.0F, 1.0F) * 255.0F);
		return (alpha << 24) | (config.color & 0xFFFFFF);
	}
}
