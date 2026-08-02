package pt.joao.enchantmentsplus.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import pt.joao.enchantmentsplus.enchantment.BurningProtectionEnchantment;
import pt.joao.enchantmentsplus.enchantment.JumpEnchantment;

/**
 * Incoming damage, and the enchantments that can turn it away.
 *
 * <p>The defensive counterpart to {@link AttackEvents}: that one asks who hit
 * whom, this one asks whether a hit should land at all. {@code ALLOW_DAMAGE}
 * runs before any damage is applied, so an enchantment that answers here
 * prevents the hit outright rather than healing it back afterwards.
 *
 * <p>This fires for every source of damage on the server, so anything asked
 * here must be cheap and give up early.
 */
public final class DamageEvents {

	private DamageEvents() {
	}

	/** Registers the listeners. Call once from mod init. */
	public static void init() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(DamageEvents::allowDamage);
	}

	private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
		return !BurningProtectionEnchantment.absorbs(entity, source)
				&& !JumpEnchantment.absorbsFall(entity, source);
	}
}
