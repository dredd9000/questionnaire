package com.questionnaire.core;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class MyEchoBot implements LongPollingUpdateConsumer {

    private final TelegramClient telegramClient;

    public MyEchoBot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(List<Update> updates) {
        // Iterate through received update batches
        for (Update update : updates) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String userText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();

                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text("You said: " + userText)
                        .build();

                try {
                    telegramClient.execute(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}