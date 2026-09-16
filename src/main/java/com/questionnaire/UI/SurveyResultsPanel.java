package com.questionnaire.UI;

import com.questionnaire.Globals;
import com.questionnaire.core.TelegramCom;
import com.questionnaire.core.model.AnswerResult;
import com.questionnaire.core.model.QuestionResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SurveyResultsPanel extends JPanel {

    private final JPanel containerPanel;
    private final TelegramCom telegramCom;
    private final RightSidePanel rightSidePanel;

    public SurveyResultsPanel(TelegramCom telegramCom, RightSidePanel rightSidePanel) {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        this.telegramCom = telegramCom;
        this.rightSidePanel = rightSidePanel;

        // Header Title
        JLabel headerLabel = new JLabel("Survey Results", SwingConstants.CENTER);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        headerLabel.setForeground(new Color(33, 37, 41));
        headerLabel.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(headerLabel, BorderLayout.NORTH);

        // Container for question cards
        containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.setBackground(new Color(245, 247, 250));
        containerPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(containerPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Footer with "Start New Survey" Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(new Color(245, 247, 250));
        bottomPanel.setBorder(new EmptyBorder(10, 15, 15, 15));

        JButton startNewSurveyButton = new JButton("Start New Survey");
        startNewSurveyButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        startNewSurveyButton.setBackground(new Color(13, 110, 253));
        startNewSurveyButton.setForeground(Color.WHITE);
        startNewSurveyButton.setFocusPainted(false);
        startNewSurveyButton.setPreferredSize(new Dimension(180, 40));

        startNewSurveyButton.addActionListener(e -> onStartNewSurveyClicked());
        bottomPanel.add(startNewSurveyButton);

        add(bottomPanel, BorderLayout.SOUTH);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                onPanelDisplayed();
            }
        });
    }

    private void onStartNewSurveyClicked() {
        if (this.telegramCom != null && this.telegramCom.getSurvey() != null) {
            // Set status back to PRE so a new survey can be set up
            this.telegramCom.getSurvey().preSurvey();
        }

        // Navigate back to the setup view using RightSidePanel
        if (this.rightSidePanel != null) {
            // Adjust card name ("SURVEY_SETUP" / "CREATE_SURVEY") to match your
            // RightSidePanel setup
            this.rightSidePanel.showPanel(RightSidePanel.CARD_CREATE_POLL);
        }
    }

    private void onPanelDisplayed() {
        if (this.telegramCom != null && this.telegramCom.getSurvey() != null) {
            Globals.toast.success("The poll ended and here is the results");
            this.displayResults(this.telegramCom.getSurvey().getResults());
        }
    }

    public void displayResults(List<QuestionResult> results) {
        containerPanel.removeAll();

        if (results == null || results.isEmpty()) {
            JLabel emptyLabel = new JLabel("No survey results to display.");
            emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            containerPanel.add(emptyLabel);
        } else {
            for (int i = 0; i < results.size(); i++) {
                QuestionResult qResult = results.get(i);

                // Copy and sort answers by frequency (votes) descending
                List<AnswerResult> sortedAnswers = new ArrayList<>(qResult.getResults());
                sortedAnswers.sort(Comparator.comparingInt(AnswerResult::getVotes).reversed());

                containerPanel.add(createQuestionCard(i + 1, qResult.getQuestion(), sortedAnswers));
                containerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }

        containerPanel.revalidate();
        containerPanel.repaint();
    }

    private JPanel createQuestionCard(int qNum, String questionText, List<AnswerResult> answers) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel("Q" + qNum + ". " + questionText);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(new Color(49, 53, 59));
        card.add(titleLabel, BorderLayout.NORTH);

        JPanel answersPanel = new JPanel();
        answersPanel.setLayout(new BoxLayout(answersPanel, BoxLayout.Y_AXIS));
        answersPanel.setOpaque(false);

        if (answers == null || answers.isEmpty()) {
            JLabel noVotesLabel = new JLabel("No choices available.");
            noVotesLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
            noVotesLabel.setForeground(Color.GRAY);
            answersPanel.add(noVotesLabel);
        } else {
            for (AnswerResult answer : answers) {
                answersPanel.add(createAnswerRow(answer));
                answersPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        card.add(answersPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createAnswerRow(AnswerResult answerResult) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel answerTextLabel = new JLabel(answerResult.getAnswer());
        answerTextLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        answerTextLabel.setPreferredSize(new Dimension(220, 25));

        double pct = answerResult.getPercentage();
        if (Double.isNaN(pct) || Double.isInfinite(pct)) {
            pct = 0.0;
        }

        JProgressBar progressBar = new JProgressBar(0, 100);
        int percentageInt = (int) Math.round(pct);
        progressBar.setValue(percentageInt);
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("%.1f%%", pct));
        progressBar.setFont(new Font("SansSerif", Font.BOLD, 11));
        progressBar.setForeground(new Color(13, 110, 253));
        progressBar.setPreferredSize(new Dimension(200, 20));

        JLabel votesLabel = new JLabel(answerResult.getVotes() + " vote(s)", SwingConstants.RIGHT);
        votesLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        votesLabel.setForeground(Color.GRAY);
        votesLabel.setPreferredSize(new Dimension(80, 25));

        row.add(answerTextLabel, BorderLayout.WEST);
        row.add(progressBar, BorderLayout.CENTER);
        row.add(votesLabel, BorderLayout.EAST);

        return row;
    }
}