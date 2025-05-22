package com.quanlynganhangdethi.dao; // Hoặc package com.quanlynganhangdethi.dao

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quanlynganhangdethi.config.DatabaseConnection;
// import com.quanlynganhangdethi.dao.CauHoiDAO; // Sẽ cần nếu findCauHoiByDeThiId cần map chi tiết CauHoi
import com.quanlynganhangdethi.models.CauHoi;
import com.quanlynganhangdethi.models.DeThi;

public class DeThiDAOImpl implements DeThiDAO {
	private static final Logger logger = LoggerFactory.getLogger(DeThiDAOImpl.class);
	// private final CauHoiDAO cauHoiDAO; // Khởi tạo nếu cần

	public DeThiDAOImpl() {
		// this.cauHoiDAO = new CauHoiDAOImpl(); // Ví dụ
	}

	// --- Triển khai các phương thức của ICRUDDAO<DeThi, Integer> ---

	@Override
	public DeThi create(DeThi deThi) throws SQLException {
		// Chỉ lưu các trường có trong Model DeThi.java: tieude, nguoitao
		// Cột ngaytao thường được DB tự động set (DEFAULT CURRENT_TIMESTAMP)
		// Các cột khác như id_chude, thoigianlambai,... sẽ không được set từ đây.
		String sql = "INSERT INTO DETHI (tieude, nguoitao) VALUES (?, ?)";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet generatedKeys = null;
		try {
			conn = DatabaseConnection.getInstance().getConnection();
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			ps.setString(1, deThi.getTieuDe());
			ps.setString(2, deThi.getNguoiTao());

			int affectedRows = ps.executeUpdate();
			if (affectedRows > 0) {
				generatedKeys = ps.getGeneratedKeys();
				if (generatedKeys.next()) {
					deThi.setId(generatedKeys.getInt(1));
					// Tải lại từ DB để lấy ngayTao (nếu DB tự set)
					DeThi createdDeThi = findById(deThi.getId());
					if (createdDeThi != null) {
						return createdDeThi; // Trả về đối tượng đã có ngayTao từ DB
					}
					// Fallback, dù ít khi xảy ra nếu tạo thành công
					logger.warn("Tạo Đề Thi ID {} thành công nhưng không thể tải lại từ DB.", deThi.getId());
					return deThi; // Trả về với ID, ngayTao có thể là null
				} else {
					logger.error("Tạo Đề Thi thất bại, không lấy được ID trả về.");
					throw new SQLException("Tạo Đề Thi thất bại, không lấy được ID trả về.");
				}
			}
		} catch (SQLException e) {
			logger.error("Lỗi SQL khi tạo Đề Thi: {}", e.getMessage(), e);
			throw e;
		} finally {
			DatabaseConnection.closeResources(ps, generatedKeys);
		}
		return null;
	}

	@Override
	public DeThi findById(Integer id) throws SQLException {
		// Chỉ lấy các trường có trong Model DeThi.java
		String sql = "SELECT id, tieude, ngaytao, nguoitao FROM DETHI WHERE id = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		DeThi deThi = null;
		if (id == null || id <= 0) {
			logger.warn("ID Đề Thi không hợp lệ để tìm kiếm: {}", id);
			return null;
		}
		try {
			conn = DatabaseConnection.getInstance().getConnection();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				deThi = new DeThi();
				deThi.setId(rs.getInt("id"));
				deThi.setTieuDe(rs.getString("tieude"));
				deThi.setNgayTao(rs.getTimestamp("ngaytao"));
				deThi.setNguoiTao(rs.getString("nguoitao"));
				// Không set cauHoiList ở đây, phương thức này chỉ lấy thông tin cơ bản
			}
		} catch (SQLException e) {
			logger.error("Lỗi SQL khi tìm Đề Thi theo ID {}: {}", id, e.getMessage(), e);
			throw e;
		} finally {
			DatabaseConnection.closeResources(ps, rs);
		}
		return deThi;
	}

	@Override
	public List<DeThi> findAll() throws SQLException {
		List<DeThi> deThiList = new ArrayList<>();
		// Chỉ lấy các trường có trong Model DeThi.java
		String sql = "SELECT id, tieude, ngaytao, nguoitao FROM DETHI ORDER BY ngaytao DESC";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getInstance().getConnection();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				DeThi deThi = new DeThi();
				deThi.setId(rs.getInt("id"));
				deThi.setTieuDe(rs.getString("tieude"));
				deThi.setNgayTao(rs.getTimestamp("ngaytao"));
				deThi.setNguoiTao(rs.getString("nguoitao"));
				deThiList.add(deThi);
			}
		} catch (SQLException e) {
			logger.error("Lỗi SQL khi lấy tất cả Đề Thi: {}", e.getMessage(), e);
			throw e;
		} finally {
			DatabaseConnection.closeResources(ps, rs);
		}
		return deThiList;
	}

	@Override
	public boolean update(DeThi deThi) throws SQLException {
		// Chỉ cập nhật các trường có trong Model DeThi.java
		String sql = "UPDATE DETHI SET tieude = ?, nguoitao = ? WHERE id = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		if (deThi == null || deThi.getId() <= 0) {
			throw new IllegalArgumentException("Đối tượng Đề Thi hoặc ID không hợp lệ để cập nhật.");
		}
		try {
			conn = DatabaseConnection.getInstance().getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, deThi.getTieuDe());
			ps.setString(2, deThi.getNguoiTao());
			ps.setInt(3, deThi.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Lỗi SQL khi cập nhật Đề Thi ID {}: {}", deThi.getId(), e.getMessage(), e);
			throw e;
		} finally {
			DatabaseConnection.closeResources(ps, null);
		}
	}

	@Override
	public boolean delete(Integer id) throws SQLException {
		// Giả định ON DELETE CASCADE đã được thiết lập trên khóa ngoại của DETHI_CAUHOI
		// trỏ đến DETHI.id
		String sql = "DELETE FROM DETHI WHERE id = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("ID Đề Thi không hợp lệ để xóa.");
		}
		try {
			conn = DatabaseConnection.getInstance().getConnection();
			// Trước khi xóa Đề Thi, cần xóa các liên kết trong DETHI_CAUHOI
			// nếu không có ON DELETE CASCADE từ DETHI_CAUHOI.id_dethi -> DETHI.id
			// Nếu có ON DELETE CASCADE thì bước này không cần thiết.
			// removeAllCauHoiFromDeThi(id); // Gọi nếu không có ON DELETE CASCADE

			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			boolean deleted = ps.executeUpdate() > 0;
			if (deleted) {
				logger.info("Đã xóa Đề Thi ID: {}", id);
			} else {
				logger.warn("Không tìm thấy Đề Thi ID {} để xóa hoặc xóa không thành công.", id);
			}
			return deleted;
		} catch (SQLException e) {
			logger.error("Lỗi SQL khi xóa Đề Thi ID {}: {}", id, e.getMessage(), e);
			throw e;
		} finally {
			DatabaseConnection.closeResources(ps, null);
		}
	}

	// --- Triển khai các phương thức đặc thù của DeThiDAO ---
	// Các phương thức này được giữ nguyên từ code bạn đã cung cấp trong
	// DeThiDAOImpl trước đó.
	// Chúng tự quản lý Connection.
	// Nếu Service layer cần quản lý transaction cho các thao tác này, bạn cần tạo
	// các phiên bản overload nhận Connection làm tham số.

	@Override
	public boolean addCauHoiToDeThi(int idDeThi, int idCauHoi, int thuTu) throws SQLException {
		String sql = "INSERT INTO DETHI_CAUHOI (id_dethi, id_cauhoi, thutu) VALUES (?, ?, ?)";
		Connection conn = null;
		PreparedStatement ps = null;
		logger.debug("[DAO] addCauHoiToDeThi - Đề ID: {}, CH ID: {}, Thứ tự: {}", idDeThi, idCauHoi, thuTu);
		try {
			conn = DatabaseConnection.getInstance().getConnection();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, idDeThi);
			ps.setInt(2, idCauHoi);
			ps.setInt(3, thuTu);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("[DAO SQL ERROR] addCauHoiToDeThi - Lỗi SQL: {}", e.getMessage(), e);
			throw e;
		} finally {
			DatabaseConnection.closeResources(ps, null);
		}
	}

	@Override
	public boolean removeCauHoiFromDeThi(int idDeThi, int idCauHoi) throws SQLException {
		String sql = "DELETE FROM DETHI_CAUHOI WHERE id_dethi = ? AND id_cauhoi = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		logger.debug("[DAO] removeCauHoiFromDeThi - Đề ID: {}, CH ID: {}", idDeThi, idCauHoi);
		try {
			conn = DatabaseConnection.getInstance().getConnection();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, idDeThi);
			ps.setInt(2, idCauHoi);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("[DAO SQL ERROR] removeCauHoiFromDeThi - Lỗi SQL: {}", e.getMessage(), e);
			throw e;
		} finally {
			DatabaseConnection.closeResources(ps, null);
		}
	}

	@Override
	public boolean removeAllCauHoiFromDeThi(int idDeThi) throws SQLException {
		String sql = "DELETE FROM DETHI_CAUHOI WHERE id_dethi = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		logger.debug("[DAO] removeAllCauHoiFromDeThi - Xóa tất cả câu hỏi cho Đề ID: {}", idDeThi);
		try {
			conn = DatabaseConnection.getInstance().getConnection();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, idDeThi);
			ps.executeUpdate(); // Chỉ cần thực thi, không cần kiểm tra số dòng > 0
			logger.info("Đã xóa tất cả câu hỏi liên kết với Đề Thi ID: {}", idDeThi);
			return true; // Coi như thành công nếu không có lỗi
		} catch (SQLException e) {
			logger.error("[DAO SQL ERROR] removeAllCauHoiFromDeThi - Lỗi SQL: {}", e.getMessage(), e);
			throw e;
		} finally {
			DatabaseConnection.closeResources(ps, null);
		}
	}

	@Override
	public boolean updateThuTuCauHoi(int idDeThi, int idCauHoi, int thuTuMoi) throws SQLException {
		String sql = "UPDATE DETHI_CAUHOI SET thutu = ? WHERE id_dethi = ? AND id_cauhoi = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		logger.debug("[DAO] updateThuTuCauHoi - Đề ID: {}, CH ID: {}, Thứ tự mới: {}", idDeThi, idCauHoi, thuTuMoi);
		try {
			conn = DatabaseConnection.getInstance().getConnection();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, thuTuMoi);
			ps.setInt(2, idDeThi);
			ps.setInt(3, idCauHoi);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("[DAO SQL ERROR] updateThuTuCauHoi - Lỗi SQL: {}", e.getMessage(), e);
			throw e;
		} finally {
			DatabaseConnection.closeResources(ps, null);
		}
	}

	@Override
	public List<CauHoi> findCauHoiByDeThiId(int idDeThi) throws SQLException {
		List<CauHoi> cauHoiList = new ArrayList<>();
		// **QUAN TRỌNG: Đảm bảo tên cột trong CAUHOI và DETHI_CAUHOI là chính xác!**
		// Ví dụ: ch.id, dtc.id_cauhoi, dtc.id_dethi, dtc.thutu
		String sql = "SELECT ch.*, dtc.thutu " + "FROM CAUHOI ch " + "JOIN DETHI_CAUHOI dtc ON ch.id = dtc.id_cauhoi "
				+ "WHERE dtc.id_dethi = ? " + "ORDER BY dtc.thutu ASC";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getInstance().getConnection();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, idDeThi);
			rs = ps.executeQuery();
			// CauHoiDAO cauHoiDAO = new CauHoiDAOImpl(); // Để lấy chi tiết câu hỏi nếu cần
			while (rs.next()) {
				CauHoi ch = new CauHoi();
				// Map các trường của CauHoi từ ResultSet
				ch.setId(rs.getInt("id")); // id từ bảng CAUHOI
				ch.setNoiDung(rs.getString("noidung"));
				ch.setLoaiCauHoi(rs.getString("loaicauhoi"));
				int doKhoVal = rs.getInt("dokho"); // Giả sử có cột dokho
				ch.setDoKho(rs.wasNull() ? null : doKhoVal);
				ch.setIdChuDe(rs.getInt("id_chude")); // Giả sử có cột id_chude
				ch.setAudioPath(rs.getString("audio_path")); // Giả sử có cột audio_path
				// int thuTuTrongDe = rs.getInt("thutu"); // Lấy từ DETHI_CAUHOI
				// Model CauHoi của bạn cần có trường để lưu thứ tự nếu muốn hiển thị
				// Ví dụ: ch.setThuTuTrongDe(thuTuTrongDe);

				// Nếu cần load Đáp Án cho mỗi câu hỏi, bạn sẽ cần DapAnDAO
				// Ví dụ:
				// DapAnDAO dapAnDAO = new DapAnDAOImpl();
				// ch.setListDapAn(dapAnDAO.findByCauHoiId(ch.getId()));
				// Hoặc CauHoiDAO có phương thức findByIdWithDapAn.
				// CauHoi chiTietCauHoi = cauHoiDAO.findByIdWithDapAn(ch.getId()); // Nếu có
				// if (chiTietCauHoi != null) ch = chiTietCauHoi;

				cauHoiList.add(ch);
			}
		} catch (SQLException e) {
			logger.error("[DAO SQL ERROR] findCauHoiByDeThiId - Lỗi SQL cho Đề ID {}: {}", idDeThi, e.getMessage(), e);
			throw e;
		} finally {
			DatabaseConnection.closeResources(ps, rs);
		}
		return cauHoiList;
	}
}