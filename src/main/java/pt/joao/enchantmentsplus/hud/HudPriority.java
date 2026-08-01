package pt.joao.enchantmentsplus.hud;

/**
 * Relative importance of a {@link HudIndicator}.
 *
 * <p>When more indicators want to be shown than the HUD is willing to draw, the
 * ones with the highest priority win. Declaration order is significant: higher
 * in the list means more important.
 *
 * <p>The scale is deliberately coarse and described in terms of <em>kinds of
 * state</em> rather than concrete enchantments, so any future indicator can pick
 * a level without the HUD having to learn about it.
 */
public enum HudPriority {

	/** Something is happening right now, e.g. a running effect. */
	HIGH,

	/** Something is building up, e.g. an ongoing charge. */
	MEDIUM,

	/** Something the player is only waiting for, e.g. a cooldown. */
	LOW,

	/** Passive or permanent hints. */
	VERY_LOW
}
