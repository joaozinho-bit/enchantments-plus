package pt.joao.enchantmentsplus.client.hud;

/**
 * Which screen corner the indicator column grows from.
 *
 * <p>Anchoring to a corner rather than to absolute coordinates keeps the column
 * out of the vanilla bars at any GUI scale, and makes the position a single
 * value the player can change later.
 */
public enum HudAnchor {

	TOP_LEFT,
	TOP_RIGHT,
	BOTTOM_LEFT,
	BOTTOM_RIGHT;

	/** @return {@code true} when lines are right-aligned against the edge */
	public boolean isRight() {
		return this == TOP_RIGHT || this == BOTTOM_RIGHT;
	}

	/** @return {@code true} when the column grows upwards from the edge */
	public boolean isBottom() {
		return this == BOTTOM_LEFT || this == BOTTOM_RIGHT;
	}
}
