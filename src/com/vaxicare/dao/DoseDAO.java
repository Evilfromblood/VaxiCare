package com.vaxicare.dao;

import com.vaxicare.model.DoseRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for DoseRecord entities.
 * Demonstrates Unit IV: JDBC CRUD operations and SQL parameter bindings.
 */
public class DoseDAO {

    public int insert(DoseRecord d) throws SQLException {
        String sql = "INSERT INTO doses (ben_id, vaccine_name, dose_no, dose_date, next_due, certificate_no) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, d.getBenId());
            ps.setString(2, d.getVaccineName());
            ps.setInt(3, d.getDoseNo());
            ps.setString(4, d.getDoseDate());
            ps.setString(5, d.getNextDue());
            ps.setString(6, d.getCertificateNo());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                d.setDoseId(generatedId);
                return generatedId;
            }
            return 0;
        } finally {
            DatabaseManager.closeResultSet(rs);
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public DoseRecord findById(int doseId) throws SQLException {
        String sql = "SELECT dose_id, ben_id, vaccine_name, dose_no, dose_date, next_due, certificate_no FROM doses WHERE dose_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, doseId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new DoseRecord(
                        rs.getInt("dose_id"),
                        rs.getString("ben_id"),
                        rs.getString("vaccine_name"),
                        rs.getInt("dose_no"),
                        rs.getString("dose_date"),
                        rs.getString("next_due"),
                        rs.getString("certificate_no")
                );
            }
            return null;
        } finally {
            DatabaseManager.closeResultSet(rs);
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public List<DoseRecord> findAll() throws SQLException {
        String sql = "SELECT dose_id, ben_id, vaccine_name, dose_no, dose_date, next_due, certificate_no FROM doses ORDER BY dose_id ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<DoseRecord> list = new ArrayList<>();
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new DoseRecord(
                        rs.getInt("dose_id"),
                        rs.getString("ben_id"),
                        rs.getString("vaccine_name"),
                        rs.getInt("dose_no"),
                        rs.getString("dose_date"),
                        rs.getString("next_due"),
                        rs.getString("certificate_no")
                ));
            }
            return list;
        } finally {
            DatabaseManager.closeResultSet(rs);
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public List<DoseRecord> findByBeneficiary(String benId) throws SQLException {
        String sql = "SELECT dose_id, ben_id, vaccine_name, dose_no, dose_date, next_due, certificate_no FROM doses WHERE ben_id = ? ORDER BY dose_no ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<DoseRecord> list = new ArrayList<>();
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, benId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new DoseRecord(
                        rs.getInt("dose_id"),
                        rs.getString("ben_id"),
                        rs.getString("vaccine_name"),
                        rs.getInt("dose_no"),
                        rs.getString("dose_date"),
                        rs.getString("next_due"),
                        rs.getString("certificate_no")
                ));
            }
            return list;
        } finally {
            DatabaseManager.closeResultSet(rs);
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public DoseRecord getLatestDose(String benId) throws SQLException {
        String sql = "SELECT dose_id, ben_id, vaccine_name, dose_no, dose_date, next_due, certificate_no FROM doses WHERE ben_id = ? ORDER BY dose_no DESC LIMIT 1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, benId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new DoseRecord(
                        rs.getInt("dose_id"),
                        rs.getString("ben_id"),
                        rs.getString("vaccine_name"),
                        rs.getInt("dose_no"),
                        rs.getString("dose_date"),
                        rs.getString("next_due"),
                        rs.getString("certificate_no")
                );
            }
            return null;
        } finally {
            DatabaseManager.closeResultSet(rs);
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public int getLatestDoseNumber(String benId, String vaccineName) throws SQLException {
        String sql = "SELECT MAX(dose_no) FROM doses WHERE ben_id = ? AND vaccine_name = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, benId);
            ps.setString(2, vaccineName);
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

    public boolean update(DoseRecord d) throws SQLException {
        String sql = "UPDATE doses SET ben_id = ?, vaccine_name = ?, dose_no = ?, dose_date = ?, next_due = ?, certificate_no = ? WHERE dose_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, d.getBenId());
            ps.setString(2, d.getVaccineName());
            ps.setInt(3, d.getDoseNo());
            ps.setString(4, d.getDoseDate());
            ps.setString(5, d.getNextDue());
            ps.setString(6, d.getCertificateNo());
            ps.setInt(7, d.getDoseId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } finally {
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public boolean delete(int doseId) throws SQLException {
        String sql = "DELETE FROM doses WHERE dose_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, doseId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } finally {
            DatabaseManager.closeStatement(ps);
            DatabaseManager.closeConnection(conn);
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM doses";
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
