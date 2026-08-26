package com.questionnaire;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import com.questionnaire.core.MyEchoBot;
import com.questionnaire.core.utils.ConfigLoader;

public class Main {
    public static void main(String[] args) {
        String botToken = ConfigLoader.getBotToken();

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new MyEchoBot(botToken));
            System.out.println("Bot started!");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}