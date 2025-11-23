package com.lennyscustomclues;

import net.runelite.api.AnimationID;

public class AnimationTriggers
{
	public static boolean isTriggerAnimation(int animationId)
	{
		// Emote animation IDs and other trigger animations
		switch (animationId)
		{
			// Basic emotes (confirmed)
			case 855: // Yes
			case 856: // No
			case 857: // Thinking
			case 858: // Bow
			case 859: // Angry
			case 860: // Cry
			case 861: // Laugh
			case 862: // Cheer
			case 863: // Wave
			case 864: // Beckon
			case 865: // Clap
			case 866: // Dance
			case 1128: // Joy (Jump for Joy)
			case 1129: // Yawn
			case 1130: // Spin
			case 1131: // Shrug
			case 2105: // Salute
			case 2127: // Goblin bow
			case 2128: // Goblin salute
			case 2108: // Glass box
			case 2109: // Climb rope
			case 2110: // Lean
			case 2111: // Glass wall
			case 1374: // Blow Kiss
			case 3544: // Zombie Walk
			case 6111: // Rabbit Hop
			
			// Additional emotes (educated guesses - may need testing)
			case 1132: // Jig (estimated)
			case 2104: // Headbang (estimated)
			case 2112: // Panic (estimated)
			case 2113: // Raspberry (estimated)
			case 3920: // Premier Shield (estimated)
			case 1200: // Sit down (estimated)
			case 1133: // Flex (estimated)
			case 3545: // Zombie Dance (estimated)
			case 1201: // Sit up (estimated)
			case 1202: // Push up (estimated)
			case 1203: // Star jump (estimated)
			case 1204: // Jog (estimated)
			case 7535: // Air Guitar (estimated)
			case 8118: // Uri transform (estimated)
			case 8524: // Explore (estimated)
			case 9990: // Fortis Salute (estimated - newest)
			case 4275: // Idea (estimated)
			case 4276: // Stamp (estimated)
			case 4277: // Flap (estimated)
			case 4278: // Slap Head (estimated)
			case 3867: // Scared (estimated)
			case 3546: // Zombie Hand (estimated)
			case 7929: // Hypermobile Drinker (estimated)
			case 7930: // Smooth dance (estimated)
			case 7931: // Crazy dance (estimated)
			case 7932: // Party (estimated)
			case 7933: // Trick (estimated)
			
			// Tool actions
			case AnimationID.DIG: // Digging with spade (830)
				return true;
			default:
				return false;
		}
	}
}