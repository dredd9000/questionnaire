package com.questionnaire.UI;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.JPanel;

import com.questionnaire.core.TelegramCom;

public class RightSidePanel extends JPanel {
    // Unique Card Keys
    public static final String CARD_CREATE_POLL = "CREATE_POLL";
    public static final String CARD_COUNTDOWN = "COUNTDOWN";
    public static final String CARD_STATS = "STATS";

    private final CardLayout cardLayout;
    private final JPanel rightPanelContainer;

    private final TelegramCom telegramCom;

    // View Panels
    private CreatePollPanel createPollPanel;
    private CountdownPanel countdownPanel; // Optional
    private PollStatsPanel pollStatsPanel;

    public RightSidePanel(TelegramCom telegramCom) {
        this.telegramCom = telegramCom;

        cardLayout = new CardLayout();
        rightPanelContainer = new JPanel(cardLayout);

        // Initialize sub-panels
        this.initPanels();

        // Add panels to CardLayout container
        rightPanelContainer.add(createPollPanel, CARD_CREATE_POLL);
        rightPanelContainer.add(countdownPanel, CARD_COUNTDOWN);
        rightPanelContainer.add(pollStatsPanel, CARD_STATS);

        setLayout(new BorderLayout());
        add(this.rightPanelContainer, BorderLayout.CENTER);

        // Default view
        showPanel(CARD_CREATE_POLL);
    }

    private void initPanels() {
        this.createPollPanel = new CreatePollPanel(this.telegramCom, this);
        this.countdownPanel = new CountdownPanel();
        this.pollStatsPanel = new PollStatsPanel();
    }

    public void showPanel(String cardKey) {
        cardLayout.show(rightPanelContainer, cardKey);
    }
}
