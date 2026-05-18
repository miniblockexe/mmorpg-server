package com.mmorpg.server;

import org.java_websocket.WebSocket;
import org.json.JSONObject;

public class ClientHandler {

    private final WebSocket conn;
    private final GameWorld gameWorld;

    private String  playerName = null;
    private int     playerId   = -1;
    private float   posX       = 0f;
    private float   posY       = 0f;
    private boolean isLoggedIn = false;

    public ClientHandler(WebSocket conn, GameWorld gameWorld) {
        this.conn      = conn;
        this.gameWorld = gameWorld;
    }

    public void sendPacket(JSONObject packet) {
        if (conn != null && conn.isOpen()) {
            conn.send(packet.toString());
        }
    }

    public void onDisconnect() {
        if (!isLoggedIn || playerName == null) return;
        PlayerDAO.savePosition(playerId, posX, posY);
        gameWorld.removePlayer(playerName);
        JSONObject leavePacket = new JSONObject();
        leavePacket.put("type",       "player_leave");
        leavePacket.put("playerName", playerName);
        gameWorld.broadcastToAll(leavePacket, this);
        System.out.println("[-] " + playerName + " left the game.");
    }

    public GameWorld getGameWorld()                { return gameWorld; }
    public String    getPlayerName()               { return playerName; }
    public void      setPlayerName(String n)       { this.playerName = n; }
    public int       getPlayerId()                 { return playerId; }
    public void      setPlayerId(int id)           { this.playerId = id; }
    public float     getPosX()                     { return posX; }
    public float     getPosY()                     { return posY; }
    public void      setPosition(float x, float y) { posX = x; posY = y; }
    public boolean   isLoggedIn()                  { return isLoggedIn; }
    public void      setLoggedIn(boolean b)        { this.isLoggedIn = b; }
}