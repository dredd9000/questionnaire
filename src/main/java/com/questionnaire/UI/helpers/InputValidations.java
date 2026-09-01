package com.questionnaire.UI.helpers;

public class InputValidations {
    public static boolean isTextValid(String txt, int min, int max) {
        if (txt == null) {
            return false;
        }

        txt = txt.trim();

        if (txt.isBlank()) {
            return false;
        }

        int len = txt.length();

        if (!isInRange(len, min, max)) {
            return false;
        }

        return true;
    }

    public static boolean isInRange(int size, int min, int max) {
        return min <= size && size <= max;
    }
}
