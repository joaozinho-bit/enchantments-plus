/**
 * The client half of the HUD: everything that decides and draws.
 *
 * <p>{@link pt.joao.enchantmentsplus.client.hud.HudState} holds the
 * {@link pt.joao.enchantmentsplus.hud.HudIndicator} snapshots that are
 * currently live, ages them and drops them when they expire;
 * {@link pt.joao.enchantmentsplus.client.hud.HudManager} listens to the client
 * events, receives the snapshots sent by the server and picks which ones fit
 * within {@link pt.joao.enchantmentsplus.client.hud.HudConfig};
 * {@link pt.joao.enchantmentsplus.client.hud.HudFormatter} turns each one into
 * a line of text and
 * {@link pt.joao.enchantmentsplus.client.hud.HudRenderer} draws it.
 *
 * <p>The split matters: an enchantment only ever produces snapshots, so adding
 * one never touches this package, and changing how the HUD looks never touches
 * an enchantment.
 */
package pt.joao.enchantmentsplus.client.hud;
