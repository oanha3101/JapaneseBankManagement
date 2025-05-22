// src/main/java/com/quanlynganhangdethi/models/DeThiCauHoi.java
package com.quanlynganhangdethi.models;

public class DeThiCauHoi {
	private int idDeThi;
	private int idCauHoi;
	private int thuTu;

	public DeThiCauHoi() {
	}

	public DeThiCauHoi(int idDeThi, int idCauHoi, int thuTu) {
		this.idDeThi = idDeThi;
		this.idCauHoi = idCauHoi;
		this.thuTu = thuTu;
	}

	// Getters and Setters
	public int getIdDeThi() {
		return idDeThi;
	}

	public void setIdDeThi(int idDeThi) {
		this.idDeThi = idDeThi;
	}

	public int getIdCauHoi() {
		return idCauHoi;
	}

	public void setIdCauHoi(int idCauHoi) {
		this.idCauHoi = idCauHoi;
	}

	public int getThuTu() {
		return thuTu;
	}

	public void setThuTu(int thuTu) {
		this.thuTu = thuTu;
	}

	@Override
	public String toString() {
		return "DeThiCauHoi{" + "idDeThi=" + idDeThi + ", idCauHoi=" + idCauHoi + ", thuTu=" + thuTu + '}';
	}
}