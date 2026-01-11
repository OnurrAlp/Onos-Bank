package com.onosbank.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.onosbank.db.UserDAO;

public class LoginFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtUsername;
	private JPasswordField txtPassword;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				LoginFrame frame = new LoginFrame();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public LoginFrame() {
		setResizable(false);
		setTitle("ONOS BANK - Giriş");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 415, 680);
		
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBounds(0, 0, 400, 650);
		contentPane.add(panel);
		panel.setLayout(null);

		JLabel lblWelcome = new JLabel("ONOS BANK");
		lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
		lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
		lblWelcome.setForeground(new Color(0, 51, 102));
		lblWelcome.setBounds(10, 80, 380, 50);
		panel.add(lblWelcome);

		JLabel lblSub = new JLabel("Dijital Bankacılığa Hoş Geldiniz");
		lblSub.setHorizontalAlignment(SwingConstants.CENTER);
		lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		lblSub.setBounds(10, 125, 380, 20);
		panel.add(lblSub);

		txtUsername = new JTextField();
		txtUsername.setBorder(BorderFactory.createTitledBorder("Kullanıcı Adı"));
		txtUsername.setBounds(50, 200, 300, 50);
		panel.add(txtUsername);

		txtPassword = new JPasswordField();
		txtPassword.setBorder(BorderFactory.createTitledBorder("Şifre"));
		txtPassword.setBounds(50, 270, 300, 50);
		panel.add(txtPassword);

		JButton btnLogin = new JButton("GİRİŞ YAP");
		btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btnLogin.setForeground(Color.WHITE);
		btnLogin.setBackground(new Color(0, 51, 102));
		btnLogin.setBounds(50, 360, 300, 45);
		panel.add(btnLogin);

		JLabel lblRegister = new JLabel("Hesabınız yok mu? Hemen Kayıt Olun");
		lblRegister.setForeground(new Color(0, 102, 204));
		lblRegister.setHorizontalAlignment(SwingConstants.CENTER);
		lblRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblRegister.setBounds(50, 420, 300, 20);
		panel.add(lblRegister);

		// --- Aksiyonlar ---

		btnLogin.addActionListener(e -> {
			String user = txtUsername.getText().trim();
			String pass = new String(txtPassword.getPassword());

			if (user.isEmpty() || pass.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun!");
				return;
			}

			UserDAO dao = new UserDAO();
			if (dao.loginCheck(user, pass)) {
				// Dashboard'a isim göndererek geçiş
				DashboardFrame dashboard = new DashboardFrame(user);
				dashboard.setVisible(true);
				this.dispose();
			} else {
				JOptionPane.showMessageDialog(this, "Hatalı kullanıcı adı veya şifre!", "Giriş Başarısız", JOptionPane.ERROR_MESSAGE);
			}
		});

		lblRegister.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				RegisterFrame rf = new RegisterFrame();
				rf.setVisible(true);
				dispose();
			}
		});

		getRootPane().setDefaultButton(btnLogin);
	}
}