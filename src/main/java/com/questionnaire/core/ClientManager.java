package com.questionnaire.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.questionnaire.core.model.Client;

import lombok.Getter;

public class ClientManager {
    private Map<Long, Client> clients; // <userId, Client>
    @Getter
    private List<Client> clientsList;
    @Getter
    private AtomicInteger clientsCount;

    public ClientManager() {
        this.clients = new ConcurrentHashMap<>();
        this.clientsList = new CopyOnWriteArrayList<>();
        this.clientsCount = new AtomicInteger(0);
    }

    public boolean put(Client client) {
        if (this.clients.putIfAbsent(client.getClientId(), client) == null) {
            this.clientsCount.incrementAndGet();
            this.clientsList.add(client);
            return true;
        }
        return false;
    }
}
