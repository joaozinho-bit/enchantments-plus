package pt.joao.enchantmentsplus.enchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.Registerable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.event.MeleeAttack;
import pt.joao.enchantmentsplus.registry.ModEnchantments;

/**
 * Crush: a sword that occasionally drops an anvil on what it hits.
 *
 * <p>The anvil is a real {@link FallingBlockEntity}, so the fall, the gravity,
 * the landing sound and the shattering particles are all vanilla &mdash; the
 * mod only chooses where it appears and what happens when it lands. Two vanilla
 * switches keep it from touching the world: it is marked as destroyed on
 * landing, so it never becomes a block, and its item drop is turned off, so it
 * can never be picked up.
 *
 * <p>Because the anvil takes a moment to fall, the target has time to move.
 * Damage is therefore applied in a small configurable radius around wherever it
 * actually lands, rather than to the entity that was originally hit.
 */
public final class CrushEnchantment {

	private static ConfigHolder<CrushConfig> config;

	/**
	 * Anvils on their way down, mapped to the attacker that summoned them.
	 *
	 * <p>Only holds entries while something is actually falling, and each one is
	 * removed the moment its anvil leaves the world, so nothing is ever polled
	 * and nothing accumulates.
	 */
	private static final Map<UUID, UUID> FALLING = new HashMap<>();

	private CrushEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration. Call once from mod init,
	 * before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("crush", new CrushConfig());
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		SwordEnchantments.register(registry, ModEnchantments.CRUSH, 2, 3, 15, 65, 4);
	}

	/**
	 * Rolls for an anvil after a successful hit.
	 *
	 * @param attack the hit that landed
	 */
	public static void onHit(MeleeAttack attack) {
		CrushConfig settings = config.get();
		int level = attack.levelOf(ModEnchantments.CRUSH, settings);
		if (level <= 0) {
			return;
		}

		if (!(attack.target().getWorld() instanceof ServerWorld world)) {
			return;
		}
		if (world.getRandom().nextFloat() >= settings.chancePerLevel * level) {
			return;
		}

		Vec3d above = attack.target().getPos().add(0.0, settings.fallHeight, 0.0);
		BlockPos spawn = BlockPos.ofFloored(above);

		// Vanilla clears the block an anvil starts from, so only ever start in
		// air: clearing air changes nothing, clearing a ceiling would delete it.
		if (!world.getBlockState(spawn).isAir()) {
			return;
		}

		FallingBlockEntity anvil = FallingBlockEntity.spawnFromBlock(world, spawn, Blocks.ANVIL.getDefaultState());
		anvil.setDestroyedOnLanding();
		anvil.dropItem = false;
		FALLING.put(anvil.getUuid(), attack.attacker().getUuid());
	}

	/**
	 * An entity left the world; if it was one of our anvils, this is its
	 * impact.
	 *
	 * <p>Called from {@link pt.joao.enchantmentsplus.event.EntityEvents}. Only
	 * {@code DISCARDED} counts as a landing: an anvil that merely rode out of a
	 * loaded chunk is dropped from the map without hurting anything.
	 *
	 * @param entity the entity being removed
	 * @param world  the world it was in
	 */
	public static void onEntityRemoved(Entity entity, ServerWorld world) {
		if (FALLING.isEmpty() || !(entity instanceof FallingBlockEntity)) {
			return;
		}

		UUID attacker = FALLING.remove(entity.getUuid());
		if (attacker == null || entity.getRemovalReason() != Entity.RemovalReason.DISCARDED) {
			return;
		}

		CrushConfig settings = config.get();
		Box area = new Box(entity.getPos(), entity.getPos()).expand(settings.impactRadius);
		DamageSource source = world.getDamageSources().fallingAnvil(entity);

		for (LivingEntity victim : world.getNonSpectatingEntities(LivingEntity.class, area)) {
			// The player who swung the sword is spared; being punished for
			// using your own enchantment would be a surprise, not a trade-off.
			if (!victim.getUuid().equals(attacker)) {
				victim.damage(source, settings.damage);
			}
		}
	}
}
