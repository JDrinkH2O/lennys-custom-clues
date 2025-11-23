package com.lennyscustomclues.dialogs;

import com.lennyscustomclues.AnswerBuilder;
import com.lennyscustomclues.ApiClient;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

public class SubmitAnswerDialog extends JDialog
{
	private JTextField eventKeyField;
	private JButton validateKeyButton;
	private JButton submitButton;
	private JButton cancelButton;
	private JLabel validationStatusLabel;
	
	private AnswerBuilder answerBuilder;
	private ApiClient apiClient;
	private Consumer<String> onSubmit;
	private boolean cancelled = true;
	private boolean keyValidated = false;

	public SubmitAnswerDialog(JFrame parent, AnswerBuilder answerBuilder, ApiClient apiClient, Consumer<String> onSubmit)
	{
		super(parent, "Submit Answer to Server", false); // Non-modal
		this.answerBuilder = answerBuilder;
		this.apiClient = apiClient;
		this.onSubmit = onSubmit;
		
		initializeUI();
		pack();
		setLocationRelativeTo(parent);
		setAlwaysOnTop(true);
	}

	private void initializeUI()
	{
		setLayout(new BorderLayout());
		getContentPane().setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Main content panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		// Title/instructions
		JLabel titleLabel = new JLabel("Submit Answer to Server");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel instructionLabel = new JLabel("<html><center>Enter an event key to associate with this answer.<br/>The key must be unique and not already in use.</center></html>");
		instructionLabel.setForeground(Color.LIGHT_GRAY);
		instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Answer summary
		JPanel summaryPanel = new JPanel(new BorderLayout());
		summaryPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		summaryPanel.setBorder(BorderFactory.createTitledBorder("Answer Summary"));
		summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

		String rewardText = answerBuilder.getRewardText();
		if (rewardText == null || rewardText.trim().isEmpty())
		{
			rewardText = "(No reward text specified)";
		}

		JTextArea summaryArea = new JTextArea(String.format("Reward: %s\nConstraints: %d", 
			rewardText, answerBuilder.getConstraintCount()));
		summaryArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		summaryArea.setForeground(Color.WHITE);
		summaryArea.setEditable(false);
		summaryArea.setLineWrap(true);
		summaryArea.setWrapStyleWord(true);
		JScrollPane summaryScroll = new JScrollPane(summaryArea);
		summaryPanel.add(summaryScroll, BorderLayout.CENTER);

		// Event key input panel
		JPanel eventKeyPanel = new JPanel(new BorderLayout());
		eventKeyPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		eventKeyPanel.setBorder(BorderFactory.createTitledBorder("Event Key"));

		eventKeyField = new JTextField(20);
		eventKeyField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		eventKeyField.setForeground(Color.WHITE);
		eventKeyField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			public void insertUpdate(javax.swing.event.DocumentEvent e) { onEventKeyChanged(); }
			public void removeUpdate(javax.swing.event.DocumentEvent e) { onEventKeyChanged(); }
			public void changedUpdate(javax.swing.event.DocumentEvent e) { onEventKeyChanged(); }
		});

		validateKeyButton = new JButton("Validate Key");
		validateKeyButton.addActionListener(this::onValidateKey);
		validateKeyButton.setEnabled(false);

		JPanel keyInputPanel = new JPanel(new BorderLayout());
		keyInputPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		keyInputPanel.add(eventKeyField, BorderLayout.CENTER);
		keyInputPanel.add(validateKeyButton, BorderLayout.EAST);

		validationStatusLabel = new JLabel(" ");
		validationStatusLabel.setForeground(Color.GRAY);

		eventKeyPanel.add(keyInputPanel, BorderLayout.NORTH);
		eventKeyPanel.add(validationStatusLabel, BorderLayout.SOUTH);

		// Button panel
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		submitButton = new JButton("Submit Answer");
		submitButton.addActionListener(this::onSubmit);
		submitButton.setEnabled(false); // Initially disabled

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this::onCancel);

		buttonPanel.add(submitButton);
		buttonPanel.add(cancelButton);

		// Add all components
		mainPanel.add(titleLabel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(instructionLabel);
		mainPanel.add(Box.createVerticalStrut(15));
		mainPanel.add(summaryPanel);
		mainPanel.add(Box.createVerticalStrut(15));
		mainPanel.add(eventKeyPanel);

		add(mainPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void onEventKeyChanged()
	{
		String eventKey = eventKeyField.getText().trim();
		keyValidated = false;
		validationStatusLabel.setText(" ");
		validationStatusLabel.setForeground(Color.GRAY);
		
		validateKeyButton.setEnabled(!eventKey.isEmpty());
		submitButton.setEnabled(false);
	}

	private void onValidateKey(ActionEvent e)
	{
		String eventKey = eventKeyField.getText().trim();
		if (eventKey.isEmpty())
		{
			return;
		}

		validateKeyButton.setEnabled(false);
		validationStatusLabel.setText("Validating key...");
		validationStatusLabel.setForeground(Color.YELLOW);

		// Validate the key with the API
		apiClient.validateEventKey(eventKey).thenAccept(response -> {
			SwingUtilities.invokeLater(() -> {
				if (response.success)
				{
					// 200 response means key already exists - NOT available for new answer
					keyValidated = false;
					validationStatusLabel.setText("✗ Key already exists");
					validationStatusLabel.setForeground(Color.RED);
					submitButton.setEnabled(false);
				}
				else
				{
					// Non-success response
					if ("KEY_NOT_FOUND".equals(response.errorType))
					{
						// 404 means key doesn't exist - GOOD for creating new answer
						keyValidated = true;
						validationStatusLabel.setText("✓ Key is available");
						validationStatusLabel.setForeground(Color.GREEN);
						submitButton.setEnabled(true);
					}
					else
					{
						// Other error (server error, network error, etc.)
						keyValidated = false;
						validationStatusLabel.setText("✗ " + response.message);
						validationStatusLabel.setForeground(Color.RED);
						submitButton.setEnabled(false);
					}
				}
				validateKeyButton.setEnabled(true);
			});
		});
	}

	private void onSubmit(ActionEvent e)
	{
		if (!keyValidated)
		{
			JOptionPane.showMessageDialog(this, "Please validate the event key first.", "Validation Required", JOptionPane.WARNING_MESSAGE);
			return;
		}

		String eventKey = eventKeyField.getText().trim();
		if (eventKey.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "Please enter an event key.", "Event Key Required", JOptionPane.WARNING_MESSAGE);
			return;
		}

		cancelled = false;
		onSubmit.accept(eventKey);
		dispose();
	}

	private void onCancel(ActionEvent e)
	{
		cancelled = true;
		dispose();
	}

	public boolean wasCancelled()
	{
		return cancelled;
	}
}