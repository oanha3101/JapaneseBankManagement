// src/main/java/com/quanlynganhangdethi/ui/dethi/QuanLyCauHoiTrongDeDialog.java
package com.quanlynganhangdethi.ui.dethi;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel; // Có thể dùng DefaultTableModel nếu không cần tùy chỉnh nhiều

import com.quanlynganhangdethi.models.CauHoi;
import com.quanlynganhangdethi.models.DeThi;
import com.quanlynganhangdethi.service.CauHoiService; // Để truyền vào ChonCauHoiDialog
import com.quanlynganhangdethi.service.ChuDeService; // Để truyền vào ChonCauHoiDialog
import com.quanlynganhangdethi.service.DeThiService;

public class QuanLyCauHoiTrongDeDialog extends JDialog {
	private JTable tblCauHoiCuaDe;
	// Sử dụng một TableModel đơn giản hơn ở đây vì chúng ta có thể quản lý list
	// trực tiếp
	// Hoặc vẫn dùng CauHoiTableModel nếu muốn hiển thị đầy đủ thông tin
	private DefaultTableModel cauHoiCuaDeTableModel;
	private JButton btnThemCauHoiVaoDe;
	private JButton btnXoaCauHoiKhoiDe;
	private JButton btnMoveUp;
	private JButton btnMoveDown;
	private JButton btnLuuThayDoi; // Nút lưu các thay đổi (thêm, xóa, thứ tự)
	private JButton btnDong;

	private DeThi deThiHienTai; // Đề thi đang được quản lý câu hỏi
	private List<CauHoi> danhSachCauHoiTrongDe; // Danh sách làm việc, thay đổi sẽ được lưu
	private DeThiService deThiService;
	private CauHoiService cauHoiService; // Cần cho ChonCauHoiDialog
	private ChuDeService chuDeService; // Cần cho ChonCauHoiDialog

	private final String[] columnNames = { "Thứ Tự", "ID Câu Hỏi", "Nội Dung (Rút Gọn)" };

	public QuanLyCauHoiTrongDeDialog(Frame parent, DeThi deThi, DeThiService deThiService, CauHoiService cauHoiService,
			ChuDeService chuDeService) {
		super(parent, "Quản Lý Câu Hỏi Cho Đề Thi: " + deThi.getTieuDe() + " (ID: " + deThi.getId() + ")", true);
		this.deThiHienTai = deThi;
		this.deThiService = deThiService;
		this.cauHoiService = cauHoiService;
		this.chuDeService = chuDeService;

		// Tạo bản sao của danh sách câu hỏi để có thể hủy thay đổi
		this.danhSachCauHoiTrongDe = new ArrayList<>();
		if (deThi.getCauHoiList() != null) {
			// Cần clone sâu nếu đối tượng CauHoi có thể bị thay đổi ở nơi khác
			// Hiện tại, giả sử CauHoi là immutable hoặc chỉ lấy thông tin cơ bản
			for (CauHoi ch : deThi.getCauHoiList()) {
				// Giả sử CauHoi model có constructor copy hoặc ta clone thủ công các trường cần
				// thiết
				this.danhSachCauHoiTrongDe.add(cloneBasicCauHoiInfo(ch));
			}
		}

		initComponents();
		loadCauHoiCuaDeVaoTable();

		setSize(700, 500);
		setLocationRelativeTo(parent);
	}

	// Hàm clone thông tin cơ bản, không cần clone sâu đáp án ở đây
	private CauHoi cloneBasicCauHoiInfo(CauHoi original) {
		if (original == null)
			return null;
		CauHoi clone = new CauHoi();
		clone.setId(original.getId());
		clone.setNoiDung(original.getNoiDung());
		// Không cần các trường khác nếu chỉ hiển thị ID và nội dung
		return clone;
	}

	private void initComponents() {
		setLayout(new BorderLayout(10, 10));

		cauHoiCuaDeTableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false; // Không cho sửa trực tiếp trên bảng này
			}
		};
		tblCauHoiCuaDe = new JTable(cauHoiCuaDeTableModel);
		tblCauHoiCuaDe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(new JScrollPane(tblCauHoiCuaDe), BorderLayout.CENTER);

		JPanel controlPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		btnThemCauHoiVaoDe = new JButton("Thêm Câu Hỏi Từ Ngân Hàng...");
		btnXoaCauHoiKhoiDe = new JButton("Xóa Câu Hỏi Khỏi Đề");
		btnMoveUp = new JButton("Di Chuyển Lên");
		btnMoveDown = new JButton("Di Chuyển Xuống");

		gbc.gridx = 0;
		gbc.gridy = 0;
		controlPanel.add(btnThemCauHoiVaoDe, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		controlPanel.add(btnXoaCauHoiKhoiDe, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(15, 5, 5, 5);
		controlPanel.add(btnMoveUp, gbc);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.insets = new Insets(5, 5, 5, 5);
		controlPanel.add(btnMoveDown, gbc);

		add(controlPanel, BorderLayout.EAST);

		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnLuuThayDoi = new JButton("Lưu Thay Đổi Vào Đề Thi");
		btnDong = new JButton("Đóng");

		bottomPanel.add(btnLuuThayDoi);
		bottomPanel.add(btnDong);
		add(bottomPanel, BorderLayout.SOUTH);

		// Sự kiện
		btnThemCauHoiVaoDe.addActionListener(e -> themCauHoi());
		btnXoaCauHoiKhoiDe.addActionListener(e -> xoaCauHoi());
		btnMoveUp.addActionListener(e -> moveCauHoi(-1));
		btnMoveDown.addActionListener(e -> moveCauHoi(1));
		btnLuuThayDoi.addActionListener(e -> luuThayDoiVaoCSDL());
		btnDong.addActionListener(e -> dispose());

		tblCauHoiCuaDe.getSelectionModel().addListSelectionListener(e -> updateButtonStates());
		updateButtonStates(); // Gọi lần đầu
	}

	private void loadCauHoiCuaDeVaoTable() {
		cauHoiCuaDeTableModel.setRowCount(0); // Xóa dữ liệu cũ
		for (int i = 0; i < danhSachCauHoiTrongDe.size(); i++) {
			CauHoi ch = danhSachCauHoiTrongDe.get(i);
			cauHoiCuaDeTableModel.addRow(new Object[] { i + 1, // Thứ tự
					ch.getId(),
					ch.getNoiDung().length() > 50 ? ch.getNoiDung().substring(0, 47) + "..." : ch.getNoiDung() });
		}
	}

	private void updateButtonStates() {
		int selectedRow = tblCauHoiCuaDe.getSelectedRow();
		btnXoaCauHoiKhoiDe.setEnabled(selectedRow != -1);
		btnMoveUp.setEnabled(selectedRow > 0); // Không thể di chuyển lên nếu là hàng đầu tiên
		btnMoveDown.setEnabled(selectedRow != -1 && selectedRow < tblCauHoiCuaDe.getRowCount() - 1); // Không thể di
																										// chuyển xuống
																										// nếu là hàng
																										// cuối
	}

	private void themCauHoi() {
		ChonCauHoiDialog chonDialog = new ChonCauHoiDialog(this, cauHoiService, chuDeService);
		chonDialog.setVisible(true);

		List<Integer> idsCauHoiMoi = chonDialog.getDanhSachIdCauHoiDaChon();
		if (idsCauHoiMoi != null && !idsCauHoiMoi.isEmpty()) {
			int currentMaxThuTu = danhSachCauHoiTrongDe.size();
			for (Integer idCh : idsCauHoiMoi) {
				// Kiểm tra xem câu hỏi đã tồn tại trong đề chưa
				boolean daTonTai = danhSachCauHoiTrongDe.stream().anyMatch(ch -> ch.getId() == idCh);
				if (!daTonTai) {
					try {
						// Lấy thông tin cơ bản của câu hỏi để hiển thị (không cần đáp án ở đây)
						CauHoi cauHoiDayDu = cauHoiService.getCauHoiByIdWithDapAn(idCh); // Hoặc một hàm chỉ lấy info cơ
																							// bản
						if (cauHoiDayDu != null) {
							danhSachCauHoiTrongDe.add(cloneBasicCauHoiInfo(cauHoiDayDu)); // Thêm vào list làm việc
						}
					} catch (SQLException ex) {
						JOptionPane.showMessageDialog(this, "Lỗi khi lấy thông tin câu hỏi ID: " + idCh, "Lỗi",
								JOptionPane.ERROR_MESSAGE);
					}
				} else {
					System.out.println("Câu hỏi ID " + idCh + " đã có trong đề.");
				}
			}
			loadCauHoiCuaDeVaoTable(); // Cập nhật bảng
		}
	}

	private void xoaCauHoi() {
		int selectedViewRow = tblCauHoiCuaDe.getSelectedRow();
		if (selectedViewRow != -1) {
			int modelRowIndex = tblCauHoiCuaDe.convertRowIndexToModel(selectedViewRow); // Mặc dù ở đây model và view
																						// index giống nhau
			danhSachCauHoiTrongDe.remove(modelRowIndex);
			loadCauHoiCuaDeVaoTable();
		}
	}

	private void moveCauHoi(int direction) { // -1 for up, 1 for down
		int selectedViewRow = tblCauHoiCuaDe.getSelectedRow();
		if (selectedViewRow == -1)
			return;
		int modelRowIndex = selectedViewRow; // Vì không sort/filter bảng này

		if (direction == -1 && modelRowIndex > 0) { // Move Up
			Collections.swap(danhSachCauHoiTrongDe, modelRowIndex, modelRowIndex - 1);
			loadCauHoiCuaDeVaoTable();
			tblCauHoiCuaDe.setRowSelectionInterval(modelRowIndex - 1, modelRowIndex - 1);
		} else if (direction == 1 && modelRowIndex < danhSachCauHoiTrongDe.size() - 1) { // Move Down
			Collections.swap(danhSachCauHoiTrongDe, modelRowIndex, modelRowIndex + 1);
			loadCauHoiCuaDeVaoTable();
			tblCauHoiCuaDe.setRowSelectionInterval(modelRowIndex + 1, modelRowIndex + 1);
		}
	}

	private void luuThayDoiVaoCSDL() {
		// Lấy danh sách ID câu hỏi theo thứ tự hiện tại trong danhSachCauHoiTrongDe
		List<Integer> danhSachIdCauHoiDaCapNhat = danhSachCauHoiTrongDe.stream().map(CauHoi::getId)
				.collect(Collectors.toList());
		try {
			boolean success = deThiService.capNhatToanBoCauHoiChoDeThi(deThiHienTai.getId(), danhSachIdCauHoiDaCapNhat);
			if (success) {
				JOptionPane.showMessageDialog(this, "Cập nhật danh sách câu hỏi cho đề thi thành công!", "Thành công",
						JOptionPane.INFORMATION_MESSAGE);
				// Cập nhật lại đối tượng deThiHienTai nếu cần
				this.deThiHienTai.setCauHoiList(new ArrayList<>(danhSachCauHoiTrongDe)); // Cập nhật bản sao đang làm
																							// việc
				dispose(); // Đóng dialog
			} else {
				JOptionPane.showMessageDialog(this, "Không thể cập nhật danh sách câu hỏi.", "Lỗi",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Lỗi SQL khi cập nhật câu hỏi cho đề thi: " + e.getMessage(), "Lỗi SQL",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Lỗi không xác định khi cập nhật: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
}