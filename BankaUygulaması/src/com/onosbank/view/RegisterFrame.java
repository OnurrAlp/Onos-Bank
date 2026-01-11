package com.onosbank.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import com.onosbank.db.UserDAO;

public class RegisterFrame extends JFrame {
    private JPanel mainContainer;
    private JPanel step1Panel, step2Panel, step3Panel;
    private String ad, soyad, kullaniciAdi, sifre, eposta, telefon, dogumTarihi;
    private final Color primaryColor = new Color(0, 51, 102);
    private JButton btnNext1, btnNext2, btnFinish;

    public RegisterFrame() {
        setResizable(false);
        setTitle("ONOS BANK - Yeni Kayıt");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 415, 680);

        mainContainer = new JPanel();
        mainContainer.setLayout(null);
        mainContainer.setBackground(Color.WHITE);
        setContentPane(mainContainer);

        initStep1();
        initStep2();
        initStep3();

        getRootPane().setDefaultButton(btnNext1);
    }

    private void initStep1() {
        step1Panel = createPanel(0);
        mainContainer.add(step1Panel);
        addTitle(step1Panel, "KİŞİSEL BİLGİLER");

        JTextField txtAd = createField(step1Panel, "Adınız", 150);
        JTextField txtSoyad = createField(step1Panel, "Soyadınız", 220);

        btnNext1 = createButton(step1Panel, "İLERİ >", 330, primaryColor);
        JButton btnCancel = createButton(step1Panel, "İPTAL / GERİ", 390, primaryColor);

        btnNext1.addActionListener(e -> {
            if(txtAd.getText().trim().isEmpty() || txtSoyad.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ad ve Soyad boş bırakılamaz!");
            } else {
                this.ad = txtAd.getText();
                this.soyad = txtSoyad.getText();
                slidePanels(step1Panel, step2Panel, true);
                getRootPane().setDefaultButton(btnNext2);
            }
        });
        btnCancel.addActionListener(e -> { new LoginFrame().setVisible(true); this.dispose(); });
    }

    private void initStep2() {
        step2Panel = createPanel(415);
        mainContainer.add(step2Panel);
        addTitle(step2Panel, "HESAP GÜVENLİĞİ");

        JTextField txtRegUser = createField(step2Panel, "Kullanıcı Adı", 150);
        JPasswordField txtRegPass = createPasswordField(step2Panel, "Şifre", 220);
        JPasswordField txtRegPassC = createPasswordField(step2Panel, "Şifre Tekrar", 290);

        btnNext2 = createButton(step2Panel, "SON ADIMA GEÇ >", 380, primaryColor);
        JButton btnBack = createButton(step2Panel, "< GERİ DÖN", 440, primaryColor);

        btnNext2.addActionListener(e -> {
            String pass = new String(txtRegPass.getPassword());
            String user = txtRegUser.getText().trim();
            UserDAO dao = new UserDAO();

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Alanlar boş bırakılamaz!");
            } else if (dao.isUsernameTaken(user)) {
                JOptionPane.showMessageDialog(this, "Bu kullanıcı adı zaten alınmış!", "Hata", JOptionPane.ERROR_MESSAGE);
            } else if (!isPasswordStrong(pass)) {
                JOptionPane.showMessageDialog(this, "Şifre en az 5 karakter olmalı; büyük harf, küçük harf ve sayı içermelidir!", "Zayıf Şifre", JOptionPane.WARNING_MESSAGE);
            } else if (!pass.equals(new String(txtRegPassC.getPassword()))) {
                JOptionPane.showMessageDialog(this, "Şifreler uyuşmuyor!");
            } else {
                this.kullaniciAdi = user;
                this.sifre = pass;
                slidePanels(step2Panel, step3Panel, true);
                getRootPane().setDefaultButton(btnFinish);
            }
        });
        btnBack.addActionListener(e -> { slidePanels(step2Panel, step1Panel, false); getRootPane().setDefaultButton(btnNext1); });
    }

    private void initStep3() {
        step3Panel = createPanel(415);
        mainContainer.add(step3Panel);
        addTitle(step3Panel, "İLETİŞİM BİLGİLERİ");

        JTextField txtEmail = createField(step3Panel, "E-posta (Gmail/Outlook)", 150);
        JTextField txtPhone = createField(step3Panel, "Telefon (11 Hane)", 220);
        JTextField txtBirth = createField(step3Panel, "Doğum Tarihi (GG.AA.YYYY)", 290);

        txtPhone.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) || c == KeyEvent.VK_SPACE || txtPhone.getText().length() >= 11) e.consume();
            }
        });

        txtBirth.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) || txtBirth.getText().length() >= 10) e.consume();
            }
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) return;
                String t = txtBirth.getText();
                if (t.length() == 2 || t.length() == 5) txtBirth.setText(t + ".");
            }
        });

        btnFinish = createButton(step3Panel, "KAYDI TAMAMLA", 400, new Color(0, 102, 51));
        JButton btnBack = createButton(step3Panel, "< GERİ DÖN", 460, primaryColor);

        btnFinish.addActionListener(e -> {
            if (validateStep3(txtEmail.getText(), txtPhone.getText(), txtBirth.getText())) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Girdiğiniz bilgilerin doğruluğundan emin misiniz?", 
                    "Kayıt Onayı", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    UserDAO dao = new UserDAO();
                    boolean result = dao.registerUser(ad, soyad, kullaniciAdi, sifre, eposta, telefon, dogumTarihi);
                    if(result) {
                        JOptionPane.showMessageDialog(this, "Kayıt Başarılı!");
                        new LoginFrame().setVisible(true);
                        this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Kayıt sırasında bir hata oluştu!");
                    }
                }
            }
        });
        btnBack.addActionListener(e -> { slidePanels(step3Panel, step2Panel, false); getRootPane().setDefaultButton(btnNext2); });
    }

    private boolean isPasswordStrong(String pass) {
        if (pass.length() < 5) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : pass.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }

    private boolean validateStep3(String email, String phone, String birth) {
        UserDAO dao = new UserDAO();
        String em = email.toLowerCase().trim();
        
        if (!(em.endsWith("@gmail.com") || em.endsWith("@outlook.com"))) {
            JOptionPane.showMessageDialog(this, "Sadece Gmail veya Outlook kabul edilir!"); return false;
        }
        
        // E-POSTA KONTROLÜ
        if (dao.isEmailTaken(em)) {
            JOptionPane.showMessageDialog(this, "Bu e-posta zaten kayıtlı!"); return false;
        }

        if (phone.length() != 11 || !phone.startsWith("0")) {
            JOptionPane.showMessageDialog(this, "Telefon 11 haneli olmalı ve 0 ile başlamalı!"); return false;
        }
        
        // TELEFON KONTROLÜ (HATA BURADAYDI)
        if (dao.isPhoneTaken(phone)) {
            JOptionPane.showMessageDialog(this, "Bu telefon numarası zaten sisteme kayıtlı!", "Hata", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate bDay = LocalDate.parse(birth, fmt);
            if (Period.between(bDay, LocalDate.now()).getYears() < 18) {
                JOptionPane.showMessageDialog(this, "En az 18 yaşında olmalısınız!"); return false;
            }
            this.eposta = em; this.telefon = phone; this.dogumTarihi = birth; return true;
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Format GG.AA.YYYY olmalı!"); return false;
        }
    }

    private JPanel createPanel(int x) {
        JPanel p = new JPanel(); p.setBounds(x, 0, 400, 650);
        p.setBackground(Color.WHITE); p.setLayout(null); return p;
    }
    private void addTitle(JPanel p, String text) {
        JLabel l = new JLabel(text); l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18)); l.setBounds(50, 60, 300, 30); p.add(l);
    }
    private JTextField createField(JPanel p, String title, int y) {
        JTextField t = new JTextField(); t.setBorder(BorderFactory.createTitledBorder(title));
        t.setBounds(50, y, 300, 45); p.add(t); return t;
    }
    private JPasswordField createPasswordField(JPanel p, String title, int y) {
        JPasswordField t = new JPasswordField(); t.setBorder(BorderFactory.createTitledBorder(title));
        t.setBounds(50, y, 300, 45); p.add(t); return t;
    }
    private JButton createButton(JPanel p, String text, int y, Color c) {
        JButton b = new JButton(text); b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13)); b.setBounds(50, y, 300, 45); p.add(b); return b;
    }
    private void slidePanels(JPanel outgoing, JPanel incoming, boolean isForward) {
        incoming.setVisible(true);
        incoming.setLocation(isForward ? 415 : -415, 0);
        Timer timer = new Timer(5, new ActionListener() {
            int xOut = outgoing.getX(), xIn = incoming.getX();
            public void actionPerformed(ActionEvent event) {
                if (isForward) { xOut -= 20; xIn -= 20; if (xIn <= 0) finish(event); }
                else { xOut += 20; xIn += 20; if (xIn >= 0) finish(event); }
                outgoing.setLocation(xOut, 0); incoming.setLocation(xIn, 0);
            }
            private void finish(ActionEvent event) {
                incoming.setLocation(0, 0); ((Timer) event.getSource()).stop(); outgoing.setVisible(false);
            }
        });
        timer.start();
    }
}