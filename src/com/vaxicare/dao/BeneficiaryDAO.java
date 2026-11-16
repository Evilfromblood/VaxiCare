package com.vaxicare.dao;

import com.vaxicare.model.Beneficiary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Beneficiary entities.
 * Demonstrates Unit IV: JDBC PreparedStatement for CRUD operations with explicit finally cleanup.
 */
public class BeneficiaryDAO {

    public boolean insert(Beneficiary b) throws SQLException {
        String sql = "INSERT INTO beneficiaries (ben_id, name, age, id_number, phone, age_group) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, b.getBenId());
            ps.setString(2, b.getName());
            ps.setInt(3, b.getAge());
            ps.setString(4, b.getIdNumber());
            ps.setString(5, b.getPhone());
            ps.setString(6, b.getAgeGroup());
            int rows = ps.executeUpdate();
            return rows > 0;
        } finally {
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public Beneficiary findById(String benId) throws SQLException {
        String sql = "SELECT ben_id, name, age, id_number, phone, age_group FROM beneficiaries WHERE ben_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, benId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new Beneficiary(
                        rs.getString("ben_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("id_number"),
                        rs.getString("phone"),
                        rs.getString("age_group")
                );
            }
            return null;
        } finally {
            DatabaseManager.closeResultSet(rs);
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public List<Beneficiary> findAll() throws SQLException {
        String sql = "SELECT ben_id, name, age, id_number, phone, age_group FROM beneficiaries ORDER BY ben_id ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Beneficiary> list = new ArrayList<>();
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Beneficiary(
                        rs.getString("ben_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("id_number"),
                        rs.getString("phone"),
                        rs.getString("age_group")
                ));
            }
            return list;
        } finally {
            DatabaseManager.closeResultSet(rs);
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public boolean update(Beneficiary b) throws SQLException {
        String sql = "UPDATE beneficiaries SET name = ?, age = ?, id_number = ?, phone = ?, age_group = ? WHERE ben_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, b.getName());
            ps.setInt(2, b.getAge());
            ps.setString(3, b.getIdNumber());
            ps.setString(4, b.getPhone());
            ps.setString(5, b.getAgeGroup());
            ps.setString(6, b.getBenId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } finally {
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public boolean delete(String benId) throws SQLException {
        String sql = "DELETE FROM beneficiaries WHERE ben_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, benId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } finally {
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM beneficiaries";
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
