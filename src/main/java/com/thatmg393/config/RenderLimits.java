package com.thatmg393.config;

/** What the active backend and device allow. Updated whenever the surface is configured. */
public final class RenderLimits {
	/** Used until a surface is configured, and when the device reports no maximum. */
	private static final int FALLBACK_MAX = 10;
	/** The engine's GlCommandEncoder.MAX_SUBMITS_IN_FLIGHT (2) + 1. That class is package-private. */
	private static final int OPENGL_MAX = 3;

	private enum Backend { UNKNOWN, VULKAN, OPENGL }

	private static Backend backend = Backend.UNKNOWN;
	private static int minImages;
	private static int maxImages; // 0 means no limit

	private RenderLimits() {
	}

	public static void useVulkan(int min, int max) {
		backend = Backend.VULKAN;
		minImages = min;
		maxImages = max;
	}

	public static void useOpenGl() {
		backend = Backend.OPENGL;
	}

	/** Highest setting that still has an effect. */
	public static int maxSetting() {
		return switch (backend) {
			case OPENGL -> OPENGL_MAX;
			// the setting is one less than the image count; keep at least 2 so sliders have a range
			case VULKAN -> maxImages > 0 ? Math.max(2, maxImages - 1) : FALLBACK_MAX;
			case UNKNOWN -> FALLBACK_MAX;
		};
	}

	public static int clamp(int setting) {
		return Math.max(AheadConfig.MIN_IMAGE_COUNT, Math.min(maxSetting(), setting));
	}

	/** Swapchain image count to request for a setting, within what the surface supports. */
	public static int vulkanImageCount(int setting) {
		int count = Math.max(clamp(setting) + 1, minImages);
		return maxImages > 0 ? Math.min(count, maxImages) : count;
	}

	/** Setting description for the config screens, specific to the active backend. */
	public static String tooltip(int setting) {
		String text = "How far Minecraft may render ahead. Lower values can reduce input lag, "
			+ "higher values can smooth out frame pacing. Applies without a restart.";
		return switch (backend) {
			case VULKAN -> text
				+ "\n\nVulkan: at " + setting + ", the game asks for " + vulkanImageCount(setting)
				+ " swapchain images. Vanilla asks for 3 (a value of 2)."
				+ "\n\nMaximum " + maxSetting() + (maxImages > 0
					? ": your device supports up to " + maxImages + " images."
					: ": your device sets no limit, so Ahead stops here.");
			case OPENGL -> text
				+ "\n\nOpenGL: at " + setting + ", the CPU can get up to " + Math.max(1, clamp(setting) - 1)
				+ " frame(s) ahead of the GPU. 1 and 2 behave the same."
				+ "\n\nMaximum " + OPENGL_MAX + ": the game itself never queues more.";
			case UNKNOWN -> text;
		};
	}
}
