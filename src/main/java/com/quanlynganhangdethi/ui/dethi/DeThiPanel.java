// src/main/java/com/quanlynganhangdethi/ui/dethi/DeThiPanel.java
package com.quanlynganhangdethi.ui.dethi;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.quanlynganhangdethi.models.DeThi;
import com.quanlynganhangdethi.service.AIAssistantService;
import com.quanlynganhangdethi.service.CauHoiService;
import com.quanlynganhangdethi.service.ChuDeService;
import com.quanlynganhangdethi.service.DeThiGeneratorService;
import com.quanlynganhangdethi.service.DeThiService;

public class DeThiPanel extends JPanel {
	private JTable tblDeThi;
	private DeThiTableModel deThiTableModel;
	private JButton btnThemDeThi;
	private JButton btnSuaThongTinDe;
	private JButton btnXoaDeThi;
	private JButton btnQuanLyCauHoi;
	private JButton btnLamMoiDanhSach;
	private JButton btnExportDeThiNay;
	private JButton btnGoiYTuAI;

	private DeThiService deThiService;
	private CauHoiService cauHoiService;
	private ChuDeService chuDeService;
	private DeThiGeneratorService deThiGeneratorService;
	private AIAssistantService aiAssistantService;

	// Fonts
	private Font FONT_PANEL_TITLE;
	private Font FONT_BUTTON;
	private Font FONT_TABLE_HEADER;
	private Font FONT_TABLE_CELL;
	private Font FONT_LABEL;

	public DeThiPanel() {
		this.deThiService = new DeThiService();
		this.cauHoiService = new CauHoiService();
		this.chuDeService = new ChuDeService();
		this.deThiGeneratorService = new DeThiGeneratorService();
		this.aiAssistantService = new AIAssistantService();

		initializeFonts();

		setLayout(new BorderLayout(10, 15)); // Khoảng cách ngang, dọc
		setBorder(new EmptyBorder(15, 20, 15, 20)); // Padding tổng thể
		setBackground(UIManager.getColor("Panel.background"));

		initComponents();
		addEventListeners();
		loadDanhSachDeThi();
	}

	private void initializeFonts() {
		FONT_PANEL_TITLE = UIManager.getFont("h1.font"); // Hoặc "Large.font"
		if (FONT_PANEL_TITLE == null) {
			Font labelFont = UIManager.getFont("Label.font");
			FONT_PANEL_TITLE = (labelFont != null) ? labelFont.deriveFont(Font.BOLD, 22f)
					: new Font("SansSerif", Font.BOLD, 22);
		}

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
		mainContentPanel.add(createTableScrollPane(), BorderLayout.CENTER);
		mainContentPanel.add(createBottomButtonPanel(), BorderLayout.SOUTH); // Panel nút ở dưới
		add(mainContentPanel, BorderLayout.CENTER);
	}

	private JPanel createHeaderPanel() {
		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		headerPanel.setOpaque(false);
		headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

		JLabel lblTitle = new JLabel("QUẢN LÝ NGÂN HÀNG ĐỀ THI");
		lblTitle.setFont(FONT_PANEL_TITLE);
		lblTitle.setForeground(UIManager.getColor("Label.foreground"));
		// lblTitle.setIconTextGap(10); // Không cần nếu không có icon
		headerPanel.add(lblTitle);
		return headerPanel;
	}

	private JScrollPane createTableScrollPane() {
		deThiTableModel = new DeThiTableModel();
		tblDeThi = new JTable(deThiTableModel);

		tblDeThi.setFont(FONT_TABLE_CELL);
		tblDeThi.setRowHeight(32);
		tblDeThi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblDeThi.setAutoCreateRowSorter(true);
		tblDeThi.setGridColor(UIManager.getColor("Table.gridColor"));
		tblDeThi.setShowGrid(true);
		tblDeThi.setIntercellSpacing(new Dimension(0, 0));
		tblDeThi.setFillsViewportHeight(true);
		tblDeThi.setSelectionBackground(UIManager.getColor("Table.selectionBackground"));
		tblDeThi.setSelectionForeground(UIManager.getColor("Table.selectionForeground"));

		tblDeThi.setDefaultRenderer(Object.class, new TableCellRenderer() {
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
					} else {
						label.setHorizontalAlignment(SwingConstants.LEFT);
					}
					if (column == 1 || column == 5) {
						String text = (value != null) ? value.toString() : "";
						if (label.getFontMetrics(label.getFont()).stringWidth(text) > table.getColumnModel()
								.getColumn(column).getWidth()) {
							label.setToolTipText(
									"<html><p width='300px'>" + text.replace("\n", "<br>") + "</p></html>");
						} else {
							label.setToolTipText(null);
						}
					}
				}
				if (!isSelected) {
					c.setBackground(UIManager.getColor(row % 2 == 0 ? "Table.background" : "Table.alternateRowColor"));
				}
				return c;
			}
		});

		JTableHeader header = tblDeThi.getTableHeader();
		header.setFont(FONT_TABLE_HEADER);
		header.setBackground(UIManager.getColor("TableHeader.background"));
		header.setForeground(UIManager.getColor("TableHeader.foreground"));
		header.setPreferredSize(new Dimension(0, 40));
		header.setReorderingAllowed(false);
		((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

		tblDeThi.putClientProperty("JTable.showHorizontalLines", Boolean.TRUE);
		tblDeThi.putClientProperty("JTable.showVerticalLines", Boolean.TRUE);

		JScrollPane scrollPane = new JScrollPane(tblDeThi);
		scrollPane.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
		scrollPane.getViewport().setBackground(UIManager.getColor("Table.background"));
		return scrollPane;
	}

	private JPanel createBottomButtonPanel() {
		JPanel buttonContainerPanel = new JPanel(new GridBagLayout());
		buttonContainerPanel.setOpaque(false);

		Font tiltedBorderFont = UIManager.getFont("TitledBorder.font");
		if (tiltedBorderFont == null)
			tiltedBorderFont = FONT_BUTTON.deriveFont(Font.PLAIN);

		buttonContainerPanel
				.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder(
								BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
								"Chức năng Đề Thi", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
								tiltedBorderFont, UIManager.getColor("TitledBorder.titleColor")),
						new EmptyBorder(10, 10, 10, 10)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.weightx = 1.0;

		Dimension buttonSize = new Dimension(0, 38); // Chiều rộng tự động theo GridBagLayout, chiều cao cố định

		btnThemDeThi = createStyledButton("Tạo Đề Mới", buttonSize);
		btnThemDeThi.putClientProperty("JButton.buttonType", "default"); // Nút chính
		gbc.gridx = 0;
		gbc.gridy = 0;
		buttonContainerPanel.add(btnThemDeThi, gbc);

		btnSuaThongTinDe = createStyledButton("Sửa Thông Tin", buttonSize);
		gbc.gridx = 1;
		gbc.gridy = 0;
		buttonContainerPanel.add(btnSuaThongTinDe, gbc);

		btnQuanLyCauHoi = createStyledButton("QL Câu Hỏi", buttonSize);
		gbc.gridx = 2;
		gbc.gridy = 0;
		buttonContainerPanel.add(btnQuanLyCauHoi, gbc);

		btnXoaDeThi = createStyledButton("Xóa Đề Thi", buttonSize);
		btnXoaDeThi.putClientProperty("JButton.buttonType", "danger"); // Nút nguy hiểm
		gbc.gridx = 0;
		gbc.gridy = 1;
		buttonContainerPanel.add(btnXoaDeThi, gbc);

		btnExportDeThiNay = createStyledButton("Export Đề Này", buttonSize);
		gbc.gridx = 1;
		gbc.gridy = 1;
		buttonContainerPanel.add(btnExportDeThiNay, gbc);

		btnLamMoiDanhSach = createStyledButton("Làm Mới", buttonSize);
		gbc.gridx = 2;
		gbc.gridy = 1;
		buttonContainerPanel.add(btnLamMoiDanhSach, gbc);

		if (aiAssistantService != null) {
			btnGoiYTuAI = createStyledButton("Gợi Ý AI", buttonSize);
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.gridwidth = 3;
			gbc.anchor = GridBagConstraints.CENTER;
			buttonContainerPanel.add(btnGoiYTuAI, gbc);
		}

		setRowDependentButtonsEnabled(false);
		return buttonContainerPanel;
	}

	// Bỏ tham số iconPath
	private JButton createStyledButton(String text, Dimension preferredSize) {
		JButton button = new JButton(text);
		button.setFont(FONT_BUTTON);
		if (preferredSize.width > 0) {
			button.setPreferredSize(preferredSize);
		}
		button.setMinimumSize(new Dimension(100, preferredSize.height));
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setFocusPainted(false);
		button.setMargin(new Insets(5, 15, 5, 15)); // Padding bên trong nút
		return button;
	}

	private void setRowDependentButtonsEnabled(boolean enabled) {
		if (btnSuaThongTinDe != null)
			btnSuaThongTinDe.setEnabled(enabled);
		if (btnXoaDeThi != null)
			btnXoaDeThi.setEnabled(enabled);
		if (btnQuanLyCauHoi != null)
			btnQuanLyCauHoi.setEnabled(enabled);
		if (btnExportDeThiNay != null)
			btnExportDeThiNay.setEnabled(enabled);
	}

	private void addEventListeners() {
		tblDeThi.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				setRowDependentButtonsEnabled(tblDeThi.getSelectedRow() != -1);
			}
		});

		btnThemDeThi.addActionListener(e -> themMoiDeThi());
		btnSuaThongTinDe.addActionListener(e -> suaThongTinDeThi());
		btnXoaDeThi.addActionListener(e -> xoaDeThi());
		btnLamMoiDanhSach.addActionListener(e -> loadDanhSachDeThi());
		btnQuanLyCauHoi.addActionListener(e -> quanLyCauHoiTrongDe());
		btnExportDeThiNay.addActionListener(e -> exportDeThiDaChon());
		if (btnGoiYTuAI != null) {
			btnGoiYTuAI.addActionListener(e -> goiYCauHoiTuAI());
		}
	}

	private void loadDanhSachDeThi() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setRowDependentButtonsEnabled(false);
		btnThemDeThi.setEnabled(false);
		btnLamMoiDanhSach.setEnabled(false);
		if (btnGoiYTuAI != null)
			btnGoiYTuAI.setEnabled(false);

		SwingWorker<List<DeThi>, Void> worker = new SwingWorker<List<DeThi>, Void>() {
			@Override
			protected List<DeThi> doInBackground() throws Exception {
				return deThiService.getAllDeThiCoBan();
			}

			@Override
			protected void done() {
				try {
					List<DeThi> list = get();
					deThiTableModel.setData(list);
					configureTableColumns();
				} catch (InterruptedException | ExecutionException e) {
					Throwable cause = e.getCause() != null ? e.getCause() : e;
					JOptionPane.showMessageDialog(DeThiPanel.this, "Lỗi tải danh sách đề thi: " + cause.getMessage(),
							"Lỗi Dữ Liệu", JOptionPane.ERROR_MESSAGE);
					cause.printStackTrace();
				} finally {
					setCursor(Cursor.getDefaultCursor());
					tblDeThi.clearSelection();
					btnThemDeThi.setEnabled(true);
					btnLamMoiDanhSach.setEnabled(true);
					if (btnGoiYTuAI != null)
						btnGoiYTuAI.setEnabled(true);
					setRowDependentButtonsEnabled(false);
				}
			}
		};
		worker.execute();
	}

	private void configureTableColumns() {
		if (tblDeThi != null && tblDeThi.getColumnCount() > 0) {
			TableColumnModel columnModel = tblDeThi.getColumnModel();
			columnModel.getColumn(0).setPreferredWidth(60);
			columnModel.getColumn(0).setMaxWidth(80);
			if (columnModel.getColumnCount() > 1)
				columnModel.getColumn(1).setPreferredWidth(300);
			if (columnModel.getColumnCount() > 2)
				columnModel.getColumn(2).setPreferredWidth(150);
			if (columnModel.getColumnCount() > 3)
				columnModel.getColumn(3).setPreferredWidth(100);
			if (columnModel.getColumnCount() > 4)
				columnModel.getColumn(4).setPreferredWidth(100);
		}
	}

	private Frame getParentFrame() {
		Component parent = this;
		while (parent != null && !(parent instanceof Frame)) {
			parent = parent.getParent();
		}
		return (Frame) parent;
	}

	private void themMoiDeThi() {
		DeThiDialog dialog = new DeThiDialog(getParentFrame(), "Tạo Đề Thi Mới", null);
		dialog.setVisible(true);

		if (dialog.isDaLuu()) {
			DeThi deThiMoiTuDialog = dialog.getDeThi();
			try {
				// Model DeThi.java hiện tại chỉ có thông tin cơ bản
				DeThi deThiDaTaoTrongDB = deThiService.taoDeThiCoBan(deThiMoiTuDialog);

				if (deThiDaTaoTrongDB != null && deThiDaTaoTrongDB.getId() > 0) {
					JOptionPane
							.showMessageDialog(this,
									"Tạo đề thi thành công!\nID: " + deThiDaTaoTrongDB.getId() + "\nTiêu đề: "
											+ deThiDaTaoTrongDB.getTieuDe(),
									"Thành công", JOptionPane.INFORMATION_MESSAGE);
					loadDanhSachDeThi();
				} else {
					JOptionPane.showMessageDialog(this, "Không thể tạo đề thi trong cơ sở dữ liệu.", "Lỗi Tạo Đề Thi",
							JOptionPane.ERROR_MESSAGE);
				}
			} catch (SQLException sqlEx) {
				JOptionPane.showMessageDialog(this, "Lỗi SQL khi tạo đề thi: " + sqlEx.getMessage(), "Lỗi SQL",
						JOptionPane.ERROR_MESSAGE);
				sqlEx.printStackTrace();
			} catch (IllegalArgumentException iae) {
				JOptionPane.showMessageDialog(this, "Lỗi dữ liệu đầu vào: " + iae.getMessage(), "Lỗi Dữ Liệu",
						JOptionPane.WARNING_MESSAGE);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Lỗi không xác định khi tạo đề thi: " + e.getMessage(),
						"Lỗi Không Xác Định", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	private void suaThongTinDeThi() {
		int selectedViewRow = tblDeThi.getSelectedRow();
		if (selectedViewRow == -1) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn một đề thi để sửa.", "Chưa chọn đề thi",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		int modelRow = tblDeThi.convertRowIndexToModel(selectedViewRow);
		DeThi deThiCanSua = deThiTableModel.getDeThiAt(modelRow);
		if (deThiCanSua == null) {
			JOptionPane.showMessageDialog(this, "Không thể lấy thông tin đề thi đã chọn.", "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			// Tải lại thông tin chi tiết (nếu DeThiDialog cần nhiều hơn thông tin cơ bản)
			// DeThi deThiChiTiet = deThiService.getDeThiChiTiet(deThiCanSua.getId());
			// if (deThiChiTiet == null) deThiChiTiet = deThiCanSua; // Fallback nếu không
			// tải được chi tiết

			DeThiDialog dialog = new DeThiDialog(getParentFrame(),
					"Sửa Thông Tin Đề Thi (ID: " + deThiCanSua.getId() + ")", deThiCanSua);
			dialog.setVisible(true);

			if (dialog.isDaLuu()) {
				DeThi deThiDaCapNhatTuDialog = dialog.getDeThi();
				boolean success = deThiService.capNhatThongTinCoBanCuaDeThi(deThiDaCapNhatTuDialog);
				if (success) {
					JOptionPane.showMessageDialog(this, "Cập nhật thông tin đề thi thành công.", "Thành công",
							JOptionPane.INFORMATION_MESSAGE);
					loadDanhSachDeThi();
				} else {
					JOptionPane.showMessageDialog(this, "Không thể cập nhật thông tin đề thi.", "Lỗi Cập Nhật",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		} catch (SQLException sqlEx) {
			JOptionPane.showMessageDialog(this, "Lỗi SQL khi sửa đề thi: " + sqlEx.getMessage(), "Lỗi SQL",
					JOptionPane.ERROR_MESSAGE);
			sqlEx.printStackTrace();
		} catch (IllegalArgumentException iae) {
			JOptionPane.showMessageDialog(this, "Lỗi dữ liệu đầu vào: " + iae.getMessage(), "Lỗi Dữ Liệu",
					JOptionPane.WARNING_MESSAGE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Lỗi không xác định khi sửa đề thi: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void xoaDeThi() {
		int selectedViewRow = tblDeThi.getSelectedRow();
		if (selectedViewRow == -1) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn một đề thi để xóa.", "Chưa chọn đề thi",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		int modelRow = tblDeThi.convertRowIndexToModel(selectedViewRow);
		DeThi deThiCanXoa = deThiTableModel.getDeThiAt(modelRow);

		int confirm = JOptionPane.showConfirmDialog(this, String.format(
				"Bạn có chắc chắn muốn xóa Đề Thi:\nID: %d\nTiêu đề: %s\n\n(Tất cả câu hỏi liên kết với đề thi này cũng sẽ bị xóa khỏi đề.)",
				deThiCanXoa.getId(), deThiCanXoa.getTieuDe()), "Xác nhận xóa Đề Thi", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);

		if (confirm == JOptionPane.YES_OPTION) {
			SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
				@Override
				protected Boolean doInBackground() throws Exception {
					return deThiService.xoaDeThiHoanToan(deThiCanXoa.getId());
				}

				@Override
				protected void done() {
					try {
						boolean success = get();
						if (success) {
							JOptionPane.showMessageDialog(DeThiPanel.this, "Xóa đề thi thành công.", "Thành công",
									JOptionPane.INFORMATION_MESSAGE);
							loadDanhSachDeThi();
						} else {
							JOptionPane.showMessageDialog(DeThiPanel.this,
									"Không thể xóa đề thi. Có thể đề thi không tồn tại hoặc đã xảy ra lỗi.", "Lỗi Xóa",
									JOptionPane.ERROR_MESSAGE);
						}
					} catch (Exception e) {
						Throwable cause = e.getCause() != null ? e.getCause() : e;
						if (cause instanceof java.sql.SQLIntegrityConstraintViolationException) {
							JOptionPane.showMessageDialog(DeThiPanel.this,
									"Không thể xóa đề thi này vì nó có thể đang được tham chiếu ở nơi khác.", "Lỗi Xóa",
									JOptionPane.ERROR_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(DeThiPanel.this, "Lỗi khi xóa đề thi: " + cause.getMessage(),
									"Lỗi", JOptionPane.ERROR_MESSAGE);
						}
						cause.printStackTrace();
					}
				}
			};
			worker.execute();
		}
	}

	private void quanLyCauHoiTrongDe() {
		int selectedViewRow = tblDeThi.getSelectedRow();
		if (selectedViewRow == -1) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn một đề thi để quản lý câu hỏi.", "Chưa chọn đề thi",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		int modelRow = tblDeThi.convertRowIndexToModel(selectedViewRow);
		DeThi deThiDuocChon = deThiTableModel.getDeThiAt(modelRow);

		SwingWorker<DeThi, Void> worker = new SwingWorker<DeThi, Void>() {
			@Override
			protected DeThi doInBackground() throws Exception {
				return deThiService.getDeThiChiTiet(deThiDuocChon.getId());
			}

			@Override
			protected void done() {
				try {
					DeThi deThiChiTiet = get();
					if (deThiChiTiet == null) {
						JOptionPane.showMessageDialog(DeThiPanel.this,
								"Không thể tải chi tiết đề thi ID: " + deThiDuocChon.getId(), "Lỗi Tải Dữ Liệu",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					QuanLyCauHoiTrongDeDialog qlchDialog = new QuanLyCauHoiTrongDeDialog(getParentFrame(), deThiChiTiet,
							deThiService, cauHoiService, chuDeService);
					qlchDialog.setVisible(true);
					loadDanhSachDeThi();
				} catch (Exception e) {
					Throwable cause = e.getCause() != null ? e.getCause() : e;
					JOptionPane.showMessageDialog(DeThiPanel.this, "Lỗi khi mở quản lý câu hỏi: " + cause.getMessage(),
							"Lỗi", JOptionPane.ERROR_MESSAGE);
					cause.printStackTrace();
				}
			}
		};
		worker.execute();
	}

	private void goiYCauHoiTuAI() {
		TaoDeThiTuAnhDialog taoTuAnhDialog = new TaoDeThiTuAnhDialog(getParentFrame());
		taoTuAnhDialog.setVisible(true);
	}

	private void exportDeThiDaChon() {
		int selectedViewRow = tblDeThi.getSelectedRow();
		if (selectedViewRow == -1) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn một đề thi để export.", "Chưa chọn đề thi",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		int modelRow = tblDeThi.convertRowIndexToModel(selectedViewRow);
		final DeThi deThiCoBanCanExport = deThiTableModel.getDeThiAt(modelRow);

		if (deThiCoBanCanExport == null) {
			JOptionPane.showMessageDialog(this, "Không thể lấy thông tin đề thi đã chọn từ bảng.", "Lỗi Dữ Liệu",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Chọn thư mục và tên file cơ sở để lưu Đề Thi");
		String suggestedFileName = deThiCoBanCanExport.getTieuDe().replaceAll("[^a-zA-Z0-9\\.\\-_]", "_").replace(" ",
				"_");
		if (suggestedFileName.trim().isEmpty() || suggestedFileName.equals("_")) {
			suggestedFileName = "DeThiXuat_" + deThiCoBanCanExport.getId();
		}
		fileChooser.setSelectedFile(new File(suggestedFileName));

		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			final File selectedFileBasis = fileChooser.getSelectedFile();

			DeThiGeneratorService.OutputType[] options = { DeThiGeneratorService.OutputType.DE_THI_ONLY_PDF,
					DeThiGeneratorService.OutputType.DE_THI_PDF_DAP_AN_TXT,
					DeThiGeneratorService.OutputType.DAP_AN_CHI_TIET_PDF,
					DeThiGeneratorService.OutputType.CAU_HOI_VA_DAP_AN_TXT };
			final DeThiGeneratorService.OutputType selectedOutputType = (DeThiGeneratorService.OutputType) JOptionPane
					.showInputDialog(this,
							"Chọn loại file muốn xuất cho đề thi:\n'" + deThiCoBanCanExport.getTieuDe() + "' (ID: "
									+ deThiCoBanCanExport.getId() + ")",
							"Tùy Chọn Export Đề Thi", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

			if (selectedOutputType == null)
				return;

			final JDialog progressDialog = new JDialog(getParentFrame(), "Đang Export Đề Thi...", false);
			final JProgressBar progressBarExport = new JProgressBar(0, 100);
			final JLabel lblProgressStatus = new JLabel("Đang chuẩn bị...", JLabel.CENTER);

			progressBarExport.setStringPainted(true);
			Font progressFont = UIManager.getFont("ProgressBar.font");
			if (progressFont == null)
				progressFont = new Font("SansSerif", Font.PLAIN, 12);
			progressBarExport.setFont(progressFont);
			lblProgressStatus.setFont(progressFont.deriveFont(Font.ITALIC));

			JButton btnCancelExport = new JButton("Hủy");
			btnCancelExport.setFont(FONT_BUTTON);
			btnCancelExport.setMargin(new Insets(2, 8, 2, 8));

			JPanel progressBottomPanel = new JPanel(new BorderLayout(5, 0));
			progressBottomPanel.setOpaque(false);
			progressBottomPanel.add(progressBarExport, BorderLayout.CENTER);
			progressBottomPanel.add(btnCancelExport, BorderLayout.EAST);

			progressDialog.setLayout(new BorderLayout(10, 10));
			((JPanel) progressDialog.getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));
			progressDialog.getContentPane().setBackground(UIManager.getColor("Panel.background"));
			progressDialog.add(lblProgressStatus, BorderLayout.NORTH);
			progressDialog.add(progressBottomPanel, BorderLayout.CENTER);
			progressDialog.pack();
			progressDialog.setMinimumSize(new Dimension(450, progressDialog.getHeight()));
			progressDialog.setLocationRelativeTo(this);
			progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

			final SwingWorker<String, String> worker = new SwingWorker<String, String>() {
				private String warningMessage = null;

				@Override
				protected String doInBackground() throws Exception {
					publish("Đang chuẩn bị export...");
					progressBarExport.setIndeterminate(true);
					if (isCancelled())
						return "Đã hủy bởi người dùng.";

					publish("Đang tải chi tiết đề thi ID: " + deThiCoBanCanExport.getId() + "...");
					DeThi deThiDayDu = deThiService.getDeThiChiTiet(deThiCoBanCanExport.getId());
					if (isCancelled())
						return "Đã hủy bởi người dùng.";

					progressBarExport.setIndeterminate(false);
					progressBarExport.setValue(30);
					publish("Đã tải xong chi tiết đề thi.");

					if (deThiDayDu == null) {
						throw new Exception("Lỗi nghiêm trọng: Không thể tải chi tiết Đề Thi ID: "
								+ deThiCoBanCanExport.getId() + ".");
					}
					if (deThiDayDu.getCauHoiList() == null || deThiDayDu.getCauHoiList().isEmpty()) {
						warningMessage = "Lưu ý: Đề thi '" + deThiDayDu.getTieuDe() + "' (ID: " + deThiDayDu.getId()
								+ ") hiện không có câu hỏi nào.";
						publish(warningMessage);
					}
					if (isCancelled())
						return "Đã hủy bởi người dùng.";

					publish("Đang tạo file export cho '" + deThiDayDu.getTieuDe() + "'...");
					progressBarExport.setValue(60);

					String filePathDaTao = deThiGeneratorService.exportSpecificDeThi(deThiDayDu,
							selectedFileBasis.getParent(), selectedFileBasis.getName(), selectedOutputType);
					if (isCancelled()) {
						return "Đã hủy bởi người dùng trong quá trình tạo file.";
					}

					progressBarExport.setValue(100);
					publish("Hoàn tất quá trình tạo file!");

					return "Export thành công!\nFile(s) được lưu tại thư mục: " + selectedFileBasis.getParent()
							+ "\nVới tên cơ sở: " + selectedFileBasis.getName() + "\nLoại file: "
							+ selectedOutputType.toString();
				}

				@Override
				protected void process(List<String> chunks) {
					if (isCancelled())
						return;
					if (!chunks.isEmpty()) {
						String latestStatus = chunks.get(chunks.size() - 1);
						lblProgressStatus.setText(latestStatus);
					}
				}

				@Override
				protected void done() {
					progressDialog.dispose();
					try {
						if (isCancelled()) {
							JOptionPane.showMessageDialog(DeThiPanel.this,
									"Quá trình export đã được hủy bởi người dùng.", "Export Đã Hủy",
									JOptionPane.WARNING_MESSAGE);
							return;
						}
						String resultMessage = get();
						if (warningMessage != null) {
							JOptionPane.showMessageDialog(DeThiPanel.this, resultMessage + "\n\n" + warningMessage,
									"Hoàn Thành Export (Có Lưu Ý)", JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(DeThiPanel.this, resultMessage, "Hoàn Thành Export",
									JOptionPane.INFORMATION_MESSAGE);
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						JOptionPane.showMessageDialog(DeThiPanel.this, "Quá trình export bị gián đoạn (Interrupted).",
								"Export Bị Gián Đoạn", JOptionPane.WARNING_MESSAGE);
						e.printStackTrace();
					} catch (ExecutionException e) {
						Throwable cause = e.getCause();
						if (cause == null)
							cause = e;
						String errorMessage = "Đã xảy ra lỗi trong quá trình export:\n";
						if (cause.getMessage() != null && !cause.getMessage().trim().isEmpty()) {
							errorMessage += cause.getMessage();
						} else {
							errorMessage += "Lỗi không xác định. Vui lòng kiểm tra log.";
						}
						JOptionPane.showMessageDialog(DeThiPanel.this, errorMessage, "Lỗi Export",
								JOptionPane.ERROR_MESSAGE);
						cause.printStackTrace();
					}
				}
			};

			btnCancelExport.addActionListener(e -> {
				if (worker != null && !worker.isDone()) {
					boolean cancelled = worker.cancel(true);
					if (cancelled) {
						lblProgressStatus.setText("Đang hủy export...");
					} else {
						lblProgressStatus.setText("Không thể hủy ngay lúc này...");
					}
				}
			});
			worker.execute();
			progressDialog.setVisible(true);
		}
	}
}