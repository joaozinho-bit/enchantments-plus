package pt.joao.enchantmentsplus.event;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import pt.joao.enchantmentsplus.enchantment.CrushEnchantment;
import pt.joao.enchantmentsplus.enchantment.StormEnchantment;
import pt.joao.enchantmentsplus.enchantment.VampirismEnchantment;
import pt.joao.enchantmentsplus.enchantment.WitherEnchantment;

/**
 * Melee hits, and the enchantments that care about them.
 *
 * <p>Listens once and dispatches, so an enchantment never registers an event of
 * its own and the guards shared by all of them are written a single time. A new
 * on-hit enchantment adds one call below.
 *
 * <p>There are two entry points because vanilla decides the two things at
 * different moments:
 * <ul>
 * <li>{@code AFTER_DAMAGE} knows how much damage landed, but not whether the
 *     hit was critical;
 * <li>a critical hit is only confirmed afterwards, which the
 *     {@code ServerPlayerEntityMixin} forwards to {@link #onCriticalHit}.
 * </ul>
 * Both are server-side and event-driven, so nothing is ever polled and
 * singleplayer behaves exactly like a dedicated server.
 */
public final class AttackEvents {

	private AttackEvents() {
	}

	/** Registers the listeners. Call once from mod init. */
	public static void init() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register(AttackEvents::onAfterDamage);
	}

	private static void onAfterDamage(LivingEntity target, DamageSource source,
			float baseDamageTaken, float damageTaken, boolean blocked) {
		if (blocked) {
			return;
		}
		if (!(source.getAttacker() instanceof LivingEntity attacker)) {
			return;
		}

		// Null for anything that was not swung by hand, which is how indirect
		// damage such as an arrow or a potion is filtered out here.
		MeleeAttack attack = of(attacker, target, source.getWeaponStack());
		if (attack == null) {
			return;
		}

		WitherEnchantment.onHit(attack);
		VampirismEnchantment.onHit(attack, damageTaken);
		CrushEnchantment.onHit(attack);
	}

	/**
	 * A critical hit landed and dealt damage.
	 *
	 * <p>Called from the mixin on the one vanilla method that is reached
	 * exactly in that case, so the mod never has to decide for itself what
	 * counts as a critical hit. Only players can land one, and only on the
	 * server.
	 *
	 * @param attacker the player that landed the hit
	 * @param target   the entity that was hit
	 */
	public static void onCriticalHit(ServerPlayerEntity attacker, Entity target) {
		MeleeAttack attack = of(attacker, target, attacker.getMainHandStack());
		if (attack == null) {
			return;
		}

		StormEnchantment.onCriticalHit(attack);
	}

	/**
	 * @return the shared context for this hit, or {@code null} when there is
	 *         nothing an enchantment could act on
	 */
	@Nullable
	private static MeleeAttack of(LivingEntity attacker, Entity target, @Nullable ItemStack weapon) {
		if (weapon == null || weapon.isEmpty()) {
			return null;
		}
		if (!(target instanceof LivingEntity living)) {
			return null;
		}
		return new MeleeAttack(attacker, living, weapon);
	}
}
