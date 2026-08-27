package com.questionnaire.core.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class Question {
    private static final int INIT_ANSWER_COUNTER = 0;

    private String question;
    private Map<String, Integer> answers; // answer, counter
    private Set<Client> answeredBy;

    public Question(String question, List<String> answers) {
        this.question = question;

        this.answeredBy = new HashSet<>();

        int i = 1;
        for (String answer : answers) {
            this.answers.put(answer, INIT_ANSWER_COUNTER);
            if (++i == 5) {
                break;
            }
        }
    }
}
