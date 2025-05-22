// src/main/java/com/quanlynganhangdethi/ui/dethi/TaoDeThiTuAnhDialog.java
package com.quanlynganhangdethi.ui.dethi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
// import java.nio.charset.StandardCharsets; // Không cần nữa nếu không tạo file txt ở đây
import java.nio.file.Files;
// import java.nio.file.Path; // Không cần nữa
// import java.util.ArrayList; // Không cần nữa
import java.util.Base64;
// import java.util.List; // Không cần nữa
import java.util.concurrent.ExecutionException;
// import java.util.regex.Matcher; // Không cần nữa
// import java.util.regex.Pattern; // Không cần nữa

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame; // Chỉ dùng cho main test
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager; // Chỉ dùng cho main test
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import com.quanlynganhangdethi.models.CauHoi; // Không cần parse ở đây nữa
// import com.quanlynganhangdethi.models.DapAn; // Không cần parse ở đây nữa
import com.quanlynganhangdethi.service.AIAssistantService;
import com.quanlynganhangdethi.ui.ai.AIQuestionPreviewDialog; // **IMPORT DIALOG MỚI**

public class TaoDeThiTuAnhDialog extends JDialog {
	private static final Logger logger = LoggerFactory.getLogger(TaoDeThiTuAnhDialog.class);

	private JTextField txtFilePath;
	private JButton btnChonAnh;
	private JLabel lblImagePreview;
	private JTextArea txtCustomPrompt;
	private JSpinner spnSoLuongCauHoi;
	private JComboBox<String> cmbTrinhDoJLPT;
	private JButton btnTaoCauHoiTuAnh; // Đổi tên nút cho rõ nghĩa hơn
	private JProgressBar progressBar;

	private File selectedImageFile;
	private AIAssistantService aiAssistantService;

	public TaoDeThiTuAnhDialog(Frame owner) {
		super(owner, "Tạo Câu Hỏi từ Hình Ảnh (AI)", true); // Đổi tiêu đề dialog
		this.aiAssistantService = new AIAssistantService();
		initComponents();
		layoutComponents();
		addEventListeners();
		setSize(650, 550);
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	private void initComponents() {
		txtFilePath = new JTextField(30);
		txtFilePath.setEditable(false);
		btnChonAnh = new JButton("Chọn Ảnh...");
		lblImagePreview = new JLabel("Chưa chọn ảnh", SwingConstants.CENTER);
		lblImagePreview.setPreferredSize(new Dimension(200, 150));
		lblImagePreview.setBorder(BorderFactory.createEtchedBorder());
		txtCustomPrompt = new JTextArea(5, 30);
		txtCustomPrompt.setLineWrap(true);
		txtCustomPrompt.setWrapStyleWord(true);
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(5, 1, 50, 1); // Min 1, Max 50, Step 1
		spnSoLuongCauHoi = new JSpinner(spinnerModel);
		String[] jlptLevels = { "N5", "N4", "N3", "N2", "N1", "Khác (ghi rõ trong prompt)" }; // Thêm lựa chọn "Khác"
		cmbTrinhDoJLPT = new JComboBox<>(jlptLevels);
		cmbTrinhDoJLPT.setSelectedItem("N3");
		btnTaoCauHoiTuAnh = new JButton("Bắt Đầu Tạo Câu Hỏi (AI)"); // Đổi tên nút
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setVisible(false);
	}

	private void layoutComponents() {
		JPanel panelMain = new JPanel(new BorderLayout(10, 10));
		panelMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel panelAnh = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelAnh.add(new JLabel("File Ảnh:"));
		panelAnh.add(txtFilePath);
		panelAnh.add(btnChonAnh);

		JPanel panelPreview = new JPanel(new BorderLayout());
		panelPreview.add(lblImagePreview, BorderLayout.CENTER);
		panelPreview.setBorder(BorderFactory.createTitledBorder("Ảnh xem trước"));

		JPanel panelConfigAI = new JPanel(new GridBagLayout());
		panelConfigAI.setBorder(BorderFactory.createTitledBorder("Cấu hình AI"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;

		gbc.gridx = 0;
		gbc.gridy = 0;
		panelConfigAI.add(new JLabel("Yêu cầu tùy chỉnh (Prompt):"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		panelConfigAI.add(new JScrollPane(txtCustomPrompt), gbc);

		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0; // Reset
		gbc.gridx = 0;
		gbc.gridy = 1;
		panelConfigAI.add(new JLabel("Số lượng câu hỏi mong muốn:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		panelConfigAI.add(spnSoLuongCauHoi, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		panelConfigAI.add(new JLabel("Trình độ JLPT (gợi ý):"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 2;
		panelConfigAI.add(cmbTrinhDoJLPT, gbc);

		JPanel panelInput = new JPanel(new BorderLayout(10, 10));
		panelInput.add(panelAnh, BorderLayout.NORTH);
		panelInput.add(panelPreview, BorderLayout.CENTER);
		panelInput.add(panelConfigAI, BorderLayout.SOUTH);

		JPanel panelAction = new JPanel(new FlowLayout(FlowLayout.CENTER));
		panelAction.add(btnTaoCauHoiTuAnh); // Sử dụng tên nút mới
		panelAction.add(progressBar);

		panelMain.add(panelInput, BorderLayout.CENTER);
		panelMain.add(panelAction, BorderLayout.SOUTH);
		add(panelMain);
	}

	private void addEventListeners() {
		btnChonAnh.addActionListener(e -> chonAnh());
		btnTaoCauHoiTuAnh.addActionListener(e -> xuLyTaoCauHoiTuAnh()); // Đổi tên hàm xử lý
	}

	private void chonAnh() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Chọn file hình ảnh");
		fileChooser.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Hình ảnh (JPG, PNG, WEBP, BMP, GIF)", "jpg",
				"jpeg", "png", "webp", "bmp", "gif");
		fileChooser.addChoosableFileFilter(filter);

		int returnValue = fileChooser.showOpenDialog(this);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			selectedImageFile = fileChooser.getSelectedFile();
			txtFilePath.setText(selectedImageFile.getAbsolutePath());
			try {
				// Hiển thị preview ảnh với kích thước cố định của JLabel
				ImageIcon imageIcon = new ImageIcon(selectedImageFile.getAbsolutePath());
				Image image = imageIcon.getImage();
				// Tính toán tỷ lệ để vừa với JLabel mà không làm méo ảnh
				int previewWidth = lblImagePreview.getWidth();
				int previewHeight = lblImagePreview.getHeight();
				int imgWidth = image.getWidth(null);
				int imgHeight = image.getHeight(null);

				double scaleX = (double) previewWidth / (double) imgWidth;
				double scaleY = (double) previewHeight / (double) imgHeight;
				double scale = Math.min(scaleX, scaleY); // Giữ tỷ lệ, lấy scale nhỏ hơn

				int newImgWidth = (int) (scale * imgWidth);
				int newImgHeight = (int) (scale * imgHeight);

				Image scaledImage = image.getScaledInstance(newImgWidth, newImgHeight, Image.SCALE_SMOOTH);
				lblImagePreview.setIcon(new ImageIcon(scaledImage));
				lblImagePreview.setText(null); // Xóa text "Chưa chọn ảnh"
			} catch (Exception ex) {
				lblImagePreview.setIcon(null);
				lblImagePreview.setText("Lỗi hiển thị ảnh");
				logger.error("Lỗi khi hiển thị ảnh preview: ", ex);
				selectedImageFile = null; // Reset nếu có lỗi
				txtFilePath.setText("");
			}
		}
	}

	// Phương thức convertJlptLevelToDoKho không cần thiết ở đây nữa
	// vì việc parse độ khó sẽ do AIQuestionPreviewDialog xử lý từ text AI trả về

	private void xuLyTaoCauHoiTuAnh() { // Đổi tên từ taoDeTuAnh
		if (selectedImageFile == null) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn một file hình ảnh.", "Chưa chọn ảnh",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		String base64Image;
		String mimeType;
		try {
			byte[] fileContent = Files.readAllBytes(selectedImageFile.toPath());
			base64Image = Base64.getEncoder().encodeToString(fileContent);
			String fileName = selectedImageFile.getName().toLowerCase();
			if (fileName.endsWith(".png"))
				mimeType = "image/png";
			else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
				mimeType = "image/jpeg";
			else if (fileName.endsWith(".webp"))
				mimeType = "image/webp";
			else if (fileName.endsWith(".gif"))
				mimeType = "image/gif";
			else if (fileName.endsWith(".bmp"))
				mimeType = "image/bmp";
			else {
				JOptionPane.showMessageDialog(this,
						"Định dạng ảnh không được hỗ trợ. Chỉ chấp nhận JPG, PNG, WEBP, BMP, GIF.", "Lỗi Định Dạng Ảnh",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		} catch (IOException ex) {
			logger.error("Lỗi đọc file ảnh: {}", selectedImageFile.getAbsolutePath(), ex);
			JOptionPane.showMessageDialog(this, "Lỗi đọc file ảnh: " + ex.getMessage(), "Lỗi File Ảnh",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String customPromptText = txtCustomPrompt.getText().trim();
		int numQuestions = (Integer) spnSoLuongCauHoi.getValue();
		String selectedJlptLevelString = (String) cmbTrinhDoJLPT.getSelectedItem();

		// Tạo prompt mặc định nếu customPromptText rỗng
		if (customPromptText.isEmpty()) {
			customPromptText = String.format(
					"Dựa vào hình ảnh được cung cấp, hãy tạo %d câu hỏi trắc nghiệm tiếng Nhật trình độ %s. "
							+ "Mỗi câu hỏi cần bao gồm nội dung câu hỏi, 4 lựa chọn (A, B, C, D), và chỉ rõ đáp án đúng. "
							+ "Định dạng mong muốn cho MỖI câu hỏi (PHẢI TUÂN THỦ NGHIÊM NGẶT ĐỊNH DẠNG NÀY):\n"
							+ "```\n" + "Câu hỏi: [Nội dung câu hỏi ở đây]\n" + "A. [Nội dung lựa chọn A]\n"
							+ "B. [Nội dung lựa chọn B]\n" + "C. [Nội dung lựa chọn C]\n" + "D. [Nội dung lựa chọn D]\n"
							+ "Đáp án đúng: [CHỈ GHI KÝ TỰ A, B, C, hoặc D]\n" + "```\n"
							+ "--- (DÙNG BA DẤU GẠCH NGANG ĐỂ NGĂN CÁCH GIỮA CÁC CÂU HỎI)",
					numQuestions, selectedJlptLevelString);
		} else {
			// Đảm bảo prompt tùy chỉnh cũng yêu cầu định dạng output
			if (!customPromptText.toLowerCase().contains("đáp án đúng:")
					|| !customPromptText.toLowerCase().contains("lựa chọn")
					|| !customPromptText.toLowerCase().contains("câu hỏi:")
					|| !customPromptText.toLowerCase().contains("---")) {
				customPromptText += "\n\nYÊU CẦU BẮT BUỘC VỀ ĐỊNH DẠNG CHO MỖI CÂU HỎI (PHẢI TUÂN THỦ NGHIÊM NGẶT ĐỊNH DẠNG NÀY):\n"
						+ "```\n" + "Câu hỏi: [Nội dung câu hỏi ở đây]\n" + "A. [Nội dung lựa chọn A]\n"
						+ "B. [Nội dung lựa chọn B]\n" + "C. [Nội dung lựa chọn C]\n" + "D. [Nội dung lựa chọn D]\n"
						+ "Đáp án đúng: [CHỈ GHI KÝ TỰ A, B, C, hoặc D]\n" + "```\n"
						+ "--- (DÙNG BA DẤU GẠCH NGANG ĐỂ NGĂN CÁCH GIỮA CÁC CÂU HỎI)";
			}
		}

		btnTaoCauHoiTuAnh.setEnabled(false);
		progressBar.setValue(0); // Reset progress bar
		progressBar.setString("Đang gửi yêu cầu đến AI...");
		progressBar.setVisible(true);

		// Sử dụng final cho các biến sẽ được truy cập từ trong SwingWorker
		final String finalCustomPromptText = customPromptText;
		final String finalBase64Image = base64Image;
		final String finalMimeType = mimeType;
		final int finalNumQuestions = numQuestions;
		final String finalSelectedJlptLevelString = selectedJlptLevelString;

		SwingWorker<String, Void> worker = new SwingWorker<>() {
			@Override
			protected String doInBackground() throws Exception { // Nên throws Exception
				return aiAssistantService.generateQuestionsFromImageAndPrompt(finalCustomPromptText, finalBase64Image,
						finalMimeType, finalNumQuestions, finalSelectedJlptLevelString);
			}

			@Override
			protected void done() {
				progressBar.setVisible(false);
				btnTaoCauHoiTuAnh.setEnabled(true);
				try {
					String aiResultText = get(); // Lấy kết quả từ AI

					if (aiResultText != null && !aiResultText.trim().isEmpty() && !aiResultText.startsWith("Lỗi:")) {
						// Đóng dialog hiện tại (TaoDeThiTuAnhDialog)
						TaoDeThiTuAnhDialog.this.dispose();

						// Mở AIQuestionPreviewDialog để xem trước và lưu
						Frame ownerFrame = (Frame) SwingUtilities
								.getWindowAncestor(TaoDeThiTuAnhDialog.this.getParent()); // Lấy frame cha của dialog
																							// này
						if (ownerFrame == null)
							ownerFrame = JOptionPane.getRootFrame(); // Fallback

						AIQuestionPreviewDialog previewAndSaveDialog = new AIQuestionPreviewDialog(ownerFrame,
								"Xem Trước và Lưu Câu Hỏi Tạo Từ Ảnh", aiResultText // Truyền chuỗi text gốc từ AI
						);
						previewAndSaveDialog.setVisible(true);

					} else if (aiResultText != null && aiResultText.startsWith("Thông báo:")) {
						JOptionPane.showMessageDialog(TaoDeThiTuAnhDialog.this, aiResultText, "Thông báo từ AI",
								JOptionPane.INFORMATION_MESSAGE);
					} else {
						String errorMessage = (aiResultText == null || aiResultText.trim().isEmpty())
								? "AI không trả về nội dung hoặc có lỗi không mong muốn."
								: aiResultText;
						JOptionPane.showMessageDialog(TaoDeThiTuAnhDialog.this, errorMessage, "Lỗi từ AI",
								JOptionPane.ERROR_MESSAGE);
					}
				} catch (InterruptedException | ExecutionException e) {
					logger.error("Lỗi thực thi AI trong background: ", e);
					JOptionPane.showMessageDialog(TaoDeThiTuAnhDialog.this,
							"Lỗi khi thực thi yêu cầu AI: " + e.getMessage(), "Lỗi Thực Thi",
							JOptionPane.ERROR_MESSAGE);
				} catch (Exception e) { // Bắt các lỗi khác có thể xảy ra
					logger.error("Lỗi không mong muốn trong SwingWorker done(): ", e);
					JOptionPane.showMessageDialog(TaoDeThiTuAnhDialog.this, "Lỗi không mong muốn: " + e.getMessage(),
							"Lỗi Chung", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		worker.execute();
	}

	// Bỏ phương thức processAiGeneratedQuestions và taoFileDapAnTxt khỏi dialog
	// này,
	// vì AIQuestionPreviewDialog sẽ đảm nhiệm việc parse và các hành động khác.

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(() -> {
			JFrame testFrame = new JFrame("Parent Frame for Testing");
			testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			// Tạo một nút để mở dialog, thay vì mở trực tiếp từ main của dialog
			JButton openButton = new JButton("Mở Dialog Tạo Câu Hỏi Từ Ảnh");
			openButton.addActionListener(e -> {
				TaoDeThiTuAnhDialog dialog = new TaoDeThiTuAnhDialog(testFrame);
				dialog.setVisible(true);
			});
			testFrame.setLayout(new FlowLayout());
			testFrame.add(openButton);
			testFrame.pack();
			testFrame.setLocationRelativeTo(null);
			testFrame.setVisible(true);
			// System.exit(0); // Không thoát ngay, để frame cha còn tồn tại
		});
	}
}