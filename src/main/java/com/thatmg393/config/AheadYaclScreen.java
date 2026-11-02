package com.thatmg393.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AheadYaclScreen {
	private AheadYaclScreen() {
	}

	public static Screen create(Screen parent) {
		AheadConfig config = AheadConfig.get();

		Option<Integer> imageCount = Option.<Integer>createBuilder()
			.name(Component.literal("Swapchain Image Count"))
			.description(OptionDescription.of(Component.literal(
				"How many frames the Vulkan swapchain can queue. Vanilla uses 3. Higher values can smooth frame pacing at the cost of input latency. Applies the next time the swapchain is recreated, such as when resizing the window or toggling VSync.")))
			.binding(AheadConfig.DEFAULT_IMAGE_COUNT, () -> config.swapchainImageCount, value -> config.swapchainImageCount = value)
			.controller(option -> IntegerSliderControllerBuilder.create(option)
				.range(AheadConfig.MIN_IMAGE_COUNT, AheadConfig.MAX_IMAGE_COUNT)
				.step(1))
			.build();

		return YetAnotherConfigLib.createBuilder()
			.title(Component.literal("Ahead"))
			.category(ConfigCategory.createBuilder()
				.name(Component.literal("Rendering"))
				.option(imageCount)
				.build())
			.save(AheadConfig::save)
			.build()
			.generateScreen(parent);
	}
}
