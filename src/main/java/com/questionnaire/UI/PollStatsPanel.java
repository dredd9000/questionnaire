package com.questionnaire.UI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatClientProperties;
import com.questionnaire.Globals;
import com.questionnaire.UI.enums.PollStatus;
import com.questionnaire.UI.model.RowValueData;
import com.questionnaire.core.CoreConstants;
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

    // Global Stats Labels
    private JLabel totalParticipantsLabel;
    private JLabel completedCountLabel;
    private JLabel pendingCountLabel;
    private JLabel timeRemainingLabel;

    private DefaultTableModel tableModel;
    private JTable membersTable;
    private JButton finishPollBtn;

    private final Map<Long, RowValueData> clientRowMap;

    // Countdown Timer support
    private Timer countdownTimer;
    private int remainingSeconds; // Example duration: 2 minutes (120 seconds)

    public PollStatsPanel(TelegramCom telegramCom, RightSidePanel rightSidePanel, CreatePollPanel createPollPanel) {
        this.telegramCom = telegramCom;
        this.rightSidePanel = rightSidePanel;
        this.createPollPanel = createPollPanel;

        this.clientRowMap = new ConcurrentHashMap<>();

        this.resetRemaining();

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

        // 1. TOP: Header & Status Section (Contains Global Stats)
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

        // Panel for Global Statistics Grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 10, 5));

        totalParticipantsLabel = new JLabel("Participants: 0");
        completedCountLabel = new JLabel("Completed: 0");
        pendingCountLabel = new JLabel("Pending: 0");
        timeRemainingLabel = new JLabel("Time Remaining: 00:00");

        Font statsFont = totalParticipantsLabel.getFont().deriveFont(Font.BOLD, 12f);
        totalParticipantsLabel.setFont(statsFont);
        completedCountLabel.setFont(statsFont);
        pendingCountLabel.setFont(statsFont);
        timeRemainingLabel.setFont(statsFont);

        statsGrid.add(totalParticipantsLabel);
        statsGrid.add(completedCountLabel);
        statsGrid.add(pendingCountLabel);
        statsGrid.add(timeRemainingLabel);

        headerPanel.add(statusLabel, BorderLayout.NORTH);
        headerPanel.add(statsGrid, BorderLayout.CENTER);

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

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        this.membersTable.setDefaultRenderer(Object.class, cellRenderer);

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
        this.startTimer();
    }

    /**
     * Clears table, resets header state, and stops timers.
     */
    public void resetUi() {
        if (this.tableModel != null) {
            this.tableModel.setRowCount(0);
        }

        if (this.statusLabel != null) {
            this.statusLabel.setText("Status: LIVE SURVEY IN PROGRESS");
        }

        if (this.clientRowMap != null) {
            this.clientRowMap.clear();
        }

        if (this.countdownTimer != null) {
            this.countdownTimer.stop();
        }

        updateGlobalStatsSummary();
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

        updateGlobalStatsSummary();
    }

    /**
     * Calculates current status counts from the table and updates global status
     * labels.
     */
    private synchronized void updateGlobalStatsSummary() {
        int total = tableModel != null ? tableModel.getRowCount() : 0;
        int completed = 0;
        int pending = 0;

        if (tableModel != null) {
            for (int i = 0; i < total; i++) {
                Object statusObj = tableModel.getValueAt(i, TABLE_COL_STATUS_IDX);
                if (statusObj != null) {
                    String statusStr = statusObj.toString();
                    if (statusStr.equals(PollStatus.COMPLETED.getLabel())) {
                        completed++;
                    } else {
                        pending++;
                    }
                }
            }

        }

        if (totalParticipantsLabel != null)
            totalParticipantsLabel.setText("Participants: " + total);
        if (completedCountLabel != null)
            completedCountLabel.setText("Completed: " + completed);
        if (pendingCountLabel != null)
            pendingCountLabel.setText("Pending: " + pending);

        if (total != 0 && completed == total) {
            this.endPollAndReset();
        }
    }

    private void resetRemaining() {
        this.remainingSeconds = CoreConstants.MAX_SURVEY_TIME_SEC; // Set remaining time in seconds
    }

    /**
     * Starts or resets the live countdown timer.
     */
    private void startTimer() {
        this.resetRemaining();

        if (this.countdownTimer != null) {
            this.countdownTimer.stop();
        }

        this.countdownTimer = new Timer(1000, e -> {
            if (this.remainingSeconds > 0) {
                this.remainingSeconds--;
                int minutes = this.remainingSeconds / 60;
                int seconds = this.remainingSeconds % 60;
                timeRemainingLabel.setText(String.format("Time Remaining: %02d:%02d", minutes, seconds));

                if (this.remainingSeconds == CoreConstants.NOTIFY_AFTER_SEC) {
                    this.telegramCom.getSurvey().notifyClientsNotCompletedSurvey();
                }
            } else {
                ((Timer) e.getSource()).stop();
                timeRemainingLabel.setText("Time Remaining: 00:00 (Time's Up)");
                this.endPollAndReset();
            }
        });

        timeRemainingLabel
                .setText(String.format("Time Remaining: %02d:%02d",
                        this.remainingSeconds / 60,
                        this.remainingSeconds % 60));
        countdownTimer.start();
    }

    /**
     * Concludes the current survey and returns to Panel 1 (Create Poll).
     */
    private synchronized void endPollAndReset() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        if (telegramCom != null && telegramCom.getSurvey() != null) {
            telegramCom.getSurvey().endSurvey(); // Clears survey group map and changes status
        }

        this.rightSidePanel.showPanel(RightSidePanel.CARD_RESULTS);
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
        }).start();
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

            // Re-calculate and update global stats labels in real time
            this.updateGlobalStatsSummary();
        });
    }
}