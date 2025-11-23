package com.lennyscustomclues;

import com.lennyscustomclues.constraints.*;
import com.lennyscustomclues.ApiClient.GameStateSubmission;
import java.util.ArrayList;
import java.util.List;

public class AnswerBuilder
{
	private String rewardText;
	private List<Constraint> constraints;

	public AnswerBuilder()
	{
		this.constraints = new ArrayList<>();
		this.rewardText = "";
	}

	public void addConstraint(Constraint constraint)
	{
		constraints.add(constraint);
	}

	public void removeConstraint(int index)
	{
		if (index >= 0 && index < constraints.size())
		{
			constraints.remove(index);
		}
	}

	public String toJson()
	{
		// TODO: Convert answer structure to JSON for export/API submission
		return "{}";
	}

	public void clear()
	{
		constraints.clear();
		rewardText = "";
	}

	// Getters and setters
	public String getRewardText() { return rewardText; }
	public void setRewardText(String rewardText) { this.rewardText = rewardText; }
	public List<Constraint> getConstraints() { return constraints; }
	public void setConstraints(List<Constraint> constraints) { this.constraints = constraints; }

	public int getConstraintCount()
	{
		return constraints.size();
	}

	public Constraint getConstraint(int index)
	{
		if (index >= 0 && index < constraints.size())
		{
			return constraints.get(index);
		}
		return null;
	}
}