package com.questionnaire.UI;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JFrame;

import com.questionnaire.UI.helpers.ToastMessage;
import com.questionnaire.UI.helpers.ToastNotification;
import com.questionnaire.core.CoreConstants;

public class Toast {
    private final BlockingQueue<ToastMessage> msgsQueue;
    private ToastNotification toastNotification;

    public Toast(JFrame frame) {
        this.msgsQueue = new ArrayBlockingQueue<>(CoreConstants.MAX_QUEUE_VALUE);

        this.toastNotification = new ToastNotification(frame);

        this.popThread();
    }

    private void popThread() {
        new Thread(
                () -> {
                    while (true) {
                        try {
                            ToastMessage toastMessage = this.msgsQueue.take();
                            switch (toastMessage.getToastType()) {
                                case SUCCESS:
                                    this.toastNotification.showSuccess(toastMessage.getMessage());
                                    break;
                                case WARNING:
                                    this.toastNotification.showWarning(toastMessage.getMessage());
                                    break;
                                case INFO:
                                    this.toastNotification.showInfo(toastMessage.getMessage());
                                    break;
                                default:
                                    this.toastNotification.showError(toastMessage.getMessage());
                                    break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
    }
}
