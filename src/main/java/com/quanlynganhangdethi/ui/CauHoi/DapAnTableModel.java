// src/main/java/com/quanlynganhangdethi/ui/cauhoi/DapAnTableModel.java
package com.quanlynganhangdethi.ui.CauHoi;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.quanlynganhangdethi.models.DapAn;

public class DapAnTableModel extends AbstractTableModel {
	private final String[] columnNames = { "Nội Dung Đáp Án", "Là Đáp Án Đúng" };
	private List<DapAn> dapAnList;

	public DapAnTableModel() {
		this.dapAnList = new ArrayList<>();
	}

	public DapAnTableModel(List<DapAn> dapAnList) {
		this.dapAnList = dapAnList != null ? dapAnList : new ArrayList<>();
	}

	@Override
	public int getRowCount() {
		return dapAnList.size();
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
		DapAn dapAn = dapAnList.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return dapAn.getNoiDung();
		case 1:
			return dapAn.isLaDapAnDung();
		default:
			return null;
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 1) {
			return Boolean.class;
		}
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 1 && aValue instanceof Boolean) {
			DapAn dapAn = dapAnList.get(rowIndex);
			boolean newValue = (Boolean) aValue;
			dapAn.setLaDapAnDung(newValue);

			if (newValue) { // Nếu người dùng tick chọn đúng
				// Bỏ chọn các đáp án đúng khác (nếu có quy tắc chỉ 1 đáp án đúng)
				// Giả sử đây là câu hỏi trắc nghiệm chỉ cho phép 1 đáp án đúng
				for (int i = 0; i < dapAnList.size(); i++) {
					if (i != rowIndex && dapAnList.get(i).isLaDapAnDung()) {
						dapAnList.get(i).setLaDapAnDung(false);
						fireTableCellUpdated(i, columnIndex);
					}
				}
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}

	public void setData(List<DapAn> dapAnList) {
		this.dapAnList = dapAnList != null ? dapAnList : new ArrayList<>();
		fireTableDataChanged();
	}

	public DapAn getDapAnAt(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < dapAnList.size()) {
			return dapAnList.get(rowIndex);
		}
		return null;
	}

	public List<DapAn> getDapAnList() {
		return this.dapAnList;
	}

	public void addDapAn(DapAn dapAn) {
		dapAnList.add(dapAn);
		fireTableRowsInserted(dapAnList.size() - 1, dapAnList.size() - 1);
	}

	public void updateDapAn(int rowIndex, DapAn dapAn) {
		if (rowIndex >= 0 && rowIndex < dapAnList.size()) {
			dapAnList.set(rowIndex, dapAn);
			fireTableRowsUpdated(rowIndex, rowIndex);
		}
	}

	public void removeDapAn(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < dapAnList.size()) {
			dapAnList.remove(rowIndex);
			fireTableRowsDeleted(rowIndex, rowIndex);
		}
	}
}