package com.questionnaire.core.model;

import lombok.Data;

@Data
public class Message {
    private long chatId;
    private String msg;

    public Message(long chatId, String msg) {
        this.chatId = chatId;
        this.msg = msg;
    }
}
