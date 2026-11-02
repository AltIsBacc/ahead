package com.thatmg393.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thatmg393.Ahead;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AheadConfig {
	public static final int MIN_IMAGE_COUNT = 2;
	public static final int MAX_IMAGE_COUNT = 8;
	public static final int DEFAULT_IMAGE_COUNT = 3;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("ahead.json");

	private static AheadConfig instance = new AheadConfig();

	public int swapchainImageCount = DEFAULT_IMAGE_COUNT;

	public static AheadConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.exists(PATH)) {
			try {
				AheadConfig loaded = GSON.fromJson(Files.readString(PATH), AheadConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (Exception e) {
				Ahead.LOGGER.error("Failed to read {}, using defaults", PATH, e);
			}
		}
		instance.swapchainImageCount = clamp(instance.swapchainImageCount);
		save();
	}

	public static void save() {
		instance.swapchainImageCount = clamp(instance.swapchainImageCount);
		try {
			Files.writeString(PATH, GSON.toJson(instance));
		} catch (IOException e) {
			Ahead.LOGGER.error("Failed to write {}", PATH, e);
		}
	}

	private static int clamp(int value) {
		return Math.max(MIN_IMAGE_COUNT, Math.min(MAX_IMAGE_COUNT, value));
	}
}
