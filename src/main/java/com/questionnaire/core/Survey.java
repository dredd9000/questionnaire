package com.questionnaire.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import com.questionnaire.core.enums.ESurveyStatus;
import com.questionnaire.core.model.Answer;
import com.questionnaire.core.model.Client;
import com.questionnaire.core.model.Question;

import lombok.Data;

@Data
public class Survey {
    private List<Question> questions;
    private Set<Client> group;
    private ESurveyStatus status;
    private ClientManager clientManager;
    private final BlockingQueue<Answer> newAnswer;

    public Survey(ClientManager clientManager, BlockingQueue<Answer> newAnswer) {
        this.status = ESurveyStatus.PRE;
        this.clientManager = clientManager;
        this.group = new HashSet<>();

        this.newAnswer = newAnswer;
    }

    public void startSurvey(List<Question> questions) {
        this.status = ESurveyStatus.STARTED;
        this.questions = questions;
        this.createGroup();
    }

    private void createGroup() {
        this.group.clear();

        for (Client client : this.clientManager.getClientsList()) {
            this.group.add(client);
        }
    }

    public void endSurvey() {
        this.status = ESurveyStatus.ENDED;
        this.group.clear();
    }

    public void preSurvey() {
        this.status = ESurveyStatus.PRE;
    }

    public String recordAnswer(long clientId, String res) {
        String[] arr = res.split(Constants.ANSWER_DELIMITER);

        if (arr.length != 2) {
            return Constants.INVALID_RESPONSE;
        }

        int questionIdx = Integer.parseInt(arr[0]);
        int answerIdx = Integer.parseInt(arr[1]);

        if (this.questions == null) {
            return Constants.SOMETHING_WENT_WRONG;
        }

        if (questionIdx >= this.questions.size()) {
            return Constants.INVALID_QUESTION;
        }

        Question question = this.questions.get(questionIdx);

        Client client = this.clientManager.getClientById(clientId);

        String answerResponse = question.answer(questionIdx, answerIdx, client);

        if (answerResponse.equals(Constants.THANK_YOU)) {
            this.newAnswer.add(new Answer(client, question, answerIdx));
        }

        return answerResponse;
    }
}
