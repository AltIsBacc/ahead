package com.thatmg393.mixin;

import com.thatmg393.Ahead;
import com.thatmg393.config.AheadConfig;
import com.thatmg393.config.RenderLimits;
import org.lwjgl.opengl.GL32C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Replaces the engine's fixed limit of 2 submits in flight with a configurable one. */
@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder")
public class GlCommandEncoderMixin {
	@Shadow
	@Final
	@Mutable
	private long[] fences;

	@Unique
	private int ahead$submitDepth = RenderLimits.OPENGL_VANILLA_SUBMITS;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void ahead$growFences(CallbackInfo ci) {
		this.fences = new long[RenderLimits.OPENGL_RING_SIZE];
	}

	@Inject(method = "submit", at = @At("HEAD"))
	private void ahead$applyDepth(CallbackInfo ci) {
		int wanted = RenderLimits.openGlSubmitDepth(AheadConfig.get().swapchainImageCount);
		if (wanted == this.ahead$submitDepth) {
			return;
		}

		// slots are index % depth, so let the GPU go idle before remapping them
		GL32C.glFinish();
		for (int i = 0; i < this.fences.length; i++) {
			if (this.fences[i] != 0L) {
				GL32C.glDeleteSync(this.fences[i]);
				this.fences[i] = 0L;
			}
		}
		Ahead.LOGGER.info("hey, ahead here. opengl submits in flight: {} -> {}", this.ahead$submitDepth, wanted);
		this.ahead$submitDepth = wanted;
	}

	// every 2L in these methods is MAX_SUBMITS_IN_FLIGHT; all four must be replaced together
	@ModifyConstant(method = {"currentSubmitSlot", "submit", "awaitSubmit"}, constant = @Constant(longValue = 2L), require = 4)
	private long ahead$submitsInFlight(long vanilla) {
		return this.ahead$submitDepth;
	}

	@Inject(method = "awaitSubmit", at = @At("HEAD"), cancellable = true)
	private void ahead$skipBeforeFirstSubmit(long index, long timeoutNS, CallbackInfoReturnable<Boolean> cir) {
		// the engine starts counting at 2; a deeper queue would otherwise look up negative slots
		if (index < RenderLimits.OPENGL_FIRST_SUBMIT) {
			cir.setReturnValue(true);
		}
	}
}
