package com.questionnaire.UI;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

public class MainFrame {
    private JFrame frame;
    private CommunityPanel communityPanel;
    private CreatePollPanel createPollPanel;

    public MainFrame() {
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
        this.communityPanel = new CommunityPanel();
        this.createPollPanel = new CreatePollPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.communityPanel, this.createPollPanel);

        splitPane.setResizeWeight(UIConstants.PANELS_SPLIT_RATIO);
        splitPane.setContinuousLayout(true);

        this.frame.add(splitPane);
    }
}
