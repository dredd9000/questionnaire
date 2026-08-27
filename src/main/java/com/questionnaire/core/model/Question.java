package com.questionnaire.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.questionnaire.core.Constants;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Question {
    private static AtomicInteger nextId = new AtomicInteger(0);

    private String question;
    private List<String> answers; // answers
    private Map<Client, Integer> answeredBy; // what client, what answer by index of answer
    private int questionId;

    public Question(String question, List<String> answers) {
        this.questionId = nextId.incrementAndGet();
        this.question = question;

        this.answeredBy = new HashMap<>();
        this.answers = new ArrayList<>();

        int i = 1;
        for (String answer : answers) {
            this.answers.add(answer);
            if (++i == Constants.MAX_ANSWERS) {
                break;
            }
        }
    }
}
