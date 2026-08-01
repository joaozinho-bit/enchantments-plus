package pt.joao.enchantmentsplus.hud;

import java.util.Objects;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import pt.joao.enchantmentsplus.effect.TimedEffect;

/**
 * One immutable snapshot of something an enchantment wants the player to know.
 *
 * <p>This is the entire contract between an enchantment and the HUD. An
 * enchantment describes its state and nothing else; deciding whether, where and
 * in which order the snapshot is drawn belongs to the client HUD. Because a
 * snapshot is plain data it can be published locally or sent over the network
 * without either side knowing which enchantment produced it.
 *
 * @param id            identifies the indicator, so a later snapshot with the
 *                      same id replaces the previous one instead of stacking;
 *                      an enchantment reuses the id of its timed effect
 * @param priority      how important this is compared to other indicators
 * @param icon          a short glyph shown first, or {@code ""} for none
 * @param labelKey      translation key of the name, or {@code ""} for none
 * @param value         the measurable part, see {@link HudValue}
 * @param lifetimeTicks how long the indicator stays before hiding itself;
 *                      {@code 0} keeps it until it is explicitly hidden
 */
public record HudIndicator(Identifier id, HudPriority priority, String icon, String labelKey,
		HudValue value, int lifetimeTicks) {

	public HudIndicator {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(priority, "priority");
		Objects.requireNonNull(icon, "icon");
		Objects.requireNonNull(labelKey, "labelKey");
		Objects.requireNonNull(value, "value");
		lifetimeTicks = Math.max(0, lifetimeTicks);
	}

	/**
	 * Describes a running {@link TimedEffect}, which is where most indicators
	 * come from: the effect system already tracks the phase and the ticks left,
	 * so the enchantment only supplies the presentation.
	 *
	 * <p>The lifetime is taken from the remaining ticks, so a single snapshot
	 * per phase change is enough &mdash; the indicator counts itself down and
	 * disappears on its own. An effect that is {@code READY} has nothing left to
	 * show and should be hidden rather than published.
	 *
	 * @param id       the indicator id, normally the effect's own id
	 * @param icon     a short glyph, or {@code ""} for none
	 * @param labelKey translation key of the name, or {@code ""} for none
	 * @param effect   the effect to describe
	 * @return a snapshot of the effect's current phase
	 */
	public static HudIndicator ofEffect(Identifier id, String icon, String labelKey, TimedEffect effect) {
		return switch (effect.state()) {
			case ACTIVE -> new HudIndicator(id, HudPriority.HIGH, icon, labelKey,
					new HudValue.Timer(effect.remainingActiveTicks()), effect.remainingActiveTicks());
			case COOLDOWN -> new HudIndicator(id, HudPriority.LOW, icon, labelKey,
					new HudValue.Cooldown(effect.remainingCooldownTicks()), effect.remainingCooldownTicks());
			case CHARGING, READY -> new HudIndicator(id, HudPriority.MEDIUM, icon, labelKey,
					new HudValue.State(effect.state()), 0);
		};
	}

	/** Writes a snapshot for {@link pt.joao.enchantmentsplus.networking.HudSync}. */
	public static void write(PacketByteBuf buf, HudIndicator indicator) {
		buf.writeIdentifier(indicator.id());
		buf.writeEnumConstant(indicator.priority());
		buf.writeString(indicator.icon());
		buf.writeString(indicator.labelKey());
		HudValue.write(buf, indicator.value());
		buf.writeVarInt(indicator.lifetimeTicks());
	}

	/** Reads a snapshot written by {@link #write}. */
	public static HudIndicator read(PacketByteBuf buf) {
		return new HudIndicator(
				buf.readIdentifier(),
				buf.readEnumConstant(HudPriority.class),
				buf.readString(),
				buf.readString(),
				HudValue.read(buf),
				buf.readVarInt());
	}
}
