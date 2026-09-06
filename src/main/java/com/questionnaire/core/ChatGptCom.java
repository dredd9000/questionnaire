package com.questionnaire.core;

import java.util.ArrayList;
import java.util.List;

import com.questionnaire.Globals;
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

        sb.append(" " + requestQuestion);

        sb.append(". please struck the question with answers like so question" + CoreConstants.ANSWER_DELIMITER
                + "answer" + CoreConstants.ANSWER_DELIMITER + "answer..." + CoreConstants.QUESTION_DELIMITER);

        sb.append(" please create " + CoreConstants.MIN_QUESTIONS + " to " + CoreConstants.MAX_QUESTIONS);
        sb.append(" questions with " + CoreConstants.MIN_ANSWERS + " to " + CoreConstants.MAX_ANSWERS + " answers.");

        sb.append(" your response must be at that exact structure because i need to parse it on my end.");

        System.out.println("request:");
        System.out.println(sb.toString());

        String response = this.chatGpt.getResponse(sb.toString());
        // TODO: bring it back, for chatgpt apis calls
        // String response = "Which Bon Jovi song is your favorite?:Livin' on a
        // Prayer:It's My Life:You Give Love a Bad Name:Wanted Dead or Alive;Which Bon
        // Jovi album do you like most?:Slippery When Wet:New Jersey:Keep the
        // Faith:Crush;When did you first discover Bon Jovi?:1980s:1990s:2000s:2010s or
        // later";

        if (response == null) {
            return null;
        }

        System.out.println("response:");
        System.out.println(response);

        String[] questionsWithAnswers = response.split(CoreConstants.QUESTION_DELIMITER);

        if (CoreConstants.MIN_QUESTIONS > questionsWithAnswers.length
                || questionsWithAnswers.length > CoreConstants.MAX_QUESTIONS) {
            System.out.println("1: " + questionsWithAnswers.length);
            Globals.toast.error("chatGPT did not generate questions between " + CoreConstants.MIN_QUESTIONS + " and "
                    + CoreConstants.MAX_QUESTIONS + ", try to clarify or ask another question");
            return null;
        }

        List<QuestionResult> questions = new ArrayList<>();

        for (String questionWithAnswers : questionsWithAnswers) {
            String[] arr = questionWithAnswers.split(CoreConstants.ANSWER_DELIMITER);

            if (CoreConstants.MIN_ANSWERS + 1 > arr.length || arr.length > CoreConstants.MAX_ANSWERS + 1) {
                Globals.toast.error("chatGPT did not generate answers between " + CoreConstants.MIN_QUESTIONS + " and "
                        + CoreConstants.MAX_QUESTIONS + " for some questions, try to clarify or ask another question");
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
