/**
 * Shared client-server synchronisation.
 *
 * <p>When an enchantment needs to sync state between sides it reuses the common
 * networking here instead of inventing a dedicated packet. This keeps the wire
 * format small and consistent as more enchantments are added.
 *
 * <p>{@link pt.joao.enchantmentsplus.networking.HudSync} is the first such
 * channel: it carries generic
 * {@link pt.joao.enchantmentsplus.hud.HudIndicator} snapshots from the
 * server-side state to the client HUD, so no enchantment ever needs a HUD
 * packet of its own.
 */
package pt.joao.enchantmentsplus.networking;
