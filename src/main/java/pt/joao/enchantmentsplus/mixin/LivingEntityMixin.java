package pt.joao.enchantmentsplus.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import pt.joao.enchantmentsplus.event.DropEvents;

/**
 * Hands a dead entity's loot to the mod before it hits the ground.
 *
 * <p>Vanilla pours the loot table straight into a sink that spawns item
 * entities, so there is no list to intercept the way there is for blocks. What
 * there is, is that sink: replacing it leaves the table, the seed and Looting
 * completely untouched and only changes what happens to each stack once it has
 * been rolled.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@ModifyArg(
			method = "dropLoot",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/loot/LootTable;generateLoot"
							+ "(Lnet/minecraft/loot/context/LootContextParameterSet;JLjava/util/function/Consumer;)V"),
			index = 2)
	private Consumer<ItemStack> enchantmentsPlus$redirectLoot(Consumer<ItemStack> vanilla) {
		return DropEvents.onLootDropped((LivingEntity) (Object) this, vanilla);
	}
}
