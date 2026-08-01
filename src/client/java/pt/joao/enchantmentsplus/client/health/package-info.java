/**
 * Client-side handling of health changes that are not injuries.
 *
 * <p>Vanilla draws every drop in health the server reports as damage. When the
 * drop is only a maximum-health adjustment the server says so first, and
 * {@link pt.joao.enchantmentsplus.client.health.HealthAdjustment} holds that
 * notice open long enough for the health update behind it to be recognised.
 * The value itself is always applied; only the presentation changes.
 */
package pt.joao.enchantmentsplus.client.health;
