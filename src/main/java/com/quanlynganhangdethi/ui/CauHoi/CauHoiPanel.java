// src/main/java/com/quanlynganhangdethi/ui/CauHoi/CauHoiPanel.java
package com.quanlynganhangdethi.ui.CauHoi;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
// import java.awt.Image; // Không cần nữa
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
// import javax.swing.ImageIcon; // Không cần nữa
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.quanlynganhangdethi.models.CauHoi;
import com.quanlynganhangdethi.models.ChuDe;
import com.quanlynganhangdethi.service.CauHoiService;
import com.quanlynganhangdethi.service.ChuDeService;

public class CauHoiPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	// Fonts - Khởi tạo với fallback
	private Font FONT_PANEL_TITLE;
	private Font FONT_BUTTON;
	private Font FONT_TABLE_HEADER;
	private Font FONT_TABLE_CELL;
	private Font FONT_LABEL;

	private JTable tblCauHoi;
	private CauHoiTableModel cauHoiTableModel;
	private JButton btnThem, btnSua, btnXoa, btnLamMoi;
	private JComboBox<Object> cmbFilterChuDe;
	private JLabel lblTitle;

	private CauHoiService cauHoiService;
	private ChuDeService chuDeService;

	/**
	 * Constructor cho CauHoiPanel.
	 */
	public CauHoiPanel() {
		this.cauHoiService = new CauHoiService();
		this.chuDeService = new ChuDeService();

		initializeFonts();

		setLayout(new BorderLayout(10, 15));
		setBorder(new EmptyBorder(15, 20, 15, 20));
		setBackground(UIManager.getColor("Panel.background"));

		initComponents();
		loadFilterChuDe();
	}

	private void initializeFonts() {
		// Lấy font từ UIManager, nếu không có thì dùng font mặc định cứng
		FONT_PANEL_TITLE = UIManager.getFont("Label.font");
		FONT_PANEL_TITLE = (FONT_PANEL_TITLE != null) ? FONT_PANEL_TITLE.deriveFont(Font.BOLD, 22f)
				: new Font("SansSerif", Font.BOLD, 22);

		FONT_BUTTON = UIManager.getFont("Button.font");
		FONT_BUTTON = (FONT_BUTTON != null) ? FONT_BUTTON.deriveFont(Font.BOLD, 13f)
				: new Font("SansSerif", Font.BOLD, 13);

		FONT_TABLE_HEADER = UIManager.getFont("TableHeader.font");
		FONT_TABLE_HEADER = (FONT_TABLE_HEADER != null) ? FONT_TABLE_HEADER.deriveFont(Font.BOLD)
				: new Font("SansSerif", Font.BOLD, 14);

		FONT_TABLE_CELL = UIManager.getFont("Table.font");
		if (FONT_TABLE_CELL == null) {
			FONT_TABLE_CELL = new Font("SansSerif", Font.PLAIN, 13);
		}

		FONT_LABEL = UIManager.getFont("Label.font");
		if (FONT_LABEL == null) {
			FONT_LABEL = new Font("SansSerif", Font.PLAIN, 14);
		}
	}

	private void initComponents() {
		add(createHeaderPanel(), BorderLayout.NORTH);
		JPanel mainContentPanel = new JPanel(new BorderLayout(0, 10));
		mainContentPanel.setOpaque(false);
		mainContentPanel.add(createControlPanel(), BorderLayout.NORTH);
		mainContentPanel.add(createTableScrollPane(), BorderLayout.CENTER);
		add(mainContentPanel, BorderLayout.CENTER);
	}

	private JPanel createHeaderPanel() {
		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		headerPanel.setOpaque(false);
		headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

		lblTitle = new JLabel("QUẢN LÝ NGÂN HÀNG CÂU HỎI");
		lblTitle.setFont(FONT_PANEL_TITLE);
		lblTitle.setForeground(UIManager.getColor("Label.foreground"));
		// Không set icon nữa
		// lblTitle.setIconTextGap(10);
		headerPanel.add(lblTitle);
		return headerPanel;
	}

	private JPanel createControlPanel() {
		JPanel controlPanel = new JPanel(new BorderLayout(20, 0));
		controlPanel.setOpaque(false);
		controlPanel.setBorder(new EmptyBorder(5, 0, 10, 0));

		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		filterPanel.setOpaque(false);

		Font titleBorderFont = (FONT_LABEL != null) ? FONT_LABEL.deriveFont(Font.BOLD)
				: new Font("SansSerif", Font.BOLD, 14);
		TitledBorder filterBorder = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")), "Bộ lọc Câu Hỏi",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, titleBorderFont,
				UIManager.getColor("TitledBorder.titleColor"));
		filterPanel.setBorder(BorderFactory.createCompoundBorder(filterBorder, new EmptyBorder(5, 5, 5, 5)));

		JLabel lblFilter = new JLabel("Chủ đề:");
		lblFilter.setFont(FONT_LABEL);
		filterPanel.add(lblFilter);

		cmbFilterChuDe = new JComboBox<>();
		cmbFilterChuDe.setFont(FONT_LABEL);
		cmbFilterChuDe.setPreferredSize(new Dimension(300, 32));
		cmbFilterChuDe.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				if (value instanceof ChuDe) {
					label.setText(((ChuDe) value).getTenChuDe());
				} else if (value instanceof String) {
					label.setText((String) value);
				} else if (value == null && index == -1) {
					label.setText("Tất cả Chủ Đề");
				}
				label.setBorder(new EmptyBorder(2, 5, 2, 5));
				return label;
			}
		});
		cmbFilterChuDe.addActionListener(e -> loadCauHoiData(getSelectedChuDeFilterId()));
		filterPanel.add(cmbFilterChuDe);
		controlPanel.add(filterPanel, BorderLayout.WEST);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		buttonPanel.setOpaque(false);
		Dimension buttonPreferredSize = new Dimension(120, 38); // Giữ nguyên hoặc điều chỉnh

		btnThem = createStyledButton("Thêm Mới", buttonPreferredSize);
		btnThem.putClientProperty("JButton.buttonType", "default");
		btnThem.addActionListener(e -> themHoacSuaCauHoi(null));

		btnSua = createStyledButton("Sửa", buttonPreferredSize);
		btnSua.setEnabled(false);
		btnSua.addActionListener(e -> {
			int selectedViewRow = tblCauHoi.getSelectedRow();
			if (selectedViewRow != -1) {
				int modelRow = tblCauHoi.convertRowIndexToModel(selectedViewRow);
				CauHoi selectedCauHoi = cauHoiTableModel.getCauHoiAt(modelRow);
				themHoacSuaCauHoi(selectedCauHoi);
			} else {
				showWarningMessage("Vui lòng chọn một câu hỏi để sửa.");
			}
		});

		btnXoa = createStyledButton("Xóa", buttonPreferredSize);
		btnXoa.putClientProperty("JButton.buttonType", "danger");
		btnXoa.setEnabled(false);
		btnXoa.addActionListener(e -> xoaCauHoi());

		btnLamMoi = createStyledButton("Làm Mới", buttonPreferredSize);
		btnLamMoi.addActionListener(e -> loadCauHoiData(getSelectedChuDeFilterId()));

		buttonPanel.add(btnThem);
		buttonPanel.add(btnSua);
		buttonPanel.add(btnXoa);
		buttonPanel.add(btnLamMoi);
		controlPanel.add(buttonPanel, BorderLayout.EAST);

		return controlPanel;
	}

	private JScrollPane createTableScrollPane() {
		cauHoiTableModel = new CauHoiTableModel();
		tblCauHoi = new JTable(cauHoiTableModel);

		tblCauHoi.setFont(FONT_TABLE_CELL);
		tblCauHoi.setRowHeight(30);
		tblCauHoi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblCauHoi.setGridColor(UIManager.getColor("Table.gridColor"));
		tblCauHoi.setShowGrid(true);
		tblCauHoi.setIntercellSpacing(new Dimension(0, 0));

		tblCauHoi.setSelectionBackground(UIManager.getColor("Table.selectionBackground"));
		tblCauHoi.setSelectionForeground(UIManager.getColor("Table.selectionForeground"));
		tblCauHoi.setFillsViewportHeight(true);

		tblCauHoi.setDefaultRenderer(Object.class, new TableCellRenderer() {
			private DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);
				if (c instanceof JLabel) {
					JLabel label = (JLabel) c;
					label.setBorder(new EmptyBorder(0, 8, 0, 8));
					if (column == 0 || column == 3 || column == 4) {
						label.setHorizontalAlignment(SwingConstants.CENTER);
					} else if (column == 1) {
						label.setHorizontalAlignment(SwingConstants.LEFT);
						String text = (value != null) ? value.toString() : "";
						// Tooltip nên được set bên ngoài renderer nếu font chưa chắc chắn có
						// Hoặc set font mặc định cho tooltip
						if (label.getFontMetrics(label.getFont()).stringWidth(text) > table.getColumnModel()
								.getColumn(column).getWidth()) {
							label.setToolTipText(
									"<html><p width='300px'>" + text.replace("\n", "<br>") + "</p></html>");
						} else {
							label.setToolTipText(null);
						}
					} else {
						label.setHorizontalAlignment(SwingConstants.LEFT);
					}
				}

				if (!isSelected) {
					c.setBackground(UIManager.getColor(row % 2 == 0 ? "Table.background" : "Table.alternateRowColor"));
				}
				return c;
			}
		});

		JTableHeader header = tblCauHoi.getTableHeader();
		header.setFont(FONT_TABLE_HEADER);
		header.setBackground(UIManager.getColor("TableHeader.background"));
		header.setForeground(UIManager.getColor("TableHeader.foreground"));
		header.setPreferredSize(new Dimension(0, 40));
		header.setReorderingAllowed(false);
		((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

		tblCauHoi.putClientProperty("JTable.showHorizontalLines", Boolean.TRUE);
		tblCauHoi.putClientProperty("JTable.showVerticalLines", Boolean.TRUE);

		tblCauHoi.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				boolean rowSelected = tblCauHoi.getSelectedRow() != -1;
				if (btnSua != null)
					btnSua.setEnabled(rowSelected);
				if (btnXoa != null)
					btnXoa.setEnabled(rowSelected);
			}
		});

		tblCauHoi.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && tblCauHoi.getSelectedRow() != -1) {
					int modelRow = tblCauHoi.convertRowIndexToModel(tblCauHoi.getSelectedRow());
					CauHoi selectedCauHoi = cauHoiTableModel.getCauHoiAt(modelRow);
					themHoacSuaCauHoi(selectedCauHoi);
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(tblCauHoi);
		scrollPane.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1));
		scrollPane.getViewport().setBackground(UIManager.getColor("Table.background"));
		return scrollPane;
	}

	// Sửa lại createStyledButton để không yêu cầu iconPath
	private JButton createStyledButton(String text, Dimension preferredSize) {
		JButton button = new JButton(text);
		button.setFont(FONT_BUTTON); // FONT_BUTTON đã được đảm bảo không null
		button.setPreferredSize(preferredSize);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setFocusPainted(false);
		// button.setIconTextGap(8); // Không cần nếu không có icon
		button.setMargin(new Insets(5, 15, 5, 15)); // Tăng padding ngang nếu không có icon
		return button;
	}

	// ... (các phương thức showMessage, getSelectedChuDeFilterId, loadFilterChuDe,
	// loadCauHoiData, configureTableColumns giữ nguyên) ...
	// ... (themHoacSuaCauHoi, xoaCauHoi giữ nguyên logic, chỉ bỏ phần icon nếu có
	// trong dialog của chúng) ...

	// ----- CÁC PHƯƠNG THỨC HELPER VÀ LOGIC GIỮ NGUYÊN NHƯ TRƯỚC -----
	private void showInfoMessage(String message) {
		JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
	}

	private void showWarningMessage(String message) {
		JOptionPane.showMessageDialog(this, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
	}

	private void showErrorMessage(String message) {
		JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
	}

	private Integer getSelectedChuDeFilterId() {
		Object selectedItem = cmbFilterChuDe.getSelectedItem();
		if (selectedItem instanceof ChuDe)
			return ((ChuDe) selectedItem).getId();
		return null;
	}

	private void loadFilterChuDe() {
		SwingWorker<Vector<Object>, Void> worker = new SwingWorker<Vector<Object>, Void>() {
			@Override
			protected Vector<Object> doInBackground() throws Exception {
				List<ChuDe> list = chuDeService.getAllChuDe();
				Vector<Object> vector = new Vector<>();
				vector.add("Tất cả Chủ Đề");
				if (list != null) {
					vector.addAll(list);
				}
				return vector;
			}

			@Override
			protected void done() {
				try {
					Vector<Object> modelData = get();
					cmbFilterChuDe.setModel(new DefaultComboBoxModel<>(modelData));
					loadCauHoiData(getSelectedChuDeFilterId());
				} catch (Exception e) {
					showErrorMessage("Lỗi tải danh sách chủ đề cho bộ lọc: " + e.getMessage());
					e.printStackTrace();
				}
			}
		};
		worker.execute();
	}

	private void loadCauHoiData(Integer idChuDeFilter) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (btnThem != null)
			btnThem.setEnabled(false);
		if (btnSua != null)
			btnSua.setEnabled(false);
		if (btnXoa != null)
			btnXoa.setEnabled(false);
		if (btnLamMoi != null)
			btnLamMoi.setEnabled(false);

		SwingWorker<List<CauHoi>, Void> worker = new SwingWorker<List<CauHoi>, Void>() {
			@Override
			protected List<CauHoi> doInBackground() throws Exception {
				List<CauHoi> list;
				if (idChuDeFilter == null) {
					list = cauHoiService.getAllCauHoi();
				} else {
					list = cauHoiService.getCauHoiByChuDeId(idChuDeFilter);
				}
				return list;
			}

			@Override
			protected void done() {
				try {
					List<CauHoi> list = get();
					cauHoiTableModel.setData(list);
					configureTableColumns();
				} catch (Exception e) {
					showErrorMessage("Lỗi khi tải danh sách câu hỏi: " + e.getMessage());
					e.printStackTrace();
				} finally {
					setCursor(Cursor.getDefaultCursor());
					if (tblCauHoi != null)
						tblCauHoi.clearSelection();
					if (btnThem != null)
						btnThem.setEnabled(true);
					if (btnSua != null)
						btnSua.setEnabled(false);
					if (btnXoa != null)
						btnXoa.setEnabled(false);
					if (btnLamMoi != null)
						btnLamMoi.setEnabled(true);
				}
			}
		};
		worker.execute();
	}

	private void configureTableColumns() {
		if (tblCauHoi != null && tblCauHoi.getColumnCount() > 0) {
			TableColumnModel columnModel = tblCauHoi.getColumnModel();
			columnModel.getColumn(0).setPreferredWidth(60);
			columnModel.getColumn(0).setMaxWidth(80);
			if (columnModel.getColumnCount() > 1)
				columnModel.getColumn(1).setPreferredWidth(400);
			if (columnModel.getColumnCount() > 2)
				columnModel.getColumn(2).setPreferredWidth(120);
			if (columnModel.getColumnCount() > 3)
				columnModel.getColumn(3).setPreferredWidth(80);
			if (columnModel.getColumnCount() > 4)
				columnModel.getColumn(4).setPreferredWidth(100);
		}
	}

	private void themHoacSuaCauHoi(CauHoi cauHoiHienTai) {
		CauHoi cauHoiDeXuLyDialog;
		String dialogTitle;
		boolean isEditMode = (cauHoiHienTai != null);

		if (isEditMode) {
			dialogTitle = "Sửa Câu Hỏi (ID: " + cauHoiHienTai.getId() + ")";
			try {
				cauHoiDeXuLyDialog = cauHoiService.getCauHoiByIdWithDapAn(cauHoiHienTai.getId());
				if (cauHoiDeXuLyDialog == null) {
					showErrorMessage("Không tìm thấy thông tin chi tiết của câu hỏi ID: " + cauHoiHienTai.getId());
					return;
				}
			} catch (SQLException e) {
				showErrorMessage("Lỗi SQL khi tải chi tiết câu hỏi: " + e.getMessage());
				e.printStackTrace();
				return;
			}
		} else {
			dialogTitle = "Thêm Câu Hỏi Mới";
			cauHoiDeXuLyDialog = new CauHoi();
		}

		CauHoiDialog dialog = new CauHoiDialog((Frame) SwingUtilities.getWindowAncestor(this), dialogTitle,
				cauHoiDeXuLyDialog);
		dialog.setVisible(true);

		if (dialog.isSaved()) {
			CauHoi cauHoiTuDialog = dialog.getCauHoi();
			String newAudioFileAbsolutePath = dialog.getSelectedAudioFileAbsolutePath();

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				private boolean success = false;
				private String successMessage = "";
				private Exception operationException = null;

				@Override
				protected Void doInBackground() throws Exception {
					try {
						if (!isEditMode) {
							CauHoi cauHoiDaTao = cauHoiService.taoCauHoiVoiDapAn(cauHoiTuDialog,
									newAudioFileAbsolutePath);
							if (cauHoiDaTao != null && cauHoiDaTao.getId() > 0) {
								success = true;
								successMessage = "Thêm câu hỏi thành công! ID: " + cauHoiDaTao.getId();
							} else {
								successMessage = "Thêm câu hỏi thất bại (Service không trả về ID hợp lệ).";
							}
						} else {
							cauHoiTuDialog.setId(cauHoiDeXuLyDialog.getId());
							success = cauHoiService.capNhatCauHoiVoiDapAn(cauHoiTuDialog, newAudioFileAbsolutePath);
							if (success) {
								successMessage = "Cập nhật câu hỏi ID " + cauHoiTuDialog.getId() + " thành công!";
							} else {
								successMessage = "Cập nhật câu hỏi thất bại.";
							}
						}
					} catch (Exception e) {
						operationException = e;
					}
					return null;
				}

				@Override
				protected void done() {
					if (operationException != null) {
						if (operationException instanceof SQLException) {
							showErrorMessage("Lỗi CSDL khi lưu câu hỏi:\n" + operationException.getMessage());
						} else if (operationException instanceof IOException) {
							showErrorMessage("Lỗi File IO khi lưu audio:\n" + operationException.getMessage());
						} else {
							showErrorMessage("Lỗi không xác định khi lưu câu hỏi: " + operationException.getMessage());
						}
						operationException.printStackTrace();
					} else {
						if (success) {
							showInfoMessage(successMessage);
							loadCauHoiData(getSelectedChuDeFilterId());
						} else {
							showErrorMessage(successMessage);
						}
					}
				}
			};
			worker.execute();
		}
	}

	private void xoaCauHoi() {
		int selectedViewRow = tblCauHoi.getSelectedRow();
		if (selectedViewRow == -1) {
			showWarningMessage("Vui lòng chọn một câu hỏi để xóa.");
			return;
		}
		int modelRow = tblCauHoi.convertRowIndexToModel(selectedViewRow);
		CauHoi cauHoiToDelete = cauHoiTableModel.getCauHoiAt(modelRow);
		if (cauHoiToDelete == null) {
			showErrorMessage("Không thể lấy thông tin câu hỏi để xóa.");
			return;
		}

		String noiDungRutGon = cauHoiToDelete.getNoiDung();
		if (noiDungRutGon.length() > 70) {
			noiDungRutGon = noiDungRutGon.substring(0, 70) + "...";
		}

		int confirm = JOptionPane.showConfirmDialog(this, String.format(
				"Bạn có chắc muốn xóa câu hỏi:\nID: %d\nNội dung: %s\n\n(Tất cả đáp án và file audio liên quan cũng sẽ bị xóa)",
				cauHoiToDelete.getId(), noiDungRutGon), "Xác nhận xóa Câu Hỏi", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);

		if (confirm == JOptionPane.YES_OPTION) {
			SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
				@Override
				protected Boolean doInBackground() throws Exception {
					return cauHoiService.xoaCauHoiVoiDapAn(cauHoiToDelete.getId());
				}

				@Override
				protected void done() {
					try {
						boolean success = get();
						if (success) {
							showInfoMessage("Xóa câu hỏi ID " + cauHoiToDelete.getId() + " thành công!");
							loadCauHoiData(getSelectedChuDeFilterId());
						} else {
							showErrorMessage("Không thể xóa câu hỏi ID " + cauHoiToDelete.getId() + ".");
						}
					} catch (Exception e) {
						if (e.getCause() instanceof java.sql.SQLIntegrityConstraintViolationException
								|| (e.getMessage() != null
										&& e.getMessage().toLowerCase().contains("foreign key constraint"))) { // Bắt cả
																												// lỗi
																												// cause
																												// và
																												// message
							showErrorMessage(
									"Không thể xóa câu hỏi này vì nó có thể đang được sử dụng trong một đề thi.");
						} else {
							showErrorMessage("Lỗi khi xóa câu hỏi: " + e.getMessage());
						}
						e.printStackTrace();
					}
				}
			};
			worker.execute();
		}
	}
}