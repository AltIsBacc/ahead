package com.thatmg393.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.thatmg393.Ahead;
import com.thatmg393.config.AheadConfig;
import com.thatmg393.config.RenderLimits;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "com.mojang.renderpearl.backend.vulkan.VulkanGpuSurface")
public class VulkanGpuSurfaceMixin {
	@ModifyArg(
		method = "configure",
		at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkSwapchainCreateInfoKHR;minImageCount(I)Lorg/lwjgl/vulkan/VkSwapchainCreateInfoKHR;")
	)
	private int ahead$imageCount(int vanilla, @Local VkSurfaceCapabilitiesKHR capabilities) {
		RenderLimits.useVulkan(capabilities.minImageCount(), capabilities.maxImageCount());
		int count = RenderLimits.vulkanImageCount(AheadConfig.get().swapchainImageCount);
		Ahead.LOGGER.info("hey, ahead here. requesting {} swapchain images (device min {}, max {})",
			count, capabilities.minImageCount(), capabilities.maxImageCount());
		return count;
	}
}
