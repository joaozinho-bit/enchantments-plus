package pt.joao.enchantmentsplus.client.hud;

/**
 * Every knob the HUD has, in one place.
 *
 * <p>Nothing here is read from disk yet: the point is that no layout number is
 * scattered through the drawing code, so wiring a config file later only means
 * filling {@link #INSTANCE} in once at start-up. The fields are plain and
 * mutable for exactly that reason, and each one is read live, so a change takes
 * effect on the next frame.
 *
 * <p>This is client-only and deliberately separate from
 * {@link pt.joao.enchantmentsplus.config.EnchantmentConfig}: the HUD is not an
 * enchantment and has neither an enabled flag per level nor a maximum level.
 */
public final class HudConfig {

	/** The live configuration used by the HUD. */
	public static final HudConfig INSTANCE = new HudConfig();

	/** How many indicators may be drawn at once; the rest are dropped. */
	public int maxVisible = 4;

	/** Screen corner the column grows from. */
	public HudAnchor anchor = HudAnchor.TOP_RIGHT;

	/** Distance from the horizontal screen edge, in scaled pixels. */
	public int marginX = 4;

	/** Distance from the vertical screen edge, in scaled pixels. */
	public int marginY = 4;

	/** Extra space between two lines, in scaled pixels. */
	public int lineSpacing = 2;

	/** Text colour, as {@code 0xRRGGBB}; the alpha comes from {@link #opacity}. */
	public int color = 0xFFFFFF;

	/** Text opacity, from fully transparent {@code 0} to opaque {@code 1}. */
	public float opacity = 0.85F;

	/** Whether the indicator's name is drawn next to its icon. */
	public boolean showLabels = false;

	/** Whether indicators that are only counting a cooldown down are drawn. */
	public boolean showCooldowns = true;

	/**
	 * Whether an indicator that expires on its own also shows how long it has
	 * left, as a bar after its value.
	 *
	 * <p>Skipped for timers and cooldowns, which already say it in words.
	 */
	public boolean showTimeBars = true;

	/** Number of segments in a progress bar. */
	public int barWidth = 10;

	/** Glyph used for a filled bar segment. */
	public String barFilled = "█";

	/** Glyph used for an empty bar segment. */
	public String barEmpty = "░";

	private HudConfig() {
	}
}
