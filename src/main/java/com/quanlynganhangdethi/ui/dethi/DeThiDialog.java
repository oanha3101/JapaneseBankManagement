// src/main/java/com/quanlynganhangdethi/ui/dethi/DeThiDialog.java
package com.quanlynganhangdethi.ui.dethi;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.quanlynganhangdethi.models.DeThi;

public class DeThiDialog extends JDialog {
	private JTextField txtTieuDe;
	private JTextField txtNguoiTao;
	private JButton btnLuu;
	private JButton btnHuy;

	private DeThi deThiHienTai; // null nếu thêm mới
	private boolean daLuu = false;

	public DeThiDialog(Frame parent, String title, DeThi deThiToEdit) {
		super(parent, title, true); // Modal dialog
		this.deThiHienTai = deThiToEdit;

		initComponents();
		pack();
		setLocationRelativeTo(parent);

		if (deThiHienTai != null) {
			txtTieuDe.setText(deThiHienTai.getTieuDe());
			txtNguoiTao.setText(deThiHienTai.getNguoiTao());
		}
	}

	private void initComponents() {
		setLayout(new BorderLayout(10, 10));

		JPanel formPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;

		// Tiêu đề
		gbc.gridx = 0;
		gbc.gridy = 0;
		formPanel.add(new JLabel("Tiêu đề đề thi:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		txtTieuDe = new JTextField(30);
		formPanel.add(txtTieuDe, gbc);

		// Người tạo
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		formPanel.add(new JLabel("Người tạo:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		txtNguoiTao = new JTextField(30);
		formPanel.add(txtNguoiTao, gbc);

		add(formPanel, BorderLayout.CENTER);

		// Panel nút
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnLuu = new JButton("Lưu");
		btnHuy = new JButton("Hủy");

		btnLuu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				luuThongTinDeThi();
			}
		});
		btnHuy.addActionListener(e -> {
			daLuu = false;
			dispose();
		});

		buttonPanel.add(btnLuu);
		buttonPanel.add(btnHuy);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void luuThongTinDeThi() {
		String tieuDe = txtTieuDe.getText().trim();
		String nguoiTao = txtNguoiTao.getText().trim();

		if (tieuDe.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Tiêu đề không được để trống.", "Lỗi Nhập Liệu",
					JOptionPane.ERROR_MESSAGE);
			txtTieuDe.requestFocus();
			return;
		}
		// Người tạo có thể để trống hoặc không tùy yêu cầu

		if (deThiHienTai == null) { // Thêm mới
			deThiHienTai = new DeThi(); // ngayTao sẽ được set bởi DB hoặc Service
		}
		deThiHienTai.setTieuDe(tieuDe);
		deThiHienTai.setNguoiTao(nguoiTao);

		daLuu = true;
		dispose();
	}

	public DeThi getDeThi() {
		return daLuu ? deThiHienTai : null;
	}

	public boolean isDaLuu() {
		return daLuu;
	}
}