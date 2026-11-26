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
			.description(value -> OptionDescription.of(Component.literal(RenderLimits.tooltip(value))))
			.binding(RenderLimits.clamp(AheadConfig.DEFAULT_IMAGE_COUNT), () -> RenderLimits.clamp(config.swapchainImageCount), value -> config.swapchainImageCount = value)
			.controller(option -> IntegerSliderControllerBuilder.create(option)
				.range(AheadConfig.MIN_IMAGE_COUNT, RenderLimits.maxSetting())
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
