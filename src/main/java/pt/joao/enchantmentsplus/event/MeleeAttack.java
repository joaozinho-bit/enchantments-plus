package pt.joao.enchantmentsplus.event;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import pt.joao.enchantmentsplus.config.EnchantmentConfig;
import pt.joao.enchantmentsplus.util.EnchantmentLevels;

/**
 * One melee hit that actually landed: who hit whom, with what.
 *
 * <p>Every on-hit enchantment needs the same three things, so they live here
 * instead of in each enchantment. {@link AttackEvents} builds one of these once
 * per hit and hands the same instance to everyone interested.
 *
 * @param attacker the entity that dealt the damage
 * @param target   the entity that was hit
 * @param weapon   the non-empty stack used for the attack
 */
public record MeleeAttack(LivingEntity attacker, LivingEntity target, ItemStack weapon) {

	/**
	 * Resolves the level an enchantment is effectively acting at, for the
	 * weapon behind this hit.
	 *
	 * @param enchantment the enchantment to look for
	 * @param config      that enchantment's live configuration
	 * @return the level to act at, or {@code 0} to do nothing
	 */
	public int levelOf(RegistryKey<Enchantment> enchantment, EnchantmentConfig config) {
		return EnchantmentLevels.effective(attacker.getWorld(), weapon, enchantment, config);
	}
}
