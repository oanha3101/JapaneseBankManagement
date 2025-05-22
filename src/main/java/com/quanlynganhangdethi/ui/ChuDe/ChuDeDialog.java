// src/main/java/com/quanlynganhangdethi/ui/ChuDe/ChuDeDialog.java
package com.quanlynganhangdethi.ui.ChuDe;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.quanlynganhangdethi.models.ChuDe;

public class ChuDeDialog extends JDialog {
	private JTextField txtTenChuDe;
	private JTextArea txtMoTa;
	private JButton btnSave;
	private JButton btnCancel;

	private ChuDe chuDe;
	private boolean saved = false;

	// Fonts
	private Font FONT_LABEL = UIManager.getFont("Label.font") != null ? UIManager.getFont("Label.font")
			: new Font("Segoe UI", Font.PLAIN, 14);
	private Font FONT_TEXT_FIELD = UIManager.getFont("TextField.font") != null ? UIManager.getFont("TextField.font")
			: new Font("Segoe UI", Font.PLAIN, 14);
	private Font FONT_BUTTON = UIManager.getFont("Button.font") != null
			? UIManager.getFont("Button.font").deriveFont(Font.BOLD)
			: new Font("Segoe UI", Font.BOLD, 13);

	public ChuDeDialog(Frame parent, String title, ChuDe chuDeToEdit) {
		super(parent, title, true);
		this.chuDe = chuDeToEdit;

		// Áp dụng font nếu UIManager trả về null
		if (FONT_LABEL == null)
			FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 14);
		if (FONT_TEXT_FIELD == null)
			FONT_TEXT_FIELD = new Font("Segoe UI", Font.PLAIN, 14);
		if (FONT_BUTTON == null)
			FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);

		initComponents();
		setPreferredSize(new Dimension(500, 320)); // Kích thước ưu tiên cho dialog
		pack();
		setMinimumSize(new Dimension(450, 300)); // Kích thước tối thiểu
		setLocationRelativeTo(parent);

		if (chuDeToEdit != null) {
			loadChuDeData();
		}

		// Đặt nút Lưu làm nút mặc định (nhấn Enter sẽ kích hoạt)
		getRootPane().setDefaultButton(btnSave);

		// Cho phép đóng dialog bằng phím ESC
		getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	private void initComponents() {
		// Sử dụng màu nền của dialog từ UIManager
		getContentPane().setBackground(UIManager.getColor("Panel.background"));
		setLayout(new BorderLayout(10, 10));
		((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15)); // Padding cho toàn dialog

		// Panel chứa các trường nhập liệu
		JPanel formPanel = new JPanel(new GridBagLayout());
		formPanel.setOpaque(false); // Để màu nền của contentPane hiển thị
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 8, 8, 8);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;

		// Tên Chủ Đề
		JLabel lblTenChuDe = new JLabel("Tên Chủ Đề:");
		lblTenChuDe.setFont(FONT_LABEL);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.2; // Phân bổ không gian cho label
		formPanel.add(lblTenChuDe, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0.8; // Phân bổ không gian nhiều hơn cho text field
		txtTenChuDe = new JTextField(30);
		txtTenChuDe.setFont(FONT_TEXT_FIELD);
		txtTenChuDe.setPreferredSize(new Dimension(0, 32)); // Chiều cao ưu tiên
		formPanel.add(txtTenChuDe, gbc);

		// Mô Tả
		JLabel lblMoTa = new JLabel("Mô Tả:");
		lblMoTa.setFont(FONT_LABEL);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 0.2;
		formPanel.add(lblMoTa, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 0.8;
		gbc.weighty = 1.0; // Cho phép JTextArea mở rộng theo chiều dọc
		gbc.fill = GridBagConstraints.BOTH;
		txtMoTa = new JTextArea(5, 30);
		txtMoTa.setFont(FONT_TEXT_FIELD);
		txtMoTa.setLineWrap(true); // Tự động xuống dòng
		txtMoTa.setWrapStyleWord(true); // Xuống dòng theo từ
		JScrollPane scrollPaneMoTa = new JScrollPane(txtMoTa);
		scrollPaneMoTa.setPreferredSize(new Dimension(0, 100)); // Chiều cao ưu tiên cho JScrollPane
		formPanel.add(scrollPaneMoTa, gbc);

		add(formPanel, BorderLayout.CENTER);

		// Panel chứa các nút Save và Cancel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setOpaque(false);
		buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0)); // Padding trên cho button panel

		Dimension buttonSize = new Dimension(100, 36);

		btnSave = new JButton("Lưu");
		btnSave.setFont(FONT_BUTTON);
		btnSave.setPreferredSize(buttonSize);
		btnSave.putClientProperty("JButton.buttonType", "default"); // Đánh dấu là nút chính (FlatLaf)
		// btnSave.setIcon(createScaledIcon("/icons/save.png", 16, 16));
		btnSave.addActionListener(e -> saveChuDe());

		btnCancel = new JButton("Hủy");
		btnCancel.setFont(FONT_BUTTON);
		btnCancel.setPreferredSize(buttonSize);
		// btnCancel.setIcon(createScaledIcon("/icons/cancel.png", 16, 16));
		btnCancel.addActionListener(e -> dispose());

		buttonPanel.add(btnSave);
		buttonPanel.add(btnCancel);

		add(buttonPanel, BorderLayout.SOUTH);
	}

	// Helper method to create scaled icons (tùy chọn)
	private ImageIcon createScaledIcon(String path, int width, int height) {
		try {
			ImageIcon originalIcon = new ImageIcon(getClass().getResource(path));
			if (originalIcon.getImage() != null) {
				Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
				return new ImageIcon(scaledImage);
			}
		} catch (Exception e) {
			System.err.println("Lỗi tải icon " + path + ": " + e.getMessage());
		}
		return null; // Hoặc một icon placeholder
	}

	private void loadChuDeData() {
		if (chuDe != null) {
			txtTenChuDe.setText(chuDe.getTenChuDe());
			txtMoTa.setText(chuDe.getMoTa() != null ? chuDe.getMoTa() : "");
		}
	}

	private void saveChuDe() {
		String tenChuDe = txtTenChuDe.getText().trim();
		String moTa = txtMoTa.getText().trim();

		if (tenChuDe.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Tên chủ đề không được để trống!", "Lỗi Nhập Liệu",
					JOptionPane.WARNING_MESSAGE);
			txtTenChuDe.requestFocusInWindow();
			return;
		}

		// Logic kiểm tra tên chủ đề đã tồn tại hay chưa (NÊN THÊM VÀO)
		// Ví dụ:
		// ChuDeDAO dao = new ChuDeDAOImpl();
		// try {
		// if (dao.isTenChuDeExists(tenChuDe, (this.chuDe != null ? this.chuDe.getId() :
		// -1))) {
		// JOptionPane.showMessageDialog(this, "Tên chủ đề này đã tồn tại. Vui lòng chọn
		// tên khác.", "Lỗi Trùng Lặp", JOptionPane.WARNING_MESSAGE);
		// txtTenChuDe.requestFocusInWindow();
		// return;
		// }
		// } catch (SQLException ex) {
		// JOptionPane.showMessageDialog(this, "Lỗi khi kiểm tra tên chủ đề: " +
		// ex.getMessage(), "Lỗi Cơ Sở Dữ Liệu", JOptionPane.ERROR_MESSAGE);
		// return;
		// }

		if (this.chuDe == null) {
			this.chuDe = new ChuDe(); // Khởi tạo đối tượng mới
		}
		// Luôn set các giá trị mới, dù là thêm mới hay sửa
		this.chuDe.setTenChuDe(tenChuDe);
		this.chuDe.setMoTa(moTa);

		this.saved = true;
		dispose();
	}

	public ChuDe getChuDe() {
		return this.chuDe;
	}

	public boolean isSaved() {
		return this.saved;
	}
}