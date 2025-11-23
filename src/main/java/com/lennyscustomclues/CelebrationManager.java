package com.lennyscustomclues;

import net.runelite.api.Client;
import net.runelite.api.SoundEffectID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class CelebrationManager
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private LennysCustomCluesConfig config;

	public void triggerVictoryCelebration(String puzzleName)
	{
		clientThread.invokeLater(() -> {
			log.info("Triggering victory celebration for puzzle: {}", puzzleName);

			if (config.showCelebrationSound())
			{
				playVictorySound();
			}

			if (config.showCelebrationFireworks())
			{
				triggerFireworks();
			}
		});
	}

	private void playVictorySound()
	{
		try
		{
			// Play a celebratory sound - using NPC teleport woosh which has a magical/triumphant feel
			client.playSoundEffect(SoundEffectID.NPC_TELEPORT_WOOSH);
			log.debug("Played victory sound effect");
		}
		catch (Exception e)
		{
			log.warn("Failed to play victory sound", e);
		}
	}

	private void triggerFireworks()
	{
		try
		{
			if (client.getLocalPlayer() != null)
			{
				// Create the vanilla OSRS level-up fireworks animation on the player
				// SpotAnim ID 199 is the standard fireworks that appear when leveling up
				client.getLocalPlayer().createSpotAnim(0, 199, 0, 0);
				
				// Also play the level-up sound effects for authenticity
				client.playSoundEffect(2396); // Level up sound 1
				clientThread.invokeLater(() -> {
					try {
						Thread.sleep(583); // 35 game ticks delay (35 * 16.67ms = ~583ms)
						client.playSoundEffect(2384); // Level up sound 2
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				});
				
				log.debug("Triggered vanilla fireworks animation (SpotAnim 199) on player");
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to trigger fireworks", e);
		}
	}
}