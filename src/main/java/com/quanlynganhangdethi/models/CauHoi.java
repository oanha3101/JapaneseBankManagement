// src/main/java/com/quanlynganhangdethi/models/CauHoi.java
package com.quanlynganhangdethi.models;

import java.util.List;

public class CauHoi {
	private int id;
	private String noiDung;
	private String loaiCauHoi;
	private Integer doKho;
	private int idChuDe;
	private String audioPath;
	private List<DapAn> dapAnList; // Đổi tên từ listDapAn để nhất quán

	public CauHoi() {
	}

	public CauHoi(String noiDung, String loaiCauHoi, Integer doKho, int idChuDe, String audioPath) {
		this.noiDung = noiDung;
		this.loaiCauHoi = loaiCauHoi;
		this.doKho = doKho;
		this.idChuDe = idChuDe;
		this.audioPath = audioPath;
	}

	public CauHoi(int id, String noiDung, String loaiCauHoi, Integer doKho, int idChuDe, String audioPath) {
		this.id = id;
		this.noiDung = noiDung;
		this.loaiCauHoi = loaiCauHoi;
		this.doKho = doKho;
		this.idChuDe = idChuDe;
		this.audioPath = audioPath;
	}

	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNoiDung() {
		return noiDung;
	}

	public void setNoiDung(String noiDung) {
		this.noiDung = noiDung;
	}

	public String getLoaiCauHoi() {
		return loaiCauHoi;
	}

	public void setLoaiCauHoi(String loaiCauHoi) {
		this.loaiCauHoi = loaiCauHoi;
	}

	public Integer getDoKho() {
		return doKho;
	}

	public void setDoKho(Integer doKho) {
		this.doKho = doKho;
	}

	public int getIdChuDe() {
		return idChuDe;
	}

	public void setIdChuDe(int idChuDe) {
		this.idChuDe = idChuDe;
	}

	public String getAudioPath() {
		return audioPath;
	}

	public void setAudioPath(String audioPath) {
		this.audioPath = audioPath;
	}

	// Getter và Setter cho dapAnList
	public List<DapAn> getDapAnList() { // Đây là getter bạn cần
		return dapAnList;
	}

	public void setDapAnList(List<DapAn> dapAnList) { // Đây là setter bạn cần
		this.dapAnList = dapAnList;
	}

	@Override
	public String toString() {
		return "CauHoi{" + "id=" + id + ", noiDung='" + noiDung + '\'' + ", loaiCauHoi='" + loaiCauHoi + '\''
				+ ", doKho=" + doKho + ", idChuDe=" + idChuDe + ", audioPath='" + audioPath + '\'' + '}';
	}

	public void setListDapAn(List<DapAn> dapAnList2) {
		// TODO Auto-generated method stub

	}

	public List<DapAn> getListDapAn() {
		// TODO Auto-generated method stub
		return null;
	}
}