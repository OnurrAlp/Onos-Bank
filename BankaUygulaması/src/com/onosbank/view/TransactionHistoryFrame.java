package com.onosbank.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import com.onosbank.db.UserDAO;

public class TransactionHistoryFrame extends JFrame {
    private String username, iban;
    private int accountID;
    private JPanel listContainer;
    private final Color primaryColor = new Color(0, 51, 102);

    public TransactionHistoryFrame(String username, int accountID, String iban) {
        this.username = username;
        this.accountID = accountID;
        this.iban = iban;
        
        setTitle("Hesap Hareketleri");
        setResizable(false);
        setBounds(150, 150, 415, 680);
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(null);

        JLabel lblTitle = new JLabel("Hesap Hareketleri");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBounds(25, 25, 250, 30);
        add(lblTitle);

        // IBAN bilgisini boşluklu formatta gösteriyoruz
        JLabel lblIban = new JLabel(formatIban(iban));
        lblIban.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblIban.setForeground(Color.GRAY);
        lblIban.setBounds(25, 55, 300, 20);
        add(lblIban);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(new Color(245, 247, 250));

        JScrollPane scroll = new JScrollPane(listContainer);
        // Genişlik ve hizalama AllAccountsFrame ile senkronize edildi
        scroll.setBounds(20, 90, 375, 430);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(245, 247, 250));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll);

        JButton btnBack = new JButton("KAPAT");
        btnBack.setBounds(25, 550, 350, 45);
        btnBack.setBackground(new Color(230, 230, 230));
        btnBack.setForeground(primaryColor);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBack.setFocusPainted(false);
        btnBack.setBorder(null);
        btnBack.addActionListener(e -> this.dispose());
        add(btnBack);

        loadTransactions();
    }

    private void loadTransactions() {
        UserDAO dao = new UserDAO();
        ResultSet rs = dao.getTransactionHistory(accountID);

        try {
            while (rs != null && rs.next()) {
                String type = rs.getString("TransactionType");
                double amount = rs.getDouble("Amount");
                String date = rs.getTimestamp("TransactionDate").toString().substring(0, 16);
                
                // KRİTİK DÜZELTME: Eğer bu hesap 'Gönderen' tarafındaysa para çıkmıştır (Kırmızı)
                int senderID = rs.getInt("SenderAccountID");
                boolean isOut = (senderID == this.accountID);

                // Özel durum: ATM-Yatır işlemi SenderID tutsa bile her zaman yeşildir
                if (type.contains("Yatır")) isOut = false;
                // Özel durum: ATM-Çek işlemi her zaman kırmızıdır
                if (type.contains("Çek")) isOut = true;

                listContainer.add(createTransactionItem(type, amount, date, isOut));
                listContainer.add(Box.createVerticalStrut(12));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JPanel createTransactionItem(String type, double amount, String date, boolean isOut) {
        JPanel p = new JPanel(null);
        Dimension itemSize = new Dimension(350, 85);
        p.setMaximumSize(itemSize);
        p.setPreferredSize(itemSize);
        p.setMinimumSize(itemSize);
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JLabel lblType = new JLabel(type);
        lblType.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblType.setBounds(20, 15, 200, 20);
        p.add(lblType);

        JLabel lblDate = new JLabel(date);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDate.setForeground(Color.GRAY);
        lblDate.setBounds(20, 40, 200, 20);
        p.add(lblDate);

        // Sembol ve Renk Ayarı
        String sign = isOut ? "-" : "+";
        Color amountColor = isOut ? new Color(180, 0, 0) : new Color(0, 120, 60); // Çıkışsa Kırmızı, Girişse Yeşil
        
        JLabel lblAmount = new JLabel(sign + " ₺" + String.format("%.2f", amount));
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAmount.setForeground(amountColor);
        lblAmount.setHorizontalAlignment(SwingConstants.RIGHT);
        lblAmount.setBounds(180, 30, 150, 25);
        p.add(lblAmount);

        return p;
    }

    // IBAN formatlama metodunu buraya da ekledik
    private String formatIban(String iban) {
        if (iban == null || iban.isEmpty()) return "";
        return iban.replaceAll("(.{4})", "$1 ").trim();
    }
}