// src/main/java/com/quanlynganhangdethi/service/AIAssistantService.java
package com.quanlynganhangdethi.service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.quanlynganhangdethi.util.AppConfig;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIAssistantService {
	private static final Logger logger = LoggerFactory.getLogger(AIAssistantService.class);

	private static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

	// Model sẽ được lấy từ AppConfig, nhưng có thể có giá trị mặc định ở đây nếu
	// AppConfig không cung cấp
	private static final String FALLBACK_TEXT_MODEL = "gemini-1.5-flash-latest";
	private static final String FALLBACK_VISION_MODEL = "gemini-1.5-flash-latest"; // Hoặc gemini-1.5-pro-latest cho
																					// chất lượng cao hơn

	private final OkHttpClient httpClient;
	private final String apiKey; // Biến final sau khi được gán trong constructor
	private String textModelName;
	private String visionModelName;
	private final double generationTemperature;
	private final int generationTopK;
	private final double generationTopP;
	private final int generationMaxOutputTokens;

	public AIAssistantService() {
		this.apiKey = AppConfig.getGeminiApiKey();

		if (this.apiKey == null || this.apiKey.trim().isEmpty() || this.apiKey.trim().toUpperCase().startsWith("YOUR_")
				|| this.apiKey.trim().equalsIgnoreCase("YOUR_API_KEY_HERE")
				|| this.apiKey.trim().equalsIgnoreCase("PASTE_YOUR_API_KEY_HERE")
				|| this.apiKey.trim().equalsIgnoreCase("YOUR_GOOGLE_AI_STUDIO_API_KEY_HERE")) {
			logger.warn("AIService: Google Gemini API Key CHƯA ĐƯỢC CẤU HÌNH hoặc đang sử dụng giá trị placeholder. "
					+ "Các chức năng AI sẽ KHÔNG HOẠT ĐỘNG. Vui lòng kiểm tra file config.properties.");
			// Không set this.apiKey = null nữa, mà sẽ để nó là giá trị không hợp lệ
			// Việc kiểm tra this.apiKey == null ở các hàm public sẽ bắt được trường hợp này
			// nếu AppConfig trả về null
			// Hoặc, nếu muốn chặt chẽ, bạn có thể ném một Exception ở đây để báo lỗi ngay
			// khi khởi tạo service
			// throw new IllegalStateException("API Key của Gemini không được cấu hình hợp
			// lệ.");
		} else {
			logger.info("AIService: Google Gemini API Key đã được load: {}...",
					(this.apiKey.length() > 10 ? this.apiKey.substring(0, 10) : this.apiKey));
		}

		// Load các cấu hình khác từ AppConfig
		String configuredModel = AppConfig.getGeminiModelName(); // AppConfig đã có giá trị mặc định
		this.textModelName = configuredModel; // Sử dụng cùng model cho text và vision nếu không phân tách
		this.visionModelName = configuredModel; // Hoặc bạn có thể có key riêng cho vision model trong config

		// Gán các hằng số model fallback nếu model config là null hoặc rỗng (dù
		// AppConfig đã có mặc định)
		if (this.textModelName == null || this.textModelName.trim().isEmpty()) {
			logger.warn("Tên model Gemini không được cấu hình, sử dụng fallback: {}", FALLBACK_TEXT_MODEL);
			this.textModelName = FALLBACK_TEXT_MODEL;
		}
		if (this.visionModelName == null || this.visionModelName.trim().isEmpty()) {
			logger.warn("Tên model Vision Gemini không được cấu hình, sử dụng fallback: {}", FALLBACK_VISION_MODEL);
			this.visionModelName = FALLBACK_VISION_MODEL;
		}

		this.generationTemperature = AppConfig.getGeminiTemperature();
		this.generationTopK = AppConfig.getGeminiTopK();
		this.generationTopP = AppConfig.getGeminiTopP();
		this.generationMaxOutputTokens = AppConfig.getGeminiMaxOutputTokens();

		logger.info("Cấu hình Gemini Service: TextModel={}, VisionModel={}, Temp={}, TopK={}, TopP={}, MaxTokens={}",
				this.textModelName, this.visionModelName, this.generationTemperature, this.generationTopK,
				this.generationTopP, this.generationMaxOutputTokens);

		this.httpClient = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
				.readTimeout(180, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).build();
	}

	// Phương thức kiểm tra API Key nội bộ
	private boolean isApiKeyInvalid() {
		if (this.apiKey == null || this.apiKey.trim().isEmpty() || this.apiKey.trim().toUpperCase().startsWith("YOUR_")
				|| this.apiKey.trim().equalsIgnoreCase("YOUR_API_KEY_HERE")
				|| this.apiKey.trim().equalsIgnoreCase("PASTE_YOUR_API_KEY_HERE")
				|| this.apiKey.trim().equalsIgnoreCase("YOUR_GOOGLE_AI_STUDIO_API_KEY_HERE")) {
			// Log một lần trong constructor là đủ, ở đây chỉ cần kiểm tra
			return true;
		}
		return false;
	}

	public String getAiSuggestion(String userPrompt) {
		if (isApiKeyInvalid()) { // Sử dụng hàm kiểm tra nội bộ
			return "Lỗi: API Key của Google AI (Gemini) chưa được cấu hình hợp lệ trong Cài đặt.";
		}
		if (userPrompt == null || userPrompt.trim().isEmpty()) {
			logger.warn("User prompt rỗng, không gửi yêu cầu đến AI.");
			return "Lỗi: Nội dung yêu cầu AI không được để trống.";
		}
		return executeGeminiRequest(userPrompt, null, null, this.textModelName, "gợi ý dựa trên văn bản");
	}

	public String getAiSuggestionFromImage(String textPromptForImage, String base64Image, String imageMimeType) {
		if (isApiKeyInvalid()) {
			return "Lỗi: API Key của Google AI (Gemini) chưa được cấu hình hợp lệ.";
		}
		if (base64Image == null || base64Image.trim().isEmpty()) {
			return "Lỗi: Dữ liệu hình ảnh rỗng.";
		}
		if (imageMimeType == null || imageMimeType.trim().isEmpty()) {
			logger.warn("MIME type của ảnh không được cung cấp, mặc định là image/jpeg cho Vision API call.");
			imageMimeType = "image/jpeg";
		}
		return executeGeminiRequest(textPromptForImage, base64Image, imageMimeType, this.visionModelName,
				"gợi ý dựa trên hình ảnh");
	}

	public String generateQuestionsFromImageAndPrompt(String customPrompt, String base64Image, String imageMimeType,
			int numberOfQuestions, String jlptLevel) {
		if (isApiKeyInvalid()) {
			return "Lỗi: API Key của Google AI (Gemini) chưa được cấu hình hợp lệ.";
		}
		// ... (các kiểm tra input khác giữ nguyên) ...
		if (base64Image == null || base64Image.trim().isEmpty()) {
			return "Lỗi: Dữ liệu hình ảnh không được để trống.";
		}
		if (imageMimeType == null || imageMimeType.trim().isEmpty()) {
			logger.warn(
					"MIME type của ảnh không được cung cấp cho generateQuestionsFromImageAndPrompt, mặc định là image/jpeg.");
			imageMimeType = "image/jpeg";
		}
		if (numberOfQuestions <= 0) {
			numberOfQuestions = 3;
			logger.warn("Số lượng câu hỏi không hợp lệ hoặc không được cung cấp, mặc định là {}.", numberOfQuestions);
		}
		if (jlptLevel == null || jlptLevel.trim().isEmpty()) {
			jlptLevel = "N3";
			logger.warn("Trình độ JLPT không được cung cấp, mặc định là {}.", jlptLevel);
		}

		String effectivePrompt;
		// ... (logic tạo effectivePrompt giữ nguyên) ...
		if (customPrompt == null || customPrompt.trim().isEmpty()) {
			effectivePrompt = String.format(
					"Dựa vào hình ảnh được cung cấp, hãy tạo %d câu hỏi trắc nghiệm tiếng Nhật trình độ %s. "
							+ "Mỗi câu hỏi cần bao gồm nội dung câu hỏi, 4 lựa chọn (A, B, C, D), và chỉ rõ đáp án đúng. "
							+ "Nếu hình ảnh là một trang đề thi có chứa văn bản, hãy cố gắng trích xuất hoặc tạo lại các câu hỏi từ đó. "
							+ "Nếu hình ảnh là một cảnh hoặc đối tượng, hãy tạo câu hỏi liên quan đến nội dung hình ảnh. "
							+ "Trả lời bằng tiếng Việt cho các câu hỏi và lựa chọn nếu có thể, trừ khi câu hỏi yêu cầu cụ thể bằng tiếng Nhật."
							+ "Định dạng mong muốn cho MỖI câu hỏi (PHẢI TUÂN THỦ NGHIÊM NGẶT ĐỊNH DẠNG NÀY):\n"
							+ "```\n" + "Câu hỏi: [Nội dung câu hỏi ở đây]\n" + "A. [Nội dung lựa chọn A]\n"
							+ "B. [Nội dung lựa chọn B]\n" + "C. [Nội dung lựa chọn C]\n" + "D. [Nội dung lựa chọn D]\n"
							+ "Đáp án đúng: [CHỈ GHI KÝ TỰ A, B, C, hoặc D]\n" + "```\n"
							+ "--- (DÙNG BA DẤU GẠCH NGANG ĐỂ NGĂN CÁCH GIỮA CÁC CÂU HỎI)",
					numberOfQuestions, jlptLevel);
		} else {
			effectivePrompt = customPrompt;
			if (!effectivePrompt.toLowerCase().contains("đáp án đúng:")
					|| !effectivePrompt.toLowerCase().contains("lựa chọn")
					|| !effectivePrompt.toLowerCase().contains("câu hỏi:")) {
				effectivePrompt += "\n\nYÊU CẦU BẮT BUỘC VỀ ĐỊNH DẠNG CHO MỖI CÂU HỎI (PHẢI TUÂN THỦ NGHIÊM NGẶT ĐỊNH DẠNG NÀY):\n"
						+ "```\n" + "Câu hỏi: [Nội dung câu hỏi ở đây]\n" + "A. [Nội dung lựa chọn A]\n"
						+ "B. [Nội dung lựa chọn B]\n" + "C. [Nội dung lựa chọn C]\n" + "D. [Nội dung lựa chọn D]\n"
						+ "Đáp án đúng: [CHỈ GHI KÝ TỰ A, B, C, hoặc D]\n" + "```\n"
						+ "--- (DÙNG BA DẤU GẠCH NGANG ĐỂ NGĂN CÁCH GIỮA CÁC CÂU HỎI)";
			}
		}

		logger.info("Yêu cầu tạo {} câu hỏi {} từ ảnh. Model: {}. Prompt hiệu lực (150 chars): {}", numberOfQuestions,
				jlptLevel, this.visionModelName, // Sử dụng visionModelName đã load
				effectivePrompt.substring(0, Math.min(effectivePrompt.length(), 150)) + "...");

		return executeGeminiRequest(effectivePrompt, base64Image, imageMimeType, this.visionModelName,
				"tạo bộ câu hỏi từ hình ảnh");
	}

	private String executeGeminiRequest(String textPrompt, String base64Image, String imageMimeType, String modelToUse,
			String requestTypeDescription) {
		if (isApiKeyInvalid()) { // Kiểm tra lại ở đây để chắc chắn
			logger.error("Không thể thực thi yêu cầu '{}' do API Key không hợp lệ.", requestTypeDescription);
			return "Lỗi: API Key của Google AI (Gemini) chưa được cấu hình hợp lệ.";
		}
		// ... (phần kiểm tra textPrompt và base64Image rỗng giữ nguyên) ...
		if ((textPrompt == null || textPrompt.trim().isEmpty())
				&& (base64Image == null || base64Image.trim().isEmpty())) {
			logger.warn("Cả text prompt và base64Image đều rỗng cho yêu cầu '{}'.", requestTypeDescription);
			return "Lỗi: Cần cung cấp ít nhất text prompt hoặc hình ảnh.";
		}

		// ... (phần xây dựng payload JSON giữ nguyên, nhưng sử dụng các giá trị config
		// đã load) ...
		MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		JsonObject payload = new JsonObject();
		// ... (phần contentsArray, contentObject, partsArray giữ nguyên) ...
		JsonArray contentsArray = new JsonArray();
		JsonObject contentObject = new JsonObject();
		JsonArray partsArray = new JsonArray();

		if (textPrompt != null && !textPrompt.trim().isEmpty()) {
			JsonObject textPart = new JsonObject();
			textPart.addProperty("text", textPrompt);
			partsArray.add(textPart);
		}

		if (base64Image != null && !base64Image.trim().isEmpty()) {
			JsonObject inlineData = new JsonObject();
			inlineData.addProperty("mime_type", imageMimeType);
			inlineData.addProperty("data", base64Image);
			JsonObject imagePart = new JsonObject();
			imagePart.add("inline_data", inlineData);
			partsArray.add(imagePart);
		}

		if (partsArray.isEmpty()) {
			logger.warn("Không có nội dung (text hoặc image) hợp lệ để gửi cho yêu cầu '{}'.", requestTypeDescription);
			return "Lỗi: Không có nội dung hợp lệ để gửi đến AI.";
		}

		contentObject.add("parts", partsArray);
		contentsArray.add(contentObject);
		payload.add("contents", contentsArray);

		JsonObject generationConfig = new JsonObject();
		generationConfig.addProperty("temperature", this.generationTemperature);
		generationConfig.addProperty("topK", this.generationTopK);
		generationConfig.addProperty("topP", this.generationTopP);
		generationConfig.addProperty("maxOutputTokens", this.generationMaxOutputTokens);
		payload.add("generationConfig", generationConfig);

		// Về Safety Settings: Nếu bạn muốn dùng, bạn cần xây dựng cấu trúc JSON cho
		// "safetySettings"
		// dựa trên các giá trị từ AppConfig và thêm vào payload.
		// Ví dụ:
		// JsonArray safetySettingsArray = new JsonArray();
		// JsonObject harassmentSetting = new JsonObject();
		// harassmentSetting.addProperty("category", "HARM_CATEGORY_HARASSMENT");
		// harassmentSetting.addProperty("threshold",
		// AppConfig.getSafetySettingHarassment());
		// safetySettingsArray.add(harassmentSetting);
		// ... (tương tự cho các category khác) ...
		// payload.add("safetySettings", safetySettingsArray);

		String apiUrl = GEMINI_API_BASE_URL + modelToUse + ":generateContent?key=" + this.apiKey; // Sử dụng this.apiKey
		RequestBody body = RequestBody.create(payload.toString(), mediaType);
		Request request = new Request.Builder().url(apiUrl).post(body).addHeader("Content-Type", "application/json")
				.build();

		logger.info("Đang gửi yêu cầu '{}' đến Gemini model: {}. URL: {}", requestTypeDescription, modelToUse, apiUrl);
		// logger.debug("Payload cho '{}': {}", requestTypeDescription,
		// payload.toString());

		try (Response response = httpClient.newCall(request).execute()) {
			// ... (phần xử lý response giữ nguyên) ...
			String responseBodyString = response.body() != null ? response.body().string() : null;

			if (!response.isSuccessful() || responseBodyString == null) {
				String errorMsg = String.format("Lỗi khi gọi Gemini API cho '%s'. HTTP %d: %s.", requestTypeDescription,
						response.code(), response.message());
				logger.error(errorMsg + (responseBodyString != null
						? " Body: " + responseBodyString.substring(0, Math.min(responseBodyString.length(), 500))
						: ""));

				if (response.code() == 400 && responseBodyString != null
						&& responseBodyString.toLowerCase().contains("api key not valid")) {
					return "Lỗi: API Key của Google AI (Gemini) không hợp lệ hoặc đã hết hạn. Vui lòng kiểm tra lại.";
				}
				if (response.code() == 429) {
					return "Lỗi: Đã vượt quá giới hạn số lượng yêu cầu đến Gemini API. Vui lòng thử lại sau.";
				}
				return "Lỗi: Không nhận được phản hồi thành công từ AI. Mã lỗi: " + response.code();
			}

			JsonElement jsonElement = JsonParser.parseString(responseBodyString);
			JsonObject responseObject = jsonElement.getAsJsonObject();

			if (responseObject.has("promptFeedback")) {
				JsonElement promptFeedbackElement = responseObject.get("promptFeedback");
				if (promptFeedbackElement.isJsonObject()) {
					JsonObject promptFeedback = promptFeedbackElement.getAsJsonObject();
					if (promptFeedback.has("blockReason")) {
						String blockReason = promptFeedback.get("blockReason").getAsString();
						String blockMessage = promptFeedback.has("blockReasonMessage")
								? promptFeedback.get("blockReasonMessage").getAsString()
								: "Không có thông báo cụ thể.";
						logger.warn("Prompt bị chặn bởi Gemini. Lý do: {}, Message: {}", blockReason, blockMessage);
						return "Lỗi: Yêu cầu của bạn đã bị chặn bởi bộ lọc của AI. Lý do: " + blockReason
								+ (blockMessage.isEmpty() ? "" : " (" + blockMessage + ")");
					}
				}
			}

			if (!responseObject.has("candidates") || !responseObject.get("candidates").isJsonArray()
					|| responseObject.getAsJsonArray("candidates").isEmpty()) {
				logger.warn("Phản hồi từ Gemini không chứa 'candidates' hợp lệ. Response: {}",
						responseBodyString.substring(0, Math.min(responseBodyString.length(), 500)));
				return "Lỗi: AI không trả về nội dung gợi ý hợp lệ (không có candidates).";
			}

			JsonArray candidates = responseObject.getAsJsonArray("candidates");
			JsonObject firstCandidate = candidates.get(0).getAsJsonObject();

			if (firstCandidate.has("finishReason")) {
				String finishReason = firstCandidate.get("finishReason").getAsString();
				if (!"STOP".equalsIgnoreCase(finishReason) && !"MAX_TOKENS".equalsIgnoreCase(finishReason)) {
					StringBuilder reasonDetails = new StringBuilder("Lý do kết thúc không mong muốn: " + finishReason);
					if ("SAFETY".equalsIgnoreCase(finishReason) && firstCandidate.has("safetyRatings")) {
						JsonArray safetyRatings = firstCandidate.getAsJsonArray("safetyRatings");
						reasonDetails.append(" (Chi tiết an toàn: ");
						for (JsonElement ratingElement : safetyRatings) {
							JsonObject ratingObject = ratingElement.getAsJsonObject();
							if (ratingObject.has("blocked") && ratingObject.get("blocked").getAsBoolean()) {
								reasonDetails.append(ratingObject.get("category").getAsString()).append(" bị chặn, ");
							}
						}
						if (reasonDetails.toString().endsWith(", ")) {
							reasonDetails.setLength(reasonDetails.length() - 2);
						}
						reasonDetails.append(")");
					}
					logger.warn("Phản hồi từ AI không hoàn thành với lý do 'STOP' hoặc 'MAX_TOKENS'. {}",
							reasonDetails.toString());
					String partialText = extractTextFromCandidate(firstCandidate);
					if (!partialText.isEmpty()) {
						return partialText + "\n\nCẢNH BÁO: " + reasonDetails.toString();
					}
					return "Lỗi: AI không hoàn thành việc tạo nội dung. " + reasonDetails.toString();
				}
			}

			String generatedText = extractTextFromCandidate(firstCandidate);

			if (generatedText.isEmpty()) {
				logger.warn("AI đã trả về phản hồi nhưng không có nội dung text. FinishReason: {}. Response: {}",
						firstCandidate.has("finishReason") ? firstCandidate.get("finishReason").getAsString() : "N/A",
						responseBodyString.substring(0, Math.min(responseBodyString.length(), 500)));
				return "Thông báo: AI không tạo ra nội dung văn bản cho yêu cầu này, mặc dù có phản hồi.";
			}
			return generatedText.trim();

		} catch (IOException e) {
			logger.error("Lỗi IO khi gọi Gemini API cho '{}': ", requestTypeDescription, e);
			return "Lỗi: Không thể kết nối đến dịch vụ AI. Vui lòng kiểm tra kết nối mạng.";
		} catch (JsonSyntaxException e) {
			logger.error("Lỗi phân tích JSON từ phản hồi của Gemini cho '{}': ", requestTypeDescription, e);
			return "Lỗi: Không thể đọc phản hồi từ dịch vụ AI (JSON không hợp lệ).";
		} catch (IllegalStateException | NullPointerException e) {
			logger.error("Lỗi xử lý cấu trúc JSON không mong đợi từ Gemini cho '{}': ", requestTypeDescription, e);
			return "Lỗi: Phản hồi từ AI có cấu trúc không mong đợi.";
		} catch (Exception e) {
			logger.error("Lỗi không xác định khi thực thi yêu cầu Gemini cho '{}': ", requestTypeDescription, e);
			return "Lỗi: Có lỗi không mong muốn xảy ra khi tương tác với AI.";
		}
	}

	private String extractTextFromCandidate(JsonObject candidate) {
		// ... (giữ nguyên) ...
		if (!candidate.has("content") || !candidate.getAsJsonObject("content").has("parts")
				|| !candidate.getAsJsonObject("content").get("parts").isJsonArray()
				|| candidate.getAsJsonObject("content").getAsJsonArray("parts").isEmpty()) {
			logger.warn("Candidate không chứa 'content.parts' hợp lệ. Candidate: {}",
					candidate.toString().substring(0, Math.min(candidate.toString().length(), 200)));
			return "";
		}

		JsonArray parts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
		StringBuilder generatedText = new StringBuilder();
		for (JsonElement partElement : parts) {
			JsonObject partObject = partElement.getAsJsonObject();
			if (partObject.has("text")) {
				generatedText.append(partObject.get("text").getAsString());
			}
		}
		return generatedText.toString();
	}
}