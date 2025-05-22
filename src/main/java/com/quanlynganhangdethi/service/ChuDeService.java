// src/main/java/com/quanlynganhangdethi/service/ChuDeService.java
package com.quanlynganhangdethi.service;

import java.sql.SQLException; // <<<< THÊM IMPORT NÀY
import java.util.List;

import com.quanlynganhangdethi.dao.ChuDeDAO;
import com.quanlynganhangdethi.dao.ChuDeDAOImpl;
import com.quanlynganhangdethi.models.ChuDe;

public class ChuDeService {
	private ChuDeDAO chuDeDAO;

	public ChuDeService() {
		this.chuDeDAO = new ChuDeDAOImpl(); // Hoặc inject Dependency
	}

	public List<ChuDe> getAllChuDe() throws SQLException { // <<<< THÊM throws SQLException
		System.out.println("[SERVICE] ChuDeService.getAllChuDe - Bắt đầu.");
		try {
			List<ChuDe> result = chuDeDAO.findAll(); // Giờ đây có thể ném SQLException
			System.out.println("[SERVICE] ChuDeService.getAllChuDe - Kết thúc, số lượng: "
					+ (result != null ? result.size() : "null"));
			return result;
		} catch (SQLException e) {
			System.err.println("[SERVICE SQL ERROR] ChuDeService.getAllChuDe - Lỗi SQL: " + e.getMessage());
			e.printStackTrace();
			throw e; // Ném lại SQLException để lớp gọi xử lý
		} catch (Exception e_other) { // Bắt các lỗi runtime không mong muốn khác từ DAO (nếu có)
			System.err.println(
					"[SERVICE EXCEPTION] ChuDeService.getAllChuDe - Lỗi không xác định: " + e_other.getMessage());
			e_other.printStackTrace();
			throw new SQLException("Lỗi không xác định trong Service khi lấy tất cả chủ đề: " + e_other.getMessage(),
					e_other);
		}
	}

	public ChuDe createChuDe(ChuDe chuDe) throws SQLException, Exception { // <<<< GIỮ NGUYÊN Exception hoặc đổi thành
																			// custom exception
																			// VÀ THÊM SQLException
		System.out.println("[SERVICE] ChuDeService.createChuDe - Bắt đầu tạo chủ đề: " + chuDe.getTenChuDe());
		// Ví dụ: Kiểm tra logic nghiệp vụ (nếu có)
		// if (chuDeDAO.findByTen(chuDe.getTenChuDe()) != null) { // Giả sử có phương
		// thức findByTen
		// throw new Exception("Tên chủ đề '" + chuDe.getTenChuDe() + "' đã tồn tại.");
		// }
		try {
			ChuDe chuDeDaTao = chuDeDAO.create(chuDe); // Giờ đây có thể ném SQLException
			if (chuDeDaTao == null) {
				// DAO.create có thể trả về null nếu không có lỗi SQL nhưng không tạo được (ít
				// xảy ra nếu DAO ném lỗi)
				throw new Exception("Không thể tạo chủ đề '" + chuDe.getTenChuDe() + "' (DAO trả về null).");
			}
			System.out.println("[SERVICE] ChuDeService.createChuDe - Tạo thành công chủ đề ID: " + chuDeDaTao.getId());
			return chuDeDaTao;
		} catch (SQLException e) {
			System.err.println("[SERVICE SQL ERROR] ChuDeService.createChuDe - Lỗi SQL: " + e.getMessage());
			e.printStackTrace();
			throw e; // Ném lại SQLException
		}
		// Không cần catch (Exception e) ở đây nữa nếu các lỗi nghiệp vụ đã được ném như
		// trên,
		// và các lỗi runtime khác sẽ tự nổi lên.
		// Tuy nhiên, nếu bạn muốn bao bọc tất cả các lỗi khác thành một kiểu thống nhất
		// từ service, bạn có thể thêm:
		// catch (Exception e_other) {
		// System.err.println("[SERVICE EXCEPTION] ChuDeService.createChuDe - Lỗi không
		// xác định: " + e_other.getMessage());
		// e_other.printStackTrace();
		// throw new Exception("Lỗi không xác định trong Service khi tạo chủ đề: " +
		// e_other.getMessage(), e_other);
		// }
	}

	public ChuDe getChuDeById(int id) throws SQLException { // <<<< THÊM PHƯƠNG THỨC NÀY
		System.out.println("[SERVICE] ChuDeService.getChuDeById - Bắt đầu tìm chủ đề ID: " + id);
		try {
			ChuDe chuDe = chuDeDAO.findById(id);
			System.out.println("[SERVICE] ChuDeService.getChuDeById - Kết thúc, tìm thấy: " + (chuDe != null));
			return chuDe;
		} catch (SQLException e) {
			System.err.println("[SERVICE SQL ERROR] ChuDeService.getChuDeById - Lỗi SQL: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public boolean updateChuDe(ChuDe chuDe) throws SQLException, Exception { // <<<< THÊM PHƯƠNG THỨC NÀY
		System.out.println("[SERVICE] ChuDeService.updateChuDe - Bắt đầu cập nhật chủ đề ID: " + chuDe.getId());
		// Ví dụ: Kiểm tra logic nghiệp vụ trước khi cập nhật
		// ChuDe existingByName = chuDeDAO.findByTen(chuDe.getTenChuDe());
		// if (existingByName != null && existingByName.getId() != chuDe.getId()) {
		// throw new Exception("Tên chủ đề '" + chuDe.getTenChuDe() + "' đã được sử dụng
		// bởi một chủ đề khác.");
		// }
		try {
			boolean success = chuDeDAO.update(chuDe);
			System.out.println("[SERVICE] ChuDeService.updateChuDe - Kết quả cập nhật: " + success);
			return success;
		} catch (SQLException e) {
			System.err.println("[SERVICE SQL ERROR] ChuDeService.updateChuDe - Lỗi SQL: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public boolean deleteChuDe(int id) throws SQLException, Exception { // <<<< THÊM PHƯƠNG THỨC NÀY
		System.out.println("[SERVICE] ChuDeService.deleteChuDe - Bắt đầu xóa chủ đề ID: " + id);
		// Ví dụ: Kiểm tra xem chủ đề có câu hỏi liên kết không trước khi xóa
		// (Mặc dù DAO có thể đã xử lý, nhưng Service có thể có logic nghiệp vụ riêng)
		// CauHoiService cauHoiService = new CauHoiService(); // Cẩn thận vòng lặp
		// dependency
		// if (cauHoiService.getCauHoiByChuDeId(id).size() > 0) {
		// throw new Exception("Không thể xóa chủ đề ID " + id + " vì vẫn còn câu hỏi
		// liên kết.");
		// }
		try {
			boolean success = chuDeDAO.delete(id);
			System.out.println("[SERVICE] ChuDeService.deleteChuDe - Kết quả xóa: " + success);
			return success;
		} catch (SQLException e) {
			System.err.println("[SERVICE SQL ERROR] ChuDeService.deleteChuDe - Lỗi SQL: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
}