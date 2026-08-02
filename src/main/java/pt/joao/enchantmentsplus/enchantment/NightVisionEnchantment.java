package pt.joao.enchantmentsplus.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registerable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import pt.joao.enchantmentsplus.EnchantmentsPlus;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.effect.ToggleManager;
import pt.joao.enchantmentsplus.effect.ToggleableEffect;
import pt.joao.enchantmentsplus.registry.ModEnchantments;

/**
 * Night Vision: a helmet that can be told to light the world up.
 *
 * <p>Left on permanently it would simply delete the night, and with it torches,
 * caves being frightening and any reason to look for a light source. Being able
 * to switch it off is the point: the player decides when they would rather see
 * than be somewhere dark.
 *
 * <p>The effect is granted with an <em>infinite</em> duration, which is what
 * makes it steady. Vanilla dims and pulses night vision over its last ten
 * seconds, so an effect topped up on a timer flickers every time it is renewed;
 * an infinite one is never within ten seconds of ending, and vanilla's own
 * strength calculation short-circuits to full brightness for it. Nothing has to
 * be re-applied on a tick, so there is no cost while it runs and nothing to
 * synchronise.
 *
 * <p>Only an infinite instance is ever taken away again, which is how a potion
 * the player drank survives being switched off. Vanilla has no infinite night
 * vision of its own, so this is an unambiguous mark of ownership.
 *
 * <p>Everything about when it runs belongs to
 * {@link pt.joao.enchantmentsplus.effect.ToggleManager}; this class only says
 * what "on" means.
 */
public final class NightVisionEnchantment {

	/** The toggle and the key binding are both this one thing. */
	public static final Identifier ID = EnchantmentsPlus.id("night_vision");

	private static ConfigHolder<NightVisionConfig> config;

	private NightVisionEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration and its toggle. Call once from
	 * mod init, before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("night_vision", new NightVisionConfig());
		ToggleManager.register(ToggleableEffect.applied(ID, ModEnchantments.NIGHT_VISION, EquipmentSlot.HEAD,
				config::get, NightVisionEnchantment::enable, NightVisionEnchantment::disable));
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime. Helmets only, so it can never be rolled onto another piece.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		EnchantmentDefinitions.helmet(registry, ModEnchantments.NIGHT_VISION, 2, 1, 20, 70, 4);
	}

	/**
	 * Grants the effect, unless it is already ours.
	 *
	 * <p>The guard is what keeps it steady: a refresh happens on every armour
	 * change, including the durability tick of the helmet itself, and re-adding
	 * the effect each time would send the client a packet and restart its fade
	 * for no reason at all.
	 */
	private static void enable(ServerPlayerEntity player, int level) {
		StatusEffectInstance active = player.getStatusEffect(StatusEffects.NIGHT_VISION);
		if (active != null && active.isInfinite()) {
			return;
		}

		// No particles, so it reads as a property of the helmet rather than as
		// something the player drank; the icon stays, because a toggle the player
		// cannot see the state of is a toggle they will lose track of.
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION,
				StatusEffectInstance.INFINITE, 0, false, false, true));
	}

	private static void disable(ServerPlayerEntity player) {
		StatusEffectInstance active = player.getStatusEffect(StatusEffects.NIGHT_VISION);
		if (active != null && active.isInfinite()) {
			player.removeStatusEffect(StatusEffects.NIGHT_VISION);
		}
	}
}
