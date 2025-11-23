package com.lennyscustomclues;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.AnimationID;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.events.ConfigChanged;

import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
	name = "Lenny's Custom Clues"
)
public class LennysCustomCluesPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private LennysCustomCluesConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private MainPanel mainPanel;

	@Inject
	private GameStateService gameStateService;

	@Inject
	private CelebrationManager celebrationManager;

	private NavigationButton navButton;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Lenny's Custom Clues started!");

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/util/clue_arrow.png");

		navButton = NavigationButton.builder()
			.tooltip("Lenny's Custom Clues")
			.icon(icon)
			.priority(5)
			.panel(mainPanel)
			.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Lenny's Custom Clues stopped!");
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		// No chat message on login
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		// Only process player animations
		if (!(event.getActor() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getActor();
		
		// Only track local player animations
		if (player != client.getLocalPlayer())
		{
			return;
		}

		int animationId = player.getAnimation();
		
		// Debug mode: show all animation IDs in chat
		if (config.debug())
		{
			client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				"[LL-debug] Animation ID: " + animationId,
				null
			);
		}
		
		// Check for trigger animations
		if (AnimationTriggers.isTriggerAnimation(animationId))
		{
			String triggerType = (animationId == AnimationID.DIG) ? "dig" : "emote";
			log.info("{} detected with animation ID: {}", triggerType, animationId);
			gameStateService.captureFromAnimation(animationId);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		MenuAction action = event.getMenuAction();
		
		// Only capture NPC interactions
		switch (action)
		{
			case NPC_FIRST_OPTION:
			case NPC_SECOND_OPTION:
			case NPC_THIRD_OPTION:
			case NPC_FOURTH_OPTION:
			case NPC_FIFTH_OPTION:
				int npcId = event.getId();
				String menuOption = event.getMenuOption();
				
				log.info("NPC interaction detected: {} (ID: {}) - {}", event.getMenuTarget(), npcId, menuOption);
				gameStateService.captureFromNpcInteraction(npcId, menuOption);
				break;
			default:
				// Ignore all other menu actions (walk, objects, widgets, etc.)
				break;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getGroup().equals("lennyscustomclues"))
		{
			mainPanel.updateMode();
		}
	}

	@Provides
	LennysCustomCluesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LennysCustomCluesConfig.class);
	}
}