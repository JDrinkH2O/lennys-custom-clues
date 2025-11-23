package com.lennyscustomclues;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class EventKeyDialog extends JDialog
{
	private final JTextField eventKeyField;
	private final JButton submitButton;
	private final JButton cancelButton;
	private final JLabel statusLabel;
	private final Consumer<String> onEventKeySet;
	private final ApiClient apiClient;
	private boolean submitted = false;
	private boolean validating = false;

	public EventKeyDialog(JFrame parent, String currentEventKey, boolean isChanging, Consumer<String> onEventKeySet, ApiClient apiClient)
	{
		super(parent, isChanging ? "Change Event Key" : "Set Event Key", true);
		this.onEventKeySet = onEventKeySet;
		this.apiClient = apiClient;

		// Setup dialog properties
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);

		// Create components
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Title label
		JLabel titleLabel = new JLabel("Enter event key:");
		titleLabel.setFont(FontManager.getRunescapeFont());
		titleLabel.setForeground(Color.WHITE);

		// Text field
		eventKeyField = new JTextField(20);
		eventKeyField.setFont(FontManager.getRunescapeFont());
		eventKeyField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		eventKeyField.setForeground(Color.WHITE);
		eventKeyField.setCaretColor(Color.WHITE);
		eventKeyField.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
			BorderFactory.createEmptyBorder(5, 5, 5, 5)
		));

		// Pre-fill if changing
		if (isChanging && currentEventKey != null)
		{
			eventKeyField.setText(currentEventKey);
			eventKeyField.selectAll();
		}

		// Status label for validation feedback
		statusLabel = new JLabel(" "); // Space to maintain height
		statusLabel.setFont(FontManager.getRunescapeSmallFont());
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		cancelButton = new JButton("Cancel");
		styleButton(cancelButton);
		cancelButton.addActionListener(this::onCancelClick);

		submitButton = new JButton("Submit");
		styleButton(submitButton);
		submitButton.addActionListener(this::onSubmitClick);

		buttonPanel.add(cancelButton);
		buttonPanel.add(submitButton);

		// Layout
		JPanel inputPanel = new JPanel(new BorderLayout(0, 5));
		inputPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		inputPanel.add(titleLabel, BorderLayout.NORTH);
		inputPanel.add(eventKeyField, BorderLayout.CENTER);
		inputPanel.add(statusLabel, BorderLayout.SOUTH);

		mainPanel.add(inputPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		add(mainPanel);

		// Setup keyboard shortcuts
		setupKeyBindings();

		// Pack and center
		pack();
		setLocationRelativeTo(parent);

		// Focus text field
		SwingUtilities.invokeLater(() -> eventKeyField.requestFocusInWindow());
	}

	private void styleButton(JButton button)
	{
		button.setFont(FontManager.getRunescapeFont());
		button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		button.setForeground(Color.WHITE);
		button.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
			BorderFactory.createEmptyBorder(5, 15, 5, 15)
		));
		button.setFocusPainted(false);
	}

	private void setupKeyBindings()
	{
		// Enter key submits
		eventKeyField.addActionListener(this::onSubmitClick);

		// Escape key cancels
		getRootPane().registerKeyboardAction(
			this::onCancelClick,
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW
		);

		// Make Submit the default button
		getRootPane().setDefaultButton(submitButton);
	}

	private void onSubmitClick(ActionEvent e)
	{
		if (validating)
		{
			return; // Ignore if already validating
		}

		String eventKey = eventKeyField.getText().trim();
		
		if (eventKey.isEmpty())
		{
			setStatusMessage("Event key cannot be empty.", true);
			eventKeyField.requestFocusInWindow();
			return;
		}

		// Start validation
		validating = true;
		setButtonsEnabled(false);
		setStatusMessage("Validating event key...", false);

		apiClient.validateEventKey(eventKey).thenAccept(response -> {
			SwingUtilities.invokeLater(() -> {
				validating = false;
				setButtonsEnabled(true);

				if (response.success)
				{
					// Key is valid, proceed
					submitted = true;
					onEventKeySet.accept(eventKey);
					dispose();
				}
				else
				{
					// Handle different error types with user-friendly messages
					String userMessage = getUserFriendlyErrorMessage(response);
					setStatusMessage(userMessage, true);
					eventKeyField.requestFocusInWindow();
					eventKeyField.selectAll();
				}
			});
		}).exceptionally(throwable -> {
			SwingUtilities.invokeLater(() -> {
				validating = false;
				setButtonsEnabled(true);
				setStatusMessage("Can't reach the server", true);
				eventKeyField.requestFocusInWindow();
			});
			return null;
		});
	}

	private String getUserFriendlyErrorMessage(ApiClient.ApiResponse response)
	{
		if (response.errorType != null)
		{
			switch (response.errorType)
			{
				case "KEY_NOT_FOUND":
					return "Invalid event key";
				case "NETWORK_ERROR":
				case "IO_ERROR":
				case "REQUEST_ERROR":
					return "Can't reach the server";
				case "SERVER_ERROR":
				case "PARSE_ERROR":
				default:
					return "An unexpected error occurred";
			}
		}
		
		// Fallback for responses without error type
		return "Invalid event key";
	}

	private void onCancelClick(ActionEvent e)
	{
		if (!validating)
		{
			dispose();
		}
	}

	private void setStatusMessage(String message, boolean isError)
	{
		if (isError)
		{
			statusLabel.setForeground(Color.RED);
		}
		else
		{
			statusLabel.setForeground(Color.LIGHT_GRAY);
		}
		statusLabel.setText(message);
	}

	private void setButtonsEnabled(boolean enabled)
	{
		submitButton.setEnabled(enabled);
		cancelButton.setEnabled(enabled);
		eventKeyField.setEnabled(enabled);
		
		if (enabled)
		{
			submitButton.setText("Submit");
		}
		else
		{
			submitButton.setText("Validating...");
		}
	}

	public boolean wasSubmitted()
	{
		return submitted;
	}
}