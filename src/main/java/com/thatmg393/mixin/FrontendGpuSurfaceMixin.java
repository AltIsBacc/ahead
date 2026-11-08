package com.thatmg393.mixin;

import com.mojang.renderpearl.api.device.GpuSurface;
import com.mojang.renderpearl.api.device.SurfaceException;
import com.mojang.renderpearl.frontend.FrontendGpuSurface;
import com.thatmg393.Ahead;
import com.thatmg393.config.AheadConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(FrontendGpuSurface.class)
public abstract class FrontendGpuSurfaceMixin {
	@Shadow
	private Optional<GpuSurface.Configuration> currentConfiguration;

	@Shadow
	private boolean hasImageAcquired;

	@Shadow
	public abstract void configure(GpuSurface.Configuration config) throws SurfaceException;

	@Unique
	private int ahead$appliedCount = -1;

	@Inject(method = "configure", at = @At("HEAD"))
	private void ahead$recordCount(GpuSurface.Configuration config, CallbackInfo ci) {
		this.ahead$appliedCount = AheadConfig.get().swapchainImageCount;
	}

	@Unique
	private boolean ahead$needsReload() {
		return this.ahead$appliedCount != -1
			&& this.ahead$appliedCount != AheadConfig.get().swapchainImageCount
			&& !this.hasImageAcquired
			&& this.currentConfiguration.isPresent();
	}

	@Unique
	private void ahead$reload() throws SurfaceException {
		Ahead.LOGGER.info("hey, ahead here. image count changed from {} to {}, reconfiguring surface",
			this.ahead$appliedCount, AheadConfig.get().swapchainImageCount);
		this.configure(this.currentConfiguration.get());
	}

	@Inject(method = "acquireNextTexture", at = @At("HEAD"))
	private void ahead$reloadBeforeAcquire(CallbackInfo ci) throws SurfaceException {
		if (this.ahead$needsReload()) {
			this.ahead$reload();
		}
	}

	@Inject(method = "present", at = @At("TAIL"))
	private void ahead$reloadAfterPresent(CallbackInfo ci) {
		if (!this.ahead$needsReload()) {
			return;
		}
		try {
			this.ahead$reload();
		} catch (SurfaceException e) {
			Ahead.LOGGER.warn("hey, ahead here. surface reload failed, leaving it to the game to recover", e);
		}
	}
}
