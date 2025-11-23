package com.lennyscustomclues.constraints;

import java.util.List;

public class EquipmentConstraint implements Constraint
{
	private String type; // "contains", "exact_slot", "any_of"
	private Integer itemId;
	private List<Integer> itemIds; // For "any_of" type
	private Integer slot;
	private Integer minQuantity;

	public EquipmentConstraint()
	{
		// Default constructor for JSON deserialization
	}

	public EquipmentConstraint(String type)
	{
		this.type = type;
	}


	@Override
	public String description()
	{
		switch (type)
		{
			case "contains":
				return "Must have specified item equipped";
			case "exact_slot":
				return "Specific equipment slot must contain specified item";
			case "any_of":
				return "Must have at least one of specified items equipped";
			default:
				return "Equipment constraint";
		}
	}

	@Override
	public String getConstraintType()
	{
		return "equipment";
	}

	// Getters and setters
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	public Integer getItemId() { return itemId; }
	public void setItemId(Integer itemId) { this.itemId = itemId; }
	public List<Integer> getItemIds() { return itemIds; }
	public void setItemIds(List<Integer> itemIds) { this.itemIds = itemIds; }
	public Integer getSlot() { return slot; }
	public void setSlot(Integer slot) { this.slot = slot; }
	public Integer getMinQuantity() { return minQuantity; }
	public void setMinQuantity(Integer minQuantity) { this.minQuantity = minQuantity; }
}