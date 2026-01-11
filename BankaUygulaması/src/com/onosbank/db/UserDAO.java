package com.onosbank.db;

import java.sql.*;
import java.util.Random;

public class UserDAO {

    // --- PROFİL GÜNCELLEME SORGULARI ---

    public boolean checkOldPassword(String username, String oldPassword) {
        String sql = "SELECT Password FROM Users WHERE Username = ? AND Password = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, oldPassword);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserInfo(String username, String newAd, String newSoyad, String newEmail, String newPhone, String newPass) {
        String sql = "UPDATE Users SET FirstName = ?, LastName = ?, Email = ?, PhoneNumber = ?, Password = ? WHERE Username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newAd);
            pstmt.setString(2, newSoyad);
            pstmt.setString(3, newEmail);
            pstmt.setString(4, newPhone);
            pstmt.setString(5, newPass);
            pstmt.setString(6, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- DASHBOARD VE LİSTELEME SORGULARI ---

    public Object[] getDashboardData(String username) {
    // Sorguya a.AccountID eklendi
    String sql = "SELECT u.FirstName + ' ' + u.LastName, a.IBAN, a.Balance, a.AccountID FROM Users u " +
                 "JOIN Accounts a ON u.UserID = a.UserID " +
                 "WHERE u.Username = ? AND a.isDefault = 1"; 
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, username);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return new Object[]{
                    rs.getString(1), // İsim Soyisim
                    rs.getString(2), // IBAN
                    rs.getDouble(3), // Bakiye
                    rs.getInt(4)     // AccountID (YENİ eklendi)
                };
            }
        }
    } catch (SQLException e) { e.printStackTrace(); }
    return null;
}

    public Object[] getUserFullDetails(String username) {
        String sql = "SELECT FirstName, LastName, Email, PhoneNumber, BirthDate, Password FROM Users WHERE Username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getDate(5).toString(), rs.getString(6)
                    };
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public ResultSet getMyAccounts(String username, String query) {
        // Sorguya AccountID ve isDefault eklendi
        String sql = "SELECT a.AccountID, a.IBAN, a.Balance, a.AccountType, a.isDefault FROM Accounts a " +
                     "JOIN Users u ON a.UserID = u.UserID " +
                     "WHERE u.Username = ? AND a.IBAN LIKE ? ORDER BY a.isDefault DESC";
        try {
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, "%" + query + "%");
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- KONTROL VE KAYIT SORGULARI ---

    public boolean isUsernameTaken(String u) { return checkExists("Username", u); }
    public boolean isPhoneTaken(String p) { return checkExists("PhoneNumber", p); }
    public boolean isEmailTaken(String e) { return checkExists("Email", e); }

    private boolean checkExists(String column, String value) {
        String sql = "SELECT COUNT(*) FROM Users WHERE " + column + " = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean registerUser(String ad, String soyad, String username, String password, 
                                 String email, String phone, String birthDate) {
        String userSql = "INSERT INTO Users (FirstName, LastName, Username, Password, Email, PhoneNumber, BirthDate, CreatedAt) " +
                         "VALUES (?, ?, ?, ?, ?, ?, CONVERT(DATE, ?, 104), GETDATE())";
        // Kayıtta açılan ilk hesap isDefault = 1 (Varsayılan) olarak ayarlandı
        String accountSql = "INSERT INTO Accounts (UserID, IBAN, Balance, AccountType, isDefault) VALUES (?, ?, ?, ?, 1)";
        
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); 

            PreparedStatement pstmtUser = conn.prepareStatement(userSql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmtUser.setString(1, ad); pstmtUser.setString(2, soyad);
            pstmtUser.setString(3, username); pstmtUser.setString(4, password);
            pstmtUser.setString(5, email); pstmtUser.setString(6, phone);
            pstmtUser.setString(7, birthDate);
            pstmtUser.executeUpdate();

            int userID = -1;
            try (ResultSet generatedKeys = pstmtUser.getGeneratedKeys()) {
                if (generatedKeys.next()) userID = generatedKeys.getInt(1);
            }

            PreparedStatement pstmtAcc = conn.prepareStatement(accountSql);
            pstmtAcc.setInt(1, userID);
            pstmtAcc.setString(2, generateIban());
            pstmtAcc.setDouble(3, 0.00); 
            pstmtAcc.setString(4, "Vadesiz");
            pstmtAcc.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        }
    }

    public boolean loginCheck(String u, String p) {
        String sql = "SELECT * FROM Users WHERE Username = ? AND Password = ?";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u); ps.setString(2, p);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { return false; }
    }

    // --- PARA İŞLEMLERİ ---

    public boolean processCashAction(int accountID, double amount, String actionType) {
        double finalAmount = actionType.equals("Çek") ? -amount : amount;
        String updateSql = "UPDATE Accounts SET Balance = Balance + ? WHERE AccountID = ?";
        String logSql = "INSERT INTO Transactions (SenderAccountID, ReceiverAccountID, Amount, TransactionType, TransactionDate) " +
                        "VALUES (?, ?, ?, ?, GETDATE())";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement psUpdate = conn.prepareStatement(updateSql);
            psUpdate.setDouble(1, finalAmount);
            psUpdate.setInt(2, accountID);
            psUpdate.executeUpdate();

            PreparedStatement psLog = conn.prepareStatement(logSql);
            psLog.setInt(1, accountID);
            psLog.setInt(2, accountID); 
            psLog.setDouble(3, amount);
            psLog.setString(4, "ATM - " + actionType);
            psLog.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        }
    }

    public boolean processTransfer(int senderAccID, int receiverAccID, double amount) {
        String updateSender = "UPDATE Accounts SET Balance = Balance - ? WHERE AccountID = ?";
        String updateReceiver = "UPDATE Accounts SET Balance = Balance + ? WHERE AccountID = ?";
        String logSql = "INSERT INTO Transactions (SenderAccountID, ReceiverAccountID, Amount, TransactionType, TransactionDate) VALUES (?, ?, ?, ?, GETDATE())";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement ps1 = conn.prepareStatement(updateSender);
            ps1.setDouble(1, amount);
            ps1.setInt(2, senderAccID);
            ps1.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement(updateReceiver);
            ps2.setDouble(1, amount);
            ps2.setInt(2, receiverAccID);
            ps2.executeUpdate();

            PreparedStatement ps3 = conn.prepareStatement(logSql);
            ps3.setInt(1, senderAccID);
            ps3.setInt(2, receiverAccID);
            ps3.setDouble(3, amount);
            ps3.setString(4, "Havale/EFT");
            ps3.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        }
    }

    public Object[] getReceiverByIban(String iban) {
    String sql = "SELECT u.FirstName, u.LastName, a.AccountID FROM Users u " +
                 "JOIN Accounts a ON u.UserID = a.UserID WHERE a.IBAN = ?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, iban);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return new Object[]{
                rs.getString("FirstName"), 
                rs.getString("LastName"), 
                rs.getInt("AccountID")
            };
        }
    } catch (SQLException e) { e.printStackTrace(); }
    return null; 
}

    // --- HESAP YÖNETİMİ ---

    public boolean setDefaultAccount(String username, String iban) {
        String sqlReset = "UPDATE Accounts SET isDefault = 0 WHERE UserID = (SELECT UserID FROM Users WHERE Username = ?)";
        String sqlSet = "UPDATE Accounts SET isDefault = 1 WHERE IBAN = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement psReset = conn.prepareStatement(sqlReset)) {
                psReset.setString(1, username);
                psReset.executeUpdate();
            }
            try (PreparedStatement psSet = conn.prepareStatement(sqlSet)) {
                psSet.setString(1, iban);
                psSet.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        }
    }

    public boolean openNewAccount(String username) {
        String sql = "INSERT INTO Accounts (UserID, IBAN, Balance, AccountType, isDefault) " +
                     "VALUES ((SELECT UserID FROM Users WHERE Username = ?), ?, 0.0, 'Vadesiz', 0)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, generateIban());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

 // UserDAO.java içindeki generateIban metodunu bununla değiştir:
    private String generateIban() {
        Random rd = new Random();
        StringBuilder sb = new StringBuilder("TR");
        
        // TR'den sonra 24 hane daha ekleyerek toplam 26 karaktere tamamlıyoruz
        // İlk 2 hane kontrol basamağı (Örn: 56)
        sb.append(String.format("%02d", rd.nextInt(100))); 
        
        // Geri kalan 22 hane (Banka kodu ve Hesap numarası benzeri)
        for (int i = 0; i < 22; i++) {
            sb.append(rd.nextInt(10));
        }
        
        return sb.toString();
    }
    public boolean deleteAccount(String iban) {
    // 1. Hem isDefault hem de Balance kontrolü yapıyoruz
    String checkSql = "SELECT isDefault, Balance, AccountID FROM Accounts WHERE IBAN = ?";
    String deleteLogsSql = "DELETE FROM Transactions WHERE SenderAccountID = ? OR ReceiverAccountID = ?";
    String deleteAccSql = "DELETE FROM Accounts WHERE IBAN = ? AND isDefault = 0 AND Balance = 0";
    
    Connection conn = null;
    try {
        conn = DatabaseManager.getConnection();
        conn.setAutoCommit(false);

        int accID = -1;
        try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            psCheck.setString(1, iban);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                // GÜVENLİK KONTROLLERİ
                if (rs.getBoolean("isDefault")) return false; // Varsayılan hesap silinemez
                if (rs.getDouble("Balance") > 0) return false; // İÇİNDE PARA VARSA SİLEMEZ
                
                accID = rs.getInt("AccountID");
            }
        }

        if (accID != -1) {
            // Önce Transaction geçmişini temizle
            try (PreparedStatement psLogs = conn.prepareStatement(deleteLogsSql)) {
                psLogs.setInt(1, accID);
                psLogs.setInt(2, accID);
                psLogs.executeUpdate();
            }

            // Sonra hesabı sil (Balance = 0 şartı burada da var)
            try (PreparedStatement psDel = conn.prepareStatement(deleteAccSql)) {
                psDel.setString(1, iban);
                int affected = psDel.executeUpdate();
                
                conn.commit();
                return affected > 0;
            }
        }
        return false;
    } catch (SQLException e) {
        if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
        e.printStackTrace();
        return false;
    }
}
    public ResultSet getTransactionHistory(int accountID) {
    // Sorguya t.ReceiverAccountID ve t.SenderAccountID ekleyerek kimin gönderdiğini netleştiriyoruz
    String sql = "SELECT t.TransactionType, t.Amount, t.TransactionDate, t.SenderAccountID, t.ReceiverAccountID, " +
                 "s.IBAN as SenderIBAN, r.IBAN as ReceiverIBAN " +
                 "FROM Transactions t " +
                 "LEFT JOIN Accounts s ON t.SenderAccountID = s.AccountID " +
                 "LEFT JOIN Accounts r ON t.ReceiverAccountID = r.AccountID " +
                 "WHERE t.SenderAccountID = ? OR t.ReceiverAccountID = ? " +
                 "ORDER BY t.TransactionDate DESC";
    try {
        Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, accountID);
        pstmt.setInt(2, accountID);
        return pstmt.executeQuery();
    } catch (SQLException e) {
        e.printStackTrace();
        return null;
    }
}
}
