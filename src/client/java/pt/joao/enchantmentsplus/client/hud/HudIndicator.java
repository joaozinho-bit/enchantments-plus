package pt.joao.enchantmentsplus.client.hud;

import net.minecraft.text.Text;

/**
 * A single discreet HUD hint contributed by an enchantment.
 *
 * <p>The HUD system is deliberately unaware of any concrete enchantment: an
 * enchantment only describes <em>what</em> to show through this contract, and
 * {@link HudManager} decides <em>whether, where and in which order</em> to draw
 * it. Every planned indicator type from the design (a timer such as
 * {@code ⚡ 5.2s}, a counter such as {@code ⛏ x18} or a charge bar such as
 * {@code ██████░░░░}) is just a differently formatted {@link Text}, so this
 * text-based contract is enough to cover all of them.
 *
 * <p>Implementations are registered once at start-up and are queried every
 * frame, so {@link #shouldRender()} and {@link #text()} should read the current
 * client state cheaply and must not allocate more than necessary.
 */
public interface HudIndicator {

	/**
	 * @return {@code true} while this indicator is relevant and should be drawn
	 */
	boolean shouldRender();

	/**
	 * @return the already-formatted line to display, e.g. {@code ⚡ 5.2s}
	 */
	Text text();

	/**
	 * @return how important this indicator is relative to the others
	 */
	default HudPriority priority() {
		return HudPriority.MEDIUM;
	}
}
