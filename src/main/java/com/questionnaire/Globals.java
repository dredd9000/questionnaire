package com.questionnaire;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JFrame;

import com.questionnaire.UI.helpers.ToastNotification;
import com.questionnaire.core.CoreConstants;

public class Globals {
    private static final BlockingQueue<String> generalMsgs = new ArrayBlockingQueue<>(CoreConstants.MAX_QUEUE_VALUE);
    public static ToastNotification toastNotification;

    public static void initToast(JFrame frame) {
        toastNotification = new ToastNotification(frame);

        popThread();
    }

    public static void alert(String error) {
        generalMsgs.add(error);
    }

    public static void popThread() {
        new Thread(() -> {
            while (true) {
                try {
                    String msg = generalMsgs.take();
                    toastNotification.showError(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
