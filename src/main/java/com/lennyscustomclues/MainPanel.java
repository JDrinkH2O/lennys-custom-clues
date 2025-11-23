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
	private LennysCustomCluesPanel normalPanel;

	public MainPanel()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
	}

	@Inject
	public void initialize()
	{
		// Initialize child panel
		normalPanel.initialize();

		// Add the normal panel
		add(normalPanel, BorderLayout.CENTER);
	}
}