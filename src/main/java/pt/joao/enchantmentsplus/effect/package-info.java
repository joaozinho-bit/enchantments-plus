/**
 * Generic timed-effect system for enchantments with temporary states.
 *
 * <p>{@link pt.joao.enchantmentsplus.effect.TimedEffectManager} keeps, per
 * player, the {@link pt.joao.enchantmentsplus.effect.TimedEffect}s that are
 * currently running and advances them from the server tick, exposing an
 * active phase, a cooldown and the shared
 * {@link pt.joao.enchantmentsplus.effect.EffectState} vocabulary. It is
 * server-authoritative (so it behaves the same in singleplayer and
 * multiplayer) and completely generic: effects are keyed by an arbitrary
 * {@code Identifier}, so the package never knows about a specific enchantment,
 * the registry or the HUD.
 *
 * <p>An enchantment applies its own behaviour on start and undoes it in the
 * end callback; this package only handles the timing.
 */
package pt.joao.enchantmentsplus.effect;
