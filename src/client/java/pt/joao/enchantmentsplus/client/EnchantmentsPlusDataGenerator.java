package pt.joao.enchantmentsplus.client;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import pt.joao.enchantmentsplus.enchantment.AttackSpeedEnchantment;
import pt.joao.enchantmentsplus.enchantment.AutoSmeltEnchantment;
import pt.joao.enchantmentsplus.enchantment.BurningProtectionEnchantment;
import pt.joao.enchantmentsplus.enchantment.CrushEnchantment;
import pt.joao.enchantmentsplus.enchantment.EternalEnchantment;
import pt.joao.enchantmentsplus.enchantment.ExcavatorEnchantment;
import pt.joao.enchantmentsplus.enchantment.HeartyEnchantment;
import pt.joao.enchantmentsplus.enchantment.JumpEnchantment;
import pt.joao.enchantmentsplus.enchantment.MomentumEnchantment;
import pt.joao.enchantmentsplus.enchantment.NightVisionEnchantment;
import pt.joao.enchantmentsplus.enchantment.SpeedEnchantment;
import pt.joao.enchantmentsplus.enchantment.StormEnchantment;
import pt.joao.enchantmentsplus.enchantment.TelekinesisEnchantment;
import pt.joao.enchantmentsplus.enchantment.VampirismEnchantment;
import pt.joao.enchantmentsplus.enchantment.WitherEnchantment;
import pt.joao.enchantmentsplus.registry.ModEnchantments;
import pt.joao.enchantmentsplus.registry.ModItemTags;

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
		pack.addProvider(ItemTagProvider::new);
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
		BurningProtectionEnchantment.bootstrap(registry);
		TelekinesisEnchantment.bootstrap(registry);
		AutoSmeltEnchantment.bootstrap(registry);
		MomentumEnchantment.bootstrap(registry);
		AttackSpeedEnchantment.bootstrap(registry);
		JumpEnchantment.bootstrap(registry);
		EternalEnchantment.bootstrap(registry);
		SpeedEnchantment.bootstrap(registry);
		NightVisionEnchantment.bootstrap(registry);
		ExcavatorEnchantment.bootstrap(registry);
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

	/** The item tags the mod defines, built out of vanilla ones. */
	private static final class ItemTagProvider extends FabricTagProvider.ItemTagProvider {

		private ItemTagProvider(FabricDataOutput output,
				CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
			super(output, registries);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup registries) {
			// Composed from vanilla tags rather than listing items, so any tool
			// or weapon a mod adds is already covered.
			getOrCreateTagBuilder(ModItemTags.TELEKINESIS_ENCHANTABLE)
					.addOptionalTag(ItemTags.MINING_ENCHANTABLE)
					.addOptionalTag(ItemTags.WEAPON_ENCHANTABLE);

			getOrCreateTagBuilder(ModItemTags.MOMENTUM_ENCHANTABLE)
					.addOptionalTag(ItemTags.PICKAXES)
					.addOptionalTag(ItemTags.SHOVELS);

			// The same two families as Momentum, kept apart so a pack can widen
			// a swing's reach without also widening a mining speed streak.
			getOrCreateTagBuilder(ModItemTags.EXCAVATOR_ENCHANTABLE)
					.addOptionalTag(ItemTags.PICKAXES)
					.addOptionalTag(ItemTags.SHOVELS);

			// Named separately from the vanilla tag it currently mirrors, so a
			// pack can widen it to a weapon mod without touching the mod.
			getOrCreateTagBuilder(ModItemTags.ATTACK_SPEED_ENCHANTABLE)
					.addOptionalTag(ItemTags.SWORD_ENCHANTABLE);
		}
	}
}
