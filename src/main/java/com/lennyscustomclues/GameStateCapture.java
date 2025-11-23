package com.lennyscustomclues;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class GameStateCapture
{
	@Inject
	private Client client;

	public List<ApiClient.ItemData> getInventoryData()
	{
		List<ApiClient.ItemData> inventoryItems = new ArrayList<>();
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

		if (inventory == null)
		{
			return inventoryItems;
		}

		Item[] items = inventory.getItems();
		for (int i = 0; i < items.length; i++)
		{
			Item item = items[i];
			if (item != null && item.getId() != -1)
			{
				inventoryItems.add(new ApiClient.ItemData(i, item.getId(), item.getQuantity()));
			}
		}

		return inventoryItems;
	}

	public List<ApiClient.ItemData> getWornItemsData()
	{
		List<ApiClient.ItemData> wornItems = new ArrayList<>();
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

		if (equipment == null)
		{
			return wornItems;
		}

		Item[] items = equipment.getItems();
		for (int i = 0; i < items.length; i++)
		{
			Item item = items[i];
			if (item != null && item.getId() != -1)
			{
				wornItems.add(new ApiClient.ItemData(i, item.getId(), item.getQuantity()));
			}
		}

		return wornItems;
	}

	public ApiClient.LocationData getLocationData(WorldPoint worldLocation, LocalPoint localLocation)
	{
		ApiClient.WorldCoords worldCoords = new ApiClient.WorldCoords(
			worldLocation.getX(), worldLocation.getY(), worldLocation.getPlane());
		
		ApiClient.LocalCoords localCoords = new ApiClient.LocalCoords(
			localLocation.getSceneX(), localLocation.getSceneY());
		
		return new ApiClient.LocationData(worldCoords, localCoords);
	}

	public ApiClient.GameStateSubmission createGameStateSubmission(ApiClient.LocationData locationData, 
		List<ApiClient.ItemData> inventoryData, List<ApiClient.ItemData> wornItemsData, 
		Integer emoteId, Integer npcId, String interactionType, String eventKey)
	{
		// Get player's RSN (RuneScape Name)
		Player localPlayer = client.getLocalPlayer();
		String rsn = (localPlayer != null) ? localPlayer.getName() : null;
		
		return new ApiClient.GameStateSubmission(locationData, inventoryData, wornItemsData, 
			emoteId, npcId, interactionType, eventKey, rsn);
	}
}