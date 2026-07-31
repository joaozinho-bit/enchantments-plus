/**
 * Stateless, reusable helpers kept separate from enchantment logic.
 *
 * <p>Cross-cutting utilities (maths, inventory, entities, particles, sounds,
 * ...) live here so they can be shared by any enchantment. A helper is added
 * only when a real caller needs it, never speculatively.
 */
package pt.joao.enchantmentsplus.util;
