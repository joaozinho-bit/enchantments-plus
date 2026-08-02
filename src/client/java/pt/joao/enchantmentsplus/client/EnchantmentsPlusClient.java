package pt.joao.enchantmentsplus.client;

import net.fabricmc.api.ClientModInitializer;
import pt.joao.enchantmentsplus.client.health.HealthAdjustment;
import pt.joao.enchantmentsplus.client.hud.HudManager;
import pt.joao.enchantmentsplus.client.jump.JumpInput;
import pt.joao.enchantmentsplus.client.outline.AreaOutline;
import pt.joao.enchantmentsplus.client.toggle.ToggleKeys;
import pt.joao.enchantmentsplus.client.toggle.ToggleState;

/**
 * Client entry point. Wires up client-only infrastructure such as the HUD;
 * per-enchantment client logic hooks into these systems rather than living here.
 */
public class EnchantmentsPlusClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HudManager.init();
		HealthAdjustment.init();
		JumpInput.init();
		ToggleKeys.init();
		ToggleState.init();
		AreaOutline.init();
	}
}
