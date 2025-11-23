package com.lennyscustomclues;

import com.lennyscustomclues.constraints.*;
import com.lennyscustomclues.dialogs.LocationConstraintDialog;
import com.lennyscustomclues.dialogs.SubmitAnswerDialog;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

@Singleton
public class AnswerBuilderPanel extends PluginPanel
{
	@Inject
	private GameStateService gameStateService;

	@Inject
	private Client client;

	@Inject
	private ApiClient apiClient;

	private AnswerBuilder answerBuilder;
	private JLabel titleLabel;
	private JTextArea rewardTextArea;
	private JPanel constraintsPanel;
	private JPanel buttonPanel;
	private JLabel statusLabel;
	private JScrollPane constraintsScrollPane;

	// Buttons
	private JButton addLocationButton;
	private JButton addInventoryButton;
	private JButton addEquipmentButton;
	private JButton addActionButton;
	private JButton clearAnswerButton;
	private JButton submitAnswerButton;

	public AnswerBuilderPanel()
	{
		this.answerBuilder = new AnswerBuilder();
		
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Title
		titleLabel = new JLabel("Answer Builder");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(FontManager.getRunescapeFont());
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Main content panel - single column layout
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// 1. Reward text area
		JPanel rewardPanel = new JPanel(new BorderLayout());
		rewardPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		rewardPanel.setBorder(BorderFactory.createTitledBorder("Reward Text (Required)"));
		rewardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

		rewardTextArea = new JTextArea(3, 0);
		rewardTextArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		rewardTextArea.setForeground(Color.WHITE);
		rewardTextArea.setLineWrap(true);
		rewardTextArea.setWrapStyleWord(true);
		rewardTextArea.setToolTipText("This reward text will be shown to players that solve your puzzle!");
		rewardTextArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSubmitButtonState(); }
			public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSubmitButtonState(); }
			public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSubmitButtonState(); }
		});
		JScrollPane rewardScrollPane = new JScrollPane(rewardTextArea);
		rewardPanel.add(rewardScrollPane, BorderLayout.CENTER);

		// 2. Constraints panel
		constraintsPanel = new JPanel();
		constraintsPanel.setLayout(new BoxLayout(constraintsPanel, BoxLayout.Y_AXIS));
		constraintsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		constraintsScrollPane = new JScrollPane(constraintsPanel);
		constraintsScrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		constraintsScrollPane.setBorder(BorderFactory.createTitledBorder("Constraints"));
		constraintsScrollPane.setPreferredSize(new Dimension(0, 200));

		// 3. Add constraint buttons panel
		JPanel addButtonsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
		addButtonsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		addButtonsPanel.setBorder(BorderFactory.createTitledBorder("Add Constraints"));
		addButtonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

		// 4. Action buttons panel
		JPanel actionButtonsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		actionButtonsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		actionButtonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		// 5. Submit button panel
		JPanel submitPanel = new JPanel(new FlowLayout());
		submitPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		submitPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

		createButtons();
		
		// Add buttons to panels
		addButtonsPanel.add(addLocationButton);
		addButtonsPanel.add(addInventoryButton);
		addButtonsPanel.add(addEquipmentButton);
		addButtonsPanel.add(addActionButton);

		actionButtonsPanel.add(clearAnswerButton);
		actionButtonsPanel.add(new JLabel()); // Spacer

		submitPanel.add(submitAnswerButton);

		// Status label
		statusLabel = new JLabel("Ready to build answer");
		statusLabel.setForeground(Color.WHITE);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Add all sections to main panel
		mainPanel.add(rewardPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(constraintsScrollPane);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(addButtonsPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(actionButtonsPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(submitPanel);

		add(titleLabel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);

		updateConstraintsDisplay();
	}

	private void createButtons()
	{
		addLocationButton = new JButton("+ Location");
		addLocationButton.addActionListener(this::onAddLocationConstraint);

		addInventoryButton = new JButton("+ Inventory");
		addInventoryButton.addActionListener(this::onAddInventoryConstraint);

		addEquipmentButton = new JButton("+ Equipment");
		addEquipmentButton.addActionListener(this::onAddEquipmentConstraint);

		addActionButton = new JButton("+ Action");
		addActionButton.addActionListener(this::onAddActionConstraint);

		clearAnswerButton = new JButton("Clear All");
		clearAnswerButton.addActionListener(this::onClearAnswer);

		submitAnswerButton = new JButton("Submit Answer to Server");
		submitAnswerButton.addActionListener(this::onSubmitAnswer);
		submitAnswerButton.setEnabled(false); // Initially disabled
	}

	@Inject
	public void initialize()
	{
		updateConstraintsDisplay();
	}

	private void onAddLocationConstraint(ActionEvent e)
	{
		// Find the parent frame
		Window parentWindow = SwingUtilities.getWindowAncestor(this);
		JFrame parentFrame = (parentWindow instanceof JFrame) ? (JFrame) parentWindow : null;

		LocationConstraintDialog dialog = new LocationConstraintDialog(
			parentFrame, 
			null, 
			constraint -> {
				answerBuilder.addConstraint(constraint);
				updateConstraintsDisplay();
				statusLabel.setText("Added location constraint");
			},
			client
		);
		
		dialog.setVisible(true);
	}

	private void onAddInventoryConstraint(ActionEvent e)
	{
		InventoryConstraint constraint = new InventoryConstraint("contains");
		// TODO: Show dialog to configure constraint
		answerBuilder.addConstraint(constraint);
		updateConstraintsDisplay();
		statusLabel.setText("Added inventory constraint");
	}

	private void onAddEquipmentConstraint(ActionEvent e)
	{
		EquipmentConstraint constraint = new EquipmentConstraint("contains");
		// TODO: Show dialog to configure constraint
		answerBuilder.addConstraint(constraint);
		updateConstraintsDisplay();
		statusLabel.setText("Added equipment constraint");
	}

	private void onAddActionConstraint(ActionEvent e)
	{
		ActionConstraint constraint = new ActionConstraint("emote");
		// TODO: Show dialog to configure constraint
		answerBuilder.addConstraint(constraint);
		updateConstraintsDisplay();
		statusLabel.setText("Added action constraint");
	}

	private void onSubmitAnswer(ActionEvent e)
	{
		// Update the answer builder with current reward text
		answerBuilder.setRewardText(rewardTextArea.getText().trim());

		// Find the parent frame
		Window parentWindow = SwingUtilities.getWindowAncestor(this);
		JFrame parentFrame = (parentWindow instanceof JFrame) ? (JFrame) parentWindow : null;

		SubmitAnswerDialog dialog = new SubmitAnswerDialog(
			parentFrame,
			answerBuilder,
			apiClient,
			eventKey -> {
				// Handle the submission
				statusLabel.setText("Submitting answer to server...");
				// TODO: Actually submit the answer to the server
				// For now, just show success
				statusLabel.setText("Answer submitted successfully with key: " + eventKey);
			}
		);

		dialog.setVisible(true);
	}

	private void onClearAnswer(ActionEvent e)
	{
		answerBuilder.clear();
		rewardTextArea.setText("");
		updateConstraintsDisplay();
		updateSubmitButtonState();
		statusLabel.setText("Answer cleared");
	}

	private void updateConstraintsDisplay()
	{
		constraintsPanel.removeAll();
		
		if (answerBuilder.getConstraintCount() == 0)
		{
			JLabel emptyLabel = new JLabel("No constraints added");
			emptyLabel.setForeground(Color.GRAY);
			constraintsPanel.add(emptyLabel);
		}
		else
		{
			for (int i = 0; i < answerBuilder.getConstraintCount(); i++)
			{
				Constraint constraint = answerBuilder.getConstraint(i);
				JPanel constraintPanel = createConstraintPanel(constraint, i);
				constraintsPanel.add(constraintPanel);
				constraintsPanel.add(Box.createVerticalStrut(5));
			}
		}

		constraintsPanel.revalidate();
		constraintsPanel.repaint();
		
		// Update submit button state
		updateSubmitButtonState();
	}

	private void updateSubmitButtonState()
	{
		boolean hasConstraints = answerBuilder.getConstraintCount() > 0;
		boolean hasRewardText = rewardTextArea != null && !rewardTextArea.getText().trim().isEmpty();
		boolean canSubmit = hasConstraints && hasRewardText;
		
		if (submitAnswerButton != null)
		{
			submitAnswerButton.setEnabled(canSubmit);
		}
	}

	private JPanel createConstraintPanel(Constraint constraint, int index)
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel typeLabel = new JLabel(constraint.getConstraintType().toUpperCase());
		typeLabel.setForeground(Color.CYAN);
		typeLabel.setFont(typeLabel.getFont().deriveFont(Font.BOLD));

		JLabel descLabel = new JLabel(constraint.description());
		descLabel.setForeground(Color.WHITE);

		JButton removeButton = new JButton("X");
		removeButton.setPreferredSize(new Dimension(25, 25));
		removeButton.addActionListener(e -> {
			answerBuilder.removeConstraint(index);
			updateConstraintsDisplay();
			statusLabel.setText("Constraint removed");
		});

		JPanel infoPanel = new JPanel(new BorderLayout());
		infoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		infoPanel.add(typeLabel, BorderLayout.NORTH);
		infoPanel.add(descLabel, BorderLayout.CENTER);

		panel.add(infoPanel, BorderLayout.CENTER);
		panel.add(removeButton, BorderLayout.EAST);

		return panel;
	}

	public AnswerBuilder getAnswerBuilder()
	{
		return answerBuilder;
	}

	public void updateStatusLabel(String text)
	{
		SwingUtilities.invokeLater(() -> {
			statusLabel.setText(text);
		});
	}
}