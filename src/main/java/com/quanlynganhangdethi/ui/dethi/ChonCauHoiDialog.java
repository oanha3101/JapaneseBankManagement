// src/main/java/com/quanlynganhangdethi/ui/dethi/ChonCauHoiDialog.java
package com.quanlynganhangdethi.ui.dethi;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.quanlynganhangdethi.models.CauHoi;
import com.quanlynganhangdethi.models.ChuDe; // Nếu có filter theo chủ đề
import com.quanlynganhangdethi.service.CauHoiService;
import com.quanlynganhangdethi.service.ChuDeService; // Nếu có filter
import com.quanlynganhangdethi.ui.CauHoi.CauHoiTableModel;

public class ChonCauHoiDialog extends JDialog {
	private JTable tblNganHangCauHoi;
	private CauHoiTableModel cauHoiTableModel; // Hoặc một TableModel riêng cho việc chọn
	private JButton btnChon;
	private JButton btnHuy;
	private JComboBox<Object> cmbFilterChuDeDialog; // (Tùy chọn) Filter

	private CauHoiService cauHoiService;
	private ChuDeService chuDeService; // (Tùy chọn)

	private List<Integer> danhSachIdCauHoiDaChon;

	public ChonCauHoiDialog(Dialog parent, CauHoiService cauHoiService, ChuDeService chuDeService) {
		super(parent, "Chọn Câu Hỏi Từ Ngân Hàng", true);
		this.cauHoiService = cauHoiService;
		this.chuDeService = chuDeService; // Có thể null nếu không filter
		this.danhSachIdCauHoiDaChon = new ArrayList<>();

		initComponents();
		loadTatCaCauHoi(null); // Load tất cả ban đầu hoặc theo filter mặc định
		if (this.chuDeService != null) {
			loadFilterChuDeDialog();
		}

		setSize(800, 600);
		setLocationRelativeTo(parent);
	}

	private void initComponents() {
		setLayout(new BorderLayout(10, 10));

		// (Tùy chọn) Panel Filter
		if (chuDeService != null) {
			JPanel filterPanelDialog = new JPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanelDialog.add(new JLabel("Lọc theo Chủ đề:"));
			cmbFilterChuDeDialog = new JComboBox<>();
			cmbFilterChuDeDialog.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					if (value instanceof ChuDe)
						setText(((ChuDe) value).getTenChuDe());
					else if (value instanceof String)
						setText((String) value);
					else if (value == null)
						setText("Tất cả Chủ Đề");
					return this;
				}
			});
			cmbFilterChuDeDialog.addActionListener(e -> {
				Object selected = cmbFilterChuDeDialog.getSelectedItem();
				loadTatCaCauHoi(selected instanceof ChuDe ? ((ChuDe) selected).getId() : null);
			});
			filterPanelDialog.add(cmbFilterChuDeDialog);
			add(filterPanelDialog, BorderLayout.NORTH);
		}

		cauHoiTableModel = new CauHoiTableModel(); // Dùng lại hoặc tạo model mới phù hợp
		tblNganHangCauHoi = new JTable(cauHoiTableModel);
		// Cho phép chọn nhiều dòng
		tblNganHangCauHoi.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tblNganHangCauHoi.setAutoCreateRowSorter(true);
		add(new JScrollPane(tblNganHangCauHoi), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnChon = new JButton("Thêm Câu Hỏi Đã Chọn");
		btnHuy = new JButton("Hủy");

		btnChon.addActionListener(e -> xuLyChonCauHoi());
		btnHuy.addActionListener(e -> {
			danhSachIdCauHoiDaChon.clear(); // Xóa lựa chọn nếu hủy
			dispose();
		});

		buttonPanel.add(btnChon);
		buttonPanel.add(btnHuy);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void loadFilterChuDeDialog() {
		try {
			List<ChuDe> list = chuDeService.getAllChuDe();
			Vector<Object> vector = new Vector<>();
			vector.add("Tất cả Chủ Đề");
			if (list != null)
				vector.addAll(list);
			cmbFilterChuDeDialog.setModel(new DefaultComboBoxModel<>(vector));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Lỗi tải chủ đề cho bộ lọc: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void loadTatCaCauHoi(Integer idChuDeFilter) {
		try {
			List<CauHoi> list;
			if (idChuDeFilter == null) {
				list = cauHoiService.getAllCauHoi(); // Service này nên trả về list câu hỏi cơ bản
			} else {
				list = cauHoiService.getCauHoiByChuDeId(idChuDeFilter);
			}
			cauHoiTableModel.setData(list);
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Lỗi tải danh sách câu hỏi: " + e.getMessage(), "Lỗi SQL",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Lỗi không xác định khi tải câu hỏi: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void xuLyChonCauHoi() {
		int[] selectedViewRows = tblNganHangCauHoi.getSelectedRows();
		if (selectedViewRows.length == 0) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một câu hỏi.", "Thông báo",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		danhSachIdCauHoiDaChon.clear();
		for (int viewRow : selectedViewRows) {
			int modelRow = tblNganHangCauHoi.convertRowIndexToModel(viewRow);
			CauHoi ch = cauHoiTableModel.getCauHoiAt(modelRow);
			if (ch != null) {
				danhSachIdCauHoiDaChon.add(ch.getId());
			}
		}
		dispose(); // Đóng dialog, danh sách ID đã được lưu
	}

	public List<Integer> getDanhSachIdCauHoiDaChon() {
		return this.danhSachIdCauHoiDaChon;
	}
}