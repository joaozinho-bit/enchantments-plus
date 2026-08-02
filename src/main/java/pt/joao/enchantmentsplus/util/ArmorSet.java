package pt.joao.enchantmentsplus.util;

import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKey;
import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * Answers questions about the armour someone is wearing as a <em>set</em>.
 *
 * <p>Armour enchantments all start from the same two questions &mdash; is this
 * piece enchanted, and how much of the set is &mdash; so the four slots are
 * enumerated once here rather than in every enchantment. Which slot the answer
 * came from matters to some of them and not to others, hence both a per-slot
 * and a counting view.
 *
 * <p>Nothing is cached. The answer is always derived from what the entity is
 * wearing at that instant, which is what lets a set bonus appear and vanish the
 * moment a piece is swapped, with no state to invalidate.
 */
public final class ArmorSet {

	/** The worn armour slots, from head to feet. */
	public static final List<EquipmentSlot> SLOTS =
			List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

	private ArmorSet() {
	}

	/**
	 * @param entity      the wearer
	 * @param slot        the slot to inspect
	 * @param enchantment the enchantment to look for
	 * @param config      that enchantment's live configuration
	 * @return whether the piece worn in this slot carries the enchantment
	 */
	public static boolean has(LivingEntity entity, EquipmentSlot slot,
			RegistryKey<Enchantment> enchantment, EnchantmentConfig config) {
		return EnchantmentLevels.effective(
				entity.getWorld(), entity.getEquippedStack(slot), enchantment, config) > 0;
	}

	/**
	 * Whether every one of the four worn pieces carries the enchantment.
	 *
	 * <p>Stops at the first piece that does not, so the common answer &mdash;
	 * "no" &mdash; usually costs a single lookup. Worth preferring over
	 * comparing {@link #count} to the set size on paths that run often.
	 *
	 * @param entity      the wearer
	 * @param enchantment the enchantment to look for
	 * @param config      that enchantment's live configuration
	 * @return {@code true} only for a complete set
	 */
	public static boolean isComplete(LivingEntity entity,
			RegistryKey<Enchantment> enchantment, EnchantmentConfig config) {
		for (EquipmentSlot slot : SLOTS) {
			if (!has(entity, slot, enchantment, config)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param entity      the wearer
	 * @param enchantment the enchantment to look for
	 * @param config      that enchantment's live configuration
	 * @return how many of the four worn pieces carry the enchantment, so
	 *         {@code SLOTS.size()} means a complete set
	 */
	public static int count(LivingEntity entity,
			RegistryKey<Enchantment> enchantment, EnchantmentConfig config) {
		int worn = 0;
		for (EquipmentSlot slot : SLOTS) {
			if (has(entity, slot, enchantment, config)) {
				worn++;
			}
		}
		return worn;
	}
}
