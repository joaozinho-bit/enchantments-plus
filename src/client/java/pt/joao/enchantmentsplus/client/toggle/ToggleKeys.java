package pt.joao.enchantmentsplus.client.toggle;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import pt.joao.enchantmentsplus.EnchantmentsPlus;
import pt.joao.enchantmentsplus.enchantment.ExcavatorEnchantment;
import pt.joao.enchantmentsplus.enchantment.NightVisionEnchantment;
import pt.joao.enchantmentsplus.enchantment.SpeedEnchantment;
import pt.joao.enchantmentsplus.networking.ToggleSync;

/**
 * The keys that switch effects on and off, and nothing else.
 *
 * <p>One binding per toggleable enchantment, all under a single category so they
 * sit together in the Controls screen. The default key is only a default: the
 * binding is what vanilla actually reads, so rebinding it in the options works
 * with no help from here and takes effect immediately.
 *
 * <p>Nothing is decided on this side. A press becomes one packet naming the
 * effect, and the server settles whether the player is wearing the right piece,
 * what level it is and what changes. That keeps the client honest by
 * construction and means the state can never disagree between the two.
 *
 * <p>Adding a toggleable enchantment is one line in {@link #init()}; the id it
 * is given is the same one the server registered, so the two cannot drift apart
 * without the compiler saying so.
 */
public final class ToggleKeys {

	/** Groups the mod's keys in the Controls screen; Fabric adds it on first use. */
	private static final String CATEGORY = "category." + EnchantmentsPlus.MOD_ID;

	private static final List<Binding> BINDINGS = new ArrayList<>();

	private ToggleKeys() {
	}

	/**
	 * Registers the bindings and starts watching them. Call once from client
	 * init, which is early enough that the game has not yet built its options.
	 */
	public static void init() {
		bind(SpeedEnchantment.ID, GLFW.GLFW_KEY_PERIOD);
		bind(NightVisionEnchantment.ID, GLFW.GLFW_KEY_V);
		bind(ExcavatorEnchantment.ID, GLFW.GLFW_KEY_MINUS);

		ClientTickEvents.END_CLIENT_TICK.register(ToggleKeys::tick);
	}

	private static void bind(Identifier effect, int defaultKey) {
		KeyBinding binding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key." + effect.getNamespace() + "." + effect.getPath(),
				InputUtil.Type.KEYSYM, defaultKey, CATEGORY));
		BINDINGS.add(new Binding(binding, effect));
	}

	private static void tick(MinecraftClient client) {
		if (client.player == null || !ClientPlayNetworking.canSend(ToggleSync.Toggle.ID)) {
			return;
		}

		for (Binding binding : BINDINGS) {
			// Drains the presses vanilla queued, so a key pressed twice between
			// two ticks toggles twice rather than once.
			while (binding.key.wasPressed()) {
				ClientPlayNetworking.send(new ToggleSync.Toggle(binding.effect));
			}
		}
	}

	/** One key, and the effect it belongs to. */
	private record Binding(KeyBinding key, Identifier effect) {
	}
}
