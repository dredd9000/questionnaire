package com.questionnaire.core;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.questionnaire.core.model.Client;
import com.questionnaire.core.utils.ConfigLoader;
import com.questionnaire.core.utils.MyEchoBot;

import lombok.Getter;

public class TelegramCom {
    private final ClientManager clientManager;
    private final MyEchoBot myEchoBot;
    private final TelegramBotsLongPollingApplication botsApplication;

    @Getter
    private final BlockingQueue<Client> newClientQueue;

    public TelegramCom() {
        this.clientManager = new ClientManager();
        this.newClientQueue = new ArrayBlockingQueue<>(Constants.MAX_QUEUE_VALUE);

        String botToken = ConfigLoader.getBotToken();

        // Pass function handle directly into MyEchoBot
        this.myEchoBot = new MyEchoBot(botToken, this::handleUpdate);
        this.botsApplication = new TelegramBotsLongPollingApplication();

        this.startBot(botToken);
    }

    private void startBot(String botToken) {
        try {
            botsApplication.registerBot(botToken, this.myEchoBot);
            System.out.println("Bot started!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Handled synchronously when an update arrives
    private void handleUpdate(Update update) {
        String msg = update.getMessage().getText();

        if (isJoinMsg(msg)) {
            handleNewUserJoining(update);
        }
    }

    private boolean isJoinMsg(String msg) {
        return Constants.JOIN_KEYWORDS.contains(msg);
    }

    private void handleNewUserJoining(Update update) {
        long chatId = update.getMessage().getChatId();
        Client client = new Client(update.getMessage().getFrom(), chatId);
        String response = "you already in the group";

        if (this.clientManager.put(client)) {
            response = "Welcome " + client.getFullName();

            this.broadcastExcept(
                    client.getFullName() + " joined and now we have " + this.clientManager.getClientsCount(),
                    client);

            // Notify GUI queue
            this.newClientQueue.add(client);
        }

        this.myEchoBot.sendMessage(chatId, response);
    }

    private void broadcastExcept(String msg, Client excludedClient) {
        for (Client client : this.clientManager.getClientsList()) {
            if (client.equals(excludedClient)) {
                continue;
            }
            this.myEchoBot.sendMessage(client.getChatId(), msg);
        }
    }
}