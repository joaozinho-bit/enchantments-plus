package pt.joao.enchantmentsplus.client.outline;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import pt.joao.enchantmentsplus.client.mixin.WorldRendererAccessor;
import pt.joao.enchantmentsplus.client.toggle.ToggleState;
import pt.joao.enchantmentsplus.enchantment.ExcavatorEnchantment;
import pt.joao.enchantmentsplus.registry.ModEnchantments;
import pt.joao.enchantmentsplus.util.EnchantmentLevels;
import pt.joao.enchantmentsplus.util.MiningArea;
import pt.joao.enchantmentsplus.util.MiningShape;

/**
 * Shows what a widened swing is about to cost, before it is taken.
 *
 * <p>Drawn as vanilla's own block outline, repeated: the same shape, the same
 * colour, the same lines, borrowed rather than reproduced. There is no new look
 * to learn and nothing that can fall out of step with a resource pack or a
 * shader &mdash; there are simply several outlines where there was one.
 *
 * <p>Nothing is remembered between frames. The area is worked out afresh from
 * whatever the player is looking at right now, so it follows the crosshair
 * exactly, turns with them, flips as they move from a wall to the floor, and is
 * gone the instant the mode is switched off or the tool leaves their hand.
 * There is no state to clear, because none was kept.
 *
 * <p>The square and the rule for which blocks count both come from
 * {@link MiningArea}, the same code the server breaks by, so the outline cannot
 * promise a block that will survive or hide one that will not.
 */
public final class AreaOutline {

	/** Vanilla's own outline colour: black, mostly transparent. */
	private static final float ALPHA = 0.4F;

	private AreaOutline() {
	}

	/** Registers the render listener. Call once from client init. */
	public static void init() {
		WorldRenderEvents.BLOCK_OUTLINE.register(AreaOutline::render);
	}

	/**
	 * Draws the rest of the area around the block vanilla is already outlining.
	 *
	 * @return always {@code true}; the block actually aimed at keeps its own
	 *         outline, drawn by vanilla exactly as it always was
	 */
	private static boolean render(WorldRenderContext context, WorldRenderContext.BlockOutlineContext outline) {
		MatrixStack matrices = context.matrixStack();
		VertexConsumerProvider consumers = context.consumers();
		if (matrices == null || consumers == null) {
			return true;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if (player == null || !ToggleState.isEnabled(ExcavatorEnchantment.ID)) {
			return true;
		}

		// Read straight off the stack rather than through the registry: this runs
		// once a frame, and the answer travels with the item anyway.
		ItemStack tool = player.getMainHandStack();
		int level = EnchantmentLevels.level(tool, ModEnchantments.EXCAVATOR);
		if (level <= 0 || !(client.crosshairTarget instanceof BlockHitResult hit)) {
			return true;
		}

		BlockView world = context.world();
		VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
		// Held to the level in hand exactly as the server holds it, so a shape
		// chosen with a better tool never outlines a reach this one lacks.
		MiningShape shape = MiningShape.clamp(
				MiningShape.byName(ToggleState.mode(ExcavatorEnchantment.ID)), level);

		for (BlockPos pos : shape.around(outline.blockPos(), hit.getSide())) {
			if (!MiningArea.isBreakable(world, pos)) {
				continue;
			}

			BlockState state = world.getBlockState(pos);
			WorldRendererAccessor.invokeDrawCuboidShapeOutline(matrices, lines,
					state.getOutlineShape(world, pos, ShapeContext.of(outline.entity())),
					pos.getX() - outline.cameraX(),
					pos.getY() - outline.cameraY(),
					pos.getZ() - outline.cameraZ(),
					0.0F, 0.0F, 0.0F, ALPHA);
		}
		return true;
	}
}
