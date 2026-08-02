package pt.joao.enchantmentsplus.effect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import pt.joao.enchantmentsplus.EnchantmentsPlus;
import pt.joao.enchantmentsplus.networking.ToggleSync;
import pt.joao.enchantmentsplus.util.EnchantmentLevels;

/**
 * Server-side owner of every effect the player switches on and off by hand.
 *
 * <p>Two different things are being kept apart here, and keeping them apart is
 * the whole design. What the player <em>asked for</em> is a preference: it is
 * remembered per player for as long as they are connected and nothing but
 * pressing the key again changes it. Whether the effect is <em>running</em> is
 * never stored at all &mdash; it is derived, every time anything relevant moves,
 * from that preference and the piece actually being worn.
 *
 * <p>Deriving instead of tracking is what makes the awkward cases disappear
 * rather than needing to be handled. Taking the armour off, having the
 * enchantment ground away, dying, changing dimension, a config being switched
 * off: none of them are special-cased anywhere, because each one simply changes
 * what the next derivation produces. Nothing can be left behind, because nothing
 * is ever assumed to still be there.
 *
 * <p>Entirely server-side, so it behaves identically in singleplayer and on a
 * dedicated server. The client's only part is to say that a key was pressed; it
 * decides nothing and is told nothing it could not already see.
 *
 * <p>Generic, in the same way {@link TimedEffectManager} is: effects are keyed
 * by an arbitrary {@link Identifier} and describe themselves through
 * {@link ToggleableEffect}, so this class never refers to a specific
 * enchantment.
 */
public final class ToggleManager {

	/** Prefix of the action bar message keys, e.g. {@code toggle.enchantments-plus.speed.enabled}. */
	private static final String MESSAGE_PREFIX = "toggle." + EnchantmentsPlus.MOD_ID + ".";

	/**
	 * How close together two presses have to be to count as one gesture.
	 *
	 * <p>Wide enough to be comfortable, narrow enough that two deliberate
	 * switches in a row are still two switches.
	 */
	private static final int DOUBLE_TAP_TICKS = 8;

	/** Every registered toggle, in registration order. */
	private static final Map<Identifier, ToggleableEffect> REGISTERED = new LinkedHashMap<>();

	/** What each player has asked for; absent for everyone who has asked for nothing. */
	private static final Map<UUID, Set<Identifier>> ENABLED = new HashMap<>();

	/** The last key press per player, kept only long enough to pair it with a second. */
	private static final Map<UUID, Press> LAST_PRESS = new HashMap<>();

	private ToggleManager() {
	}

	/**
	 * Registers one toggleable effect. Call once from the enchantment's own
	 * {@code init}, at mod init.
	 *
	 * @param effect the effect to run
	 */
	public static void register(ToggleableEffect effect) {
		if (REGISTERED.putIfAbsent(effect.id(), effect) != null) {
			throw new IllegalArgumentException("Toggleable effect already registered: " + effect.id());
		}
	}

	/**
	 * Flips one effect for a player, because they pressed its key.
	 *
	 * <p>Called from {@link pt.joao.enchantmentsplus.networking.ToggleSync}, so
	 * the id arrives from a client and is treated as a request rather than a
	 * fact: an id nobody registered, or one for gear the player is not wearing,
	 * is simply ignored. Refusing quietly is deliberate &mdash; the key belongs
	 * to a piece of armour, and a player who is not wearing it has not done
	 * anything worth being told about.
	 *
	 * @param player the player who pressed the key
	 * @param id     the effect they asked to flip
	 */
	public static void toggle(ServerPlayerEntity player, Identifier id) {
		ToggleableEffect effect = REGISTERED.get(id);
		if (effect == null) {
			return;
		}

		int level = levelOf(player, effect);
		// Switching something off never asks for the gear. A preference the
		// player cannot withdraw because they put the tool away is a trap, and
		// they may well be putting it away precisely to stop it.
		if (level <= 0 && !isEnabled(player, id)) {
			return;
		}

		// A second press hard on the heels of the first was never two decisions.
		// The first one has already flipped the switch, so flipping it back is
		// what leaves the player where they started, with only the mode moved on.
		boolean cycling = effect.modes() != null && level > 0 && isSecondTap(player, id);
		boolean nowEnabled = flip(player, id);

		String mode = "";
		if (cycling) {
			mode = effect.modes().next(player, level);
		} else if (effect.modes() != null && level > 0) {
			mode = effect.modes().current(player, level);
		}

		refresh(player);
		ToggleSync.publish(player, id, nowEnabled, mode);

		if (cycling) {
			announce(player, MESSAGE_PREFIX + id.getPath() + ".mode." + mode);
		} else {
			announce(player, MESSAGE_PREFIX + id.getPath() + (nowEnabled ? ".enabled" : ".disabled"));
		}
	}

	/**
	 * The level a toggle is currently worth, for the effects that are asked
	 * rather than applied.
	 *
	 * <p>Answers the whole question at once &mdash; switched on, wearing or
	 * holding the right piece, enchanted, and within the configured cap &mdash;
	 * so a caller never has to combine the preference with the gear itself and
	 * cannot get that combination wrong.
	 *
	 * @param player the player to ask about
	 * @param id     the effect to ask about
	 * @return the level to act at, or {@code 0} to do nothing
	 */
	public static int activeLevel(ServerPlayerEntity player, Identifier id) {
		ToggleableEffect effect = REGISTERED.get(id);
		if (effect == null) {
			return 0;
		}

		Set<Identifier> enabled = ENABLED.get(player.getUuid());
		return enabled != null && enabled.contains(id) ? levelOf(player, effect) : 0;
	}

	/**
	 * Brings every effect back in line with what the player has asked for and
	 * what they are currently wearing.
	 *
	 * <p>Called from {@link pt.joao.enchantmentsplus.event.EntityEvents} whenever
	 * a piece of equipment changes and once whenever the player enters the world,
	 * which covers joining, respawning and changing dimension. Every effect is
	 * rewritten from scratch on every call, so the order things happen in cannot
	 * matter and no sequence of equipping, dying and toggling can leave an effect
	 * running that should not be.
	 *
	 * @param player the player to bring in line
	 */
	public static void refresh(ServerPlayerEntity player) {
		if (REGISTERED.isEmpty()) {
			return;
		}

		Set<Identifier> enabled = ENABLED.get(player.getUuid());
		for (ToggleableEffect effect : REGISTERED.values()) {
			int level = enabled != null && enabled.contains(effect.id()) ? levelOf(player, effect) : 0;
			if (level > 0) {
				effect.onEnable().apply(player, level);
			} else {
				// Unconditional, and cheap by contract: undoing nothing has to be
				// free, because that is what almost every call is.
				effect.onDisable().accept(player);
			}
		}
	}

	/**
	 * Forgets a player's preferences without touching them, for when they are
	 * already on their way out.
	 *
	 * @param player the player leaving
	 */
	public static void forget(ServerPlayerEntity player) {
		ENABLED.remove(player.getUuid());
		LAST_PRESS.remove(player.getUuid());
	}

	/**
	 * @return the level the worn piece grants, or {@code 0} when the piece is
	 *         absent, unenchanted or the enchantment is switched off in the
	 *         configuration
	 */
	private static int levelOf(ServerPlayerEntity player, ToggleableEffect effect) {
		return EnchantmentLevels.effective(player.getWorld(),
				player.getEquippedStack(effect.slot()), effect.enchantment(), effect.config().get());
	}

	/** @return whether this player currently has that preference switched on */
	private static boolean isEnabled(ServerPlayerEntity player, Identifier id) {
		Set<Identifier> enabled = ENABLED.get(player.getUuid());
		return enabled != null && enabled.contains(id);
	}

	/**
	 * Turns the preference over.
	 *
	 * @return what it is now
	 */
	private static boolean flip(ServerPlayerEntity player, Identifier id) {
		Set<Identifier> enabled = ENABLED.computeIfAbsent(player.getUuid(), uuid -> new HashSet<>());
		boolean nowEnabled = !enabled.remove(id);
		if (nowEnabled) {
			enabled.add(id);
		} else if (enabled.isEmpty()) {
			ENABLED.remove(player.getUuid());
		}
		return nowEnabled;
	}

	/**
	 * Whether this press closes a pair with the one before it.
	 *
	 * <p>Consumes the record either way. A third press is the start of something
	 * new rather than the tail of the last pair, so holding the key down cannot
	 * run through every mode at once.
	 */
	private static boolean isSecondTap(ServerPlayerEntity player, Identifier id) {
		int now = player.getServer().getTicks();
		Press previous = LAST_PRESS.put(player.getUuid(), new Press(id, now));
		boolean paired = previous != null
				&& previous.effect().equals(id)
				&& now - previous.tick() <= DOUBLE_TAP_TICKS;

		if (paired) {
			LAST_PRESS.remove(player.getUuid());
		}
		return paired;
	}

	/**
	 * Says what just changed, on the action bar rather than in chat: it is a
	 * confirmation of something the player just did, so it should be seen once
	 * and then leave, without a line of history behind it.
	 */
	private static void announce(ServerPlayerEntity player, String key) {
		player.sendMessage(Text.translatable(key).formatted(Formatting.GRAY), true);
	}

	/** One key press, remembered only long enough to see whether a second follows. */
	private record Press(Identifier effect, int tick) {
	}
}
