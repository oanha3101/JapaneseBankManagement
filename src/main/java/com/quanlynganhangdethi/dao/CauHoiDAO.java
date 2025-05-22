// src/main/java/com/quanlynganhangdethi/dao/CauHoiDAO.java
package com.quanlynganhangdethi.dao;

import java.sql.SQLException;
import java.util.List; // Nếu findAll hoặc findByChuDeId trả về List

import com.quanlynganhangdethi.models.CauHoi;

public interface CauHoiDAO extends ICRUDDAO<CauHoi, Integer> {
	// Giả sử ICRUDDAO đã có:
	// CauHoi create(CauHoi entity) throws SQLException; // Hoặc boolean save(CauHoi
	// entity)
	// boolean update(CauHoi entity) throws SQLException;
	// boolean delete(Integer id) throws SQLException;
	// CauHoi findById(Integer id) throws SQLException;
	// List<CauHoi> findAll() throws SQLException;

	// Phương thức tìm câu hỏi theo chủ đề (bạn đã dùng trong service)
	List<CauHoi> findByChuDeId(int idChuDe) throws SQLException;

	// THÊM CÁC PHƯƠNG THỨC CÒN THIẾU
	String getAudioPathById(int idCauHoi) throws SQLException;

	boolean updateAudioPath(int idCauHoi, String newAudioPath) throws SQLException;

	// Nếu ICRUDDAO không có save mà bạn muốn dùng tên `save` để tạo mới và lấy ID
	// (thay vì `create`), bạn có thể thêm ở đây, nhưng tốt hơn là tuân theo
	// ICRUDDAO.
	// Ví dụ, nếu ICRUDDAO dùng `create` trả về `CauHoi` đã có ID:
	// (Không cần thêm gì nếu ICRUDDAO đã có `create`)
	// Nếu ICRUDDAO dùng `save` trả về `boolean` và cập nhật ID vào đối tượng:
	// (Không cần thêm gì nếu ICRUDDAO đã có `save`)
}