package pt.joao.enchantmentsplus;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.effect.TimedEffectManager;
import pt.joao.enchantmentsplus.registry.ModEnchantments;

/**
 * Common entry point of the mod, loaded on both the client and the server.
 *
 * <p>Its only job is to bootstrap the shared infrastructure. Every enchantment
 * lives in its own class under {@code enchantment} and plugs into the systems
 * provided by the sibling packages ({@code registry}, {@code effect},
 * {@code event}, {@code config}, ...), so this class should stay small.
 */
public class EnchantmentsPlus implements ModInitializer {
	public static final String MOD_ID = "enchantments-plus";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Enchantments Plus");

		// Touch the registry so the whole enchantment roster is loaded and
		// registered up front.
		LOGGER.info("Registered {} enchantment(s)", ModEnchantments.registered().size());

		// Server-side systems, shared by singleplayer and multiplayer.
		TimedEffectManager.init();

		// Enchantments register their config before this call; load() then reads
		// the file and writes back any missing defaults.
		ConfigManager.load();
	}

	/**
	 * Builds an {@link Identifier} namespaced to this mod.
	 *
	 * @param path the path part of the identifier
	 * @return an identifier of the form {@code enchantments-plus:<path>}
	 */
	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
