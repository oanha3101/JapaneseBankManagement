package com.quanlynganhangdethi;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.quanlynganhangdethi.config.DatabaseConnection; // Để gọi shutdown

public class MainApp {
	public static void main(String[] args) {
		// Test Google Gemini AI API (thay thế cho OpenAI test)
		try {
			System.out.println("Đang kiểm tra kết nối với Google Gemini AI...");
//			AIAssistantService aiService = new AIAssistantService(); // Sử dụng đúng tên lớp
			String prompt = "gHãy đặt 1 câu hỏi trắc nghiệm N3 tiếng Nhật về ngữ pháp";
//			String response = aiService.getAiResponse(prompt); // Sử dụng đúng tên phương thức
			System.out.println("AI (Gemini) trả lời: " + prompt);
		} catch (Exception e) {
			System.err.println("Lỗi khi kết nối với Google Gemini AI: " + e.getMessage());
			e.printStackTrace();
			// Không cần thoát ứng dụng, vẫn tiếp tục khởi động UI
		}

		// Cố gắng đặt Look and Feel của hệ thống
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			System.err.println("Không thể đặt Look and Feel của hệ thống. Sử dụng Look and Feel mặc định.");
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Nếu bạn có cấu hình gì đó cần kiểm tra cho Gemini (ví dụ: PROJECT_ID)
				// bạn có thể làm ở đây, tương tự như cách bạn kiểm tra OpenAI API key.
				// Tuy nhiên, với ADC, việc kiểm tra chủ yếu là đảm bảo ADC đã được login.
				// System.err.println("Cảnh báo: Cấu hình Google Cloud Project ID chưa được
				// thiết lập trong AIAssistantService.");

				MainFrame mainFrame = new MainFrame();
				mainFrame.setVisible(true);
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("Ứng dụng đang thoát, thực hiện đóng kết nối DB...");
				try {
					DatabaseConnection dbInstance = DatabaseConnection.getInstance();
					if (dbInstance != null) {
						dbInstance.shutdown();
					}
				} catch (Exception e) {
					System.err.println("Lỗi khi cố gắng lấy instance DB hoặc shutdown: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}, "ShutdownHookThread"));
	}

	// Phương thức testOpenAIConnection không còn phù hợp nếu bạn chỉ dùng Gemini.
	// Nếu bạn muốn giữ cả hai, bạn sẽ cần một phương thức tương tự cho Gemini
	// hoặc kiểm tra trực tiếp trong khối try-catch ở main.
	/*
	 * private static boolean testOpenAIConnection() { // ... (code cũ của bạn) ...
	 * }
	 */

	// (Tùy chọn) Bạn có thể thêm một phương thức testGeminiConnection nếu muốn
	// private static boolean testGeminiConnection() {
	// try {
	// AIAssistantService aiService = new AIAssistantService();
	// // Gửi một prompt rất đơn giản để kiểm tra
	// String response = aiService.getAiResponse("Xin chào");
	// // Kiểm tra xem phản hồi có hợp lệ không (ví dụ, không phải là thông báo lỗi
	// từ service)
	// return response != null && !response.startsWith("Lỗi:");
	// } catch (Exception e) {
	// e.printStackTrace();
	// return false;
	// }
	// }
}