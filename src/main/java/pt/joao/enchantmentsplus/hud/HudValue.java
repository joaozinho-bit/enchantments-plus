package pt.joao.enchantmentsplus.hud;

import java.util.Objects;

import net.minecraft.network.PacketByteBuf;
import pt.joao.enchantmentsplus.effect.EffectState;

/**
 * The measurable part of a {@link HudIndicator}: <em>what</em> is being shown,
 * with no say in how it looks.
 *
 * <p>This is the whole vocabulary the HUD understands. An enchantment picks the
 * variant that matches its state and the client turns it into a line of text;
 * because the type is {@code sealed}, adding a variant is a compile-time error
 * everywhere it has to be handled, so a new indicator kind can never be
 * silently forgotten.
 *
 * <p>Values are immutable snapshots taken when the state changes, which is what
 * lets the HUD be push-based: {@link Timer} and {@link Cooldown} carry the
 * remaining ticks at that moment and the client counts them down locally
 * instead of asking the enchantment every frame.
 */
public sealed interface HudValue {

	/** @return the discriminator used to (de)serialise this value */
	Kind kind();

	/** The closed set of value shapes, used as the wire discriminator. */
	enum Kind {
		TIMER,
		COOLDOWN,
		PROGRESS,
		COUNTER,
		STATE,
		ICON
	}

	/**
	 * A countdown for something currently running, shown as a time left.
	 *
	 * @param ticks ticks remaining when the snapshot was taken
	 */
	record Timer(int ticks) implements HudValue {

		public Timer {
			ticks = Math.max(0, ticks);
		}

		@Override
		public Kind kind() {
			return Kind.TIMER;
		}
	}

	/**
	 * A countdown for something the player is waiting on. Separate from
	 * {@link Timer} so the HUD can present, coarsen or hide it independently.
	 *
	 * @param ticks ticks remaining when the snapshot was taken
	 */
	record Cooldown(int ticks) implements HudValue {

		public Cooldown {
			ticks = Math.max(0, ticks);
		}

		@Override
		public Kind kind() {
			return Kind.COOLDOWN;
		}
	}

	/**
	 * A filling bar.
	 *
	 * @param progress how full the bar is, clamped to {@code 0..1}
	 */
	record Progress(float progress) implements HudValue {

		public Progress {
			progress = Math.clamp(progress, 0.0F, 1.0F);
		}

		@Override
		public Kind kind() {
			return Kind.PROGRESS;
		}
	}

	/**
	 * A plain tally, e.g. a number of stacks.
	 *
	 * @param count the current amount
	 */
	record Counter(int count) implements HudValue {

		@Override
		public Kind kind() {
			return Kind.COUNTER;
		}
	}

	/**
	 * A named lifecycle state, reusing the effect system's vocabulary so an
	 * enchantment never has to invent its own wording.
	 *
	 * @param state the state to display
	 */
	record State(EffectState state) implements HudValue {

		public State {
			Objects.requireNonNull(state, "state");
		}

		@Override
		public Kind kind() {
			return Kind.STATE;
		}
	}

	/** No value at all: the indicator's icon (and label) carry the meaning. */
	record Icon() implements HudValue {

		public static final Icon INSTANCE = new Icon();

		@Override
		public Kind kind() {
			return Kind.ICON;
		}
	}

	/** Writes a value, discriminator first. */
	static void write(PacketByteBuf buf, HudValue value) {
		buf.writeEnumConstant(value.kind());
		switch (value) {
			case Timer timer -> buf.writeVarInt(timer.ticks());
			case Cooldown cooldown -> buf.writeVarInt(cooldown.ticks());
			case Progress progress -> buf.writeFloat(progress.progress());
			case Counter counter -> buf.writeVarInt(counter.count());
			case State state -> buf.writeEnumConstant(state.state());
			case Icon ignored -> {
				// The discriminator alone is the whole value.
			}
		}
	}

	/** Reads a value written by {@link #write}. */
	static HudValue read(PacketByteBuf buf) {
		return switch (buf.readEnumConstant(Kind.class)) {
			case TIMER -> new Timer(buf.readVarInt());
			case COOLDOWN -> new Cooldown(buf.readVarInt());
			case PROGRESS -> new Progress(buf.readFloat());
			case COUNTER -> new Counter(buf.readVarInt());
			case STATE -> new State(buf.readEnumConstant(EffectState.class));
			case ICON -> Icon.INSTANCE;
		};
	}
}
