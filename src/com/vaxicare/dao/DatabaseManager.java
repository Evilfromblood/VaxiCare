package com.vaxicare.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite database connectivity and schema lifecycle.
 * Demonstrates Unit IV: JDBC DriverManager, Connection, Statement, and resource cleanup in finally blocks.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:vaccination.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseManager] SQLite JDBC driver not found: " + e.getMessage());
        }
    }

    /**
     * Obtains a new database connection.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initializes tables on first run using DDL statements.
     */
    public static void initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();

            // 1. Beneficiaries table
            String createBeneficiaries = "CREATE TABLE IF NOT EXISTS beneficiaries (" +
                    "ben_id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "age INTEGER NOT NULL, " +
                    "id_number TEXT, " +
                    "phone TEXT, " +
                    "age_group TEXT);";
            stmt.executeUpdate(createBeneficiaries);

            // 2. Sessions table
            String createSessions = "CREATE TABLE IF NOT EXISTS sessions (" +
                    "session_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "session_date TEXT NOT NULL, " +
                    "vaccine_name TEXT NOT NULL, " +
                    "capacity INTEGER NOT NULL, " +
                    "booked INTEGER NOT NULL DEFAULT 0);";
            stmt.executeUpdate(createSessions);

            // 3. Doses table
            String createDoses = "CREATE TABLE IF NOT EXISTS doses (" +
                    "dose_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "ben_id TEXT NOT NULL, " +
                    "vaccine_name TEXT NOT NULL, " +
                    "dose_no INTEGER NOT NULL, " +
                    "dose_date TEXT NOT NULL, " +
                    "next_due TEXT, " +
                    "certificate_no TEXT);";
            stmt.executeUpdate(createDoses);

        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Initialization error: " + e.getMessage());
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Utility method to safely close Connection in finally blocks.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("[DatabaseManager] Failed to close connection: " + e.getMessage());
            }
        }
    }

    /**
     * Utility method to safely close Statement / PreparedStatement in finally blocks.
     */
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("[DatabaseManager] Failed to close statement: " + e.getMessage());
            }
        }
    }

    /**
     * Utility method to safely close ResultSet in finally blocks.
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("[DatabaseManager] Failed to close result set: " + e.getMessage());
            }
        }
    }
}
