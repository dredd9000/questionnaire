package com.questionnaire.UI;

import javax.swing.JFrame;

public class MainFrame {
    private JFrame frame;
    private CommunityPanel communityPanel;

    public MainFrame() {
        this.initFrame();

        this.initCommunityPanel();

        this.frame.setVisible(true);
    }

    private void initFrame() {
        this.frame = new JFrame(Constants.WINDOW_TITLE);

        this.frame.setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.frame.setLocationRelativeTo(null);

    }

    private void initCommunityPanel() {
        this.communityPanel = new CommunityPanel();

        this.frame.add(this.communityPanel);
    }
}
