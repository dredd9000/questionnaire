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

import com.questionnaire.Globals;
import com.questionnaire.Utils;
import com.questionnaire.core.CoreConstants;
import com.questionnaire.core.model.Question;
import com.questionnaire.core.model.SendToTlgrm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class MyEchoBot implements LongPollingUpdateConsumer {

    private final TelegramClient telegramClient;
    private final Consumer<Update> updateHandler;
    private final BiFunction<Long, String, String> answerHandler;
    private final BlockingQueue<SendToTlgrm> outcome;

    public MyEchoBot(String botToken, Consumer<Update> updateHandler, BiFunction<Long, String, String> answerHandler) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.updateHandler = updateHandler;
        this.answerHandler = answerHandler;
        this.outcome = new ArrayBlockingQueue<>(CoreConstants.MAX_QUEUE_VALUE);

        this.outcomeThread();
    }

    @Override
    public void consume(List<Update> updates) {

        for (Update update : updates) {

            if (update.hasMessage() && update.getMessage().hasText()) {
                // Instantly hand off the update to TelegramCom
                updateHandler.accept(update);

                // this.sendQuestion(update.getMessage().getChatId(),
                // new Question("question", List.of("1", "2", "3", "4")));
            } else if (update.hasCallbackQuery()) {
                this.handleButtonClick(update);
            }
        }
    }

    private void handleButtonClick(Update update) {
        try {
            String callbackId = update.getCallbackQuery().getId();
            String clickedOption = update.getCallbackQuery().getData(); // E.g., "OPT_1"
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            // Process the selected answer
            System.out.println("User in chat " + chatId + " clicked: " + clickedOption);

            long clientId;
            if (update.getMessage() == null) {
                clientId = update.getCallbackQuery().getFrom().getId();
            } else {
                clientId = update.getMessage().getFrom().getId();
            }
            String response = this.answerHandler.apply(clientId, clickedOption);

            // Stop the loading spinner on the user's button (and show optional alert)
            AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackId)
                    .text(response) // Pop-up banner text
                    .showAlert(false) // Set to true for a modal popup box
                    .build();

            // Send answer confirmation back via TelegramClient
            System.out.println("sending back");
            this.telegramClient.execute(answer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void outcomeThread() {
        new Thread(() -> {
            while (true) {
                try {
                    SendToTlgrm obj = this.outcome.take();

                    if (obj.getData() instanceof String) {
                        this.sendMessage(obj.getChatId(), (String) obj.getData());
                    }

                    if (obj.getData() instanceof Question) {
                        this.sendQuestion(obj.getChatId(), (Question) obj.getData());
                    }

                    Utils.sleep(CoreConstants.SEND_SLEEP_DELAY);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void addMessageToQueue(long chatId, String text) {
        this.outcome.add(new SendToTlgrm(chatId, text));
    }

    public void addQuestionToQueue(long chatId, Question question) {
        this.outcome.add(new SendToTlgrm(chatId, question));
    }

    private boolean sendMessage(long chatId, String text) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            telegramClient.execute(msg);
            return true;
        } catch (TelegramApiException e) {
            // Globals.generalMsgs.add(CoreConstants.SOMETHING_WENT_WRONG);
            Globals.toast.error(CoreConstants.SOMETHING_WENT_WRONG);
            e.printStackTrace();
            return false;
        }
    }

    private boolean sendQuestion(long chatId, Question question) {

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
            if (currentRowButtons.size() == 2
                    || i == question.getAnswers().size() - 1) {
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

            // Globals.generalMsgs.add(CoreConstants.SOMETHING_WENT_WRONG);
            Globals.toast.error(CoreConstants.SOMETHING_WENT_WRONG);

            return false;
        }
    }
}