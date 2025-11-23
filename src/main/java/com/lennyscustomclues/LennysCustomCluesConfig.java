package com.lennyscustomclues;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("lennyscustomclues")
public interface LennysCustomCluesConfig extends Config
{
	@ConfigItem(
		keyName = "debug",
		name = "Debug Mode",
		description = "Show all animation IDs in chat when player performs animations"
	)
	default boolean debug()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showCelebrationSound",
		name = "Victory sound effects",
		description = "Play a celebratory sound when you solve a puzzle correctly"
	)
	default boolean showCelebrationSound()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showCelebrationFireworks",
		name = "Victory fireworks",
		description = "Show fireworks when you solve a puzzle correctly"
	)
	default boolean showCelebrationFireworks()
	{
		return true;
	}

}