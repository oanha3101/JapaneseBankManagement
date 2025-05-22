// src/main/java/com/quanlynganhangdethi/ui/taodethithu/TaoDeThiThuPanel.java
package com.quanlynganhangdethi.ui.taodethithu;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.quanlynganhangdethi.models.ChuDe;
import com.quanlynganhangdethi.service.CauHoiService; // Cần thiết cho DeThiGeneratorService
import com.quanlynganhangdethi.service.ChuDeService;
import com.quanlynganhangdethi.service.DeThiGeneratorService;
import com.quanlynganhangdethi.service.DeThiGeneratorService.OutputType;

public class TaoDeThiThuPanel extends JPanel {
	private JSpinner spSoLuongDe;
	private JTextField txtThuMucLuu;
	private JButton btnChonThuMuc;
	private JButton btnTaoDe;
	private JProgressBar progressBar;
	private JPanel pnlTieuChiCauHoiNoiDung; // Panel chứa các dòng tiêu chí
	private JComboBox<OutputType> cmbOutputType;

	private ChuDeService chuDeService;
	private DeThiGeneratorService deThiGeneratorService;

	private List<TieuChiComponent> danhSachTieuChiComponents = new ArrayList<>();

	// Fonts
	private Font FONT_PANEL_TITLE;
	private Font FONT_BUTTON;
	private Font FONT_LABEL;
	private Font FONT_TEXT_FIELD; // Font cho JTextField, JSpinner
	private Font FONT_COMBO_BOX;

	// Lớp nội TieuChiComponent
	private static class TieuChiComponent {
		JLabel lblChuDeDisplay; // Chỉ hiển thị tên chủ đề
		JComboBox<String> cmbLoaiCauHoiTrongTieuChi;
		JSpinner spSoLuongCau;
		ChuDe chuDeObject; // Lưu trữ đối tượng ChuDe đầy đủ

		public TieuChiComponent(ChuDe chuDe, Font labelFont, Font controlFont) {
			this.chuDeObject = chuDe;
			this.lblChuDeDisplay = new JLabel(chuDe.getTenChuDe() + ":");
			this.lblChuDeDisplay.setFont(labelFont);
			this.lblChuDeDisplay.setPreferredSize(new Dimension(200, 30)); // Đảm bảo đủ rộng

			String[] cacLoaiCauHoi = { "Tất cả Loại", "Trắc nghiệm", "Tự luận", "Điền khuyết", "Nghe (Trắc nghiệm)" };
			this.cmbLoaiCauHoiTrongTieuChi = new JComboBox<>(cacLoaiCauHoi);
			this.cmbLoaiCauHoiTrongTieuChi.setFont(controlFont);
			this.cmbLoaiCauHoiTrongTieuChi.setSelectedItem("Tất cả Loại");
			this.cmbLoaiCauHoiTrongTieuChi.setPreferredSize(new Dimension(150, 30));

			this.spSoLuongCau = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
			this.spSoLuongCau.setFont(controlFont);
			this.spSoLuongCau.setPreferredSize(new Dimension(70, 30));
		}
	}

	public TaoDeThiThuPanel() {
		this.chuDeService = new ChuDeService();
		// DeThiGeneratorService cần CauHoiService, đảm bảo nó được truyền vào
		this.deThiGeneratorService = new DeThiGeneratorService(new CauHoiService());

		initializeFonts();

		setLayout(new BorderLayout(15, 15)); // Khoảng cách giữa các vùng
		setBorder(new EmptyBorder(15, 20, 15, 20)); // Padding tổng thể
		setBackground(UIManager.getColor("Panel.background"));

		initComponents();
		loadDanhSachChuDeChoTieuChi();
	}

	private void initializeFonts() {
		FONT_PANEL_TITLE = UIManager.getFont("h1.font");
		if (FONT_PANEL_TITLE == null) {
			Font labelFont = UIManager.getFont("Label.font");
			FONT_PANEL_TITLE = (labelFont != null) ? labelFont.deriveFont(Font.BOLD, 22f)
					: new Font("SansSerif", Font.BOLD, 22);
		}

		FONT_BUTTON = UIManager.getFont("Button.font");
		FONT_BUTTON = (FONT_BUTTON != null) ? FONT_BUTTON.deriveFont(Font.BOLD, 13f)
				: new Font("SansSerif", Font.BOLD, 13);

		FONT_LABEL = UIManager.getFont("Label.font");
		if (FONT_LABEL == null)
			FONT_LABEL = new Font("SansSerif", Font.PLAIN, 14);

		FONT_TEXT_FIELD = UIManager.getFont("TextField.font");
		if (FONT_TEXT_FIELD == null)
			FONT_TEXT_FIELD = new Font("SansSerif", Font.PLAIN, 14);

		FONT_COMBO_BOX = UIManager.getFont("ComboBox.font");
		if (FONT_COMBO_BOX == null)
			FONT_COMBO_BOX = new Font("SansSerif", Font.PLAIN, 14);
	}

	private void initComponents() {
		// --- Panel Tiêu đề ---
		add(createHeaderPanel(), BorderLayout.NORTH);

		// --- Panel Cài đặt chung (Số lượng đề, Loại output, Thư mục lưu) ---
		JPanel generalSettingsPanel = createGeneralSettingsPanel();

		// --- Panel Tiêu chí Câu hỏi ---
		JPanel tieuChiPanel = createTieuChiPanel();

		// --- Panel Nút Tạo và Progress Bar ---
		JPanel actionPanel = createActionPanel();

		// Gộp generalSettingsPanel và tieuChiPanel vào một panel trung gian để đặt ở
		// CENTER
		JPanel centerContainer = new JPanel(new BorderLayout(0, 15)); // Khoảng cách dọc
		centerContainer.setOpaque(false);
		centerContainer.add(generalSettingsPanel, BorderLayout.NORTH);
		centerContainer.add(tieuChiPanel, BorderLayout.CENTER);

		add(centerContainer, BorderLayout.CENTER);
		add(actionPanel, BorderLayout.SOUTH);
	}

	private JPanel createHeaderPanel() {
		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		headerPanel.setOpaque(false);
		headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

		JLabel lblTitle = new JLabel("TẠO ĐỀ THI THỬ NGẪU NHIÊN");
		lblTitle.setFont(FONT_PANEL_TITLE);
		lblTitle.setForeground(UIManager.getColor("Label.foreground"));
		headerPanel.add(lblTitle);
		return headerPanel;
	}

	private JPanel createGeneralSettingsPanel() {
		JPanel topPanel = new JPanel(new GridBagLayout());
		topPanel.setOpaque(false);
		topPanel.setBorder(BorderFactory.createCompoundBorder(createStyledTitledBorder("Cài đặt chung"),
				new EmptyBorder(10, 10, 10, 10)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Số lượng đề
		JLabel lblSoLuongDe = new JLabel("Số lượng đề cần tạo:");
		lblSoLuongDe.setFont(FONT_LABEL);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.gridwidth = 1;
		topPanel.add(lblSoLuongDe, gbc);

		spSoLuongDe = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
		spSoLuongDe.setFont(FONT_TEXT_FIELD);
		spSoLuongDe.setPreferredSize(new Dimension(80, 30));
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0.2;
		topPanel.add(spSoLuongDe, gbc);

		// Loại Output
		JLabel lblOutputType = new JLabel("Loại File Xuất:");
		lblOutputType.setFont(FONT_LABEL);
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.weightx = 0;
		topPanel.add(lblOutputType, gbc);

		cmbOutputType = new JComboBox<>(DeThiGeneratorService.OutputType.values());
		cmbOutputType.setFont(FONT_COMBO_BOX);
		cmbOutputType.setPreferredSize(new Dimension(250, 30));
		cmbOutputType.setSelectedItem(DeThiGeneratorService.OutputType.DE_THI_PDF_DAP_AN_TXT);
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.weightx = 0.8;
		topPanel.add(cmbOutputType, gbc);

		// Thư mục lưu
		JLabel lblThuMucLuu = new JLabel("Thư mục lưu:");
		lblThuMucLuu.setFont(FONT_LABEL);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		topPanel.add(lblThuMucLuu, gbc);

		txtThuMucLuu = new JTextField(30);
		txtThuMucLuu.setFont(FONT_TEXT_FIELD);
		txtThuMucLuu.setEditable(false);
		txtThuMucLuu.setPreferredSize(new Dimension(0, 30)); // Chiều rộng sẽ do weightx quyết định
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		topPanel.add(txtThuMucLuu, gbc);
		gbc.gridwidth = 1;

		btnChonThuMuc = new JButton("Chọn...");
		btnChonThuMuc.setFont(FONT_BUTTON);
		btnChonThuMuc.setPreferredSize(new Dimension(90, 30));
		btnChonThuMuc.addActionListener(e -> chonThuMucLuu());
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		topPanel.add(btnChonThuMuc, gbc);

		return topPanel;
	}

	private JPanel createTieuChiPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		panel.setBorder(createStyledTitledBorder("Chọn số lượng câu hỏi theo tiêu chí"));

		pnlTieuChiCauHoiNoiDung = new JPanel();
		pnlTieuChiCauHoiNoiDung.setLayout(new BoxLayout(pnlTieuChiCauHoiNoiDung, BoxLayout.Y_AXIS));
		pnlTieuChiCauHoiNoiDung.setOpaque(false);
		pnlTieuChiCauHoiNoiDung.setBorder(new EmptyBorder(10, 10, 10, 10));

		JScrollPane scrollPane = new JScrollPane(pnlTieuChiCauHoiNoiDung);
		scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Bỏ border của scrollpane
		scrollPane.getViewport().setBackground(UIManager.getColor("Panel.background"));
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createActionPanel() {
		JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
		bottomPanel.setOpaque(false);
		bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0)); // Padding trên

		btnTaoDe = new JButton("Bắt Đầu Tạo Đề Thi");
		btnTaoDe.setFont(FONT_BUTTON.deriveFont(15f)); // Font to hơn cho nút chính
		btnTaoDe.setPreferredSize(new Dimension(0, 45)); // Chiều cao lớn hơn
		btnTaoDe.putClientProperty("JButton.buttonType", "default");
		btnTaoDe.addActionListener(e -> batDauTaoDe());
		bottomPanel.add(btnTaoDe, BorderLayout.CENTER);

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setFont(UIManager.getFont("ProgressBar.font") != null ? UIManager.getFont("ProgressBar.font")
				: new Font("SansSerif", Font.PLAIN, 12));
		progressBar.setPreferredSize(new Dimension(0, 25));
		bottomPanel.add(progressBar, BorderLayout.SOUTH);

		return bottomPanel;
	}

	// Helper để tạo TitledBorder đồng nhất
	private TitledBorder createStyledTitledBorder(String title) {
		Font borderTitleFont = FONT_LABEL.deriveFont(Font.BOLD);
		return BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")), title,
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, borderTitleFont,
				UIManager.getColor("TitledBorder.titleColor"));
	}

	private void loadDanhSachChuDeChoTieuChi() {
		SwingWorker<List<ChuDe>, Void> worker = new SwingWorker<List<ChuDe>, Void>() {
			@Override
			protected List<ChuDe> doInBackground() throws Exception {
				return chuDeService.getAllChuDe();
			}

			@Override
			protected void done() {
				try {
					List<ChuDe> danhSachChuDeFull = get();
					pnlTieuChiCauHoiNoiDung.removeAll();
					danhSachTieuChiComponents.clear();

					if (danhSachChuDeFull != null && !danhSachChuDeFull.isEmpty()) {
						for (ChuDe chuDe : danhSachChuDeFull) {
							if (chuDe == null || chuDe.getId() == 0)
								continue;

							TieuChiComponent tcComp = new TieuChiComponent(chuDe, FONT_LABEL, FONT_TEXT_FIELD);
							danhSachTieuChiComponents.add(tcComp);

							JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
							rowPanel.setOpaque(false);
							rowPanel.add(tcComp.lblChuDeDisplay);

							JLabel lblLoai = new JLabel("Loại:");
							lblLoai.setFont(FONT_LABEL);
							rowPanel.add(lblLoai);
							rowPanel.add(tcComp.cmbLoaiCauHoiTrongTieuChi);

							JLabel lblSoCau = new JLabel("Số câu:");
							lblSoCau.setFont(FONT_LABEL);
							rowPanel.add(lblSoCau);
							rowPanel.add(tcComp.spSoLuongCau);
							pnlTieuChiCauHoiNoiDung.add(rowPanel);
						}
					} else {
						JLabel noDataLabel = new JLabel("Không có chủ đề nào trong CSDL hoặc không thể tải.");
						noDataLabel.setFont(FONT_LABEL);
						noDataLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
						pnlTieuChiCauHoiNoiDung.add(noDataLabel);
					}
					pnlTieuChiCauHoiNoiDung.revalidate();
					pnlTieuChiCauHoiNoiDung.repaint();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(TaoDeThiThuPanel.this, "Lỗi tải danh sách chủ đề: " + e.getMessage(),
							"Lỗi", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
		};
		worker.execute();
	}

	private void chonThuMucLuu() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Chọn thư mục để lưu đề thi");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// fileChooser.setAcceptAllFileFilterUsed(false); // Có thể giữ lại nếu muốn
		if (txtThuMucLuu.getText() != null && !txtThuMucLuu.getText().trim().isEmpty()) {
			File currentDir = new File(txtThuMucLuu.getText().trim());
			if (currentDir.exists() && currentDir.isDirectory()) {
				fileChooser.setCurrentDirectory(currentDir);
			}
		}

		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			txtThuMucLuu.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void batDauTaoDe() {
		String thuMucLuuStr = txtThuMucLuu.getText().trim();
		if (thuMucLuuStr.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn thư mục lưu trữ đề thi.", "Thiếu thông tin",
					JOptionPane.WARNING_MESSAGE);
			btnChonThuMuc.requestFocusInWindow();
			return;
		}
		File thuMucLuuFile = new File(thuMucLuuStr);
		if (!thuMucLuuFile.exists() || !thuMucLuuFile.isDirectory()) {
			JOptionPane.showMessageDialog(this, "Thư mục lưu không hợp lệ. Vui lòng chọn lại.", "Lỗi Thư Mục",
					JOptionPane.ERROR_MESSAGE);
			btnChonThuMuc.requestFocusInWindow();
			return;
		}

		int soLuongDe = (Integer) spSoLuongDe.getValue();
		if (soLuongDe <= 0) {
			JOptionPane.showMessageDialog(this, "Số lượng đề cần tạo phải lớn hơn 0.", "Số lượng không hợp lệ",
					JOptionPane.WARNING_MESSAGE);
			spSoLuongDe.requestFocusInWindow();
			return;
		}

		OutputType selectedOutputType = (OutputType) cmbOutputType.getSelectedItem();
		if (selectedOutputType == null) { // Mặc dù JComboBox thường không cho phép null nếu có item
			JOptionPane.showMessageDialog(this, "Vui lòng chọn Loại File Xuất.", "Thiếu thông tin",
					JOptionPane.WARNING_MESSAGE);
			cmbOutputType.requestFocusInWindow();
			return;
		}

		List<DeThiGeneratorService.TieuChiChonCauHoi> danhSachTieuChi = new ArrayList<>();
		int tongSoCauYeuCau = 0;
		for (TieuChiComponent tcComp : danhSachTieuChiComponents) {
			int soLuongCau = (Integer) tcComp.spSoLuongCau.getValue();
			if (soLuongCau > 0) {
				if (tcComp.chuDeObject == null || tcComp.chuDeObject.getId() == 0) {
					JOptionPane.showMessageDialog(this,
							"Có lỗi với dữ liệu chủ đề: " + tcComp.lblChuDeDisplay.getText()
									+ ". Vui lòng tải lại danh sách chủ đề.",
							"Lỗi Dữ Liệu Chủ Đề", JOptionPane.ERROR_MESSAGE);
					return; // Dừng nếu có lỗi dữ liệu chủ đề
				}
				String loaiCH = (String) tcComp.cmbLoaiCauHoiTrongTieuChi.getSelectedItem();
				if ("Tất cả Loại".equalsIgnoreCase(loaiCH)) {
					loaiCH = null;
				}
				danhSachTieuChi.add(
						new DeThiGeneratorService.TieuChiChonCauHoi(tcComp.chuDeObject.getId(), loaiCH, soLuongCau));
				tongSoCauYeuCau += soLuongCau;
			}
		}

		if (danhSachTieuChi.isEmpty() || tongSoCauYeuCau == 0) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một tiêu chí với số lượng câu lớn hơn 0.",
					"Thiếu Tiêu Chí", JOptionPane.WARNING_MESSAGE);
			return;
		}

		btnTaoDe.setEnabled(false);
		progressBar.setValue(0);
		// progressBar.setVisible(true); // Không cần thiết nếu nó luôn visible

		SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
			@Override
			protected Void doInBackground() throws Exception {
				// Truyền JProgressBar vào service để nó cập nhật từ bên trong
				deThiGeneratorService.generateDeThiThu(danhSachTieuChi, soLuongDe, thuMucLuuStr, progressBar,
						selectedOutputType);
				return null;
			}

			@Override
			protected void done() {
				try {
					get(); // Gọi get() để bắt exception từ doInBackground nếu có
					JOptionPane.showMessageDialog(TaoDeThiThuPanel.this, "Hoàn tất tạo " + soLuongDe + " đề thi!",
							"Hoàn thành", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					Throwable cause = e.getCause() != null ? e.getCause() : e;
					JOptionPane.showMessageDialog(
							TaoDeThiThuPanel.this, "Có lỗi xảy ra trong quá trình tạo đề thi:\n"
									+ cause.getClass().getSimpleName() + ": " + cause.getMessage(),
							"Lỗi Tạo Đề Thi", JOptionPane.ERROR_MESSAGE);
					System.err.println("Lỗi trong SwingWorker done() của TaoDeThiThuPanel:");
					cause.printStackTrace();
				} finally {
					btnTaoDe.setEnabled(true);
					progressBar.setValue(0); // Reset progress bar
				}
			}
		};
		worker.execute();
	}
}