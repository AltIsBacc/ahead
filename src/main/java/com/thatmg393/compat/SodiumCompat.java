package com.thatmg393.compat;

import com.thatmg393.Ahead;
import com.thatmg393.config.AheadConfig;
import com.thatmg393.config.RenderLimits;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.api.config.option.Range;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;

public class SodiumCompat implements ConfigEntryPoint {
	@Override
	public void registerConfigLate(ConfigBuilder builder) {
		AheadConfig.ensureLoaded();

		builder.registerOwnModOptions()
			.addPage(builder.createOptionPage()
				.setName(Component.literal("Rendering"))
				.addOptionGroup(builder.createOptionGroup()
					.addOption(builder.createIntegerOption(Ahead.id("swapchain_image_count"))
						.setName(Component.literal("Swapchain Image Count"))
						.setTooltip(Component.literal("Frames the swapchain can queue (Vulkan) or the render-ahead limit (OpenGL). Vanilla uses 3. Applies on the next frame."))
						.setRangeProvider(state -> new Range(AheadConfig.MIN_IMAGE_COUNT, RenderLimits.maxSetting(), 1), ConfigState.UPDATE_ON_REBUILD)
						.setDefaultProvider(state -> RenderLimits.clamp(AheadConfig.DEFAULT_IMAGE_COUNT), ConfigState.UPDATE_ON_REBUILD)
						.setValueFormatter(value -> Component.literal(String.valueOf(value))) 
						.setBinding(value -> AheadConfig.get().swapchainImageCount = value, () -> RenderLimits.clamp(AheadConfig.get().swapchainImageCount))
						.setStorageHandler(AheadConfig::save))));
	}
}
