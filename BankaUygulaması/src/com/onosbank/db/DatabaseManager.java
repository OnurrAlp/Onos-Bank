package com.onosbank.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    
    // Bağlantı bilgilerini buraya giriyoruz
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=OnosBankDB;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa"; // SSMS kullanıcı adın
    private static final String PASSWORD = "orhanonur555666"; // SSMS şifren

    private static Connection connection = null;

    // Singleton Yapısı: Bağlantı yoksa açar, varsa mevcut olanı döndürür
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Sürücü sınıfını yükle
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Onos Bank Veritabanına Başarıyla Bağlanıldı!");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Bağlantı Hatası: " + e.getMessage());
        }
        return connection;
    }
}