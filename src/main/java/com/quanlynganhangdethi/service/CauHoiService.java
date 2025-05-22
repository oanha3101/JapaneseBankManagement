// src/main/java/com/quanlynganhangdethi/service/CauHoiService.java
package com.quanlynganhangdethi.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quanlynganhangdethi.config.DatabaseConnection;
import com.quanlynganhangdethi.dao.CauHoiDAO;
import com.quanlynganhangdethi.dao.CauHoiDAOImpl;
import com.quanlynganhangdethi.dao.DapAnDAO;
import com.quanlynganhangdethi.dao.DapAnDAOImpl;
import com.quanlynganhangdethi.models.CauHoi;
import com.quanlynganhangdethi.models.DapAn;

public class CauHoiService {
	private static final Logger logger = LoggerFactory.getLogger(CauHoiService.class);
	private final CauHoiDAO cauHoiDAO;
	private final DapAnDAO dapAnDAO;

	public static final String AUDIO_STORAGE_BASE_DIR_NAME = "data_storage";
	private static final String AUDIO_SUB_DIR_NAME = "audio_files";
	private static final File AUDIO_STORAGE_DIR = Paths
			.get(System.getProperty("user.dir"), AUDIO_STORAGE_BASE_DIR_NAME, AUDIO_SUB_DIR_NAME).toFile();
	private static final String AUDIO_DB_RELATIVE_PATH_PREFIX = AUDIO_SUB_DIR_NAME + File.separator;

	public CauHoiService() {
		this.cauHoiDAO = new CauHoiDAOImpl();
		this.dapAnDAO = new DapAnDAOImpl();
		if (!AUDIO_STORAGE_DIR.exists()) {
			if (AUDIO_STORAGE_DIR.mkdirs()) {
				logger.info("Đã tạo thư mục lưu trữ audio: {}", AUDIO_STORAGE_DIR.getAbsolutePath());
			} else {
				logger.error("Không thể tạo thư mục lưu trữ audio: {}. Vui lòng kiểm tra quyền ghi hoặc tạo thủ công.",
						AUDIO_STORAGE_DIR.getAbsolutePath());
			}
		} else {
			logger.info("Thư mục lưu trữ audio đã tồn tại: {}", AUDIO_STORAGE_DIR.getAbsolutePath());
		}
	}

	private String processAndSaveAudioFile(String newAudioFileAbsolutePath, int cauHoiIdForFileName,
			String oldRelativeAudioPathToDelete) throws IOException {
		// ... (Giữ nguyên logic) ...
		if (newAudioFileAbsolutePath == null || newAudioFileAbsolutePath.trim().isEmpty()) {
			return oldRelativeAudioPathToDelete;
		}
		File sourceFile = new File(newAudioFileAbsolutePath);
		if (!sourceFile.exists() || !sourceFile.isFile()) {
			throw new IOException(
					"File âm thanh nguồn không tồn tại hoặc không phải là file: " + newAudioFileAbsolutePath);
		}
		String originalFileName = sourceFile.getName();
		String fileExtension = "";
		int dotIndex = originalFileName.lastIndexOf('.');
		if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
			fileExtension = originalFileName.substring(dotIndex);
		}
		String newFileNameInStorage;
		if (cauHoiIdForFileName > 0) {
			newFileNameInStorage = "ch_" + cauHoiIdForFileName + "_" + System.currentTimeMillis() + fileExtension;
		} else {
			newFileNameInStorage = "temp_audio_" + UUID.randomUUID().toString().substring(0, 12) + fileExtension;
		}
		Path destinationPath = AUDIO_STORAGE_DIR.toPath().resolve(newFileNameInStorage);
		logger.info("Chuẩn bị copy audio từ: {} đến: {}", sourceFile.getAbsolutePath(), destinationPath);
		Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
		logger.info("Copy file audio mới '{}' thành công.", newFileNameInStorage);
		if (oldRelativeAudioPathToDelete != null && !oldRelativeAudioPathToDelete.trim().isEmpty()) {
			Path oldAudioPathOnDisk = AUDIO_STORAGE_DIR.toPath()
					.resolve(Paths.get(oldRelativeAudioPathToDelete).getFileName());
			if (!oldAudioPathOnDisk.getFileName().toString().equals(newFileNameInStorage)
					&& Files.exists(oldAudioPathOnDisk)) {
				try {
					Files.delete(oldAudioPathOnDisk);
					logger.info("Đã xóa file audio cũ: {}", oldAudioPathOnDisk);
				} catch (IOException e) {
					logger.warn("Không thể xóa file audio cũ '{}': {}", oldAudioPathOnDisk, e.getMessage());
				}
			}
		}
		return AUDIO_DB_RELATIVE_PATH_PREFIX + newFileNameInStorage;
	}

	public CauHoi taoCauHoiVoiDapAn(CauHoi cauHoiCanTao, String newAudioFileAbsolutePath)
			throws SQLException, IOException {
		if (cauHoiCanTao == null) {
			logger.error("Đối tượng CauHoi đầu vào là null, không thể tạo.");
			throw new IllegalArgumentException("Đối tượng CauHoi không được null.");
		}
		Connection conn = null;
		CauHoi cauHoiDaTao = null;
		boolean originalAutoCommit = true;
		String finalAudioPathForDB = cauHoiCanTao.getAudioPath();
		String tempAudioFileNameOnDiskOnly = null;

		logger.info("Bắt đầu tạo câu hỏi. Nội dung: '{}...'. Audio mới: {}",
				cauHoiCanTao.getNoiDung() != null
						? cauHoiCanTao.getNoiDung().substring(0, Math.min(20, cauHoiCanTao.getNoiDung().length()))
						: "N/A",
				newAudioFileAbsolutePath);
		try {
			if (newAudioFileAbsolutePath != null && !newAudioFileAbsolutePath.trim().isEmpty()) {
				finalAudioPathForDB = processAndSaveAudioFile(newAudioFileAbsolutePath, 0, null);
				if (finalAudioPathForDB != null) {
					tempAudioFileNameOnDiskOnly = Paths.get(finalAudioPathForDB).getFileName().toString();
				}
				cauHoiCanTao.setAudioPath(finalAudioPathForDB);
			}

			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			conn = dbInstance.getConnection();
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			logger.debug("Transaction CSDL bắt đầu cho việc tạo câu hỏi.");

			// SỬA Ở ĐÂY: Sử dụng .create() thay vì .save()
			// Giả sử cauHoiDAO.create() trả về đối tượng CauHoi đã có ID
			cauHoiDaTao = cauHoiDAO.create(cauHoiCanTao);
			if (cauHoiDaTao == null || cauHoiDaTao.getId() <= 0) { // Kiểm tra cả null và ID
				throw new SQLException("Không thể tạo bản ghi câu hỏi chính trong CSDL hoặc không lấy được ID.");
			}
			// Không cần gán lại cauHoiDaTao = cauHoiCanTao; nếu create trả về đối tượng đã
			// cập nhật
			logger.info("Câu hỏi cơ bản đã tạo với ID: {}", cauHoiDaTao.getId());

			if (tempAudioFileNameOnDiskOnly != null) {
				Path tempAudioFile = AUDIO_STORAGE_DIR.toPath().resolve(tempAudioFileNameOnDiskOnly);
				String fileExtension = "";
				int dotIndex = tempAudioFileNameOnDiskOnly.lastIndexOf('.');
				if (dotIndex > 0)
					fileExtension = tempAudioFileNameOnDiskOnly.substring(dotIndex);
				String newPermFileName = "ch_" + cauHoiDaTao.getId() + fileExtension;
				Path permAudioFile = AUDIO_STORAGE_DIR.toPath().resolve(newPermFileName);
				if (Files.exists(tempAudioFile)) {
					try {
						Files.move(tempAudioFile, permAudioFile, StandardCopyOption.REPLACE_EXISTING);
						String newRelativePath = AUDIO_DB_RELATIVE_PATH_PREFIX + newPermFileName;
						cauHoiDaTao.setAudioPath(newRelativePath);
						if (!cauHoiDAO.updateAudioPath(cauHoiDaTao.getId(), newRelativePath)) {
							logger.warn("Không thể cập nhật audio_path trong DB cho ID: {}. Giữ lại path tạm.",
									cauHoiDaTao.getId());
						} else {
							logger.info("Đã đổi tên file audio thành '{}' và cập nhật DB.", newPermFileName);
						}
					} catch (IOException e) {
						logger.error("Không thể đổi tên file audio tạm '{}' sang '{}'. Lỗi: {}",
								tempAudioFile.getFileName(), newPermFileName, e.getMessage());
					}
				} else {
					logger.warn("File audio tạm '{}' không tồn tại để đổi tên.", tempAudioFileNameOnDiskOnly);
				}
			}

			List<DapAn> dapAnListInput = cauHoiDaTao.getDapAnList();
			List<DapAn> dapAnDaTaoList = new ArrayList<>();
			if (dapAnListInput != null && !dapAnListInput.isEmpty()) {
				logger.debug("Bắt đầu lưu {} đáp án cho câu hỏi ID: {}", dapAnListInput.size(), cauHoiDaTao.getId());
				for (DapAn da : dapAnListInput) {
					da.setIdCauHoi(cauHoiDaTao.getId());
					// SỬA Ở ĐÂY: Sử dụng .create() thay vì .save()
					// Giả sử dapAnDAO.create() trả về đối tượng DapAn đã có ID
					DapAn dapAnMoi = dapAnDAO.create(da);
					if (dapAnMoi == null || dapAnMoi.getId() <= 0) { // Kiểm tra cả null và ID
						throw new SQLException("Không thể tạo đáp án: '" + da.getNoiDung() + "' cho câu hỏi ID: "
								+ cauHoiDaTao.getId());
					}
					dapAnDaTaoList.add(dapAnMoi); // Thêm đối tượng đã có ID từ DB
				}
			}
			conn.commit();
			logger.info("Transaction CSDL committed cho việc tạo câu hỏi ID: {}", cauHoiDaTao.getId());
			cauHoiDaTao.setListDapAn(dapAnDaTaoList);

		} catch (SQLException | IOException e) {
			// ... (Xử lý lỗi và rollback giữ nguyên) ...
			logger.error("Lỗi trong quá trình tạo câu hỏi và đáp án: {}", e.getMessage(), e);
			if (conn != null) {
				try {
					conn.rollback();
					logger.info("Transaction CSDL đã rollback do lỗi.");
				} catch (SQLException exRollback) {
					logger.error("Lỗi khi rollback transaction: {}", exRollback.getMessage());
				}
			}
			if (tempAudioFileNameOnDiskOnly != null) {
				Path copiedFilePath = AUDIO_STORAGE_DIR.toPath().resolve(tempAudioFileNameOnDiskOnly);
				if (Files.exists(copiedFilePath)) {
					try {
						Files.delete(copiedFilePath);
						logger.info("Đã xóa file audio tạm '{}' do lỗi.", tempAudioFileNameOnDiskOnly);
					} catch (IOException exDelete) {
						logger.error("Không thể xóa file audio tạm '{}' sau lỗi: {}", tempAudioFileNameOnDiskOnly,
								exDelete.getMessage());
					}
				}
			}
			if (e instanceof SQLException)
				throw (SQLException) e;
			if (e instanceof IOException)
				throw (IOException) e;
			throw new SQLException("Lỗi không xác định khi tạo câu hỏi: " + e.getMessage(), e);
		} finally {
			// ... (finally block giữ nguyên) ...
			if (conn != null) {
				try {
					conn.setAutoCommit(originalAutoCommit);
				} catch (SQLException ex) {
					logger.error("Lỗi khi reset autoCommit cho connection: {}", ex.getMessage());
				}
			}
		}
		logger.info("Hoàn tất tạo câu hỏi, trả về: {}",
				(cauHoiDaTao != null ? "Câu hỏi ID " + cauHoiDaTao.getId() : "null"));
		return cauHoiDaTao;
	}

	public boolean capNhatCauHoiVoiDapAn(CauHoi cauHoiCanCapNhat, String newAudioFileAbsolutePath)
			throws SQLException, IOException {
		// ... (Phần đầu giữ nguyên) ...
		if (cauHoiCanCapNhat == null || cauHoiCanCapNhat.getId() <= 0) {
			logger.error("Đối tượng CauHoi không hợp lệ hoặc thiếu ID để cập nhật.");
			throw new IllegalArgumentException("Câu hỏi không hợp lệ để cập nhật.");
		}
		Connection conn = null;
		boolean originalAutoCommit = true;
		boolean success = false;
		logger.info("Bắt đầu cập nhật câu hỏi ID: {}. File audio mới: {}", cauHoiCanCapNhat.getId(),
				newAudioFileAbsolutePath);

		String oldRelativeAudioPath = cauHoiDAO.getAudioPathById(cauHoiCanCapNhat.getId());
		String finalAudioPathForDB = processAndSaveAudioFile(newAudioFileAbsolutePath, cauHoiCanCapNhat.getId(),
				oldRelativeAudioPath);
		cauHoiCanCapNhat.setAudioPath(finalAudioPathForDB);

		try {
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			conn = dbInstance.getConnection();
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			logger.debug("Transaction CSDL bắt đầu cho việc cập nhật câu hỏi ID: {}.", cauHoiCanCapNhat.getId());

			if (!cauHoiDAO.update(cauHoiCanCapNhat)) { // Giả sử .update() là phương thức đúng để cập nhật
				throw new SQLException(
						"Không thể cập nhật thông tin cơ bản của câu hỏi ID: " + cauHoiCanCapNhat.getId());
			}
			logger.info("Câu hỏi cơ bản (ID: {}) đã cập nhật.", cauHoiCanCapNhat.getId());

			dapAnDAO.deleteByCauHoiId(cauHoiCanCapNhat.getId());
			logger.debug("Đã xóa các đáp án cũ của câu hỏi ID: {}", cauHoiCanCapNhat.getId());

			List<DapAn> dapAnListMoi = cauHoiCanCapNhat.getDapAnList();
			if (dapAnListMoi != null && !dapAnListMoi.isEmpty()) {
				logger.debug("Bắt đầu thêm {} đáp án mới cho câu hỏi ID: {}", dapAnListMoi.size(),
						cauHoiCanCapNhat.getId());
				for (DapAn da : dapAnListMoi) {
					da.setIdCauHoi(cauHoiCanCapNhat.getId());
					// SỬA Ở ĐÂY: Sử dụng .create() thay vì .save()
					DapAn dapAnMoi = dapAnDAO.create(da);
					if (dapAnMoi == null || dapAnMoi.getId() <= 0) { // Kiểm tra cả null và ID
						throw new SQLException("Không thể tạo lại đáp án: '" + da.getNoiDung() + "' cho câu hỏi ID: "
								+ cauHoiCanCapNhat.getId());
					}
				}
			}
			conn.commit();
			logger.info("Transaction CSDL committed cho việc cập nhật câu hỏi ID: {}", cauHoiCanCapNhat.getId());
			success = true;
		} catch (SQLException e) {
			// ... (Xử lý lỗi và rollback giữ nguyên) ...
			logger.error("Lỗi SQL khi cập nhật câu hỏi ID {}: {}", cauHoiCanCapNhat.getId(), e.getMessage(), e);
			if (conn != null)
				try {
					conn.rollback();
					logger.info("Transaction CSDL đã rollback do lỗi SQL.");
				} catch (SQLException exRb) {
					logger.error("Lỗi rollback: {}", exRb.getMessage());
				}
			throw e;
		} catch (Exception e_runtime) {
			// ... (Xử lý lỗi và rollback giữ nguyên) ...
			logger.error("Lỗi runtime không mong muốn khi cập nhật câu hỏi ID {}: {}", cauHoiCanCapNhat.getId(),
					e_runtime.getMessage(), e_runtime);
			if (conn != null)
				try {
					conn.rollback();
					logger.info("Transaction CSDL đã rollback do lỗi runtime.");
				} catch (SQLException exRb) {
					logger.error("Lỗi rollback: {}", exRb.getMessage());
				}
			throw new SQLException("Lỗi không xác định khi cập nhật: " + e_runtime.getMessage(), e_runtime);
		} finally {
			// ... (finally block giữ nguyên) ...
			if (conn != null)
				try {
					conn.setAutoCommit(originalAutoCommit);
				} catch (SQLException ex) {
					logger.error("Lỗi reset autocommit: {}", ex.getMessage());
				}
		}
		logger.info("Hoàn tất cập nhật câu hỏi ID: {}, thành công: {}", cauHoiCanCapNhat.getId(), success);
		return success;
	}

	// ... (Các phương thức get và delete còn lại giữ nguyên như phiên bản bạn đã
	// cung cấp) ...
	public List<CauHoi> getAllCauHoi() throws SQLException {
		logger.info("Service: Bắt đầu lấy tất cả câu hỏi (kèm đáp án).");
		List<CauHoi> cauHoiList = cauHoiDAO.findAll();
		if (cauHoiList != null) {
			for (CauHoi ch : cauHoiList) {
				List<DapAn> dapAnCuaCauHoi = dapAnDAO.findByCauHoiId(ch.getId());
				ch.setListDapAn(dapAnCuaCauHoi != null ? dapAnCuaCauHoi : new ArrayList<>());
			}
		} else {
			cauHoiList = new ArrayList<>();
		}
		logger.info("Service: Kết thúc lấy tất cả câu hỏi. Số lượng: {}", cauHoiList.size());
		return cauHoiList;
	}

	public List<CauHoi> getCauHoiByChuDeId(int idChuDe) throws SQLException {
		logger.info("Service: Bắt đầu lấy câu hỏi theo chủ đề ID: {} (kèm đáp án).", idChuDe);
		List<CauHoi> cauHoiList = cauHoiDAO.findByChuDeId(idChuDe);
		if (cauHoiList != null) {
			for (CauHoi ch : cauHoiList) {
				List<DapAn> dapAnCuaCauHoi = dapAnDAO.findByCauHoiId(ch.getId());
				ch.setListDapAn(dapAnCuaCauHoi != null ? dapAnCuaCauHoi : new ArrayList<>());
			}
		} else {
			cauHoiList = new ArrayList<>();
		}
		logger.info("Service: Kết thúc lấy câu hỏi theo chủ đề ID: {}. Số lượng: {}", idChuDe, cauHoiList.size());
		return cauHoiList;
	}

	public CauHoi getCauHoiByIdWithDapAn(int idCauHoi) throws SQLException {
		logger.info("Service: Bắt đầu lấy câu hỏi chi tiết ID: {} (kèm đáp án).", idCauHoi);
		CauHoi cauHoi = cauHoiDAO.findById(idCauHoi);
		if (cauHoi != null) {
			List<DapAn> dapAnList = dapAnDAO.findByCauHoiId(idCauHoi);
			cauHoi.setListDapAn(dapAnList != null ? dapAnList : new ArrayList<>());
			if (cauHoi.getDapAnList() != null) { // Thêm kiểm tra null trước khi gọi size()
				logger.debug("Câu hỏi ID {}: Tìm thấy {} đáp án.", idCauHoi, cauHoi.getDapAnList().size());
			} else {
				logger.debug("Câu hỏi ID {}: dapAnList là null.", idCauHoi);
			}
		} else {
			logger.warn("Không tìm thấy câu hỏi với ID: {}", idCauHoi);
		}
		logger.info("Service: Kết thúc lấy câu hỏi chi tiết ID: {}. Tìm thấy: {}", idCauHoi, (cauHoi != null));
		return cauHoi;
	}

	public boolean xoaCauHoiVoiDapAn(int idCauHoi) throws SQLException {
		logger.info("Service: Bắt đầu xóa câu hỏi ID: {}", idCauHoi);
		Connection conn = null;
		boolean originalAutoCommit = true;
		boolean success = false;
		try {
			DatabaseConnection dbInstance = DatabaseConnection.getInstance();
			conn = dbInstance.getConnection();
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			dapAnDAO.deleteByCauHoiId(idCauHoi);
			logger.debug("Đã thực thi xóa đáp án cho câu hỏi ID: {}", idCauHoi);
			success = cauHoiDAO.delete(idCauHoi);
			if (success) {
				logger.info("Đã xóa câu hỏi ID: {} thành công.", idCauHoi);
				conn.commit();
			} else {
				logger.warn("Không thể xóa câu hỏi ID: {} từ CSDL. Rollback.", idCauHoi);
				conn.rollback();
			}
		} catch (SQLException e) {
			logger.error("Lỗi SQL khi xóa câu hỏi ID {}: {}", idCauHoi, e.getMessage(), e);
			if (conn != null)
				try {
					conn.rollback();
					logger.info("Transaction xóa đã rollback.");
				} catch (SQLException exRb) {
					logger.error("Lỗi rollback: {}", exRb.getMessage());
				}
			throw e;
		} finally {
			if (conn != null)
				try {
					conn.setAutoCommit(originalAutoCommit);
				} catch (SQLException ex) {
					logger.error("Lỗi reset autocommit: {}", ex.getMessage());
				}
		}
		return success;
	}
}