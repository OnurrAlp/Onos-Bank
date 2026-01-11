package com.onosbank.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.onosbank.db.UserDAO;

public class EditProfileFrame extends JFrame {
    private String username;
    private JPanel mainContainer, viewPanel, editStep1, editStep2, editStep3;
    private JTextField txtAd, txtSoyad, txtEmail, txtPhone;
    private JPasswordField txtOldPass, txtNewPass, txtNewPassC;
    private JLabel lblNameVal, lblEmailVal, lblPhoneVal, lblBirthVal;
    
    private final Color primaryColor = new Color(0, 51, 102);
    private final Color successColor = new Color(0, 102, 51);
    private final Color secondaryColor = new Color(235, 235, 235);

    public EditProfileFrame(String username) {
        this.username = username;
        setTitle("Profil Bilgilerim");
        setResizable(false);
        setBounds(100, 100, 415, 750);
        
        mainContainer = new JPanel(null);
        mainContainer.setBackground(Color.WHITE);
        setContentPane(mainContainer);

        initViewPanel();
        initEditSteps();
        loadUserDataFromDB();
    }

    private void initViewPanel() {
        viewPanel = new JPanel(null);
        viewPanel.setBounds(0, 0, 415, 750);
        viewPanel.setBackground(Color.WHITE);
        mainContainer.add(viewPanel);

        JLabel lblTitle = new JLabel("PROFİL BİLGİLERİM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setBounds(50, 40, 300, 40);
        lblTitle.setHorizontalAlignment(0);
        viewPanel.add(lblTitle);

        // --- SABİT BAŞLIKLAR VE VERİ ALANLARI ---
        String[] headers = {"Ad Soyad:", "E-posta:", "Telefon:", "Doğum Tarihi:"};
        int yStart = 120;

        for (int i = 0; i < 4; i++) {
            JLabel lblH = new JLabel(headers[i]);
            lblH.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblH.setForeground(Color.GRAY);
            lblH.setBounds(50, yStart + (i * 80), 150, 20);
            viewPanel.add(lblH);
        }

        lblNameVal = createDataLabel(yStart + 25);
        lblEmailVal = createDataLabel(yStart + 105);
        lblPhoneVal = createDataLabel(yStart + 185);
        lblBirthVal = createDataLabel(yStart + 265);

        JButton btnStartEdit = new JButton("BİLGİLERİ DÜZENLE");
        btnStartEdit.setBounds(50, 480, 315, 50);
        btnStartEdit.setBackground(primaryColor);
        btnStartEdit.setForeground(Color.WHITE);
        btnStartEdit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnStartEdit.setFocusPainted(false);
        viewPanel.add(btnStartEdit);

        JButton btnBackHome = new JButton("ANA SAYFAYA DÖN");
        btnBackHome.setBounds(50, 545, 315, 50);
        btnBackHome.setBackground(secondaryColor);
        btnBackHome.setForeground(primaryColor);
        btnBackHome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBackHome.setFocusPainted(false);
        viewPanel.add(btnBackHome);

        btnBackHome.addActionListener(e -> this.dispose());
        btnStartEdit.addActionListener(e -> slide(viewPanel, editStep1, true));
    }

    private void initEditSteps() {
        // Step 1: Ad Soyad
        editStep1 = createStepPanel("AD VE SOYAD GÜNCELLE", 415, viewPanel);
        txtAd = createField(editStep1, "Adınız", 150);
        txtSoyad = createField(editStep1, "Soyadınız", 220);
        JButton btnNext1 = createModernButton(editStep1, "İLERİ >", 320, primaryColor);

        // Step 2: İletişim
        editStep2 = createStepPanel("İLETİŞİM GÜNCELLE", 415, editStep1);
        txtEmail = createField(editStep2, "E-posta", 150);
        txtPhone = createField(editStep2, "Telefon (11 Hane)", 220);
        
        txtPhone.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) || txtPhone.getText().length() >= 11) e.consume();
            }
        });

        JButton btnNext2 = createModernButton(editStep2, "İLERİ >", 320, primaryColor);

        // Step 3: Şifre
        editStep3 = createStepPanel("GÜVENLİK GÜNCELLEME", 415, editStep2);
        txtOldPass = createPasswordField(editStep3, "Mevcut Şifre", 130);
        txtNewPass = createPasswordField(editStep3, "Yeni Şifre (Değişmeyecekse Boş Bırakın)", 200);
        txtNewPassC = createPasswordField(editStep3, "Yeni Şifre Tekrar", 270);
        JButton btnFinish = createModernButton(editStep3, "GÜNCELLEMEYİ TAMAMLA", 360, successColor);

        // --- BUTON AKSİYONLARI ---
        btnNext1.addActionListener(e -> slide(editStep1, editStep2, true));
        
        btnNext2.addActionListener(e -> {
            String email = txtEmail.getText().toLowerCase().trim();
            String phone = txtPhone.getText().trim();
            UserDAO dao = new UserDAO();

            if (!email.endsWith("@gmail.com") && !email.endsWith("@outlook.com")) {
                JOptionPane.showMessageDialog(this, "E-posta @gmail.com veya @outlook.com olmalı!");
            } else if (phone.length() != 11) {
                JOptionPane.showMessageDialog(this, "Telefon numarası 11 haneli olmalıdır!");
            } else if (dao.isEmailTaken(email) && !email.equals(lblEmailVal.getText())) {
                JOptionPane.showMessageDialog(this, "Bu e-posta başka bir kullanıcı tarafından kullanılıyor!");
            } else if (dao.isPhoneTaken(phone) && !phone.equals(lblPhoneVal.getText())) {
                JOptionPane.showMessageDialog(this, "Bu telefon numarası başka bir kullanıcı tarafından kullanılıyor!");
            } else {
                slide(editStep2, editStep3, true);
            }
        });

        btnFinish.addActionListener(e -> handleUpdate());

        mainContainer.add(editStep1); mainContainer.add(editStep2); mainContainer.add(editStep3);
    }

    private void handleUpdate() {
        String oldP = new String(txtOldPass.getPassword());
        String newP = new String(txtNewPass.getPassword());
        String newPC = new String(txtNewPassC.getPassword());
        UserDAO dao = new UserDAO();

        if (!dao.checkOldPassword(username, oldP)) {
            JOptionPane.showMessageDialog(this, "Mevcut şifreniz hatalı!");
            return;
        }

        String passwordToSave = oldP; // Değişmezse eski şifre kalsın
        if (!newP.isEmpty()) {
            if (newP.equals(oldP)) {
                JOptionPane.showMessageDialog(this, "Yeni şifre eski şifre ile aynı olamaz!");
                return;
            } else if (!newP.equals(newPC)) {
                JOptionPane.showMessageDialog(this, "Yeni şifreler birbiriyle uyuşmuyor!");
                return;
            } else if (!isPasswordStrong(newP)) {
                JOptionPane.showMessageDialog(this, "Şifre en az 1 büyük harf, 1 rakam içermeli ve en az 5 karakter olmalıdır!");
                return;
            }
            passwordToSave = newP;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bilgileriniz güncellenecektir. Onaylıyor musunuz?", "Onay", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.updateUserInfo(username, txtAd.getText(), txtSoyad.getText(), txtEmail.getText(), txtPhone.getText(), passwordToSave)) {
                JOptionPane.showMessageDialog(this, "Profil başarıyla güncellendi!");
                loadUserDataFromDB();
                slide(editStep3, viewPanel, false);
            }
        }
    }

    private boolean isPasswordStrong(String p) {
        return p.length() >= 5 && p.matches(".*[A-Z].*") && p.matches(".*[0-9].*");
    }

    private void loadUserDataFromDB() {
        UserDAO dao = new UserDAO();
        Object[] data = dao.getUserFullDetails(username);
        if (data != null) {
            lblNameVal.setText(data[0] + " " + data[1]);
            lblEmailVal.setText((String)data[2]);
            lblPhoneVal.setText((String)data[3]);
            lblBirthVal.setText(data[4].toString());
            txtAd.setText((String)data[0]); 
            txtSoyad.setText((String)data[1]);
            txtEmail.setText((String)data[2]); 
            txtPhone.setText((String)data[3]);
        }
    }

    private void slide(JPanel out, JPanel in, boolean fwd) {
        in.setVisible(true);
        in.setLocation(fwd ? 415 : -415, 0);
        Timer t = new Timer(5, e -> {
            int xO = out.getX() + (fwd ? -20 : 20);
            int xI = in.getX() + (fwd ? -20 : 20);
            out.setLocation(xO, 0);
            in.setLocation(xI, 0);
            if (fwd ? xI <= 0 : xI >= 0) {
                in.setLocation(0, 0);
                ((Timer)e.getSource()).stop();
                out.setVisible(false);
            }
        });
        t.start();
    }

    private JLabel createDataLabel(int y) {
        JLabel l = new JLabel("...");
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(primaryColor);
        l.setBounds(50, y, 315, 25);
        viewPanel.add(l);
        return l;
    }

    private JPanel createStepPanel(String title, int x, JPanel prev) {
        JPanel p = new JPanel(null);
        p.setBounds(x, 0, 415, 750);
        p.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setBounds(50, 40, 300, 30);
        lbl.setHorizontalAlignment(0);
        p.add(lbl);

        JButton btnBack = new JButton("< GERİ GİT");
        btnBack.setBounds(50, 430, 315, 45);
        btnBack.setBackground(secondaryColor);
        btnBack.setForeground(primaryColor);
        btnBack.setFocusPainted(false);
        p.add(btnBack);
        btnBack.addActionListener(e -> slide(p, prev, false));
        return p;
    }

    private JTextField createField(JPanel p, String t, int y) {
        JTextField f = new JTextField();
        f.setBorder(BorderFactory.createTitledBorder(t));
        f.setBounds(50, y, 315, 55);
        p.add(f);
        return f;
    }

    private JPasswordField createPasswordField(JPanel p, String t, int y) {
        JPasswordField f = new JPasswordField();
        f.setBorder(BorderFactory.createTitledBorder(t));
        f.setBounds(50, y, 315, 55);
        p.add(f);
        return f;
    }

    private JButton createModernButton(JPanel p, String t, int y, Color c) {
        JButton b = new JButton(t);
        b.setBounds(50, y, 315, 50);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        p.add(b);
        return b;
    }
}