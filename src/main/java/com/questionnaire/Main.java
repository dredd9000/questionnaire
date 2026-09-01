package com.questionnaire;

import com.questionnaire.UI.MainFrame;
import com.questionnaire.core.TelegramCom;

public class Main {
    public static void main(String[] args) {
        TelegramCom telegramCom = new TelegramCom();

        new MainFrame(telegramCom);
    }
}