// src/main/java/com/quanlynganhangdethi/dao/ICRUDDAO.java
package com.quanlynganhangdethi.dao;

import java.sql.SQLException; // <<<< THÊM IMPORT NÀY
import java.util.List;

public interface ICRUDDAO<T, K> {
	T create(T entity) throws SQLException; // <<<< THÊM throws SQLException

	T findById(K id) throws SQLException; // <<<< THÊM throws SQLException

	List<T> findAll() throws SQLException; // <<<< THÊM throws SQLException

	boolean update(T entity) throws SQLException; // <<<< THÊM throws SQLException

	boolean delete(K id) throws SQLException; // <<<< THÊM throws SQLException
}