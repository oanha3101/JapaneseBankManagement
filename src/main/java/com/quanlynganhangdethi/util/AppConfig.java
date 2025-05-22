// src/main/java/com/quanlynganhangdethi/util/AppConfig.java
package com.quanlynganhangdethi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppConfig {
	private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
	private static final Properties properties = new Properties();
	private static final String CONFIG_FILE_NAME = "config.properties"; // File config của bạn

	static {
		// Load properties khi lớp được khởi tạo lần đầu
		try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
			if (input == null) {
				logger.error("LỖI NGHIÊM TRỌNG: Không tìm thấy file cấu hình '{}' trong classpath! "
						+ "Đảm bảo file này nằm trong thư mục 'src/main/resources'.", CONFIG_FILE_NAME);
				// Bạn có thể muốn ném một RuntimeException ở đây để ứng dụng dừng lại
				// vì không có cấu hình, ứng dụng sẽ không hoạt động đúng.
				// throw new RuntimeException("Không tìm thấy file cấu hình: " +
				// CONFIG_FILE_NAME);
			} else {
				properties.load(input);
				logger.info("Đã load cấu hình thành công từ file: {}", CONFIG_FILE_NAME);
			}
		} catch (IOException ex) {
			logger.error("LỖI NGHIÊM TRỌNG: Không thể đọc file cấu hình '{}': ", CONFIG_FILE_NAME, ex);
			// throw new RuntimeException("Lỗi khi đọc file cấu hình: " + CONFIG_FILE_NAME,
			// ex);
		}
	}

	/**
	 * Lấy giá trị của một property từ file config.
	 *
	 * @param key Tên của property.
	 * @return Giá trị của property, hoặc null nếu không tìm thấy.
	 */
	public static String getProperty(String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			logger.warn("Không tìm thấy property '{}' trong file cấu hình.", key);
		}
		return value;
	}

	/**
	 * Lấy giá trị của một property, với giá trị mặc định nếu không tìm thấy.
	 *
	 * @param key          Tên của property.
	 * @param defaultValue Giá trị mặc định.
	 * @return Giá trị của property, hoặc defaultValue nếu không tìm thấy.
	 */
	public static String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	// --- Database Configuration ---
	public static String getDbUrl() {
		return getProperty("db.url");
	}

	public static String getDbUsername() {
		return getProperty("db.username");
	}

	public static String getDbPassword() {
		return getProperty("db.password");
	}

	// --- Gemini AI Configuration ---
	public static String getGeminiApiKey() {
		return getProperty("gemini.api.key"); // Key này khớp với file config.properties của bạn
	}

	public static String getGeminiModelName() {
		// Cung cấp giá trị mặc định nếu key không tồn tại trong file config
		return getProperty("gemini.model.name", "gemini-1.5-flash-latest");
	}

	public static double getGeminiTemperature() {
		try {
			return Double.parseDouble(getProperty("gemini.generation.temperature", "0.7"));
		} catch (NumberFormatException e) {
			logger.warn(
					"Giá trị 'gemini.generation.temperature' không hợp lệ trong config, sử dụng giá trị mặc định 0.7. Lỗi: {}",
					e.getMessage());
			return 0.7; // Giá trị mặc định an toàn
		}
	}

	public static int getGeminiTopK() {
		try {
			return Integer.parseInt(getProperty("gemini.generation.topk", "50"));
		} catch (NumberFormatException e) {
			logger.warn(
					"Giá trị 'gemini.generation.topk' không hợp lệ trong config, sử dụng giá trị mặc định 50. Lỗi: {}",
					e.getMessage());
			return 50; // Giá trị mặc định an toàn
		}
	}

	public static double getGeminiTopP() {
		try {
			return Double.parseDouble(getProperty("gemini.generation.topp", "0.95"));
		} catch (NumberFormatException e) {
			logger.warn(
					"Giá trị 'gemini.generation.topp' không hợp lệ trong config, sử dụng giá trị mặc định 0.95. Lỗi: {}",
					e.getMessage());
			return 0.95; // Giá trị mặc định an toàn
		}
	}

	public static int getGeminiMaxOutputTokens() {
		try {
			return Integer.parseInt(getProperty("gemini.generation.maxoutputtokens", "2048"));
		} catch (NumberFormatException e) {
			logger.warn(
					"Giá trị 'gemini.generation.maxoutputtokens' không hợp lệ trong config, sử dụng giá trị mặc định 2048. Lỗi: {}",
					e.getMessage());
			return 2048; // Giá trị mặc định an toàn
		}
	}

	// --- Safety Settings (ví dụ, bạn có thể không cần dùng trực tiếp nếu OkHttp
	// client không hỗ trợ dễ dàng) ---
	// Nếu bạn dùng thư viện client chính thức của Google, việc đọc các setting này
	// sẽ hữu ích hơn.
	// Với OkHttp tự xây dựng request, bạn có thể cần điều chỉnh payload JSON nếu
	// muốn áp dụng Safety Settings.
	// Hiện tại, payload trong AIAssistantService chưa bao gồm safetySettings.
	public static String getSafetySettingHarassment() {
		return getProperty("gemini.safety.HARASSMENT", "BLOCK_MEDIUM_AND_ABOVE");
	}
	// ... (tương tự cho các safety settings khác nếu bạn quyết định dùng chúng) ...
}