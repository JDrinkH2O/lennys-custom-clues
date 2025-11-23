package com.lennyscustomclues.constraints;

import java.util.List;

public class ActionConstraint implements Constraint
{
	private String type; // "emote", "npc_interaction", "any_emote", "any_npc"
	private Integer emoteId;
	private List<Integer> emoteIds;
	private Integer npcId;
	private List<Integer> npcIds;
	private String interactionType;
	private List<String> interactionTypes;

	public ActionConstraint()
	{
		// Default constructor for JSON deserialization
	}

	public ActionConstraint(String type)
	{
		this.type = type;
	}


	@Override
	public String description()
	{
		switch (type)
		{
			case "emote":
				return "Must perform specific emote";
			case "any_emote":
				return "Must perform one of specified emotes";
			case "npc_interaction":
				return "Must interact with specified NPC";
			case "any_npc":
				return "Must interact with any NPC";
			default:
				return "Action constraint";
		}
	}

	@Override
	public String getConstraintType()
	{
		return "action";
	}

	// Getters and setters
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	public Integer getEmoteId() { return emoteId; }
	public void setEmoteId(Integer emoteId) { this.emoteId = emoteId; }
	public List<Integer> getEmoteIds() { return emoteIds; }
	public void setEmoteIds(List<Integer> emoteIds) { this.emoteIds = emoteIds; }
	public Integer getNpcId() { return npcId; }
	public void setNpcId(Integer npcId) { this.npcId = npcId; }
	public List<Integer> getNpcIds() { return npcIds; }
	public void setNpcIds(List<Integer> npcIds) { this.npcIds = npcIds; }
	public String getInteractionType() { return interactionType; }
	public void setInteractionType(String interactionType) { this.interactionType = interactionType; }
	public List<String> getInteractionTypes() { return interactionTypes; }
	public void setInteractionTypes(List<String> interactionTypes) { this.interactionTypes = interactionTypes; }
}