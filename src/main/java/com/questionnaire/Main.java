package com.questionnaire;

import com.questionnaire.UI.MainFrame;
import com.questionnaire.core.ChatGptCom;
import com.questionnaire.core.TelegramCom;
import com.questionnaire.core.utils.ConfigLoader;

public class Main {
    public static void main(String[] args) {
        ConfigLoader.loadConfig();

        TelegramCom telegramCom = new TelegramCom();
        Globals.chatGptCom = new ChatGptCom();

        new MainFrame(telegramCom);
    }
}