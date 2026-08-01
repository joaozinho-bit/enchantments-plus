package pt.joao.enchantmentsplus.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registerable;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.event.MeleeAttack;
import pt.joao.enchantmentsplus.registry.ModEnchantments;

/**
 * Vampirism: a sword that gives back part of what it takes.
 *
 * <p>Every hit heals, with no roll involved: the reward is proportional to the
 * damage that actually got through, so armoured targets return less and the
 * enchantment never turns into a flat regeneration. Higher levels only raise
 * the share.
 *
 * <p>The healing deliberately also applies to the killing blow, which is why
 * {@link pt.joao.enchantmentsplus.event.AttackEvents} does not require a living
 * target. Overhealing is impossible: vanilla clamps to the maximum health.
 */
public final class VampirismEnchantment {

	private static ConfigHolder<VampirismConfig> config;

	private VampirismEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration. Call once from mod init,
	 * before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("vampirism", new VampirismConfig());
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime. Slightly pricier than Wither, since lifesteal on every swing is
	 * worth more than a damage-over-time effect.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		EnchantmentDefinitions.sword(registry, ModEnchantments.VAMPIRISM, 2, 3, 15, 65, 4);
	}

	/**
	 * Heals the attacker after a successful hit.
	 *
	 * @param attack       the hit that landed
	 * @param damageDealt  the damage the target actually took
	 */
	public static void onHit(MeleeAttack attack, float damageDealt) {
		if (damageDealt <= 0.0F) {
			return;
		}

		VampirismConfig settings = config.get();
		int level = attack.levelOf(ModEnchantments.VAMPIRISM, settings);
		if (level <= 0) {
			return;
		}

		float healed = (float) (damageDealt * settings.healFractionPerLevel * level);
		if (healed <= 0.0F) {
			return;
		}

		attack.attacker().heal(healed);
	}
}
