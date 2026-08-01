package pt.joao.enchantmentsplus.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.registry.Registerable;
import net.minecraft.server.world.ServerWorld;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.event.MeleeAttack;
import pt.joao.enchantmentsplus.registry.ModEnchantments;

/**
 * Storm: a sword that occasionally answers a critical hit with lightning.
 *
 * <p>Deliberately tied to critical hits rather than to every swing. A critical
 * hit is something the player earns by timing a jump, so the enchantment
 * rewards good combat instead of firing at random, and it stays rare enough not
 * to become the main source of damage.
 *
 * <p>The mod never decides for itself what counts as a critical hit: it hooks
 * the one vanilla method that is only reached once vanilla has already made
 * that call, which also means it keeps agreeing with any mod that changes the
 * rules.
 *
 * <p>The bolt is summoned <em>cosmetic</em>. Vanilla skips both the fire it
 * would spread and the damage it would deal for cosmetic bolts, while the
 * flash and the thunder are played client-side and stay untouched. The
 * enchantment then applies its own configurable damage to the entity that was
 * hit, so the strike is felt without a single block ever catching fire &mdash;
 * safe on servers and around builds, whatever {@code doFireTick} is set to.
 */
public final class StormEnchantment {

	private static ConfigHolder<StormConfig> config;

	private StormEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration. Call once from mod init,
	 * before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("storm", new StormConfig());
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		SwordEnchantments.register(registry, ModEnchantments.STORM, 2, 3, 15, 65, 4);
	}

	/**
	 * Rolls for lightning after a critical hit.
	 *
	 * @param attack the critical hit that landed
	 */
	public static void onCriticalHit(MeleeAttack attack) {
		StormConfig settings = config.get();
		int level = attack.levelOf(ModEnchantments.STORM, settings);
		if (level <= 0) {
			return;
		}

		if (!(attack.target().getWorld() instanceof ServerWorld world)) {
			return;
		}
		if (world.getRandom().nextFloat() >= settings.chancePerLevel * level) {
			return;
		}

		LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
		if (lightning == null) {
			return;
		}

		lightning.refreshPositionAfterTeleport(attack.target().getPos());
		lightning.setCosmetic(true);
		world.spawnEntity(lightning);

		attack.target().damage(world.getDamageSources().lightningBolt(), settings.damage);
	}
}
