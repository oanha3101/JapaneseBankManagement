// src/main/java/com/quanlynganhangdethi/ui/cauhoi/DapAnDialog.java
package com.quanlynganhangdethi.ui.CauHoi;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.quanlynganhangdethi.models.DapAn;

public class DapAnDialog extends JDialog {
	private JTextArea txtNoiDungDapAn;
	private JCheckBox chkLaDapAnDung;
	private JButton btnSave;
	private JButton btnCancel;

	private DapAn dapAn;
	private boolean saved = false;
	private boolean chiMotDapAnDung; // Biến để kiểm soát logic chỉ 1 đáp án đúng

	public DapAnDialog(Dialog parent, String title, DapAn dapAnToEdit, boolean chiMotDapAnDung) {
		super(parent, title, true);
		this.dapAn = dapAnToEdit;
		this.chiMotDapAnDung = chiMotDapAnDung;

		initComponents();
		pack();
		setLocationRelativeTo(parent);

		if (dapAnToEdit != null) {
			txtNoiDungDapAn.setText(dapAnToEdit.getNoiDung());
			chkLaDapAnDung.setSelected(dapAnToEdit.isLaDapAnDung());
		}
	}

	private void initComponents() {
		setLayout(new BorderLayout(10, 10));
		JPanel formPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		formPanel.add(new JLabel("Nội dung đáp án:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		txtNoiDungDapAn = new JTextArea(3, 25);
		formPanel.add(new JScrollPane(txtNoiDungDapAn), gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		chkLaDapAnDung = new JCheckBox("Là đáp án đúng");
		formPanel.add(chkLaDapAnDung, gbc);
		add(formPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnSave = new JButton("Lưu");
		btnCancel = new JButton("Hủy");
		btnSave.addActionListener(e -> saveDapAn());
		btnCancel.addActionListener(e -> dispose());
		buttonPanel.add(btnSave);
		buttonPanel.add(btnCancel);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void saveDapAn() {
		String noiDung = txtNoiDungDapAn.getText().trim();
		if (noiDung.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Nội dung đáp án không được để trống.", "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			txtNoiDungDapAn.requestFocus();
			return;
		}
		boolean isDung = chkLaDapAnDung.isSelected();

		if (this.dapAn == null) { // Thêm mới
			// idCauHoi sẽ được set sau khi câu hỏi chính được lưu
			this.dapAn = new DapAn(0, noiDung, isDung);
		} else { // Sửa
			this.dapAn.setNoiDung(noiDung);
			this.dapAn.setLaDapAnDung(isDung);
		}
		this.saved = true;
		dispose();
	}

	public DapAn getDapAn() {
		return dapAn;
	}

	public boolean isSaved() {
		return saved;
	}

	public boolean isChiMotDapAnDung() {
		return chiMotDapAnDung;
	}
}