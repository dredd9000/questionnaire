package com.questionnaire.core.model;

import lombok.Getter;

@Getter
public class SendToTlgrm {
    private long chatId;
    private Object data;

    public SendToTlgrm(long chatId, Object data) {
        this.chatId = chatId;
        this.data = data;
    }
}
