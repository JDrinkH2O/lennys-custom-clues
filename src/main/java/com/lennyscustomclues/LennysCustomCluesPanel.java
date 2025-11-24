package com.lennyscustomclues;

import com.lennyscustomclues.dialogs.AnswerBuilderDialog;
import com.lennyscustomclues.dialogs.ManageEventDialog;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

@Singleton
public class LennysCustomCluesPanel extends PluginPanel
{
	@Inject
	private GameStateService gameStateService;

	@Inject
	private ApiClient apiClient;

	@Inject
	private Client client;

	private JLabel titleLabel;
	private JPanel buttonPanel;
	private JLabel statusLabel;
	private JLabel instructionalLabel;
	private JLabel resultLabel;

	// Buttons for different states
	private JButton setEventKeyButton;
	private JButton unsetEventKeyButton;
	private JButton changeEventKeyButton;
	private JButton createAnswerButton;
	private JButton manageEventButton;

	public LennysCustomCluesPanel()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Title label
		titleLabel = new JLabel("Lenny's Custom Clues");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(FontManager.getRunescapeFont());
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Button panel - will be dynamically populated
		buttonPanel = new JPanel();
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Status label
		statusLabel = new JLabel();
		statusLabel.setForeground(Color.WHITE);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Create all buttons
		createButtons();

		// Layout
		add(titleLabel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);

		// UI state will be initialized in initialize() after injection
	}

	private void createButtons()
	{
		setEventKeyButton = new JButton("Set Event Key");
		setEventKeyButton.addActionListener(this::onSetEventKeyClick);
		setEventKeyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		setEventKeyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, setEventKeyButton.getPreferredSize().height));

		unsetEventKeyButton = new JButton("Unset Event Key");
		unsetEventKeyButton.addActionListener(this::onUnsetEventKeyClick);
		unsetEventKeyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		unsetEventKeyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, unsetEventKeyButton.getPreferredSize().height));

		changeEventKeyButton = new JButton("Change Event Key");
		changeEventKeyButton.addActionListener(this::onChangeEventKeyClick);
		changeEventKeyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		changeEventKeyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, changeEventKeyButton.getPreferredSize().height));

		createAnswerButton = new JButton("Create a new event");
		createAnswerButton.addActionListener(this::onCreateAnswerClick);
		createAnswerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		createAnswerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, createAnswerButton.getPreferredSize().height));

		manageEventButton = new JButton("Manage existing event");
		manageEventButton.addActionListener(this::onManageEventClick);
		manageEventButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		manageEventButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, manageEventButton.getPreferredSize().height));
	}

	@Inject
	public void initialize()
	{
		// Set up the bidirectional reference with the service
		gameStateService.setPanel(this);
		
		// Initialize UI state now that injection is complete
		updatePanelForEventKeyState();
	}

	public void onEventKeyChanged()
	{
		SwingUtilities.invokeLater(this::updatePanelForEventKeyState);
	}

	private void updatePanelForEventKeyState()
	{
		buttonPanel.removeAll();

		if (gameStateService.hasEventKey())
		{
			showEventKeySetState();
		}
		else
		{
			showNoEventKeyState();
		}

		buttonPanel.revalidate();
		buttonPanel.repaint();
	}

	private void showNoEventKeyState()
	{
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Play in an event section
		buttonPanel.add(createSectionLabel("Play in an event"));
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		buttonPanel.add(setEventKeyButton);

		// Spacing between sections
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Event management section
		buttonPanel.add(createSectionLabel("Event management"));
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		buttonPanel.add(createAnswerButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		buttonPanel.add(manageEventButton);

		statusLabel.setText("");
	}

	private void showEventKeySetState()
	{
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		String eventKey = gameStateService.getEventKey();

		// Playing in event section
		buttonPanel.add(createSectionLabel("Playing in: " + eventKey));
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		// Instructional text
		instructionalLabel = new JLabel("<html>Try and solve the clue!<br/>Good luck!</html>");
		instructionalLabel.setForeground(Color.WHITE);
		instructionalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		buttonPanel.add(instructionalLabel);
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		// Result text area
		resultLabel = new JLabel("");
		resultLabel.setForeground(Color.WHITE);
		resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		buttonPanel.add(resultLabel);

		// Push buttons to bottom
		buttonPanel.add(Box.createVerticalGlue());

		// Event key buttons
		buttonPanel.add(changeEventKeyButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		buttonPanel.add(unsetEventKeyButton);

		statusLabel.setText("");
	}

	private JLabel createSectionLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(ColorScheme.BRAND_ORANGE);
		label.setFont(FontManager.getRunescapeBoldFont());
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private void onSetEventKeyClick(ActionEvent e)
	{
		showEventKeyDialog(null, false);
	}

	private void onChangeEventKeyClick(ActionEvent e)
	{
		showEventKeyDialog(gameStateService.getEventKey(), true);
	}

	private void onUnsetEventKeyClick(ActionEvent e)
	{
		gameStateService.clearEventKey();
	}

	private void showEventKeyDialog(String currentEventKey, boolean isChanging)
	{
		// Find the parent frame
		Window parentWindow = SwingUtilities.getWindowAncestor(this);
		JFrame parentFrame = (parentWindow instanceof JFrame) ? (JFrame) parentWindow : null;

		EventKeyDialog dialog = new EventKeyDialog(
			parentFrame,
			currentEventKey,
			isChanging,
			eventKey -> gameStateService.setEventKey(eventKey),
			apiClient
		);

		dialog.setVisible(true);
	}

	private void onCreateAnswerClick(ActionEvent e)
	{
		// Find the parent frame
		Window parentWindow = SwingUtilities.getWindowAncestor(this);
		JFrame parentFrame = (parentWindow instanceof JFrame) ? (JFrame) parentWindow : null;

		AnswerBuilderDialog dialog = new AnswerBuilderDialog(
			parentFrame,
			client,
			apiClient
		);

		dialog.setVisible(true);
	}

	private void onManageEventClick(ActionEvent e)
	{
		// Find the parent frame
		Window parentWindow = SwingUtilities.getWindowAncestor(this);
		JFrame parentFrame = (parentWindow instanceof JFrame) ? (JFrame) parentWindow : null;

		ManageEventDialog dialog = new ManageEventDialog(
			parentFrame,
			client,
			apiClient
		);

		dialog.setVisible(true);
	}

	public void updateStatusLabel(String text)
	{
		SwingUtilities.invokeLater(() -> {
			if (resultLabel != null)
			{
				resultLabel.setText(text);
				buttonPanel.revalidate();
				buttonPanel.repaint();
			}
		});
	}
}