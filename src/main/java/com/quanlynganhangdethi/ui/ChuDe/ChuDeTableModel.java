// src/main/java/com/quanlynganhangdethi/ui/chude/ChuDeTableModel.java
package com.quanlynganhangdethi.ui.ChuDe;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.quanlynganhangdethi.models.ChuDe;

public class ChuDeTableModel extends AbstractTableModel {
	private final String[] columnNames = { "ID", "Tên Chủ Đề", "Mô Tả" };
	private List<ChuDe> chuDeList;

	public ChuDeTableModel() {
		this.chuDeList = new ArrayList<>();
	}

	public ChuDeTableModel(List<ChuDe> chuDeList) {
		this.chuDeList = chuDeList;
	}

	@Override
	public int getRowCount() {
		return chuDeList.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		ChuDe chuDe = chuDeList.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return chuDe.getId();
		case 1:
			return chuDe.getTenChuDe();
		case 2:
			return chuDe.getMoTa();
		default:
			return null;
		}
	}

	// Cho phép JTable biết kiểu dữ liệu của mỗi cột (hữu ích cho việc sắp xếp,
	// render)
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (chuDeList.isEmpty()) {
			return Object.class;
		}
		return getValueAt(0, columnIndex).getClass();
	}

	// Phương thức để cập nhật dữ liệu cho table model
	public void setData(List<ChuDe> chuDeList) {
		this.chuDeList = chuDeList;
		fireTableDataChanged(); // Thông báo cho JTable rằng dữ liệu đã thay đổi
	}

	// Lấy một đối tượng ChuDe tại một hàng cụ thể
	public ChuDe getChuDeAt(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < chuDeList.size()) {
			return chuDeList.get(rowIndex);
		}
		return null;
	}

	public void addChuDe(ChuDe chuDe) {
		chuDeList.add(chuDe);
		fireTableRowsInserted(chuDeList.size() - 1, chuDeList.size() - 1);
	}

	public void updateChuDe(int rowIndex, ChuDe chuDe) {
		if (rowIndex >= 0 && rowIndex < chuDeList.size()) {
			chuDeList.set(rowIndex, chuDe);
			fireTableRowsUpdated(rowIndex, rowIndex);
		}
	}

	public void removeChuDe(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < chuDeList.size()) {
			chuDeList.remove(rowIndex);
			fireTableRowsDeleted(rowIndex, rowIndex);
		}
	}
}