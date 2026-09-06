package com.questionnaire.core;

import java.util.ArrayList;
import java.util.List;

import com.questionnaire.core.model.AnswerResult;
import com.questionnaire.core.model.QuestionResult;
import com.questionnaire.core.utils.ChatGpt;

public class ChatGptCom {
    private ChatGpt chatGpt;

    public ChatGptCom() {
        this.chatGpt = new ChatGpt();
    }

    public List<QuestionResult> getQuestions(String requestQuestion) {
        requestQuestion = requestQuestion.trim();

        if (requestQuestion == null || requestQuestion.isBlank()) {
            return null;
        }

        StringBuffer sb = new StringBuffer();

        sb.append("i want to create poll with few questions about the next topic:");

        sb.append(requestQuestion);

        sb.append(". please struck the question with answers like so question" + CoreConstants.ANSWER_DELIMITER
                + "answer" + CoreConstants.ANSWER_DELIMITER + "answer..." + CoreConstants.QUESTION_DELIMITER);

        sb.append(" please create " + CoreConstants.MIN_QUESTIONS + " to " + CoreConstants.MAX_QUESTIONS);
        sb.append(" questions with " + CoreConstants.MIN_ANSWERS + " to " + CoreConstants.MAX_ANSWERS + " answers.");

        sb.append(" your response must be at that exact structure because i need to parse it on my end.");

        System.out.println("request:");
        System.out.println(sb.toString());

        // String response = this.chatGpt.getResponse(sb.toString());
        // TODO: bring it back, for chatgpt apis calls
        String response = "What is Bon Jovi's most famous song?:Livin' on a Prayer:You Give Love a Bad Name:It's My Life;Which decade did Bon Jovi form?:1970s:1980s:1990s:2000s;Who is the lead singer of Bon Jovi?:Jon Bon Jovi:Richie Sambora:David Bryan:Phil X";

        if (response == null) {
            return null;
        }

        System.out.println("response:");
        System.out.println(response);

        String[] questionsWithAnswers = response.split(CoreConstants.QUESTION_DELIMITER);

        if (CoreConstants.MIN_QUESTIONS > questionsWithAnswers.length
                || questionsWithAnswers.length > CoreConstants.MAX_QUESTIONS) {
            System.out.println("1: " + questionsWithAnswers.length);
            return null;
        }

        List<QuestionResult> questions = new ArrayList<>();

        for (String questionWithAnswers : questionsWithAnswers) {
            String[] arr = questionWithAnswers.split(CoreConstants.ANSWER_DELIMITER);

            if (CoreConstants.MIN_ANSWERS + 1 > arr.length || arr.length > CoreConstants.MAX_ANSWERS + 1) {
                System.out.println("2");
                return null;
            }

            QuestionResult questionResult = new QuestionResult(arr[0]);

            for (int i = 1; i < arr.length; i++) {
                questionResult.addResult(new AnswerResult(arr[i]));
            }

            questions.add(questionResult);
        }

        return questions;
    }
}
