package com.onosbank.view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import com.onosbank.db.UserDAO;

public class OtherAccountTransferFrame extends JFrame {
    private String username;
    private int senderID;
    private double senderBalance;
    private String realFirstName, realLastName;
    private int receiverID;

    public OtherAccountTransferFrame(String username, int senderID, double senderBalance) {
        this.username = username;
        this.senderID = senderID;
        this.senderBalance = senderBalance;

        setTitle("Başka Hesaba Transfer");
        setResizable(false);
        setBounds(150, 150, 415, 680);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        // --- IBAN GİRİŞİ ---
        JTextField txtIban = new JTextField();
        txtIban.setBorder(BorderFactory.createTitledBorder("Alıcı IBAN"));
        txtIban.setBounds(50, 80, 315, 55);
        add(txtIban);

        // İpucu Label
        JLabel lblHint = new JLabel("İpucu: **** ****");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(new Color(100, 100, 100));
        lblHint.setBounds(55, 140, 300, 20);
        add(lblHint);

        // --- İSİM DOĞRULAMA (Başlangıçta kapalı) ---
        JTextField txtFullName = new JTextField();
        txtFullName.setBorder(BorderFactory.createTitledBorder("Alıcı Adı Soyadı (Doğrulama)"));
        txtFullName.setBounds(50, 185, 315, 55);
        txtFullName.setEnabled(false);
        txtFullName.setDisabledTextColor(Color.LIGHT_GRAY);
        add(txtFullName);

        // --- MİKTAR GİRİŞİ ---
        JTextField txtAmount = new JTextField();
        txtAmount.setBorder(BorderFactory.createTitledBorder("Gönderilecek Miktar"));
        txtAmount.setBounds(50, 270, 315, 55);
        
        // SADECE SAYI GİRİŞİ KONTROLÜ
        txtAmount.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                // Sadece rakam, nokta ve backspace tuşuna izin ver
                if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == '.'))) {
                    e.consume(); // Geçersiz karakteri reddet
                }
                // Birden fazla nokta girilmesini engelle
                if (c == '.' && txtAmount.getText().contains(".")) {
                    e.consume();
                }
            }
        });
        add(txtAmount);

        // --- DEVAM ET BUTONU ---
        JButton btnSend = new JButton("DEVAM ET");
        btnSend.setBounds(50, 390, 315, 50);
        btnSend.setBackground(new Color(0, 51, 102));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSend.setFocusPainted(false);
        add(btnSend);

        // --- GERİ DÖN BUTONU ---
        JButton btnBack = new JButton("İŞLEMİ İPTAL ET VE DÖN");
        btnBack.setBounds(50, 540, 315, 45);
        btnBack.setBackground(new Color(235, 235, 235));
        btnBack.setForeground(new Color(0, 51, 102));
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBack.setFocusPainted(false);
        btnBack.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(btnBack);

        // IBAN DİNLEYİCİ
        txtIban.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { checkIban(); }
            public void removeUpdate(DocumentEvent e) { checkIban(); }
            public void changedUpdate(DocumentEvent e) { checkIban(); }

            private void checkIban() {
                String iban = txtIban.getText().trim();
                if (iban.length() == 26) {
                    UserDAO dao = new UserDAO();
                    Object[] data = dao.getReceiverByIban(iban);
                    
                    if (data != null) {
                        realFirstName = (String) data[0];
                        realLastName = (String) data[1];
                        receiverID = (int) data[2];
                        
                        String masked = maskName(realFirstName) + " " + maskName(realLastName);
                        lblHint.setText("İpucu: " + masked);
                        lblHint.setForeground(new Color(0, 102, 51));
                        
                        txtFullName.setEnabled(true);
                        txtFullName.requestFocus();
                    } else {
                        lblHint.setText("İpucu: Hesap Bulunamadı!");
                        lblHint.setForeground(Color.RED);
                        txtFullName.setEnabled(false);
                    }
                } else {
                    lblHint.setText("İpucu: **** ****");
                    lblHint.setForeground(Color.GRAY);
                    txtFullName.setEnabled(false);
                }
            }
        });

        btnBack.addActionListener(e -> {
            new TransferFrame(username).setVisible(true);
            this.dispose();
        });

        // Transfer Aksiyonu
        btnSend.addActionListener(e -> {
            String inputName = txtFullName.getText().trim().toUpperCase();
            String correctName = (realFirstName + " " + realLastName).toUpperCase();
            
            if (txtIban.getText().length() < 26) {
                JOptionPane.showMessageDialog(this, "Lütfen geçerli bir IBAN girin.");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(txtAmount.getText());
                if (amount <= 0) throw new Exception();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lütfen geçerli bir miktar girin!");
                return;
            }

            if (!inputName.equals(correctName)) {
                JOptionPane.showMessageDialog(this, "Alıcı ismi uyuşmuyor!\nLütfen Ad Soyad bilgisini tam girin.", "Doğrulama Hatası", JOptionPane.ERROR_MESSAGE);
            } else if (amount > senderBalance) {
                JOptionPane.showMessageDialog(this, "Yetersiz bakiye! Mevcut Bakiyeniz: ₺" + senderBalance);
            } else {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    realFirstName + " " + realLastName + " kişisine\n" + String.format("%.2f", amount) + " ₺ gönderilecek.\nOnaylıyor musunuz?", 
                    "Transfer Onayı", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (new UserDAO().processTransfer(senderID, receiverID, amount)) {
                        JOptionPane.showMessageDialog(this, "Para başarıyla gönderildi!");
                        
                        // DASHBOARD YENİLEME TETİKLEYİCİSİ
                        if (DashboardFrame.instance != null) {
                            DashboardFrame.instance.loadData();
                        }
                        
                        this.dispose();
                    }
                }
            }
        });
    }

    private String maskName(String s) {
        if (s == null || s.isEmpty()) return "**";
        if (s.length() <= 2) return s + "**";
        return s.substring(0, 2) + "*".repeat(s.length() - 2);
    }
}