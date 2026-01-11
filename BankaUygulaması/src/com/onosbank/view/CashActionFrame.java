package com.onosbank.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import com.onosbank.db.UserDAO;

public class CashActionFrame extends JFrame {
    private String username, actionType;
    private JPanel mainPanel, stepAccount, stepAmount;
    private int selectedAccID = -1;
    private double selectedAccBalance = 0;
    private final Color primaryColor = new Color(0, 51, 102);
    private final Color bgColor = new Color(245, 247, 250);

    public CashActionFrame(String username, String actionType) {
        this.username = username;
        this.actionType = actionType; // "Çek" veya "Yatır"
        
        setResizable(false);
        setTitle("Para " + actionType);
        setBounds(150, 150, 415, 680);
        
        mainPanel = new JPanel(null);
        setContentPane(mainPanel);

        initStepAccount();
    }

    private void initStepAccount() {
        stepAccount = createBasePanel("İşlem Yapılacak Hesabı Seçin", null);
        mainPanel.add(stepAccount);

        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(bgColor);

        UserDAO dao = new UserDAO();
        ResultSet rs = dao.getMyAccounts(username, "");

        try {
            while (rs != null && rs.next()) {
                int id = rs.getInt("AccountID");
                String iban = rs.getString("IBAN");
                double bal = rs.getDouble("Balance");

                // AllAccountsFrame ile uyumlu kart yapısı
                JPanel card = createAccountSelectCard(id, iban, bal);
                card.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                listContainer.add(card);
                listContainer.add(Box.createVerticalStrut(15));
            }
        } catch (Exception e) { e.printStackTrace(); }

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBounds(20, 80, 375, 430);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(bgColor);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        stepAccount.add(scroll);
    }

    private JPanel createAccountSelectCard(int id, String iban, double bal) {
        JPanel p = new JPanel(null);
        Dimension cardSize = new Dimension(350, 100);
        p.setPreferredSize(cardSize);
        p.setMaximumSize(cardSize);
        p.setMinimumSize(cardSize);
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
                selectedAccID = id;
                selectedAccBalance = bal;
                initStepAmount();
            }
            public void mouseEntered(MouseEvent e) {
                p.setBackground(new Color(242, 245, 255));
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

    private void initStepAmount() {
        stepAmount = createBasePanel("Miktar Giriniz (" + actionType + ")", stepAccount);
        mainPanel.add(stepAmount);

        JTextField txtAmount = new JTextField();
        txtAmount.setBounds(50, 150, 315, 65);
        txtAmount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        txtAmount.setHorizontalAlignment(JTextField.CENTER);
        txtAmount.setBorder(BorderFactory.createTitledBorder(null, "Tutar (₺)", 0, 0, new Font("Segoe UI", Font.PLAIN, 12), Color.GRAY));
        stepAmount.add(txtAmount);

        JButton btnConfirm = new JButton(actionType.toUpperCase() + " İŞLEMİNİ ONAYLA");
        btnConfirm.setBounds(50, 260, 315, 55);
        btnConfirm.setBackground(primaryColor);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorder(null);

        btnConfirm.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(txtAmount.getText().replace(",", "."));
                if (amount <= 0) throw new Exception();
                
                if (actionType.equals("Çek") && amount > selectedAccBalance) {
                    JOptionPane.showMessageDialog(this, "Yetersiz bakiye! Mevcut bakiye: ₺" + String.format("%.2f", selectedAccBalance));
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(this, 
                    String.format("%.2f", amount) + " ₺ tutarındaki " + actionType + " işlemini onaylıyor musunuz?", 
                    "İşlem Onayı", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    UserDAO dao = new UserDAO();
                    if (dao.processCashAction(selectedAccID, amount, actionType)) {
                        JOptionPane.showMessageDialog(this, "İşlem Başarıyla Gerçekleştirildi!");
                        if (DashboardFrame.instance != null) DashboardFrame.instance.loadData();
                        this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "İşlem sırasında bir hata oluştu!");
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lütfen geçerli bir miktar giriniz!");
            }
        });
        stepAmount.add(btnConfirm);

        slide(stepAccount, stepAmount, true);
    }

    private JPanel createBasePanel(String title, JPanel prevPanel) {
        JPanel p = new JPanel(null);
        p.setBounds(0, 0, 415, 680);
        p.setBackground(bgColor);
        
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setBounds(25, 30, 350, 30);
        p.add(lbl);

        JButton btnBack = new JButton(prevPanel == null ? "ANA SAYFAYA DÖN" : "GERİ DÖN");
        btnBack.setBounds(25, 540, 350, 45);
        btnBack.setBackground(new Color(230, 230, 230));
        btnBack.setForeground(primaryColor);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBack.setFocusPainted(false);
        btnBack.setBorder(null);
        
        btnBack.addActionListener(e -> {
            if (prevPanel == null) this.dispose();
            else slide(p, prevPanel, false);
        });
        p.add(btnBack);

        return p;
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