package com.lennyscustomclues;

import net.runelite.api.AnimationID;

public class AnimationTriggers
{
	public static boolean isTriggerAnimation(int animationId)
	{
		// Check for digging animation
		if (animationId == AnimationID.DIG) // 830
		{
			return true;
		}

		// Check for supported emote animations
		return EmoteData.getEmoteName(animationId) != null && !EmoteData.getEmoteName(animationId).equals("Unknown Emote");
	}

	public static boolean isDigAnimation(int animationId)
	{
		return animationId == AnimationID.DIG;
	}

	public static boolean isEmoteAnimation(int animationId)
	{
		String emoteName = EmoteData.getEmoteName(animationId);
		return emoteName != null && !emoteName.equals("Unknown Emote");
	}
}