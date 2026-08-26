package com.questionnaire.core.utils;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class MyEchoBot implements LongPollingUpdateConsumer {

    private final TelegramClient telegramClient;
    private final BlockingQueue<Update> queue;

    public MyEchoBot(String botToken, BlockingQueue<Update> queue) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.queue = queue;
    }

    @Override
    public void consume(List<Update> updates) {
        // Iterate through received update batches
        for (Update update : updates) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                try {
                    this.queue.put(update);
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

    public boolean sendMessage(long chatId, String message) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();
        try {
            this.telegramClient.execute(msg);
            return true;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
        }
    }
}