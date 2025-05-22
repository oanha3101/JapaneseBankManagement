// src/main/java/com/quanlynganhangdethi/service/DeThiService.java
package com.quanlynganhangdethi.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quanlynganhangdethi.config.DatabaseConnection;
import com.quanlynganhangdethi.dao.CauHoiDAO;
import com.quanlynganhangdethi.dao.CauHoiDAOImpl;
import com.quanlynganhangdethi.dao.ChuDeDAO; // Cần nếu muốn lấy thông tin chủ đề
import com.quanlynganhangdethi.dao.ChuDeDAOImpl;
import com.quanlynganhangdethi.dao.DapAnDAO;
import com.quanlynganhangdethi.dao.DapAnDAOImpl;
import com.quanlynganhangdethi.dao.DeThiDAO;
import com.quanlynganhangdethi.dao.DeThiDAOImpl;
import com.quanlynganhangdethi.models.CauHoi;
import com.quanlynganhangdethi.models.DapAn;
import com.quanlynganhangdethi.models.DeThi;

public class DeThiService {
	private static final Logger logger = LoggerFactory.getLogger(DeThiService.class);
	private final DeThiDAO deThiDAO;
	private final CauHoiDAO cauHoiDAO;
	private final DapAnDAO dapAnDAO;
	private final ChuDeDAO chuDeDAO; // Thêm ChuDeDAO

	public DeThiService() {
		this.deThiDAO = new DeThiDAOImpl();
		this.cauHoiDAO = new CauHoiDAOImpl();
		this.dapAnDAO = new DapAnDAOImpl();
		this.chuDeDAO = new ChuDeDAOImpl(); // Khởi tạo ChuDeDAO
	}

	/**
	 * Tạo một đề thi mới với thông tin cơ bản và danh sách ID câu hỏi. Model
	 * DeThi.java hiện tại chỉ có tieuDe, nguoiTao. Các trường khác (idChuDe,
	 * thoiGianLamBai,...) sẽ không được set từ model này và phụ thuộc vào cấu hình
	 * CSDL (NULL hoặc DEFAULT).
	 * 
	 * @param deThiThongTinCoBan Đối tượng DeThi chứa tieuDe, (có thể cả) nguoiTao.
	 * @param danhSachIdCauHoi   Danh sách ID các câu hỏi sẽ được liên kết với đề
	 *                           thi.
	 * @return Đối tượng DeThi đã được tạo và lấy lại từ CSDL (bao gồm id và
	 *         ngaytao).
	 * @throws SQLException Nếu có lỗi CSDL.
	 */
	public DeThi taoDeThiDayDu(DeThi deThiThongTinCoBan, List<Integer> danhSachIdCauHoi) throws SQLException {
		if (deThiThongTinCoBan == null || deThiThongTinCoBan.getTieuDe() == null
				|| deThiThongTinCoBan.getTieuDe().trim().isEmpty()) {
			throw new IllegalArgumentException("Tiêu đề đề thi không được để trống.");
		}
		// Nếu nguoiTao không được cung cấp, bạn có thể set giá trị mặc định
		if (deThiThongTinCoBan.getNguoiTao() == null || deThiThongTinCoBan.getNguoiTao().trim().isEmpty()) {
			deThiThongTinCoBan.setNguoiTao("System"); // Hoặc lấy từ user đang đăng nhập
		}

		logger.info("Service: Bắt đầu tạo đề thi '{}' với {} câu hỏi.", deThiThongTinCoBan.getTieuDe(),
				danhSachIdCauHoi != null ? danhSachIdCauHoi.size() : 0);

		Connection conn = null;
		DeThi deThiDaTao = null;
		boolean originalAutoCommit = true;

		try {
			conn = DatabaseConnection.getInstance().getConnection();
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false); // Bắt đầu transaction

			// 1. Tạo bản ghi DeThi chính (chỉ với tieude, nguoitao)
			DeThi deThiMoiTrongDB = deThiDAO.create(deThiThongTinCoBan); // Gọi hàm create của ICRUDDAO
			if (deThiMoiTrongDB == null || deThiMoiTrongDB.getId() <= 0) {
				throw new SQLException("Không thể tạo bản ghi đề thi chính trong CSDL hoặc không lấy được ID.");
			}
			deThiDaTao = deThiMoiTrongDB;
			logger.info("Đã tạo bản ghi đề thi chính ID: {}", deThiDaTao.getId());

			// 2. Thêm các câu hỏi vào bảng liên kết DETHI_CAUHOI
			if (danhSachIdCauHoi != null && !danhSachIdCauHoi.isEmpty()) {
				int thuTu = 1;
				for (Integer idCauHoi : danhSachIdCauHoi) {
					// Giả sử DeThiDAO có addCauHoiToDeThi(Connection conn, int idDeThi, int
					// idCauHoi, int thuTu)
					// Nếu không, bạn cần đảm bảo deThiDAO.addCauHoiToDeThi(idDeThi, idCauHoi,
					// thuTu) chạy đúng
					// mà không can thiệp vào transaction hiện tại (ít khả năng nếu nó tự mở
					// connection)
					// TỐT NHẤT: DeThiDAO nên có phiên bản nhận Connection
					if (!deThiDAO.addCauHoiToDeThi(deThiDaTao.getId(), idCauHoi, thuTu++)) { // Nếu hàm này tự quản lý
																								// conn, transaction có
																								// thể không đúng
						throw new SQLException(
								"Không thể thêm câu hỏi ID " + idCauHoi + " vào đề thi ID " + deThiDaTao.getId());
					}
				}
				logger.info("Đã thêm {} câu hỏi vào đề thi ID: {}", danhSachIdCauHoi.size(), deThiDaTao.getId());
				// Cập nhật số lượng câu hỏi vào đối tượng DeThi (nếu model có trường này)
				// và có thể cả vào DB nếu bảng DETHI có cột soluongcauhoi.
				// Hiện tại model DeThi.java không có trường soLuongCauHoi riêng.
			}

			conn.commit();
			logger.info("Transaction tạo đề thi ID {} đã commit.", deThiDaTao.getId());

		} catch (SQLException e) {
			logger.error("Lỗi SQL khi tạo đề thi '{}': {}", deThiThongTinCoBan.getTieuDe(), e.getMessage(), e);
			if (conn != null) {
				try {
					conn.rollback();
					logger.info("Transaction đã rollback do lỗi.");
				} catch (SQLException exRollback) {
					logger.error("Lỗi khi rollback transaction: {}", exRollback.getMessage());
				}
			}
			throw e;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(originalAutoCommit);
				} catch (SQLException ex) {
					logger.error("Lỗi khi reset autoCommit cho connection: {}", ex.getMessage());
				}
				// Connection KHÔNG được đóng ở đây nếu nó là Singleton được quản lý bởi
				// DatabaseConnection
			}
		}
		// Tải lại đề thi với chi tiết (bao gồm cả câu hỏi đã liên kết) để trả về
		if (deThiDaTao != null && deThiDaTao.getId() > 0) {
			return getDeThiChiTiet(deThiDaTao.getId());
		}
		return null;
	}

	/**
	 * Lấy thông tin chi tiết của một đề thi, bao gồm danh sách các câu hỏi và đáp
	 * án của chúng.
	 * 
	 * @param idDeThi ID của đề thi.
	 * @return Đối tượng DeThi chi tiết, hoặc null nếu không tìm thấy.
	 * @throws SQLException
	 */
	public DeThi getDeThiChiTiet(int idDeThi) throws SQLException {
		logger.debug("Service: Lấy chi tiết Đề Thi ID: {}", idDeThi);
		DeThi deThi = deThiDAO.findById(idDeThi); // Lấy thông tin cơ bản của đề thi
		if (deThi != null) {
			// Lấy danh sách các đối tượng CauHoi thuộc đề thi này
			List<CauHoi> cauHoiCuaDe = deThiDAO.findCauHoiByDeThiId(idDeThi);
			if (cauHoiCuaDe != null) {
				for (CauHoi ch : cauHoiCuaDe) {
					// Với mỗi câu hỏi, lấy danh sách đáp án của nó
					List<DapAn> dapAnList = dapAnDAO.findByCauHoiId(ch.getId());
					ch.setDapAnList(dapAnList != null ? dapAnList : new ArrayList<>());
				}
			}
			deThi.setCauHoiList(cauHoiCuaDe != null ? cauHoiCuaDe : new ArrayList<>());

			// Nếu model DeThi.java có trường idChuDe và bạn muốn hiển thị tên chủ đề
			// Và giả sử DeThi.java cũng có trường ChuDe chuDe; và setChuDe(ChuDe);
			// if (deThi.getIdChuDe() != null && deThi.getIdChuDe() > 0) {
			// ChuDe chuDeObj = chuDeDAO.findById(deThi.getIdChuDe());
			// deThi.setChuDe(chuDeObj);
			// }
		}
		return deThi;
	}

	/**
	 * Lấy tất cả các đề thi với thông tin cơ bản.
	 * 
	 * @return Danh sách các đối tượng DeThi.
	 * @throws SQLException
	 */
	public List<DeThi> getAllDeThiCoBan() throws SQLException {
		logger.debug("Service: Lấy tất cả đề thi (thông tin cơ bản).");
		List<DeThi> danhSachDeThi = deThiDAO.findAll();
		// Nếu bạn muốn hiển thị tên chủ đề ngay trong danh sách này (và DeThi model có
		// trường ChuDe)
		// thì bạn cần lặp qua danhSachDeThi và gọi chuDeDAO.findById(dt.getIdChuDe())
		// và dt.setChuDe(chuDeObj) cho từng đề.
		return danhSachDeThi != null ? danhSachDeThi : new ArrayList<>();
	}

	/**
	 * Cập nhật thông tin cơ bản của một đề thi (ví dụ: tiêu đề, người tạo). Danh
	 * sách câu hỏi không được thay đổi bởi phương thức này.
	 * 
	 * @param deThiThongTinCoBan Đối tượng DeThi chứa thông tin cần cập nhật (phải
	 *                           có ID).
	 * @return true nếu cập nhật thành công, false nếu không.
	 * @throws SQLException
	 */
	// Thêm phương thức này vào file DeThiService.java của bạn

	/**
	 * Tạo một đề thi mới chỉ với các thông tin cơ bản (tieude, nguoitao). Các
	 * trường khác sẽ phụ thuộc vào CSDL (NULL hoặc DEFAULT).
	 * 
	 * @param deThiThongTinCoBan Đối tượng DeThi chỉ chứa tieude, nguoitao.
	 * @return Đối tượng DeThi đã được tạo và lấy lại từ CSDL (bao gồm id và
	 *         ngaytao).
	 * @throws SQLException Nếu có lỗi CSDL.
	 */
	public DeThi taoDeThiCoBan(DeThi deThiThongTinCoBan) throws SQLException {
		if (deThiThongTinCoBan == null || deThiThongTinCoBan.getTieuDe() == null
				|| deThiThongTinCoBan.getTieuDe().trim().isEmpty()) {
			throw new IllegalArgumentException("Tiêu đề đề thi không được để trống.");
		}
		// Bạn có thể set người tạo mặc định ở đây nếu deThiThongTinCoBan.getNguoiTao()
		// là null
		if (deThiThongTinCoBan.getNguoiTao() == null || deThiThongTinCoBan.getNguoiTao().trim().isEmpty()) {
			deThiThongTinCoBan.setNguoiTao("System"); // Hoặc lấy từ user đang đăng nhập
		}

		logger.info("Service: Bắt đầu tạo đề thi cơ bản với tiêu đề '{}'", deThiThongTinCoBan.getTieuDe());
		DeThi deThiDaTao = null;
		// Không cần quản lý transaction ở đây nếu deThiDAO.create() là một thao tác đơn
		// lẻ
		// và không có các bước phụ thuộc khác trong phương thức này.
		try {
			// Gọi hàm create của ICRUDDAO (DeThiDAOImpl sẽ triển khai nó)
			// Hàm create này trong DeThiDAOImpl chỉ INSERT tieude, nguoitao
			deThiDaTao = deThiDAO.create(deThiThongTinCoBan);

			if (deThiDaTao != null && deThiDaTao.getId() > 0) {
				logger.info("Đã tạo đề thi cơ bản thành công với ID: {}", deThiDaTao.getId());
			} else {
				logger.error("Tạo đề thi cơ bản thất bại, DAO trả về null hoặc ID không hợp lệ.");
				// Không ném SQLException ở đây nếu DAO đã xử lý và trả về null,
				// Panel sẽ hiển thị thông báo "Không thể tạo đề thi."
				// Nếu DAO ném SQLException thì nó sẽ được bắt ở Panel.
			}
		} catch (SQLException e) {
			logger.error("Lỗi SQL trong service khi tạo đề thi cơ bản: {}", e.getMessage(), e);
			throw e; // Ném lại để Panel xử lý
		}
		return deThiDaTao;
	}

	public boolean capNhatThongTinCoBanCuaDeThi(DeThi deThiThongTinCoBan) throws SQLException {
		if (deThiThongTinCoBan == null || deThiThongTinCoBan.getId() <= 0) {
			throw new IllegalArgumentException("Thông tin đề thi không hợp lệ hoặc thiếu ID để cập nhật.");
		}
		logger.info("Service: Cập nhật thông tin cơ bản cho Đề Thi ID: {}", deThiThongTinCoBan.getId());
		return deThiDAO.update(deThiThongTinCoBan); // Gọi hàm update của ICRUDDAO
	}

	/**
	 * Cập nhật toàn bộ danh sách câu hỏi cho một đề thi. Logic: Xóa tất cả các liên
	 * kết câu hỏi cũ, sau đó thêm lại các liên kết mới.
	 * 
	 * @param idDeThi             ID của đề thi cần cập nhật câu hỏi.
	 * @param danhSachIdCauHoiMoi Danh sách ID các câu hỏi mới.
	 * @return true nếu cập nhật thành công.
	 * @throws SQLException
	 */
	public boolean capNhatToanBoCauHoiChoDeThi(int idDeThi, List<Integer> danhSachIdCauHoiMoi) throws SQLException {
		logger.info("Service: Cập nhật toàn bộ câu hỏi cho Đề Thi ID: {}. Số câu hỏi mới: {}", idDeThi,
				danhSachIdCauHoiMoi != null ? danhSachIdCauHoiMoi.size() : 0);
		Connection conn = null;
		boolean originalAutoCommit = true;
		try {
			conn = DatabaseConnection.getInstance().getConnection();
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false); // Bắt đầu transaction

			// 1. Xóa tất cả các liên kết câu hỏi cũ của đề thi này
			// Giả sử DeThiDAO có removeAllCauHoiFromDeThi(Connection conn, int idDeThi)
			deThiDAO.removeAllCauHoiFromDeThi(idDeThi); // Nếu hàm này tự quản lý conn, transaction có thể không đúng
			logger.debug("Đã xóa các liên kết câu hỏi cũ cho Đề Thi ID: {}", idDeThi);

			// 2. Thêm lại danh sách câu hỏi mới
			if (danhSachIdCauHoiMoi != null && !danhSachIdCauHoiMoi.isEmpty()) {
				int thuTu = 1;
				for (Integer idCauHoi : danhSachIdCauHoiMoi) {
					// Giả sử DeThiDAO có addCauHoiToDeThi(Connection conn, int idDeThi, int
					// idCauHoi, int thuTu)
					if (!deThiDAO.addCauHoiToDeThi(idDeThi, idCauHoi, thuTu++)) { // Nếu hàm này tự quản lý conn,
																					// transaction có thể không đúng
						throw new SQLException(
								"Không thể thêm câu hỏi ID " + idCauHoi + " (mới) vào đề thi ID " + idDeThi);
					}
				}
				logger.info("Đã thêm {} câu hỏi mới vào Đề Thi ID: {}", danhSachIdCauHoiMoi.size(), idDeThi);
			}
			// Cập nhật số lượng câu hỏi trong bảng DETHI nếu có cột đó
			// DeThi deThiToUpdateCount = deThiDAO.findById(idDeThi);
			// if (deThiToUpdateCount != null) {
			// deThiToUpdateCount.setSoLuongCauHoi(danhSachIdCauHoiMoi != null ?
			// danhSachIdCauHoiMoi.size() : 0);
			// deThiDAO.update(deThiToUpdateCount); // Update chỉ số lượng câu hỏi
			// }

			conn.commit();
			return true;
		} catch (SQLException e) {
			logger.error("Lỗi SQL khi cập nhật câu hỏi cho Đề Thi ID {}: {}", idDeThi, e.getMessage(), e);
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException exRb) {
					logger.error("Lỗi khi rollback: {}", exRb.getMessage());
				}
			}
			throw e;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(originalAutoCommit);
				} catch (SQLException ex) {
					logger.error("Lỗi khi reset autoCommit: {}", ex.getMessage());
				}
			}
		}
	}

	/**
	 * Xóa một đề thi và tất cả các liên kết câu hỏi của nó.
	 * 
	 * @param idDeThi ID của đề thi cần xóa.
	 * @return true nếu xóa thành công.
	 * @throws SQLException
	 */
	public boolean xoaDeThiHoanToan(int idDeThi) throws SQLException {
		logger.info("Service: Bắt đầu xóa hoàn toàn Đề Thi ID: {}", idDeThi);
		Connection conn = null;
		boolean originalAutoCommit = true;
		boolean success = false;
		try {
			conn = DatabaseConnection.getInstance().getConnection();
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			// Bước 1: Xóa tất cả các liên kết câu hỏi trong DETHI_CAUHOI
			// Quan trọng: Nếu DeThiDAO.removeAllCauHoiFromDeThi tự mở connection mới, nó sẽ
			// không thuộc transaction này.
			// Cần phiên bản DeThiDAO.removeAllCauHoiFromDeThi(Connection conn, int idDeThi)
			// Hoặc nếu CSDL có ON DELETE CASCADE từ DETHI_CAUHOI.id_dethi -> DETHI.id thì
			// bước này không cần thiết.
			// Để an toàn và tường minh (nếu không chắc về ON DELETE CASCADE):
			deThiDAO.removeAllCauHoiFromDeThi(idDeThi); // Giả định hàm này trong DeThiDAO của bạn là đủ
			logger.debug("Đã xử lý xóa các liên kết câu hỏi cho Đề Thi ID: {}", idDeThi);

			// Bước 2: Xóa bản ghi DeThi chính
			success = deThiDAO.delete(idDeThi); // Gọi hàm delete của ICRUDDAO

			if (success) {
				conn.commit();
				logger.info("Đã xóa hoàn toàn Đề Thi ID: {} và commit transaction.", idDeThi);
			} else {
				// Nếu deThiDAO.delete trả về false (ví dụ: không tìm thấy ID để xóa)
				logger.warn("Không thể xóa bản ghi Đề Thi ID: {} (có thể không tồn tại). Rollback.", idDeThi);
				conn.rollback(); // Rollback nếu bước 2 không thành công dù bước 1 có thể đã chạy
			}
		} catch (SQLException e) {
			logger.error("Lỗi SQL khi xóa hoàn toàn Đề Thi ID {}: {}", idDeThi, e.getMessage(), e);
			if (conn != null)
				try {
					conn.rollback();
					logger.info("Transaction xóa đã rollback do lỗi SQL.");
				} catch (SQLException exRb) {
					logger.error("Lỗi rollback: {}", exRb.getMessage());
				}
			throw e;
		} finally {
			if (conn != null)
				try {
					conn.setAutoCommit(originalAutoCommit);
				} catch (SQLException ex) {
					logger.error("Lỗi reset autocommit: {}", ex.getMessage());
				}
		}
		return success;
	}

	// Phương thức này có vẻ như là một placeholder bạn đã tạo.
	// Nếu bạn muốn tạo đề thi với đầy đủ thông tin (bao gồm idChuDe,
	// thoiGianLamBai,... từ DeThiDialog)
	// thì bạn cần MỞ RỘNG model DeThi.java trước.
	// Hiện tại, nó sẽ giống hệt taoDeThiDayDu vì model DeThi.java rất đơn giản.
	// public DeThi taoDeThiVoiThongTinDayDu(DeThi deThiMoi, Object object) {
	// // TODO Auto-generated method stub
	// // Nếu Object là List<Integer> cauHoiIds:
	// // try {
	// // return taoDeThiDayDu(deThiMoi, (List<Integer>) object);
	// // } catch (SQLException e) {
	// // logger.error("Lỗi khi gọi taoDeThiDayDu từ placeholder: ", e);
	// // return null;
	// // }
	// logger.warn("Phương thức taoDeThiVoiThongTinDayDu chưa được triển khai đầy đủ
	// hoặc cần xem lại model DeThi.java");
	// return null;
	// }
}