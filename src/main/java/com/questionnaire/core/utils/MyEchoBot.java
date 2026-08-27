package com.questionnaire.core.utils;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.questionnaire.core.model.Question;

import java.util.ArrayList;
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

                this.sendQuestion(update.getMessage().getChatId(),
                        new Question("question", List.of("1", "2", "3", "4")));
            } else if (update.hasCallbackQuery()) {
                this.handleButtonClick(update);
            }
        }
    }

    private void handleButtonClick(Update update) {
        String callbackId = update.getCallbackQuery().getId();
        String clickedOption = update.getCallbackQuery().getData(); // E.g., "OPT_1"
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        // Process the selected answer
        System.out.println("User in chat " + chatId + " clicked: " + clickedOption);

        // Stop the loading spinner on the user's button (and show optional alert)
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackId)
                .text("Option recorded!") // Pop-up banner text
                .showAlert(false) // Set to true for a modal popup box
                .build();

        try {
            // Send answer confirmation back via TelegramClient
            this.telegramClient.execute(answer);
        } catch (Exception e) {
            e.printStackTrace();
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

    public boolean sendQuestion(long chatId, Question question) {

        if (question == null || question.getAnswers() == null ||
                question.getAnswers().isEmpty()) {
            System.err.println("Cannot send question: missing data or empty answer list.");
            return false;
        }

        List<InlineKeyboardRow> rows = new ArrayList<>();
        List<InlineKeyboardButton> currentRowButtons = new ArrayList<>();

        for (int i = 0; i < question.getAnswers().size(); i++) {
            InlineKeyboardButton btn = InlineKeyboardButton.builder()
                    .text(question.getAnswers().get(i))

                    .callbackData(question.getQuestionId() + ":" + i)
                    .build();

            currentRowButtons.add(btn);

            // Group 2 buttons per row, or flush the last remaining button
            if (currentRowButtons.size() == 2 || i == question.getAnswers().size() -
                    1) {
                rows.add(new InlineKeyboardRow(currentRowButtons));
                currentRowButtons = new ArrayList<>(); // Reset for next row
            }
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(rows);

        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(question.getQuestion())
                .replyMarkup(markup)
                .build();

        try {
            this.telegramClient.execute(msg);
            return true;
        } catch (TelegramApiException e) {
            System.err.println("Failed to send question to chat " + chatId + ": " +
                    e.getMessage());
            return false;
        }
    }
}