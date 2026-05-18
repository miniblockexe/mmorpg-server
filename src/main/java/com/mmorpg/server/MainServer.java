package com.mmorpg.server;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import java.net.InetSocketAddress;

public class MainServer extends WebSocketServer {

    private static final int PORT = 9000;

    public static final GameWorld gameWorld = new GameWorld();

    public MainServer() {
        super(new InetSocketAddress(PORT));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("[+] New WebSocket connection: " + conn.getRemoteSocketAddress());

        ClientHandler handler = new ClientHandler(conn, gameWorld);

        conn.setAttachment(handler);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        ClientHandler handler = conn.getAttachment();
        if (handler == null) return;

        try {
            JSONObject packet = new JSONObject(message.trim());
            PacketProcessor.handlePacket(handler, packet);   // dispatcher chung
        } catch (Exception e) {
            System.err.println("[Error] Parsing packet: " + e.getMessage());
            System.err.println("[Raw] " + message);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        ClientHandler handler = conn.getAttachment();
        if (handler != null) {
            handler.onDisconnect();  
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("[WS Error] " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("=== Mini MMORPG WebSocket Server ===");
        System.out.println("Listening on port " + PORT + " | Ready!\n");
        setConnectionLostTimeout(30); 
    }


    public static void main(String[] args) {
        DatabaseManager.getInstance().initialize();
        System.out.println("[DB] Database connected!");

        System.out.println("[System] Khoi dong Web Server (Angular)...");
        SimpleHttpServer.start(4200); 

        System.out.println("[System] Khoi dong Game WebSocket Server...");
        MainServer server = new MainServer();
        server.start(); 
    }
}