/**
 * Gameplay event listeners, split by responsibility.
 *
 * <p>Instead of one large handler, each concern (attack, mining, damage, death,
 * equipment, tick, inventory, ...) gets its own small listener that reacts to
 * the relevant Fabric API event and delegates to the enchantments involved.
 * Events are preferred over per-tick polling for performance.
 */
package pt.joao.enchantmentsplus.event;
