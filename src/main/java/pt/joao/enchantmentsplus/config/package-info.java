/**
 * Reusable, per-enchantment configuration.
 *
 * <p>{@link pt.joao.enchantmentsplus.config.EnchantmentConfig} carries the
 * options shared by every enchantment; each enchantment subclasses it to add
 * its own. {@link pt.joao.enchantmentsplus.config.ConfigManager} registers,
 * loads and persists them all in a single {@code enchantments-plus.json},
 * returning a {@link pt.joao.enchantmentsplus.config.ConfigHolder} for live
 * access. Adding an enchantment means writing its config subclass and one
 * {@code register} call &mdash; no other system changes.
 *
 * <p>Anything that can sensibly be tuned belongs here rather than being
 * hardcoded.
 */
package pt.joao.enchantmentsplus.config;
