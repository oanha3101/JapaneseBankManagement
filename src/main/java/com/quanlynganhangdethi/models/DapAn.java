// src/main/java/com/quanlynganhangdethi/models/DapAn.java
package com.quanlynganhangdethi.models;

public class DapAn {
	private int id;
	private int idCauHoi; // id_cauhoi
	private String noiDung; // noidung
	private boolean laDapAnDung; // ladapandung

	public DapAn() {
	}

	public DapAn(int id, int idCauHoi, String noiDung, boolean laDapAnDung) {
		this.id = id;
		this.idCauHoi = idCauHoi;
		this.noiDung = noiDung;
		this.laDapAnDung = laDapAnDung;
	}

	// Constructor cho tạo mới
	public DapAn(int idCauHoi, String noiDung, boolean laDapAnDung) {
		this.idCauHoi = idCauHoi;
		this.noiDung = noiDung;
		this.laDapAnDung = laDapAnDung;
	}

	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIdCauHoi() {
		return idCauHoi;
	}

	public void setIdCauHoi(int idCauHoi) {
		this.idCauHoi = idCauHoi;
	}

	public String getNoiDung() {
		return noiDung;
	}

	public void setNoiDung(String noiDung) {
		this.noiDung = noiDung;
	}

	public boolean isLaDapAnDung() { // Getter cho boolean thường là is<PropertyName>
		return laDapAnDung;
	}

	public void setLaDapAnDung(boolean laDapAnDung) {
		this.laDapAnDung = laDapAnDung;
	}

	@Override
	public String toString() {
		return "DapAn{" + "id=" + id + ", idCauHoi=" + idCauHoi + ", noiDung='" + noiDung + '\'' + ", laDapAnDung="
				+ laDapAnDung + '}';
	}
}