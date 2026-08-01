/**
 * The shared vocabulary the HUD is built on, known to both sides.
 *
 * <p>A {@link pt.joao.enchantmentsplus.hud.HudIndicator} is an immutable
 * snapshot of what an enchantment wants shown: an id, a
 * {@link pt.joao.enchantmentsplus.hud.HudPriority}, an optional icon and label,
 * and a {@link pt.joao.enchantmentsplus.hud.HudValue} carrying the measurable
 * part (timer, cooldown, progress, counter, state or nothing at all). That is
 * the complete contract &mdash; an enchantment never expresses layout, colours
 * or visibility, and the HUD never learns which enchantment a snapshot came
 * from.
 *
 * <p>Snapshots are pure data so they can be published on the client directly or
 * sent from the server through
 * {@link pt.joao.enchantmentsplus.networking.HudSync}; the drawing lives
 * entirely in the client-only {@code client.hud} package.
 */
package pt.joao.enchantmentsplus.hud;
