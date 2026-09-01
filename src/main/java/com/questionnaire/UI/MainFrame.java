package com.questionnaire.UI;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import com.questionnaire.core.TelegramCom;

public class MainFrame {
    private TelegramCom telegramCom;

    private JFrame frame;
    private CommunityPanel communityPanel;
    private CreatePollPanel createPollPanel;

    public MainFrame(TelegramCom telegramCom) {
        this.telegramCom = telegramCom;

        this.initFrame();

        this.initPanelsAndTabs();

        this.frame.setVisible(true);
    }

    private void initFrame() {
        this.frame = new JFrame(UIConstants.WINDOW_TITLE);

        this.frame.setSize(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);

        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.frame.setLocationRelativeTo(null);

    }

    private void initPanelsAndTabs() {
        this.communityPanel = new CommunityPanel(this.telegramCom);
        this.createPollPanel = new CreatePollPanel(this.telegramCom);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.communityPanel, this.createPollPanel);

        splitPane.setResizeWeight(UIConstants.PANELS_SPLIT_RATIO);
        splitPane.setContinuousLayout(true);

        this.frame.add(splitPane);
    }
}
