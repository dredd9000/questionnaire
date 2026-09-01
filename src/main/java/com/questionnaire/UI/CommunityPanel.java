package com.questionnaire.UI;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

public class CommunityPanel extends JPanel {
    // 1. Keep a reference to DefaultTableModel so you can modify it later
    private DefaultTableModel tableModel;
    private JLabel totalMembersLabel;

    public CommunityPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.NORTH);

        JScrollPane tableScrollPane = createTablePanel();
        add(tableScrollPane, BorderLayout.CENTER);
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBorder(BorderFactory.createTitledBorder(Constants.COMMUNITY_OVERVIEW));

        JLabel totalLabel = new JLabel("Total Members: ");

        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 13f));

        this.totalMembersLabel = new JLabel("0");
        this.totalMembersLabel.setFont(this.totalMembersLabel.getFont().deriveFont(Font.BOLD, 13f));

        panel.add(totalLabel);

        panel.add(this.totalMembersLabel);

        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] columns = { "Name", "Telegram Username", "Joined At" };

        Object[][] data = {
                { "Danny Cohen", "@danny", "12:35" },
                { "Yael Levi", "@yael", "12:42" },
                { "Uri Israeli", "@uri", "13:01" }
        };

        // 2. Initialize the instance variable tableModel
        // Also make cells non-editable by overriding isCellEditable
        this.tableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(this.tableModel);

        return new JScrollPane(table);
    }

    /**
     * Call this method whenever a new client joins via Telegram.
     * Appends a single row to the table in real-time.
     */
    public void addClientRow(String name, String telegramUsername, String joinedAt) {
        // Swing components MUST be updated on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            this.tableModel.addRow(new Object[] { name, telegramUsername, joinedAt });
        });
    }

    public void setTotalCommunityMembersNumber(int num) {
        SwingUtilities.invokeLater(() -> {
            this.totalMembersLabel.setText(num + "");
        });
    }
}
