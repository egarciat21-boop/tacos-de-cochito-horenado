/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Productos {
    private int Id_producto;
    private int stock;
    private String nombre;
    private String talla;
    private String estado;
    private String categoria;
    private double precio;

    public int getId_producto() { return Id_producto; }
    public void setId_producto(int Id_producto) { this.Id_producto = Id_producto; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public boolean Guardar() {
        final String sql = "INSERT INTO producto (nombre, talla, precio, stock, estado, categoria) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, getNombre());
            ps.setString(2, getTalla());
            ps.setDouble(3, getPrecio());
            ps.setInt(4, getStock());
            ps.setString(5, getEstado());
            ps.setString(6, getCategoria());

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al guardar producto: " + e.getMessage());
            return false;
        }
    }
}