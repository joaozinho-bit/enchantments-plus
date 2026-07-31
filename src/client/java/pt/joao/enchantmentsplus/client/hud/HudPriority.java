package pt.joao.enchantmentsplus.client.hud;

/**
 * Relative importance of a {@link HudIndicator}.
 *
 * <p>When more indicators want to be shown than the HUD is willing to draw, the
 * ones with the highest priority win. Declaration order is significant: higher
 * in the list means more important.
 */
public enum HudPriority {

	/** Active effects, e.g. the Attack Speed window. */
	HIGH,

	/** Ongoing build-ups, e.g. a Jump being charged. */
	MEDIUM,

	/** Cooldowns waiting to expire. */
	LOW,

	/** Passive or permanent hints. */
	VERY_LOW
}
