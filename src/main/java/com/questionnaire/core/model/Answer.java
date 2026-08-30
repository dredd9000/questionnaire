package com.questionnaire.core.model;

import lombok.Getter;

@Getter
public class Answer {
    private Client client;
    private Question question;
    private int answer;

    public Answer(Client client, Question question, int answer) {
        this.client = client;
        this.question = question;
        this.answer = answer;
    }
}
