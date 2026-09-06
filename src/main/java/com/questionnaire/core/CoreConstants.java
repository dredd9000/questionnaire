package com.questionnaire.core;

import java.util.List;

public class CoreConstants {
    public static final List<String> JOIN_KEYWORDS = List.of("/start", "hi", "היי");

    public static final int MAX_QUEUE_VALUE = 100;

    public static final int SEND_SLEEP_DELAY = 35;

    public static final int MIN_ANSWERS = 2;
    public static final int MAX_ANSWERS = 4;
    public static final int MIN_QUESTIONS = 1;
    public static final int MAX_QUESTIONS = 3;

    public static final int MIN_CLIENTS = 1; // ! TODO: bring back to 3

    public static final String QUESTION_DELIMITER = ";";
    public static final String ANSWER_DELIMITER = ":";

    /*  */
    public static final String SOMETHING_WENT_WRONG = "something went wrong";
    public static final String INVALID_RESPONSE = "Invalid response";
    public static final String INVALID_QUESTION = "Invalid question";
    public static final String INVALID_ANSWER = "Invalid answer";
    public static final String YOU_ALREADY_ANSWERED_TO_THIS_QUESTION = "You already answered to this question";
    public static final String THANK_YOU = "Thank you";
    public static final String POLL_DID_NOT_STARTED_YET = "Poll did not started yet";
    public static final String YOU_NOT_GROUP_MEMBER_OF_THIS_POLL = "You not group member of this poll";

    /* questions limitations */
    public static final int MIN_CHARS_IN_QUESTION = 2;
    public static final int MAX_CHARS_IN_QUESTION = 500;

    /* answers limitations */
    public static final int MIN_CHARS_IN_ANSWER = 1;
    public static final int MAX_CHARS_IN_ANSWER = 50;

    /* survey timers limits */
    public static final int MAX_SURVEY_TIME_SEC = 1 * 60; // TODO: bring back to 5 * 60
    public static final int NOTIFY_AFTER_SEC = 30; // TODO: bring back to 3 * 60
}
