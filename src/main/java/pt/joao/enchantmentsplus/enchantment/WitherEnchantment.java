package pt.joao.enchantmentsplus.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registerable;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.event.MeleeAttack;
import pt.joao.enchantmentsplus.registry.ModEnchantments;

/**
 * Wither: a sword that leaves its victims withering.
 *
 * <p>Hitting an entity applies the vanilla Wither effect for a duration that
 * grows with the enchantment level, while the amplifier stays where the config
 * puts it. Scaling only the duration keeps the enchantment readable and stops
 * higher levels from turning into raw damage.
 *
 * <p>Runs on the server, from {@link pt.joao.enchantmentsplus.event.AttackEvents},
 * so singleplayer and dedicated servers behave identically and the effect syncs
 * to the client the usual way. There is no HUD indicator: the vanilla status
 * effect icon already says everything.
 */
public final class WitherEnchantment {

	private static ConfigHolder<WitherConfig> config;

	private WitherEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration. Call once from mod init,
	 * before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("wither", new WitherConfig());
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime. The values mirror Fire Aspect, the closest vanilla equivalent.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		SwordEnchantments.register(registry, ModEnchantments.WITHER, 2, 3, 10, 60, 4);
	}

	/**
	 * Applies the effect after a successful hit.
	 *
	 * @param attack the hit that landed
	 */
	public static void onHit(MeleeAttack attack) {
		// Nothing to wither on a target the hit already killed.
		if (!attack.target().isAlive()) {
			return;
		}

		WitherConfig settings = config.get();
		int level = attack.levelOf(ModEnchantments.WITHER, settings);
		if (level <= 0) {
			return;
		}

		int duration = settings.durationTicksPerLevel * level;
		if (duration <= 0) {
			return;
		}

		attack.target().addStatusEffect(
				new StatusEffectInstance(StatusEffects.WITHER, duration, settings.amplifier),
				attack.attacker());
	}
}
