package com.questionnaire.core;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.questionnaire.Utils;
import com.questionnaire.core.model.Answer;
import com.questionnaire.core.model.Client;
import com.questionnaire.core.model.Question;
import com.questionnaire.core.utils.ConfigLoader;
import com.questionnaire.core.utils.MyEchoBot;

import lombok.Getter;

public class TelegramCom {
    private final ClientManager clientManager;
    private final MyEchoBot myEchoBot;
    private final TelegramBotsLongPollingApplication botsApplication;
    private final Survey survey;

    @Getter
    private final BlockingQueue<Client> newClientQueue;
    private final BlockingQueue<Answer> newAnswer;

    public TelegramCom() {
        this.clientManager = new ClientManager();
        this.newClientQueue = new ArrayBlockingQueue<>(Constants.MAX_QUEUE_VALUE);
        this.newAnswer = new ArrayBlockingQueue<>(Constants.MAX_QUEUE_VALUE);

        String botToken = ConfigLoader.getBotToken();

        // Pass function handle directly into MyEchoBot
        this.botsApplication = new TelegramBotsLongPollingApplication();

        this.survey = new Survey(this.clientManager, this.newAnswer);

        this.myEchoBot = new MyEchoBot(botToken, this::handleUpdate, this.survey::recordAnswer);

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

        this.myEchoBot.addMessageToQueue(chatId, response);
    }

    private void broadcastQuestionToGroup(Collection<Client> group, Question question) {
        this.broadcastExceptToGroup(group, question, null);
    }

    private void broadcast(String msg) {
        this.broadcastExceptToGroup(this.clientManager.getClientsList(), msg, null);
    }

    private void broadcastExcept(String msg, Client excludedClient) {
        this.broadcastExceptToGroup(this.clientManager.getClientsList(), msg, excludedClient);
    }

    private void broadcastExceptToGroup(Collection<Client> to, Object msg, Client excludedClient) {
        if (!(msg instanceof String || msg instanceof Question)) {
            System.out.println("Invalid msg, msg must be of type String or Question");
            return;
        }

        for (Client client : to) {
            if (excludedClient != null && client.equals(excludedClient)) {
                continue;
            }

            if (msg instanceof String) {
                this.myEchoBot.addMessageToQueue(client.getChatId(), (String) msg);
            } else if (msg instanceof Question) {
                this.myEchoBot.addQueestionToQueue(client.getChatId(), (Question) msg);
            }

            Utils.sleep(Constants.SEND_SLEEP_DELAY);
        }
    }
}