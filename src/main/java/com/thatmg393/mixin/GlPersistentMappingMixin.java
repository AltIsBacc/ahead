package com.thatmg393.mixin;

import com.thatmg393.config.RenderLimits;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Makes room for one block rotation per submit in flight. */
@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlTransientMemory$PersistentMapping")
public class GlPersistentMappingMixin {
	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 2))
	private int ahead$rotationSlots(int vanilla) {
		// only this memory path waits for the GPU before reusing blocks, so only it allows a deeper queue
		RenderLimits.allowDeepOpenGlQueue();
		return RenderLimits.OPENGL_RING_SIZE;
	}
}
