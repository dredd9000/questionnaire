package com.questionnaire.core.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class QuestionResult {
    private String question;
    private List<AnswerResult> results;

    public QuestionResult(String question) {
        this.question = question;
        this.results = new ArrayList<>();
    }

    public void addResult(AnswerResult answerResult) {
        this.results.add(answerResult);
    }
}
