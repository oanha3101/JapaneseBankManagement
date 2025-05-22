// src/main/java/com/quanlynganhangdethi/dao/ChuDeDAOImpl.java
package com.quanlynganhangdethi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.quanlynganhangdethi.config.DatabaseConnection;
import com.quanlynganhangdethi.models.ChuDe;

public class ChuDeDAOImpl implements ChuDeDAO {

	@Override
	public ChuDe create(ChuDe chuDe) {
		String sql = "INSERT INTO CHUDE (tenchude, mota) VALUES (?, ?)";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet generatedKeys = null;
		try {
			// SỬA Ở ĐÂY:
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			if (dbInstance == null) {
				System.err.println("Không thể lấy instance của DatabaseConnection trong create ChuDe.");
				return null;
			}
			conn = dbInstance.getConnection();
			if (conn == null) {
				System.err.println("Không thể lấy Connection từ DatabaseConnection instance trong create ChuDe.");
				return null;
			}
			// KẾT THÚC SỬA

			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, chuDe.getTenChuDe());
			ps.setString(2, chuDe.getMoTa());

			int affectedRows = ps.executeUpdate();

			if (affectedRows > 0) {
				generatedKeys = ps.getGeneratedKeys();
				if (generatedKeys.next()) {
					chuDe.setId(generatedKeys.getInt(1));
					return chuDe;
				}
			}
		} catch (SQLException e) {
			System.err.println("Error creating ChuDe: " + e.getMessage());
			// e.printStackTrace();
		} finally {
			// Sử dụng phương thức closeResources mới để chỉ đóng ps và rs
			DatabaseConnection.closeResources(ps, generatedKeys);
			// KHÔNG đóng 'conn' ở đây vì nó là connection của Singleton
			// DatabaseConnection.close(conn, ps, generatedKeys); // Dòng này không nên dùng
			// nữa
		}
		return null;
	}

	@Override
	public ChuDe findById(Integer id) {
		String sql = "SELECT id, tenchude, mota FROM CHUDE WHERE id = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// SỬA Ở ĐÂY:
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			if (dbInstance == null) {
				System.err.println("Không thể lấy instance của DatabaseConnection trong findById ChuDe.");
				return null;
			}
			conn = dbInstance.getConnection();
			if (conn == null) {
				System.err.println("Không thể lấy Connection từ DatabaseConnection instance trong findById ChuDe.");
				return null;
			}
			// KẾT THÚC SỬA

			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				return new ChuDe(rs.getInt("id"), rs.getString("tenchude"), rs.getString("mota"));
			}
		} catch (SQLException e) {
			System.err.println("Error finding ChuDe by ID: " + e.getMessage());
		} finally {
			DatabaseConnection.closeResources(ps, rs);
		}
		return null;
	}

	@Override
	public List<ChuDe> findAll() throws SQLException {
		List<ChuDe> chuDeList = new ArrayList<>();
		String sql = "SELECT id, tenchude, mota FROM CHUDE";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// SỬA Ở ĐÂY:
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			if (dbInstance == null) {
				System.err.println("Không thể lấy instance của DatabaseConnection trong findAll ChuDe.");
				return chuDeList; // Trả về list rỗng
			}
			conn = dbInstance.getConnection();
			if (conn == null) {
				System.err.println("Không thể lấy Connection từ DatabaseConnection instance trong findAll ChuDe.");
				return chuDeList; // Trả về list rỗng
			}
			// KẾT THÚC SỬA

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				chuDeList.add(new ChuDe(rs.getInt("id"), rs.getString("tenchude"), rs.getString("mota")));
			}
		} catch (SQLException e) {
			System.err.println("Error finding all ChuDe: " + e.getMessage());
		} finally {
			DatabaseConnection.closeResources(ps, rs);
		}
		return chuDeList;
	}

	@Override
	public boolean update(ChuDe chuDe) {
		String sql = "UPDATE CHUDE SET tenchude = ?, mota = ? WHERE id = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			// SỬA Ở ĐÂY:
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			if (dbInstance == null) {
				System.err.println("Không thể lấy instance của DatabaseConnection trong update ChuDe.");
				return false;
			}
			conn = dbInstance.getConnection();
			if (conn == null) {
				System.err.println("Không thể lấy Connection từ DatabaseConnection instance trong update ChuDe.");
				return false;
			}
			// KẾT THÚC SỬA

			ps = conn.prepareStatement(sql);
			ps.setString(1, chuDe.getTenChuDe());
			ps.setString(2, chuDe.getMoTa());
			ps.setInt(3, chuDe.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("Error updating ChuDe: " + e.getMessage());
		} finally {
			DatabaseConnection.closeResources(ps);
		}
		return false;
	}

	@Override
	public boolean delete(Integer id) {
		String sql = "DELETE FROM CHUDE WHERE id = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			// SỬA Ở ĐÂY:
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			if (dbInstance == null) {
				System.err.println("Không thể lấy instance của DatabaseConnection trong delete ChuDe.");
				return false;
			}
			conn = dbInstance.getConnection();
			if (conn == null) {
				System.err.println("Không thể lấy Connection từ DatabaseConnection instance trong delete ChuDe.");
				return false;
			}
			// KẾT THÚC SỬA

			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			if (e.getSQLState().equals("23000")) {
				System.err.println("Không thể xóa chủ đề ID " + id
						+ " do có ràng buộc khóa ngoại (ví dụ: có câu hỏi thuộc chủ đề này). " + e.getMessage());
			} else {
				System.err.println("Error deleting ChuDe: " + e.getMessage());
			}
		} finally {
			DatabaseConnection.closeResources(ps);
		}
		return false;
	}
}