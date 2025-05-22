// src/main/java/com/quanlynganhangdethi/dao/DapAnDAO.java
package com.quanlynganhangdethi.dao;

import java.sql.SQLException; // Thêm nếu phương thức có thể ném SQLException
import java.util.List;

import com.quanlynganhangdethi.models.DapAn;

public interface DapAnDAO extends ICRUDDAO<DapAn, Integer> {
	List<DapAn> findByCauHoiId(int idCauHoi) throws SQLException; // Thêm throws SQLException

	boolean deleteByCauHoiId(int idCauHoi) throws SQLException; // Thêm throws SQLException

	// Thêm phương thức này nếu ICRUDDAO không có save/create phù hợp
	// Giả sử save trả về boolean và cập nhật ID vào đối tượng 'entity'
	// boolean save(DapAn entity) throws SQLException;
	// Hoặc nếu bạn dùng create và nó trả về đối tượng đã lưu:
	// DapAn create(DapAn entity) throws SQLException;
	// Dựa vào code CauHoiService bạn dùng cả dapAnDAO.create và dapAnDAO.save
	// nên cần thống nhất. Giả sử ICRUDDAO có:
	// T create(T entity) throws SQLException;
	// boolean save(T entity) throws SQLException; -> dùng cái này cho update
	// boolean update(T entity) throws SQLException; -> nên có phương thức update
	// riêng
}