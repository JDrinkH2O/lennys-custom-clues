package com.lennyscustomclues.dialogs;

import com.lennyscustomclues.ApiClient;
import com.lennyscustomclues.EmoteData;
import com.lennyscustomclues.constraints.LocationConstraint;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class EventInfoDialog extends JDialog
{
	private ApiClient apiClient;
	private Client client;
	private JFrame parentFrame;
	private String eventKey;
	private String secretKey;
	private String rewardText;
	private List<Map<String, Object>> constraints;
	private List<ApiClient.PlayerCompletion> completedBy;

	public EventInfoDialog(JFrame parent, Client client, ApiClient apiClient,
		String eventKey, String secretKey, String rewardText,
		List<Map<String, Object>> constraints, List<ApiClient.PlayerCompletion> completedBy)
	{
		super(parent, "Event Information", true);
		this.parentFrame = parent;
		this.client = client;
		this.apiClient = apiClient;
		this.eventKey = eventKey;
		this.secretKey = secretKey;
		this.rewardText = rewardText;
		this.constraints = constraints;
		this.completedBy = completedBy;

		initializeUI();
		pack();
		setMinimumSize(new Dimension(600, 500));
		setLocationRelativeTo(parent);
	}

	private void initializeUI()
	{
		setLayout(new BorderLayout());
		getContentPane().setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Title
		JLabel titleLabel = new JLabel("Event: " + eventKey);
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(FontManager.getRunescapeFont().deriveFont(Font.BOLD, 14f));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Main content panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Answer summary panel
		JPanel summaryPanel = createAnswerSummaryPanel();

		// Solvers panel
		JPanel solversPanel = createSolversPanel();

		mainPanel.add(summaryPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(solversPanel);

		// Bottom panel with buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JButton editButton = new JButton("Edit Answer");
		editButton.addActionListener(e -> onEditAnswer());
		buttonPanel.add(editButton);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> dispose());
		buttonPanel.add(closeButton);

		add(titleLabel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private JPanel createAnswerSummaryPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
			"Answer Summary",
			0,
			0,
			FontManager.getRunescapeFont(),
			Color.WHITE
		));

		// Build the summary text
		StringBuilder summaryBuilder = new StringBuilder();
		summaryBuilder.append("Reward: ").append(rewardText != null && !rewardText.trim().isEmpty() ? rewardText : "(No reward text specified)").append("\n\n");

		if (constraints == null || constraints.isEmpty())
		{
			summaryBuilder.append("No constraints defined");
		}
		else
		{
			// Check if there's an emote constraint
			String requiredAction = "Dig with a spade";
			for (Map<String, Object> constraintMap : constraints)
			{
				String constraintType = (String) constraintMap.get("constraint_type");
				if ("action".equals(constraintType))
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
								String emoteName = EmoteData.getEmoteName(emoteId);
								requiredAction = "Perform an Emote: " + emoteName;
							}
						}
					}
				}
			}
			summaryBuilder.append("Required Action: ").append(requiredAction).append("\n");

			for (Map<String, Object> constraintMap : constraints)
			{
				String constraintType = (String) constraintMap.get("constraint_type");
				if ("location".equals(constraintType))
				{
					summaryBuilder.append(getLocationConstraintDescription(constraintMap));
				}
			}
		}

		JTextArea summaryArea = new JTextArea(summaryBuilder.toString());
		summaryArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		summaryArea.setForeground(Color.WHITE);
		summaryArea.setFont(FontManager.getRunescapeSmallFont());
		summaryArea.setEditable(false);
		summaryArea.setLineWrap(true);
		summaryArea.setWrapStyleWord(true);

		JScrollPane scrollPane = new JScrollPane(summaryArea);
		scrollPane.setPreferredSize(new Dimension(0, 120));
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createSolversPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
			"Players Who Solved This Event",
			0,
			0,
			FontManager.getRunescapeFont(),
			Color.WHITE
		));

		// Create table
		String[] columnNames = {"Rank", "Player Name", "Completed At"};
		DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false; // Make table non-editable
			}
		};

		// Add data to table
		if (completedBy != null && !completedBy.isEmpty())
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getDefault());

			for (int i = 0; i < completedBy.size(); i++)
			{
				ApiClient.PlayerCompletion completion = completedBy.get(i);
				String rank = String.valueOf(i + 1);
				String playerName = completion.rsn;
				String timestamp = formatTimestamp(completion.timestamp);

				tableModel.addRow(new Object[]{rank, playerName, timestamp});
			}
		}

		JTable table = new JTable(tableModel);
		table.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		table.setForeground(Color.WHITE);
		table.setFont(FontManager.getRunescapeSmallFont());
		table.setSelectionBackground(ColorScheme.MEDIUM_GRAY_COLOR);
		table.setSelectionForeground(Color.WHITE);
		table.setGridColor(ColorScheme.MEDIUM_GRAY_COLOR);
		table.setShowGrid(true);
		table.setRowHeight(25);

		// Center align the Rank column
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.getColumnModel().getColumn(2).setPreferredWidth(200);

		// Style table header
		table.getTableHeader().setBackground(ColorScheme.DARKER_GRAY_COLOR);
		table.getTableHeader().setForeground(Color.WHITE);
		table.getTableHeader().setFont(FontManager.getRunescapeFont());

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(0, 200));

		// Add message if no players have solved it yet
		if (completedBy == null || completedBy.isEmpty())
		{
			JLabel noSolversLabel = new JLabel("No players have solved this event yet");
			noSolversLabel.setForeground(Color.LIGHT_GRAY);
			noSolversLabel.setFont(FontManager.getRunescapeSmallFont());
			noSolversLabel.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(noSolversLabel, BorderLayout.CENTER);
		}
		else
		{
			panel.add(scrollPane, BorderLayout.CENTER);
		}

		return panel;
	}

	private String formatTimestamp(String timestamp)
	{
		if (timestamp == null || timestamp.isEmpty())
		{
			return "Unknown";
		}

		try
		{
			// Parse ISO 8601 timestamp (e.g., "2024-11-23T12:34:56.789Z")
			SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

			// Handle both with and without milliseconds
			String cleanTimestamp = timestamp.replace("Z", "");
			if (cleanTimestamp.contains("."))
			{
				cleanTimestamp = cleanTimestamp.substring(0, cleanTimestamp.indexOf("."));
			}

			Date date = inputFormat.parse(cleanTimestamp);

			// Format to local time
			SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			outputFormat.setTimeZone(TimeZone.getDefault());
			return outputFormat.format(date);
		}
		catch (Exception e)
		{
			// If parsing fails, return the original timestamp
			return timestamp;
		}
	}

	private String getLocationConstraintDescription(Map<String, Object> constraintMap)
	{
		StringBuilder desc = new StringBuilder();
		String type = (String) constraintMap.get("type");

		switch (type)
		{
			case "exact":
				desc.append("Location: Exact tile (");
				desc.append("X: ").append(getIntValue(constraintMap, "exact_x"));
				desc.append(", Y: ").append(getIntValue(constraintMap, "exact_y"));
				if (constraintMap.containsKey("plane"))
				{
					desc.append(", Plane: ").append(getIntValue(constraintMap, "plane"));
				}
				desc.append(")");
				break;

			case "bounds":
				desc.append("Location: Within rectangle (");
				desc.append("X: ").append(getIntValue(constraintMap, "min_x")).append("-").append(getIntValue(constraintMap, "max_x"));
				desc.append(", Y: ").append(getIntValue(constraintMap, "min_y")).append("-").append(getIntValue(constraintMap, "max_y"));
				if (constraintMap.containsKey("plane"))
				{
					desc.append(", Plane: ").append(getIntValue(constraintMap, "plane"));
				}
				desc.append(")");
				break;

			case "tolerance":
				desc.append("Location: Near tile (");
				desc.append("X: ").append(getIntValue(constraintMap, "exact_x"));
				desc.append(", Y: ").append(getIntValue(constraintMap, "exact_y"));
				if (constraintMap.containsKey("tolerance"))
				{
					desc.append(", within ").append(getIntValue(constraintMap, "tolerance")).append(" tiles");
				}
				if (constraintMap.containsKey("plane"))
				{
					desc.append(", Plane: ").append(getIntValue(constraintMap, "plane"));
				}
				desc.append(")");
				break;

			default:
				desc.append("Location: ").append(type);
				break;
		}

		return desc.toString();
	}

	private int getIntValue(Map<String, Object> map, String key)
	{
		Object value = map.get(key);
		if (value instanceof Number)
		{
			return ((Number) value).intValue();
		}
		return 0;
	}

	private void onEditAnswer()
	{
		// Close this dialog
		dispose();

		// Open the answer builder dialog in update mode
		AnswerBuilderDialog answerDialog = new AnswerBuilderDialog(
			parentFrame,
			client,
			apiClient,
			eventKey,
			secretKey,
			rewardText,
			constraints
		);
		answerDialog.setVisible(true);
	}
}
