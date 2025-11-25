package com.lennyscustomclues.dialogs;

import com.lennyscustomclues.constraints.*;
import com.lennyscustomclues.AnswerBuilder;
import com.lennyscustomclues.ApiClient;
import com.lennyscustomclues.EmoteData;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AnswerBuilderDialog extends JDialog
{
	private AnswerBuilder answerBuilder;
	private Client client;
	private ApiClient apiClient;

	// Fields for update mode
	private boolean isUpdateMode;
	private String eventKey;
	private String secretKey;

	private JTextArea rewardTextArea;
	private JLabel statusLabel;

	// Required Action section
	private JComboBox<String> requiredActionCombo;

	// Location Constraint section
	private JComboBox<String> locationTypeCombo;
	private JPanel locationFieldsPanel;
	private Map<String, JTextField> locationFields;
	private Map<String, JLabel> locationLabels;
	private JButton useCurrentLocationButton;
	private JLabel coordinatesLabel;
	private Timer coordinateUpdateTimer;

	// Emote Constraint section
	private JPanel emoteSelectionPanel;
	private JLabel emoteDisplayLabel;
	private JButton selectEmoteButton;
	private ActionConstraint emoteConstraint;

	// Buttons
	private JButton clearAnswerButton;
	private JButton submitAnswerButton;
	private JButton cancelButton;

	// Constructor for creating a new answer
	public AnswerBuilderDialog(JFrame parent, Client client, ApiClient apiClient)
	{
		this(parent, client, apiClient, null, null, null, null);
	}

	// Constructor for updating an existing answer
	public AnswerBuilderDialog(JFrame parent, Client client, ApiClient apiClient,
		String eventKey, String secretKey, String rewardText, java.util.List<java.util.Map<String, Object>> constraints)
	{
		super(parent, eventKey != null ? "Update Answer" : "Create Answer", false); // false = non-modal
		this.client = client;
		this.apiClient = apiClient;
		this.answerBuilder = new AnswerBuilder();
		this.locationFields = new HashMap<>();
		this.locationLabels = new HashMap<>();
		this.isUpdateMode = eventKey != null;
		this.eventKey = eventKey;
		this.secretKey = secretKey;

		initializeUI();

		// Pre-populate fields if in update mode
		if (isUpdateMode && rewardText != null)
		{
			prepopulateFields(rewardText, constraints);
		}

		startCoordinateUpdates();
		pack();
		setMinimumSize(new Dimension(500, 600));
		setLocationRelativeTo(parent);
		setAlwaysOnTop(true); // Keep dialog visible above game client
	}

	private void initializeUI()
	{
		setLayout(new BorderLayout());
		getContentPane().setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Title
		JLabel titleLabel = new JLabel("Answer Builder");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(FontManager.getRunescapeFont());
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

		// 2. Required Action section
		JPanel requiredActionPanel = new JPanel();
		requiredActionPanel.setLayout(new BoxLayout(requiredActionPanel, BoxLayout.Y_AXIS));
		requiredActionPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		requiredActionPanel.setBorder(BorderFactory.createTitledBorder("Required Action"));

		JPanel actionTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		actionTypePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel actionLabel = new JLabel("Action Type:");
		actionLabel.setForeground(Color.WHITE);
		actionTypePanel.add(actionLabel);

		requiredActionCombo = new JComboBox<>(new String[]{"Dig with a spade", "Perform an Emote"});
		requiredActionCombo.addActionListener(this::onActionTypeChanged);
		actionTypePanel.add(requiredActionCombo);

		requiredActionPanel.add(actionTypePanel);

		// Emote selection panel (initially hidden)
		emoteSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		emoteSelectionPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		emoteSelectionPanel.setVisible(false);

		emoteDisplayLabel = new JLabel("No emote selected");
		emoteDisplayLabel.setForeground(Color.LIGHT_GRAY);
		emoteSelectionPanel.add(emoteDisplayLabel);

		selectEmoteButton = new JButton("Select Emote");
		selectEmoteButton.addActionListener(this::onSelectEmote);
		emoteSelectionPanel.add(selectEmoteButton);

		JButton clearEmoteButton = new JButton("Clear");
		clearEmoteButton.addActionListener(this::onClearEmote);
		emoteSelectionPanel.add(clearEmoteButton);

		requiredActionPanel.add(emoteSelectionPanel);

		// 3. Location Constraint section
		JPanel locationConstraintPanel = new JPanel();
		locationConstraintPanel.setLayout(new BoxLayout(locationConstraintPanel, BoxLayout.Y_AXIS));
		locationConstraintPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		locationConstraintPanel.setBorder(BorderFactory.createTitledBorder("Location Constraint"));

		// Location type dropdown and coordinates display
		JPanel locationTopPanel = new JPanel(new BorderLayout());
		locationTopPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel locationTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		locationTypePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JLabel locationTypeLabel = new JLabel("Type:");
		locationTypeLabel.setForeground(Color.WHITE);
		locationTypePanel.add(locationTypeLabel);

		locationTypeCombo = new JComboBox<>(new String[]{"Exact Tile", "Near a Tile", "Within a Rectangle", "None"});
		locationTypeCombo.addActionListener(this::onLocationTypeChanged);
		locationTypePanel.add(locationTypeCombo);

		// Coordinates display
		JPanel coordinatesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		coordinatesPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		coordinatesLabel = new JLabel("Current Location: Loading...");
		coordinatesLabel.setForeground(Color.CYAN);
		coordinatesLabel.setFont(coordinatesLabel.getFont().deriveFont(Font.BOLD));
		coordinatesPanel.add(coordinatesLabel);

		locationTopPanel.add(locationTypePanel, BorderLayout.NORTH);
		locationTopPanel.add(coordinatesPanel, BorderLayout.SOUTH);

		// Dynamic location fields panel
		locationFieldsPanel = new JPanel(new GridBagLayout());
		locationFieldsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		createLocationFields();

		locationConstraintPanel.add(locationTopPanel);
		locationConstraintPanel.add(locationFieldsPanel);

		// 4. Required Worn Items section
		JPanel wornItemsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		wornItemsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wornItemsPanel.setBorder(BorderFactory.createTitledBorder("Required Worn Items"));
		wornItemsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

		JLabel wornItemsLabel = new JLabel("Will be implemented soon!");
		wornItemsLabel.setForeground(Color.LIGHT_GRAY);
		wornItemsPanel.add(wornItemsLabel);

		// 5. Clear button panel (centered)
		JPanel clearButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		clearButtonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		clearButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		// 6. Submit button panel
		JPanel submitPanel = new JPanel(new FlowLayout());
		submitPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		submitPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

		createButtons();

		clearButtonPanel.add(clearAnswerButton);

		submitPanel.add(submitAnswerButton);
		submitPanel.add(cancelButton);

		// Status label
		statusLabel = new JLabel("Ready to build answer");
		statusLabel.setForeground(Color.WHITE);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Add all sections to main panel
		mainPanel.add(rewardPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(requiredActionPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(locationConstraintPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(wornItemsPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(clearButtonPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(submitPanel);

		add(titleLabel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);

		updateLocationFieldsVisibility();
	}

	private void createButtons()
	{
		clearAnswerButton = new JButton("Clear All");
		clearAnswerButton.addActionListener(this::onClearAnswer);

		submitAnswerButton = new JButton(isUpdateMode ? "Update on Server" : "Submit to Server");
		submitAnswerButton.addActionListener(this::onSubmitAnswer);
		submitAnswerButton.setEnabled(false); // Initially disabled

		cancelButton = new JButton("Cancel and discard changes");
		cancelButton.addActionListener(this::onCancel);
	}

	private void onCancel(ActionEvent e)
	{
		int result = JOptionPane.showConfirmDialog(
			this,
			"Are you sure you want to cancel and discard all changes?",
			"Confirm Cancel",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE
		);

		if (result == JOptionPane.YES_OPTION)
		{
			dispose();
		}
	}

	private void createLocationFields()
	{
		// Create all possible fields
		locationFields.put("exactX", createNumberField());
		locationFields.put("exactY", createNumberField());
		locationFields.put("minX", createNumberField());
		locationFields.put("maxX", createNumberField());
		locationFields.put("minY", createNumberField());
		locationFields.put("maxY", createNumberField());
		locationFields.put("plane", createNumberField());
		locationFields.put("tolerance", createNumberField());

		// Create corresponding labels
		locationLabels.put("exactX", createLabel("Exact X:"));
		locationLabels.put("exactY", createLabel("Exact Y:"));
		locationLabels.put("minX", createLabel("Min X:"));
		locationLabels.put("maxX", createLabel("Max X:"));
		locationLabels.put("minY", createLabel("Min Y:"));
		locationLabels.put("maxY", createLabel("Max Y:"));
		locationLabels.put("plane", createLabel("Plane (optional):"));
		locationLabels.put("tolerance", createLabel("Tolerance:"));
	}

	private JTextField createNumberField()
	{
		JTextField field = new JTextField(20);
		field.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		field.setForeground(Color.WHITE);
		return field;
	}

	private JLabel createLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(Color.WHITE);
		return label;
	}

	private void onActionTypeChanged(ActionEvent e)
	{
		String selectedAction = (String) requiredActionCombo.getSelectedItem();
		boolean isEmoteAction = "Perform an Emote".equals(selectedAction);

		emoteSelectionPanel.setVisible(isEmoteAction);
		pack(); // Resize dialog to fit new content
	}

	private void onLocationTypeChanged(ActionEvent e)
	{
		updateLocationFieldsVisibility();
		pack(); // Resize dialog to fit new content
	}

	private void updateLocationFieldsVisibility()
	{
		String selectedType = (String) locationTypeCombo.getSelectedItem();

		// Clear the panel
		locationFieldsPanel.removeAll();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;

		int row = 0;

		// Map UI labels to internal constraint types
		String constraintType;
		switch (selectedType)
		{
			case "Exact Tile":
				constraintType = "exact";
				addFieldToPanel(gbc, row++, "exactX");
				addFieldToPanel(gbc, row++, "exactY");
				break;

			case "Within a Rectangle":
				constraintType = "bounds";
				addFieldToPanel(gbc, row++, "minX");
				addFieldToPanel(gbc, row++, "maxX");
				addFieldToPanel(gbc, row++, "minY");
				addFieldToPanel(gbc, row++, "maxY");
				break;

			case "None":
				// No location constraint fields needed
				constraintType = null;
				break;

			case "Near a Tile":
			default:
				constraintType = "tolerance";
				addFieldToPanel(gbc, row++, "exactX");
				addFieldToPanel(gbc, row++, "exactY");
				addFieldToPanel(gbc, row++, "tolerance");
				break;
		}

		// Always add plane field (optional for all types except None)
		if (!"None".equals(selectedType))
		{
			addFieldToPanel(gbc, row++, "plane");
		}

		// Add "Use Current Location" button for exact and tolerance types
		if ("Exact Tile".equals(selectedType) || "Near a Tile".equals(selectedType))
		{
			useCurrentLocationButton = new JButton("Use Current Location");
			useCurrentLocationButton.addActionListener(this::onUseCurrentLocation);

			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.gridwidth = 2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(10, 5, 5, 5);
			locationFieldsPanel.add(useCurrentLocationButton, gbc);
			gbc.gridwidth = 1;
			gbc.insets = new Insets(5, 5, 5, 5);
		}

		// Refresh the panel
		locationFieldsPanel.revalidate();
		locationFieldsPanel.repaint();
	}

	private void addFieldToPanel(GridBagConstraints gbc, int row, String fieldName)
	{
		JLabel label = locationLabels.get(fieldName);
		JTextField field = locationFields.get(fieldName);

		if (label != null && field != null)
		{
			// Add label
			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.fill = GridBagConstraints.NONE;
			locationFieldsPanel.add(label, gbc);

			// Add field
			gbc.gridx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			locationFieldsPanel.add(field, gbc);
			gbc.weightx = 0.0;
		}
	}

	private void onUseCurrentLocation(ActionEvent e)
	{
		WorldPoint currentLocation = getCurrentPlayerLocation();
		if (currentLocation != null)
		{
			JTextField exactXField = locationFields.get("exactX");
			JTextField exactYField = locationFields.get("exactY");
			JTextField planeField = locationFields.get("plane");

			if (exactXField != null) exactXField.setText(String.valueOf(currentLocation.getX()));
			if (exactYField != null) exactYField.setText(String.valueOf(currentLocation.getY()));
			if (planeField != null) planeField.setText(String.valueOf(currentLocation.getPlane()));
		}
	}

	private void onSelectEmote(ActionEvent e)
	{
		Window parentWindow = SwingUtilities.getWindowAncestor(this);
		JFrame parentFrame = (parentWindow instanceof JFrame) ? (JFrame) parentWindow : null;

		EmoteConstraintDialog dialog = new EmoteConstraintDialog(
			parentFrame,
			emoteConstraint,
			constraint -> {
				emoteConstraint = constraint;
				updateEmoteDisplay();
			}
		);

		dialog.setVisible(true);
	}

	private void onClearEmote(ActionEvent e)
	{
		emoteConstraint = null;
		updateEmoteDisplay();
	}

	private void updateEmoteDisplay()
	{
		if (emoteConstraint != null && emoteConstraint.getEmoteId() != null)
		{
			String emoteName = EmoteData.getEmoteName(emoteConstraint.getEmoteId());
			emoteDisplayLabel.setText("Selected: " + emoteName);
			emoteDisplayLabel.setForeground(Color.WHITE);
		}
		else
		{
			emoteDisplayLabel.setText("No emote selected");
			emoteDisplayLabel.setForeground(Color.LIGHT_GRAY);
		}
	}

	private void onSubmitAnswer(ActionEvent e)
	{
		try
		{
			// Clear any existing data first
			answerBuilder.clear();

			// Update the answer builder with current reward text
			answerBuilder.setRewardText(rewardTextArea.getText().trim());

			// Build and add the location constraint (if not "None")
			LocationConstraint locationConstraint = buildLocationConstraint();
			if (locationConstraint != null)
			{
				answerBuilder.addConstraint(locationConstraint);
			}

			// Add the emote constraint if "Perform an Emote" is selected
			String selectedAction = (String) requiredActionCombo.getSelectedItem();
			if ("Perform an Emote".equals(selectedAction))
			{
				if (emoteConstraint == null || emoteConstraint.getEmoteId() == null)
				{
					JOptionPane.showMessageDialog(this, "Please select an emote", "Emote Required", JOptionPane.WARNING_MESSAGE);
					return;
				}
				answerBuilder.addConstraint(emoteConstraint);
			}

			if (isUpdateMode)
			{
				// Update mode - directly submit the update
				statusLabel.setText("Updating answer on server...");
				statusLabel.setForeground(java.awt.Color.YELLOW);

				apiClient.updateAnswer(eventKey, secretKey, answerBuilder).thenAccept(response -> {
					javax.swing.SwingUtilities.invokeLater(() -> {
						if (response.success)
						{
							// Show success popup
							JOptionPane.showMessageDialog(
								AnswerBuilderDialog.this,
								"Answer updated successfully",
								"Success",
								JOptionPane.INFORMATION_MESSAGE
							);

							// Close the dialog
							AnswerBuilderDialog.this.dispose();
						}
						else
						{
							// Handle different error types with appropriate messages
							String errorMessage = "✗ Failed to update answer: " + response.message;
							if ("NOT_FOUND".equals(response.errorType))
							{
								errorMessage = "✗ Event key not found";
							}
							else if ("UNAUTHORIZED".equals(response.errorType))
							{
								errorMessage = "✗ Invalid secret key";
							}
							else if ("NETWORK_ERROR".equals(response.errorType))
							{
								errorMessage = "✗ Network error: " + response.message;
							}
							else if ("VALIDATION_ERROR".equals(response.errorType))
							{
								errorMessage = "✗ Validation error: " + response.message;
							}
							else if ("SERVER_ERROR".equals(response.errorType))
							{
								errorMessage = "✗ Server error: " + response.message;
							}

							statusLabel.setText(errorMessage);
							statusLabel.setForeground(java.awt.Color.RED);
						}
					});
				});
			}
			else
			{
				// Create mode - show dialog to get event key and secret key
				// Find the parent frame
				Window parentWindow = SwingUtilities.getWindowAncestor(this);
				JFrame parentFrame = (parentWindow instanceof JFrame) ? (JFrame) parentWindow : null;

				SubmitAnswerDialog dialog = new SubmitAnswerDialog(
					parentFrame,
					answerBuilder,
					apiClient,
					(eventKey, secretKey) -> {
						// Handle the submission
						statusLabel.setText("Submitting answer to server...");
						statusLabel.setForeground(java.awt.Color.YELLOW);

						// Actually submit the answer to the server
						apiClient.createAnswer(eventKey, secretKey, answerBuilder).thenAccept(response -> {
							javax.swing.SwingUtilities.invokeLater(() -> {
								if (response.success)
								{
									// Show success popup
									JOptionPane.showMessageDialog(
										AnswerBuilderDialog.this,
										"Answer submitted successfully",
										"Success",
										JOptionPane.INFORMATION_MESSAGE
									);

									// Close both dialogs
									AnswerBuilderDialog.this.dispose();
								}
								else
								{
									// Handle different error types with appropriate messages
									String errorMessage = "✗ Failed to submit answer: " + response.message;
									if ("KEY_ALREADY_EXISTS".equals(response.errorType))
									{
										errorMessage = "✗ Event key '" + eventKey + "' already exists";
									}
									else if ("NETWORK_ERROR".equals(response.errorType))
									{
										errorMessage = "✗ Network error: " + response.message;
									}
									else if ("VALIDATION_ERROR".equals(response.errorType))
									{
										errorMessage = "✗ Validation error: " + response.message;
									}
									else if ("SERVER_ERROR".equals(response.errorType))
									{
										errorMessage = "✗ Server error: " + response.message;
									}

									statusLabel.setText(errorMessage);
									statusLabel.setForeground(java.awt.Color.RED);
								}
							});
						});
					}
				);

				dialog.setVisible(true);
			}
		}
		catch (NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(this, "Please enter valid numbers for location fields", "Input Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (IllegalArgumentException ex)
		{
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private LocationConstraint buildLocationConstraint()
	{
		String selectedType = (String) locationTypeCombo.getSelectedItem();
		String constraintType;

		switch (selectedType)
		{
			case "Exact Tile":
				constraintType = "exact";
				break;
			case "Within a Rectangle":
				constraintType = "bounds";
				break;
			case "None":
				return null; // No location constraint
			case "Near a Tile":
			default:
				constraintType = "tolerance";
				break;
		}

		LocationConstraint constraint = new LocationConstraint(constraintType);
		constraint.setExactX(parseInteger("exactX"));
		constraint.setExactY(parseInteger("exactY"));
		constraint.setMinX(parseInteger("minX"));
		constraint.setMaxX(parseInteger("maxX"));
		constraint.setMinY(parseInteger("minY"));
		constraint.setMaxY(parseInteger("maxY"));
		constraint.setPlane(parseInteger("plane"));
		constraint.setTolerance(parseInteger("tolerance"));

		// Validate bounds constraints
		if ("bounds".equals(constraintType))
		{
			Integer minX = constraint.getMinX();
			Integer maxX = constraint.getMaxX();
			Integer minY = constraint.getMinY();
			Integer maxY = constraint.getMaxY();

			if (minX != null && maxX != null && maxX < minX)
			{
				throw new IllegalArgumentException("Max X must be greater than or equal to Min X");
			}

			if (minY != null && maxY != null && maxY < minY)
			{
				throw new IllegalArgumentException("Max Y must be greater than or equal to Min Y");
			}
		}

		return constraint;
	}

	private Integer parseInteger(String fieldName)
	{
		JTextField field = locationFields.get(fieldName);
		if (field == null)
		{
			return null;
		}

		String text = field.getText();
		if (text == null || text.trim().isEmpty())
		{
			return null;
		}
		return Integer.parseInt(text.trim());
	}

	private void onClearAnswer(ActionEvent e)
	{
		int result = JOptionPane.showConfirmDialog(
			this,
			"Are you sure you want to clear all drafted answer information?",
			"Confirm Clear",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE
		);

		if (result == JOptionPane.YES_OPTION)
		{
			answerBuilder.clear();
			rewardTextArea.setText("");
			requiredActionCombo.setSelectedItem("Dig with a spade");
			clearLocationFields();
			emoteConstraint = null;
			updateEmoteDisplay();
			updateSubmitButtonState();
			statusLabel.setText("Answer cleared");
		}
	}

	private void clearLocationFields()
	{
		for (JTextField field : locationFields.values())
		{
			field.setText("");
		}
	}

	private void updateSubmitButtonState()
	{
		boolean hasRewardText = rewardTextArea != null && !rewardTextArea.getText().trim().isEmpty();

		if (submitAnswerButton != null)
		{
			submitAnswerButton.setEnabled(hasRewardText);
		}
	}

	private void startCoordinateUpdates()
	{
		// Update coordinates every 500ms
		coordinateUpdateTimer = new Timer(500, e -> updateCoordinatesDisplay());
		coordinateUpdateTimer.start();
		updateCoordinatesDisplay(); // Initial update
	}

	private void stopCoordinateUpdates()
	{
		if (coordinateUpdateTimer != null)
		{
			coordinateUpdateTimer.stop();
		}
	}

	private void updateCoordinatesDisplay()
	{
		SwingUtilities.invokeLater(() -> {
			WorldPoint location = getCurrentPlayerLocation();
			if (location != null)
			{
				coordinatesLabel.setText(String.format("Current Location: X:%d Y:%d Plane:%d",
					location.getX(), location.getY(), location.getPlane()));
			}
			else
			{
				coordinatesLabel.setText("Current Location: Not available");
			}
		});
	}

	private WorldPoint getCurrentPlayerLocation()
	{
		if (client == null) return null;

		Player player = client.getLocalPlayer();
		if (player == null) return null;

		return player.getWorldLocation();
	}

	private void prepopulateFields(String rewardText, java.util.List<java.util.Map<String, Object>> constraints)
	{
		// Set reward text
		rewardTextArea.setText(rewardText);

		// Parse and set constraints if present
		if (constraints != null && !constraints.isEmpty())
		{
			for (java.util.Map<String, Object> constraintMap : constraints)
			{
				String constraintType = (String) constraintMap.get("constraint_type");

				if ("location".equals(constraintType))
				{
					String type = (String) constraintMap.get("type");

					// Set the location type in the dropdown
					if ("exact".equals(type))
					{
						locationTypeCombo.setSelectedItem("Exact Tile");
						setIntegerField("exactX", constraintMap.get("exact_x"));
						setIntegerField("exactY", constraintMap.get("exact_y"));
					}
					else if ("tolerance".equals(type))
					{
						locationTypeCombo.setSelectedItem("Near a Tile");
						setIntegerField("exactX", constraintMap.get("exact_x"));
						setIntegerField("exactY", constraintMap.get("exact_y"));
						setIntegerField("tolerance", constraintMap.get("tolerance"));
					}
					else if ("bounds".equals(type))
					{
						locationTypeCombo.setSelectedItem("Within a Rectangle");
						setIntegerField("minX", constraintMap.get("min_x"));
						setIntegerField("maxX", constraintMap.get("max_x"));
						setIntegerField("minY", constraintMap.get("min_y"));
						setIntegerField("maxY", constraintMap.get("max_y"));
					}

					// Set plane if present
					setIntegerField("plane", constraintMap.get("plane"));
				}
				else if ("action".equals(constraintType))
				{
					String type = (String) constraintMap.get("type");

					if ("emote".equals(type))
					{
						Object emoteIdObj = constraintMap.get("emote_id");
						if (emoteIdObj != null)
						{
							Integer emoteId = null;
							if (emoteIdObj instanceof Number)
							{
								emoteId = ((Number) emoteIdObj).intValue();
							}

							if (emoteId != null)
							{
								// Set the action type to "Perform an Emote"
								requiredActionCombo.setSelectedItem("Perform an Emote");

								emoteConstraint = new ActionConstraint("emote");
								emoteConstraint.setEmoteId(emoteId);
								updateEmoteDisplay();
							}
						}
					}
				}
			}
		}

		updateSubmitButtonState();
	}

	private void setIntegerField(String fieldName, Object value)
	{
		JTextField field = locationFields.get(fieldName);
		if (field != null && value != null)
		{
			// Handle both Integer and Double from JSON parsing
			if (value instanceof Number)
			{
				field.setText(String.valueOf(((Number) value).intValue()));
			}
			else if (value instanceof String)
			{
				field.setText((String) value);
			}
		}
	}

	@Override
	public void dispose()
	{
		stopCoordinateUpdates();
		super.dispose();
	}

	public AnswerBuilder getAnswerBuilder()
	{
		return answerBuilder;
	}
}
