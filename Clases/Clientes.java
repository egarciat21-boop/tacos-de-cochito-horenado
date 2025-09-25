/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Yefri Chay
 */
public class Clientes {
    
    private int Id_cliente;
    private String nombre;
    private String telefono;
    private String direccion;
    private String nit;
    private String email;
    private String tipo_cliente;
    private float saldo_pendiente;

    /**
     * @return the Id_cliente
     */
    public int getId_cliente() {
        return Id_cliente;
    }

    /**
     * @param Id_cliente the Id_cliente to set
     */
    public void setId_cliente(int Id_cliente) {
        this.Id_cliente = Id_cliente;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the telefono
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * @param telefono the telefono to set
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * @return the direccion
     */
    public String getDireccion() {
        return direccion;
    }

    /**
     * @param direccion the direccion to set
     */
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    /**
     * @return the nit
     */
    public String getNit() {
        return nit;
    }

    /**
     * @param nit the nit to set
     */
    public void setNit(String nit) {
        this.nit = nit;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the tipo_cliente
     */
    public String getTipo_cliente() {
        return tipo_cliente;
    }

    /**
     * @param tipo_cliente the tipo_cliente to set
     */
    public void setTipo_cliente(String tipo_cliente) {
        this.tipo_cliente = tipo_cliente;
    }

    /**
     * @return the saldo_pendiente
     */
    public float getSaldo_pendiente() {
        return saldo_pendiente;
    }

    /**
     * @param saldo_pendiente the saldo_pendiente to set
     */
    public void setSaldo_pendiente(float saldo_pendiente) {
        this.saldo_pendiente = saldo_pendiente;
    }
    
    public boolean Guardar(){
        //conectar BD
        Connection _conexion = null;
        try{ // almacenar la conexion a BD (direecion a donde se conecta)
            String conexionString = "jdbc:mysql://localhost/crm?characterEncoding=latin1";
            // nombre del driver a usar
            String driverName = "com.mysql.cj.jdbc.Driver";
            //crear instancia para el driver
            Class.forName(driverName).newInstance();
            _conexion = DriverManager.getConnection(conexionString,"root","umG25");
            _conexion.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            // insertar campos
            Statement st = _conexion.createStatement();
            st.execute("insert into cliente (nombre, telefono, direccion, nit, email, saldo_pendiente, tipo_cliente) values ('"+getNombre()+"', '"+getTelefono()+"', '"+ getDireccion()+"', '"+ getNit()+"', '"+ getEmail()+"', '"+ getSaldo_pendiente()+"', '"+ getTipo_cliente()+ "')");
//            System.out.println("Conexion Exitosa!!!");
            return true;
        }
        catch (Exception ex){
            System.out.println("Error:" + ex.getMessage());
            return false;
        } 
        // cerrar conexion
        finally{
            try{
                _conexion.close();
            }
            catch (Exception ex2){
            }
            
        }
    
    }   
}
