package com.questionnaire.core;

import java.util.ArrayList;
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
import com.questionnaire.core.model.QuestionResult;

import lombok.Data;

@Data
public class Survey {
    private Map<Integer, Question> questions;
    private ClientManager group; // current group
    private ESurveyStatus status;
    private ClientManager clientManager; // general community
    private final BlockingQueue<Answer> newAnswer;
    private final BiConsumer<Collection<Client>, Question> broadcastQuestionToGroup;
    private final BiConsumer<Collection<Client>, String> broadcastMessageToGroup;

    public Survey(ClientManager clientManager,
            BlockingQueue<Answer> newAnswer,
            BiConsumer<Collection<Client>, Question> broadcastQuestionToGroup,
            BiConsumer<Collection<Client>, String> broadcastMessageToGroup) {
        this.status = ESurveyStatus.PRE;

        this.group = new ClientManager();

        this.clientManager = clientManager;

        this.broadcastQuestionToGroup = broadcastQuestionToGroup;
        this.broadcastMessageToGroup = broadcastMessageToGroup;

        this.newAnswer = newAnswer;
    }

    private boolean isMyQuestionById(int questionId) {
        return this.questions.get(questionId) != null;
    }

    public void startSurvey(List<Question> questions) {
        this.status = ESurveyStatus.STARTED;
        this.questions = new ConcurrentHashMap<>();
        this.createGroup();

        this.broadcastMessage("Survey started");

        for (Question question : questions) {
            this.questions.put(question.getQuestionId(), question);

            this.broadcastQuestionToGroup.accept(this.group.getClientsList(), question);
        }
    }

    private void createGroup() {
        this.group.copyFromClientManager(this.clientManager);
    }

    public void endSurvey() {
        this.status = ESurveyStatus.ENDED;

        this.broadcastMessage("The survey is ended");

        this.group.clear();
    }

    private void broadcastMessage(String msg) {
        this.broadcastMessageToGroup.accept(this.group.getClientsList(), msg);
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

    public void notifyClientsNotCompletedSurvey() {
        List<Client> clientsToNotify = new ArrayList<>();

        synchronized (this) {
            int questionsCount = this.questions.size();

            for (Client client : this.group.getClientsList()) {
                int count = 0;
                for (Question question : this.questions.values()) {
                    if (question.didIAlreadyAnswered(client)) {
                        count++;
                    }
                }

                if (count != questionsCount) {
                    clientsToNotify.add(client);
                }
            }
        }

        this.broadcastMessageToGroup.accept(clientsToNotify, "Please answer to all questions");
    }

    public synchronized List<QuestionResult> getResults() {
        List<QuestionResult> results = new ArrayList<>();

        Collection<Client> participants = this.group.getClientsList();

        for (Question question : this.questions.values()) {
            results.add(question.getResults(participants));
        }

        return results;
    }
}
