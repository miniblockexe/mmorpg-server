package com.mmorpg.server;

import org.json.JSONArray;
import org.json.JSONObject;

public class PacketProcessor {

    public static void handlePacket(ClientHandler client, JSONObject packet) {
        String type = packet.optString("type", "");

        System.out.println("[Packet] From " +
            (client.getPlayerName() != null ? client.getPlayerName() : "unknown") +
            " | Type: " + type);

        switch (type) {
            case "login":
                handleLogin(client, packet);
                break;
            case "register":
                handleRegister(client, packet);
                break;
            case "move":
                if (client.isLoggedIn()) handleMove(client, packet);
                break;
            case "chat":
                if (client.isLoggedIn()) handleChat(client, packet);
                break;
            case "use_item":
                if (client.isLoggedIn()) handleUseItem(client, packet);
                break;
            case "ping":
                client.sendPacket(new JSONObject().put("type", "pong"));
                break;
            default:
                System.out.println("[!] Unknown packet type: " + type);
        }
    }

    public static void handleLogin(ClientHandler client, JSONObject packet) {
        String username = packet.getString("username");
        String password = packet.getString("password");

        PlayerDAO.LoginResult result = PlayerDAO.login(username, password);

        JSONObject response = new JSONObject();
        response.put("type", "login_result");

        if (result.success) {
            if (client.getGameWorld().isOnline(result.playerName)) {
                response.put("success", false);
                response.put("message", "Tài khoản đang online!");
                client.sendPacket(response);
                return;
            }

            client.setPlayerName(result.playerName);
            client.setPlayerId(result.playerId);
            client.setPosition(result.posX, result.posY);
            client.setLoggedIn(true);
            client.getGameWorld().addPlayer(client);

            response.put("success",    true);
            response.put("playerName", result.playerName);
            response.put("posX",       result.posX);
            response.put("posY",       result.posY);
            response.put("level",      result.level);
            response.put("hp",         result.hp);
            response.put("maxHp",      result.maxHp);
            response.put("gold",       result.gold);
            response.put("inventory",  PlayerDAO.getInventory(result.playerId));
            client.sendPacket(response);

            sendOnlinePlayersList(client);

            JSONObject joinPacket = new JSONObject();
            joinPacket.put("type",       "player_join");
            joinPacket.put("playerName", result.playerName);
            joinPacket.put("posX",       result.posX);
            joinPacket.put("posY",       result.posY);
            client.getGameWorld().broadcastToAll(joinPacket, client);

            System.out.println("[Login] " + result.playerName + " joined!");

        } else {
            response.put("success", false);
            response.put("message", result.message);
            client.sendPacket(response);
        }
    }

    public static void handleRegister(ClientHandler client, JSONObject packet) {
        String username   = packet.getString("username");
        String password   = packet.getString("password");
        String playerName = packet.optString("playerName", username);

        JSONObject response = new JSONObject();
        response.put("type", "register_result");

        boolean success = PlayerDAO.register(username, password, playerName);
        response.put("success", success);
        response.put("message", success ? "Đăng ký thành công!" : "Username đã tồn tại!");
        client.sendPacket(response);
    }

    public static void handleMove(ClientHandler client, JSONObject packet) {
        float newX = (float) packet.getDouble("posX");
        float newY = (float) packet.getDouble("posY");

        float maxSpeed = 10.0f;
        float dx = Math.abs(newX - client.getPosX());
        float dy = Math.abs(newY - client.getPosY());
        if (dx > maxSpeed || dy > maxSpeed) {
            JSONObject correct = new JSONObject();
            correct.put("type", "position_correct");
            correct.put("posX", client.getPosX());
            correct.put("posY", client.getPosY());
            client.sendPacket(correct);
            return;
        }

        client.setPosition(newX, newY);

        JSONObject movePacket = new JSONObject();
        movePacket.put("type",       "player_move");
        movePacket.put("playerName", client.getPlayerName());
        movePacket.put("posX",       newX);
        movePacket.put("posY",       newY);
        client.getGameWorld().broadcastToAll(movePacket, client);
    }

    public static void handleChat(ClientHandler client, JSONObject packet) {
        String message = packet.getString("message");
        if (message == null || message.trim().isEmpty() || message.length() > 200) return;

        PlayerDAO.saveChat(client.getPlayerName(), message);

        JSONObject chatPacket = new JSONObject();
        chatPacket.put("type",       "chat");
        chatPacket.put("playerName", client.getPlayerName());
        chatPacket.put("message",    message);
        client.getGameWorld().broadcastToAll(chatPacket, null);
    }

    public static void handleUseItem(ClientHandler client, JSONObject packet) {
        int itemId = packet.getInt("itemId");

        JSONObject response = new JSONObject();
        response.put("type",    "use_item_result");
        response.put("itemId",  itemId);
        response.put("success", true);
        response.put("message", "Đã dùng item!");
        client.sendPacket(response);
    }

    private static void sendOnlinePlayersList(ClientHandler newClient) {
        JSONArray players = new JSONArray();
        for (ClientHandler other : newClient.getGameWorld().getAllPlayers()) {
            if (other != newClient && other.isLoggedIn()) {
                JSONObject p = new JSONObject();
                p.put("playerName", other.getPlayerName());
                p.put("posX",       other.getPosX());
                p.put("posY",       other.getPosY());
                players.put(p);
            }
        }

        JSONObject packet = new JSONObject();
        packet.put("type",    "online_players");
        packet.put("players", players);
        newClient.sendPacket(packet);
    }
}