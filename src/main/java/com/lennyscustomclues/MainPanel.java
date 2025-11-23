package com.lennyscustomclues;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class MainPanel extends PluginPanel
{
	@Inject
	private LennysCustomCluesConfig config;

	@Inject
	private LennysCustomCluesPanel normalPanel;

	@Inject
	private AnswerBuilderPanel answerBuilderPanel;

	private boolean isBuilderMode = false;

	public MainPanel()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
	}

	@Inject
	public void initialize()
	{
		// Initialize child panels
		normalPanel.initialize();
		answerBuilderPanel.initialize();
		
		// Set initial mode
		updateMode();
	}

	public void updateMode()
	{
		boolean shouldBeBuilderMode = config.answerBuilderMode();
		
		if (shouldBeBuilderMode != isBuilderMode || getComponentCount() == 0)
		{
			isBuilderMode = shouldBeBuilderMode;
			
			// Clear current panel
			removeAll();
			
			// Add appropriate panel
			if (isBuilderMode)
			{
				add(answerBuilderPanel, BorderLayout.CENTER);
			}
			else
			{
				add(normalPanel, BorderLayout.CENTER);
			}
			
			// Refresh display
			revalidate();
			repaint();
		}
	}
}