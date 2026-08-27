package com.questionnaire.core;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.questionnaire.core.model.Client;

public class ClientManager {
    private Map<Long, Client> clients; // <userId, Client>

    public ClientManager() {
        this.clients = new ConcurrentHashMap<>();
    }

    public boolean put(Client client) {
        return (this.clients.putIfAbsent(client.getClientId(), client) == null);
    }

    public long getClientsCount() {
        return this.clients.size();
    }

    public Collection<Client> getClientsList() {
        return this.clients.values();
    }
}
