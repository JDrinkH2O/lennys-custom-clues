package com.lennyscustomclues.constraints;

import java.util.List;

public class EventKeyConstraint implements Constraint
{
	private String type; // "exact", "prefix", "suffix", "any_of"
	private String exactKey;
	private String prefix;
	private String suffix;
	private List<String> allowedKeys;

	public EventKeyConstraint()
	{
		// Default constructor for JSON deserialization
	}

	public EventKeyConstraint(String type)
	{
		this.type = type;
	}


	@Override
	public String description()
	{
		switch (type)
		{
			case "exact":
				return "Must have exact event key";
			case "any_of":
				return "Must have one of allowed event keys";
			default:
				return "Event key constraint";
		}
	}

	@Override
	public String getConstraintType()
	{
		return "event_key";
	}

	// Getters and setters
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	public String getExactKey() { return exactKey; }
	public void setExactKey(String exactKey) { this.exactKey = exactKey; }
	public String getPrefix() { return prefix; }
	public void setPrefix(String prefix) { this.prefix = prefix; }
	public String getSuffix() { return suffix; }
	public void setSuffix(String suffix) { this.suffix = suffix; }
	public List<String> getAllowedKeys() { return allowedKeys; }
	public void setAllowedKeys(List<String> allowedKeys) { this.allowedKeys = allowedKeys; }
}