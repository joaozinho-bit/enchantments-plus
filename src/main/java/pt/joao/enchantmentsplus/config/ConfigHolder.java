package pt.joao.enchantmentsplus.config;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A stable, typed handle to one enchantment's live configuration.
 *
 * <p>Enchantments keep the holder returned by
 * {@link ConfigManager#register(String, EnchantmentConfig)} and always read
 * through {@link #get()}, so a later reload transparently swaps in the new
 * values without the enchantment holding a stale reference.
 *
 * @param <T> the concrete config type
 */
public final class ConfigHolder<T extends EnchantmentConfig> {

	private final T defaults;
	private volatile T value;

	ConfigHolder(T defaults) {
		this.defaults = defaults;
		this.value = defaults;
	}

	/** @return the currently loaded configuration */
	public T get() {
		return value;
	}

	/**
	 * Rebuilds the value by overlaying the persisted fields on top of the
	 * defaults. Starting from the defaults means options missing from the file
	 * (for example a newly added one) keep their default instead of a zero
	 * value, and unknown/stale keys are ignored.
	 */
	@SuppressWarnings("unchecked")
	void reload(Gson gson, JsonElement persisted) {
		JsonObject merged = gson.toJsonTree(defaults).getAsJsonObject();
		if (persisted != null && persisted.isJsonObject()) {
			for (Map.Entry<String, JsonElement> field : persisted.getAsJsonObject().entrySet()) {
				if (merged.has(field.getKey())) {
					merged.add(field.getKey(), field.getValue());
				}
			}
		}
		this.value = (T) gson.fromJson(merged, defaults.getClass());
	}
}
