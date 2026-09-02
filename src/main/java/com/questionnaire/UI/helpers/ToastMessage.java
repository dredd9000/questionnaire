package com.questionnaire.UI.helpers;

import com.questionnaire.UI.enums.ToastType;

import lombok.Getter;

@Getter
public class ToastMessage {
    private String message;
    private ToastType toastType;

    public ToastMessage(String message, ToastType toastType) {
        this.message = message;
        this.toastType = toastType;
    }
}
