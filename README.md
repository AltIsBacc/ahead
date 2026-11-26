# Ahead

i am speed

Ahead lets you change how far Minecraft renders ahead, on both the Vulkan and OpenGL backends.

- **Vulkan:** changes the number of images requested for the swapchain
- **OpenGL:** changes how many frames the CPU can get ahead of the GPU

Lower values can reduce input latency. Higher values can smooth out frame pacing, at the cost of latency. Results depend on your hardware and driver.

## Requirements

- Minecraft 26.3
- Fabric Loader 0.20.1 or newer
- Java 25

## Configuration

The setting is called **Swapchain Image Count**. It defaults to 3 and applies without restarting the game. The minimum is 1; the maximum depends on your setup:

- **Vulkan:** one less than the most swapchain images your device supports (10 if it reports no limit)
- **OpenGL:** 10. Anything above 3 goes past the game's own limit, which is experimental and adds input lag. Drivers without persistent buffer mapping stay at 3

You can change it:

- in Sodium's video settings, if [Sodium](https://modrinth.com/mod/sodium) is installed
- through [Mod Menu](https://modrinth.com/mod/modmenu), if [YetAnotherConfigLib](https://modrinth.com/mod/yacl) is also installed
- by editing `config/ahead.json` while the game is closed

## License

All rights reserved. See [LICENSE](LICENSE).
