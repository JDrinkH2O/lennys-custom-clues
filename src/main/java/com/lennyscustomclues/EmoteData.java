package com.lennyscustomclues;

import java.util.LinkedHashMap;
import java.util.Map;

public class EmoteData
{
	private static final Map<String, Integer> EMOTES = new LinkedHashMap<>();

	static
	{
		EMOTES.put("Yes", 855);
		EMOTES.put("No", 856);
		EMOTES.put("Bow", 858);
		EMOTES.put("Angry", 859);
		EMOTES.put("Think", 857);
		EMOTES.put("Wave", 863);
		EMOTES.put("Shrug", 2113);
		EMOTES.put("Cheer", 862);
		EMOTES.put("Beckon", 864);
		EMOTES.put("Laugh", 861);
		EMOTES.put("Jump for Joy", 2109);
		EMOTES.put("Yawn", 2111);
		EMOTES.put("Dance", 866);
		EMOTES.put("Jig", 2106);
		EMOTES.put("Spin", 2107);
		EMOTES.put("Headbang", 2108);
		EMOTES.put("Cry", 860);
		EMOTES.put("Blow Kiss", 1374);
		EMOTES.put("Panic", 2105);
		EMOTES.put("Raspberry", 2110);
		EMOTES.put("Clap", 865);
		EMOTES.put("Salute", 2112);
		EMOTES.put("Goblin Bow", 2127);
		EMOTES.put("Goblin Salute", 2128);
		EMOTES.put("Glass Box", 1131);
		EMOTES.put("Climb Rope", 1130);
		EMOTES.put("Lean", 1129);
		EMOTES.put("Glass Wall", 1128);
		EMOTES.put("Idea", 4276);
		EMOTES.put("Stamp", 4278);
		EMOTES.put("Flap", 4280);
		EMOTES.put("Slap Head", 4275);
		EMOTES.put("Zombie Walk", 3544);
		EMOTES.put("Zombie Dance", 3543);
		EMOTES.put("Scared", 2836);
		EMOTES.put("Rabbit Hop", 6111);
		EMOTES.put("Sit up", 874);
		EMOTES.put("Push up", 872);
		EMOTES.put("Star jump", 870);
		EMOTES.put("Jog", 868);
		EMOTES.put("Flex", 8917);
		EMOTES.put("Zombie Hand", 1708);
		EMOTES.put("Hypermobile Drinker", 7131);
		EMOTES.put("Smooth dance", 7533);
		EMOTES.put("Crazy dance", 7537);
		EMOTES.put("Premier Shield", 7751);
		EMOTES.put("Party", 10031);
		EMOTES.put("Trick", 10503);
		EMOTES.put("Fortis Salute", 10796);
		EMOTES.put("Sit down", 10061);
	}

	public static Map<String, Integer> getEmotes()
	{
		return EMOTES;
	}

	public static String getEmoteName(int emoteId)
	{
		for (Map.Entry<String, Integer> entry : EMOTES.entrySet())
		{
			if (entry.getValue() == emoteId)
			{
				return entry.getKey();
			}
		}
		return "Unknown Emote";
	}

	public static Integer getEmoteId(String emoteName)
	{
		return EMOTES.get(emoteName);
	}
}
