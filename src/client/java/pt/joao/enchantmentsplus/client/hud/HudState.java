package pt.joao.enchantmentsplus.client.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.Identifier;
import pt.joao.enchantmentsplus.hud.HudIndicator;

/**
 * What the HUD currently knows, on the client.
 *
 * <p>The store is push-based: an enchantment publishes a snapshot when its
 * state changes and the HUD holds on to it, so nothing is ever asked "are you
 * still relevant?" every frame. Each entry ages by one tick per client tick and
 * drops itself once its lifetime is up, which is what makes a countdown cost a
 * single update instead of a stream of them.
 *
 * <p>Entries are keyed by indicator id, so publishing again simply refreshes
 * the existing line rather than adding a second one. The priority-ordered view
 * is cached and only rebuilt when the set of entries actually changes.
 */
public final class HudState {

	private static final Comparator<Entry> BY_PRIORITY =
			Comparator.comparingInt(entry -> entry.indicator().priority().ordinal());

	private static final Map<Identifier, Entry> ENTRIES = new LinkedHashMap<>();
	private static final List<Entry> SORTED = new ArrayList<>();

	private static boolean dirty;

	private HudState() {
	}

	/**
	 * Shows an indicator, replacing any previous one with the same id and
	 * restarting its lifetime.
	 *
	 * <p>Client-side enchantments call this directly; state owned by the server
	 * arrives here through
	 * {@link pt.joao.enchantmentsplus.networking.HudSync}.
	 *
	 * @param indicator the snapshot to display
	 */
	public static void show(HudIndicator indicator) {
		Entry existing = ENTRIES.get(indicator.id());
		if (existing == null) {
			ENTRIES.put(indicator.id(), new Entry(indicator));
			dirty = true;
			return;
		}
		// Reordering is only needed when the importance itself changed; a new
		// value for the same line keeps its place.
		dirty |= existing.indicator.priority() != indicator.priority();
		existing.indicator = indicator;
		existing.elapsedTicks = 0;
	}

	/**
	 * Hides an indicator before its lifetime runs out.
	 *
	 * @param id the indicator id used when it was shown
	 */
	public static void hide(Identifier id) {
		dirty |= ENTRIES.remove(id) != null;
	}

	/** Forgets everything, e.g. when leaving a world. */
	public static void clear() {
		if (!ENTRIES.isEmpty()) {
			ENTRIES.clear();
			dirty = true;
		}
	}

	/** Ages every entry by one tick and drops the ones that have expired. */
	static void tick() {
		if (ENTRIES.isEmpty()) {
			return;
		}
		Iterator<Entry> entries = ENTRIES.values().iterator();
		while (entries.hasNext()) {
			Entry entry = entries.next();
			entry.elapsedTicks++;
			int lifetime = entry.indicator.lifetimeTicks();
			if (lifetime > 0 && entry.elapsedTicks >= lifetime) {
				entries.remove();
				dirty = true;
			}
		}
	}

	/**
	 * @return every live entry, most important first; the list is reused
	 *         between frames and must not be modified by the caller
	 */
	static List<Entry> sorted() {
		if (dirty) {
			SORTED.clear();
			SORTED.addAll(ENTRIES.values());
			// Stable, so indicators of equal priority keep the order they were
			// published in.
			SORTED.sort(BY_PRIORITY);
			dirty = false;
		}
		return SORTED;
	}

	/**
	 * One indicator being tracked over time.
	 *
	 * <p>Mutable on purpose: the snapshot itself is immutable, but the entry
	 * around it ages every tick and would otherwise be rebuilt constantly.
	 */
	static final class Entry {

		private HudIndicator indicator;
		private int elapsedTicks;

		private Entry(HudIndicator indicator) {
			this.indicator = indicator;
		}

		/** @return the latest snapshot published for this id */
		HudIndicator indicator() {
			return indicator;
		}

		/** @return ticks elapsed since that snapshot was published */
		int elapsedTicks() {
			return elapsedTicks;
		}
	}
}
