package pt.joao.enchantmentsplus.enchantment;

import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.Registerable;
import net.minecraft.server.world.ServerWorld;
import pt.joao.enchantmentsplus.config.ConfigHolder;
import pt.joao.enchantmentsplus.config.ConfigManager;
import pt.joao.enchantmentsplus.event.MinedBlock;
import pt.joao.enchantmentsplus.registry.ModEnchantments;

/**
 * Auto Smelt: what you dig comes out of the ground already cooked.
 *
 * <p>What smelts into what is never written down here. Each drop is offered to
 * the game's own furnace recipes, and to blast furnace recipes after that, so
 * the enchantment knows about exactly the ores, sands and stones the pack
 * knows about &mdash; including everything a mod adds, the moment it adds it.
 * A drop with no cooking recipe is left completely alone.
 *
 * <p>Counts are carried across rather than assumed: a recipe smelts one item,
 * so eight raw iron from a lucky Fortune roll become eight ingots.
 */
public final class AutoSmeltEnchantment {

	private static ConfigHolder<AutoSmeltConfig> config;

	private AutoSmeltEnchantment() {
	}

	/**
	 * Registers the enchantment's configuration. Call once from mod init,
	 * before {@link ConfigManager#load()} reads the file.
	 */
	public static void init() {
		config = ConfigManager.register("auto_smelt", new AutoSmeltConfig());
	}

	/**
	 * Builds the data-driven definition. Called from data generation, never at
	 * runtime. Rare and expensive: skipping the furnace entirely is worth a lot
	 * more than it costs to say.
	 *
	 * @param registry the registry being bootstrapped
	 */
	public static void bootstrap(Registerable<Enchantment> registry) {
		EnchantmentDefinitions.mining(registry, ModEnchantments.AUTO_SMELT, 1, 1, 20, 70, 8);
	}

	/**
	 * Replaces every drop that has a cooking recipe with its result.
	 *
	 * @param mined the break, with its drops still mutable
	 */
	public static void onMined(MinedBlock mined) {
		if (mined.levelOf(ModEnchantments.AUTO_SMELT, config.get()) <= 0) {
			return;
		}

		List<ItemStack> drops = mined.drops();
		for (int i = 0; i < drops.size(); i++) {
			ItemStack cooked = cook(mined.world(), drops.get(i));
			if (!cooked.isEmpty()) {
				drops.set(i, cooked);
			}
		}
	}

	/**
	 * @return what this stack cooks into, or an empty stack when the game has
	 *         no recipe for it
	 */
	private static ItemStack cook(ServerWorld world, ItemStack drop) {
		if (drop.isEmpty()) {
			return ItemStack.EMPTY;
		}

		SingleStackRecipeInput input = new SingleStackRecipeInput(drop);
		ItemStack result = craft(world, input, RecipeType.SMELTING);
		if (result.isEmpty()) {
			// Blast furnace second, so a mod that only registers the fast
			// recipe still works.
			result = craft(world, input, RecipeType.BLASTING);
		}
		if (result.isEmpty()) {
			return ItemStack.EMPTY;
		}

		// One input item yields one recipe result, so a stack of eight raw ore
		// yields eight times what the recipe produces.
		result.setCount(result.getCount() * drop.getCount());
		return result;
	}

	private static <T extends AbstractCookingRecipe> ItemStack craft(ServerWorld world,
			SingleStackRecipeInput input, RecipeType<T> type) {
		return world.getRecipeManager()
				.getFirstMatch(type, input, world)
				.map(recipe -> recipe.value().craft(input, world.getRegistryManager()))
				.orElse(ItemStack.EMPTY);
	}
}
