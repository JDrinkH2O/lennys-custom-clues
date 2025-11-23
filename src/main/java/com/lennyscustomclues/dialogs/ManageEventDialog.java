package com.lennyscustomclues.dialogs;

import com.lennyscustomclues.ApiClient;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;

public class ManageEventDialog extends JDialog
{
	private ApiClient apiClient;
	private Client client;
	private JFrame parentFrame;

	private JTextField eventKeyField;
	private JTextField secretKeyField;
	private JButton submitButton;
	private JButton cancelButton;
	private JLabel statusLabel;

	public ManageEventDialog(JFrame parent, Client client, ApiClient apiClient)
	{
		super(parent, "Manage Existing Event", true);
		this.parentFrame = parent;
		this.client = client;
		this.apiClient = apiClient;

		initializeUI();
		pack();
		setMinimumSize(new Dimension(400, 200));
		setLocationRelativeTo(parent);
	}

	private void initializeUI()
	{
		setLayout(new BorderLayout());
		getContentPane().setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Title
		JLabel titleLabel = new JLabel("Enter Event Credentials");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(FontManager.getRunescapeFont());
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Main content panel
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;

		// Event Key label and field
		JLabel eventKeyLabel = new JLabel("Event Key:");
		eventKeyLabel.setForeground(Color.WHITE);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(eventKeyLabel, gbc);

		eventKeyField = new JTextField(20);
		eventKeyField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		eventKeyField.setForeground(Color.WHITE);
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		mainPanel.add(eventKeyField, gbc);

		// Secret Key label and field
		JLabel secretKeyLabel = new JLabel("Secret Key:");
		secretKeyLabel.setForeground(Color.WHITE);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		mainPanel.add(secretKeyLabel, gbc);

		secretKeyField = new JTextField(20);
		secretKeyField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		secretKeyField.setForeground(Color.WHITE);
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		mainPanel.add(secretKeyField, gbc);

		// Bottom panel containing buttons and status
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Buttons panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		submitButton = new JButton("Load Event");
		submitButton.addActionListener(e -> onSubmit());
		buttonPanel.add(submitButton);

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());
		buttonPanel.add(cancelButton);

		// Status label
		statusLabel = new JLabel(" ");
		statusLabel.setForeground(Color.WHITE);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		bottomPanel.add(buttonPanel);
		bottomPanel.add(statusLabel);

		add(titleLabel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	private void onSubmit()
	{
		String eventKey = eventKeyField.getText().trim();
		String secretKey = secretKeyField.getText().trim();

		if (eventKey.isEmpty())
		{
			statusLabel.setText("Event key is required");
			statusLabel.setForeground(Color.RED);
			return;
		}

		if (secretKey.isEmpty())
		{
			statusLabel.setText("Secret key is required");
			statusLabel.setForeground(Color.RED);
			return;
		}

		// Disable buttons while loading
		submitButton.setEnabled(false);
		statusLabel.setText("Loading event...");
		statusLabel.setForeground(Color.YELLOW);

		// Make API request to get the answer
		apiClient.getAnswer(eventKey, secretKey).thenAccept(response -> {
			SwingUtilities.invokeLater(() -> {
				submitButton.setEnabled(true);

				if (response.isSuccess())
				{
					// Successfully retrieved the answer - open the answer builder dialog
					statusLabel.setText("Event loaded successfully!");
					statusLabel.setForeground(Color.GREEN);

					// Close this dialog
					dispose();

					// Open the answer builder dialog in update mode
					AnswerBuilderDialog answerDialog = new AnswerBuilderDialog(
						parentFrame,
						client,
						apiClient,
						eventKey,
						secretKey,
						response.reward_text,
						response.constraints
					);
					answerDialog.setVisible(true);
				}
				else
				{
					// Handle error
					String errorMessage = "Failed to load event: ";
					if ("NOT_FOUND".equals(response.errorType))
					{
						errorMessage += "Event key not found";
					}
					else if ("UNAUTHORIZED".equals(response.errorType))
					{
						errorMessage += "Invalid secret key";
					}
					else if ("NETWORK_ERROR".equals(response.errorType))
					{
						errorMessage += "Network error";
					}
					else
					{
						errorMessage += response.errorMessage;
					}

					statusLabel.setText(errorMessage);
					statusLabel.setForeground(Color.RED);
				}
			});
		});
	}
}
