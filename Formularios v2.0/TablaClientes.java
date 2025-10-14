package Formularios;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import Clases.Conexion;

public class TablaClientes extends JFrame {

    private JTable tablaClientes;
    private JButton btnMostrar;
    private JButton btnAgregar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private DefaultTableModel modelo;

    public TablaClientes() {
        setTitle("Listado de Clientes");
        setSize(950, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

   
        modelo = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Teléfono", "Dirección", "NIT", "Email", "Saldo", "Tipo"}, 0
        );
        tablaClientes = new JTable(modelo);

    
        tablaClientes.getColumnModel().getColumn(0).setMinWidth(0);
        tablaClientes.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaClientes.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollPane = new JScrollPane(tablaClientes);

       
        btnMostrar = new JButton("Mostrar");
        btnAgregar = new JButton("Agregar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");

        btnMostrar.addActionListener(e -> mostrarClientes());
        btnAgregar.addActionListener(e -> abrirVentanaAgregar());
        btnEditar.addActionListener(e -> abrirVentanaEdicion());
        btnEliminar.addActionListener(e -> eliminarCliente());

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnMostrar);
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);

        add(scrollPane, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

  
    private void mostrarClientes() {
        modelo.setRowCount(0);
        try {
            Conexion conexion = new Conexion();
            Connection conn = conexion.getConexion();

            String sql = "SELECT id_cliente, nombre, telefono, direccion, nit, email, saldo_pendiente, tipo_cliente FROM cliente";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("id_cliente"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("nit"),
                        rs.getString("email"),
                        rs.getDouble("saldo_pendiente"),
                        rs.getString("tipo_cliente")
                });
            }

            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al mostrar clientes: " + e.getMessage());
        }
    }

 
    private void eliminarCliente() {
        int fila = tablaClientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente para eliminar.");
            return;
        }

        int idCliente = (int) modelo.getValueAt(fila, 0);
        String nombre = (String) modelo.getValueAt(fila, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Seguro que deseas eliminar al cliente: " + nombre + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Conexion conexion = new Conexion();
            Connection conn = conexion.getConexion();

            String sql = "DELETE FROM cliente WHERE id_cliente = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idCliente);
            ps.executeUpdate();

            ps.close();
            conn.close();

            JOptionPane.showMessageDialog(this, "Cliente eliminado correctamente.");
            mostrarClientes();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
        }
    }

    
    private void abrirVentanaAgregar() {
        JDialog dialog = new JDialog(this, "Agregar Cliente", true);
        dialog.setLayout(new GridLayout(8, 2, 10, 10));
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JTextField txtNombre = new JTextField();
        JTextField txtTelefono = new JTextField();
        JTextField txtDireccion = new JTextField();
        JTextField txtNIT = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtSaldo = new JTextField();
        JTextField txtTipo = new JTextField();

        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        dialog.add(new JLabel("Nombre:"));
        dialog.add(txtNombre);
        dialog.add(new JLabel("Teléfono:"));
        dialog.add(txtTelefono);
        dialog.add(new JLabel("Dirección:"));
        dialog.add(txtDireccion);
        dialog.add(new JLabel("NIT:"));
        dialog.add(txtNIT);
        dialog.add(new JLabel("Email:"));
        dialog.add(txtEmail);
        dialog.add(new JLabel("Saldo:"));
        dialog.add(txtSaldo);
        dialog.add(new JLabel("Tipo:"));
        dialog.add(txtTipo);
        dialog.add(btnGuardar);
        dialog.add(btnCancelar);

        btnGuardar.addActionListener(e -> {
            if (txtNombre.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "El nombre no puede estar vacío.");
                return;
            }
            try {
                Conexion conexion = new Conexion();
                Connection conn = conexion.getConexion();
                String sql = "INSERT INTO cliente (nombre, telefono, direccion, nit, email, saldo_pendiente, tipo_cliente) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, txtNombre.getText());
                ps.setString(2, txtTelefono.getText());
                ps.setString(3, txtDireccion.getText());
                ps.setString(4, txtNIT.getText());
                ps.setString(5, txtEmail.getText());
                ps.setDouble(6, txtSaldo.getText().isEmpty() ? 0.0 : Double.parseDouble(txtSaldo.getText()));
                ps.setString(7, txtTipo.getText());
                ps.executeUpdate();

                ps.close();
                conn.close();

                JOptionPane.showMessageDialog(dialog, "Cliente agregado correctamente.");
                dialog.dispose();
                mostrarClientes();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al agregar: " + ex.getMessage());
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

   
    private void abrirVentanaEdicion() {
        int fila = tablaClientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente para editar.");
            return;
        }

        int idCliente = (int) modelo.getValueAt(fila, 0);
        String nombre = (String) modelo.getValueAt(fila, 1);
        String telefono = (String) modelo.getValueAt(fila, 2);
        String direccion = (String) modelo.getValueAt(fila, 3);
        String nit = (String) modelo.getValueAt(fila, 4);
        String email = (String) modelo.getValueAt(fila, 5);
        Double saldo = Double.parseDouble(modelo.getValueAt(fila, 6).toString());
        String tipo = (String) modelo.getValueAt(fila, 7);

        JDialog dialog = new JDialog(this, "Editar Cliente", true);
        dialog.setLayout(new GridLayout(8, 2, 10, 10));
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JTextField txtNombre = new JTextField(nombre);
        JTextField txtTelefono = new JTextField(telefono);
        JTextField txtDireccion = new JTextField(direccion);
        JTextField txtNIT = new JTextField(nit);
        JTextField txtEmail = new JTextField(email);
        JTextField txtSaldo = new JTextField(String.valueOf(saldo));
        JTextField txtTipo = new JTextField(tipo);

        JButton btnGuardar = new JButton("Guardar cambios");
        JButton btnCancelar = new JButton("Cancelar");

        dialog.add(new JLabel("Nombre:"));
        dialog.add(txtNombre);
        dialog.add(new JLabel("Teléfono:"));
        dialog.add(txtTelefono);
        dialog.add(new JLabel("Dirección:"));
        dialog.add(txtDireccion);
        dialog.add(new JLabel("NIT:"));
        dialog.add(txtNIT);
        dialog.add(new JLabel("Email:"));
        dialog.add(txtEmail);
        dialog.add(new JLabel("Saldo:"));
        dialog.add(txtSaldo);
        dialog.add(new JLabel("Tipo:"));
        dialog.add(txtTipo);
        dialog.add(btnGuardar);
        dialog.add(btnCancelar);

        btnGuardar.addActionListener(e -> {
            try {
                Conexion conexion = new Conexion();
                Connection conn = conexion.getConexion();
                String sql = "UPDATE cliente SET nombre=?, telefono=?, direccion=?, nit=?, email=?, saldo_pendiente=?, tipo_cliente=? WHERE id_cliente=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, txtNombre.getText());
                ps.setString(2, txtTelefono.getText());
                ps.setString(3, txtDireccion.getText());
                ps.setString(4, txtNIT.getText());
                ps.setString(5, txtEmail.getText());
                ps.setDouble(6, Double.parseDouble(txtSaldo.getText()));
                ps.setString(7, txtTipo.getText());
                ps.setInt(8, idCliente);
                ps.executeUpdate();

                ps.close();
                conn.close();

                JOptionPane.showMessageDialog(dialog, "Cliente actualizado correctamente.");
                dialog.dispose();
                mostrarClientes();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al guardar cambios: " + ex.getMessage());
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new TablaClientes().setVisible(true));
    }
}
