// src/main/java/com/quanlynganhangdethi/ui/cauhoi/CauHoiTableModel.java
package com.quanlynganhangdethi.ui.CauHoi;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.quanlynganhangdethi.models.CauHoi;

public class CauHoiTableModel extends AbstractTableModel {
	// Có thể thêm cột "Tên Chủ Đề" nếu muốn join hoặc lấy từ đối tượng CauHoi nếu
	// có
	private final String[] columnNames = { "ID", "Nội Dung (Rút Gọn)", "Loại", "Độ Khó", "Chủ Đề ID" };
	private List<CauHoi> cauHoiList;

	public CauHoiTableModel() {
		this.cauHoiList = new ArrayList<>();
	}

	public CauHoiTableModel(List<CauHoi> cauHoiList) {
		this.cauHoiList = cauHoiList;
	}

	@Override
	public int getRowCount() {
		return cauHoiList.size();
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
		CauHoi cauHoi = cauHoiList.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return cauHoi.getId();
		case 1:
			String noiDung = cauHoi.getNoiDung();
			return noiDung.length() > 50 ? noiDung.substring(0, 47) + "..." : noiDung; // Rút gọn nội dung
		case 2:
			return cauHoi.getLoaiCauHoi();
		case 3:
			return cauHoi.getDoKho();
		case 4:
			return cauHoi.getIdChuDe(); // Hoặc tên chủ đề nếu bạn có
		default:
			return null;
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (cauHoiList.isEmpty()) {
			return Object.class;
		}
		// Xử lý cẩn thận nếu cột có thể null, ví dụ Độ Khó
		if (columnIndex == 3)
			return Integer.class; // Độ khó là Integer
		if (columnIndex == 0 || columnIndex == 4)
			return Integer.class; // ID và ID Chủ Đề là int/Integer
		return String.class; // Mặc định là String cho các cột còn lại
	}

	public void setData(List<CauHoi> cauHoiList) {
		this.cauHoiList = cauHoiList;
		fireTableDataChanged();
	}

	public CauHoi getCauHoiAt(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < cauHoiList.size()) {
			return cauHoiList.get(rowIndex);
		}
		return null;
	}

	public void addCauHoi(CauHoi cauHoi) {
		cauHoiList.add(cauHoi);
		fireTableRowsInserted(cauHoiList.size() - 1, cauHoiList.size() - 1);
	}

	public void updateCauHoi(int rowIndex, CauHoi cauHoi) {
		if (rowIndex >= 0 && rowIndex < cauHoiList.size()) {
			cauHoiList.set(rowIndex, cauHoi);
			fireTableRowsUpdated(rowIndex, rowIndex);
		}
	}

	public void removeCauHoi(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < cauHoiList.size()) {
			cauHoiList.remove(rowIndex);
			fireTableRowsDeleted(rowIndex, rowIndex);
		}
	}
}