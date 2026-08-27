package com.questionnaire.core.utils;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.questionnaire.core.Constants;
import com.questionnaire.core.model.Message;

import lombok.Getter;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MyEchoBot implements LongPollingUpdateConsumer {

    private final TelegramClient telegramClient;
    @Getter
    private final BlockingQueue<Update> incomeQueue;
    private final BlockingQueue<Message> outcomeQueue;

    public MyEchoBot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.incomeQueue = new ArrayBlockingQueue<>(Constants.MAX_QUEUE_VALUE);
        this.outcomeQueue = new ArrayBlockingQueue<>(Constants.MAX_QUEUE_VALUE);
        this.sendMsgQueueThread();
    }

    @Override
    public void consume(List<Update> updates) {
        // Iterate through received update batches
        for (Update update : updates) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                try {
                    this.incomeQueue.put(update);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // String userText = update.getMessage().getText();
                // long chatId = update.getMessage().getChatId();

                // SendMessage message = SendMessage.builder()
                // .chatId(chatId)
                // .text("You said: " + userText)
                // .build();

                // Client client = new Client(update.getMessage().getFrom(), chatId);
                // System.out.println(client);

                // try {
                // telegramClient.execute(message);
                // } catch (Exception e) {
                // e.printStackTrace();
                // }
            }
        }
    }

    private boolean sendMessage(Message message) {
        SendMessage msg = SendMessage.builder()
                .chatId(message.getChatId())
                .text(message.getMsg())
                .build();
        try {
            this.telegramClient.execute(msg);
            return true;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendMsgQueueThread() {
        new Thread(() -> {
            while (true) {
                try {
                    Message msg = this.outcomeQueue.take();
                    this.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMessage(long chatId, String message) {
        this.outcomeQueue.add(new Message(chatId, message));
    }
}