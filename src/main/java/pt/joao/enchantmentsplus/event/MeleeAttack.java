package pt.joao.enchantmentsplus.event;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * One melee hit that actually landed: who hit whom, with what.
 *
 * <p>Every on-hit enchantment needs the same three things and the same question
 * answered ("am I on this weapon, and at which level?"), so they live here
 * instead of in each enchantment. {@link AttackEvents} builds one of these once
 * per hit and hands the same instance to everyone interested.
 *
 * @param attacker the entity that dealt the damage
 * @param target   the entity that was hit
 * @param weapon   the non-empty stack used for the attack
 */
public record MeleeAttack(LivingEntity attacker, LivingEntity target, ItemStack weapon) {

	/**
	 * Resolves the level an enchantment is effectively acting at.
	 *
	 * <p>This folds together the three checks every enchantment would otherwise
	 * repeat: is it switched on, is it on the weapon at all, and is its level
	 * within the configured cap. The cap matters because an enchantment's own
	 * maximum is baked into its datapack file at build time, so lowering it in
	 * the config can only take effect here.
	 *
	 * @param enchantment the enchantment to look for
	 * @param config      that enchantment's live configuration
	 * @return the level to act at, or {@code 0} to do nothing
	 */
	public int levelOf(RegistryKey<Enchantment> enchantment, EnchantmentConfig config) {
		if (!config.isEnabled()) {
			return 0;
		}
		return Math.min(level(enchantment), config.getMaxLevel());
	}

	/**
	 * Enchantments are data-driven, so the entry has to come from the world's
	 * registries rather than from a constant.
	 */
	private int level(RegistryKey<Enchantment> enchantment) {
		return attacker.getWorld().getRegistryManager()
				.get(RegistryKeys.ENCHANTMENT)
				.getEntry(enchantment)
				.map(entry -> EnchantmentHelper.getLevel(entry, weapon))
				.orElse(0);
	}
}
