// src/main/java/com/quanlynganhangdethi/MainFrame.java
package com.quanlynganhangdethi;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout; // Sẽ dùng GridBagLayout cho sidebar
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.quanlynganhangdethi.ui.CauHoi.CauHoiPanel;
import com.quanlynganhangdethi.ui.ChuDe.ChuDePanel;
import com.quanlynganhangdethi.ui.dethi.DeThiPanel;
import com.quanlynganhangdethi.ui.taodethithu.TaoDeThiThuPanel;

public class MainFrame extends JFrame {
	private JPanel mainPanel; // Panel nội dung chính (CardLayout)
	private CardLayout cardLayout;
	private JPanel sidebar; // Panel sidebar

	// Các panel con
	private ChuDePanel chuDePanel;
	private CauHoiPanel cauHoiPanel;
	private DeThiPanel deThiPanel;
	private TaoDeThiThuPanel taoDeThiThuPanel;

	// Màu sắc và font (tùy chỉnh dựa trên FlatLaf hoặc sở thích)
	private Color sidebarBackgroundColor = UIManager.getColor("Panel.background"); // Lấy màu từ L&F
	private Color sidebarButtonForegroundColor = UIManager.getColor("Button.foreground");
	private Color sidebarButtonHoverBackgroundColor = UIManager.getColor("List.selectionBackground");
	private Color sidebarButtonSelectedBackgroundColor = UIManager.getColor("List.selectionInactiveBackground"); // Hoặc
																													// ...Focus.selectionBackground
	private Color sidebarButtonSelectedForegroundColor = UIManager.getColor("List.selectionForeground"); // Màu chữ khi
																											// nút được
																											// chọn
	private Font sidebarButtonFont = new Font("Segoe UI", Font.PLAIN, 15);
	private Font sidebarTitleFont = new Font("Segoe UI", Font.BOLD, 18);

	private JButton lastSelectedButton = null; // Để theo dõi nút đang được chọn

	public MainFrame() {
		setTitle("Phần mềm Quản lý Ngân hàng Đề thi Tiếng Nhật");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1200, 750); // Kích thước có thể cần điều chỉnh
		setLocationRelativeTo(null);
		setLayout(new BorderLayout(0, 0)); // Bỏ khoảng cách giữa các component BorderLayout

		// --- Panel Nội dung chính (Bên phải) ---
		cardLayout = new CardLayout();
		mainPanel = new JPanel(cardLayout);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding cho nội dung

		// Khởi tạo các panel con
		chuDePanel = new ChuDePanel();
		cauHoiPanel = new CauHoiPanel();
		deThiPanel = new DeThiPanel();
		taoDeThiThuPanel = new TaoDeThiThuPanel();

		// Panel chào mừng cải tiến
		JPanel welcomePanel = new JPanel(new BorderLayout());
		JLabel welcomeLabel = new JLabel("<html><div style='text-align: center; padding: 20px;'>"
				+ "<h1 style='color: #2c3e50; font-size: 28px;'>👋 Chào mừng bạn!</h1>"
				+ "<p style='color: #34495e; font-size: 16px;'>Phần mềm Quản lý Ngân hàng Đề thi Tiếng Nhật</p>"
				+ "<hr style='width:50%; margin-top: 15px; margin-bottom: 15px;'>"
				+ "<p style='color: #7f8c8d; font-size: 14px;'>Chọn một chức năng từ thanh điều hướng bên trái để bắt đầu.</p>"
				+ "</div></html>", SwingConstants.CENTER);
		// welcomeLabel.setIcon(new
		// ImageIcon(getClass().getResource("/icons/welcome_banner.png"))); // Thêm ảnh
		// nếu muốn
		welcomePanel.add(welcomeLabel, BorderLayout.CENTER);

		mainPanel.add(welcomePanel, "WELCOME_PANEL");
		mainPanel.add(chuDePanel, "CHU_DE_PANEL");
		mainPanel.add(cauHoiPanel, "CAU_HOI_PANEL");
		mainPanel.add(deThiPanel, "DE_THI_PANEL");
		mainPanel.add(taoDeThiThuPanel, "TAO_DE_THI_THU_PANEL");

		// --- Tạo Sidebar (Bên trái) ---
		sidebar = createSidebar();

		// Thêm sidebar và mainPanel vào JFrame
		getContentPane().add(sidebar, BorderLayout.WEST);
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		// Hiển thị panel chào mừng ban đầu và chọn nút tương ứng
		cardLayout.show(mainPanel, "WELCOME_PANEL");
		// Tìm nút "Trang chủ" và set nó là selected ban đầu
		for (Component comp : sidebar.getComponents()) {
			if (comp instanceof JButton && "WELCOME_PANEL".equals(((JButton) comp).getName())) {
				setSelectedButton((JButton) comp);
				break;
			}
		}
	}

	private JPanel createSidebar() {
		JPanel sidebarPanel = new JPanel();
		sidebarPanel.setLayout(new GridBagLayout()); // Dùng GridBagLayout để kiểm soát tốt hơn
		sidebarPanel.setPreferredSize(new Dimension(230, 0)); // Độ rộng sidebar
		sidebarPanel.setBackground(sidebarBackgroundColor);
		sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Separator.foreground"))); // Đường
																															// kẻ
																															// phải

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER; // Mỗi component chiếm hết chiều rộng
		gbc.fill = GridBagConstraints.HORIZONTAL; // Kéo dài theo chiều ngang
		gbc.weightx = 1.0; // Cho phép co giãn theo chiều ngang
		gbc.insets = new Insets(8, 10, 8, 10); // Padding cho mỗi nút (top, left, bottom, right)

		// Tiêu đề Sidebar (tùy chọn)
		JLabel titleLabel = new JLabel("  MENU CHÍNH"); // Thêm khoảng trắng cho đẹp
		titleLabel.setFont(sidebarTitleFont);
		titleLabel.setForeground(UIManager.getColor("Label.foreground"));
		titleLabel.setBorder(new EmptyBorder(15, 5, 15, 5)); // Padding cho tiêu đề
		titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
		gbc.anchor = GridBagConstraints.NORTHWEST; // Đẩy lên trên và sang trái
		sidebarPanel.add(titleLabel, gbc);

		gbc.insets = new Insets(4, 10, 4, 10); // Padding nhỏ hơn cho các nút

		// Tạo và thêm các nút vào sidebar
		sidebarPanel.add(createSidebarButton("Trang chủ", "/icons/home.png", "WELCOME_PANEL"), gbc);
		sidebarPanel.add(createSidebarButton("Quản lý Chủ đề", "/icons/chude.png", "CHU_DE_PANEL"), gbc);
		sidebarPanel.add(createSidebarButton("Quản lý Câu hỏi", "/icons/questions.png", "CAU_HOI_PANEL"), gbc);
		sidebarPanel.add(createSidebarButton("Quản lý Đề thi", "/icons/dethi.png", "DE_THI_PANEL"), gbc);
		sidebarPanel.add(createSidebarButton("Tạo Đề thi thử", "/icons/export.png", "TAO_DE_THI_THU_PANEL"), gbc);

		// Spacer để đẩy nút Thoát xuống dưới
		gbc.weighty = 1.0; // Cho phép co giãn theo chiều dọc, đẩy các component khác lên
		JPanel spacer = new JPanel();
		spacer.setOpaque(false);
		sidebarPanel.add(spacer, gbc);
		gbc.weighty = 0.0; // Reset lại

		sidebarPanel.add(createSidebarButton("Thoát Ứng Dụng", "/icons/exit.png", "EXIT_ACTION"), gbc);

		return sidebarPanel;
	}

	private JButton createSidebarButton(String text, String iconPath, final String actionCommandOrPanelName) {
		JButton button = new JButton(text);
		button.setName(actionCommandOrPanelName); // Gán tên để dễ tìm lại cho việc set selected ban đầu
		button.setFont(sidebarButtonFont);
		button.setForeground(sidebarButtonForegroundColor);
		button.setFocusPainted(false);
		button.setBorderPainted(false); // FlatLaf thường xử lý border tốt
		button.setContentAreaFilled(false); // Quan trọng để vẽ nền tùy chỉnh
		button.setOpaque(false);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setIconTextGap(12); // Khoảng cách giữa icon và text
		button.setPreferredSize(new Dimension(0, 45)); // Chiều cao cố định, chiều rộng sẽ theo GridBagLayout

		// Set icon (bạn cần có các file icon, ví dụ trong src/main/resources/icons)
		try {
			ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
			Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH); // Kích thước icon
			button.setIcon(new ImageIcon(img));
		} catch (Exception e) {
			System.err.println("Lỗi tải icon: " + iconPath + " cho nút " + text);
			// Có thể đặt một icon mặc định hoặc khoảng trống
			button.setIcon(new ImageIcon(new byte[0])); // Icon rỗng
		}

		// Thêm padding bên trong nút thông qua border
		button.setBorder(new EmptyBorder(0, 15, 0, 10)); // top, left, bottom, right

		// Hiệu ứng Hover và Click
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (!button.isSelected()) {
					button.setOpaque(true); // Để vẽ nền hover
					button.setBackground(sidebarButtonHoverBackgroundColor);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (!button.isSelected()) {
					button.setOpaque(false); // Tắt opaque để trở về nền của sidebar
					button.setBackground(sidebarBackgroundColor); // Hoặc null
				}
			}
		});

		button.addActionListener(e -> {
			if ("EXIT_ACTION".equals(actionCommandOrPanelName)) {
				System.exit(0);
			} else {
				cardLayout.show(mainPanel, actionCommandOrPanelName);
				setSelectedButton(button);
			}
		});
		return button;
	}

	private void setSelectedButton(JButton selectedBtn) {
		if (lastSelectedButton != null) {
			lastSelectedButton.setSelected(false);
			lastSelectedButton.setOpaque(false);
			lastSelectedButton.setBackground(sidebarBackgroundColor); // Hoặc null
			lastSelectedButton.setForeground(sidebarButtonForegroundColor);
			// lastSelectedButton.setFont(sidebarButtonFont); // Reset font nếu có thay đổi
		}
		selectedBtn.setSelected(true);
		selectedBtn.setOpaque(true); // Phải true để màu nền được vẽ
		selectedBtn.setBackground(sidebarButtonSelectedBackgroundColor);
		selectedBtn.setForeground(sidebarButtonSelectedForegroundColor); // Thay đổi màu chữ khi chọn
		// selectedBtn.setFont(sidebarButtonFont.deriveFont(Font.BOLD)); // Làm đậm chữ
		// khi chọn

		lastSelectedButton = selectedBtn;
		sidebar.revalidate();
		sidebar.repaint();
	}

	// Cần tạo các file icon:
	// src/main/resources/icons/home.png
	// src/main/resources/icons/chude.png
	// src/main/resources/icons/questions.png
	// src/main/resources/icons/dethi.png
	// src/main/resources/icons/export.png
	// src/main/resources/icons/exit.png
	// (Kích thước gợi ý: 24x24 hoặc 32x32 pixels)
}