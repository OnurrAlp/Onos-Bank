package com.onosbank.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import com.onosbank.db.UserDAO;

public class TransferFrame extends JFrame {
    private String username;
    private int senderID = -1, receiverID = -1;
    private double senderBalance = 0;
    private JPanel mainPanel, step1, stepChoice, stepInternal;
    private final Color primaryColor = new Color(0, 51, 102);
    private final Color bgColor = new Color(245, 247, 250);

    public TransferFrame(String username) {
        this.username = username;
        setResizable(false);
        setTitle("Para Transferi");
        setBounds(150, 150, 415, 680);
        mainPanel = new JPanel(null);
        setContentPane(mainPanel);

        initStep1(); // 1. Adım: Kaynak hesap seçimi
    }

    private void initStep1() {
        step1 = createBasePanel("Para Göndericek Hesabı Seçin", null);
        mainPanel.add(step1);
        loadAccountsToPanel(step1, -1, "SOURCE"); 
    }

    private void initStepChoice() {
        stepChoice = createBasePanel("Transfer Türünü Seçin", step1);
        mainPanel.add(stepChoice);

        JButton btnOwn = createLargeButton("Kendi Hesabıma", 150);
        JButton btnOther = createLargeButton("Başka Hesaba (IBAN)", 230);

        stepChoice.add(btnOwn);
        stepChoice.add(btnOther);

        btnOwn.addActionListener(e -> initStepInternal());
        btnOther.addActionListener(e -> {
            new OtherAccountTransferFrame(username, senderID, senderBalance).setVisible(true);
            this.dispose();
        });

        slide(step1, stepChoice, true);
    }

    private void initStepInternal() {
        stepInternal = createBasePanel("Alıcı Hesabı Seçin", stepChoice);
        mainPanel.add(stepInternal);
        loadAccountsToPanel(stepInternal, senderID, "INTERNAL_DEST");
        slide(stepChoice, stepInternal, true);
    }

    private void loadAccountsToPanel(JPanel panel, int excludeID, String mode) {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(bgColor);

        UserDAO dao = new UserDAO();
        ResultSet rs = dao.getMyAccounts(username, "");

        try {
            while (rs != null && rs.next()) {
                int id = rs.getInt("AccountID");
                if (id == excludeID) continue;

                String iban = rs.getString("IBAN");
                double bal = rs.getDouble("Balance");

                JPanel card = createAccountCard(id, iban, bal, mode);
                card.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                list.add(card);
                list.add(Box.createVerticalStrut(15));
            }
        } catch (Exception e) { e.printStackTrace(); }

        JScrollPane sc = new JScrollPane(list);
        sc.setBounds(20, 80, 375, 430);
        sc.setBorder(null);
        sc.getViewport().setBackground(bgColor);
        sc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(sc);
    }

    private JPanel createAccountCard(int id, String iban, double bal, String mode) {
        JPanel p = new JPanel(null);
        Dimension size = new Dimension(350, 100);
        p.setPreferredSize(size);
        p.setMaximumSize(size);
        p.setMinimumSize(size);
        p.setBackground(Color.WHITE);
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JLabel lblI = new JLabel(formatIban(iban));
        lblI.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblI.setBounds(20, 20, 310, 20);
        p.add(lblI);

        JLabel lblB = new JLabel("Bakiye: ₺ " + String.format("%.2f", bal));
        lblB.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblB.setForeground(new Color(0, 102, 51));
        lblB.setBounds(20, 50, 250, 25);
        p.add(lblB);

        MouseAdapter ma = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (mode.equals("SOURCE")) {
                    senderID = id;
                    senderBalance = bal;
                    initStepChoice();
                } else if (mode.equals("INTERNAL_DEST")) {
                    receiverID = id;
                    askAmount();
                }
            }
            public void mouseEntered(MouseEvent e) {
                p.setBackground(new Color(242, 245, 250));
                p.setBorder(BorderFactory.createLineBorder(primaryColor, 1));
            }
            public void mouseExited(MouseEvent e) {
                p.setBackground(Color.WHITE);
                p.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
            }
        };
        p.addMouseListener(ma);
        return p;
    }

    private void askAmount() {
    String input = JOptionPane.showInputDialog(this, "Gönderilecek Miktar:", "Transfer Tutarı", JOptionPane.QUESTION_MESSAGE);
    if (input != null && !input.isEmpty()) {
        try {
            double amount = Double.parseDouble(input.replace(",", "."));
            if (amount <= 0) throw new Exception();
            if (amount > senderBalance) {
                JOptionPane.showMessageDialog(this, "Yetersiz Bakiye!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, String.format("%.2f", amount) + " ₺ transferi onaylıyor musunuz?", "Transfer Onayı", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (new UserDAO().processTransfer(senderID, receiverID, amount)) {
                    JOptionPane.showMessageDialog(this, "Transfer Başarıyla Tamamlandı!");
                    
                    // --- KRİTİK NOKTA: DASHBOARD'U YENİLE ---
                    if (DashboardFrame.instance != null) {
                        DashboardFrame.instance.loadData();
                    }
                    // ----------------------------------------

                    this.dispose();
                }
            }
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Lütfen geçerli bir sayısal tutar giriniz!"); 
        }
    }
}

    private JPanel createBasePanel(String title, JPanel prevPanel) {
        JPanel p = new JPanel(null);
        p.setBounds(0, 0, 415, 680);
        p.setBackground(bgColor);
        
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lbl.setBounds(25, 30, 350, 30);
        p.add(lbl);

        JButton btnBack = new JButton(prevPanel == null ? "ANA SAYFAYA DÖN" : "GERİ DÖN");
        btnBack.setBounds(25, 540, 350, 45);
        btnBack.setBackground(new Color(230, 230, 230));
        btnBack.setForeground(primaryColor);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBack.setFocusPainted(false);
        btnBack.setBorder(null);
        
        btnBack.addActionListener(e -> {
            if (prevPanel == null) this.dispose();
            else slide(p, prevPanel, false);
        });
        p.add(btnBack);

        return p;
    }

    private JButton createLargeButton(String text, int y) {
        JButton b = new JButton(text);
        b.setBounds(50, y, 315, 65);
        b.setBackground(Color.WHITE);
        b.setForeground(primaryColor);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(primaryColor, 1));
        
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                b.setBackground(primaryColor);
                b.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent e) { 
                b.setBackground(Color.WHITE);
                b.setForeground(primaryColor);
            }
        });
        return b;
    }

    private void slide(JPanel out, JPanel in, boolean forward) {
        in.setVisible(true);
        in.setLocation(forward ? 415 : -415, 0);
        Timer t = new Timer(5, e -> {
            int xO = out.getX() + (forward ? -20 : 20);
            int xI = in.getX() + (forward ? -20 : 20);
            out.setLocation(xO, 0);
            in.setLocation(xI, 0);
            if (forward ? xI <= 0 : xI >= 0) {
                in.setLocation(0, 0);
                ((Timer)e.getSource()).stop();
                out.setVisible(false);
            }
        });
        t.start();
    }

    private String formatIban(String iban) {
        if (iban == null || iban.isEmpty()) return "";
        return iban.replaceAll("(.{4})", "$1 ").trim();
    }
}