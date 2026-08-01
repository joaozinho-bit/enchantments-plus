package pt.joao.enchantmentsplus.client;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import pt.joao.enchantmentsplus.enchantment.CrushEnchantment;
import pt.joao.enchantmentsplus.enchantment.HeartyEnchantment;
import pt.joao.enchantmentsplus.enchantment.StormEnchantment;
import pt.joao.enchantmentsplus.enchantment.VampirismEnchantment;
import pt.joao.enchantmentsplus.enchantment.WitherEnchantment;
import pt.joao.enchantmentsplus.registry.ModEnchantments;

/**
 * Writes the data-driven files the mod ships with.
 *
 * <p>Enchantments live in a dynamic registry in 1.21, so each one is really a
 * JSON file. Building them here instead of hand-writing that JSON keeps the
 * definition next to the enchantment's own code and lets the compiler check the
 * item tags and costs.
 */
public class EnchantmentsPlusDataGenerator implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(EnchantmentProvider::new);
	}

	@Override
	public void buildRegistry(RegistryBuilder registryBuilder) {
		registryBuilder.addRegistry(RegistryKeys.ENCHANTMENT, EnchantmentsPlusDataGenerator::bootstrapEnchantments);
	}

	/** One call per enchantment; each one owns its definition. */
	private static void bootstrapEnchantments(Registerable<Enchantment> registry) {
		WitherEnchantment.bootstrap(registry);
		VampirismEnchantment.bootstrap(registry);
		StormEnchantment.bootstrap(registry);
		CrushEnchantment.bootstrap(registry);
		HeartyEnchantment.bootstrap(registry);
	}

	/**
	 * Emits one file per enchantment in the roster, so adding an enchantment
	 * never means touching this provider.
	 */
	private static final class EnchantmentProvider extends FabricDynamicRegistryProvider {

		private EnchantmentProvider(FabricDataOutput output,
				CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
			super(output, registries);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
			RegistryWrapper.Impl<Enchantment> enchantments =
					registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
			for (RegistryKey<Enchantment> key : ModEnchantments.registered()) {
				entries.add(enchantments, key);
			}
		}

		@Override
		public String getName() {
			return "Enchantments";
		}
	}
}
