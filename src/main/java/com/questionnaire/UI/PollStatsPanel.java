package com.questionnaire.UI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatClientProperties;
import com.questionnaire.Globals;
import com.questionnaire.core.TelegramCom;
import com.questionnaire.core.model.Client;

public class PollStatsPanel extends JPanel {
    private final TelegramCom telegramCom;
    private final RightSidePanel rightSidePanel;
    private final CreatePollPanel createPollPanel;

    private JLabel statusLabel;
    private JLabel totalParticipantsLabel;
    private DefaultTableModel tableModel;
    private JTable membersTable;
    private JButton finishPollBtn;

    public PollStatsPanel(TelegramCom telegramCom, RightSidePanel rightSidePanel, CreatePollPanel createPollPanel) {
        this.telegramCom = telegramCom;
        this.rightSidePanel = rightSidePanel;
        this.createPollPanel = createPollPanel;

        this.initUI();

        // Automatically trigger display logic when CardLayout switches to this panel
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                onPanelDisplayed();
            }
        });
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. TOP: Header & Status Section
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. CENTER: Members / Responses Table
        add(createTablePanel(), BorderLayout.CENTER);

        // 3. BOTTOM: Actions (Finish / Create New Poll)
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBorder(BorderFactory.createTitledBorder("Active Poll Overview"));

        statusLabel = new JLabel("Status: LIVE SURVEY IN PROGRESS", SwingConstants.LEFT);
        statusLabel.putClientProperty(FlatClientProperties.STYLE, "font: $h3.font");

        totalParticipantsLabel = new JLabel("Total Target Members: 0", SwingConstants.RIGHT);
        totalParticipantsLabel.setFont(totalParticipantsLabel.getFont().deriveFont(Font.BOLD, 12f));

        headerPanel.add(statusLabel, BorderLayout.WEST);
        headerPanel.add(totalParticipantsLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Community Participants"));

        // Column headers for tracking user state and responses
        String[] columns = { "Client ID", "Full Name", "Username", "Joined At", "Chat ID" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Non-editable table cells
            }
        };

        membersTable = new JTable(tableModel);
        membersTable.getTableHeader().setReorderingAllowed(false);

        tablePanel.add(new JScrollPane(membersTable), BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        finishPollBtn = new JButton("🏁 End Survey & Start New Poll");
        finishPollBtn.setFont(finishPollBtn.getFont().deriveFont(Font.BOLD, 13f));

        finishPollBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to conclude the current survey and start a new one?",
                    "Finish Poll",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                endPollAndReset();
            }
        });

        bottomPanel.add(finishPollBtn);
        return bottomPanel;
    }

    public void onPanelDisplayed() {
        this.resetUi();

        this.telegramCom.getSurvey().startSurvey(this.createPollPanel.getQuestionsList());

        Globals.toast.info("The poll sent to members and live now");

        this.loadActiveSurveyData();
    }

    /**
     * Clears table and resets header state.
     */
    public void resetUi() {
        if (tableModel != null) {
            tableModel.setRowCount(0);
        }
        if (statusLabel != null) {
            statusLabel.setText("Status: LIVE SURVEY IN PROGRESS");
        }
        if (totalParticipantsLabel != null) {
            totalParticipantsLabel.setText("Total Target Members: 0");
        }
    }

    /**
     * Loads active clients from the Telegram survey group into the table model.
     */
    private void loadActiveSurveyData() {
        if (telegramCom == null || telegramCom.getSurvey() == null) {
            return;
        }

        // Retrieve client collection from Survey group map values
        Collection<Client> activeGroup = this.telegramCom.getSurvey().getGroup().getClientsList();

        totalParticipantsLabel.setText("Total Target Members: " + activeGroup.size());

        for (Client client : activeGroup) {
            tableModel.addRow(new Object[] {
                    client.getClientId(),
                    client.getFullName(),
                    client.getUserName(),
                    client.getTimeString(),
                    client.getChatId()
            });
        }
    }

    /**
     * Concludes the current survey and returns to Panel 1 (Create Poll).
     */
    private void endPollAndReset() {
        if (telegramCom != null && telegramCom.getSurvey() != null) {
            telegramCom.getSurvey().endSurvey(); // Clears survey group map and changes status[cite: 3]
        }

        // Return back to Create Poll view
        this.rightSidePanel.showPanel(RightSidePanel.CARD_CREATE_POLL);
    }
}