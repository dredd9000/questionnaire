package com.questionnaire.core.model;

import org.telegram.telegrambots.meta.api.objects.User;

import lombok.Data;

@Data
public class Client {
    private String fullName;
    private String userName;
    private long joinAt;
    private long chatId;
    private long clientId;

    public Client(User user, long chatId) {
        this.fullName = user.getFirstName() + " " + user.getLanguageCode();
        this.userName = user.getUserName();
        this.joinAt = System.currentTimeMillis();
        this.clientId = user.getId();
        this.chatId = chatId;
    }
}
