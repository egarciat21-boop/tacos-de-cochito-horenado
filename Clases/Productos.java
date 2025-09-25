/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 *
 * @author Yefri Chay
 */
public class Productos {
    private int Id_producto;
    private int stock;
    private String nombre;
    private String talla;
    private String estado;
    private String categoria;
    private double precio;
    

    /**
     * @return the Id_producto
     */
    public int getId_producto() {
        return Id_producto;
    }

    /**
     * @param Id_producto the Id_producto to set
     */
    public void setId_producto(int Id_producto) {
        this.Id_producto = Id_producto;
    }

    /**
     * @return the stock
     */
    public int getStock() {
        return stock;
    }

    /**
     * @param stock the stock to set
     */
    public void setStock(int stock) {
        this.stock = stock;
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
     * @return the talla
     */
    public String getTalla() {
        return talla;
    }

    /**
     * @param talla the talla to set
     */
    public void setTalla(String talla) {
        this.talla = talla;
    }

    /**
     * @return the estado
     */
    public String getEstado() {
        return estado;
    }

    /**
     * @param estado the estado to set
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * @return the categoria
     */
    public String getCategoria() {
        return categoria;
    }

    /**
     * @param categoria the categoria to set
     */
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    /**
     * @return the precio
     */
    public double getPrecio() {
        return precio;
    }

    /**
     * @param precio the precio to set
     */
    public void setPrecio(double precio) {
        this.precio = precio;
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
            st.execute("insert into producto (nombre, talla, precio, stock, estado, categoria) values ('"+getNombre()+"', '"+getTalla()+"', '"+ getPrecio()+"', '"+ getStock()+"', '"+ getEstado()+"', '"+ getCategoria()+ "')");
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
