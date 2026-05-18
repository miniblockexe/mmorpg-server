package com.mmorpg.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class SimpleHttpServer {

    public static void start(int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String path = exchange.getRequestURI().getPath();
                    
                    if (path.equals("/")) {
                        path = "/index.html";
                    }

                    File file = new File("www" + path);

                    if (!file.exists() || file.isDirectory()) {
                        file = new File("www/index.html");
                    }

                    if (file.exists()) {
                        byte[] content = Files.readAllBytes(file.toPath());
                        String contentType = getContentType(file.getName());
                        
                        exchange.getResponseHeaders().set("Content-Type", contentType);
                        exchange.sendResponseHeaders(200, content.length);
                        
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(content);
                        }
                    } else {
                        String error = "thieu file index.html!";
                        exchange.sendResponseHeaders(404, error.length());
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(error.getBytes());
                        }
                    }
                }
            });

            server.setExecutor(null);
            server.start();
            System.out.println("[Web Server] Client dang chay tai: http://localhost:" + port);
            
        } catch (IOException e) {
            System.err.println("Loi khoi dong web server: " + e.getMessage());
        }
    }

    private static String getContentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html; charset=UTF-8";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";
        if (fileName.endsWith(".svg")) return "image/svg+xml";
        if (fileName.endsWith(".wav")) return "audio/wav";
        if (fileName.endsWith(".mp3")) return "audio/mpeg";
        return "application/octet-stream";
    }
}