package com.questionnaire.core.model;

import lombok.Data;

@Data
public class AnswerResult {
    private String answer;
    private int votes;
    private double percentage;

    public AnswerResult(String answer, int votes, int total) {
        this.answer = answer;
        this.setStats(votes, total);
    }

    public void setStats(int votes, int total) {
        this.votes = votes;
        this.percentage = (double) votes / total * 100;
    }
}
