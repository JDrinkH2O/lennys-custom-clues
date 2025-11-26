package com.lennyscustomclues;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

/**
 * Utility class for getting player location with boat/sailing support.
 * When the player is on a boat, this returns the boat's world coordinates
 * instead of the player's local position within the boat.
 */
public class LocationUtil
{
    /**
     * Gets the player's world location, accounting for boats/sailing.
     *
     * @param client The RuneLite client
     * @return The player's WorldPoint (or boat's WorldPoint if on a boat), or null if player not found
     */
    public static WorldPoint getPlayerWorldLocation(Client client)
    {
        if (client == null)
        {
            return null;
        }

        Player player = client.getLocalPlayer();
        if (player == null)
        {
            return null;
        }

        var worldView = player.getWorldView();
        var location = player.getWorldLocation();

        if (worldView == null || location == null)
        {
            return null;
        }

        var worldViewId = worldView.getId();
        var isOnBoat = worldViewId != -1;

        if (isOnBoat)
        {
            // Player is on a boat - get the boat's world position
            var worldEntity = client.getTopLevelWorldView().worldEntities().byIndex(worldViewId);
            if (worldEntity != null)
            {
                var worldPoint = WorldPoint.fromLocalInstance(client, worldEntity.getLocalLocation());
                // Preserve the plane from the player's location
                return new WorldPoint(worldPoint.getX(), worldPoint.getY(), location.getPlane());
            }
        }

        // Player is on land - return normal location
        return location;
    }
}
