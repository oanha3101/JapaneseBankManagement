// src/main/java/com/quanlynganhangdethi/service/DeThiGeneratorService.java
package com.quanlynganhangdethi.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

// import javax.swing.JProgressBar; // Không cần trực tiếp ở đây nữa
// import javax.swing.SwingUtilities; // Không cần trực tiếp ở đây nữa

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.quanlynganhangdethi.models.CauHoi;
import com.quanlynganhangdethi.models.DapAn;
import com.quanlynganhangdethi.models.DeThi;

public class DeThiGeneratorService {

	private CauHoiService cauHoiService;
	private Random random = new Random();
	// Font sẽ được load cục bộ trong các phương thức xuất PDF

	public enum OutputType {
		DE_THI_ONLY_PDF("Chỉ Đề Thi (PDF)"), // Đã đổi tên để rõ ràng hơn là PDF
		DAP_AN_CHI_TIET_PDF("Chỉ Đáp Án Chi Tiết (PDF)"), DE_THI_PDF_DAP_AN_TXT("Đề Thi (PDF) & Đáp Án Ngắn Gọn (TXT)"),
		CAU_HOI_VA_DAP_AN_TXT("Toàn bộ Câu hỏi & Đáp án (TXT)"); // Thêm loại này nếu bạn cần

		private final String displayName;

		OutputType(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	// SỬA CONSTRUCTOR: Nhận CauHoiService
	public DeThiGeneratorService(CauHoiService cauHoiService) {
		this.cauHoiService = cauHoiService;
		if (this.cauHoiService == null) {
			System.err.println(
					"[DeThiGeneratorService WARN] CauHoiService được truyền vào là null. Một số chức năng có thể không hoạt động.");
		}
	}

	// Nếu bạn muốn có một constructor mặc định cho trường hợp exportSpecificDeThi
	// không cần cauHoiService
	// (ví dụ DeThi object đã chứa đủ thông tin câu hỏi)
	public DeThiGeneratorService() {
		this.cauHoiService = null; // Hoặc new CauHoiService() nếu muốn mặc định
		System.out
				.println("[DeThiGeneratorService INFO] Được tạo với constructor không tham số. CauHoiService là null.");
	}

	// --- HÀM HELPER ĐỂ LOAD FONT CỤC BỘ CHO MỘT PDDocument ---
	private PDType0Font loadFontForDocument(PDDocument document, String fontResourcePath) {
		System.out.println("[FONTLOADER_LOCAL] Bắt đầu load font cho document: " + fontResourcePath);
		PDType0Font loadedFont = null;
		try (InputStream fontStream = getClass().getResourceAsStream(fontResourcePath)) {
			if (fontStream != null) {
				loadedFont = PDType0Font.load(document, fontStream); // Load VÀO document hiện tại
				System.out.println("[FONTLOADER_LOCAL] Đã load font '"
						+ (loadedFont != null ? loadedFont.getName() : "LỖI") + "' từ resources.");
			} else {
				System.err.println("[FONTLOADER_LOCAL] Không tìm thấy font '" + fontResourcePath
						+ "' trong resources. Thử hệ thống.");
				String[] systemFontPaths = { "C:/Windows/Fonts/YuGothR.ttc", "C:/Windows/Fonts/msgothic.ttc",
						"/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc" }; // Ví dụ
				for (String path : systemFontPaths) {
					File fontFile = new File(path);
					if (fontFile.exists()) {
						try {
							loadedFont = PDType0Font.load(document, fontFile);
							System.out.println("[FONTLOADER_LOCAL] Đã load font hệ thống: " + fontFile.getName());
							break;
						} catch (IOException ioeSys) {
							System.err.println("[FONTLOADER_LOCAL] Lỗi IO khi load font hệ thống " + path + ": "
									+ ioeSys.getMessage());
						}
					}
				}
			}
		} catch (IOException e) {
			System.err.println("[FONTLOADER_LOCAL ERROR] Lỗi IO khi load font: " + e.getMessage());
			e.printStackTrace(); // Quan trọng để debug
		}
		if (loadedFont == null) {
			System.err
					.println("[FONTLOADER_LOCAL CẢNH BÁO] Không thể load font tiếng Nhật. PDF sẽ không hiển thị đúng.");
			// Không nên ném lỗi ở đây, hãy để các hàm gọi xử lý nếu font là bắt buộc
		}
		return loadedFont;
	}

	public static class TieuChiChonCauHoi {
		public int idChuDe;
		public int soLuong;
		public String loaiCauHoi;

		public TieuChiChonCauHoi(int idChuDe, int soLuong) {
			this.idChuDe = idChuDe;
			this.soLuong = soLuong;
			this.loaiCauHoi = null;
		}

		public TieuChiChonCauHoi(int idChuDe, String loaiCauHoi, int soLuong) {
			this.idChuDe = idChuDe;
			this.loaiCauHoi = loaiCauHoi;
			this.soLuong = soLuong;
		}
	}

	private CauHoi cloneCauHoiWithDapAn(CauHoi original) {
		if (original == null)
			return null;
		CauHoi clone = new CauHoi();
		clone.setId(original.getId());
		clone.setNoiDung(original.getNoiDung());
		clone.setLoaiCauHoi(original.getLoaiCauHoi());
		clone.setDoKho(original.getDoKho());
		clone.setIdChuDe(original.getIdChuDe());
		clone.setAudioPath(original.getAudioPath()); // Giả sử audioPath là String
		if (original.getDapAnList() != null) {
			List<DapAn> clonedDapAnList = new ArrayList<>();
			for (DapAn daOriginal : original.getDapAnList()) {
				DapAn daClone = new DapAn();
				daClone.setId(daOriginal.getId());
				daClone.setIdCauHoi(daOriginal.getIdCauHoi());
				daClone.setNoiDung(daOriginal.getNoiDung());
				daClone.setLaDapAnDung(daOriginal.isLaDapAnDung());
				clonedDapAnList.add(daClone);
			}
			clone.setDapAnList(clonedDapAnList);
		} else {
			clone.setDapAnList(new ArrayList<>());
		}
		return clone;
	}

	// --- GENERATE ĐỀ THI THỬ NGẪU NHIÊN ---
	public void generateDeThiThu(List<TieuChiChonCauHoi> danhSachTieuChi, int soLuongDeCanTao, String thuMucLuu,
			JProgressBar progressBar, OutputType outputType) throws Exception { // progressBar vẫn giữ ở đây nếu hàm này
																				// được gọi từ chỗ khác
		System.out.println("[GENERATOR] Bắt đầu generateDeThiThu. OutputType: " + outputType);
		if (this.cauHoiService == null && !danhSachTieuChi.isEmpty()) {
			throw new IllegalStateException("CauHoiService chưa được khởi tạo, không thể tạo đề thi từ tiêu chí.");
		}

		List<CauHoi> nganHangCauHoiGoc = new ArrayList<>();
		for (TieuChiChonCauHoi tc : danhSachTieuChi) {
			System.out.println("[GENERATOR] Xử lý tiêu chí: CĐ_ID=" + tc.idChuDe + ", Loại='"
					+ (tc.loaiCauHoi == null ? "Tất cả" : tc.loaiCauHoi) + "', SL=" + tc.soLuong);
			List<CauHoi> cauHoiTheoChuDe = null;
			try {
				cauHoiTheoChuDe = cauHoiService.getCauHoiByChuDeId(tc.idChuDe); // Cần cauHoiService
			} catch (SQLException e) {
				System.err.println("[GENERATOR ERROR] SQL khi lấy CH CĐ " + tc.idChuDe + ": " + e.getMessage());
				e.printStackTrace(); // Log lỗi và tiếp tục nếu có thể
				continue; // Bỏ qua tiêu chí này nếu không lấy được câu hỏi
			}

			if (cauHoiTheoChuDe != null && !cauHoiTheoChuDe.isEmpty()) {
				List<CauHoi> cauHoiDaLoc = new ArrayList<>(cauHoiTheoChuDe);
				if (tc.loaiCauHoi != null && !tc.loaiCauHoi.isEmpty()
						&& !"Tất cả Loại".equalsIgnoreCase(tc.loaiCauHoi)) {
					cauHoiDaLoc.removeIf(
							ch -> ch.getLoaiCauHoi() == null || !ch.getLoaiCauHoi().equalsIgnoreCase(tc.loaiCauHoi));
				}
				if (cauHoiDaLoc.isEmpty()) {
					System.out.println("[GENERATOR] Không có CH cho CĐ_ID: " + tc.idChuDe + " sau lọc loại '"
							+ tc.loaiCauHoi + "'.");
					continue;
				}
				Collections.shuffle(cauHoiDaLoc);
				int slCanLay = Math.min(tc.soLuong, cauHoiDaLoc.size());
				System.out.println("[GENERATOR] CĐ_ID: " + tc.idChuDe + ", có " + cauHoiDaLoc.size() + " câu, lấy "
						+ slCanLay + " câu.");
				for (int k = 0; k < slCanLay; k++) {
					CauHoi cloned = cloneCauHoiWithDapAn(cauHoiDaLoc.get(k));
					if (cloned != null)
						nganHangCauHoiGoc.add(cloned);
				}
			} else {
				System.out.println("[GENERATOR] Không có CH nào cho CĐ_ID: " + tc.idChuDe);
			}
		}
		if (nganHangCauHoiGoc.isEmpty()) {
			throw new Exception("Không có câu hỏi nào trong ngân hàng phù hợp với tất cả các tiêu chí đã chọn.");
		}
		System.out.println("[GENERATOR] Tổng câu hỏi gốc thu thập được: " + nganHangCauHoiGoc.size());

		if (progressBar != null)
			SwingUtilities.invokeLater(() -> progressBar.setValue(0));

		for (int i = 1; i <= soLuongDeCanTao; i++) {
			System.out.println("[GENERATOR] Đang tạo bộ dữ liệu cho đề thi số: " + i + "/" + soLuongDeCanTao);
			int tongSoCauYeuCauTrongDe = danhSachTieuChi.stream().mapToInt(tc -> tc.soLuong).sum();
			List<CauHoi> deThiHienTai = taoBoCauHoiChoMotDe(nganHangCauHoiGoc, danhSachTieuChi, tongSoCauYeuCauTrongDe);

			if (deThiHienTai.isEmpty() && tongSoCauYeuCauTrongDe > 0) {
				System.err.println("[GENERATOR WARNING] Đề thi số " + i + " rỗng (sau khi cố gắng tạo). Bỏ qua.");
				if (progressBar != null) {
					final int prog = i;
					SwingUtilities
							.invokeLater(() -> progressBar.setValue((int) (((double) prog / soLuongDeCanTao) * 100)));
				}
				continue;
			}

			StringBuilder dapAnNganGonBuilder = new StringBuilder();
			StringBuilder dapAnChiTietBuilder = new StringBuilder();
			// StringBuilder cauHoiVaDapAnBuilder = new StringBuilder(); // Cho
			// OutputType.CAU_HOI_VA_DAP_AN_TXT

			String tieuDeDeThiFormatted = "Đề thi Ngẫu Nhiên - Mã Đề " + String.format("%03d", i);

			dapAnNganGonBuilder.append("ĐÁP ÁN NGẮN GỌN - ").append(tieuDeDeThiFormatted).append("\n\n");
			dapAnChiTietBuilder.append("ĐÁP ÁN CHI TIẾT - ").append(tieuDeDeThiFormatted).append("\n\n");
			// if (outputType == OutputType.CAU_HOI_VA_DAP_AN_TXT) {
			// cauHoiVaDapAnBuilder.append("ĐỀ THI VÀ ĐÁP ÁN -
			// ").append(tieuDeDeThiFormatted).append("\n\n");
			// }

			List<CauHoi> deThiChoPDF = deThiHienTai.stream().map(this::cloneCauHoiWithDapAn) // Clone lần nữa để đảm bảo
																								// list riêng cho PDF
					.filter(ch -> ch != null) // Loại bỏ null nếu clone thất bại
					.collect(Collectors.toList());

			for (int qIdx = 0; qIdx < deThiChoPDF.size(); qIdx++) {
				CauHoi cauHoiTrongDePDF = deThiChoPDF.get(qIdx); // Đã clone và có thể đã shuffle đáp án
				CauHoi cauHoiGoc = deThiHienTai.get(qIdx); // List gốc để lấy đáp án đúng
				String cauHoiPrefix = "Câu " + (qIdx + 1) + ": ";

				dapAnNganGonBuilder.append(cauHoiPrefix);
				dapAnChiTietBuilder.append(cauHoiPrefix).append(cauHoiGoc.getNoiDung()).append("\n");
				// if (outputType == OutputType.CAU_HOI_VA_DAP_AN_TXT) {
				// cauHoiVaDapAnBuilder.append(cauHoiPrefix).append(cauHoiGoc.getNoiDung()).append("\n");
				// }

				if (cauHoiGoc.getLoaiCauHoi().toLowerCase().contains("trắc nghiệm") && cauHoiGoc.getDapAnList() != null
						&& !cauHoiGoc.getDapAnList().isEmpty()) {

					if (cauHoiTrongDePDF.getDapAnList() != null) { // Đáp án cho đề PDF được xáo trộn
						Collections.shuffle(cauHoiTrongDePDF.getDapAnList());
					}

					char kyTuLuaChonPDF = 'A'; // Ký tự cho file PDF (đã xáo trộn)
					boolean daGhiDapAnNganGon = false;

					// Xử lý cho đáp án chi tiết và đáp án ngắn gọn dựa trên PDF đã xáo trộn
					if (cauHoiTrongDePDF.getDapAnList() != null) {
						for (DapAn daXaoTron : cauHoiTrongDePDF.getDapAnList()) {
							dapAnChiTietBuilder.append("  ").append(kyTuLuaChonPDF).append(". ")
									.append(daXaoTron.getNoiDung());
							if (daXaoTron.isLaDapAnDung()) { // Kiểm tra isLaDapAnDung từ đối tượng đã xáo trộn
								dapAnNganGonBuilder.append(kyTuLuaChonPDF);
								dapAnChiTietBuilder.append(" (ĐÚNG)");
								daGhiDapAnNganGon = true;
							}
							dapAnChiTietBuilder.append("\n");
							kyTuLuaChonPDF++;
						}
					}
					// if (outputType == OutputType.CAU_HOI_VA_DAP_AN_TXT &&
					// cauHoiGoc.getDapAnList() != null) { // Đáp án cho TXT lấy từ gốc
					// char optGoc = 'A';
					// for(DapAn daGoc : cauHoiGoc.getDapAnList()) {
					// cauHoiVaDapAnBuilder.append(" ").append(optGoc++).append(".
					// ").append(daGoc.getNoiDung());
					// if(daGoc.isLaDapAnDung()) cauHoiVaDapAnBuilder.append(" (*)");
					// cauHoiVaDapAnBuilder.append("\n");
					// }
					// }

					if (!daGhiDapAnNganGon && cauHoiGoc.getDapAnList().stream().anyMatch(DapAn::isLaDapAnDung)) {
						dapAnNganGonBuilder.append("?"); // Có đáp án đúng nhưng không tìm thấy trong list xáo trộn (lỗi
															// logic)
					} else if (!cauHoiGoc.getDapAnList().stream().anyMatch(DapAn::isLaDapAnDung)) {
						dapAnNganGonBuilder.append("[K]"); // Không có đáp án nào được đánh dấu là đúng
					}

				} else if (cauHoiGoc.getDapAnList() != null && !cauHoiGoc.getDapAnList().isEmpty()) { // Câu hỏi tự luận
																										// có gợi ý
					dapAnNganGonBuilder.append("[Gợi ý]");
					dapAnChiTietBuilder.append("  Gợi ý: ").append(cauHoiGoc.getDapAnList().get(0).getNoiDung());
					// if (outputType == OutputType.CAU_HOI_VA_DAP_AN_TXT) {
					// cauHoiVaDapAnBuilder.append(" Gợi ý:
					// ").append(cauHoiGoc.getDapAnList().get(0).getNoiDung()).append("\n");
					// }
				} else { // Không có đáp án/gợi ý
					dapAnNganGonBuilder.append("[N/A]");
					dapAnChiTietBuilder.append("  [N/A]");
					// if (outputType == OutputType.CAU_HOI_VA_DAP_AN_TXT) {
					// cauHoiVaDapAnBuilder.append(" [N/A]\n");
					// }
				}
				dapAnNganGonBuilder.append("\n");
				dapAnChiTietBuilder.append("\n\n");
				// if (outputType == OutputType.CAU_HOI_VA_DAP_AN_TXT) {
				// cauHoiVaDapAnBuilder.append("\n");
				// }
			}

			File outputDir = new File(thuMucLuu);
			if (!outputDir.exists())
				outputDir.mkdirs();

			String baseFileName = thuMucLuu + File.separator + "DeThiSo_" + String.format("%03d", i);
			switch (outputType) {
			case DE_THI_ONLY_PDF: // Đã đổi tên enum
				xuatDeThiRaPDF(deThiChoPDF, baseFileName + "_DeThi.pdf", tieuDeDeThiFormatted, i);
				break;
			case DAP_AN_CHI_TIET_PDF:
				xuatVanBanRaPDF(dapAnChiTietBuilder.toString(), baseFileName + "_DapAnChiTiet.pdf",
						"Đáp Án Chi Tiết - " + tieuDeDeThiFormatted);
				break;
			case DE_THI_PDF_DAP_AN_TXT:
				xuatDeThiRaPDF(deThiChoPDF, baseFileName + "_DeThi.pdf", tieuDeDeThiFormatted, i);
				luuFileDapAn(dapAnNganGonBuilder.toString(), baseFileName + "_DapAnNganGon.txt");
				break;
			// case CAU_HOI_VA_DAP_AN_TXT:
			// luuFileDapAn(cauHoiVaDapAnBuilder.toString(), baseFileName +
			// "_CauHoiVaDapAn.txt");
			// break;
			default:
				System.err.println("Loại output không xác định trong generateDeThiThu: " + outputType);
				// Có thể ném Exception ở đây
			}
			if (progressBar != null) {
				final int prog = i;
				SwingUtilities.invokeLater(() -> progressBar.setValue((int) (((double) prog / soLuongDeCanTao) * 100)));
			}
		}
		if (progressBar != null)
			SwingUtilities.invokeLater(() -> progressBar.setValue(100));
		System.out.println("Hoàn tất tạo " + soLuongDeCanTao + " đề thi.");
	}

	// --- EXPORT MỘT ĐỀ THI CỤ THỂ ĐÃ CÓ ---
	// PHIÊN BẢN ĐẦY ĐỦ CỦA PHƯƠNG THỨC NÀY (thay thế stub cũ)
	public String exportSpecificDeThi(DeThi deThiDaCo, String outputDirectory, String baseFileName,
			OutputType outputType) throws Exception {

		System.out.println("[SERVICE EXPORT SPECIFIC] Bắt đầu export Đề Thi ID: " + deThiDaCo.getId() + ". OutputType: "
				+ outputType + ". BaseFileName: " + baseFileName + ". Dir: " + outputDirectory);

		List<CauHoi> cauHoiTrongDeGoc = deThiDaCo.getCauHoiList();
		if (cauHoiTrongDeGoc == null || cauHoiTrongDeGoc.isEmpty()) {
			throw new Exception("Đề thi ID " + deThiDaCo.getId() + " (Tiêu đề: '" + deThiDaCo.getTieuDe()
					+ "') không có câu hỏi nào để export.");
		}

		List<CauHoi> deThiDeXuatPDF = new ArrayList<>();
		for (CauHoi chGoc : cauHoiTrongDeGoc) {
			CauHoi chClone = cloneCauHoiWithDapAn(chGoc);
			if (chClone != null && chClone.getLoaiCauHoi() != null
					&& chClone.getLoaiCauHoi().toLowerCase().contains("trắc nghiệm") && chClone.getDapAnList() != null
					&& !chClone.getDapAnList().isEmpty()) {
				Collections.shuffle(chClone.getDapAnList());
			}
			if (chClone != null) {
				deThiDeXuatPDF.add(chClone);
			} else {
				System.err.println("[SERVICE EXPORT] Cảnh báo: cloneCauHoiWithDapAn trả về null cho câu hỏi ID: "
						+ (chGoc != null ? chGoc.getId() : "UNKNOWN_ID"));
			}
		}

		File outputDirFile = new File(outputDirectory);
		if (!outputDirFile.exists()) {
			if (!outputDirFile.mkdirs()) {
				System.err.println(
						"[SERVICE EXPORT] LỖI: Không thể tạo thư mục output: " + outputDirFile.getAbsolutePath());
				throw new IOException("Không thể tạo thư mục lưu file: " + outputDirFile.getAbsolutePath()
						+ ". Vui lòng kiểm tra quyền ghi hoặc tạo thủ công.");
			} else {
				System.out.println("[SERVICE EXPORT] Đã tạo thư mục output: " + outputDirFile.getAbsolutePath());
			}
		}

		String finalBaseFileNameWithPath = outputDirFile.getAbsolutePath() + File.separator + baseFileName;
		String titleDeThiFormatted = deThiDaCo.getTieuDe() + " (Mã Đề: " + String.format("%03d", deThiDaCo.getId())
				+ ")";

		StringBuilder dapAnNganGonBuilder = new StringBuilder();
		StringBuilder dapAnChiTietBuilder = new StringBuilder();
		StringBuilder cauHoiVaDapAnBuilder = new StringBuilder();

		if (outputType == OutputType.DE_THI_PDF_DAP_AN_TXT || outputType == OutputType.DAP_AN_CHI_TIET_PDF
				|| outputType == OutputType.CAU_HOI_VA_DAP_AN_TXT) {

			dapAnNganGonBuilder.append("ĐÁP ÁN NGẮN GỌN - ").append(titleDeThiFormatted).append("\n");
			dapAnNganGonBuilder.append("----------------------------------\n\n");

			if (outputType == OutputType.DAP_AN_CHI_TIET_PDF) {
				dapAnChiTietBuilder.append("ĐÁP ÁN CHI TIẾT - ").append(titleDeThiFormatted).append("\n");
				dapAnChiTietBuilder.append("==================================\n\n");
			}
			if (outputType == OutputType.CAU_HOI_VA_DAP_AN_TXT) {
				cauHoiVaDapAnBuilder.append("NỘI DUNG ĐỀ THI VÀ ĐÁP ÁN - ").append(titleDeThiFormatted).append("\n");
				cauHoiVaDapAnBuilder.append("====================================================\n\n");
			}

			for (int qIdx = 0; qIdx < cauHoiTrongDeGoc.size(); qIdx++) {
				CauHoi chGoc = cauHoiTrongDeGoc.get(qIdx);
				CauHoi chDaXaoTronTrongPDF = deThiDeXuatPDF.get(qIdx);
				String cauHoiPrefix = "Câu " + (qIdx + 1) + ": ";

				if (outputType == OutputType.CAU_HOI_VA_DAP_AN_TXT) {
					cauHoiVaDapAnBuilder.append(cauHoiPrefix).append(chGoc.getNoiDung()).append("\n");
					if (chGoc.getLoaiCauHoi().toLowerCase().contains("trắc nghiệm") && chGoc.getDapAnList() != null) {
						char opt = 'A';
						for (DapAn da : chGoc.getDapAnList()) {
							cauHoiVaDapAnBuilder.append("  ").append(opt++).append(". ").append(da.getNoiDung());
							if (da.isLaDapAnDung())
								cauHoiVaDapAnBuilder.append(" (*)");
							cauHoiVaDapAnBuilder.append("\n");
						}
					} else if (chGoc.getDapAnList() != null && !chGoc.getDapAnList().isEmpty()) {
						cauHoiVaDapAnBuilder.append("  Gợi ý: ").append(chGoc.getDapAnList().get(0).getNoiDung())
								.append("\n");
					}
					cauHoiVaDapAnBuilder.append("\n");
				}

				dapAnNganGonBuilder.append(cauHoiPrefix);
				if (outputType == OutputType.DAP_AN_CHI_TIET_PDF) {
					dapAnChiTietBuilder.append(cauHoiPrefix).append(chGoc.getNoiDung()).append("\n");
				}

				if (chGoc.getLoaiCauHoi().toLowerCase().contains("trắc nghiệm") && chGoc.getDapAnList() != null
						&& !chGoc.getDapAnList().isEmpty()) {
					String dapAnDungThucTe_NoiDung = chGoc.getDapAnList().stream().filter(DapAn::isLaDapAnDung)
							.map(DapAn::getNoiDung).findFirst().orElse(null);
					char kyTuLuaChonTrongPDF = 'A';
					boolean daTimThayDapAnDungChoNganGon = false;

					if (chDaXaoTronTrongPDF.getDapAnList() != null) {
						for (DapAn daXaoTron : chDaXaoTronTrongPDF.getDapAnList()) {
							if (outputType == OutputType.DAP_AN_CHI_TIET_PDF) {
								dapAnChiTietBuilder.append("  ").append(kyTuLuaChonTrongPDF).append(". ")
										.append(daXaoTron.getNoiDung());
							}
							if (dapAnDungThucTe_NoiDung != null
									&& dapAnDungThucTe_NoiDung.equals(daXaoTron.getNoiDung())
									&& daXaoTron.isLaDapAnDung()) {
								dapAnNganGonBuilder.append(kyTuLuaChonTrongPDF);
								if (outputType == OutputType.DAP_AN_CHI_TIET_PDF) {
									dapAnChiTietBuilder.append(" (ĐÚNG)");
								}
								daTimThayDapAnDungChoNganGon = true;
							}
							if (outputType == OutputType.DAP_AN_CHI_TIET_PDF) {
								dapAnChiTietBuilder.append("\n");
							}
							kyTuLuaChonTrongPDF++;
						}
					}
					if (!daTimThayDapAnDungChoNganGon) {
						dapAnNganGonBuilder.append(dapAnDungThucTe_NoiDung != null ? "?" : "[K]");
					}
				} else if (chGoc.getDapAnList() != null && !chGoc.getDapAnList().isEmpty()) {
					dapAnNganGonBuilder.append("[Gợi ý]");
					if (outputType == OutputType.DAP_AN_CHI_TIET_PDF) {
						dapAnChiTietBuilder.append("  Gợi ý: ").append(chGoc.getDapAnList().get(0).getNoiDung());
					}
				} else {
					dapAnNganGonBuilder.append("[N/A]");
					if (outputType == OutputType.DAP_AN_CHI_TIET_PDF) {
						dapAnChiTietBuilder.append("  [Không có đáp án/gợi ý]");
					}
				}
				dapAnNganGonBuilder.append("\n");
				if (outputType == OutputType.DAP_AN_CHI_TIET_PDF) {
					dapAnChiTietBuilder.append("\n");
				}
			}
		}

		String filePathChinhTraVe = "";

		switch (outputType) {
		case DE_THI_ONLY_PDF:
			filePathChinhTraVe = finalBaseFileNameWithPath + "_DeThi.pdf";
			System.out.println("[SERVICE EXPORT] Chuẩn bị gọi xuatDeThiRaPDF cho: " + filePathChinhTraVe);
			xuatDeThiRaPDF(deThiDeXuatPDF, filePathChinhTraVe, titleDeThiFormatted, deThiDaCo.getId());
			break;
		case DE_THI_PDF_DAP_AN_TXT:
			filePathChinhTraVe = finalBaseFileNameWithPath + "_DeThi.pdf";
			System.out.println("[SERVICE EXPORT] Chuẩn bị gọi xuatDeThiRaPDF cho: " + filePathChinhTraVe);
			xuatDeThiRaPDF(deThiDeXuatPDF, filePathChinhTraVe, titleDeThiFormatted, deThiDaCo.getId());
			String dapAnNganGonPath = finalBaseFileNameWithPath + "_DapAnNganGon.txt";
			System.out.println("[SERVICE EXPORT] Chuẩn bị gọi luuFileDapAn cho: " + dapAnNganGonPath);
			luuFileDapAn(dapAnNganGonBuilder.toString(), dapAnNganGonPath);
			break;
		case DAP_AN_CHI_TIET_PDF:
			filePathChinhTraVe = finalBaseFileNameWithPath + "_DapAnChiTiet.pdf";
			System.out.println("[SERVICE EXPORT] Chuẩn bị gọi xuatVanBanRaPDF cho: " + filePathChinhTraVe);
			xuatVanBanRaPDF(dapAnChiTietBuilder.toString(), filePathChinhTraVe,
					"Đáp Án Chi Tiết - " + titleDeThiFormatted);
			break;
		case CAU_HOI_VA_DAP_AN_TXT:
			filePathChinhTraVe = finalBaseFileNameWithPath + "_CauHoiVaDapAn.txt";
			System.out.println("[SERVICE EXPORT] Chuẩn bị gọi luuFileDapAn cho: " + filePathChinhTraVe);
			luuFileDapAn(cauHoiVaDapAnBuilder.toString(), filePathChinhTraVe);
			break;
		default:
			System.err.println("[SERVICE EXPORT] LỖI: Loại output không được hỗ trợ trong switch: " + outputType);
			throw new IllegalArgumentException("Loại output không được hỗ trợ: " + outputType);
		}

		System.out.println("[SERVICE EXPORT] Hoàn tất các lệnh gọi xuất file cho Đề Thi ID: " + deThiDaCo.getId()
				+ ". File chính (dự kiến): " + filePathChinhTraVe);

		File checkFile = new File(filePathChinhTraVe);
		if (checkFile.exists() && checkFile.length() > 0) {
			System.out.println("[SERVICE EXPORT] KIỂM TRA: File '" + checkFile.getAbsolutePath()
					+ "' tồn tại và có kích thước > 0.");
		} else if (checkFile.exists()) {
			System.err.println("[SERVICE EXPORT] KIỂM TRA CẢNH BÁO: File '" + checkFile.getAbsolutePath()
					+ "' tồn tại NHƯNG kích thước là 0 byte. Có thể lỗi khi ghi nội dung.");
		} else {
			System.err.println("[SERVICE EXPORT] KIỂM TRA LỖI NGHIÊM TRỌNG: File '" + checkFile.getAbsolutePath()
					+ "' KHÔNG tồn tại sau khi thực hiện ghi!");
			// Ném lỗi ở đây nếu việc file không tồn tại là không chấp nhận được
			throw new IOException("Không thể tạo hoặc ghi file kết quả: " + checkFile.getAbsolutePath());
		}
		return filePathChinhTraVe;
	}

	private List<CauHoi> taoBoCauHoiChoMotDe(List<CauHoi> nganHangGoc, List<TieuChiChonCauHoi> danhSachTieuChi,
			int tongSoCauYeuCau) {
		List<CauHoi> deThi = new ArrayList<>();
		// Clone nganHangGoc để không làm thay đổi list gốc bên ngoài
		List<CauHoi> tempNganHang = nganHangGoc.stream().map(this::cloneCauHoiWithDapAn).filter(ch -> ch != null)
				.collect(Collectors.toList());
		int soCauDaThemVaoDe = 0;

		for (TieuChiChonCauHoi tc : danhSachTieuChi) {
			if (soCauDaThemVaoDe >= tongSoCauYeuCau && tongSoCauYeuCau > 0) // Nếu đã đủ tổng số câu yêu cầu
				break;

			int daLayChoTieuChiHienTai = 0;
			List<CauHoi> cauHoiPhuHopTieuChi = new ArrayList<>();
			// Lặp qua tempNganHang để tìm câu hỏi phù hợp
			for (CauHoi chTrongTemp : tempNganHang) {
				boolean phuHopChuDe = (chTrongTemp.getIdChuDe() == tc.idChuDe);
				boolean phuHopLoaiCauHoi = true; // Mặc định là phù hợp nếu không có loại cụ thể
				if (tc.loaiCauHoi != null && !tc.loaiCauHoi.isEmpty()
						&& !"Tất cả Loại".equalsIgnoreCase(tc.loaiCauHoi)) {
					phuHopLoaiCauHoi = chTrongTemp.getLoaiCauHoi() != null
							&& chTrongTemp.getLoaiCauHoi().equalsIgnoreCase(tc.loaiCauHoi);
				}

				// Kiểm tra xem câu hỏi đã có trong deThi chưa (theo ID)
				final int currentChId = chTrongTemp.getId();
				boolean daCoTrongDe = deThi.stream().anyMatch(c -> c.getId() == currentChId);

				if (phuHopChuDe && phuHopLoaiCauHoi && !daCoTrongDe) {
					cauHoiPhuHopTieuChi.add(chTrongTemp);
				}
			}
			Collections.shuffle(cauHoiPhuHopTieuChi); // Xáo trộn các câu hỏi phù hợp với tiêu chí này

			for (CauHoi chToAdd : cauHoiPhuHopTieuChi) {
				if (daLayChoTieuChiHienTai >= tc.soLuong) // Đã đủ số lượng cho tiêu chí này
					break;
				if (soCauDaThemVaoDe >= tongSoCauYeuCau && tongSoCauYeuCau > 0) // Đã đủ tổng số câu
					break;

				deThi.add(chToAdd); // Thêm vào đề thi
				// Xóa khỏi tempNganHang để không bị chọn lại cho các tiêu chí khác (nếu logic
				// yêu cầu vậy)
				// Hoặc không xóa nếu một câu có thể thuộc nhiều tiêu chí và được dùng lại (cẩn
				// thận trùng lặp)
				// Hiện tại logic là không được trùng câu hỏi trong một đề
				tempNganHang.removeIf(c -> c.getId() == chToAdd.getId());
				daLayChoTieuChiHienTai++;
				soCauDaThemVaoDe++;
			}
		}

		// Nếu sau khi duyệt hết các tiêu chí mà vẫn chưa đủ tổng số câu yêu cầu,
		// và tempNganHang vẫn còn câu hỏi chưa được dùng
		if (soCauDaThemVaoDe < tongSoCauYeuCau && !tempNganHang.isEmpty()) {
			Collections.shuffle(tempNganHang); // Xáo trộn phần còn lại
			int canLayThemSoLuong = tongSoCauYeuCau - soCauDaThemVaoDe;
			int soCauThucTeDaLayThem = 0;
			for (int k = 0; k < tempNganHang.size() && soCauThucTeDaLayThem < canLayThemSoLuong; k++) {
				final CauHoi cauHoiPotential = tempNganHang.get(k);
				// Kiểm tra lại lần nữa (dù đã removeIf, nhưng để chắc chắn)
				boolean daCoTrongDeThi = deThi.stream().anyMatch(c -> c.getId() == cauHoiPotential.getId());
				if (!daCoTrongDeThi) {
					deThi.add(cauHoiPotential);
					soCauDaThemVaoDe++;
					soCauThucTeDaLayThem++;
				}
			}
		}
		Collections.shuffle(deThi); // Xáo trộn thứ tự các câu hỏi trong đề thi cuối cùng
		System.out.println(
				"[TAOBO] Tổng số câu trong đề thi được tạo: " + deThi.size() + " (Yêu cầu: " + tongSoCauYeuCau + ")");
		return deThi;
	}

	private void xuatDeThiRaPDF(List<CauHoi> deThi, String filePath, String tieuDeChinh, int maDe) throws IOException {
		System.out.println("[PDF EXPORT] Đang xuất ĐỀ THI vào: " + new File(filePath).getAbsolutePath());
		try (PDDocument document = new PDDocument()) {
			PDType0Font fontForPDF = loadFontForDocument(document, "/fonts/NotoSansJP-Regular.ttf");

			if (fontForPDF == null) {
				System.err.println(
						"[PDF EXPORT ERROR] Không thể load font tiếng Nhật. PDF sẽ không được tạo cho: " + filePath);
				throw new IOException("Lỗi nghiêm trọng: Không thể load font để tạo PDF đề thi: " + filePath);
			}

			PDPage currentPage = new PDPage(PDRectangle.A4); // Sử dụng A4
			document.addPage(currentPage);
			PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

			final float margin = 50;
			float yPosition = currentPage.getMediaBox().getHeight() - margin - 30; // Lấy chiều cao từ mediaBox
			final float pageTopY = currentPage.getMediaBox().getHeight() - margin - 30;
			final float pageBottomLimit = margin + 40; // Giới hạn dưới cùng của trang
			final float leadingNormal = 18f;
			final float leadingDapAn = 16f;
			final float fontSizeCauHoi = 11f;
			final float fontSizeDapAn = 10f;
			final float fontSizeTieuDe = 16f;
			final float fontSizeInfo = 10f;

			try {
				// Vẽ tiêu đề
				yPosition = drawTextLinesHelper(contentStream, Collections.singletonList(tieuDeChinh), margin,
						yPosition, fontForPDF, fontSizeTieuDe, leadingNormal * 1.2f,
						currentPage.getMediaBox().getWidth() - 2 * margin);
				yPosition -= leadingNormal * 0.5f; // Khoảng cách nhỏ

				// Vẽ thông tin đề
				String infoLine = "Mã đề: " + String.format("%03d", maDe) + "          Thời gian làm bài: ..... phút"; // Để
																														// trống
																														// thời
																														// gian
				yPosition = drawTextLinesHelper(contentStream, Collections.singletonList(infoLine), margin, yPosition,
						fontForPDF, fontSizeInfo, leadingNormal, currentPage.getMediaBox().getWidth() - 2 * margin);
				yPosition -= leadingNormal * 1.5f; // Khoảng cách lớn hơn trước khi bắt đầu câu hỏi

				for (int i = 0; i < deThi.size(); i++) {
					CauHoi ch = deThi.get(i);
					if (ch == null) {
						System.err.println("[PDF EXPORT WARN] Câu hỏi null ở vị trí " + i + " trong đề thi PDF.");
						continue;
					}
					String cauHoiDisplay = (i + 1) + ". "
							+ (ch.getNoiDung() != null ? ch.getNoiDung() : "[Nội dung bị thiếu]");

					float availableWidth = currentPage.getMediaBox().getWidth() - 2 * margin;
					float qHeight = ướcLuongChieuCaoText(cauHoiDisplay, availableWidth, fontForPDF, fontSizeCauHoi,
							leadingNormal);
					float aHeight = 0;

					if (ch.getLoaiCauHoi() != null && ch.getLoaiCauHoi().toLowerCase().contains("trắc nghiệm")
							&& ch.getDapAnList() != null) {
						for (DapAn da : ch.getDapAnList()) {
							aHeight += ướcLuongChieuCaoText(
									"     A. " + (da.getNoiDung() != null ? da.getNoiDung() : "[Đáp án thiếu]"),
									availableWidth - 20, fontForPDF, fontSizeDapAn, leadingDapAn);
						}
					}
					float totalBlockHeightNeeded = qHeight + aHeight + (leadingNormal * 0.5f); // Thêm chút đệm

					// Kiểm tra nếu cần sang trang mới
					if (yPosition - totalBlockHeightNeeded < pageBottomLimit && i > 0) { // Chỉ sang trang nếu không
																							// phải câu đầu tiên
						contentStream.close();
						currentPage = new PDPage(PDRectangle.A4);
						document.addPage(currentPage);
						contentStream = new PDPageContentStream(document, currentPage);
						yPosition = pageTopY;
						System.out.println("[PDF EXPORT] (Đề thi) Tạo trang mới cho câu " + (i + 1));
					}

					// Vẽ câu hỏi
					yPosition = drawTextLinesHelper(contentStream,
							splitTextIntoLines(cauHoiDisplay, availableWidth, fontForPDF, fontSizeCauHoi), margin,
							yPosition, fontForPDF, fontSizeCauHoi, leadingNormal, availableWidth);

					// Vẽ đáp án nếu là trắc nghiệm
					if (ch.getLoaiCauHoi() != null && ch.getLoaiCauHoi().toLowerCase().contains("trắc nghiệm")
							&& ch.getDapAnList() != null) {
						char optionChar = 'A';
						for (DapAn da : ch.getDapAnList()) {
							if (da == null)
								continue;
							String dapAnText = "     " + optionChar + ". "
									+ (da.getNoiDung() != null ? da.getNoiDung() : "[Đáp án thiếu]");
							float singleAnswerHeight = ướcLuongChieuCaoText(dapAnText, availableWidth - 20, fontForPDF,
									fontSizeDapAn, leadingDapAn);

							if (yPosition - singleAnswerHeight < pageBottomLimit) { // Kiểm tra cho từng đáp án
								contentStream.close();
								currentPage = new PDPage(PDRectangle.A4);
								document.addPage(currentPage);
								contentStream = new PDPageContentStream(document, currentPage);
								yPosition = pageTopY;
								System.out.println("[PDF EXPORT] (Đề thi) Tạo trang mới cho đáp án của câu " + (i + 1));
							}
							yPosition = drawTextLinesHelper(contentStream,
									splitTextIntoLines(dapAnText, availableWidth - 20, fontForPDF, fontSizeDapAn),
									margin + 20, yPosition, fontForPDF, fontSizeDapAn, leadingDapAn,
									availableWidth - 20);
							optionChar++;
						}
					}
					yPosition -= leadingNormal * 0.5f; // Khoảng cách nhỏ sau mỗi khối câu hỏi-đáp án
				}
			} finally {
				if (contentStream != null) {
					try {
						contentStream.close();
					} catch (IOException e) {
						System.err.println("[PDF EXPORT ERROR] Lỗi khi đóng contentStream (Đề thi): " + e.getMessage());
					}
				}
			}
			File pdfFile = new File(filePath);
			document.save(pdfFile); // Lưu vào đối tượng File
			System.out.println("[PDF EXPORT] Đã xuất PDF ĐỀ THI thành công vào: " + pdfFile.getAbsolutePath());
		} catch (IOException e) {
			System.err.println(
					"[PDF EXPORT ERROR] Lỗi IO chính trong xuatDeThiRaPDF cho " + filePath + ": " + e.getMessage());
			e.printStackTrace();
			throw e; // Ném lại lỗi để bên gọi xử lý
		}
	}

	private void xuatVanBanRaPDF(String noiDungVanBan, String filePath, String tieuDeChinh) throws IOException {
		System.out.println(
				"[PDF EXPORT] Đang xuất VĂN BẢN (Đáp án chi tiết) vào: " + new File(filePath).getAbsolutePath());
		try (PDDocument document = new PDDocument()) {
			PDType0Font fontForPDF = loadFontForDocument(document, "/fonts/NotoSansJP-Regular.ttf");

			if (fontForPDF == null) {
				System.err.println(
						"[PDF EXPORT ERROR] Không thể load font tiếng Nhật. PDF sẽ không được tạo cho: " + filePath);
				throw new IOException("Lỗi nghiêm trọng: Không thể load font để tạo PDF văn bản: " + filePath);
			}

			PDPage currentPage = new PDPage(PDRectangle.A4);
			document.addPage(currentPage);
			PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

			final float margin = 50;
			float yPosition = currentPage.getMediaBox().getHeight() - margin - 30;
			final float pageTopY = currentPage.getMediaBox().getHeight() - margin - 30;
			final float pageBottomLimit = margin + 30;
			float leading = 16f;
			float fontSize = 10f;
			float fontSizeTieuDeVanBan = 14f;

			try {
				yPosition = drawTextLinesHelper(contentStream, Collections.singletonList(tieuDeChinh), margin,
						yPosition, fontForPDF, fontSizeTieuDeVanBan, leading * 1.2f,
						currentPage.getMediaBox().getWidth() - 2 * margin);
				yPosition -= leading * 1.5f; // Khoảng cách sau tiêu đề

				List<String> lines = splitTextIntoLines(noiDungVanBan,
						currentPage.getMediaBox().getWidth() - 2 * margin, fontForPDF, fontSize);
				for (String line : lines) {
					if (yPosition - leading < pageBottomLimit) { // Nếu dòng tiếp theo sẽ tràn trang
						contentStream.close();
						currentPage = new PDPage(PDRectangle.A4);
						document.addPage(currentPage);
						contentStream = new PDPageContentStream(document, currentPage);
						yPosition = pageTopY;
						System.out.println("[PDF EXPORT] (Văn bản) Tạo trang mới.");
					}
					yPosition = drawTextLinesHelper(contentStream, Collections.singletonList(line), margin, yPosition,
							fontForPDF, fontSize, leading, currentPage.getMediaBox().getWidth() - 2 * margin);
				}
			} finally {
				if (contentStream != null) {
					try {
						contentStream.close();
					} catch (IOException e) {
						System.err
								.println("[PDF EXPORT ERROR] Lỗi khi đóng contentStream (Văn bản): " + e.getMessage());
					}
				}
			}
			File pdfFile = new File(filePath);
			document.save(pdfFile);
			System.out.println("[PDF EXPORT] Đã xuất PDF VĂN BẢN thành công vào: " + pdfFile.getAbsolutePath());
		} catch (IOException e) {
			System.err.println(
					"[PDF EXPORT ERROR] Lỗi IO chính trong xuatVanBanRaPDF cho " + filePath + ": " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	// Sửa drawTextLinesHelper để nhận thêm maxWidth cho việc canh lề (nếu cần, hiện
	// tại chưa dùng)
	private float drawTextLinesHelper(PDPageContentStream contentStream, List<String> lines, float x, float yStart,
			PDType0Font font, float fontSize, float leading, float maxWidth) throws IOException { // Thêm maxWidth
		if (lines == null || lines.isEmpty() || font == null) { // Kiểm tra font null
			if (font == null)
				System.err.println("[drawTextLinesHelper] Font is null!");
			return yStart;
		}

		float currentY = yStart;
		contentStream.setFont(font, fontSize);

		for (String line : lines) {
			String lineToDraw = (line != null ? line : "");
			// Di chuyển đến vị trí bắt đầu của dòng mới
			// Cần reset vị trí text matrix cho mỗi dòng nếu không dùng newLineAtOffset(0,
			// -leading)
			contentStream.beginText();
			contentStream.newLineAtOffset(x, currentY);
			try {
				contentStream.showText(lineToDraw);
			} catch (IllegalArgumentException iae) {
				System.err.println("[drawTextLinesHelper ERROR] Lỗi showText: '"
						+ lineToDraw.substring(0, Math.min(20, lineToDraw.length())) + "...' - Font: " + font.getName()
						+ ". Lỗi: " + iae.getMessage());
				try {
					PDType1Font fallback = PDType1Font.HELVETICA;
					contentStream.setFont(fallback, fontSize);
					contentStream.showText("[LỗiKT]");
					contentStream.setFont(font, fontSize); // Set lại font cũ
				} catch (Exception exInner) {
					/* Bỏ qua */ }
			}
			contentStream.endText();
			currentY -= leading; // Giảm yPosition cho dòng tiếp theo
		}
		return currentY;
	}

	private float ướcLuongChieuCaoText(String text, float maxWidth, PDType0Font font, float fontSize, float leading)
			throws IOException {
		if (text == null || text.isEmpty())
			return 0;
		if (font == null) {
			System.err.println("[ướcLuongChieuCaoText] Font is null. Ước lượng 1 dòng.");
			return leading;
		}
		List<String> lines = splitTextIntoLines(text, maxWidth, font, fontSize);
		return lines.size() * leading;
	}

	private List<String> splitTextIntoLines(String text, float maxWidth, PDType0Font font, float fontSize)
			throws IOException {
		List<String> lines = new ArrayList<>();
		if (text == null || text.isEmpty()) {
			lines.add(""); // Trả về một dòng trống để tránh lỗi ở vòng lặp
			return lines;
		}
		if (font == null) {
			System.err.println("[splitTextIntoLines] Font is null. Trả về text trên một dòng.");
			lines.add(text); // Không thể tính toán, trả về text gốc
			return lines;
		}

		// Xử lý trường hợp text có nhiều dòng sẵn (tách bằng \n)
		String[] paragraphs = text.split("\\R"); // Tách theo các loại ngắt dòng
		for (String paragraph : paragraphs) {
			if (paragraph.trim().isEmpty() && lines.size() > 0 && !lines.get(lines.size() - 1).trim().isEmpty()) {
				lines.add(""); // Thêm dòng trống nếu đoạn là trống (giữ khoảng cách giữa các đoạn)
				continue;
			}

			// Sử dụng biểu thức regex đã được cải thiện từ trước
			String[] words = paragraph.split(
					"(?<=\\s)|(?=\\s)|(?<=[A-Za-z0-9.,!?()\\[\\]])(?=[\\u3000-\\u303F\\u3040-\\u309F\\u30A0-\\u30FF\\uFF00-\\uFFEF\\u4E00-\\u9FAF])|(?<=[\\u3000-\\u303F\\u3040-\\u309F\\u30A0-\\u30FF\\uFF00-\\uFFEF\\u4E00-\\u9FAF])(?=[A-Za-z0-9.,!?()\\[\\]])");
			StringBuilder currentLine = new StringBuilder();

			for (String wordToken : words) {
				if (wordToken.isEmpty())
					continue;

				String wordToAdd = wordToken;
				// Xử lý khoảng trắng: chỉ thêm nếu currentLine không rỗng và wordToken không
				// bắt đầu bằng khoảng trắng
				if (currentLine.length() > 0 && !wordToken.matches("^\\s.*")) {
					wordToAdd = " " + wordToken;
				}

				String lineToTest = currentLine.toString() + wordToAdd;
				float width = 0;
				try {
					width = font.getStringWidth(lineToTest.trim()) / 1000 * fontSize;
				} catch (Exception e) { // Bắt cả NullPointerException nếu font là null ở đây
					System.err.println("[splitText ERROR] Lỗi getStringWidth cho: '" + lineToTest.trim() + "' với font "
							+ (font != null ? font.getName() : "NULL") + ". Lỗi: " + e.getMessage());
					// Nếu có lỗi, thêm dòng hiện tại (nếu có) và bắt đầu dòng mới với từ gây lỗi
					if (currentLine.length() > 0) {
						lines.add(currentLine.toString().trim());
					}
					currentLine = new StringBuilder(wordToken.trim()); // Bắt đầu dòng mới với từ này
					continue;
				}

				if (width > maxWidth && currentLine.length() > 0) {
					lines.add(currentLine.toString().trim()); // Thêm dòng cũ
					currentLine = new StringBuilder(wordToken.trim()); // Bắt đầu dòng mới với từ hiện tại (đã trim)
				} else {
					// Nếu là từ đầu tiên của dòng và nó là khoảng trắng, bỏ qua
					if (currentLine.length() == 0 && wordToken.trim().isEmpty()) {
						// không làm gì
					} else {
						currentLine.append(wordToAdd);
					}
				}
			}
			if (currentLine.length() > 0) {
				lines.add(currentLine.toString().trim());
			}
		}
		if (lines.isEmpty()) { // Đảm bảo luôn trả về ít nhất một dòng (có thể trống)
			lines.add("");
		}
		return lines;
	}

	private void luuFileDapAn(String noiDungDapAn, String filePath) throws IOException {
		File txtFile = new File(filePath);
		System.out.println("[TXT EXPORT] Đang lưu file đáp án TXT vào: " + txtFile.getAbsolutePath());
		try (FileWriter writer = new FileWriter(txtFile, false)) { // false để ghi đè file nếu đã tồn tại
			writer.write(noiDungDapAn);
			System.out.println("[TXT EXPORT] Đã lưu file đáp án TXT thành công: " + txtFile.getAbsolutePath());
		} catch (IOException e) {
			System.err.println(
					"[TXT EXPORT ERROR] Lỗi IO khi lưu file TXT " + txtFile.getAbsolutePath() + ": " + e.getMessage());
			e.printStackTrace();
			throw e; // Ném lại lỗi
		}
	}
}