package com.lennyscustomclues.dialogs;

import com.lennyscustomclues.EmoteData;
import com.lennyscustomclues.constraints.ActionConstraint;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EmoteConstraintDialog extends JDialog
{
	private JTextField searchField;
	private JList<String> emoteList;
	private DefaultListModel<String> listModel;
	private List<String> allEmoteNames;

	private ActionConstraint constraint;
	private Consumer<ActionConstraint> onSave;
	private boolean cancelled = true;

	public EmoteConstraintDialog(JFrame parent, ActionConstraint existing, Consumer<ActionConstraint> onSave)
	{
		super(parent, "Emote Constraint", false); // false = non-modal
		this.onSave = onSave;
		this.constraint = existing != null ? existing : new ActionConstraint("emote");

		// Get all emote names
		allEmoteNames = new ArrayList<>(EmoteData.getEmotes().keySet());

		initializeUI();
		populateFields();
		pack();
		setLocationRelativeTo(parent);
		setAlwaysOnTop(true);
	}

	private void initializeUI()
	{
		setLayout(new BorderLayout(10, 10));
		getContentPane().setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Top panel with instructions and search
		JPanel topPanel = new JPanel(new BorderLayout(5, 5));
		topPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

		JLabel instructionLabel = new JLabel("Select an emote:");
		instructionLabel.setForeground(Color.WHITE);
		topPanel.add(instructionLabel, BorderLayout.NORTH);

		// Search field
		JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
		searchPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel searchLabel = new JLabel("Search:");
		searchLabel.setForeground(Color.WHITE);
		searchPanel.add(searchLabel, BorderLayout.WEST);

		searchField = new JTextField(20);
		searchField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchField.setForeground(Color.WHITE);
		searchField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				filterEmotes();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				filterEmotes();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				filterEmotes();
			}
		});
		searchPanel.add(searchField, BorderLayout.CENTER);

		topPanel.add(searchPanel, BorderLayout.SOUTH);

		// Center panel with emote list
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		listModel = new DefaultListModel<>();
		for (String emoteName : allEmoteNames)
		{
			listModel.addElement(emoteName);
		}

		emoteList = new JList<>(listModel);
		emoteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		emoteList.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		emoteList.setForeground(Color.WHITE);
		emoteList.setVisibleRowCount(15);

		JScrollPane scrollPane = new JScrollPane(emoteList);
		scrollPane.setPreferredSize(new Dimension(300, 300));
		centerPanel.add(scrollPane, BorderLayout.CENTER);

		// Buttons panel
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

		JButton saveButton = new JButton("Save");
		JButton cancelButton = new JButton("Cancel");

		saveButton.addActionListener(this::onSave);
		cancelButton.addActionListener(this::onCancel);

		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);

		add(topPanel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void populateFields()
	{
		// If editing an existing constraint, select the corresponding emote
		if (constraint.getEmoteId() != null)
		{
			String emoteName = EmoteData.getEmoteName(constraint.getEmoteId());
			if (emoteName != null && !emoteName.equals("Unknown Emote"))
			{
				emoteList.setSelectedValue(emoteName, true);
			}
		}
	}

	private void filterEmotes()
	{
		String searchText = searchField.getText().toLowerCase();
		listModel.clear();

		for (String emoteName : allEmoteNames)
		{
			if (emoteName.toLowerCase().contains(searchText))
			{
				listModel.addElement(emoteName);
			}
		}

		// If there's only one match, select it
		if (listModel.getSize() == 1)
		{
			emoteList.setSelectedIndex(0);
		}
	}

	private void onSave(ActionEvent e)
	{
		String selectedEmote = emoteList.getSelectedValue();
		if (selectedEmote == null)
		{
			JOptionPane.showMessageDialog(this, "Please select an emote", "Selection Required", JOptionPane.WARNING_MESSAGE);
			return;
		}

		Integer emoteId = EmoteData.getEmoteId(selectedEmote);
		if (emoteId == null)
		{
			JOptionPane.showMessageDialog(this, "Invalid emote selected", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		constraint.setType("emote");
		constraint.setEmoteId(emoteId);

		cancelled = false;
		onSave.accept(constraint);
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
