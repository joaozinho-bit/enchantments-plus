package pt.joao.enchantmentsplus.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import pt.joao.enchantmentsplus.client.health.HealthAdjustment;

/**
 * Stops losing maximum health from looking like being hit.
 *
 * <p>{@code ClientPlayerEntity#updateHealth} treats any drop in health the
 * server reports as damage: it sets {@code hurtTime}, the red flash and camera
 * tilt, and {@code timeUntilRegen}, which also makes the hearts shake. That is
 * right for a hit, but wrong when the health only fell because the
 * <em>maximum</em> fell &mdash; taking off enchanted armour is not an injury.
 *
 * <p>The two are told apart by the server having said so, not by guessing: see
 * {@link HealthAdjustment}. Anything outside that window, including reaching
 * zero, behaves exactly as vanilla.
 *
 * <p>Purely cosmetic: the health value itself is still applied, so the client
 * stays in step with the server.
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

	@Inject(method = "updateHealth", at = @At("HEAD"), cancellable = true)
	private void enchantmentsPlus$skipMaxHealthClamp(float health, CallbackInfo ci) {
		if (!HealthAdjustment.isPending()) {
			return;
		}

		LivingEntity self = (LivingEntity) (Object) this;
		if (health < self.getHealth()) {
			self.setHealth(health);
			ci.cancel();
		}
	}
}
