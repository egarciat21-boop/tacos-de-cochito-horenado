/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static Connection conexion;

    private static final String DB_HOST = getEnvOrDefault("DB_HOST", "localhost");
    private static final String DB_PORT = getEnvOrDefault("DB_PORT", "3306");
    private static final String DB_NAME = getEnvOrDefault("DB_NAME", "proyecto"); // o "proyecto"
    private static final String DB_USER = getEnvOrDefault("DB_USER", "root");
    private static final String DB_PASS = getEnvOrDefault("DB_PASS", "100110"); // valida tu pass real
    private static final String PARAMS = "useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8";
    private static final String URL = String.format("jdbc:mysql://%s:%s/%s?%s", DB_HOST, DB_PORT, DB_NAME, PARAMS);

    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexion = DriverManager.getConnection(URL, DB_USER, DB_PASS);
                System.out.println("Conectado a: " + URL);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
        return conexion;
    }

    private static String getEnvOrDefault(String key, String def) {
        String val = System.getenv(key);
        return (val == null || val.isEmpty()) ? def : val;
    }
}