// src/main/java/com/quanlynganhangdethi/ui/ChuDe/ChuDePanel.java
package com.quanlynganhangdethi.ui.ChuDe;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.quanlynganhangdethi.dao.ChuDeDAO;
import com.quanlynganhangdethi.dao.ChuDeDAOImpl;
import com.quanlynganhangdethi.models.ChuDe;

public class ChuDePanel extends JPanel {
	private JTable tblChuDe;
	private ChuDeTableModel chuDeTableModel;
	private JButton btnThem;
	private JButton btnSua;
	private JButton btnXoa;
	private JButton btnLamMoi;

	// Fonts - Khởi tạo với fallback
	private Font FONT_PANEL_TITLE;
	private Font FONT_BUTTON;
	private Font FONT_TABLE_HEADER;
	private Font FONT_TABLE_CELL;

	private ChuDeDAO chuDeDAO;

	public ChuDePanel() {
		this.chuDeDAO = new ChuDeDAOImpl();
		initializeFonts(); // Gọi hàm khởi tạo font

		setLayout(new BorderLayout(10, 15)); // mainPanelVgap, mainPanelHgap
		setBorder(new EmptyBorder(15, 20, 15, 20)); // top, left, bottom, right padding
		setBackground(UIManager.getColor("Panel.background"));

		initComponents();
		loadChuDeData();
	}

//	private void initializeFonts() {
//		FONT_PANEL_TITLE = UIManager.getFont("h1.font"); // Thử các key "h1.font", "h2.font", "Panel.titleFont"
//		if (FONT_PANEL_TITLE == null) {
//			Font labelFont = UIManager.getFont("Label.font");
//			FONT_PANEL_TITLE = (labelFont != null) ? labelFont.deriveFont(Font.BOLD, 22f)
//					: new Font("Segoe UI", Font.BOLD, 22f);
//		}
//
//		FONT_BUTTON = UIManager.getFont("Button.font");
//		if (FONT_BUTTON != null) {
//			FONT_BUTTON = FONT_BUTTON.deriveFont(Font.BOLD, 13f);
//		} else {
//			FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13f);
//		}
//
//		FONT_TABLE_HEADER = UIManager.getFont("TableHeader.font");
//		if (FONT_TABLE_HEADER != null) {
//			FONT_TABLE_HEADER = FONT_TABLE_HEADER.deriveFont(Font.BOLD);
//		} else {
//			FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 14f);
//		}
//
//		FONT_TABLE_CELL = UIManager.getFont("Table.font");
//		if (FONT_TABLE_CELL == null) {
//			FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 13f);
//		}
//	}

	private void initializeFonts() {
		// TODO Auto-generated method stub

	}

	private void initComponents() {
		// --- 1. Panel Tiêu đề ---
		add(createHeaderPanel(), BorderLayout.NORTH);

		// --- 2. Panel Chính cho Nội dung (Bảng và Nút điều khiển) ---
		JPanel mainContentPanel = new JPanel(new BorderLayout(0, 10)); // vgap
		mainContentPanel.setOpaque(false); // Cho phép màu nền của ChuDePanel hiển thị qua

		// --- 2a. Panel Nút Điều khiển (Thêm, Sửa, Xóa, Làm mới) ---
		mainContentPanel.add(createButtonControlPanel(), BorderLayout.NORTH);

		// --- 2b. Panel Bảng Dữ liệu ---
		mainContentPanel.add(createTableScrollPane(), BorderLayout.CENTER);

		add(mainContentPanel, BorderLayout.CENTER);
	}

	private JPanel createHeaderPanel() {
		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		headerPanel.setOpaque(false);
		headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0)); // Padding dưới cho header

		JLabel lblTitle = new JLabel("QUẢN LÝ CHỦ ĐỀ");
		lblTitle.setFont(FONT_PANEL_TITLE);
		lblTitle.setForeground(UIManager.getColor("Label.foreground"));
		try {
			ImageIcon icon = new ImageIcon(getClass().getResource("/icons/chude_title.png"));
			// Scale icon nếu cần, ví dụ cho kích thước 28x28
			if (icon.getImage() != null) {
				lblTitle.setIcon(new ImageIcon(icon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH)));
			}
		} catch (Exception e) {
			System.err.println("Lỗi tải icon tiêu đề Chủ đề (/icons/chude_title.png): " + e.getMessage());
		}
		lblTitle.setIconTextGap(10);

		headerPanel.add(lblTitle);
		return headerPanel;
	}

	private JPanel createButtonControlPanel() {
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		buttonPanel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 0, 8); // Khoảng cách phải giữa các nút
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;

		Dimension buttonPreferredSize = new Dimension(125, 38); // Kích thước nút

		btnThem = createStyledButton("Thêm Mới", "/icons/add_plus.png", buttonPreferredSize);
		btnThem.putClientProperty("JButton.buttonType", "default"); // Nút chính (FlatLaf)
		btnThem.addActionListener(e -> themChuDe());
		buttonPanel.add(btnThem, gbc);

		btnSua = createStyledButton("Sửa", "/icons/edit.png", buttonPreferredSize);
		btnSua.setEnabled(false);
		btnSua.addActionListener(e -> suaChuDe());
		buttonPanel.add(btnSua, gbc);

		btnXoa = createStyledButton("Xóa", "/icons/delete_trash.png", buttonPreferredSize);
		btnXoa.putClientProperty("JButton.buttonType", "danger");
		btnXoa.setEnabled(false);
		btnXoa.addActionListener(e -> xoaChuDe());
		buttonPanel.add(btnXoa, gbc);

		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		buttonPanel.add(Box.createHorizontalGlue(), gbc); // Spacer
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;

		gbc.insets = new Insets(0, 0, 0, 0); // Nút cuối không cần padding phải
		btnLamMoi = createStyledButton("Làm Mới", "/icons/refresh.png", buttonPreferredSize);
		btnLamMoi.addActionListener(e -> loadChuDeData());
		buttonPanel.add(btnLamMoi, gbc);

		return buttonPanel;
	}

	private JScrollPane createTableScrollPane() {
		chuDeTableModel = new ChuDeTableModel();
		tblChuDe = new JTable(chuDeTableModel);

		tblChuDe.setFont(FONT_TABLE_CELL);
		tblChuDe.setRowHeight(34);
		tblChuDe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblChuDe.setGridColor(UIManager.getColor("Table.gridColor"));
		tblChuDe.setShowGrid(true);
		tblChuDe.setIntercellSpacing(new Dimension(0, 0));

		tblChuDe.setSelectionBackground(UIManager.getColor("Table.selectionBackground"));
		tblChuDe.setSelectionForeground(UIManager.getColor("Table.selectionForeground"));

		TableColumnModel columnModel = tblChuDe.getColumnModel();
		if (columnModel.getColumnCount() > 0)
			columnModel.getColumn(0).setPreferredWidth(60);
		if (columnModel.getColumnCount() > 0)
			columnModel.getColumn(0).setMaxWidth(100);
		if (columnModel.getColumnCount() > 1)
			columnModel.getColumn(1).setPreferredWidth(300);
		if (columnModel.getColumnCount() > 2)
			columnModel.getColumn(2).setPreferredWidth(350);
		if (columnModel.getColumnCount() > 3)
			columnModel.getColumn(3).setPreferredWidth(150);
		if (columnModel.getColumnCount() > 4)
			columnModel.getColumn(4).setPreferredWidth(150);

		tblChuDe.setDefaultRenderer(Object.class, new TableCellRenderer() {
			private DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);
				if (c instanceof JLabel) {
					((JLabel) c).setHorizontalAlignment((column == 0) ? SwingConstants.CENTER : SwingConstants.LEFT);
					((JLabel) c).setBorder(new EmptyBorder(0, 8, 0, 8)); // Padding trong cell
				}

				if (!isSelected) {
					// FlatLaf có thể tự xử lý màu xen kẽ nếu bật client property
					c.setBackground(UIManager.getColor(row % 2 == 0 ? "Table.background" : "Table.alternateRowColor"));
				}
				return c;
			}
		});

		JTableHeader header = tblChuDe.getTableHeader();
		header.setFont(FONT_TABLE_HEADER);
		header.setBackground(UIManager.getColor("TableHeader.background"));
		header.setForeground(UIManager.getColor("TableHeader.foreground"));
		header.setPreferredSize(new Dimension(0, 42));
		header.setReorderingAllowed(false);
		((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

		tblChuDe.putClientProperty("JTable.showHorizontalLines", Boolean.TRUE);
		tblChuDe.putClientProperty("JTable.showVerticalLines", Boolean.TRUE);
		// tblChuDe.putClientProperty("Table.stripeColor",
		// UIManager.getColor("Table.alternateRowColor")); // Nếu L&F hỗ trợ

		tblChuDe.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				boolean rowSelected = tblChuDe.getSelectedRow() != -1;
				btnSua.setEnabled(rowSelected);
				btnXoa.setEnabled(rowSelected);
			}
		});

		JScrollPane scrollPane = new JScrollPane(tblChuDe);
		scrollPane.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1));
		scrollPane.getViewport().setBackground(UIManager.getColor("Table.background"));
		return scrollPane;
	}

	private JButton createStyledButton(String text, String iconPath, Dimension preferredSize) {
		JButton button = new JButton(text);
		button.setFont(FONT_BUTTON);
		button.setPreferredSize(preferredSize);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setFocusPainted(false);
		button.setIconTextGap(8);
		// Thêm padding bên trong nút thông qua Margin (FlatLaf) hoặc Border
		button.setMargin(new Insets(5, 10, 5, 10));

		if (iconPath != null && !iconPath.isEmpty()) {
			try {
				ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
				if (icon.getImage() != null) {
					Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
					button.setIcon(new ImageIcon(img));
				}
			} catch (Exception e) {
				System.err.println("Lỗi tải icon " + iconPath + " cho nút " + text + ": " + e.getMessage());
			}
		}
		return button;
	}

	private void loadChuDeData() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		btnThem.setEnabled(false);
		btnSua.setEnabled(false);
		btnXoa.setEnabled(false);
		btnLamMoi.setEnabled(false);

		SwingWorker<List<ChuDe>, Void> worker = new SwingWorker<List<ChuDe>, Void>() {
			@Override
			protected List<ChuDe> doInBackground() throws Exception {
				return chuDeDAO.findAll();
			}

			@Override
			protected void done() {
				try {
					List<ChuDe> list = get();
					chuDeTableModel.setData(list);
				} catch (Exception e) {
					showErrorMessage("Lỗi khi tải dữ liệu Chủ đề: " + e.getMessage());
					e.printStackTrace();
				} finally {
					setCursor(Cursor.getDefaultCursor());
					tblChuDe.clearSelection();
					btnThem.setEnabled(true);
					btnSua.setEnabled(false);
					btnXoa.setEnabled(false);
					btnLamMoi.setEnabled(true);
				}
			}
		};
		worker.execute();
	}

	private void themChuDe() {
		ChuDeDialog dialog = new ChuDeDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Chủ Đề Mới", null);
		dialog.setVisible(true);
		if (dialog.isSaved()) {
			ChuDe newChuDe = dialog.getChuDe();
			SwingWorker<ChuDe, Void> worker = new SwingWorker<ChuDe, Void>() {
				@Override
				protected ChuDe doInBackground() throws Exception {
					return chuDeDAO.create(newChuDe);
				}

				@Override
				protected void done() {
					try {
						ChuDe createdChuDe = get();
						if (createdChuDe != null) {
							showSuccessMessage("Thêm chủ đề \"" + createdChuDe.getTenChuDe() + "\" thành công!");
							loadChuDeData();
							SwingUtilities.invokeLater(() -> {
								for (int i = 0; i < chuDeTableModel.getRowCount(); i++) {
									if (chuDeTableModel.getChuDeAt(i).getId() == createdChuDe.getId()) {
										int viewRow = tblChuDe.convertRowIndexToView(i);
										selectAndScrollToRow(viewRow);
										break;
									}
								}
							});
						} else {
							showErrorMessage("Không thể thêm chủ đề. Vui lòng thử lại.");
						}
					} catch (Exception e) {
						showErrorMessage("Lỗi khi thêm chủ đề: " + e.getMessage());
						e.printStackTrace();
					}
				}
			};
			worker.execute();
		}
	}

	private void suaChuDe() {
		int selectedViewRow = tblChuDe.getSelectedRow();
		if (selectedViewRow == -1) {
			showWarningMessage("Vui lòng chọn một chủ đề để sửa.");
			return;
		}
		int modelRow = tblChuDe.convertRowIndexToModel(selectedViewRow);
		ChuDe chuDeToEdit = chuDeTableModel.getChuDeAt(modelRow);

		if (chuDeToEdit == null) {
			showErrorMessage("Không thể lấy dữ liệu chủ đề đã chọn.");
			return;
		}

		ChuDeDialog dialog = new ChuDeDialog((Frame) SwingUtilities.getWindowAncestor(this),
				"Sửa Chủ Đề: " + chuDeToEdit.getTenChuDe(), chuDeToEdit);
		dialog.setVisible(true);
		if (dialog.isSaved()) {
			ChuDe updatedChuDeData = dialog.getChuDe();
			SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
				@Override
				protected Boolean doInBackground() throws Exception {
					return chuDeDAO.update(updatedChuDeData);
				}

				@Override
				protected void done() {
					try {
						boolean success = get();
						if (success) {
							showSuccessMessage(
									"Cập nhật chủ đề \"" + updatedChuDeData.getTenChuDe() + "\" thành công!");
							loadChuDeData();
							SwingUtilities.invokeLater(() -> {
								for (int i = 0; i < chuDeTableModel.getRowCount(); i++) {
									if (chuDeTableModel.getChuDeAt(i).getId() == updatedChuDeData.getId()) {
										int viewRow = tblChuDe.convertRowIndexToView(i);
										selectAndScrollToRow(viewRow);
										break;
									}
								}
							});
						} else {
							showErrorMessage("Không thể cập nhật chủ đề. Có thể chủ đề đã bị xóa hoặc có lỗi xảy ra.");
						}
					} catch (Exception e) {
						showErrorMessage("Lỗi khi cập nhật chủ đề: " + e.getMessage());
						e.printStackTrace();
					}
				}
			};
			worker.execute();
		}
	}

	private void xoaChuDe() {
		int selectedViewRow = tblChuDe.getSelectedRow();
		if (selectedViewRow == -1) {
			showWarningMessage("Vui lòng chọn một chủ đề để xóa.");
			return;
		}
		int modelRow = tblChuDe.convertRowIndexToModel(selectedViewRow);
		ChuDe chuDeToDelete = chuDeTableModel.getChuDeAt(modelRow);

		if (chuDeToDelete == null) {
			showErrorMessage("Không thể lấy dữ liệu chủ đề đã chọn để xóa.");
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this, String.format(
				"Bạn có chắc chắn muốn xóa chủ đề:\nID: %d\nTên: %s\n\nLưu ý: Hành động này không thể hoàn tác và có thể ảnh hưởng đến dữ liệu liên quan (câu hỏi, đề thi).",
				chuDeToDelete.getId(), chuDeToDelete.getTenChuDe()), "Xác Nhận Xóa Chủ Đề", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);

		if (confirm == JOptionPane.YES_OPTION) {
			SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
				@Override
				protected Boolean doInBackground() throws Exception {
					return chuDeDAO.delete(chuDeToDelete.getId());
				}

				@Override
				protected void done() {
					try {
						boolean success = get();
						if (success) {
							showSuccessMessage("Xóa chủ đề \"" + chuDeToDelete.getTenChuDe() + "\" thành công!");
							loadChuDeData();
						} else {
							showErrorMessage(
									"Không thể xóa chủ đề.\nCó thể do chủ đề không còn tồn tại hoặc có dữ liệu liên quan không cho phép xóa.");
						}
					} catch (Exception e) {
						if (e.getCause() instanceof java.sql.SQLIntegrityConstraintViolationException
								|| (e.getMessage() != null
										&& e.getMessage().toLowerCase().contains("foreign key constraint fails"))) {
							showErrorMessage(
									"Không thể xóa chủ đề này vì vẫn còn câu hỏi hoặc đề thi được liên kết với nó.");
						} else {
							showErrorMessage("Lỗi khi xóa chủ đề: " + e.getMessage());
						}
						e.printStackTrace();
					}
				}
			};
			worker.execute();
		}
	}

	private void selectAndScrollToRow(int viewRow) {
		if (viewRow >= 0 && viewRow < tblChuDe.getRowCount()) {
			tblChuDe.setRowSelectionInterval(viewRow, viewRow);
			tblChuDe.scrollRectToVisible(tblChuDe.getCellRect(viewRow, 0, true));
		}
	}

	private void showSuccessMessage(String message) {
		JOptionPane.showMessageDialog(this, message, "Thành công", JOptionPane.INFORMATION_MESSAGE);
	}

	private void showWarningMessage(String message) {
		JOptionPane.showMessageDialog(this, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
	}

	private void showErrorMessage(String message) {
		JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
	}

	// Icon paths (đặt trong src/main/resources/icons/):
	// - chude_title.png (ví dụ 28x28)
	// - add_plus.png (ví dụ 16x16)
	// - edit.png (ví dụ 16x16)
	// - delete_trash.png (ví dụ 16x16)
	// - refresh.png (ví dụ 16x16)
}