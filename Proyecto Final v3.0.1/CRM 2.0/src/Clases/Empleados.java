/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clases;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Empleados {

    private int id_empleado;
    private String nombre;
    private String telefono;
    private String direccion;
    private String email;
    private String puesto;
    private String jornada;
    private float salario;

    
    public int getId_empleado() { return id_empleado; }
    public void setId_empleado(int id_empleado) { this.id_empleado = id_empleado; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }
    public String getJornada() { return jornada; }
    public void setJornada(String jornada) { this.jornada = jornada; }
    public float getSalario() { return salario; }
    public void setSalario(float salario) { this.salario = salario; }

   
    public boolean guardar() {
        final String sql = "INSERT INTO empleado (nombre, telefono, direccion, salario, email, puesto, jornada) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setString(3, direccion);
            ps.setFloat(4, salario);
            ps.setString(5, email);
            ps.setString(6, puesto);
            ps.setString(7, jornada);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al guardar empleado: " + e.getMessage());
            return false;
        }
    }

   
    public boolean actualizar() {
        final String sql = "UPDATE empleado SET nombre=?, telefono=?, direccion=?, salario=?, email=?, puesto=?, jornada=? WHERE id_empleado=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setString(3, direccion);
            ps.setFloat(4, salario);
            ps.setString(5, email);
            ps.setString(6, puesto);
            ps.setString(7, jornada);
            ps.setInt(8, id_empleado);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar empleado: " + e.getMessage());
            return false;
        }
    }

  
    public static boolean eliminar(int id) {
        final String sql = "DELETE FROM empleado WHERE id_empleado=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar empleado: " + e.getMessage());
            return false;
        }
    }

 
    public static List<Empleados> obtenerTodos() {
        List<Empleados> lista = new ArrayList<>();
        final String sql = "SELECT id_empleado, nombre, telefono, direccion, salario, email, puesto, jornada FROM empleado";
        try (Connection con = Conexion.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Empleados emp = new Empleados();
                emp.setId_empleado(rs.getInt("id_empleado"));
                emp.setNombre(rs.getString("nombre"));
                emp.setTelefono(rs.getString("telefono"));
                emp.setDireccion(rs.getString("direccion"));
                emp.setSalario(rs.getFloat("salario"));
                emp.setEmail(rs.getString("email"));
                emp.setPuesto(rs.getString("puesto"));
                emp.setJornada(rs.getString("jornada"));
                lista.add(emp);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener empleados: " + e.getMessage());
        }
        return lista;
    }
}