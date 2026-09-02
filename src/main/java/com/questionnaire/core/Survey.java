package com.questionnaire.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import com.questionnaire.core.enums.ESurveyStatus;
import com.questionnaire.core.model.Answer;
import com.questionnaire.core.model.Client;
import com.questionnaire.core.model.Question;

import lombok.Data;

@Data
public class Survey {
    private Map<Integer, Question> questions;
    private ClientManager group; // current group
    private ESurveyStatus status;
    private ClientManager clientManager; // general community
    private final BlockingQueue<Answer> newAnswer;
    private final BiConsumer<Collection<Client>, Question> broadcastQuestionToGroup;

    public Survey(ClientManager clientManager,
            BlockingQueue<Answer> newAnswer,
            BiConsumer<Collection<Client>, Question> broadcastQuestionToGroup) {
        this.status = ESurveyStatus.PRE;

        this.group = new ClientManager();

        this.clientManager = clientManager;

        this.broadcastQuestionToGroup = broadcastQuestionToGroup;

        this.newAnswer = newAnswer;
    }

    private boolean isMyQuestionById(int questionId) {
        return this.questions.get(questionId) != null;
    }

    public void startSurvey(List<Question> questions) {
        this.status = ESurveyStatus.STARTED;
        this.questions = new ConcurrentHashMap<>();
        this.createGroup();

        for (Question question : questions) {
            this.questions.put(question.getQuestionId(), question);
            // System.out.println(question.toString());
            this.broadcastQuestionToGroup.accept(this.group.getClientsList(), question);
        }
    }

    private void createGroup() {
        this.group.copyFromClientManager(this.clientManager);
    }

    public void endSurvey() {
        this.status = ESurveyStatus.ENDED;
        this.group.clear();
    }

    public void preSurvey() {
        this.status = ESurveyStatus.PRE;
    }

    public boolean isSurveyStarted() {
        return this.status == ESurveyStatus.STARTED;
    }

    public String recordAnswer(long clientId, String res) {
        if (this.status != ESurveyStatus.STARTED) {
            return CoreConstants.POLL_DID_NOT_STARTED_YET;
        }

        if (!this.group.isInGroup(clientId)) {
            return CoreConstants.YOU_NOT_GROUP_MEMBER_OF_THIS_POLL;
        }

        String[] arr = res.split(CoreConstants.ANSWER_DELIMITER);

        if (arr.length != 2) {
            return CoreConstants.INVALID_RESPONSE;
        }

        int questionIdx = Integer.parseInt(arr[0]);
        int answerIdx = Integer.parseInt(arr[1]);

        if (this.questions == null || !this.isMyQuestionById(questionIdx)) {
            return CoreConstants.INVALID_QUESTION;
        }

        Question question = this.questions.get(questionIdx);

        Client client = this.clientManager.getClientById(clientId);

        String answerResponse = question.answer(questionIdx, answerIdx, client);

        if (answerResponse.equals(CoreConstants.THANK_YOU)) {
            this.newAnswer.add(new Answer(client, question, answerIdx));
        }

        return answerResponse;
    }
}
