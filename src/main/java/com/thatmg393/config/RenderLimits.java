package com.thatmg393.config;

/** What the active backend and device allow. Updated whenever the surface is configured. */
public final class RenderLimits {
	/** Used until a surface is configured, and when the device reports no maximum. */
	private static final int FALLBACK_MAX = 10;
	/** The engine's GlCommandEncoder.MAX_SUBMITS_IN_FLIGHT. That class is package-private. */
	public static final int OPENGL_VANILLA_SUBMITS = 2;
	/** The engine's initial GlCommandEncoder.currentSubmitIndex. */
	public static final long OPENGL_FIRST_SUBMIT = 2L;
	private static final int OPENGL_MAX = 10;
	/** Slots needed for the deepest queue. The setting is one more than the submits in flight. */
	public static final int OPENGL_RING_SIZE = OPENGL_MAX - 1;

	private enum Backend { UNKNOWN, VULKAN, OPENGL }

	private static Backend backend = Backend.UNKNOWN;
	private static int minImages;
	private static int maxImages; // 0 means no limit
	private static boolean deepOpenGlQueue;

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

	public static void allowDeepOpenGlQueue() {
		deepOpenGlQueue = true;
	}

	private static int openGlMax() {
		return deepOpenGlQueue ? OPENGL_MAX : OPENGL_VANILLA_SUBMITS + 1;
	}

	/** Submits the engine may have in flight for a setting. Never fewer than vanilla. */
	public static int openGlSubmitDepth(int setting) {
		return Math.max(OPENGL_VANILLA_SUBMITS, Math.min(openGlMax(), setting) - 1);
	}

	/** Highest setting that still has an effect. */
	public static int maxSetting() {
		return switch (backend) {
			case OPENGL -> openGlMax();
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
				+ (deepOpenGlQueue
					? "\n\nAbove 3 goes past the game's own limit. This is experimental and adds input lag."
					: "\n\nMaximum 3: your driver lacks persistent buffer mapping, so queuing more is not safe.");
			case UNKNOWN -> text;
		};
	}
}
