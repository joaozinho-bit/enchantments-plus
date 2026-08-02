package pt.joao.enchantmentsplus.effect;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import pt.joao.enchantmentsplus.config.EnchantmentConfig;

/**
 * One enchantment whose effect the player switches on and off by hand.
 *
 * <p>Everything {@link ToggleManager} needs in order to run a toggle without
 * knowing what it does: where to look for the enchantment, how to tell whether
 * it is switched on at all, and the two callbacks that turn the effect on and
 * off. An enchantment describes itself once and never touches the state, the
 * networking or the message that follows.
 *
 * <p>Both callbacks must be safe to call at any time and as often as the manager
 * likes, because it re-derives rather than tracks: {@code onEnable} is expected
 * to rewrite its effect from scratch and {@code onDisable} to do nothing when
 * there is nothing to undo. That is what makes it impossible for an effect to
 * drift, stack up or survive something it should not.
 *
 * @param id          identifies the toggle everywhere: on the wire, in the
 *                    player's set of enabled effects, and as the translation key
 *                    for the message shown when it changes
 * @param enchantment the enchantment the worn piece must carry
 * @param slot        the equipment slot that piece is worn in
 * @param config      that enchantment's live configuration, read through the
 *                    holder so a reload is picked up
 * @param onEnable    turns the effect on, at the level the piece grants
 * @param onDisable   turns the effect off
 * @param modes       the modes the player can step through with a double tap,
 *                    or {@code null} when the effect is simply on or off
 */
public record ToggleableEffect(
		Identifier id,
		RegistryKey<Enchantment> enchantment,
		EquipmentSlot slot,
		Supplier<? extends EnchantmentConfig> config,
		Activation onEnable,
		Consumer<ServerPlayerEntity> onDisable,
		Modes modes) {

	/**
	 * Describes a toggle that is put on the player and taken off again.
	 *
	 * <p>For an effect that is a thing <em>done</em> to them &mdash; an
	 * attribute, a status effect &mdash; and that therefore has to be applied
	 * while it runs and undone when it stops. It is either on or off; there is
	 * nothing to choose between.
	 *
	 * @param id          identifies the toggle everywhere, as above
	 * @param enchantment the enchantment the worn piece must carry
	 * @param slot        the slot that piece is in
	 * @param config      that enchantment's live configuration
	 * @param onEnable    turns the effect on
	 * @param onDisable   turns the effect off
	 * @return an effect with no modes
	 */
	public static ToggleableEffect applied(Identifier id, RegistryKey<Enchantment> enchantment,
			EquipmentSlot slot, Supplier<? extends EnchantmentConfig> config,
			Activation onEnable, Consumer<ServerPlayerEntity> onDisable) {
		return new ToggleableEffect(id, enchantment, slot, config, onEnable, onDisable, null);
	}

	/**
	 * Describes a toggle that is asked about rather than applied.
	 *
	 * <p>Some effects are a thing done to the player &mdash; an attribute, a
	 * status effect &mdash; and have to be put on and taken off. Others are a
	 * standing answer to a question asked later, such as "should this swing
	 * widen?", and there is nothing to switch on in advance. Those register
	 * through here and read {@link ToggleManager#activeLevel} when the moment
	 * comes.
	 *
	 * @param id          identifies the toggle everywhere, as above
	 * @param enchantment the enchantment the held or worn piece must carry
	 * @param slot        the slot that piece is in
	 * @param config      that enchantment's live configuration
	 * @return an effect with nothing to apply or undo
	 */
	public static ToggleableEffect queried(Identifier id, RegistryKey<Enchantment> enchantment,
			EquipmentSlot slot, Supplier<? extends EnchantmentConfig> config) {
		return new ToggleableEffect(id, enchantment, slot, config, (player, level) -> {
		}, player -> {
		}, null);
	}

	/**
	 * A {@link #queried} effect that also has modes to step through.
	 *
	 * @param modes the modes, stepped through by double-tapping the key
	 * @return an effect with nothing to apply or undo, and something to choose
	 */
	public static ToggleableEffect cycled(Identifier id, RegistryKey<Enchantment> enchantment,
			EquipmentSlot slot, Supplier<? extends EnchantmentConfig> config, Modes modes) {
		return new ToggleableEffect(id, enchantment, slot, config, (player, level) -> {
		}, player -> {
		}, modes);
	}

	/** Turns an effect on for a player at a given enchantment level. */
	@FunctionalInterface
	public interface Activation {

		/**
		 * @param player the player to act on
		 * @param level  the level the worn piece grants, always {@code > 0}
		 */
		void apply(ServerPlayerEntity player, int level);
	}

	/**
	 * The modes one effect can be switched between without being switched off.
	 *
	 * <p>Some effects are not one thing but a small family of them, and which
	 * one is wanted changes from minute to minute. Making that a second key
	 * would cost the player a binding for every effect that has modes; making it
	 * a second press of the one they already know costs nothing.
	 *
	 * <p>The mode is named rather than numbered because the name travels to the
	 * client, is translated for the message, and has to survive an effect gaining
	 * or reordering its modes.
	 */
	public interface Modes {

		/**
		 * @param player the player to ask about
		 * @param level  the level their gear grants, always {@code > 0}
		 * @return the name of the mode they are on
		 */
		String current(ServerPlayerEntity player, int level);

		/**
		 * Moves the player to the next mode their level allows.
		 *
		 * @param player the player to move
		 * @param level  the level their gear grants, always {@code > 0}
		 * @return the name of the mode they are now on
		 */
		String next(ServerPlayerEntity player, int level);
	}
}
