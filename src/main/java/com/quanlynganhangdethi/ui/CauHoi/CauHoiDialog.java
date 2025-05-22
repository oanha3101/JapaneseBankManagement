// src/main/java/com/quanlynganhangdethi/ui/cauhoi/CauHoiDialog.java
// Gói của bạn có thể là com.quanlynganhangdethi.ui.cauhoi (viết thường)
package com.quanlynganhangdethi.ui.CauHoi; // Giữ nguyên package của bạn nếu đã thống nhất

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
// import javax.swing.JCheckBox; // Không thấy dùng trực tiếp
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.quanlynganhangdethi.dao.ChuDeDAO;
import com.quanlynganhangdethi.dao.ChuDeDAOImpl;
import com.quanlynganhangdethi.models.CauHoi;
import com.quanlynganhangdethi.models.ChuDe;
import com.quanlynganhangdethi.models.DapAn;
import com.quanlynganhangdethi.service.CauHoiService; // Để truy cập hằng số AUDIO_STORAGE_BASE_DIR_NAME

// Bạn cần tạo lớp DapAnTableModel nếu chưa có
// import com.quanlynganhangdethi.ui.cauhoi.DapAnTableModel;
// Bạn cần tạo lớp DapAnDialog nếu chưa có
// import com.quanlynganhangdethi.ui.cauhoi.DapAnDialog;

public class CauHoiDialog extends JDialog {
	private JTextArea txtNoiDungCauHoi;
	private JComboBox<String> cmbLoaiCauHoi;
	private JComboBox<Integer> cmbDoKho;
	private JComboBox<ChuDe> cmbChuDe;
	private JTextField txtAudioPathDisplay;
	private JButton btnBrowseAudio;
	private JButton btnPlayAudio;

	private JButton btnSaveCauHoi;
	private JButton btnCancelCauHoi;

	private JTable tblDapAn;
	private DapAnTableModel dapAnTableModel; // Đảm bảo lớp này đã được tạo
	private JButton btnThemDapAn;
	private JButton btnSuaDapAn;
	private JButton btnXoaDapAn;
	private JPanel pnlQuanLyDapAnTracNghiem;

	private JTextArea txtDapAnGoiYTuLuan;
	private JScrollPane scrollPaneDapAnGoiY;

	private JPanel answerContainerPanel;
	private CardLayout answerCardLayout;

	private CauHoi cauHoi;
	private String selectedAudioFileAbsolutePath;
	private boolean saved = false;
	private ChuDeDAO chuDeDAO;
	private Clip currentClip;

	public CauHoiDialog(Frame parent, String title, CauHoi cauHoiToEdit) {
		super(parent, title, true);
		this.cauHoi = (cauHoiToEdit != null) ? cloneCauHoi(cauHoiToEdit) : new CauHoi();
		if (this.cauHoi.getDapAnList() == null) {
			this.cauHoi.setDapAnList(new ArrayList<>());
		}
		this.chuDeDAO = new ChuDeDAOImpl();

		initComponents();
		loadInitialData();

		if (cauHoiToEdit != null) {
			loadCauHoiData(this.cauHoi);
		} else {
			// Khởi tạo DapAnTableModel với danh sách rỗng nếu thêm mới câu hỏi
			// và danh sách đáp án của câu hỏi mới cũng rỗng.
			if (this.dapAnTableModel == null) { // Khởi tạo nếu chưa có
				this.dapAnTableModel = new DapAnTableModel(this.cauHoi.getDapAnList());
				if (tblDapAn != null)
					tblDapAn.setModel(this.dapAnTableModel); // Gán lại model cho bảng
			} else {
				this.dapAnTableModel.setData(this.cauHoi.getDapAnList());
			}
			txtDapAnGoiYTuLuan.setText("");
			txtAudioPathDisplay.setText("");
		}

		updateDapAnSectionVisibility();
		updatePlayAudioButtonState();
		pack();
		setMinimumSize(new Dimension(680, 620)); // Tăng kích thước một chút
		setLocationRelativeTo(parent);
	}

	private CauHoi cloneCauHoi(CauHoi original) {
		if (original == null)
			return new CauHoi();
		CauHoi clone = new CauHoi();
		clone.setId(original.getId());
		clone.setNoiDung(original.getNoiDung());
		clone.setLoaiCauHoi(original.getLoaiCauHoi());
		clone.setDoKho(original.getDoKho());
		clone.setIdChuDe(original.getIdChuDe());
		clone.setAudioPath(original.getAudioPath());
		if (original.getDapAnList() != null) {
			List<DapAn> clonedDapAnList = new ArrayList<>();
			for (DapAn da : original.getDapAnList()) {
				DapAn daClone = new DapAn();
				daClone.setId(da.getId());
				daClone.setIdCauHoi(da.getIdCauHoi());
				daClone.setNoiDung(da.getNoiDung());
				daClone.setLaDapAnDung(da.isLaDapAnDung());
				clonedDapAnList.add(daClone);
			}
			clone.setDapAnList(clonedDapAnList);
		} else {
			clone.setDapAnList(new ArrayList<>());
		}
		return clone;
	}

	private void initComponents() {
		setLayout(new BorderLayout(10, 10));
		JPanel formCauHoiPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;

		gbc.gridx = 0;
		gbc.gridy = 0;
		formCauHoiPanel.add(new JLabel("Nội dung câu hỏi:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0;
		gbc.weighty = 0.3;
		gbc.fill = GridBagConstraints.BOTH;
		txtNoiDungCauHoi = new JTextArea(5, 40);
		formCauHoiPanel.add(new JScrollPane(txtNoiDungCauHoi), gbc);
		gbc.gridwidth = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 1;
		formCauHoiPanel.add(new JLabel("Loại câu hỏi:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		cmbLoaiCauHoi = new JComboBox<>(
				new String[] { "Trắc nghiệm", "Tự luận", "Điền khuyết", "Nghe (Trắc nghiệm)", "Sắp xếp câu" });
		cmbLoaiCauHoi.addActionListener(e -> updateDapAnSectionVisibility());
		formCauHoiPanel.add(cmbLoaiCauHoi, gbc);
		gbc.gridwidth = 1;

		gbc.gridx = 0;
		gbc.gridy = 2;
		formCauHoiPanel.add(new JLabel("Độ khó (1-5):"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		cmbDoKho = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5 });
		cmbDoKho.setSelectedIndex(1);
		formCauHoiPanel.add(cmbDoKho, gbc);
		gbc.gridwidth = 1;

		gbc.gridx = 0;
		gbc.gridy = 3;
		formCauHoiPanel.add(new JLabel("Chủ đề:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		cmbChuDe = new JComboBox<>();
		cmbChuDe.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof ChuDe)
					setText(((ChuDe) value).getTenChuDe());
				else if (value != null)
					setText(value.toString());
				return this;
			}
		});
		formCauHoiPanel.add(cmbChuDe, gbc);
		gbc.gridwidth = 1;

		gbc.gridx = 0;
		gbc.gridy = 4;
		formCauHoiPanel.add(new JLabel("File âm thanh:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.weightx = 0.7;
		txtAudioPathDisplay = new JTextField(20);
		txtAudioPathDisplay.setEditable(false);
		formCauHoiPanel.add(txtAudioPathDisplay, gbc);

		gbc.gridx = 2;
		gbc.gridy = 4;
		gbc.weightx = 0.15;
		gbc.fill = GridBagConstraints.NONE;
		btnBrowseAudio = new JButton("Browse...");
		btnBrowseAudio.addActionListener(e -> browseAudioFile());
		formCauHoiPanel.add(btnBrowseAudio, gbc);

		gbc.gridx = 3;
		gbc.gridy = 4;
		gbc.weightx = 0.15;
		btnPlayAudio = new JButton("Nghe thử");
		btnPlayAudio.setEnabled(false);
		btnPlayAudio.addActionListener(e -> playSelectedAudio());
		formCauHoiPanel.add(btnPlayAudio, gbc);
		gbc.weightx = 1.0;

		add(formCauHoiPanel, BorderLayout.NORTH);

		pnlQuanLyDapAnTracNghiem = new JPanel(new BorderLayout(5, 5));
		pnlQuanLyDapAnTracNghiem.setBorder(BorderFactory.createTitledBorder("Các Lựa Chọn Đáp Án (Trắc Nghiệm)"));
		dapAnTableModel = new DapAnTableModel(); // Khởi tạo trước
		tblDapAn = new JTable(dapAnTableModel);
		tblDapAn.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pnlQuanLyDapAnTracNghiem.add(new JScrollPane(tblDapAn), BorderLayout.CENTER);
		JPanel dapAnTracNghiemButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		btnThemDapAn = new JButton("Thêm Lựa Chọn");
		btnSuaDapAn = new JButton("Sửa Lựa Chọn");
		btnXoaDapAn = new JButton("Xóa Lựa Chọn");
		dapAnTracNghiemButtonPanel.add(btnThemDapAn);
		dapAnTracNghiemButtonPanel.add(btnSuaDapAn);
		dapAnTracNghiemButtonPanel.add(btnXoaDapAn);
		pnlQuanLyDapAnTracNghiem.add(dapAnTracNghiemButtonPanel, BorderLayout.SOUTH);
		btnThemDapAn.addActionListener(e -> themDapAnTracNghiem());
		btnSuaDapAn.addActionListener(e -> suaDapAnTracNghiem());
		btnXoaDapAn.addActionListener(e -> xoaDapAnTracNghiem());
		tblDapAn.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				boolean selected = tblDapAn.getSelectedRow() != -1;
				btnSuaDapAn.setEnabled(selected);
				btnXoaDapAn.setEnabled(selected);
			}
		});
		btnSuaDapAn.setEnabled(false);
		btnXoaDapAn.setEnabled(false);

		txtDapAnGoiYTuLuan = new JTextArea(5, 40);
		scrollPaneDapAnGoiY = new JScrollPane(txtDapAnGoiYTuLuan);
		scrollPaneDapAnGoiY.setBorder(BorderFactory.createTitledBorder("Đáp Án Tham Khảo / Gợi Ý"));

		answerCardLayout = new CardLayout();
		answerContainerPanel = new JPanel(answerCardLayout);
		answerContainerPanel.add(pnlQuanLyDapAnTracNghiem, "TRAC_NGHIEM");
		answerContainerPanel.add(scrollPaneDapAnGoiY, "TU_LUAN_GOI_Y");
		answerContainerPanel.add(new JPanel(), "EMPTY_ANSWER_PANEL");
		add(answerContainerPanel, BorderLayout.CENTER);

		JPanel mainButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnSaveCauHoi = new JButton("Lưu Câu Hỏi");
		btnCancelCauHoi = new JButton("Hủy");
		btnSaveCauHoi.addActionListener(e -> saveCauHoi());
		btnCancelCauHoi.addActionListener(e -> {
			saved = false;
			dispose();
		});
		mainButtonPanel.add(btnSaveCauHoi);
		mainButtonPanel.add(btnCancelCauHoi);
		add(mainButtonPanel, BorderLayout.SOUTH);
	}

	private void updateDapAnSectionVisibility() {
		String loaiCH = (String) cmbLoaiCauHoi.getSelectedItem();
		List<DapAn> currentDapAnList = (this.cauHoi != null && this.cauHoi.getDapAnList() != null)
				? this.cauHoi.getDapAnList()
				: new ArrayList<>();

		if ("Trắc nghiệm".equals(loaiCH) || "Nghe (Trắc nghiệm)".equals(loaiCH) || "Sắp xếp câu".equals(loaiCH)) {
			answerCardLayout.show(answerContainerPanel, "TRAC_NGHIEM");
			// Nếu chuyển từ loại khác sang trắc nghiệm, và dapAnList hiện tại có 1 phần tử
			// (có thể là gợi ý của tự luận)
			// thì xóa nó đi để bắt đầu với bảng trắc nghiệm rỗng (hoặc giữ lại nếu muốn
			// chuyển đổi)
			if (currentDapAnList.size() == 1 && !txtDapAnGoiYTuLuan.getText().isEmpty()
					&& txtDapAnGoiYTuLuan.getText().equals(currentDapAnList.get(0).getNoiDung())) {
				currentDapAnList.clear();
			}
			dapAnTableModel.setData(currentDapAnList);
		} else if ("Tự luận".equals(loaiCH) || "Điền khuyết".equals(loaiCH)) {
			answerCardLayout.show(answerContainerPanel, "TU_LUAN_GOI_Y");
			// Nếu chuyển từ trắc nghiệm sang, và dapAnList có nhiều hơn 1 (hoặc khác cấu
			// trúc gợi ý)
			// thì xóa đi để nhập gợi ý mới.
			if (currentDapAnList.size() > 1
					|| (currentDapAnList.size() == 1 && !currentDapAnList.get(0).isLaDapAnDung())) { // Giả định đáp án
																										// gợi ý luôn là
																										// isLaDapAnDung
																										// = true
				currentDapAnList.clear();
			}
			txtDapAnGoiYTuLuan.setText(!currentDapAnList.isEmpty() ? currentDapAnList.get(0).getNoiDung() : "");
		} else {
			answerCardLayout.show(answerContainerPanel, "EMPTY_ANSWER_PANEL");
		}
		this.pack(); // Gọi pack để dialog tự điều chỉnh kích thước
	}

	private void loadInitialData() {
		try {
			List<ChuDe> listChuDe = chuDeDAO.findAll();
			DefaultComboBoxModel<ChuDe> model = new DefaultComboBoxModel<>(new Vector<>(listChuDe));
			cmbChuDe.setModel(model);
		} catch (Exception e) { // Bắt Exception chung vì DAO có thể ném SQLException hoặc lỗi khác
			JOptionPane.showMessageDialog(this, "Lỗi tải danh sách chủ đề: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void loadCauHoiData(CauHoi ch) {
		txtNoiDungCauHoi.setText(ch.getNoiDung());
		cmbLoaiCauHoi.setSelectedItem(ch.getLoaiCauHoi()); // Điều này sẽ trigger updateDapAnSectionVisibility
		if (ch.getDoKho() != null)
			cmbDoKho.setSelectedItem(ch.getDoKho());

		txtAudioPathDisplay.setText(ch.getAudioPath() != null ? ch.getAudioPath() : "");
		this.selectedAudioFileAbsolutePath = null;

		if (ch.getIdChuDe() > 0) {
			ComboBoxModel<ChuDe> model = cmbChuDe.getModel();
			for (int i = 0; i < model.getSize(); i++) {
				if (model.getElementAt(i).getId() == ch.getIdChuDe()) {
					cmbChuDe.setSelectedIndex(i);
					break;
				}
			}
		}
		// updateDapAnSectionVisibility() đã được gọi khi cmbLoaiCauHoi.setSelectedItem.
		// Nó sẽ load dữ liệu đáp án phù hợp vào bảng hoặc JTextArea dựa trên
		// this.cauHoi.getDapAnList().
		updatePlayAudioButtonState();
	}

	private void browseAudioFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Chọn file âm thanh cho câu hỏi");
		FileNameExtensionFilter audioFilter = new FileNameExtensionFilter("Audio Files (*.mp3, *.wav, *.ogg, *.m4a)",
				"mp3", "wav", "ogg", "m4a");
		fileChooser.setFileFilter(audioFilter);
		fileChooser.setAcceptAllFileFilterUsed(false);

		int returnValue = fileChooser.showOpenDialog(this);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			this.selectedAudioFileAbsolutePath = selectedFile.getAbsolutePath();
			this.txtAudioPathDisplay.setText(selectedFile.getName());
			System.out.println("Đã chọn file audio mới: " + this.selectedAudioFileAbsolutePath);
			updatePlayAudioButtonState();
		}
	}

	private void updatePlayAudioButtonState() {
		boolean canPlay = false;
		String pathForCheck = null;

		if (selectedAudioFileAbsolutePath != null && !selectedAudioFileAbsolutePath.isEmpty()) {
			pathForCheck = selectedAudioFileAbsolutePath;
		} else if (cauHoi != null && cauHoi.getAudioPath() != null && !cauHoi.getAudioPath().isEmpty()) {
			// AUDIO_STORAGE_BASE_DIR_NAME là thư mục gốc (ví dụ "data")
			// cauHoi.getAudioPath() là đường dẫn tương đối (ví dụ "audio/filename.mp3")
			// Vậy đường dẫn tuyệt đối sẽ là new File(AUDIO_STORAGE_BASE_DIR_NAME,
			// cauHoi.getAudioPath())
			File audioInStorage = new File(CauHoiService.AUDIO_STORAGE_BASE_DIR_NAME, cauHoi.getAudioPath());
			pathForCheck = audioInStorage.getAbsolutePath();
		}

		if (pathForCheck != null) {
			File f = new File(pathForCheck);
			canPlay = f.exists() && f.isFile();
			if (canPlay)
				System.out.println("[AUDIO BUTTON] Sẵn sàng phát: " + pathForCheck);
			else
				System.err.println("[AUDIO BUTTON] Không tìm thấy file: " + pathForCheck
						+ " (Kiểm tra lại đường dẫn và thư mục lưu trữ)");
		} else {
			System.out.println("[AUDIO BUTTON] Không có đường dẫn file audio để kiểm tra.");
		}
		btnPlayAudio.setEnabled(canPlay);
		if (currentClip != null && currentClip.isRunning()) {
			btnPlayAudio.setText("Dừng Nghe");
			btnPlayAudio.setEnabled(true);
		} else {
			btnPlayAudio.setText("Nghe thử");
			// Trạng thái enable đã được set ở trên dựa vào canPlay
		}
	}

	private void playSelectedAudio() {
		if (currentClip != null && currentClip.isRunning()) {
			currentClip.stop();
			currentClip.close();
			currentClip = null;
			System.out.println("Đã dừng audio đang phát.");
			updatePlayAudioButtonState(); // Cập nhật lại trạng thái nút
			return;
		}

		String pathToPlay = null;
		File audioFileToPlay = null;

		if (selectedAudioFileAbsolutePath != null && !selectedAudioFileAbsolutePath.isEmpty()) {
			audioFileToPlay = new File(selectedAudioFileAbsolutePath);
		} else if (cauHoi != null && cauHoi.getAudioPath() != null && !cauHoi.getAudioPath().isEmpty()) {
			audioFileToPlay = new File(CauHoiService.AUDIO_STORAGE_BASE_DIR_NAME, cauHoi.getAudioPath());
		}

		if (audioFileToPlay == null || !audioFileToPlay.exists() || !audioFileToPlay.isFile()) {
			JOptionPane.showMessageDialog(this, "Không có file âm thanh hợp lệ hoặc file không tồn tại để phát.",
					"Lỗi Phát Âm Thanh", JOptionPane.WARNING_MESSAGE);
			updatePlayAudioButtonState();
			return;
		}
		pathToPlay = audioFileToPlay.getAbsolutePath();
		System.out.println("Đang cố gắng phát: " + pathToPlay);

		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFileToPlay);
			AudioFormat format = audioInputStream.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);

			if (!AudioSystem.isLineSupported(info)) {
				JOptionPane.showMessageDialog(this, "Định dạng audio không được hỗ trợ.\nHãy thử file .wav.",
						"Lỗi Định Dạng", JOptionPane.ERROR_MESSAGE);
				audioInputStream.close();
				updatePlayAudioButtonState();
				return;
			}

			currentClip = (Clip) AudioSystem.getLine(info);
			currentClip.addLineListener(event -> {
				if (event.getType() == LineEvent.Type.STOP) {
					SwingUtilities.invokeLater(() -> {
						if (event.getLine() == currentClip) {
							currentClip.close();
							currentClip = null;
							updatePlayAudioButtonState();
							System.out.println("Audio đã phát xong hoặc bị dừng.");
						}
					});
				} else if (event.getType() == LineEvent.Type.START) {
					SwingUtilities.invokeLater(() -> {
						btnPlayAudio.setText("Dừng Nghe");
						btnPlayAudio.setEnabled(true);
					});
				}
			});
			currentClip.open(audioInputStream);
			currentClip.start();
		} catch (UnsupportedAudioFileException uafe) {
			JOptionPane.showMessageDialog(this,
					"Định dạng file âm thanh không được hỗ trợ.\nVui lòng chọn file .wav (hoặc .mp3 nếu có thư viện).",
					"Lỗi Định Dạng", JOptionPane.ERROR_MESSAGE);
			uafe.printStackTrace();
			updatePlayAudioButtonState();
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(this, "Lỗi đọc file âm thanh: " + ioe.getMessage(), "Lỗi File",
					JOptionPane.ERROR_MESSAGE);
			ioe.printStackTrace();
			updatePlayAudioButtonState();
		} catch (LineUnavailableException lue) {
			JOptionPane.showMessageDialog(this, "Không thể mở line để phát âm thanh.", "Lỗi Âm Thanh",
					JOptionPane.ERROR_MESSAGE);
			lue.printStackTrace();
			updatePlayAudioButtonState();
		}
	}

	private void themDapAnTracNghiem() {
		boolean chiMotDapAnDung = true;
		// Giả sử DapAnDialog là một lớp bạn đã tạo và import
		DapAnDialog dapAnDialog = new DapAnDialog(this, "Thêm Lựa Chọn Đáp Án", null, chiMotDapAnDung);
		dapAnDialog.setVisible(true);
		if (dapAnDialog.isSaved()) {
			DapAn newDapAn = dapAnDialog.getDapAn();
			if (newDapAn.isLaDapAnDung() && chiMotDapAnDung) {
				for (DapAn da : this.cauHoi.getDapAnList())
					da.setLaDapAnDung(false);
			}
			this.cauHoi.getDapAnList().add(newDapAn);
			dapAnTableModel.setData(this.cauHoi.getDapAnList());
		}
	}

	private void suaDapAnTracNghiem() {
		int selectedRow = tblDapAn.getSelectedRow();
		if (selectedRow == -1)
			return;
		int modelRow = tblDapAn.convertRowIndexToModel(selectedRow);
		DapAn dapAnToEdit = this.cauHoi.getDapAnList().get(modelRow);
		if (dapAnToEdit == null)
			return;
		boolean chiMotDapAnDung = true;
		DapAnDialog dapAnDialog = new DapAnDialog(this, "Sửa Lựa Chọn Đáp Án", dapAnToEdit, chiMotDapAnDung);
		dapAnDialog.setVisible(true);
		if (dapAnDialog.isSaved()) {
			DapAn editedDapAn = dapAnDialog.getDapAn();
			if (editedDapAn.isLaDapAnDung() && chiMotDapAnDung) {
				for (DapAn da : this.cauHoi.getDapAnList()) {
					if (da != editedDapAn)
						da.setLaDapAnDung(false);
				}
			}
			dapAnTableModel.fireTableRowsUpdated(modelRow, modelRow);
		}
	}

	private void xoaDapAnTracNghiem() {
		int selectedRow = tblDapAn.getSelectedRow();
		if (selectedRow == -1)
			return;
		int modelRow = tblDapAn.convertRowIndexToModel(selectedRow);
		if (JOptionPane.showConfirmDialog(this, "Xóa lựa chọn đáp án này?", "Xác nhận",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			this.cauHoi.getDapAnList().remove(modelRow);
			dapAnTableModel.setData(this.cauHoi.getDapAnList());
		}
	}

	private void saveCauHoi() {
		String noiDung = txtNoiDungCauHoi.getText().trim();
		if (noiDung.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Nội dung câu hỏi không được trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
			txtNoiDungCauHoi.requestFocus();
			return;
		}
		String loaiCH = (String) cmbLoaiCauHoi.getSelectedItem();
		Integer doKho = (Integer) cmbDoKho.getSelectedItem();
		ChuDe selectedChuDe = (ChuDe) cmbChuDe.getSelectedItem();
		if (selectedChuDe == null) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn chủ đề.", "Lỗi", JOptionPane.ERROR_MESSAGE);
			cmbChuDe.requestFocus();
			return;
		}

		cauHoi.setNoiDung(noiDung);
		cauHoi.setLoaiCauHoi(loaiCH);
		cauHoi.setDoKho(doKho);
		cauHoi.setIdChuDe(selectedChuDe.getId());
		// audioPath sẽ được Service xử lý dựa trên selectedAudioFileAbsolutePath và
		// audioPath hiện tại của this.cauHoi

		if ("Trắc nghiệm".equals(loaiCH) || "Nghe (Trắc nghiệm)".equals(loaiCH) || "Sắp xếp câu".equals(loaiCH)) {
			if (cauHoi.getDapAnList().isEmpty()
					&& ("Trắc nghiệm".equals(loaiCH) || "Nghe (Trắc nghiệm)".equals(loaiCH))) {
				JOptionPane.showMessageDialog(this, "Câu hỏi trắc nghiệm phải có ít nhất một lựa chọn đáp án.", "Lỗi",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			boolean coDapAnDung = false;
			if ("Trắc nghiệm".equals(loaiCH) || "Nghe (Trắc nghiệm)".equals(loaiCH)) {
				for (DapAn da : cauHoi.getDapAnList()) {
					if (da.isLaDapAnDung()) {
						coDapAnDung = true;
						break;
					}
				}
				if (!coDapAnDung && !cauHoi.getDapAnList().isEmpty()) {
					JOptionPane.showMessageDialog(this,
							"Câu hỏi trắc nghiệm phải có ít nhất một đáp án được đánh dấu là đúng.", "Lỗi",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		} else if ("Tự luận".equals(loaiCH) || "Điền khuyết".equals(loaiCH)) {
			String noiDungGoiY = txtDapAnGoiYTuLuan.getText().trim();
			// Chỉ giữ lại tối đa một đáp án cho loại tự luận/điền khuyết
			List<DapAn> dapAnChoTuLuan = new ArrayList<>();
			if (!noiDungGoiY.isEmpty()) {
				DapAn dapAnGoiY;
				if (!cauHoi.getDapAnList().isEmpty()
						&& ("Tự luận".equals(cauHoi.getLoaiCauHoi()) || "Điền khuyết".equals(cauHoi.getLoaiCauHoi()))) {
					// Nếu đang sửa và loại câu hỏi không đổi, cập nhật đáp án cũ
					dapAnGoiY = cauHoi.getDapAnList().get(0);
				} else {
					// Nếu thêm mới, hoặc chuyển từ loại khác sang, hoặc list rỗng
					dapAnGoiY = new DapAn();
				}
				dapAnGoiY.setNoiDung(noiDungGoiY);
				dapAnGoiY.setLaDapAnDung(true); // Quy ước
				dapAnChoTuLuan.add(dapAnGoiY);
			}
			cauHoi.setDapAnList(dapAnChoTuLuan); // Gán lại list mới (có thể rỗng)
		} else {
			cauHoi.getDapAnList().clear();
		}

		this.saved = true;
		dispose();
	}

	public CauHoi getCauHoi() {
		return saved ? cauHoi : null;
	}

	public String getSelectedAudioFileAbsolutePath() {
		return selectedAudioFileAbsolutePath;
	}

	public boolean isSaved() {
		return saved;
	}
}