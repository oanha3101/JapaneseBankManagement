// src/main/java/com/quanlynganhangdethi/models/DeThi.java
package com.quanlynganhangdethi.models;

import java.sql.Timestamp; // Hoặc java.util.Date và chuyển đổi khi cần
import java.util.List;

public class DeThi {
	private int id;
	private String tieuDe; // tieude
	private Timestamp ngayTao; // ngaytao (DATETIME trong SQL Server map tốt với Timestamp)
	private String nguoiTao; // nguoitao
	private List<CauHoi> cauHoiList; // Để chứa danh sách câu hỏi trong đề thi

	public DeThi() {
	}

	public DeThi(int id, String tieuDe, Timestamp ngayTao, String nguoiTao) {
		this.id = id;
		this.tieuDe = tieuDe;
		this.ngayTao = ngayTao;
		this.nguoiTao = nguoiTao;
	}

	// Constructor cho tạo mới
	public DeThi(String tieuDe, String nguoiTao) {
		this.tieuDe = tieuDe;
		this.nguoiTao = nguoiTao;
		// ngayTao sẽ được set bởi DB hoặc khi lấy lại đối tượng
	}

	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTieuDe() {
		return tieuDe;
	}

	public void setTieuDe(String tieuDe) {
		this.tieuDe = tieuDe;
	}

	public Timestamp getNgayTao() {
		return ngayTao;
	}

	public void setNgayTao(Timestamp ngayTao) {
		this.ngayTao = ngayTao;
	}

	public String getNguoiTao() {
		return nguoiTao;
	}

	public void setNguoiTao(String nguoiTao) {
		this.nguoiTao = nguoiTao;
	}

	public List<CauHoi> getCauHoiList() {
		return cauHoiList;
	}

	public void setCauHoiList(List<CauHoi> cauHoiList) {
		this.cauHoiList = cauHoiList;
	}

	@Override
	public String toString() {
		return "DeThi{" + "id=" + id + ", tieuDe='" + tieuDe + '\'' + ", ngayTao=" + ngayTao + ", nguoiTao='" + nguoiTao
				+ '\'' + '}';
	}
}