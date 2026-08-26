package com.questionnaire.core;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.questionnaire.core.model.Client;
import com.questionnaire.core.utils.ConfigLoader;
import com.questionnaire.core.utils.MyEchoBot;

public class TelegramCom {
    private ClientManager clientManager;
    private BlockingQueue<Update> queue;
    private MyEchoBot myEchoBot;
    private String botToken;

    public TelegramCom() {
        this.clientManager = new ClientManager();
        this.queue = new ArrayBlockingQueue<>(100);
        this.botToken = ConfigLoader.getBotToken();
        this.myEchoBot = new MyEchoBot(this.botToken, this.queue);

        this.initCom();
    }

    private void initCom() {

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, this.myEchoBot);
            System.out.println("Bot started!");

            this.msgQueueThread();

            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void msgQueueThread() {
        new Thread(() -> {
            while (true) {
                handleMsgQueueThread();
            }
        }).start();
    }

    private synchronized void handleMsgQueueThread() {
        try {
            Update update = this.queue.take();

            String msg = update.getMessage().getText();

            if (this.isJoinMsg(msg)) {
                // the user wants to join
                this.handleNewUserJoining(update);
            }

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

            Constants.newUserLock.notify();
        }
        this.myEchoBot.sendMessage(chatId, response);
    }

    private boolean sendMessage(long chatId, String msg) {
        return this.myEchoBot.sendMessage(chatId, msg);
    }

    private void broadcastExcept(String msg, Client excludedClient) {
        for (Client client : this.clientManager.getClientsList()) {
            if (client.equals(excludedClient)) {
                continue;
            }

            this.sendMessage(client.getChatId(), msg);
        }
    }
}
