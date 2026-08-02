package pt.joao.enchantmentsplus.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.shape.VoxelShape;

/**
 * Opens up the method vanilla draws a block outline with.
 *
 * <p>The mod needs to draw more of exactly the same thing, and "exactly the
 * same" is the requirement rather than a convenience: an outline the mod drew
 * itself would be a second implementation of a look that has to match the first
 * one pixel for pixel, forever, through every change to how lines are rendered.
 * Vanilla already has that code and it is only private, so borrowing it is
 * strictly less to go wrong than copying it.
 *
 * <p>An accessor and nothing else &mdash; it changes no behaviour and injects
 * nowhere, so there is no vanilla decision here that the mod could get in the
 * way of.
 */
@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {

	@Invoker("drawCuboidShapeOutline")
	static void invokeDrawCuboidShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer,
			VoxelShape shape, double offsetX, double offsetY, double offsetZ,
			float red, float green, float blue, float alpha) {
		throw new AssertionError("Replaced by the mixin processor");
	}
}
