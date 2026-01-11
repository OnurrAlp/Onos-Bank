package com.onosbank.test;

import com.onosbank.db.DatabaseManager;
import java.sql.Connection;

public class DBTest {
    public static void main(String[] args) {
        System.out.println("Bağlantı testi başlatılıyor...");
        
        // DatabaseManager üzerinden bağlantı almayı dene
        Connection conn = DatabaseManager.getConnection();
        
        if (conn != null) {
            System.out.println("TEBRİKLER: SQL Server bağlantısı başarıyla kuruldu!");
        } else {
            System.err.println("HATA: Bağlantı kurulamadı. Lütfen SSMS ayarlarını ve portu kontrol et.");
        }
    }
}