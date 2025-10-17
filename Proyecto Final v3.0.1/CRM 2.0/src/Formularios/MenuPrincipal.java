/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Formularios;

import tu.paquete.ui.CatProductos;

import javax.swing.*;

public class MenuPrincipal extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MenuPrincipal.class.getName());

    public MenuPrincipal() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1Catologos = new javax.swing.JMenu();
        jMenuItem2Clientes = new javax.swing.JMenuItem();
        jMenuItem1Productos = new javax.swing.JMenuItem();
        jMenuItem4Empleados = new javax.swing.JMenuItem();
        jMenuItem3Inventarios = new javax.swing.JMenuItem();
        jMenu2Operaciones = new javax.swing.JMenu();
        jMenuItem5Facturas = new javax.swing.JMenuItem();
        jMenuItem8Ventas = new javax.swing.JMenuItem();
        jMenu3Consultas = new javax.swing.JMenu();
        jMenuItem6ConsuVentas = new javax.swing.JMenuItem();
        jMenuItem7ConsuInventa = new javax.swing.JMenuItem();
        jMenuItem9ConsuFactu = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("CRM - GRUPO #5");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 3, 48)); // NOI18N
        jLabel1.setText("CRM");

        jMenu1Catologos.setText("Catalogos");

        jMenuItem2Clientes.setText("Clientes");
        jMenuItem2Clientes.addActionListener(evt -> jMenuItem2ClientesActionPerformed(evt));
        jMenu1Catologos.add(jMenuItem2Clientes);

        jMenuItem1Productos.setText("Productos");
        jMenuItem1Productos.addActionListener(evt -> jMenuItem1ProductosActionPerformed(evt));
        jMenu1Catologos.add(jMenuItem1Productos);

        jMenuItem4Empleados.setText("Empleados");
        jMenuItem4Empleados.addActionListener(evt -> jMenuItem4EmpleadosActionPerformed(evt));
        jMenu1Catologos.add(jMenuItem4Empleados);

        jMenuItem3Inventarios.setText("Inventarios");
        jMenuItem3Inventarios.addActionListener(evt -> jMenuItem3InventariosActionPerformed(evt));
        jMenu1Catologos.add(jMenuItem3Inventarios);

        jMenuBar1.add(jMenu1Catologos);

        jMenu2Operaciones.setText("Operaciones");

        jMenuItem5Facturas.setText("Facturas");
        jMenuItem5Facturas.addActionListener(evt -> jMenuItem5FacturasActionPerformed(evt));
        jMenu2Operaciones.add(jMenuItem5Facturas);

        jMenuItem8Ventas.setText("Ventas");
        jMenuItem8Ventas.addActionListener(evt -> jMenuItem8VentasActionPerformed(evt));
        jMenu2Operaciones.add(jMenuItem8Ventas);

        jMenuBar1.add(jMenu2Operaciones);

        jMenu3Consultas.setText("Consultas");

        jMenuItem6ConsuVentas.setText("Ventas");
        jMenuItem6ConsuVentas.addActionListener(evt -> jMenuItem6ConsuVentasActionPerformed(evt));
        jMenu3Consultas.add(jMenuItem6ConsuVentas);

        jMenuItem7ConsuInventa.setText("Inventarios");
        jMenuItem7ConsuInventa.addActionListener(evt -> jMenuItem7ConsuInventaActionPerformed(evt));
        jMenu3Consultas.add(jMenuItem7ConsuInventa);

        jMenuItem9ConsuFactu.setText("Facturas");
        jMenuItem9ConsuFactu.addActionListener(evt -> jMenuItem9ConsuFactuActionPerformed(evt));
        jMenu3Consultas.add(jMenuItem9ConsuFactu);

        jMenuBar1.add(jMenu3Consultas);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(148, 148, 148)
                    .addComponent(jLabel1)
                    .addContainerGap(149, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(89, 89, 89)
                    .addComponent(jLabel1)
                    .addContainerGap(124, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName("CRM - GRUPO #1");

        pack();
    }

    private void jMenuItem2ClientesActionPerformed(java.awt.event.ActionEvent evt) {
        TablaClientes tablaClientes = new TablaClientes();
        tablaClientes.setLocationRelativeTo(null);
        tablaClientes.setVisible(true);
    }

    private void jMenuItem1ProductosActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            java.sql.Connection cn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/proyecto?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8",
                "root",
                "100110"
            );
            CatProductos p = new CatProductos(cn);
            p.setLocationRelativeTo(null);
            p.setVisible(true);
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
        }
    }

    private void jMenuItem4EmpleadosActionPerformed(java.awt.event.ActionEvent evt) {
        TablaEmpleados e = new TablaEmpleados();
        e.setLocationRelativeTo(null);
        e.setVisible(true);
    }

    // HISTORIAL DE FACTURAS
    private void jMenuItem5FacturasActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            java.sql.Connection cn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/proyecto?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8",
                "root",
                "100110"
            );
            OpeFacturas frm = new OpeFacturas(cn);
            frm.setLocationRelativeTo(null);
            frm.setVisible(true);
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
        }
    }

    private void jMenuItem6ConsuVentasActionPerformed(java.awt.event.ActionEvent evt) {
        ConVentas cv = new ConVentas();
        cv.setLocationRelativeTo(null);
        cv.setVisible(true);
    }

    private void jMenuItem7ConsuInventaActionPerformed(java.awt.event.ActionEvent evt) {
        ConInventarios ci = new ConInventarios();
        ci.setLocationRelativeTo(null);
        ci.setVisible(true);
    }

    private void jMenuItem3InventariosActionPerformed(java.awt.event.ActionEvent evt) {
        CatInventarios i = new CatInventarios();
        i.setLocationRelativeTo(null);
        i.setVisible(true);
    }

    private void jMenuItem8VentasActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            java.sql.Connection cn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/proyecto?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8",
                "root",
                "100110"
            );
            OpeVentas v = new OpeVentas(cn);
            v.setLocationRelativeTo(null);
            v.setVisible(true);
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
        }
    }

    private void jMenuItem9ConsuFactuActionPerformed(java.awt.event.ActionEvent evt) {
        ConFacturas cf = new ConFacturas();
        cf.setLocationRelativeTo(null);
        cf.setVisible(true);
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        MenuPrincipal mp = new MenuPrincipal();
        mp.setExtendedState(MAXIMIZED_BOTH);
        mp.setLocationRelativeTo(null);
        mp.setVisible(true);
    }

    // Variables declaration - do not modify
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1Catologos;
    private javax.swing.JMenu jMenu2Operaciones;
    private javax.swing.JMenu jMenu3Consultas;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1Productos;
    private javax.swing.JMenuItem jMenuItem2Clientes;
    private javax.swing.JMenuItem jMenuItem3Inventarios;
    private javax.swing.JMenuItem jMenuItem4Empleados;
    private javax.swing.JMenuItem jMenuItem5Facturas;
    private javax.swing.JMenuItem jMenuItem6ConsuVentas;
    private javax.swing.JMenuItem jMenuItem7ConsuInventa;
    private javax.swing.JMenuItem jMenuItem8Ventas;
    private javax.swing.JMenuItem jMenuItem9ConsuFactu;
    // End of variables declaration
}