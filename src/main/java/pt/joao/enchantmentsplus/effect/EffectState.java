package pt.joao.enchantmentsplus.effect;

/**
 * The lifecycle states shared by enchantments that expose a temporary effect
 * (for example Attack Speed, Momentum or Jump).
 *
 * <p>This is the common vocabulary of the effect system: a concrete effect
 * implementation reports which state it is currently in, and the HUD decides
 * how (and whether) to show it. Keeping the states here means the HUD never has
 * to know about any specific enchantment.
 */
public enum EffectState {

	/** No effect is running and it is ready to be triggered. */
	READY,

	/** The player is building the effect up, e.g. charging a Jump. */
	CHARGING,

	/** The effect is currently running. */
	ACTIVE,

	/** The effect has ended and cannot be used again yet. */
	COOLDOWN
}
