package pt.joao.enchantmentsplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import pt.joao.enchantmentsplus.event.AttackEvents;

/**
 * Turns vanilla's critical-hit decision into an event the mod can listen to.
 *
 * <p>Nothing in the Fabric API reports a critical hit, and re-deriving the
 * condition would mean copying half a dozen vanilla checks that could drift
 * with any update or any other mod. {@code addCritParticles} is instead reached
 * exactly once vanilla has already decided a hit was critical <em>and</em> it
 * dealt damage, and the server override is the server-side half of that, so
 * observing it needs no guards and no state of our own.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

	@Inject(method = "addCritParticles", at = @At("HEAD"))
	private void enchantmentsPlus$onCriticalHit(Entity target, CallbackInfo ci) {
		AttackEvents.onCriticalHit((ServerPlayerEntity) (Object) this, target);
	}
}
