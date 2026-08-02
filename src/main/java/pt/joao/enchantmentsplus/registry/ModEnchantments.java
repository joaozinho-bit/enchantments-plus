package pt.joao.enchantmentsplus.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import pt.joao.enchantmentsplus.EnchantmentsPlus;

/**
 * The single registry every enchantment goes through.
 *
 * <p>In 1.21.1 enchantments are data-driven: gameplay code only ever refers to
 * them by {@link RegistryKey}. This class is the one place those keys are
 * created and enumerated, so no key is ever built ad hoc and there is a single
 * list of everything the mod adds.
 *
 * <p>Adding an enchantment means one line here, declared as a constant so the
 * whole roster loads together:
 * <pre>{@code
 * public static final RegistryKey<Enchantment> STORM = register("storm");
 * }</pre>
 * The enchantment's own class then references {@code ModEnchantments.STORM} for
 * its behaviour, data generation, config and translations. The registry stays
 * completely independent of those systems &mdash; it knows nothing about the
 * HUD, timed effects or mod compatibility.
 */
public final class ModEnchantments {

	private static final List<RegistryKey<Enchantment>> REGISTERED = new ArrayList<>();

	/** Swords: hitting an entity withers it for longer at higher levels. */
	public static final RegistryKey<Enchantment> WITHER = register("wither");

	/** Swords: every hit heals back part of the damage dealt. */
	public static final RegistryKey<Enchantment> VAMPIRISM = register("vampirism");

	/** Swords: a critical hit may call lightning down on the target. */
	public static final RegistryKey<Enchantment> STORM = register("storm");

	/** Swords: a hit may drop an anvil on the target. */
	public static final RegistryKey<Enchantment> CRUSH = register("crush");

	/** Armour: every enchanted piece raises the wearer's maximum health. */
	public static final RegistryKey<Enchantment> HEARTY = register("hearty");

	/** Armour: a complete enchanted set makes the wearer immune to burning. */
	public static final RegistryKey<Enchantment> BURNING_PROTECTION = register("burning_protection");

	/** Tools and weapons: drops go straight into the inventory. */
	public static final RegistryKey<Enchantment> TELEKINESIS = register("telekinesis");

	/** Mining tools: drops come out already smelted. */
	public static final RegistryKey<Enchantment> AUTO_SMELT = register("auto_smelt");

	/** Pickaxes and shovels: mining without pause builds up speed. */
	public static final RegistryKey<Enchantment> MOMENTUM = register("momentum");

	/** Swords: a hit may set off a brief frenzy of faster attacks. */
	public static final RegistryKey<Enchantment> ATTACK_SPEED = register("attack_speed");

	/** Boots: a jump can be charged up, and its landing costs nothing. */
	public static final RegistryKey<Enchantment> JUMP = register("jump");

	/** Anything with durability: the item stops wearing out. */
	public static final RegistryKey<Enchantment> ETERNAL = register("eternal");

	/** Boots: movement speed the wearer switches on and off. */
	public static final RegistryKey<Enchantment> SPEED = register("speed");

	/** Helmets: night vision the wearer switches on and off. */
	public static final RegistryKey<Enchantment> NIGHT_VISION = register("night_vision");

	/** Pickaxes and shovels: a swing that can be widened to a whole face. */
	public static final RegistryKey<Enchantment> EXCAVATOR = register("excavator");

	private ModEnchantments() {
	}

	/**
	 * Creates and records one enchantment key. This is the single entry point
	 * for registering an enchantment; nothing else should build a
	 * {@link RegistryKey} for the {@code enchantment} registry.
	 *
	 * @param name the registry path, e.g. {@code "storm"}
	 * @return the key identifying {@code enchantments-plus:<name>}
	 */
	public static RegistryKey<Enchantment> register(String name) {
		RegistryKey<Enchantment> key = RegistryKey.of(RegistryKeys.ENCHANTMENT, EnchantmentsPlus.id(name));
		if (REGISTERED.contains(key)) {
			throw new IllegalArgumentException("Enchantment already registered: " + name);
		}
		REGISTERED.add(key);
		return key;
	}

	/**
	 * @return every registered enchantment key, in registration order; used by
	 *         consumers such as data generation to iterate the whole roster
	 */
	public static List<RegistryKey<Enchantment>> registered() {
		return Collections.unmodifiableList(REGISTERED);
	}
}
