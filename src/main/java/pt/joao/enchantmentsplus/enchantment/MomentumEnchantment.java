package pt.joao.enchantmentsplus.enchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registerable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import pt.joao.enchantmentsplus.EnchantmentsPlus;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.effect.TimedEffectManager;
import pt.joao.enchantmentsplus.hud.HudIndicator;
import pt.joao.enchantmentsplus.hud.HudPriority;
import pt.joao.enchantmentsplus.hud.HudValue;
import pt.joao.enchantmentsplus.networking.HudSync;
import pt.joao.enchantmentsplus.registry.ModEnchantments;
import pt.joao.enchantmentsplus.registry.ModItemTags;
import pt.joao.enchantmentsplus.util.EnchantmentLevels;

/**
 * Momentum: the longer you keep digging, the faster you dig.
 *
 * <p>Every block adds a stack and every stack adds a little mining speed, up to
 * a configured ceiling. What is being rewarded is the rhythm, not the rock, so
 * the block type is never looked at.
 *
 * <p>The streak is a {@link TimedEffectManager} effect restarted every time the
 * tool touches a block, which is what makes "walk away and lose it all" fall
 * out of the existing system rather than needing a countdown here. The speed
 * itself is an attribute modifier, temporary rather than persistent: a streak
 * is meant to die with the session, not be saved.
 */
public final class MomentumEnchantment {

	/** The effect, the attribute modifier and the HUD line are all this one thing. */
	private static final Identifier ID = EnchantmentsPlus.id("momentum");

	private static final String ICON = "⛏";

	private static final String LABEL_KEY = "enchantment.enchantments-plus.momentum";

	/** Current streak per player; empty for everyone who is not mid-streak. */
	private static final Map<UUID, Integer> STACKS = new HashMap<>();

	private static ConfigHolder<MomentumConfig> config;

	private MomentumEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration. Call once from mod init,
	 * before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("momentum", new MomentumConfig());
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		EnchantmentDefinitions.mainHand(registry, ModEnchantments.MOMENTUM,
				ModItemTags.MOMENTUM_ENCHANTABLE, 2, 1, 15, 65, 4);
	}

	/**
	 * Adds a stack for a block just broken.
	 *
	 * @param player the miner
	 * @param tool   the stack they are holding
	 */
	public static void onMined(ServerPlayerEntity player, ItemStack tool) {
		MomentumConfig settings = config.get();
		if (EnchantmentLevels.effective(player.getWorld(), tool, ModEnchantments.MOMENTUM, settings) <= 0) {
			reset(player);
			return;
		}

		int stacks = Math.min(STACKS.getOrDefault(player.getUuid(), 0) + 1, Math.max(1, settings.maxStacks));
		STACKS.put(player.getUuid(), stacks);
		applySpeed(player, stacks * settings.speedPerStack);
		restartWindow(player, stacks, settings);
	}

	/**
	 * Keeps the streak alive while a block is being worked on.
	 *
	 * <p>Called when the player starts swinging at a block. Without it the
	 * window would only ever be measured from the last block that
	 * <em>finished</em>, and a single long swing &mdash; obsidian, ancient
	 * debris &mdash; would quietly eat the streak that earned the speed in the
	 * first place. Starting a block never grants a stack, only buys time.
	 *
	 * @param player the miner
	 * @param tool   the stack they are holding
	 */
	public static void onToolUsed(ServerPlayerEntity player, ItemStack tool) {
		Integer stacks = STACKS.get(player.getUuid());
		if (stacks == null) {
			return;
		}

		MomentumConfig settings = config.get();
		if (EnchantmentLevels.effective(player.getWorld(), tool, ModEnchantments.MOMENTUM, settings) <= 0) {
			reset(player);
			return;
		}
		restartWindow(player, stacks, settings);
	}

	/**
	 * Restarts the countdown, on the server and on the screen at once.
	 *
	 * <p>Restarting the effect is the whole decay rule; giving the indicator
	 * the same window means the bar on the client empties in step with the
	 * streak dying on the server, without a second packet to keep them aligned.
	 */
	private static void restartWindow(ServerPlayerEntity player, int stacks, MomentumConfig settings) {
		int window = Math.max(1, settings.decayTicks);
		TimedEffectManager.start(player, ID, window, 0, MomentumEnchantment::clear);
		HudSync.show(player, new HudIndicator(
				ID, HudPriority.MEDIUM, ICON, LABEL_KEY, new HudValue.Counter(stacks), window));
	}

	/**
	 * The held item changed; a streak belongs to one tool.
	 *
	 * <p>Called for every main-hand change, which includes the durability tick
	 * of the very tool being used, so only a change of item counts.
	 *
	 * @param player   the player
	 * @param previous what they were holding
	 * @param current  what they are holding now
	 */
	public static void onHeldChanged(ServerPlayerEntity player, ItemStack previous, ItemStack current) {
		if (!STACKS.containsKey(player.getUuid()) || ItemStack.areItemsEqual(previous, current)) {
			return;
		}
		reset(player);
	}

	/**
	 * Forgets a player without touching their attributes or screen, for when
	 * they are already on their way out.
	 *
	 * @param player the player leaving
	 */
	public static void forget(ServerPlayerEntity player) {
		STACKS.remove(player.getUuid());
	}

	/**
	 * Drops the streak. Safe as a timed-effect callback: it never touches the
	 * effect itself.
	 */
	private static void clear(ServerPlayerEntity player) {
		if (STACKS.remove(player.getUuid()) == null) {
			return;
		}
		applySpeed(player, 0.0);
		HudSync.hide(player, ID);
	}

	/** Drops the streak and stops the window still counting down for it. */
	private static void reset(ServerPlayerEntity player) {
		if (!STACKS.containsKey(player.getUuid())) {
			return;
		}
		TimedEffectManager.cancel(player, ID);
		clear(player);
	}

	/**
	 * Rewrites the speed bonus from scratch under one fixed id, so no sequence
	 * of gaining and losing stacks can make it drift or stack up.
	 */
	private static void applySpeed(ServerPlayerEntity player, double bonus) {
		EntityAttributeInstance speed =
				player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED);
		if (speed == null) {
			return;
		}

		speed.removeModifier(ID);
		if (bonus > 0.0) {
			speed.addTemporaryModifier(
					new EntityAttributeModifier(ID, bonus, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		}
	}
}
