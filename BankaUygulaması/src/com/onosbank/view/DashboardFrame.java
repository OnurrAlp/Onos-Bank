package com.onosbank.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.onosbank.db.UserDAO;

public class DashboardFrame extends JFrame {
    private String username;
    private JPanel mainContent, sideMenu;
    private JLabel lblBalance, lblIban, lblWelcome;
    private boolean isMenuOpen = false;
    private int currentAccountID; // Hesap hareketleri için ID saklıyoruz
    public static DashboardFrame instance;

    public DashboardFrame(String username) {
        instance = this;
        this.username = username;
        setResizable(false);
        setTitle("ONOS BANK - Ana Sayfa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 415, 680);
        
        mainContent = new JPanel(null);
        mainContent.setBackground(new Color(245, 247, 250));
        setContentPane(mainContent);

        // --- HEADER ---
        JPanel header = new JPanel(null);
        header.setBackground(Color.WHITE);
        header.setBounds(0, 0, 415, 60);
        mainContent.add(header);

        JButton btnProfile = new JButton("...");
        btnProfile.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnProfile.setBounds(15, 10, 45, 40);
        btnProfile.setBackground(new Color(0, 51, 102));
        btnProfile.setForeground(Color.WHITE);
        btnProfile.setFocusPainted(false);
        btnProfile.setBorder(null);
        header.add(btnProfile);

        lblWelcome = new JLabel("Hoş geldin");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblWelcome.setBounds(70, 17, 200, 25);
        header.add(lblWelcome);

        // --- BAKİYE KARTI ---
        JPanel cardPanel = new JPanel(null);
        cardPanel.setBackground(new Color(0, 51, 102));
        cardPanel.setBounds(25, 80, 350, 160);
        mainContent.add(cardPanel);

        lblBalance = new JLabel("₺ 0,00");
        lblBalance.setForeground(Color.WHITE);
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblBalance.setBounds(20, 40, 300, 40);
        cardPanel.add(lblBalance);

        lblIban = new JLabel("TR...");
        lblIban.setForeground(new Color(200, 200, 200));
        lblIban.setBounds(20, 120, 310, 20);
        cardPanel.add(lblIban);

        // --- TÜM HESAPLARIM ---
        JLabel lblAllAcc = new JLabel("Tüm Hesaplarım");
        lblAllAcc.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAllAcc.setBounds(25, 255, 150, 25);
        mainContent.add(lblAllAcc);

        JButton btnGoAllAcc = new JButton("Hepsini Gör >");
        btnGoAllAcc.setBounds(260, 255, 120, 25);
        btnGoAllAcc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGoAllAcc.setForeground(new Color(0, 102, 204));
        btnGoAllAcc.setContentAreaFilled(false);
        btnGoAllAcc.setBorderPainted(false);
        btnGoAllAcc.setFocusPainted(false);
        mainContent.add(btnGoAllAcc);
        
        btnGoAllAcc.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if(!isMenuOpen) btnGoAllAcc.setForeground(new Color(0, 51, 102)); }
            public void mouseExited(MouseEvent e) { btnGoAllAcc.setForeground(new Color(0, 102, 204)); }
        });

        // --- KISAYOLLAR ---
        JLabel lblShortcuts = new JLabel("Kısayollarım");
        lblShortcuts.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblShortcuts.setBounds(25, 305, 200, 25);
        mainContent.add(lblShortcuts);

        createShortcutButton("Para Çekme", 25, 340);
        createShortcutButton("Para Yatırma", 205, 340);
        createShortcutButton("Hesap Hareketleri", 25, 425);
        createShortcutButton("Para Transferi", 205, 425);

        // --- SIDE MENU ---
        initSideMenu();

        btnProfile.addActionListener(e -> toggleSideMenu());
        btnGoAllAcc.addActionListener(e -> new AllAccountsFrame(username).setVisible(true));
        
        loadData();
    }

    private void initSideMenu() {
        sideMenu = new JPanel(null);
        sideMenu.setBounds(-300, 0, 280, 680); 
        sideMenu.setBackground(Color.WHITE);
        sideMenu.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        mainContent.add(sideMenu, 0); 

        String[] menuItems = {"Bilgilerim", "Tüm Hesaplarım", "Para Çekme", "Para Yatırma", "Hesap Hareketleri", "Güvenli Çıkış"};
        int yPos = 80;
        for (String item : menuItems) {
            JButton btn = new JButton(item);
            btn.setBounds(0, yPos, 280, 50);
            btn.setFocusPainted(false);
            btn.setBackground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            btn.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(new Color(240, 245, 255));
                    btn.setForeground(new Color(0, 102, 204));
                }
                public void mouseExited(MouseEvent e) {
                    btn.setBackground(Color.WHITE);
                    btn.setForeground(Color.BLACK);
                }
            });

            btn.addActionListener(e -> handleMenuAction(item));
            sideMenu.add(btn);
            yPos += 55;
        }
    }

    private void setBackgroundButtonsEnabled(boolean enabled) {
        for (Component c : mainContent.getComponents()) {
            if (c != sideMenu && c instanceof JButton) {
                JButton btn = (JButton) c;
                btn.setEnabled(enabled);
                if (!enabled) {
                    btn.setBackground(Color.WHITE);
                    btn.setForeground(Color.BLACK);
                }
            }
        }
    }

    private void toggleSideMenu() {
        if (!isMenuOpen) {
            setBackgroundButtonsEnabled(false);
        }
        
        Timer timer = new Timer(5, new ActionListener() {
            int x = sideMenu.getX();
            public void actionPerformed(ActionEvent e) {
                if (!isMenuOpen) {
                    x += 20;
                    if (x >= 0) {
                        sideMenu.setLocation(0, 0);
                        isMenuOpen = true;
                        ((Timer)e.getSource()).stop();
                    }
                } else {
                    x -= 20;
                    if (x <= -300) {
                        sideMenu.setLocation(-300, 0);
                        isMenuOpen = false;
                        setBackgroundButtonsEnabled(true);
                        ((Timer)e.getSource()).stop();
                    }
                }
                sideMenu.setLocation(x, 0);
            }
        });
        timer.start();
    }

    private void handleMenuAction(String action) {
        if (action.equals("Güvenli Çıkış")) {
            new LoginFrame().setVisible(true);
            this.dispose();
        } else if (action.equals("Tüm Hesaplarım")) {
            new AllAccountsFrame(username).setVisible(true);
        } else if (action.equals("Bilgilerim")) {
            new EditProfileFrame(username).setVisible(true);
        } else if (action.equals("Para Çekme")) {
            new CashActionFrame(username, "Çek").setVisible(true);
        } else if (action.equals("Para Yatırma")) {
            new CashActionFrame(username, "Yatır").setVisible(true);
        } else if (action.equals("Para Transferi")) {
            new TransferFrame(username).setVisible(true);
        } else if (action.equals("Hesap Hareketleri")) {
            new TransactionHistoryFrame(username, currentAccountID, lblIban.getText()).setVisible(true);
        }
        toggleSideMenu();
    }

    private void createShortcutButton(String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 175, 75);
        btn.setBackground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!isMenuOpen) {
                    btn.setBackground(new Color(0, 51, 102));
                    btn.setForeground(Color.WHITE);
                }
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.BLACK);
            }
        });

        btn.addActionListener(e -> {
            if (text.equals("Para Çekme")) new CashActionFrame(username, "Çek").setVisible(true);
            else if (text.equals("Para Yatırma")) new CashActionFrame(username, "Yatır").setVisible(true);
            else if (text.equals("Para Transferi")) new TransferFrame(username).setVisible(true);
            else if (text.equals("Hesap Hareketleri")) new TransactionHistoryFrame(username, currentAccountID, lblIban.getText()).setVisible(true);
        });

        mainContent.add(btn);
    }

    public void loadData() {
        UserDAO dao = new UserDAO();
        Object[] data = dao.getDashboardData(username);
        if (data != null) {
            lblWelcome.setText("Hoş geldin, " + data[0]);
            // IBAN'ı formatIban metodundan geçirerek yazdırıyoruz
            lblIban.setText(formatIban((String) data[1])); 
            lblBalance.setText("₺ " + String.format("%.2f", (Double) data[2]));
            currentAccountID = (int) data[3];
        }
    }
    private String formatIban(String iban) {
        if (iban == null || iban.isEmpty()) return "";
        return iban.replaceAll("(.{4})", "$1 ").trim();
    }
}