package pt.joao.enchantmentsplus.effect;

import java.util.function.Consumer;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * One temporary effect running for one player: an optional active phase
 * followed by an optional cooldown, with a small state machine on top.
 *
 * <p>It is a plain timer and knows nothing about what the effect <em>does</em>.
 * The owning enchantment applies its own behaviour (an attribute modifier, a
 * speed boost, ...) when it starts the effect and undoes it in the
 * {@code onEnd} callback, so this class stays completely generic.
 *
 * <p>Instances are created and advanced by {@link TimedEffectManager}; callers
 * only read the state through the query methods.
 */
public final class TimedEffect {

	private int activeTicks;
	private int cooldownTicks;
	private EffectState state;
	private final Consumer<ServerPlayerEntity> onEnd;

	TimedEffect(int activeTicks, int cooldownTicks, Consumer<ServerPlayerEntity> onEnd) {
		this.activeTicks = Math.max(0, activeTicks);
		this.cooldownTicks = Math.max(0, cooldownTicks);
		this.onEnd = onEnd;
		this.state = this.activeTicks > 0 ? EffectState.ACTIVE
				: this.cooldownTicks > 0 ? EffectState.COOLDOWN
				: EffectState.READY;
	}

	/**
	 * Advances the effect by one tick.
	 *
	 * <p>The {@code onEnd} callback runs here, when the active phase finishes.
	 * It must not start, cancel or otherwise modify this player's effects.
	 *
	 * @param player the online player this effect belongs to
	 * @return {@code true} while the effect is still running or cooling down,
	 *         {@code false} once it is done and can be discarded
	 */
	boolean tick(ServerPlayerEntity player) {
		if (state == EffectState.ACTIVE && --activeTicks <= 0) {
			if (onEnd != null) {
				onEnd.accept(player);
			}
			state = cooldownTicks > 0 ? EffectState.COOLDOWN : EffectState.READY;
		} else if (state == EffectState.COOLDOWN && --cooldownTicks <= 0) {
			state = EffectState.READY;
		}
		return state != EffectState.READY;
	}

	/** @return the current lifecycle state */
	public EffectState state() {
		return state;
	}

	/** @return {@code true} while the active phase is running */
	public boolean isActive() {
		return state == EffectState.ACTIVE;
	}

	/** @return {@code true} while the effect is cooling down */
	public boolean isOnCooldown() {
		return state == EffectState.COOLDOWN;
	}

	/** @return ticks left in the active phase, or {@code 0} if not active */
	public int remainingActiveTicks() {
		return state == EffectState.ACTIVE ? activeTicks : 0;
	}

	/** @return ticks left in the cooldown, or {@code 0} if not cooling down */
	public int remainingCooldownTicks() {
		return state == EffectState.COOLDOWN ? cooldownTicks : 0;
	}
}
