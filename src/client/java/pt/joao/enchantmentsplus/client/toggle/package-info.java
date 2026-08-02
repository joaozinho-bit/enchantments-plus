/**
 * The client half of every manually switched enchantment: keys, watched.
 *
 * <p>{@link pt.joao.enchantmentsplus.client.toggle.ToggleKeys} owns the key
 * bindings and reports which one was pressed. Nothing is decided here &mdash;
 * whether the player is wearing the right piece, what the effect is and whether
 * it may run at all all belong to the server, which keeps a client from being
 * able to switch on something it has not earned.
 */
package pt.joao.enchantmentsplus.client.toggle;
