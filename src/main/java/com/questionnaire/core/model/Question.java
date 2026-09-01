package com.questionnaire.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.questionnaire.core.CoreConstants;

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
            if (++i == CoreConstants.MAX_ANSWERS) {
                break;
            }
        }
    }

    public boolean isMyQuestionId(int questionId) {
        return this.questionId == questionId;
    }

    public boolean isMyAnswer(int answerIdx) {
        return this.answers.size() > answerIdx;
    }

    public boolean didIAlreadyAnswered(Client client) {
        return this.answeredBy.get(client) != null;
    }

    public String answer(int questionId, int answerIdx, Client client) {
        if (!this.isMyQuestionId(questionId)) {
            return CoreConstants.INVALID_QUESTION;
        }

        if (!this.isMyAnswer(answerIdx)) {
            return CoreConstants.INVALID_ANSWER;
        }

        if (this.didIAlreadyAnswered(client)) {
            return CoreConstants.YOU_ALREADY_ANSWERED_TO_THIS_QUESTION;
        }

        this.answeredBy.put(client, answerIdx);

        return CoreConstants.THANK_YOU;
    }
}
