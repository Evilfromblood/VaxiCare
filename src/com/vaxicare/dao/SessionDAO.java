package com.vaxicare.dao;

import com.vaxicare.model.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Vaccination Session entities.
 * Demonstrates Unit IV: JDBC CRUD operations and generated keys retrieval.
 */
public class SessionDAO {

    public int insert(Session s) throws SQLException {
        String sql = "INSERT INTO sessions (session_date, vaccine_name, capacity, booked) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, s.getSessionDate());
            ps.setString(2, s.getVaccineName());
            ps.setInt(3, s.getCapacity());
            ps.setInt(4, s.getBooked());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                s.setSessionId(generatedId);
                return generatedId;
            }
            return 0;
        } finally {
            DatabaseManager.closeResultSet(rs);
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public Session findById(int sessionId) throws SQLException {
        String sql = "SELECT session_id, session_date, vaccine_name, capacity, booked FROM sessions WHERE session_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, sessionId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new Session(
                        rs.getInt("session_id"),
                        rs.getString("session_date"),
                        rs.getString("vaccine_name"),
                        rs.getInt("capacity"),
                        rs.getInt("booked")
                );
            }
            return null;
        } finally {
            DatabaseManager.closeResultSet(rs);
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public List<Session> findAll() throws SQLException {
        String sql = "SELECT session_id, session_date, vaccine_name, capacity, booked FROM sessions ORDER BY session_id ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Session> list = new ArrayList<>();
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Session(
                        rs.getInt("session_id"),
                        rs.getString("session_date"),
                        rs.getString("vaccine_name"),
                        rs.getInt("capacity"),
                        rs.getInt("booked")
                ));
            }
            return list;
        } finally {
            DatabaseManager.closeResultSet(rs);
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public boolean update(Session s) throws SQLException {
        String sql = "UPDATE sessions SET session_date = ?, vaccine_name = ?, capacity = ?, booked = ? WHERE session_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, s.getSessionDate());
            ps.setString(2, s.getVaccineName());
            ps.setInt(3, s.getCapacity());
            ps.setInt(4, s.getBooked());
            ps.setInt(5, s.getSessionId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } finally {
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public boolean incrementBooked(int sessionId) throws SQLException {
        String sql = "UPDATE sessions SET booked = booked + 1 WHERE session_id = ? AND booked < capacity";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, sessionId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } finally {
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public boolean delete(int sessionId) throws SQLException {
        String sql = "DELETE FROM sessions WHERE session_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, sessionId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } finally {
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM sessions";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            DatabaseManager.closeResultSet(rs);
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }
}
