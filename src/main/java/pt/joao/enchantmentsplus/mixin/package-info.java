/**
 * The few places vanilla has to be patched directly.
 *
 * <p>A mixin is the last resort here, used only where neither the Fabric API
 * nor the data-driven enchantment system exposes what an enchantment needs.
 * Each one stays as small as possible: it observes a vanilla decision and
 * forwards it to a listener in {@code event}, never containing gameplay logic
 * of its own, so the enchantments stay testable and free of mixin details.
 */
package pt.joao.enchantmentsplus.mixin;
