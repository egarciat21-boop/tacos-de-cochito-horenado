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
public class Inventarios {
    private int Id_inventario;
    private int Id_producto;
    private int cantidad;

    /**
     * @return the Id_inventario
     */
    public int getId_inventario() {
        return Id_inventario;
    }

    /**
     * @param Id_inventario the Id_inventario to set
     */
    public void setId_inventario(int Id_inventario) {
        this.Id_inventario = Id_inventario;
    }

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
     * @return the cantidad
     */
    public int getCantidad() {
        return cantidad;
    }

    /**
     * @param cantidad the cantidad to set
     */
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
    
    public boolean Generar(){
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
            st.execute("insert into inventario (id_inventario, id_producto, cantidad) values ('"+getId_inventario()+"', '"+getId_producto()+"', '"+ getCantidad()+"')");
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
