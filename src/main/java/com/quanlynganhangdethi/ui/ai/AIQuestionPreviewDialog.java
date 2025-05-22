package com.quanlynganhangdethi.ui.ai; // Hoặc package bạn chọn

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quanlynganhangdethi.dao.ChuDeDAO;
import com.quanlynganhangdethi.dao.ChuDeDAOImpl;
import com.quanlynganhangdethi.models.CauHoi;
import com.quanlynganhangdethi.models.ChuDe;
import com.quanlynganhangdethi.models.DapAn;
import com.quanlynganhangdethi.service.CauHoiService;

public class AIQuestionPreviewDialog extends JDialog {
	private static final Logger logger = LoggerFactory.getLogger(AIQuestionPreviewDialog.class);

	private JTextArea txtAIQuestions;
	private JComboBox<ChuDe> cmbChuDe;
	private JButton btnSaveToDB;
	private JButton btnClose;

	private String aiGeneratedText; // Chuỗi text gốc từ AI
	private List<CauHoi> parsedCauHoiList; // Danh sách câu hỏi đã được parse

	private CauHoiService cauHoiService;
	private ChuDeDAO chuDeDAO;

	public AIQuestionPreviewDialog(Frame parent, String title, String aiGeneratedText) {
		super(parent, title, true);
		this.aiGeneratedText = aiGeneratedText;
		this.cauHoiService = new CauHoiService(); // Khởi tạo service
		this.chuDeDAO = new ChuDeDAOImpl(); // Khởi tạo DAO để load chủ đề

		initComponents();
		loadChuDeData();
		displayAIQuestions();

		// Parse câu hỏi ngay khi dialog được tạo
		this.parsedCauHoiList = parseAIResponseToCauHoiList(this.aiGeneratedText);
		if (this.parsedCauHoiList.isEmpty()
				&& (this.aiGeneratedText != null && !this.aiGeneratedText.trim().isEmpty())) {
			// Nếu có text từ AI nhưng không parse được câu nào, có thể hiển thị cảnh báo
			JOptionPane.showMessageDialog(this,
					"Không thể phân tích được câu hỏi nào từ nội dung AI cung cấp.\n"
							+ "Vui lòng kiểm tra định dạng của phản hồi từ AI.",
					"Lỗi Phân Tích", JOptionPane.WARNING_MESSAGE);
			btnSaveToDB.setEnabled(false); // Vô hiệu hóa nút lưu nếu không có gì để lưu
		} else if (this.parsedCauHoiList.isEmpty()) {
			btnSaveToDB.setEnabled(false);
		}

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(700, 550); // Kích thước ban đầu
		setLocationRelativeTo(parent);
	}

	private void initComponents() {
		setLayout(new BorderLayout(10, 10));
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Panel hiển thị câu hỏi
		JPanel topPanel = new JPanel(new BorderLayout(5, 5));
		topPanel.setBorder(BorderFactory.createTitledBorder("Các câu hỏi được AI gợi ý"));
		txtAIQuestions = new JTextArea();
		txtAIQuestions.setEditable(false);
		txtAIQuestions.setLineWrap(true);
		txtAIQuestions.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(txtAIQuestions);
		topPanel.add(scrollPane, BorderLayout.CENTER);

		// Panel chọn chủ đề
		JPanel chuDePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		chuDePanel.add(new JLabel("Lưu các câu hỏi này vào Chủ đề:"));
		cmbChuDe = new JComboBox<>();
		cmbChuDe.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof ChuDe) {
					setText(((ChuDe) value).getTenChuDe());
				} else if (value == null && index == -1) { // Mục "Chọn chủ đề"
					setText("-- Chọn chủ đề --");
				}
				return this;
			}
		});
		cmbChuDe.setPreferredSize(new Dimension(250, cmbChuDe.getPreferredSize().height));
		chuDePanel.add(cmbChuDe);
		topPanel.add(chuDePanel, BorderLayout.SOUTH);

		add(topPanel, BorderLayout.CENTER);

		// Panel chứa các nút
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnSaveToDB = new JButton("Lưu các câu hỏi này vào CSDL");
		btnClose = new JButton("Đóng");

		buttonPanel.add(btnSaveToDB);
		buttonPanel.add(btnClose);
		add(buttonPanel, BorderLayout.SOUTH);

		// Action Listeners
		btnSaveToDB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveParsedQuestionsToDB();
			}
		});

		btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}

	private void loadChuDeData() {
		try {
			List<ChuDe> listChuDe = chuDeDAO.findAll(); // Giả sử ChuDeDAO đã có
			if (listChuDe == null) {
				listChuDe = new ArrayList<>();
			}
			// Thêm một mục "Chọn chủ đề" hoặc để trống nếu muốn
			// Vector<ChuDe> chuDeVector = new Vector<>(listChuDe);
			// ChuDe placeholder = new ChuDe(); // Tạo một đối tượng ChuDe đặc biệt hoặc
			// null
			// placeholder.setTenChuDe("-- Chọn chủ đề --");
			// placeholder.setId(-1); // Hoặc 0
			// chuDeVector.insertElementAt(placeholder, 0);

			DefaultComboBoxModel<ChuDe> model = new DefaultComboBoxModel<>(new Vector<>(listChuDe));
			cmbChuDe.setModel(model);
			// if (!listChuDe.isEmpty()) {
			// cmbChuDe.setSelectedIndex(0); // Chọn mục đầu tiên (có thể là placeholder)
			// }
		} catch (SQLException e) {
			logger.error("Lỗi khi tải danh sách chủ đề: ", e);
			JOptionPane.showMessageDialog(this, "Lỗi tải danh sách chủ đề: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void displayAIQuestions() {
		txtAIQuestions.setText(this.aiGeneratedText);
		txtAIQuestions.setCaretPosition(0); // Cuộn lên đầu
	}

	private List<CauHoi> parseAIResponseToCauHoiList(String aiResponseText) {
		List<CauHoi> parsedQuestions = new ArrayList<>();
		if (aiResponseText == null || aiResponseText.trim().isEmpty()) {
			logger.warn("Nội dung phản hồi từ AI rỗng, không thể parse.");
			return parsedQuestions;
		}

		String[] questionBlocks = aiResponseText.split("---");
		logger.info("Phân tích phản hồi AI thành {} khối câu hỏi.", questionBlocks.length);

		for (String block : questionBlocks) {
			block = block.trim();
			if (block.isEmpty()) {
				continue;
			}

			CauHoi cauHoi = new CauHoi();
			List<DapAn> dapAnList = new ArrayList<>();
			String dapAnDungKyTu = null;

			String[] lines = block.split("\n");
			for (String line : lines) {
				line = line.trim();
				if (line.startsWith("Câu hỏi:")) {
					cauHoi.setNoiDung(line.substring("Câu hỏi:".length()).trim());
				} else if (line.matches("^[A-Da-d]\\.\\s+.*")) { // A. B. C. D. (có thể viết hoa hoặc thường)
					DapAn da = new DapAn();
					da.setNoiDung(line.substring(3).trim()); // Bỏ qua "A. ", "B. ", ...
					dapAnList.add(da);
				} else if (line.startsWith("Đáp án đúng:")) {
					dapAnDungKyTu = line.substring("Đáp án đúng:".length()).trim().toUpperCase();
				} else if (line.startsWith("Độ khó:")) {
					try {
						String doKhoStr = line.substring("Độ khó:".length()).trim();
						// Loại bỏ ký tự không phải số nếu có (ví dụ: "3 (Dễ)")
						doKhoStr = doKhoStr.replaceAll("[^0-9]", "");
						if (!doKhoStr.isEmpty()) {
							cauHoi.setDoKho(Integer.parseInt(doKhoStr));
						}
					} catch (NumberFormatException e) {
						logger.warn("Không thể parse độ khó từ AI: '{}'", line);
					}
				}
				// Thêm logic để parse 'Loại câu hỏi:' nếu AI trả về
				else if (line.startsWith("Loại câu hỏi:")) {
					cauHoi.setLoaiCauHoi(line.substring("Loại câu hỏi:".length()).trim());
				}
			}

			if (dapAnDungKyTu != null && dapAnList.size() >= 1 && dapAnList.size() <= 4) {
				char correctChar = dapAnDungKyTu.charAt(0); // Lấy ký tự đầu tiên
				int correctIndex = correctChar - 'A'; // A=0, B=1, C=2, D=3
				if (correctIndex >= 0 && correctIndex < dapAnList.size()) {
					dapAnList.get(correctIndex).setLaDapAnDung(true);
				} else {
					logger.warn("Ký tự đáp án đúng '{}' không hợp lệ cho số lượng đáp án {}.", dapAnDungKyTu,
							dapAnList.size());
				}
			} else if (dapAnDungKyTu != null) {
				logger.warn(
						"Không thể xác định đáp án đúng từ ký tự '{}' hoặc số lượng đáp án không phù hợp ({} đáp án).",
						dapAnDungKyTu, dapAnList.size());
			}

			if (cauHoi.getNoiDung() != null && !cauHoi.getNoiDung().isEmpty() && !dapAnList.isEmpty()) {
				cauHoi.setDapAnList(dapAnList);
				// Mặc định loại câu hỏi nếu AI không cung cấp
				if (cauHoi.getLoaiCauHoi() == null || cauHoi.getLoaiCauHoi().trim().isEmpty()) {
					cauHoi.setLoaiCauHoi("Trắc nghiệm");
				}
				// Độ khó mặc định nếu AI không cung cấp hoặc parse lỗi
				if (cauHoi.getDoKho() == null) {
					cauHoi.setDoKho(2); // Ví dụ độ khó trung bình
				}
				parsedQuestions.add(cauHoi);
				logger.debug("Đã parse được câu hỏi: {}",
						cauHoi.getNoiDung().substring(0, Math.min(50, cauHoi.getNoiDung().length())));
			} else {
				logger.warn("Không parse được câu hỏi hợp lệ từ khối: {}",
						block.substring(0, Math.min(50, block.length())));
			}
		}
		logger.info("Hoàn tất parse, có {} câu hỏi hợp lệ.", parsedQuestions.size());
		return parsedQuestions;
	}

	private void saveParsedQuestionsToDB() {
		if (this.parsedCauHoiList == null || this.parsedCauHoiList.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Không có câu hỏi nào đã được phân tích từ AI để lưu.", "Thông báo",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		Object selectedItem = cmbChuDe.getSelectedItem();
		if (selectedItem == null) { // || (selectedItem instanceof ChuDe && ((ChuDe)selectedItem).getId() <=0 )
			JOptionPane.showMessageDialog(this, "Vui lòng chọn một chủ đề hợp lệ để lưu các câu hỏi.",
					"Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
			cmbChuDe.requestFocus();
			return;
		}
		ChuDe chuDeDaChon = (ChuDe) selectedItem;
		int idChuDeChoCauHoiAI = chuDeDaChon.getId();
		if (idChuDeChoCauHoiAI <= 0) { // Kiểm tra thêm nếu ID của chủ đề không hợp lệ
			JOptionPane.showMessageDialog(this, "Chủ đề đã chọn không hợp lệ. Vui lòng chọn lại.", "Lỗi Chủ Đề",
					JOptionPane.ERROR_MESSAGE);
			cmbChuDe.requestFocus();
			return;
		}

		int soCauHoiDaLuu = 0;
		int soCauHoiLoi = 0;
		StringBuilder ketQuaLuu = new StringBuilder(
				"Kết quả lưu câu hỏi vào chủ đề '" + chuDeDaChon.getTenChuDe() + "':\n");
		btnSaveToDB.setEnabled(false); // Vô hiệu hóa nút trong khi lưu

		// Thực hiện trong một SwingWorker để không block UI
		SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
			@Override
			protected Void doInBackground() throws Exception {
				int currentCount = 0;
				for (CauHoi cauHoiParseDuoc : parsedCauHoiList) {
					currentCount++;
					publish("Đang xử lý câu " + currentCount + "/" + parsedCauHoiList.size() + ": " + cauHoiParseDuoc
							.getNoiDung().substring(0, Math.min(20, cauHoiParseDuoc.getNoiDung().length())) + "...");
					try {
						cauHoiParseDuoc.setIdChuDe(idChuDeChoCauHoiAI);
						// audioPath là null vì AI hiện không trả về audio
						CauHoi cauHoiDaLuuVaoDB = cauHoiService.taoCauHoiVoiDapAn(cauHoiParseDuoc, null);

						if (cauHoiDaLuuVaoDB != null && cauHoiDaLuuVaoDB.getId() > 0) {
							// Không cần non-static access ở đây nữa
							// soCauHoiDaLuu++; // Biến này sẽ được cập nhật ở done()
							publish("[THÀNH CÔNG] " + cauHoiParseDuoc.getNoiDung().substring(0,
									Math.min(30, cauHoiParseDuoc.getNoiDung().length())) + "...");
						} else {
							// soCauHoiLoi++;
							publish("[LỖI] Không lưu được: " + cauHoiParseDuoc.getNoiDung().substring(0,
									Math.min(30, cauHoiParseDuoc.getNoiDung().length())) + "...");
						}
					} catch (SQLException | IOException e) {
						// soCauHoiLoi++;
						publish("[LỖI DB/IO] "
								+ cauHoiParseDuoc.getNoiDung().substring(0,
										Math.min(30, cauHoiParseDuoc.getNoiDung().length()))
								+ "...: " + e.getMessage());
						logger.error("Lỗi khi lưu câu hỏi AI '{}' vào CSDL:", cauHoiParseDuoc.getNoiDung(), e);
					}
					Thread.sleep(100); // Giảm tải một chút nếu cần, hoặc để UI cập nhật
				}
				return null;
			}

			@Override
			protected void process(List<String> chunks) {
				// Cập nhật JTextArea hoặc một JLabel trạng thái
				for (String message : chunks) {
					// Ví dụ: statusLabel.setText(message);
					txtAIQuestions.append("\n" + message); // Tạm thời append vào text area
					txtAIQuestions.setCaretPosition(txtAIQuestions.getDocument().getLength());
				}
			}

			@Override
			protected void done() {
				// Đếm lại số câu hỏi thành công/thất bại dựa trên log hoặc một cách khác
				// Vì soCauHoiDaLuu và soCauHoiLoi không thể truy cập trực tiếp từ đây
				// một cách an toàn cho thread.
				// Cách đơn giản là kiểm tra lại DB hoặc dựa vào thông điệp publish
				// Tạm thời, chúng ta sẽ hiển thị thông báo chung và người dùng tự kiểm tra.
				int finalSuccessCount = 0;
				int finalErrorCount = 0;
				// Logic đếm lại dựa trên các message đã publish (nếu cần chính xác)
				// Hoặc đơn giản hơn, chỉ thông báo hoàn tất.
				String currentText = txtAIQuestions.getText();
				String[] lines = currentText.split("\n");
				for (String line : lines) {
					if (line.startsWith("[THÀNH CÔNG]"))
						finalSuccessCount++;
					else if (line.startsWith("[LỖI"))
						finalErrorCount++;
				}

				StringBuilder finalMessage = new StringBuilder("Hoàn tất quá trình lưu.\n");
				finalMessage.append("Số câu hỏi lưu thành công: ").append(finalSuccessCount).append("\n");
				finalMessage.append("Số câu hỏi bị lỗi: ").append(finalErrorCount).append("\n\n");
				finalMessage.append("Vui lòng kiểm tra lại danh sách câu hỏi trong chủ đề đã chọn.");

				JOptionPane.showMessageDialog(AIQuestionPreviewDialog.this, finalMessage.toString(), "Kết quả lưu trữ",
						JOptionPane.INFORMATION_MESSAGE);
				btnSaveToDB.setEnabled(true); // Kích hoạt lại nút
			}
		};
		worker.execute();
	}

	/**
	 * Phương thức main ví dụ để test dialog này (xóa hoặc comment khi tích hợp)
	 */
	public static void main(String[] args) {
		// Ví dụ chuỗi text từ AI
		String sampleAIResponse = "Câu hỏi: 日本の首都はどこですか。\n" + "A. 大阪\n" + "B. 京都\n" + "C. 東京\n" + "D. 名古屋\n"
				+ "Đáp án đúng: C\n" + "Độ khó: 1\n" + "Loại câu hỏi: Trắc nghiệm\n" + "---\n"
				+ "Câu hỏi: 「こんにちは」は英語で何と言いますか。\n" + "A. Good morning\n" + "B. Good evening\n" + "C. Hello\n"
				+ "D. Goodbye\n" + "Đáp án đúng: C\n" + "Độ khó: 1\n" + "---\n"
				+ "Câu hỏi: Đây là câu hỏi không có đáp án đúng được chỉ định rõ ràng.\n" + "A. Lựa chọn 1\n"
				+ "B. Lựa chọn 2\n" + "C. Lựa chọn 3\n" + "D. Lựa chọn 4\n" + "Đáp án đúng: X\n" + // Đáp án không hợp
																									// lệ
				"---\n" + "Câu hỏi: Câu hỏi chỉ có 2 đáp án.\n" + "A. Đúng\n" + "B. Sai\n" + "Đáp án đúng: A\n"
				+ "Độ khó: 2";

		SwingUtilities.invokeLater(() -> {
			// Cần một JFrame cha để test JDialog modal
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(300, 200);
			frame.setLocationRelativeTo(null);
			// frame.setVisible(true); // Không cần hiện frame cha cũng được

			AIQuestionPreviewDialog dialog = new AIQuestionPreviewDialog(frame, "Xem Trước Câu Hỏi AI Tạo",
					sampleAIResponse);
			dialog.setVisible(true);

			// Sau khi dialog đóng, bạn có thể xử lý gì đó nếu cần
			System.exit(0); // Thoát ứng dụng test
		});
	}
}