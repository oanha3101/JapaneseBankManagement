# Phần Mềm Quản Lý Ngân Hàng Đề Thi Tiếng Nhật

Ứng dụng desktop hỗ trợ tạo, quản lý và tổ chức ngân hàng câu hỏi, chủ đề và các bộ đề thi tiếng Nhật. Được phát triển bằng Java Swing, Maven và sử dụng cơ sở dữ liệu Microsoft SQL Server. Giao diện được cải tiến bằng Look and Feel FlatLaf.

## ✨ Tính Năng Chính

*   **Quản lý Chủ đề:**
    *   Thêm, sửa, xóa các chủ đề học liệu (ví dụ: Ngữ pháp N3, Từ vựng N2, Kanji Bài 10).
*   **Quản lý Câu hỏi:**
    *   Tạo câu hỏi với nhiều định dạng: trắc nghiệm, tự luận, điền khuyết, câu hỏi nghe.
    *   Quản lý đáp án cho từng câu hỏi, đánh dấu đáp án chính xác.
    *   Hỗ trợ tải lên, lưu trữ và phát file âm thanh (audio) cho các câu hỏi dạng nghe.
    *   Phân loại câu hỏi theo chủ đề và có thể theo độ khó (nếu có).
*   **Quản lý Đề thi (Ngân hàng đề):**
    *   Tạo các bộ đề thi bằng cách lựa chọn thủ công các câu hỏi từ ngân hàng câu hỏi đã có.
    *   Chỉnh sửa thông tin chung của đề thi như tiêu đề, chủ đề liên quan, thời gian làm bài.
    *   Quản lý danh sách các câu hỏi cụ thể có trong một đề thi.
*   **Tạo Đề thi Thử Ngẫu Nhiên:**
    *   Cho phép người dùng định nghĩa các tiêu chí (ví dụ: chọn chủ đề, loại câu hỏi, số lượng câu cho mỗi tiêu chí).
    *   Hệ thống sẽ tự động tạo ra một hoặc nhiều bộ đề thi ngẫu nhiên dựa trên các tiêu chí đó.
*   **Xuất Đề Thi và Đáp Án:**
    *   Xuất đề thi và/hoặc đáp án ra các định dạng file phổ biến như PDF và TXT.
    *   Cung cấp các tùy chọn xuất đa dạng: chỉ đề thi, đề thi kèm đáp án tóm tắt, hoặc đáp án chi tiết.
*   **(Tùy chọn) Gợi ý từ AI:** Tích hợp với Gemini AI (hoặc mô hình AI khác) để hỗ trợ gợi ý hoặc tạo câu hỏi dựa trên chủ đề (nếu chức năng này đã được triển khai).

## 🛠️ Công Nghệ Sử Dụng

*   **Ngôn ngữ lập trình:** Java (Khuyến nghị JDK 11 hoặc mới hơn)
*   **Giao diện người dùng (GUI):** Java Swing
*   **Look and Feel:** [FlatLaf](https://www.formdev.com/flatlaf/) (Thư viện giúp hiện đại hóa giao diện Swing)
*   **Quản lý Project & Dependencies:** Apache Maven
*   **Cơ sở dữ liệu:** Microsoft SQL Server (Khuyến nghị phiên bản 2017 trở lên)
*   **Thư viện xuất PDF:** Apache PDFBox
*   **Logging:** SLF4J (ví dụ với Logback hoặc Log4j2)

## 🚀 Yêu Cầu Hệ Thống

*   **Java Runtime Environment (JRE):** Phiên bản 11 trở lên. Tải tại [Adoptium (Temurin JRE)](https://adoptium.net/temurin/releases/).
*   **Microsoft SQL Server:** Một instance SQL Server đang hoạt động.
*   **(Tùy chọn) Kết nối Internet:** Nếu sử dụng chức năng gợi ý AI.

## ⚙️ Hướng Dẫn Cài Đặt và Chạy

**1. Chuẩn Bị Môi Trường:**
    *   Đảm bảo bạn đã cài đặt **JRE (phiên bản 11 trở lên)**.
    *   Đảm bảo **Microsoft SQL Server** đã được cài đặt và đang chạy.

**2. Thiết Lập Cơ Sở Dữ Liệu:**
    *   Mở SQL Server Management Studio (SSMS) hoặc công cụ tương tự.
    *   Kết nối đến instance SQL Server của bạn.
    *   Tạo một database mới với tên `NhatNguBankDB` (hoặc tên bạn muốn, nhưng cần cập nhật trong file cấu hình):
        ```sql
        CREATE DATABASE NhatNguBankDB;
        GO
        ```
    *   Chọn database `NhatNguBankDB` vừa tạo.
    *   Chạy file script SQL (ví dụ: `docs/database_schema.sql` - *bạn cần cung cấp file này*) để tạo tất cả các bảng cần thiết.

**3. Cấu Hình Chương Trình:**
    *   Trong thư mục gốc của dự án, tìm đến `src/main/resources/`.
    *   **Copy file `config.properties.example` và đổi tên thành `config.properties`**.
    *   Mở file `config.properties` bằng một trình soạn thảo văn bản.
    *   Chỉnh sửa các thông tin sau cho phù hợp với cài đặt SQL Server của bạn:
        ```properties
        # Database Configuration
        db.url=jdbc:sqlserver://YOUR_SERVER_ADDRESS:1433;databaseName=NhatNguBankDB;encrypt=true;trustServerCertificate=true;
        db.username=YOUR_SQL_SERVER_USERNAME
        db.password=YOUR_SQL_SERVER_PASSWORD

        # (Tùy chọn) Gemini API Key for AI features
        # gemini.api.key=YOUR_GEMINI_API_KEY_HERE 
        ```
        *   Thay `YOUR_SERVER_ADDRESS` (ví dụ: `localhost`, `TENMAYCHU\SQLEXPRESS`).
        *   Thay `YOUR_SQL_SERVER_USERNAME` và `YOUR_SQL_SERVER_PASSWORD`.
        *   Nếu sử dụng tính năng AI, điền API Key của bạn.
    *   Lưu file `config.properties`.

**4. Build và Chạy Dự Án (Sử dụng Maven):**
    *   Mở Command Prompt (Terminal) trong thư mục gốc của dự án (nơi có file `pom.xml`).
    *   **Build dự án:**
        ```bash
        mvn clean package
        ```
        Thao tác này sẽ biên dịch mã nguồn và tạo một file JAR thực thi (ví dụ: `BankManage-1.0-SNAPSHOT.jar`) trong thư mục `target/`.
    *   **Chạy file JAR:**
        ```bash
        java -jar target/TenFileJarCuaBan-version.jar
        ```
        (Thay `TenFileJarCuaBan-version.jar` bằng tên file JAR thực tế được tạo ra).

**5. (Cách khác) Chạy từ IDE (Eclipse, IntelliJ IDEA):**
    *   Import dự án vào IDE của bạn dưới dạng "Existing Maven Project".
    *   Đảm bảo bạn đã thực hiện Bước 2 (Thiết lập CSDL) và Bước 3 (Cấu hình `config.properties`).
    *   Tìm và chạy file `MainApp.java` (thường nằm trong package `com.quanlynganhangdethi`).

## 📖 Hướng Dẫn Sử Dụng Cơ Bản

Sau khi chương trình khởi động:

*   **Thanh Điều Hướng (Menu Bên Trái):** Sử dụng để chuyển đổi giữa các màn hình chức năng chính:
    *   **Quản lý Chủ Đề:** Thêm, sửa, xóa các chủ đề.
    *   **Quản lý Câu Hỏi:** Tạo và quản lý chi tiết các câu hỏi, đáp án, file audio.
    *   **Quản lý Đề Thi:** Xây dựng các bộ đề thi từ ngân hàng câu hỏi.
    *   **Tạo Đề Thi Thử:** Tự động sinh đề thi ngẫu nhiên.
*   **Khu Vực Nội Dung:** Hiển thị danh sách và các form nhập liệu tương ứng với chức năng được chọn.
*   **Các Nút Lệnh:** Các nút "Thêm Mới", "Sửa", "Xóa", "Làm Mới", "Export" sẽ xuất hiện tùy theo ngữ cảnh để bạn thực hiện các thao tác.

## 📁 Cấu Trúc Thư Mục Dự Án (Tham khảo)
