package com.lennyscustomclues.constraints;

import java.util.List;

public class InventoryConstraint implements Constraint
{
	private String type; // "contains", "exact", "minimum_quantity", "any_of"
	private Integer itemId;
	private List<Integer> itemIds; // For "any_of" type
	private Integer minQuantity;
	private Integer exactSlot;
	private Integer requiredSlot;

	public InventoryConstraint()
	{
		// Default constructor for JSON deserialization
	}

	public InventoryConstraint(String type)
	{
		this.type = type;
	}


	@Override
	public String description()
	{
		switch (type)
		{
			case "contains":
				return "Inventory must contain specified item";
			case "any_of":
				return "Inventory must contain at least one of specified items";
			case "exact":
				return "Specific inventory slot must contain specified item";
			default:
				return "Inventory constraint";
		}
	}

	@Override
	public String getConstraintType()
	{
		return "inventory";
	}

	// Getters and setters
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	public Integer getItemId() { return itemId; }
	public void setItemId(Integer itemId) { this.itemId = itemId; }
	public List<Integer> getItemIds() { return itemIds; }
	public void setItemIds(List<Integer> itemIds) { this.itemIds = itemIds; }
	public Integer getMinQuantity() { return minQuantity; }
	public void setMinQuantity(Integer minQuantity) { this.minQuantity = minQuantity; }
	public Integer getExactSlot() { return exactSlot; }
	public void setExactSlot(Integer exactSlot) { this.exactSlot = exactSlot; }
	public Integer getRequiredSlot() { return requiredSlot; }
	public void setRequiredSlot(Integer requiredSlot) { this.requiredSlot = requiredSlot; }
}