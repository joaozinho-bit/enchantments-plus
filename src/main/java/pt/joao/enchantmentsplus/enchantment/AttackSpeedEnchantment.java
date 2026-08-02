package pt.joao.enchantmentsplus.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registerable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import pt.joao.enchantmentsplus.EnchantmentsPlus;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.effect.TimedEffect;
import pt.joao.enchantmentsplus.effect.TimedEffectManager;
import pt.joao.enchantmentsplus.event.MeleeAttack;
import pt.joao.enchantmentsplus.hud.HudIndicator;
import pt.joao.enchantmentsplus.hud.HudPriority;
import pt.joao.enchantmentsplus.hud.HudValue;
import pt.joao.enchantmentsplus.networking.HudSync;
import pt.joao.enchantmentsplus.registry.ModEnchantments;
import pt.joao.enchantmentsplus.registry.ModItemTags;

/**
 * Attack Speed: a chance, on every hit, to fall into a brief frenzy.
 *
 * <p>Deliberately not a permanent attribute. A blade that simply swings faster
 * changes a number; one that occasionally erupts changes how a fight feels, and
 * the cooldown afterwards is what stops it from collapsing back into the former.
 *
 * <p>The whole lifecycle is one {@link TimedEffect}: an active phase followed by
 * a cooldown, with "already frenzied" and "still cooling down" both answered by
 * the effect simply existing. Nothing here counts ticks, and the attribute bonus
 * is a temporary modifier that is undone the moment the active phase ends, so a
 * disconnect or a crash mid-frenzy cannot leave anything behind.
 */
public final class AttackSpeedEnchantment {

	/** The effect, the attribute modifier and the HUD line are all this one thing. */
	private static final Identifier ID = EnchantmentsPlus.id("attack_speed");

	private static final String ICON = "⚡";

	private static final String LABEL_KEY = "enchantment.enchantments-plus.attack_speed";

	private static ConfigHolder<AttackSpeedConfig> config;

	private AttackSpeedEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration. Call once from mod init,
	 * before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("attack_speed", new AttackSpeedConfig());
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		EnchantmentDefinitions.mainHand(registry, ModEnchantments.ATTACK_SPEED,
				ModItemTags.ATTACK_SPEED_ENCHANTABLE, 2, 3, 15, 65, 4);
	}

	/**
	 * Rolls for a frenzy after a successful hit.
	 *
	 * @param attack the hit that landed
	 */
	public static void onHit(MeleeAttack attack) {
		if (!(attack.attacker() instanceof ServerPlayerEntity player)) {
			return;
		}

		AttackSpeedConfig settings = config.get();
		int level = attack.levelOf(ModEnchantments.ATTACK_SPEED, settings);
		if (level <= 0) {
			return;
		}

		// One question covers both "already frenzied" and "still cooling down":
		// the effect only exists while one of the two is true.
		if (TimedEffectManager.get(player, ID).isPresent()) {
			return;
		}
		if (player.getRandom().nextFloat() >= settings.chancePerLevel * level) {
			return;
		}

		applyBonus(player, settings.attackSpeedMultiplier);
		TimedEffect frenzy = TimedEffectManager.start(player, ID,
				Math.max(1, settings.frenzyTicks), Math.max(0, settings.cooldownTicks),
				AttackSpeedEnchantment::onFrenzyEnded);

		HudSync.show(player, HudIndicator.ofEffect(ID, ICON, LABEL_KEY, frenzy));
	}

	/**
	 * The active phase is over: drop the bonus and hand the line over to the
	 * cooldown.
	 *
	 * <p>Runs from inside the effect's own tick, which is why the cooldown
	 * indicator is built from the configured length rather than read back off
	 * the effect &mdash; it has not switched phase yet at this point.
	 */
	private static void onFrenzyEnded(ServerPlayerEntity player) {
		applyBonus(player, 0.0);

		int cooldown = Math.max(0, config.get().cooldownTicks);
		if (cooldown <= 0) {
			HudSync.hide(player, ID);
			return;
		}

		// The lifetime is the cooldown, so the client retires the line at the
		// same moment the effect frees up on the server, with no further packet.
		HudSync.show(player, new HudIndicator(
				ID, HudPriority.LOW, ICON, LABEL_KEY, new HudValue.Cooldown(cooldown), cooldown));
	}

	/**
	 * Rewrites the bonus from scratch under one fixed id, so no sequence of
	 * frenzies can make it drift or stack up.
	 */
	private static void applyBonus(ServerPlayerEntity player, double bonus) {
		EntityAttributeInstance attackSpeed =
				player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
		if (attackSpeed == null) {
			return;
		}

		attackSpeed.removeModifier(ID);
		if (bonus > 0.0) {
			// Multiplied against the total, so a slow heavy weapon and a quick
			// dagger both simply get proportionally faster.
			attackSpeed.addTemporaryModifier(new EntityAttributeModifier(
					ID, bonus, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
	}
}
