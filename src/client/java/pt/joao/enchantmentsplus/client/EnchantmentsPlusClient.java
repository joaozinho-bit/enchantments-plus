package pt.joao.enchantmentsplus.client;

import net.fabricmc.api.ClientModInitializer;
import pt.joao.enchantmentsplus.client.health.HealthAdjustment;
import pt.joao.enchantmentsplus.client.hud.HudManager;

/**
 * Client entry point. Wires up client-only infrastructure such as the HUD;
 * per-enchantment client logic hooks into these systems rather than living here.
 */
public class EnchantmentsPlusClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HudManager.init();
		HealthAdjustment.init();
	}
}
