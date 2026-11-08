package com.thatmg393.mixin;

import com.thatmg393.Ahead;
import com.thatmg393.config.AheadConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(targets = "com.mojang.renderpearl.backend.vulkan.VulkanGpuSurface")
public class VulkanGpuSurfaceMixin {
	@ModifyConstant(
		method = "configure",
		constant = @Constant(intValue = 3),
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkSwapchainCreateInfoKHR;surface(J)Lorg/lwjgl/vulkan/VkSwapchainCreateInfoKHR;"),
			to = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkSwapchainCreateInfoKHR;minImageCount(I)Lorg/lwjgl/vulkan/VkSwapchainCreateInfoKHR;")
		)
	)
	private int ahead$imageCount(int original) {
		int spoofed = AheadConfig.get().swapchainImageCount + 1; // apparently 0 is the main image so we +1 (to larp is to not larp)
		Ahead.LOGGER.info("hey, ahead here. ur now rendering ahead by " + spoofed + " frame/s");
		return spoofed;
	}
}
