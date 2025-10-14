/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clases;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Inventarios {

    // Inserta y devuelve el id_inventario generado (o null si falla)
    public static Integer insertarYDevolverId(int idProducto, int cantidad) {
        final String sql = "INSERT INTO inventario_simple (id_producto, cantidad) VALUES (?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idProducto);
            ps.setInt(2, cantidad);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error insertando inventario: " + e.getMessage());
            return null;
        }
    }

    public static boolean actualizarInventario(int idInventario, int idProducto, int cantidad) {
        final String sql = "UPDATE inventario_simple SET id_producto=?, cantidad=? WHERE id_inventario=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, cantidad);
            ps.setInt(3, idInventario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizando inventario: " + e.getMessage());
            return false;
        }
    }

    public static boolean eliminarInventario(int idInventario) {
        final String sql = "DELETE FROM inventario_simple WHERE id_inventario=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInventario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error eliminando inventario: " + e.getMessage());
            return false;
        }
    }

    public static List<Object[]> listarInventario(String filtro) {
        List<Object[]> data = new ArrayList<>();
        String base = "SELECT id_inventario, id_producto, cantidad, fecha FROM inventario_simple";
        String sql;
        boolean conFiltro = filtro != null && !filtro.trim().isEmpty() && filtro.matches("\\d+");

        if (conFiltro) {
            sql = base + " WHERE id_inventario = ? OR id_producto = ? ORDER BY id_inventario DESC";
        } else {
            sql = base + " ORDER BY id_inventario DESC";
        }

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (conFiltro) {
                int n = Integer.parseInt(filtro.trim());
                ps.setInt(1, n);
                ps.setInt(2, n);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.add(new Object[]{
                        rs.getInt("id_inventario"),
                        rs.getInt("id_producto"),
                        rs.getInt("cantidad"),
                        rs.getTimestamp("fecha")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listando inventario: " + e.getMessage());
        }
        return data;
    }
}