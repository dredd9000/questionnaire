package com.questionnaire.core.utils;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.function.Consumer;

public class MyEchoBot implements LongPollingUpdateConsumer {

    private final TelegramClient telegramClient;
    private final Consumer<Update> updateHandler;

    public MyEchoBot(String botToken, Consumer<Update> updateHandler) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.updateHandler = updateHandler;
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                // Instantly hand off the update to TelegramCom
                updateHandler.accept(update);
            }
        }
    }

    public boolean sendMessage(long chatId, String text) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            telegramClient.execute(msg);
            return true;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
        }
    }
}