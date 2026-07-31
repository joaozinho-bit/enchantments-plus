/**
 * One class per enchantment, each holding only its own specific behaviour.
 *
 * <p>There is deliberately no "god class" driving every enchantment. Shared
 * concerns (registration, configuration, temporary effects, events, HUD,
 * networking) are provided by the sibling packages, so an enchantment class
 * stays focused on what makes it unique.
 */
package pt.joao.enchantmentsplus.enchantment;
