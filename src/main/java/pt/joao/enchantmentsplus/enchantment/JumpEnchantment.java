package pt.joao.enchantmentsplus.enchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import pt.joao.enchantmentsplus.EnchantmentsPlus;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.effect.TimedEffectManager;
import pt.joao.enchantmentsplus.hud.HudIndicator;
import pt.joao.enchantmentsplus.hud.HudPriority;
import pt.joao.enchantmentsplus.hud.HudValue;
import pt.joao.enchantmentsplus.networking.HudSync;
import pt.joao.enchantmentsplus.registry.ModEnchantments;
import pt.joao.enchantmentsplus.util.EnchantmentLevels;

/**
 * Jump: boots that let a jump be wound up before it is spent.
 *
 * <p>Modelled on a horse: holding builds the jump, letting go spends it, and
 * how long it was held is all that decides the height. The fall that follows is
 * free, because a jump that hurts to land is not a reward.
 *
 * <p>The client only ever reports that a key went down or came up. Everything
 * else &mdash; whether the boots qualify, whether the ground was left, how far
 * the charge got, how hard the launch is &mdash; is decided here, so nothing a
 * client could lie about changes the outcome.
 *
 * <p>Two {@link TimedEffectManager} windows do the work, one after the other
 * under the same id: the first watches the charge for the states it cannot
 * survive, the second watches for the landing that ends the fall immunity.
 */
public final class JumpEnchantment {

	/** The charge window and the HUD line. */
	private static final Identifier ID = EnchantmentsPlus.id("jump");

	/**
	 * The descent watch, deliberately a different id from the charge.
	 *
	 * <p>Sharing one would mean a player who presses the key again on the way
	 * down replaces the watch that is still owed them a safe landing, and the
	 * immunity would never be cleared &mdash; or worse, be cleared by the wrong
	 * thing.
	 */
	private static final Identifier FALL_ID = EnchantmentsPlus.id("jump_fall");

	private static final String ICON = "⇧";

	private static final String LABEL_KEY = "enchantment.enchantments-plus.jump";

	/** Vertical acceleration, in blocks per tick squared. */
	private static final double GRAVITY = 0.08;

	/** The velocity of an ordinary jump, so charged boots are never worse. */
	private static final double VANILLA_JUMP_VELOCITY = 0.42;

	/** Segments the charge bar is split into, purely to rate-limit updates. */
	private static final int BAR_SEGMENTS = 10;

	/** Safety net: nobody holds a charge this long on purpose. */
	private static final int MAX_HOLD_TICKS = 600;

	/**
	 * Leak guard only, long enough that no real fall reaches it.
	 *
	 * <p>The immunity is meant to last until the ground is touched, however
	 * long that takes, so this exists purely so a player who somehow never
	 * lands does not keep an entry forever.
	 */
	private static final int FALL_WATCH_TICKS = 24000;

	private static final Map<UUID, Charge> CHARGING = new HashMap<>();

	/** Players still in the air from a charged jump. */
	private static final Map<UUID, Fall> FALLING = new HashMap<>();

	private static ConfigHolder<JumpConfig> config;

	private JumpEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration. Call once from mod init,
	 * before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("jump", new JumpConfig());
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		EnchantmentDefinitions.boots(registry, ModEnchantments.JUMP, 2, 3, 15, 65, 4);
	}

	/**
	 * The charge key went down or came up on some client.
	 *
	 * @param player   the player it belongs to
	 * @param charging {@code true} for pressed, {@code false} for released
	 */
	public static void onChargeInput(ServerPlayerEntity player, boolean charging) {
		if (charging) {
			beginCharge(player);
		} else {
			release(player);
		}
	}

	/**
	 * Decides whether fall damage should be ignored.
	 *
	 * <p>Called from {@link pt.joao.enchantmentsplus.event.DamageEvents}. Covers
	 * everything between the launch and the next time the player stands on
	 * something, with no time limit at all: leap off a mountain and the whole
	 * descent is free, however long it lasts. The entry is dropped on landing,
	 * so an ordinary fall afterwards hurts exactly as much as it should.
	 *
	 * @param entity the entity being damaged
	 * @param source what is damaging it
	 * @return {@code true} when the damage should be ignored entirely
	 */
	public static boolean absorbsFall(LivingEntity entity, DamageSource source) {
		return source.isIn(DamageTypeTags.IS_FALL)
				&& entity instanceof ServerPlayerEntity player
				&& FALLING.containsKey(player.getUuid());
	}

	/** Forgets a player, for when they are already on their way out. */
	public static void forget(ServerPlayerEntity player) {
		CHARGING.remove(player.getUuid());
		FALLING.remove(player.getUuid());
	}

	private static void beginCharge(ServerPlayerEntity player) {
		JumpConfig settings = config.get();
		int level = EnchantmentLevels.effective(player.getWorld(),
				player.getEquippedStack(EquipmentSlot.FEET), ModEnchantments.JUMP, settings);
		if (level <= 0 || !canCharge(player)) {
			return;
		}

		int ticksToFull = Math.max(1,
				(int) Math.round(settings.maxChargeTicks / Math.max(0.01, settings.chargeSpeed)));
		Charge charge = new Charge(level, ticksToFull);
		CHARGING.put(player.getUuid(), charge);

		TimedEffectManager.start(player, ID, MAX_HOLD_TICKS, 0,
				JumpEnchantment::watchCharge, JumpEnchantment::cancelCharge);
		publish(player, charge);
	}

	/**
	 * Advances the charge and drops it the moment the ground is no longer
	 * underfoot.
	 *
	 * <p>Runs inside the manager's iteration, so cancelling means clearing the
	 * state and letting the window run out on its own rather than stopping it.
	 */
	private static void watchCharge(ServerPlayerEntity player) {
		Charge charge = CHARGING.get(player.getUuid());
		if (charge == null) {
			return;
		}
		if (!canCharge(player)) {
			cancelCharge(player);
			return;
		}

		if (charge.ticks < charge.ticksToFull) {
			charge.ticks++;
		}
		publish(player, charge);
	}

	private static void cancelCharge(ServerPlayerEntity player) {
		if (CHARGING.remove(player.getUuid()) != null) {
			HudSync.hide(player, ID);
		}
	}

	private static void release(ServerPlayerEntity player) {
		Charge charge = CHARGING.remove(player.getUuid());
		if (charge == null) {
			return;
		}

		HudSync.hide(player, ID);
		TimedEffectManager.cancel(player, ID);

		// A charge survives a stroll off a ledge, but spending it does not: the
		// launch has to push off something, or the boots would be a double jump.
		if (!canLaunch(player) || charge.ticks <= 0) {
			return;
		}

		JumpConfig settings = config.get();
		double progress = Math.min(1.0, (double) charge.ticks / charge.ticksToFull);
		double height = settings.maxHeightPerLevel * charge.level * progress;

		// Never worse than an ordinary jump, so a quick tap still feels normal.
		double velocity = Math.max(VANILLA_JUMP_VELOCITY, Math.sqrt(2.0 * GRAVITY * height));
		Vec3d current = player.getVelocity();
		player.setVelocity(current.x, velocity, current.z);
		player.velocityModified = true;

		FALLING.put(player.getUuid(), new Fall());
		TimedEffectManager.start(player, FALL_ID, FALL_WATCH_TICKS, 0,
				JumpEnchantment::watchLanding, JumpEnchantment::stopFalling);
	}

	/**
	 * Ends the fall immunity on the first touchdown after the jump.
	 *
	 * <p>Waits to actually see the player leave the ground before it starts
	 * looking for them to land on it. Counting ticks instead would be a guess
	 * about how long a launch takes to register, and a wrong guess would end
	 * the immunity while they were still on the way up.
	 *
	 * <p>The landing is only accepted once {@code fallDistance} has been reset
	 * too. Vanilla clears it in the same breath as it deals the damage, but
	 * strictly afterwards, so waiting for it removes any question of this
	 * running first and taking the immunity away a moment too soon.
	 */
	private static void watchLanding(ServerPlayerEntity player) {
		Fall fall = FALLING.get(player.getUuid());
		if (fall == null) {
			return;
		}

		if (!fall.airborne) {
			fall.airborne = !player.isOnGround();
		} else if (player.isOnGround() && player.fallDistance == 0.0F) {
			FALLING.remove(player.getUuid());
		}
	}

	private static void stopFalling(ServerPlayerEntity player) {
		FALLING.remove(player.getUuid());
	}

	/**
	 * @return whether a charge can be started or kept going
	 *
	 * <p>Deliberately says nothing about the ground. Walking off a ledge,
	 * hopping up a block or falling a short way are all part of moving around
	 * while winding a jump up, and losing the charge to any of them would make
	 * the enchantment tiresome. Only the launch itself insists on solid ground.
	 */
	private static boolean canCharge(ServerPlayerEntity player) {
		return !player.hasVehicle()
				&& !player.isTouchingWater()
				&& !player.isFallFlying()
				&& !player.getAbilities().flying;
	}

	/** @return whether the charge can actually be spent right now */
	private static boolean canLaunch(ServerPlayerEntity player) {
		return canCharge(player) && player.isOnGround();
	}

	/**
	 * Sends the bar, but only when it would actually look different.
	 *
	 * <p>The charge is smooth and the bar is not, so a whole charge costs about
	 * ten packets instead of one per tick.
	 */
	private static void publish(ServerPlayerEntity player, Charge charge) {
		float progress = Math.min(1.0F, (float) charge.ticks / charge.ticksToFull);
		int segment = Math.round(progress * BAR_SEGMENTS);
		if (segment == charge.shownSegment) {
			return;
		}

		charge.shownSegment = segment;
		HudSync.show(player, new HudIndicator(
				ID, HudPriority.MEDIUM, ICON, LABEL_KEY, new HudValue.Progress(progress), 0));
	}

	/** One descent from a charged jump, and whether it has begun. */
	private static final class Fall {

		private boolean airborne;
	}

	/** One charge in progress. */
	private static final class Charge {

		private final int level;
		private final int ticksToFull;
		private int ticks;
		private int shownSegment = -1;

		private Charge(int level, int ticksToFull) {
			this.level = level;
			this.ticksToFull = ticksToFull;
		}
	}
}
