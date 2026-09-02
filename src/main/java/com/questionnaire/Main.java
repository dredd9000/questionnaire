package com.questionnaire;

import com.questionnaire.UI.MainFrame;
import com.questionnaire.core.TelegramCom;
import com.questionnaire.core.utils.ConfigLoader;

public class Main {
    public static void main(String[] args) {
        ConfigLoader.loadConfig();

        TelegramCom telegramCom = new TelegramCom();

        new MainFrame(telegramCom);
    }
}