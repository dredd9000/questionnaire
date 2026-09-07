package com.questionnaire.UI.helpers;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.formdev.flatlaf.FlatClientProperties;
import com.questionnaire.core.CoreConstants;
import com.questionnaire.core.model.Question;
import com.questionnaire.core.model.QuestionResult;

public class QuestionBlock extends JPanel {
    private final JLabel titleLabel;
    private final JTextField questionField;
    private final JPanel optionsPanel;
    private final List<OptionRow> optionRows;
    private final JButton addOptionBtn;
    private final JButton removeQuestionBtn;

    // Helper class to manage each option row and its components
    private static class OptionRow {
        JPanel panel;
        JLabel label;
        JTextField field;
        JButton removeBtn;

        OptionRow(JPanel panel, JLabel label, JTextField field, JButton removeBtn) {
            this.panel = panel;
            this.label = label;
            this.field = field;
            this.removeBtn = removeBtn;
        }
    }

    public QuestionBlock(int number, Runnable onDelete) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Question " + number));

        optionRows = new ArrayList<>();
        titleLabel = new JLabel("Question Wording:");

        questionField = new JTextField();
        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(titleLabel, BorderLayout.WEST);
        top.add(questionField, BorderLayout.CENTER);

        removeQuestionBtn = new JButton("Remove Question");
        removeQuestionBtn.addActionListener(e -> onDelete.run());
        top.add(removeQuestionBtn, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        add(optionsPanel, BorderLayout.CENTER);

        addOptionBtn = new JButton("+ Add Choice (Max " + CoreConstants.MAX_ANSWERS + ")");
        addOptionBtn.addActionListener(e -> addOptionField());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(addOptionBtn);
        add(bottom, BorderLayout.SOUTH);

        // Default: minimum options required
        for (int i = 0; i < CoreConstants.MIN_ANSWERS; i++) {
            addOptionField();
        }
    }

    public void setQuestionNumber(int number) {
        setBorder(BorderFactory.createTitledBorder("Question " + number));
    }

    public void setDeleteEnabled(boolean enabled) {
        removeQuestionBtn.setEnabled(enabled);
    }

    private void addOptionField() {
        if (optionRows.size() >= CoreConstants.MAX_ANSWERS)
            return;

        int optNum = optionRows.size() + 1;
        JPanel optRow = new JPanel(new BorderLayout(5, 5));
        JLabel label = new JLabel("Choice " + optNum + ": ");
        JTextField optField = new JTextField(20);

        JButton removeOptBtn = new JButton("X");
        removeOptBtn.setToolTipText("Remove choice");

        OptionRow rowObj = new OptionRow(optRow, label, optField, removeOptBtn);
        removeOptBtn.addActionListener(e -> removeOptionField(rowObj));

        optRow.add(label, BorderLayout.WEST);
        optRow.add(optField, BorderLayout.CENTER);
        optRow.add(removeOptBtn, BorderLayout.EAST);

        optionRows.add(rowObj);
        optionsPanel.add(optRow);

        updateOptionStates();

        revalidate();
        repaint();
    }

    private void removeOptionField(OptionRow row) {
        if (optionRows.size() <= CoreConstants.MIN_ANSWERS) {
            return;
        }

        optionRows.remove(row);
        optionsPanel.remove(row.panel);

        updateOptionStates();

        revalidate();
        repaint();
    }

    private void updateOptionStates() {
        boolean canRemove = optionRows.size() > CoreConstants.MIN_ANSWERS;

        for (int i = 0; i < optionRows.size(); i++) {
            OptionRow r = optionRows.get(i);
            r.label.setText("Choice " + (i + 1) + ": ");
            r.removeBtn.setEnabled(canRemove);
        }

        addOptionBtn.setEnabled(optionRows.size() < CoreConstants.MAX_ANSWERS);
    }

    public boolean validateFields() {
        boolean isValid = true;

        // 1. Validate Question Text Length
        if (!InputValidations.isTextValid(this.questionField.getText(),
                CoreConstants.MIN_CHARS_IN_QUESTION,
                CoreConstants.MAX_CHARS_IN_QUESTION)) {
            this.questionField.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            this.questionField.setToolTipText("Question length must be between "
                    + CoreConstants.MIN_CHARS_IN_QUESTION + " and "
                    + CoreConstants.MAX_CHARS_IN_QUESTION + " characters.");
            isValid = false;
        } else {
            this.questionField.putClientProperty(FlatClientProperties.OUTLINE, null);
            this.questionField.setToolTipText(null);
        }
        this.questionField.repaint();

        // 2. Validate Option Count Range
        int numberOfOptions = this.optionRows.size();
        if (!InputValidations.isInRange(numberOfOptions,
                CoreConstants.MIN_ANSWERS,
                CoreConstants.MAX_ANSWERS)) {
            isValid = false;
        }

        // 3. Validate Each Choice/Answer Field
        for (OptionRow row : this.optionRows) {
            JTextField jTextField = row.field;
            if (!InputValidations.isTextValid(jTextField.getText(),
                    CoreConstants.MIN_CHARS_IN_ANSWER,
                    CoreConstants.MAX_CHARS_IN_ANSWER)) {
                jTextField.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
                jTextField.setToolTipText("Choice length must be between "
                        + CoreConstants.MIN_CHARS_IN_ANSWER + " and "
                        + CoreConstants.MAX_CHARS_IN_ANSWER + " characters.");
                isValid = false;
            } else {
                jTextField.putClientProperty(FlatClientProperties.OUTLINE, null);
                jTextField.setToolTipText(null);
            }
            jTextField.repaint();
        }

        return isValid;
    }

    public Question createQuestionObject() {
        List<String> options = this.optionRows.stream()
                .map(r -> r.field.getText())
                .toList();

        return new Question(this.questionField.getText(), options);
    }

    public void putQuestionWithAnswers(QuestionResult questionResult) {
        if (questionResult == null) {
            return;
        }

        int delta = questionResult.getResults().size() - this.optionRows.size();

        if (delta > 0) {
            for (int i = 0; i < delta; i++) {
                this.addOptionField();
            }
        }

        this.questionField.setText(questionResult.getQuestion());

        for (int i = 0; i < questionResult.getResults().size(); i++) {
            this.optionRows.get(i).field.setText(questionResult.getResults().get(i).getAnswer());
        }
    }
}