package com.questionnaire.UI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatClientProperties;
import com.questionnaire.Globals;
import com.questionnaire.UI.enums.PollStatus;
import com.questionnaire.UI.model.RowValueData;
import com.questionnaire.core.TelegramCom;
import com.questionnaire.core.model.Answer;
import com.questionnaire.core.model.Client;

public class PollStatsPanel extends JPanel {
    private final int TABLE_COL_ANSWERS_IDX = 1;
    private final int TABLE_COL_STATUS_IDX = 2;

    private final TelegramCom telegramCom;
    private final RightSidePanel rightSidePanel;
    private final CreatePollPanel createPollPanel;

    private JLabel statusLabel;
    private JLabel totalParticipantsLabel;
    private DefaultTableModel tableModel;
    private JTable membersTable;
    private JButton finishPollBtn;

    private final Map<Long, RowValueData> clientRowMap;

    public PollStatsPanel(TelegramCom telegramCom, RightSidePanel rightSidePanel, CreatePollPanel createPollPanel) {
        this.telegramCom = telegramCom;
        this.rightSidePanel = rightSidePanel;
        this.createPollPanel = createPollPanel;

        this.clientRowMap = new ConcurrentHashMap<>();

        this.initUI();

        // Automatically trigger display logic when CardLayout switches to this panel
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                onPanelDisplayed();
            }
        });

        this.updateAnswerStatusThread();
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
        String[] columns = { "Full Name", "Answered", "Status" };
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

        if (this.clientRowMap != null) {
            this.clientRowMap.clear();
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

        int rowIdx = 0;
        for (Client client : activeGroup) {
            this.clientRowMap.put(client.getClientId(), new RowValueData(rowIdx));

            tableModel.addRow(new Object[] {
                    client.getFullName(),
                    "0/" + this.telegramCom.getSurvey().getQuestions().size(),
                    PollStatus.PENDING
            });

            rowIdx++;
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

    private void updateAnswerStatusThread() {
        new Thread(() -> {
            while (true) {
                try {
                    Answer answer = this.telegramCom.getSurvey().getNewAnswer().take();

                    RowValueData rowValueData = this.clientRowMap.get(answer.getClientId());

                    if (rowValueData == null) {
                        continue;
                    }

                    rowValueData.incAnsweredCount();

                    this.updateClientProgress(rowValueData);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        })
                .start();
    }

    private void updateClientProgress(RowValueData rowValueData) {
        SwingUtilities.invokeLater(() -> {
            if (this.telegramCom == null
                    || this.telegramCom.getSurvey() == null
                    || !this.telegramCom.getSurvey().isSurveyStarted()
                    || rowValueData == null) {
                return;
            }

            int totalQuestions = this.telegramCom.getSurvey().getQuestions().size();
            int answers = rowValueData.getAnsweredCount();
            int rowIdx = rowValueData.getRowIdx();

            if (rowIdx < 0 || rowIdx >= this.tableModel.getRowCount()) {
                return;
            }

            PollStatus status;
            if (answers == 0) {
                status = PollStatus.PENDING;
            } else if (answers < totalQuestions) {
                status = PollStatus.IN_PROGRESS;
            } else {
                status = PollStatus.COMPLETED;
            }

            this.tableModel.setValueAt(answers + "/" + totalQuestions, rowIdx, TABLE_COL_ANSWERS_IDX);
            this.tableModel.setValueAt(status.getLabel(), rowIdx, TABLE_COL_STATUS_IDX);

        });
    }
}