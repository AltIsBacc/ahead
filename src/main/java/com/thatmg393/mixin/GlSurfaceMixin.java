package com.thatmg393.mixin;

import com.mojang.renderpearl.api.device.GpuSurface;
import com.thatmg393.Ahead;
import com.thatmg393.config.AheadConfig;
import org.lwjgl.opengl.GL32C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlSurface")
public class GlSurfaceMixin {
	@Unique
	private long[] ahead$fences = new long[0];
	@Unique
	private long ahead$frame;

	@Inject(method = "configure", at = @At("TAIL"))
	private void ahead$resetFences(GpuSurface.Configuration config, CallbackInfo ci) {
		for (long fence : this.ahead$fences) {
			if (fence != 0L) {
				GL32C.glDeleteSync(fence);
			}
		}

		int spoofed = Math.max(1, AheadConfig.get().swapchainImageCount - 1);
		Ahead.LOGGER.info("hey, ahead here. ur now rendering ahead by " + spoofed + " frame/s");
		this.ahead$fences = new long[spoofed];
		this.ahead$frame = 0L;
	}

	@Inject(method = "present", at = @At("TAIL"))
	private void ahead$limitRenderAhead(CallbackInfo ci) {
		if (this.ahead$fences.length == 0) {
			return;
		}
		int slot = (int) (this.ahead$frame++ % this.ahead$fences.length);
		long old = this.ahead$fences[slot];
		if (old != 0L) {
			GL32C.glClientWaitSync(old, GL32C.GL_SYNC_FLUSH_COMMANDS_BIT, 1_000_000_000L);
			GL32C.glDeleteSync(old);
		}
		this.ahead$fences[slot] = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
	}
}
