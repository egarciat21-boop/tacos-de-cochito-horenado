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
public class Empleados {
    private int Id_empleado;
    private String nombre;
    private String telefono;
    private String direccion;
    private String email;
    private String puesto;
    private String jornada;
    private float salario;

    /**
     * @return the Id_empleado
     */
    public int getId_empleado() {
        return Id_empleado;
    }

    /**
     * @param Id_empleado the Id_empleado to set
     */
    public void setId_empleado(int Id_empleado) {
        this.Id_empleado = Id_empleado;
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
     * @return the puesto
     */
    public String getPuesto() {
        return puesto;
    }

    /**
     * @param puesto the puesto to set
     */
    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    /**
     * @return the jornada
     */
    public String getJornada() {
        return jornada;
    }

    /**
     * @param jornada the jornada to set
     */
    public void setJornada(String jornada) {
        this.jornada = jornada;
    }

    /**
     * @return the salario
     */
    public float getSalario() {
        return salario;
    }

    /**
     * @param salario the salario to set
     */
    public void setSalario(float salario) {
        this.salario = salario;
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
            st.execute("insert into empleado (nombre, telefono, direccion, salario, email, puesto, jornada) values ('"+getNombre()+"', '"+getTelefono()+"', '"+ getDireccion()+"', '"+ getSalario()+"', '"+ getEmail()+"', '"+ getPuesto()+"', '"+ getJornada()+ "')");
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
