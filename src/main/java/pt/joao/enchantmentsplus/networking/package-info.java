/**
 * Shared client-server synchronisation.
 *
 * <p>When an enchantment needs to sync state between sides it reuses the common
 * networking here instead of inventing a dedicated packet. This keeps the wire
 * format small and consistent as more enchantments are added.
 */
package pt.joao.enchantmentsplus.networking;
