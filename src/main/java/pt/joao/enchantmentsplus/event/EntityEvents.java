package pt.joao.enchantmentsplus.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import pt.joao.enchantmentsplus.enchantment.CrushEnchantment;
import pt.joao.enchantmentsplus.enchantment.HeartyEnchantment;

/**
 * Entity lifecycle and equipment, and the enchantments that care about them.
 *
 * <p>Kept apart from {@link AttackEvents} because these answer a different
 * question: not "who hit whom", but "what is this entity wearing" and "has
 * something an enchantment spawned finished". Reacting to the changes is what
 * lets enchantments stay correct without anything being checked every tick.
 */
public final class EntityEvents {

	private EntityEvents() {
	}

	/** Registers the listeners. Call once from mod init. */
	public static void init() {
		ServerEntityEvents.ENTITY_UNLOAD.register(EntityEvents::onUnload);
		ServerEntityEvents.EQUIPMENT_CHANGE.register(EntityEvents::onEquipmentChange);
		ServerEntityEvents.ENTITY_LOAD.register(EntityEvents::onLoad);
	}

	private static void onUnload(Entity entity, ServerWorld world) {
		CrushEnchantment.onEntityRemoved(entity, world);
	}

	private static void onEquipmentChange(LivingEntity entity, EquipmentSlot slot,
			ItemStack previous, ItemStack current) {
		// Only the four worn pieces can carry an armour enchantment; a weapon
		// swap must not cost anything here.
		if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
			HeartyEnchantment.refresh(entity);
		}
	}

	private static void onLoad(Entity entity, ServerWorld world) {
		// Joining, respawning and changing dimension all arrive here. Mobs keep
		// their bonus in their own saved attributes, so only players need the
		// re-check, which is also what makes a config change take effect on the
		// next login rather than silently persisting.
		if (entity instanceof ServerPlayerEntity player) {
			HeartyEnchantment.refresh(player);
		}
	}
}
