/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Formularios;

import Clases.Productos;
import javax.swing.JOptionPane;

/**
 * Catálogo de Productos
 * - Permite ingresar productos y guardarlos en la base de datos.
 * - Botones: Guardar, Limpiar, Cerrar.
 * - Valida campos obligatorios y formatos numéricos.
 *
 * Requisitos:
 * - Clases.Productos con método Guardar() usando Conexion.getConexion() y PreparedStatement.
 * - Clases.Conexion configurada a la BD correcta.
 */
public class CatProductos extends javax.swing.JFrame {

    public CatProductos() {
        initComponents();
        initCustomActions();
        setLocationRelativeTo(null);
    }

    // Acciones personalizadas de los botones
    private void initCustomActions() {
        jButton1GuardarProduc.addActionListener(evt -> guardarProducto());
        jButton2Limpiar.addActionListener(evt -> limpiarCampos());
        jButton3Cerrar.addActionListener(evt -> dispose());
    }

    private void guardarProducto() {
        String nombre = jTextField2Nom.getText().trim();
        String talla = jTextField4Talla.getText().trim();
        String precioStr = jTextField5Precio.getText().trim();
        String stockStr = jTextField1Stock.getText().trim();
        String estado = jTextField2Estado.getText().trim();
        String categoria = jTextField3Categ.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.");
            jTextField2Nom.requestFocus();
            return;
        }
        if (precioStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El precio es obligatorio.");
            jTextField5Precio.requestFocus();
            return;
        }
        if (stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El stock es obligatorio.");
            jTextField1Stock.requestFocus();
            return;
        }

        double precio;
        int stock;
        try {
            precio = Double.parseDouble(precioStr.replace(',', '.'));
            if (precio < 0) {
                JOptionPane.showMessageDialog(this, "El precio no puede ser negativo.");
                jTextField5Precio.requestFocus();
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Precio inválido. Usa formato numérico, ej: 199.99");
            jTextField5Precio.requestFocus();
            return;
        }

        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "El stock no puede ser negativo.");
                jTextField1Stock.requestFocus();
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Stock inválido. Debe ser un número entero.");
            jTextField1Stock.requestFocus();
            return;
        }

        Productos p = new Productos();
        p.setNombre(nombre);
        p.setTalla(talla);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setEstado(estado);
        p.setCategoria(categoria);

        boolean ok = p.Guardar();
        if (ok) {
            JOptionPane.showMessageDialog(this, "Producto guardado correctamente.");
            limpiarCampos();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar. Revisa la conexión y la tabla 'producto'.");
        }
    }

    private void limpiarCampos() {
        jTextField2Nom.setText("");
        jTextField4Talla.setText("");
        jTextField5Precio.setText("");
        jTextField1Stock.setText("");
        jTextField2Estado.setText("");
        jTextField3Categ.setText("");
        jTextField2Nom.requestFocus();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1Productos = new javax.swing.JLabel();
        jLabel3Nombre = new javax.swing.JLabel();
        jTextField2Nom = new javax.swing.JTextField();
        jLabel4Talla = new javax.swing.JLabel();
        jTextField4Talla = new javax.swing.JTextField();
        jLabel5Precio = new javax.swing.JLabel();
        jTextField5Precio = new javax.swing.JTextField();
        jLabel6Stock = new javax.swing.JLabel();
        jTextField1Stock = new javax.swing.JTextField();
        jLabel7Estado = new javax.swing.JLabel();
        jTextField2Estado = new javax.swing.JTextField();
        jLabel8Categoria = new javax.swing.JLabel();
        jTextField3Categ = new javax.swing.JTextField();
        jButton1GuardarProduc = new javax.swing.JButton();
        jButton2Limpiar = new javax.swing.JButton();
        jButton3Cerrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("CATÁLOGO DE PRODUCTOS");

        jLabel1Productos.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1Productos.setText("CATÁLOGO DE PRODUCTOS");

        jLabel3Nombre.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3Nombre.setText("Nombre:");

        jLabel4Talla.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4Talla.setText("Talla:");

        jLabel5Precio.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5Precio.setText("Precio:");

        jLabel6Stock.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6Stock.setText("Stock:");

        jLabel7Estado.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7Estado.setText("Estado:");

        jLabel8Categoria.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8Categoria.setText("Categoria:");

        jButton1GuardarProduc.setBackground(new java.awt.Color(204, 204, 204));
        jButton1GuardarProduc.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1GuardarProduc.setText("Guardar");

        jButton2Limpiar.setBackground(new java.awt.Color(204, 204, 204));
        jButton2Limpiar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2Limpiar.setText("Limpiar");

        jButton3Cerrar.setBackground(new java.awt.Color(204, 204, 204));
        jButton3Cerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton3Cerrar.setText("Cerrar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3Nombre)
                    .addComponent(jLabel4Talla)
                    .addComponent(jLabel5Precio)
                    .addComponent(jLabel6Stock)
                    .addComponent(jLabel7Estado)
                    .addComponent(jLabel8Categoria))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField2Nom)
                    .addComponent(jTextField4Talla)
                    .addComponent(jTextField5Precio)
                    .addComponent(jTextField1Stock)
                    .addComponent(jTextField2Estado)
                    .addComponent(jTextField3Categ, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addComponent(jButton1GuardarProduc, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton2Limpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton3Cerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addComponent(jLabel1Productos)
                .addGap(30, 30, 30))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel1Productos)
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3Nombre)
                    .addComponent(jTextField2Nom, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4Talla)
                    .addComponent(jTextField4Talla, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5Precio)
                    .addComponent(jTextField5Precio, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6Stock)
                    .addComponent(jTextField1Stock, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7Estado)
                    .addComponent(jTextField2Estado, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8Categoria)
                    .addComponent(jTextField3Categ, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1GuardarProduc, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2Limpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3Cerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1GuardarProduc;
    private javax.swing.JButton jButton2Limpiar;
    private javax.swing.JButton jButton3Cerrar;
    private javax.swing.JLabel jLabel1Productos;
    private javax.swing.JLabel jLabel3Nombre;
    private javax.swing.JLabel jLabel4Talla;
    private javax.swing.JLabel jLabel5Precio;
    private javax.swing.JLabel jLabel6Stock;
    private javax.swing.JLabel jLabel7Estado;
    private javax.swing.JLabel jLabel8Categoria;
    private javax.swing.JTextField jTextField1Stock;
    private javax.swing.JTextField jTextField2Estado;
    private javax.swing.JTextField jTextField2Nom;
    private javax.swing.JTextField jTextField3Categ;
    private javax.swing.JTextField jTextField4Talla;
    private javax.swing.JTextField jTextField5Precio;
    // End of variables declaration//GEN-END:variables
}