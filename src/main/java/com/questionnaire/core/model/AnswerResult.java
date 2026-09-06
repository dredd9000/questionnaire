package com.questionnaire.core.model;

import lombok.Data;

@Data
public class AnswerResult {
    private String answer;
    private int votes;
    private double percentage;

    public AnswerResult(String answer) {
        this.answer = answer;
        this.setStats(0, 0);
    }

    public AnswerResult(String answer, int votes, int total) {
        this.answer = answer;
        this.setStats(votes, total);
    }

    public void setStats(int votes, int total) {
        if (total == 0) {
            this.percentage = 0.0;
            return;
        }

        this.votes = votes;
        this.percentage = (double) votes / total * 100;
    }
}
