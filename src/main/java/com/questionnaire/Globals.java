package com.questionnaire;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.questionnaire.core.Constants;

public class Globals {
    public static final BlockingQueue<String> generalMsgs = new ArrayBlockingQueue<>(Constants.MAX_QUEUE_VALUE);
}
