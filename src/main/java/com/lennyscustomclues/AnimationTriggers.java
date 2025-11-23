package com.lennyscustomclues;

import net.runelite.api.AnimationID;

public class AnimationTriggers
{
	public static boolean isTriggerAnimation(int animationId)
	{
		// Only digging with spade is supported
		return animationId == AnimationID.DIG; // 830
	}
}