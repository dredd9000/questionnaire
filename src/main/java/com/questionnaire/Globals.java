package com.questionnaire;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.questionnaire.core.CoreConstants;

public class Globals {
    public static final BlockingQueue<String> generalMsgs = new ArrayBlockingQueue<>(CoreConstants.MAX_QUEUE_VALUE);
}
