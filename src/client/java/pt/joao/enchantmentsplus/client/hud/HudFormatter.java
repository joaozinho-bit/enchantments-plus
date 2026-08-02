package pt.joao.enchantmentsplus.client.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import pt.joao.enchantmentsplus.EnchantmentsPlus;
import pt.joao.enchantmentsplus.hud.HudIndicator;
import pt.joao.enchantmentsplus.hud.HudValue;

/**
 * Turns a snapshot into the single line of text that represents it.
 *
 * <p>This is the one place that knows what each {@link HudValue} looks like, so
 * a timer, a counter and a bar all come out consistent without any enchantment
 * having a say. All wording goes through translation keys; only the bar glyphs
 * are literal, and even those come from {@link HudConfig}.
 */
final class HudFormatter {

	private static final String KEY_PREFIX = "hud." + EnchantmentsPlus.MOD_ID + ".";

	private static final int TICKS_PER_SECOND = 20;

	private HudFormatter() {
	}

	/**
	 * @param indicator    the snapshot to render
	 * @param elapsedTicks ticks since it was published, used to count timers
	 *                     down without the publisher having to resend them
	 * @return the formatted line, e.g. {@code ⚡ 5.2s}
	 */
	static Text format(HudIndicator indicator, int elapsedTicks) {
		HudConfig config = HudConfig.INSTANCE;

		List<Text> parts = new ArrayList<>(4);
		if (!indicator.icon().isEmpty()) {
			parts.add(Text.literal(indicator.icon()));
		}
		if (config.showLabels && !indicator.labelKey().isEmpty()) {
			parts.add(Text.translatable(indicator.labelKey()));
		}
		Text value = value(indicator.value(), elapsedTicks, config);
		if (value != null) {
			parts.add(value);
		}
		Text left = timeLeft(indicator, elapsedTicks, config);
		if (left != null) {
			parts.add(left);
		}

		MutableText line = Text.empty();
		for (int i = 0; i < parts.size(); i++) {
			if (i > 0) {
				line.append(Text.literal(" "));
			}
			line.append(parts.get(i));
		}
		return line;
	}

	/** @return the value part, or {@code null} when there is nothing to add */
	private static Text value(HudValue value, int elapsedTicks, HudConfig config) {
		return switch (value) {
			// A running effect is precise enough to be worth a decimal.
			case HudValue.Timer timer -> seconds(timer.ticks() - elapsedTicks, true);
			// A cooldown is only "how long until I can go again", so whole
			// seconds keep it calmer than the timer above it.
			case HudValue.Cooldown cooldown -> seconds(cooldown.ticks() - elapsedTicks, false);
			case HudValue.Progress progress -> bar(progress.progress(), config);
			case HudValue.Counter counter -> Text.translatable(KEY_PREFIX + "counter", counter.count());
			case HudValue.State state ->
					Text.translatable(KEY_PREFIX + "state." + state.state().name().toLowerCase(Locale.ROOT));
			case HudValue.Icon ignored -> null;
		};
	}

	/**
	 * How much of an indicator's lifetime is left, as a bar.
	 *
	 * <p>Generic: any indicator that expires on its own can show it, which is
	 * what lets a decaying counter say both how many and how long without
	 * needing a shape of its own. Timers and cooldowns are skipped because they
	 * already say it in words.
	 *
	 * @return the bar, or {@code null} when there is nothing to count down
	 */
	private static Text timeLeft(HudIndicator indicator, int elapsedTicks, HudConfig config) {
		if (!config.showTimeBars || indicator.lifetimeTicks() <= 0) {
			return null;
		}
		if (indicator.value() instanceof HudValue.Timer || indicator.value() instanceof HudValue.Cooldown) {
			return null;
		}
		return bar(1.0F - (float) elapsedTicks / indicator.lifetimeTicks(), config);
	}

	private static Text seconds(int ticks, boolean precise) {
		float remaining = Math.max(0, ticks) / (float) TICKS_PER_SECOND;
		String formatted = precise
				? String.format(Locale.ROOT, "%.1f", remaining)
				: Integer.toString((int) Math.ceil(remaining));
		return Text.translatable(KEY_PREFIX + "seconds", formatted);
	}

	private static Text bar(float progress, HudConfig config) {
		int filled = Math.round(Math.clamp(progress, 0.0F, 1.0F) * config.barWidth);
		StringBuilder bar = new StringBuilder(config.barWidth);
		for (int i = 0; i < config.barWidth; i++) {
			bar.append(i < filled ? config.barFilled : config.barEmpty);
		}
		return Text.literal(bar.toString());
	}
}
