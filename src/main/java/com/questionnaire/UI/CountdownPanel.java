package com.questionnaire.UI;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.formdev.flatlaf.FlatClientProperties;

public class CountdownPanel extends JPanel {

    private RightSidePanel rightSidePanel;
    private CreatePollPanel createPollPanel;

    private JLabel timerLabel;
    private JLabel subTextLabel;
    private Timer swingTimer;
    private int remainingSeconds;

    public CountdownPanel(RightSidePanel rightSidePanel, CreatePollPanel createPollPanel) {
        this.rightSidePanel = rightSidePanel;
        this.createPollPanel = createPollPanel;

        this.initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Center box container
        JPanel centerPanel = new JPanel(new GridBagLayout());
        JPanel contentBox = new JPanel(new BorderLayout(0, 10));

        // Subtitle header
        subTextLabel = new JLabel("Poll Launching In...", SwingConstants.CENTER);
        subTextLabel.putClientProperty(FlatClientProperties.STYLE, "font: $h3.font");

        // Timer display initialized to 00:00 with monospaced font
        timerLabel = new JLabel("00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 48));

        contentBox.add(subTextLabel, BorderLayout.NORTH);
        contentBox.add(timerLabel, BorderLayout.CENTER);

        centerPanel.add(contentBox);
        add(centerPanel, BorderLayout.CENTER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // Fired when CardLayout switches TO this panel
                onPanelDisplayed();
            }
        });
    }

    private void onPanelDisplayed() {
        this.resetUi();

        this.startCountdown();
    }

    public void resetUi() {
        // 1. Stop active timer if running
        this.stopCountdown();

        // 2. Reset counter state
        this.remainingSeconds = 0;

        // 3. Reset labels to default state
        if (this.subTextLabel != null) {
            this.subTextLabel.setText("Poll Launching In...");
        }

        if (this.timerLabel != null) {
            this.timerLabel.setText("00:00");
        }
    }

    /**
     * Pulls the delay time from CreatePollPanel and starts the countdown.
     * Executes custom logic when timer reaches 00:00.
     */
    public void startCountdown() {
        if (swingTimer != null && swingTimer.isRunning()) {
            swingTimer.stop();
        }

        // 1. Fetch remaining seconds from CreatePollPanel getter
        this.remainingSeconds = this.createPollPanel.getDelayInMin() * 60;
        this.updateTimerDisplay();

        // 2. Start Swing timer ticking every second
        swingTimer = new Timer(1000, e -> {
            remainingSeconds--;
            updateTimerDisplay();

            if (remainingSeconds <= 0) {
                swingTimer.stop();
                onCountdownFinished(); // Trigger completion logic
            }
        });

        swingTimer.start();
    }

    /**
     * Logic executed when the timer completes (00:00).
     */
    private void onCountdownFinished() {
        // this.telegramCom.getSurvey().startSurvey(null);

        this.rightSidePanel.showPanel(RightSidePanel.CARD_STATS);
    }

    private void updateTimerDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    public void stopCountdown() {
        if (swingTimer != null && swingTimer.isRunning()) {
            swingTimer.stop();
        }
    }
}
