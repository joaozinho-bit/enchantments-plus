/**
 * Isolated integration code for third-party mods.
 *
 * <p>All mod-specific handling (for example Better Enchant or Simply Swords)
 * is kept here so that compatibility checks never leak across the rest of the
 * codebase. The mod always works standalone; anything in this package must
 * degrade gracefully when the target mod is absent.
 */
package pt.joao.enchantmentsplus.compatibility;
