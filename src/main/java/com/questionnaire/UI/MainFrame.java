package com.questionnaire.UI;

import javax.swing.JFrame;

public class MainFrame {
    private JFrame frame;

    public MainFrame() {
        this.initFrame();
    }

    private void initFrame() {
        this.frame = new JFrame(Constants.WINDOW_TITLE);

        this.frame.setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.frame.setLocationRelativeTo(null);

        this.frame.setVisible(true);
    }
}
