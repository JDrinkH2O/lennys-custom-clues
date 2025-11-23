package com.lennyscustomclues.constraints;

public class LocationConstraint implements Constraint
{
	private String type; // "exact", "bounds", "tolerance"
	private Integer minX;
	private Integer maxX;
	private Integer minY;
	private Integer maxY;
	private Integer exactX;
	private Integer exactY;
	private Integer plane;
	private Integer tolerance; // For tolerance type

	public LocationConstraint()
	{
		// Default constructor for JSON deserialization
	}

	public LocationConstraint(String type)
	{
		this.type = type;
	}


	@Override
	public String description()
	{
		switch (type)
		{
			case "exact":
				return "Must be at exact coordinates";
			case "bounds":
				return "Must be within specified area bounds";
			case "tolerance":
				return "Must be within tolerance of target location";
			default:
				return "Location constraint";
		}
	}

	@Override
	public String getConstraintType()
	{
		return "location";
	}

	// Getters and setters
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	public Integer getMinX() { return minX; }
	public void setMinX(Integer minX) { this.minX = minX; }
	public Integer getMaxX() { return maxX; }
	public void setMaxX(Integer maxX) { this.maxX = maxX; }
	public Integer getMinY() { return minY; }
	public void setMinY(Integer minY) { this.minY = minY; }
	public Integer getMaxY() { return maxY; }
	public void setMaxY(Integer maxY) { this.maxY = maxY; }
	public Integer getExactX() { return exactX; }
	public void setExactX(Integer exactX) { this.exactX = exactX; }
	public Integer getExactY() { return exactY; }
	public void setExactY(Integer exactY) { this.exactY = exactY; }
	public Integer getPlane() { return plane; }
	public void setPlane(Integer plane) { this.plane = plane; }
	public Integer getTolerance() { return tolerance; }
	public void setTolerance(Integer tolerance) { this.tolerance = tolerance; }
}