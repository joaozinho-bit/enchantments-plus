/**
 * Showing the reach of an enchantment before it is used.
 *
 * <p>{@link pt.joao.enchantmentsplus.client.outline.AreaOutline} repeats
 * vanilla's own block outline over every block a widened swing would take, so
 * the player commits to nothing they were not shown. It is drawn from what the
 * client can already see &mdash; the crosshair, the held item, the preference
 * the server published &mdash; and decides nothing: the same shared geometry the
 * server breaks by is what says which blocks appear.
 */
package pt.joao.enchantmentsplus.client.outline;
