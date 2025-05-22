// src/main/java/com/quanlynganhangdethi/models/ChuDe.java
package com.quanlynganhangdethi.models;

public class ChuDe {
	private int id;
	private String tenChuDe; // Tên cột trong DB là tenchude
	private String moTa; // Tên cột trong DB là mota

	public ChuDe() {
	}

	// Constructor không có id (cho việc tạo mới)
	public ChuDe(String tenChuDe, String moTa) {
		this.tenChuDe = tenChuDe;
		this.moTa = moTa;
	}

	// Constructor có id (khi đọc từ DB)
	public ChuDe(int id, String tenChuDe, String moTa) {
		this.id = id;
		this.tenChuDe = tenChuDe;
		this.moTa = moTa;
	}

	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTenChuDe() {
		return tenChuDe;
	}

	public void setTenChuDe(String tenChuDe) {
		this.tenChuDe = tenChuDe;
	}

	public String getMoTa() {
		return moTa;
	}

	public void setMoTa(String moTa) {
		this.moTa = moTa;
	}

	@Override
	public String toString() {
		return "ChuDe{" + "id=" + id + ", tenChuDe='" + tenChuDe + '\'' + ", moTa='" + moTa + '\'' + '}';
	}
}