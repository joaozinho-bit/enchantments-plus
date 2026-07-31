package pt.joao.enchantmentsplus.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import pt.joao.enchantmentsplus.EnchantmentsPlus;

/**
 * Single entry point for every enchantment's configuration.
 *
 * <p>Each enchantment registers its config once with {@link #register}; a single
 * call to {@link #load()} then reads {@code config/enchantments-plus.json},
 * applies any user edits on top of the defaults and writes the file back so it
 * always lists every registered option. Adding a new enchantment never requires
 * touching this class.
 */
public final class ConfigManager {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final Path FILE =
			FabricLoader.getInstance().getConfigDir().resolve(EnchantmentsPlus.MOD_ID + ".json");

	private static final Map<String, ConfigHolder<?>> HOLDERS = new LinkedHashMap<>();

	private ConfigManager() {
	}

	/**
	 * Registers an enchantment's configuration. Call once at start-up, before
	 * {@link #load()}.
	 *
	 * @param id       the enchantment id, used as the section name in the file
	 * @param defaults a fresh config instance carrying the default values
	 * @return a handle used to read the live values later
	 */
	public static <T extends EnchantmentConfig> ConfigHolder<T> register(String id, T defaults) {
		if (HOLDERS.containsKey(id)) {
			throw new IllegalArgumentException("Duplicate config id: " + id);
		}
		ConfigHolder<T> holder = new ConfigHolder<>(defaults);
		HOLDERS.put(id, holder);
		return holder;
	}

	/** Loads every registered config from disk and rewrites the file in sync. */
	public static void load() {
		if (HOLDERS.isEmpty()) {
			return;
		}

		JsonObject root = read();
		JsonObject normalized = new JsonObject();
		for (Map.Entry<String, ConfigHolder<?>> entry : HOLDERS.entrySet()) {
			ConfigHolder<?> holder = entry.getValue();
			holder.reload(GSON, root.get(entry.getKey()));
			normalized.add(entry.getKey(), GSON.toJsonTree(holder.get()));
		}
		write(normalized);
	}

	private static JsonObject read() {
		if (!Files.exists(FILE)) {
			return new JsonObject();
		}
		try {
			JsonElement parsed = JsonParser.parseString(Files.readString(FILE));
			if (parsed.isJsonObject()) {
				return parsed.getAsJsonObject();
			}
		} catch (IOException | RuntimeException e) {
			EnchantmentsPlus.LOGGER.warn("Could not read config {}, falling back to defaults", FILE, e);
		}
		return new JsonObject();
	}

	private static void write(JsonObject root) {
		try {
			Files.createDirectories(FILE.getParent());
			Files.writeString(FILE, GSON.toJson(root));
		} catch (IOException e) {
			EnchantmentsPlus.LOGGER.warn("Could not write config {}", FILE, e);
		}
	}
}
