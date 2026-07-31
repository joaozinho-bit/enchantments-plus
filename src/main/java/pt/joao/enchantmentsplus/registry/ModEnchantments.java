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
