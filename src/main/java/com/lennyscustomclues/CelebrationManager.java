package com.lennyscustomclues;

import net.runelite.api.Client;
import net.runelite.api.SoundEffectID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

	@Inject
	private ScheduledExecutorService scheduledExecutorService;

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
			// Play a celebratory sound - using the triumphant fanfare sound (ID 2930)
			client.playSoundEffect(2930);
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
				// Height set to 100 to display above the player's head
				client.getLocalPlayer().createSpotAnim(0, 199, 100, 0);

				// Also play the level-up sound effects for authenticity
				client.playSoundEffect(2396); // Level up sound 1

				// Schedule the second sound effect with a delay using ScheduledExecutorService
				// to avoid blocking the client thread
				scheduledExecutorService.schedule(() -> {
					clientThread.invokeLater(() -> {
						client.playSoundEffect(2384); // Level up sound 2
					});
				}, 583, TimeUnit.MILLISECONDS); // 35 game ticks delay (35 * 16.67ms = ~583ms)

				log.debug("Triggered vanilla fireworks animation (SpotAnim 199) on player");
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to trigger fireworks", e);
		}
	}
}