package com.lennyscustomclues;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LennysCustomCluesPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LennysCustomCluesPlugin.class);
		RuneLite.main(args);
	}
}