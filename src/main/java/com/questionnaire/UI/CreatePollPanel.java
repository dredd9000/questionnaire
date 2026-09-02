package com.questionnaire.UI;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.questionnaire.Globals;
import com.questionnaire.UI.helpers.InputValidations;
import com.questionnaire.UI.helpers.QuestionBlock;
import com.questionnaire.core.CoreConstants;
import com.questionnaire.core.TelegramCom;
import com.questionnaire.core.model.Question;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

public class CreatePollPanel extends JPanel {
    private TelegramCom telegramCom;
    private RightSidePanel rightSidePanel;

    private JRadioButton manualRadio;
    private JRadioButton aiRadio;
    private JTextField aiTopicField;
    private JButton generateAiBtn;

    private JPanel questionsContainer;
    private List<QuestionBlock> questionBlocks;
    private JButton addQuestionBtn;

    private JRadioButton immediateRadio;
    private JRadioButton delayedRadio;
    private JSpinner delayMinutesSpinner;

    private JButton launchPollBtn;
    private JLabel statusBannerLabel;

    private java.util.function.Consumer<String> onAiGenerateCallback;

    public CreatePollPanel(TelegramCom telegramCom, RightSidePanel rightSidePanel) {
        this.telegramCom = telegramCom;
        this.rightSidePanel = rightSidePanel;

        this.questionBlocks = new ArrayList<>();
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. TOP: Mode Selection & AI Prompt Input
        add(createModeSelectionPanel(), BorderLayout.NORTH);

        // 2. CENTER: Dynamic Questions Container
        add(createQuestionsScrollPane(), BorderLayout.CENTER);

        // 3. BOTTOM: Dispatch Options & Launch Controls
        add(createBottomControlPanel(), BorderLayout.SOUTH);

        // Default setup: start with 1 question block
        addQuestionBlock();
        updateModeState();
    }

    // --- SECTION 1: Creation Mode (Manual vs AI) ---
    private JPanel createModeSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Creation Mode"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        manualRadio = new JRadioButton("Manual Entry", true);
        aiRadio = new JRadioButton("ChatGPT AI Generator");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(manualRadio);
        modeGroup.add(aiRadio);

        aiTopicField = new JTextField(30);
        aiTopicField.setToolTipText("Enter a general topic (e.g., Software Engineering Tech Preferences)");
        generateAiBtn = new JButton("✨ Generate Questions");

        ActionListener modeListener = e -> updateModeState();
        manualRadio.addActionListener(modeListener);
        aiRadio.addActionListener(modeListener);

        generateAiBtn.addActionListener(e -> {
            if (onAiGenerateCallback != null && !aiTopicField.getText().trim().isEmpty()) {
                onAiGenerateCallback.accept(aiTopicField.getText().trim());
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(manualRadio, gbc);
        gbc.gridx = 1;
        panel.add(aiRadio, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("AI Topic Prompt:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(aiTopicField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panel.add(generateAiBtn, gbc);

        return panel;
    }

    // --- SECTION 2: Questions Scroll Area ---
    private JScrollPane createQuestionsScrollPane() {
        questionsContainer = new JPanel();
        questionsContainer.setLayout(new BoxLayout(questionsContainer, BoxLayout.Y_AXIS));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(questionsContainer, BorderLayout.NORTH);

        addQuestionBtn = new JButton("+ Add Question (Max " + CoreConstants.MAX_QUESTIONS + ")");
        addQuestionBtn.addActionListener(e -> addQuestionBlock());
        wrapper.add(addQuestionBtn, BorderLayout.SOUTH);

        return new JScrollPane(wrapper);
    }

    private void addQuestionBlock() {
        if (questionBlocks.size() >= CoreConstants.MAX_QUESTIONS)
            return;

        int questionNum = questionBlocks.size() + 1;
        QuestionBlock block = new QuestionBlock(questionNum, () -> removeQuestionBlock(questionBlocks.size() - 1));
        questionBlocks.add(block);
        questionsContainer.add(block);

        updateQuestionControls();
        revalidate();
        repaint();
    }

    private void removeQuestionBlock(int index) {
        if (questionBlocks.size() <= CoreConstants.MIN_QUESTIONS)
            return; // Must keep at least 1 question

        questionsContainer.remove(questionBlocks.get(index));
        questionBlocks.remove(index);

        // Renumber remaining questions
        for (int i = 0; i < questionBlocks.size(); i++) {
            questionBlocks.get(i).setQuestionNumber(i + 1);
        }

        updateQuestionControls();
        revalidate();
        repaint();
    }

    private void updateQuestionControls() {
        addQuestionBtn.setEnabled(questionBlocks.size() < CoreConstants.MAX_QUESTIONS);
        for (int i = 0; i < questionBlocks.size(); i++) {
            questionBlocks.get(i).setDeleteEnabled(questionBlocks.size() > 1 && i == questionBlocks.size() - 1);
        }
    }

    // --- SECTION 3: Bottom Control Panel (Timing & Launch Guardrails) ---
    private JPanel createBottomControlPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        // Dispatch timing
        JPanel timingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        timingPanel.setBorder(BorderFactory.createTitledBorder("Dispatch Timing"));

        immediateRadio = new JRadioButton("Send Immediately", true);
        delayedRadio = new JRadioButton("Delay Dispatch by (minutes):");
        ButtonGroup timingGroup = new ButtonGroup();
        timingGroup.add(immediateRadio);
        timingGroup.add(delayedRadio);

        delayMinutesSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 60, 1));
        delayMinutesSpinner.setEnabled(false);

        delayedRadio.addActionListener(e -> delayMinutesSpinner.setEnabled(delayedRadio.isSelected()));
        immediateRadio.addActionListener(e -> delayMinutesSpinner.setEnabled(!immediateRadio.isSelected()));

        timingPanel.add(immediateRadio);
        timingPanel.add(delayedRadio);
        timingPanel.add(delayMinutesSpinner);

        // Status & Launch
        JPanel actionPanel = new JPanel(new BorderLayout(5, 5));
        statusBannerLabel = new JLabel(" ", SwingConstants.CENTER);
        statusBannerLabel.setFont(statusBannerLabel.getFont().deriveFont(Font.BOLD, 12f));

        launchPollBtn = new JButton("⚡ Launch Poll to Community");
        launchPollBtn.setFont(launchPollBtn.getFont().deriveFont(Font.BOLD, 14f));

        launchPollBtn.addActionListener(e -> {
            if (validateForm()) {
                System.out.println("all set we can start the poll or start timer");

                List<Question> questions = this.questionBlocks.stream()
                        .map(qb -> qb.createQuestionObject())
                        .toList();

                this.telegramCom.getSurvey().startSurvey(questions);

                this.rightSidePanel.showPanel(RightSidePanel.CARD_COUNTDOWN);
            }
        });

        actionPanel.add(statusBannerLabel, BorderLayout.NORTH);
        actionPanel.add(launchPollBtn, BorderLayout.CENTER);

        bottomPanel.add(timingPanel, BorderLayout.WEST);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        return bottomPanel;
    }

    private void updateModeState() {
        boolean isAi = aiRadio.isSelected();
        aiTopicField.setEnabled(isAi);
        generateAiBtn.setEnabled(isAi);
    }

    private boolean validateForm() {
        boolean isValid = true;

        int questionsCount = 0;

        for (QuestionBlock questionBlock : questionBlocks) {
            if (!questionBlock.validateFields()) {
                isValid = false;
            }
            questionsCount++;
        }

        if (!InputValidations.isInRange(questionsCount, CoreConstants.MIN_QUESTIONS, CoreConstants.MAX_QUESTIONS)) {
            Globals.alert("There should be " + CoreConstants.MIN_QUESTIONS + " to " + CoreConstants.MAX_QUESTIONS
                    + " questions");
            isValid = false;
        }

        if (this.telegramCom.getClientsCount() < CoreConstants.MIN_CLIENTS) {
            Globals.alert("Should be atlas " + CoreConstants.MIN_CLIENTS + " clients in the community");
            isValid = false;
        }

        return isValid;
    }
}
