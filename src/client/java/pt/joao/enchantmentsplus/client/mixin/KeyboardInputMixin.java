package pt.joao.enchantmentsplus.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import pt.joao.enchantmentsplus.client.jump.JumpInput;

/**
 * The one place a jump can still be turned into a wind-up.
 *
 * <p>Vanilla jumps on the tick the key goes down, before anyone could know
 * whether it was a tap or a hold. Correcting the flag immediately after the
 * keyboard is read is the narrowest seam that allows the two to be told apart:
 * everything downstream, including the jump itself, then behaves as if the
 * player had pressed exactly what they meant.
 *
 * <p>Entirely local, so an ordinary jump never waits on the server, and inert
 * for anyone not wearing the enchantment.
 */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {

	@Inject(method = "tick", at = @At("TAIL"))
	private void enchantmentsPlus$interpretJumpKey(boolean slowDown, float tickDelta, CallbackInfo ci) {
		JumpInput.filterJump((Input) (Object) this);
	}
}
