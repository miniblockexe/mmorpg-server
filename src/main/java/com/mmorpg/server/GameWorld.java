package com.mmorpg.server;

import com.mmorpg.server.ClientHandler;
import org.json.JSONObject;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class GameWorld {

    private final ConcurrentHashMap<String, ClientHandler> onlinePlayers =
        new ConcurrentHashMap<>();

    public void addPlayer(ClientHandler client) {
        onlinePlayers.put(client.getPlayerName(), client);
        System.out.println("[World] Online: " + onlinePlayers.size() + " players");
    }

    public void removePlayer(String playerName) {
        onlinePlayers.remove(playerName);
        System.out.println("[World] Online: " + onlinePlayers.size() + " players");
    }

    public boolean isOnline(String playerName) {
        return onlinePlayers.containsKey(playerName);
    }

    public Collection<ClientHandler> getAllPlayers() {
        return onlinePlayers.values();
    }

    public void broadcastToAll(JSONObject packet, ClientHandler exclude) {
        for (ClientHandler client : onlinePlayers.values()) {
            if (client != exclude && client.isLoggedIn()) {
                client.sendPacket(packet);
            }
        }
    }
}