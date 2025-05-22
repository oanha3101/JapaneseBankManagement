// src/main/java/com/quanlynganhangdethi/dao/CauHoiDAOImpl.java
package com.quanlynganhangdethi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; // THÊM IMPORT CHO LOGGER
import org.slf4j.LoggerFactory; // THÊM IMPORT CHO LOGGER

import com.quanlynganhangdethi.config.DatabaseConnection;
import com.quanlynganhangdethi.models.CauHoi;

public class CauHoiDAOImpl implements CauHoiDAO {
	// THÊM LOGGER
	private static final Logger logger = LoggerFactory.getLogger(CauHoiDAOImpl.class);

	@Override
	public CauHoi create(CauHoi cauHoi) {
		String sql = "INSERT INTO CAUHOI (noidung, loaicauhoi, dokho, id_chude, audio_path) VALUES (?, ?, ?, ?, ?)";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet generatedKeys = null;
		logger.info("DAO: Bắt đầu tạo câu hỏi: {}...",
				cauHoi.getNoiDung() != null
						? cauHoi.getNoiDung().substring(0, Math.min(20, cauHoi.getNoiDung().length()))
						: "N/A");
		try {
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			conn = dbInstance.getConnection();
			if (conn == null || conn.isClosed()) {
				logger.error("DAO ERROR: Kết nối DB không hợp lệ khi tạo câu hỏi.");
				return null;
			}
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, cauHoi.getNoiDung());
			ps.setString(2, cauHoi.getLoaiCauHoi());
			if (cauHoi.getDoKho() != null) {
				ps.setInt(3, cauHoi.getDoKho());
			} else {
				ps.setNull(3, java.sql.Types.INTEGER);
			}
			ps.setInt(4, cauHoi.getIdChuDe());
			ps.setString(5, cauHoi.getAudioPath()); // Có thể null

			int affectedRows = ps.executeUpdate();
			logger.debug("DAO: Số hàng bị ảnh hưởng khi tạo câu hỏi: {}", affectedRows);

			if (affectedRows > 0) {
				generatedKeys = ps.getGeneratedKeys();
				if (generatedKeys.next()) {
					int newId = generatedKeys.getInt(1);
					cauHoi.setId(newId); // Cập nhật ID vào đối tượng
					logger.info("DAO: Tạo thành công câu hỏi ID: {}", newId);
					return cauHoi;
				} else {
					logger.error("DAO ERROR: Không lấy được generated key sau khi tạo câu hỏi.");
				}
			}
		} catch (SQLException e) {
			logger.error("DAO SQL ERROR: Lỗi SQL khi tạo câu hỏi: {}", e.getMessage(), e);
		} catch (Exception e) {
			logger.error("DAO EXCEPTION: Lỗi khác khi tạo câu hỏi: {}", e.getMessage(), e);
		} finally {
			DatabaseConnection.closeResources(ps, generatedKeys);
			// Không đóng conn ở đây, Service sẽ quản lý transaction và đóng conn
		}
		logger.error("DAO: Tạo câu hỏi thất bại, trả về null.");
		return null;
	}

	@Override
	public CauHoi findById(Integer id) {
		// ... (Giữ nguyên, chỉ sửa System.out thành logger) ...
		String sql = "SELECT id, noidung, loaicauhoi, dokho, id_chude, audio_path FROM CAUHOI WHERE id = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		logger.info("DAO: Bắt đầu tìm câu hỏi ID: {}", id);

		if (id == null) {
			logger.error("DAO ERROR: ID đầu vào là null khi tìm câu hỏi.");
			return null;
		}

		try {
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			conn = dbInstance.getConnection();
			if (conn == null || conn.isClosed()) {
				logger.error("DAO ERROR: Kết nối DB không hợp lệ hoặc đã đóng khi tìm câu hỏi ID {}.", id);
				return null;
			}
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();

			if (rs != null && rs.next()) {
				CauHoi ch = new CauHoi();
				ch.setId(rs.getInt("id"));
				ch.setNoiDung(rs.getString("noidung"));
				ch.setLoaiCauHoi(rs.getString("loaicauhoi"));
				int doKhoVal = rs.getInt("dokho");
				ch.setDoKho(rs.wasNull() ? null : doKhoVal);
				ch.setIdChuDe(rs.getInt("id_chude"));
				ch.setAudioPath(rs.getString("audio_path"));
				logger.info("DAO: Tìm thấy câu hỏi ID: {}", id);
				return ch;
			} else {
				logger.warn("DAO: Không tìm thấy câu hỏi với ID: {}", id);
			}
		} catch (SQLException e) {
			logger.error("DAO SQL ERROR: Lỗi SQL khi tìm câu hỏi ID {}: {}", id, e.getMessage(), e);
		} catch (Exception e) {
			logger.error("DAO EXCEPTION: Lỗi khác khi tìm câu hỏi ID {}: {}", id, e.getMessage(), e);
		} finally {
			DatabaseConnection.closeResources(ps, rs);
		}
		return null;
	}

	@Override
	public List<CauHoi> findAll() {
		// ... (Giữ nguyên, chỉ sửa System.out thành logger) ...
		List<CauHoi> cauHoiList = new ArrayList<>();
		String sql = "SELECT id, noidung, loaicauhoi, dokho, id_chude, audio_path FROM CAUHOI";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		logger.info("DAO: Bắt đầu lấy tất cả câu hỏi.");
		try {
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			conn = dbInstance.getConnection();
			if (conn == null || conn.isClosed()) {
				logger.error("DAO ERROR: Kết nối DB không hợp lệ khi lấy tất cả câu hỏi.");
				return cauHoiList;
			}
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				CauHoi ch = new CauHoi();
				ch.setId(rs.getInt("id"));
				ch.setNoiDung(rs.getString("noidung"));
				ch.setLoaiCauHoi(rs.getString("loaicauhoi"));
				int doKhoVal = rs.getInt("dokho");
				ch.setDoKho(rs.wasNull() ? null : doKhoVal);
				ch.setIdChuDe(rs.getInt("id_chude"));
				ch.setAudioPath(rs.getString("audio_path"));
				cauHoiList.add(ch);
			}
			logger.info("DAO: Tìm thấy {} câu hỏi.", cauHoiList.size());
		} catch (SQLException e) {
			logger.error("DAO SQL ERROR: Lỗi SQL khi lấy tất cả câu hỏi: {}", e.getMessage(), e);
		} catch (Exception e) {
			logger.error("DAO EXCEPTION: Lỗi khác khi lấy tất cả câu hỏi: {}", e.getMessage(), e);
		} finally {
			DatabaseConnection.closeResources(ps, rs);
		}
		return cauHoiList;
	}

	@Override
	public boolean update(CauHoi cauHoi) {
		// ... (Giữ nguyên, chỉ sửa System.out thành logger và kiểm tra null cho cauHoi)
		// ...
		if (cauHoi == null || cauHoi.getId() <= 0) {
			logger.error("DAO ERROR: Đối tượng CauHoi không hợp lệ hoặc thiếu ID để cập nhật.");
			return false;
		}
		String sql = "UPDATE CAUHOI SET noidung = ?, loaicauhoi = ?, dokho = ?, id_chude = ?, audio_path = ? WHERE id = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		logger.info("DAO: Bắt đầu cập nhật câu hỏi ID: {}", cauHoi.getId());
		try {
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			conn = dbInstance.getConnection();
			if (conn == null || conn.isClosed()) {
				logger.error("DAO ERROR: Kết nối DB không hợp lệ khi cập nhật câu hỏi ID {}.", cauHoi.getId());
				return false;
			}
			ps = conn.prepareStatement(sql);
			ps.setString(1, cauHoi.getNoiDung());
			ps.setString(2, cauHoi.getLoaiCauHoi());
			if (cauHoi.getDoKho() != null) {
				ps.setInt(3, cauHoi.getDoKho());
			} else {
				ps.setNull(3, java.sql.Types.INTEGER);
			}
			ps.setInt(4, cauHoi.getIdChuDe());
			ps.setString(5, cauHoi.getAudioPath());
			ps.setInt(6, cauHoi.getId());

			int affectedRows = ps.executeUpdate();
			logger.debug("DAO: Số hàng bị ảnh hưởng khi cập nhật câu hỏi ID {}: {}", cauHoi.getId(), affectedRows);
			return affectedRows > 0;
		} catch (SQLException e) {
			logger.error("DAO SQL ERROR: Lỗi SQL khi cập nhật câu hỏi ID {}: {}", cauHoi.getId(), e.getMessage(), e);
		} catch (Exception e) {
			logger.error("DAO EXCEPTION: Lỗi khác khi cập nhật câu hỏi ID {}: {}", cauHoi.getId(), e.getMessage(), e);
		} finally {
			DatabaseConnection.closeResources(ps);
		}
		logger.error("DAO: Cập nhật thất bại cho câu hỏi ID: {}", cauHoi.getId());
		return false;
	}

	@Override
	public boolean delete(Integer id) {
		// ... (Giữ nguyên, chỉ sửa System.out thành logger) ...
		if (id == null || id <= 0) {
			logger.error("DAO ERROR: ID không hợp lệ để xóa câu hỏi.");
			return false;
		}
		String sql = "DELETE FROM CAUHOI WHERE id = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		logger.info("DAO: Bắt đầu xóa câu hỏi ID: {}", id);
		try {
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			conn = dbInstance.getConnection();
			if (conn == null || conn.isClosed()) {
				logger.error("DAO ERROR: Kết nối DB không hợp lệ khi xóa câu hỏi ID {}.", id);
				return false;
			}
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			int affectedRows = ps.executeUpdate();
			logger.debug("DAO: Số hàng bị ảnh hưởng khi xóa câu hỏi ID {}: {}", id, affectedRows);
			return affectedRows > 0;
		} catch (SQLException e) {
			logger.error("DAO SQL ERROR: Lỗi SQL khi xóa câu hỏi ID {}: {}", id, e.getMessage(), e);
		} catch (Exception e) {
			logger.error("DAO EXCEPTION: Lỗi khác khi xóa câu hỏi ID {}: {}", id, e.getMessage(), e);
		} finally {
			DatabaseConnection.closeResources(ps);
		}
		logger.error("DAO: Xóa thất bại cho câu hỏi ID: {}", id);
		return false;
	}

	@Override
	public List<CauHoi> findByChuDeId(int idChuDe) {
		// ... (Giữ nguyên, chỉ sửa System.out thành logger) ...
		List<CauHoi> cauHoiList = new ArrayList<>();
		String sql = "SELECT id, noidung, loaicauhoi, dokho, id_chude, audio_path FROM CAUHOI WHERE id_chude = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		logger.info("DAO: Bắt đầu tìm câu hỏi cho chủ đề ID: {}", idChuDe);
		try {
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			conn = dbInstance.getConnection();
			if (conn == null || conn.isClosed()) {
				logger.error("DAO ERROR: Kết nối DB không hợp lệ khi tìm câu hỏi theo chủ đề ID {}.", idChuDe);
				return cauHoiList;
			}
			ps = conn.prepareStatement(sql);
			ps.setInt(1, idChuDe);
			rs = ps.executeQuery();
			while (rs.next()) {
				CauHoi ch = new CauHoi();
				ch.setId(rs.getInt("id"));
				ch.setNoiDung(rs.getString("noidung"));
				ch.setLoaiCauHoi(rs.getString("loaicauhoi"));
				int doKhoVal = rs.getInt("dokho");
				ch.setDoKho(rs.wasNull() ? null : doKhoVal);
				ch.setIdChuDe(rs.getInt("id_chude"));
				ch.setAudioPath(rs.getString("audio_path"));
				cauHoiList.add(ch);
			}
			logger.info("DAO: Tìm thấy {} câu hỏi cho chủ đề ID: {}", cauHoiList.size(), idChuDe);
		} catch (SQLException e) {
			logger.error("DAO SQL ERROR: Lỗi SQL khi tìm câu hỏi theo chủ đề ID {}: {}", idChuDe, e.getMessage(), e);
		} catch (Exception e) {
			logger.error("DAO EXCEPTION: Lỗi khác khi tìm câu hỏi theo chủ đề ID {}: {}", idChuDe, e.getMessage(), e);
		} finally {
			DatabaseConnection.closeResources(ps, rs);
		}
		return cauHoiList;
	}

	// --- THÊM CÁC PHƯƠNG THỨC CÒN THIẾU ---
	@Override
	public String getAudioPathById(int idCauHoi) throws SQLException {
		String audioPath = null;
		String sql = "SELECT audio_path FROM CAUHOI WHERE id = ?"; // Đảm bảo tên bảng và cột đúng
		logger.debug("DAO: Lấy audio_path cho câu hỏi ID: {}", idCauHoi);
		// Connection được quản lý bởi Service khi cần transaction,
		// hoặc tự quản lý nếu phương thức này được gọi độc lập và không cần
		// transaction.
		// Để đơn giản, ở đây sẽ tự quản lý connection.
		try (Connection conn = DatabaseConnection.getInstance().getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			if (conn == null || conn.isClosed()) {
				logger.error("DAO ERROR: Kết nối DB không hợp lệ khi lấy audio_path.");
				throw new SQLException("Kết nối DB không hợp lệ.");
			}
			pstmt.setInt(1, idCauHoi);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					audioPath = rs.getString("audio_path");
					logger.debug("DAO: Tìm thấy audio_path='{}' cho câu hỏi ID: {}", audioPath, idCauHoi);
				} else {
					logger.warn("DAO: Không tìm thấy câu hỏi hoặc audio_path cho ID: {}", idCauHoi);
				}
			}
		} catch (SQLException e) {
			logger.error("DAO SQL ERROR: Lỗi khi lấy audio_path cho câu hỏi ID {}: {}", idCauHoi, e.getMessage(), e);
			throw e; // Ném lại để Service xử lý
		}
		return audioPath;
	}

	@Override
	public boolean updateAudioPath(int idCauHoi, String newAudioPath) throws SQLException {
		String sql = "UPDATE CAUHOI SET audio_path = ? WHERE id = ?";
		logger.info("DAO: Cập nhật audio_path='{}' cho câu hỏi ID: {}", newAudioPath, idCauHoi);
		try (Connection conn = DatabaseConnection.getInstance().getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			if (conn == null || conn.isClosed()) {
				logger.error("DAO ERROR: Kết nối DB không hợp lệ khi cập nhật audio_path.");
				throw new SQLException("Kết nối DB không hợp lệ.");
			}
			if (newAudioPath == null || newAudioPath.trim().isEmpty()) {
				pstmt.setNull(1, java.sql.Types.VARCHAR);
			} else {
				pstmt.setString(1, newAudioPath);
			}
			pstmt.setInt(2, idCauHoi);
			int affectedRows = pstmt.executeUpdate();
			logger.debug("DAO: Số hàng bị ảnh hưởng khi cập nhật audio_path: {}", affectedRows);
			return affectedRows > 0;
		} catch (SQLException e) {
			logger.error("DAO SQL ERROR: Lỗi khi cập nhật audio_path cho câu hỏi ID {}: {}", idCauHoi, e.getMessage(),
					e);
			throw e;
		}
	}
	// --- KẾT THÚC PHẦN THÊM ---
}