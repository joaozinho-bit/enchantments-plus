package pt.joao.enchantmentsplus.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import pt.joao.enchantmentsplus.enchantment.CrushEnchantment;

/**
 * Entity lifecycle, and the enchantments that care about it.
 *
 * <p>Kept apart from {@link AttackEvents} because it answers a different
 * question: not "who hit whom", but "something an enchantment spawned has
 * finished". Listening for the removal is what lets an enchantment react to a
 * spawned entity without ticking it every frame.
 */
public final class EntityEvents {

	private EntityEvents() {
	}

	/** Registers the listeners. Call once from mod init. */
	public static void init() {
		ServerEntityEvents.ENTITY_UNLOAD.register(EntityEvents::onUnload);
	}

	private static void onUnload(Entity entity, ServerWorld world) {
		CrushEnchantment.onEntityRemoved(entity, world);
	}
}
