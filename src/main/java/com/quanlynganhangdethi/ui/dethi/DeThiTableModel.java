// src/main/java/com/quanlynganhangdethi/ui/dethi/DeThiTableModel.java
package com.quanlynganhangdethi.ui.dethi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.quanlynganhangdethi.models.DeThi;

public class DeThiTableModel extends AbstractTableModel {
	private final String[] columnNames = { "ID", "Tiêu Đề", "Người Tạo", "Ngày Tạo" };
	private List<DeThi> deThiList;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public DeThiTableModel() {
		this.deThiList = new ArrayList<>();
	}

	public DeThiTableModel(List<DeThi> deThiList) {
		this.deThiList = (deThiList != null) ? deThiList : new ArrayList<>();
	}

	@Override
	public int getRowCount() {
		return deThiList.size();
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
		DeThi deThi = deThiList.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return deThi.getId();
		case 1:
			return deThi.getTieuDe();
		case 2:
			return deThi.getNguoiTao();
		case 3:
			return (deThi.getNgayTao() != null) ? dateFormat.format(deThi.getNgayTao()) : "N/A";
		default:
			return null;
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (deThiList.isEmpty()) {
			return Object.class;
		}
		if (columnIndex == 0)
			return Integer.class;
		return String.class; // Tiêu đề, Người tạo, Ngày tạo (đã format) là String
	}

	public void setData(List<DeThi> deThiList) {
		this.deThiList = (deThiList != null) ? deThiList : new ArrayList<>();
		fireTableDataChanged();
	}

	public DeThi getDeThiAt(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < deThiList.size()) {
			return deThiList.get(rowIndex);
		}
		return null;
	}
}