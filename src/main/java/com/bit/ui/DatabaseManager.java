package com.bit.ui;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// 数据库部分由于技术原因百分之七十由AI辅助完成
public class DatabaseManager {
//    private static final String DB_URL = "jdbc:sqlite:tetris.db";
// 修改 DatabaseManager.java 中的 DB_URL
private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.home") + "/tetris.db";
    static {
        try {
            Class.forName("org.sqlite.JDBC");
            initDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 初始化
    private static void initDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS scores (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "score INTEGER NOT NULL," +
                    "time_seconds INTEGER NOT NULL," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    // 保存记录
    public static void saveScore(int score, long timeSeconds) {
        String sql = "INSERT INTO scores(score, time_seconds) VALUES(?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, score);
            pstmt.setLong(2, timeSeconds);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 清除记录
    public static void clearAllScores() {
        String sql = "DELETE FROM scores";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 获取记录
    public static List<String[]> getTopScores(int limit) {
        List<String[]> records = new ArrayList<>();
        String sql = "SELECT score, time_seconds FROM scores ORDER BY score DESC LIMIT ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int score = rs.getInt("score");
                long seconds = rs.getLong("time_seconds");
                records.add(new String[]{String.valueOf(score), String.valueOf(seconds)}); // 存储原始秒数
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
}