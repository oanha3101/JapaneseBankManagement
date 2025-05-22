// src/main/java/com/quanlynganhangdethi/config/DatabaseConnection.java
package com.quanlynganhangdethi.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; // QUAN TRỌNG: Đảm bảo có import này
import java.sql.ResultSet; // QUAN TRỌNG: Đảm bảo có import này
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
	private static volatile DatabaseConnection instance;
	private Connection connection;

	private static String dbUrl;
	private static String dbUsername;
	private static String dbPassword;
	private static boolean driverLoaded = false;

	static {
		Properties prop = new Properties();
		try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
			if (input == null) {
				System.err.println("Lỗi: Không tìm thấy file config.properties trong thư mục resources!");
				// Cân nhắc throw RuntimeException nếu file config là bắt buộc
			} else {
				prop.load(input);
				dbUrl = prop.getProperty("db.url");
				dbUsername = prop.getProperty("db.username");
				dbPassword = prop.getProperty("db.password");
			}

			if (dbUrl == null || dbUrl.trim().isEmpty() || dbUsername == null || dbUsername.trim().isEmpty()
					|| dbPassword == null) {
				System.err.println(
						"Lỗi: Thiếu thông tin cấu hình DB (db.url, db.username, db.password) trong config.properties.");
			} else {
				System.out.println("Đã load cấu hình DB từ config.properties.");
			}

		} catch (IOException ex) {
			System.err.println("Lỗi IO khi đọc config.properties: " + ex.getMessage());
		}

		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			driverLoaded = true;
			System.out.println("Đã load SQLServer JDBC Driver.");
		} catch (ClassNotFoundException ex) {
			System.err.println("Lỗi: Không tìm thấy SQLServer JDBC Driver. Đảm bảo đã thêm dependency mssql-jdbc.");
		}
	}

	private DatabaseConnection() throws SQLException {
		if (!driverLoaded) {
			throw new SQLException("SQLServer JDBC Driver chưa được load.");
		}
		if (dbUrl == null || dbUsername == null || dbPassword == null) {
			throw new SQLException("Thông tin cấu hình database không hợp lệ.");
		}
		this.connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
		System.out.println("Kết nối database thành công.");
	}

	public static DatabaseConnection getInstance() throws SQLException {
		if (instance == null) {
			synchronized (DatabaseConnection.class) {
				if (instance == null) {
					instance = new DatabaseConnection();
				}
			}
		} else {
			try {
				if (instance.connection == null || instance.connection.isClosed() || !instance.connection.isValid(1)) {
					System.out.println("Kết nối không hợp lệ hoặc đã đóng. Tạo lại kết nối.");
					if (instance.connection != null && !instance.connection.isClosed()) {
						try {
							instance.connection.close();
						} catch (SQLException e) {
							/* Bỏ qua */ }
					}
					instance = new DatabaseConnection();
				}
			} catch (SQLException e) {
				System.err.println("Lỗi khi kiểm tra/tạo lại kết nối: " + e.getMessage());
				try {
					instance = new DatabaseConnection();
				} catch (SQLException ex) {
					System.err.println("Không thể tạo lại DatabaseConnection: " + ex.getMessage());
					throw ex;
				}
			}
		}
		return instance;
	}

	public Connection getConnection() { // Đây là phương thức INSTANCE
		try {
			if (this.connection == null || this.connection.isClosed()) {
				System.err.println("Connection của instance đã bị đóng hoặc null. Gọi getInstance() để sửa.");
				throw new SQLException("Connection của instance không hợp lệ.");
			}
		} catch (SQLException e) {
			// Nên ném lại lỗi hoặc xử lý nghiêm túc hơn
			System.err.println("Lỗi khi kiểm tra connection trong getConnection(): " + e.getMessage());
			return null; // Hoặc throw new SQLException("Lỗi kiểm tra connection", e);
		}
		return this.connection;
	}

	// === CÁC PHƯƠNG THỨC STATIC ĐỂ ĐÓNG RESOURCES ===
	public static void closeResources(PreparedStatement ps, ResultSet rs) {
		try {
			if (rs != null && !rs.isClosed()) {
				rs.close();
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi đóng ResultSet: " + e.getMessage());
		}
		try {
			if (ps != null && !ps.isClosed()) {
				ps.close();
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi đóng PreparedStatement: " + e.getMessage());
		}
	}

	public static void closeResources(PreparedStatement ps) {
		closeResources(ps, null);
	}
	// === KẾT THÚC CÁC PHƯƠG THỨC STATIC ĐỂ ĐÓNG RESOURCES ===

	public void shutdown() {
		try {
			if (this.connection != null && !this.connection.isClosed()) {
				this.connection.close();
				System.out.println("Đã đóng kết nối database của Singleton.");
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi đóng kết nối của Singleton: " + e.getMessage());
		}
		instance = null;
	}

	public static void main(String[] args) {
		Connection conn1 = null;
		DatabaseConnection dbInstance1 = null;
		try {
			dbInstance1 = DatabaseConnection.getInstance();
			conn1 = dbInstance1.getConnection();
			if (conn1 != null && !conn1.isClosed()) {
				System.out.println("Instance 1: Kết nối thành công! " + conn1);
				try (PreparedStatement ps = conn1.prepareStatement("SELECT 1 AS TestValue");
						ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						System.out.println("Kết quả truy vấn test: " + rs.getInt("TestValue"));
					}
				}
			} else {
				System.out.println("Instance 1: Không thể lấy kết nối.");
			}
		} catch (SQLException e) {
			System.err.println("Lỗi trong main: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (dbInstance1 != null) {
				dbInstance1.shutdown();
			}
		}
	}
}