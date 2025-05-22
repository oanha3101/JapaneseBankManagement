// src/main/java/com/quanlynganhangdethi/dao/DapAnDAOImpl.java
package com.quanlynganhangdethi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.quanlynganhangdethi.config.DatabaseConnection;
import com.quanlynganhangdethi.models.DapAn;

public class DapAnDAOImpl implements DapAnDAO {

	@Override
	public DapAn create(DapAn dapAn) {
		String sql = "INSERT INTO DAPAN (id_cauhoi, noidung, ladapandung) VALUES (?, ?, ?)";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet generatedKeys = null;
		try {
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			conn = dbInstance.getConnection();
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, dapAn.getIdCauHoi());
			ps.setString(2, dapAn.getNoiDung());
			ps.setBoolean(3, dapAn.isLaDapAnDung());

			if (ps.executeUpdate() > 0) {
				generatedKeys = ps.getGeneratedKeys();
				if (generatedKeys.next()) {
					dapAn.setId(generatedKeys.getInt(1));
					return dapAn;
				}
			}
		} catch (SQLException e) {
			System.err.println("Error creating DapAn: " + e.getMessage());
		} finally {
			DatabaseConnection.closeResources(ps, generatedKeys);
			// Không đóng conn ở đây
		}
		return null;
	}

	@Override
	public DapAn findById(Integer id) {
		String sql = "SELECT * FROM DAPAN WHERE id = ?";
		// ... (Tự triển khai tương tự ChuDeDAOImpl) ...
		return null;
	}

	@Override
	public List<DapAn> findAll() {
		// Thường ít khi dùng findAll cho Đáp Án, mà sẽ tìm theo id_cauhoi
		String sql = "SELECT * FROM DAPAN";
		// ... (Tự triển khai tương tự ChuDeDAOImpl) ...
		return new ArrayList<>();
	}

	@Override
	public boolean update(DapAn dapAn) {
		String sql = "UPDATE DAPAN SET id_cauhoi = ?, noidung = ?, ladapandung = ? WHERE id = ?";
		// ... (Tự triển khai tương tự ChuDeDAOImpl) ...
		return false;
	}

	@Override
	public boolean delete(Integer id) {
		String sql = "DELETE FROM DAPAN WHERE id = ?";
		// ... (Tự triển khai tương tự ChuDeDAOImpl) ...
		return false;
	}

	@Override
	public List<DapAn> findByCauHoiId(int idCauHoi) {
		List<DapAn> dapAnList = new ArrayList<>();
		String sql = "SELECT * FROM DAPAN WHERE id_cauhoi = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			conn = dbInstance.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, idCauHoi);
			rs = ps.executeQuery();
			while (rs.next()) {
				dapAnList.add(new DapAn(rs.getInt("id"), rs.getInt("id_cauhoi"), rs.getString("noidung"),
						rs.getBoolean("ladapandung")));
			}
		} catch (SQLException e) {
			System.err.println("Error finding DapAn by CauHoi ID: " + e.getMessage());
		} finally {
			DatabaseConnection.closeResources(ps, rs);
		}
		return dapAnList;
	}

	@Override
	public boolean deleteByCauHoiId(int idCauHoi) {
		String sql = "DELETE FROM DAPAN WHERE id_cauhoi = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			conn = dbInstance.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, idCauHoi);
			return ps.executeUpdate() > 0; // Trả về true nếu có hàng bị xóa
		} catch (SQLException e) {
			System.err.println("Error deleting DapAn by CauHoi ID: " + e.getMessage());
		} finally {
			DatabaseConnection.closeResources(ps);
		}
		return false;
	}
}