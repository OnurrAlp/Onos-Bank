package com.onosbank.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import com.onosbank.db.UserDAO;

public class AllAccountsFrame extends JFrame {
    private String username;
    private JPanel mainPanel, listStep, detailStep;
    private JScrollPane scroll;
    private final Color primaryColor = new Color(0, 51, 102);
    private final Color bgColor = new Color(245, 247, 250);

    public AllAccountsFrame(String username) {
        this.username = username;
        setResizable(false);
        setTitle("Hesap Yönetimi");
        setBounds(150, 150, 415, 680);
        
        mainPanel = new JPanel(null);
        setContentPane(mainPanel);

        initListStep();
    }

    private void initListStep() {
        listStep = createBasePanel("Tüm Hesaplarım");
        mainPanel.add(listStep);

        // --- YENİ HESAP AÇ BUTONU ---
        JButton btnNewAcc = new JButton("+ Yeni Hesap Aç");
        btnNewAcc.setBounds(25, 70, 350, 50);
        btnNewAcc.setBackground(new Color(0, 102, 51));
        btnNewAcc.setForeground(Color.WHITE);
        btnNewAcc.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnNewAcc.setFocusPainted(false);
        btnNewAcc.setBorder(null);
        
        btnNewAcc.addActionListener(e -> {
            if(new UserDAO().openNewAccount(username)) {
                JOptionPane.showMessageDialog(this, "Yeni hesabınız başarıyla açıldı!");
                loadAccounts();
            }
        });
        
        listStep.add(btnAddBackAction(listStep, null));
        listStep.add(btnNewAcc);

        loadAccounts();
    }

    private void loadAccounts() {
        if (scroll != null) {
            listStep.remove(scroll);
        }

        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(bgColor);

        UserDAO dao = new UserDAO();
        ResultSet rs = dao.getMyAccounts(username, ""); 

        try {
            while (rs != null && rs.next()) {
                String iban = rs.getString("IBAN");
                double bal = rs.getDouble("Balance");
                boolean isDef = rs.getBoolean("isDefault");

                JPanel card = createAccountCard(iban, bal, isDef);
                // BoxLayout içinde tam ortalanması için hizalama kritik
                card.setAlignmentX(Component.CENTER_ALIGNMENT); 
                
                scrollContent.add(card);
                scrollContent.add(Box.createVerticalStrut(15)); // Kartlar arası dikey boşluk
            }
        } catch (Exception e) { e.printStackTrace(); }

        scroll = new JScrollPane(scrollContent);
        // Genişlik kartlara göre merkezlenmiş durumda
        scroll.setBounds(20, 135, 375, 385); 
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(bgColor);
        listStep.add(scroll);
        
        listStep.revalidate();
        listStep.repaint();
    }

    private void showDetail(String iban, double balance, boolean isDefault) {
        if (detailStep != null) mainPanel.remove(detailStep);

        detailStep = createBasePanel("Hesap Detayları");
        detailStep.setLocation(415, 0); 
        mainPanel.add(detailStep);

        JPanel infoCard = new JPanel(null);
        infoCard.setBounds(25, 80, 350, 140);
        infoCard.setBackground(Color.WHITE);
        infoCard.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        JLabel lblIVal = new JLabel(formatIban(iban));
        lblIVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblIVal.setBounds(20, 40, 310, 25);
        infoCard.add(lblIVal);

        JLabel lblBVal = new JLabel("₺ " + String.format("%.2f", balance));
        lblBVal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblBVal.setForeground(new Color(0, 102, 51));
        lblBVal.setBounds(20, 80, 310, 40);
        infoCard.add(lblBVal);
        detailStep.add(infoCard);

        JButton btnSetDef = new JButton(isDefault ? "VARSAYILAN HESABINIZ" : "ANA HESAP OLARAK AYARLA");
        btnSetDef.setBounds(25, 260, 350, 55);
        btnSetDef.setBackground(isDefault ? new Color(200, 200, 200) : primaryColor);
        btnSetDef.setForeground(isDefault ? Color.DARK_GRAY : Color.WHITE);
        btnSetDef.setEnabled(!isDefault);
        btnSetDef.setFocusPainted(false);
        
        btnSetDef.addActionListener(e -> {
            if (new UserDAO().setDefaultAccount(username, iban)) {
                JOptionPane.showMessageDialog(this, "Varsayılan hesap güncellendi.");
                if (DashboardFrame.instance != null) {
                    DashboardFrame.instance.loadData();
                }
                slide(detailStep, listStep, false);
                loadAccounts();
            }
        });
        detailStep.add(btnSetDef);

        JButton btnDelete = new JButton("HESABI KALICI OLARAK SİL");
        btnDelete.setBounds(25, 330, 350, 55);
        btnDelete.setBackground(new Color(180, 0, 0));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        
        btnDelete.addActionListener(e -> {
            if (isDefault) {
                JOptionPane.showMessageDialog(this, "Varsayılan hesabı silemezsiniz!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (balance > 0) {
                JOptionPane.showMessageDialog(this, "İçinde para bulunan bir hesabı silemezsiniz!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Hesabı silmek istediğinize emin misiniz?", "Hesap Sil", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (new UserDAO().deleteAccount(iban)) {
                    JOptionPane.showMessageDialog(this, "Hesap başarıyla silindi.");
                    slide(detailStep, listStep, false);
                    loadAccounts();
                } else {
                    JOptionPane.showMessageDialog(this, "Hesap silinirken bir hata oluştu.");
                }
            }
        });
        detailStep.add(btnDelete);

        detailStep.add(btnAddBackAction(detailStep, listStep));
        slide(listStep, detailStep, true);
    }

    private JPanel createAccountCard(String iban, double balance, boolean isDefault) {
        JPanel p = new JPanel(null);
        
        // Boyutu kesin olarak sabitleyen Dimension ayarı
        Dimension cardSize = new Dimension(350, 115);
        p.setPreferredSize(cardSize);
        p.setMinimumSize(cardSize);
        p.setMaximumSize(cardSize); 
        
        p.setBackground(Color.WHITE);
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.setBorder(BorderFactory.createLineBorder(isDefault ? primaryColor : new Color(230, 230, 230), isDefault ? 2 : 1));

        JLabel lblI = new JLabel(formatIban(iban));
        lblI.setFont(new Font("Segoe UI", Font.BOLD, 12)); 
        lblI.setBounds(25, 20, 310, 20); 
        p.add(lblI);

        JLabel lblB = new JLabel("₺ " + String.format("%.2f", balance));
        lblB.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblB.setForeground(new Color(0, 102, 51));
        lblB.setBounds(25, 50, 250, 30);
        p.add(lblB);

        if (isDefault) {
            JLabel lblCheck = new JLabel("ANA HESAP");
            lblCheck.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblCheck.setForeground(primaryColor);
            lblCheck.setHorizontalAlignment(SwingConstants.RIGHT);
            lblCheck.setBounds(230, 85, 100, 20);
            p.add(lblCheck);
        }

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showDetail(iban, balance, isDefault);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                p.setBackground(new Color(242, 245, 250));
                if (!isDefault) p.setBorder(BorderFactory.createLineBorder(primaryColor, 1));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                p.setBackground(Color.WHITE);
                p.setBorder(BorderFactory.createLineBorder(isDefault ? primaryColor : new Color(230, 230, 230), isDefault ? 2 : 1));
            }
        };

        p.addMouseListener(ma);
        lblI.addMouseListener(ma);
        lblB.addMouseListener(ma);
        
        return p;
    }

    private JPanel createBasePanel(String title) {
        JPanel p = new JPanel(null);
        p.setBounds(0, 0, 415, 680);
        p.setBackground(bgColor);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setBounds(25, 20, 300, 30);
        p.add(lbl);
        return p;
    }

    private JButton btnAddBackAction(JPanel current, JPanel target) {
        JButton b = new JButton(target == null ? "ANA SAYFAYA DÖN" : "LİSTEYE GERİ DÖN");
        b.setBounds(25, 540, 350, 45);
        b.setBackground(new Color(230, 230, 230));
        b.setForeground(primaryColor);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.addActionListener(e -> {
            if (target == null) this.dispose();
            else slide(current, target, false);
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