// src/main/java/com/quanlynganhangdethi/dao/DeThiDAO.java
package com.quanlynganhangdethi.dao;

import java.sql.SQLException;
import java.util.List;

import com.quanlynganhangdethi.models.CauHoi; // Cần để trả về danh sách câu hỏi
import com.quanlynganhangdethi.models.DeThi;

public interface DeThiDAO extends ICRUDDAO<DeThi, Integer> {
	// Phương thức CRUD cho DeThi đã được kế thừa từ ICRUDDAO

	// Thao tác với bảng liên kết DETHI_CAUHOI
	boolean addCauHoiToDeThi(int idDeThi, int idCauHoi, int thuTu) throws SQLException;

	boolean removeCauHoiFromDeThi(int idDeThi, int idCauHoi) throws SQLException;

	boolean removeAllCauHoiFromDeThi(int idDeThi) throws SQLException; // Xóa tất cả câu hỏi của một đề

	boolean updateThuTuCauHoi(int idDeThi, int idCauHoi, int thuTuMoi) throws SQLException;

	// Lấy danh sách câu hỏi (đã có thông tin chi tiết) thuộc một đề thi, sắp xếp
	// theo thứ tự
	List<CauHoi> findCauHoiByDeThiId(int idDeThi) throws SQLException;
}