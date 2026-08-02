/**
 * The client half of the Jump enchantment: one key, watched.
 *
 * <p>{@link pt.joao.enchantmentsplus.client.jump.JumpInput} owns the key
 * binding and reports when it goes down and comes back up. Nothing is measured
 * or decided here &mdash; the charge, the height and whether any of it is
 * allowed all belong to the server, which keeps a client from being able to
 * claim a jump it did not earn.
 */
package pt.joao.enchantmentsplus.client.jump;
