package com.mmorpg.server;

import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.*;

public class PlayerDAO {

    public static class LoginResult {
        public boolean success;
        public String  message;
        public int     playerId;
        public String  playerName;
        public float   posX, posY;
        public int     level, hp, maxHp, gold;
    }

   public static LoginResult login(String username, String password) {
    LoginResult result = new LoginResult();
    String sql = "SELECT a.id as aid, p.id as pid, p.player_name, " +
                 "p.pos_x, p.pos_y, p.level, p.hp, p.max_hp, p.gold " +
                 "FROM accounts a " +
                 "JOIN players p ON p.account_id = a.id " +
                 "WHERE a.username = ? AND a.password = ? AND a.is_banned = 0";

    try {
        System.out.println("[DEBUG] Login attempt: username=" + username + " password=" + password);
        
        PreparedStatement stmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);

        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            System.out.println("[DEBUG] Login SUCCESS for: " + rs.getString("player_name"));
            result.success    = true;
            result.playerId   = rs.getInt("pid");
            result.playerName = rs.getString("player_name");
            result.posX       = rs.getFloat("pos_x");
            result.posY       = rs.getFloat("pos_y");
            result.level      = rs.getInt("level");
            result.hp         = rs.getInt("hp");
            result.maxHp      = rs.getInt("max_hp");
            result.gold       = rs.getInt("gold");
            updateLastLogin(rs.getInt("aid"));
        } else {
            System.out.println("[DEBUG] Login FAILED - no rows found");
            result.success = false;
            result.message = "Sai tên đăng nhập hoặc mật khẩu!";
        }

    } catch (SQLException e) {
        System.out.println("[DEBUG] SQL ERROR: " + e.getMessage());
        result.success = false;
        result.message = "Lỗi server database!";
    }

    return result;
}

    public static boolean register(String username, String password, String playerName) {
        String sqlAcc = "INSERT INTO accounts (username, password) VALUES (?, ?)";
        String sqlPly = "INSERT INTO players (account_id, player_name) VALUES (?, ?)";

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmtAcc = conn.prepareStatement(sqlAcc,
                    Statement.RETURN_GENERATED_KEYS)) {

                stmtAcc.setString(1, username);
                stmtAcc.setString(2, password);
                stmtAcc.executeUpdate();

                ResultSet keys = stmtAcc.getGeneratedKeys();
                if (keys.next()) {
                    int accountId = keys.getInt(1);

                    try (PreparedStatement stmtPly = conn.prepareStatement(sqlPly)) {
                        stmtPly.setInt(1, accountId);
                        stmtPly.setString(2, playerName);
                        stmtPly.executeUpdate();
                    }
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            System.err.println("[DB] Register error: " + e.getMessage());
            return false;  
        }
    }

    public static void savePosition(int playerId, float posX, float posY) {
        String sql = "UPDATE players SET pos_x=?, pos_y=? WHERE id=?";
        try (PreparedStatement stmt =
                DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setFloat(1, posX);
            stmt.setFloat(2, posY);
            stmt.setInt(3, playerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] SavePosition error: " + e.getMessage());
        }
    }

    public static JSONArray getInventory(int playerId) {
        JSONArray items = new JSONArray();
        String sql = "SELECT item_id, item_name, quantity, slot FROM inventory WHERE player_id=?";

        try (PreparedStatement stmt =
                DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject item = new JSONObject();
                item.put("itemId",   rs.getInt("item_id"));
                item.put("itemName", rs.getString("item_name"));
                item.put("quantity", rs.getInt("quantity"));
                item.put("slot",     rs.getInt("slot"));
                items.put(item);
            }
        } catch (SQLException e) {
            System.err.println("[DB] GetInventory error: " + e.getMessage());
        }

        return items;
    }

    public static void saveChat(String playerName, String message) {
        String sql = "INSERT INTO chat_logs (player_name, message) VALUES (?, ?)";
        try (PreparedStatement stmt =
                DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, playerName);
            stmt.setString(2, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] SaveChat error: " + e.getMessage());
        }
    }

    private static void updateLastLogin(int accountId) {
        String sql = "UPDATE accounts SET last_login=NOW() WHERE id=?";
        try (PreparedStatement stmt =
                DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.executeUpdate();
        } catch (SQLException e) { /* ignore */ }
    }
}