/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Formularios;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/* Conexión a MySQL (cambia 'proyecto' si tu base se llama diferente) */
class Conexion {
    private static final String URL  = "jdbc:mysql://localhost:3306/proyecto?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "100110";
    static {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { System.err.println("Driver MySQL no encontrado: " + e.getMessage()); }
    }
    public static Connection getConexion() throws SQLException { return DriverManager.getConnection(URL, USER, PASS); }
}

public class CatInventarios extends JFrame {

    // Campos de formulario
    private JTextField txtIdProducto, txtCantidad, txtDescripcion;

    // Tabla visible con 4 columnas
    private JTable tabla;
    private DefaultTableModel modelo;

    // Mantenemos el id_inventario del registro seleccionado (no visible)
    private Integer idInventarioSeleccionado = null;

    // Botones
    private JButton btnElegir, btnAgregar, btnMostrar, btnEditar, btnEliminar;

    public CatInventarios() {
        setTitle("Inventario (inventario_simple)");
        setSize(840, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        construirUI();
        wireEvents();

        cargarTabla();
        actualizarBotones();
    }

    private void construirUI() {
        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        c.gridx = 0; c.gridy = row; form.add(new JLabel("ID Producto:"), c);
        txtIdProducto = new JTextField(); txtIdProducto.setEditable(false);
        c.gridx = 1; c.weightx = 1.0; form.add(txtIdProducto, c); c.weightx = 0;
        btnElegir = new JButton("Elegir...");
        c.gridx = 2; form.add(btnElegir, c); row++;

        c.gridx = 0; c.gridy = row; form.add(new JLabel("Cantidad:"), c);
        txtCantidad = new JTextField();
        c.gridx = 1; c.gridwidth = 2; form.add(txtCantidad, c); c.gridwidth = 1; row++;

        c.gridx = 0; c.gridy = row; form.add(new JLabel("Descripción (opcional):"), c);
        txtDescripcion = new JTextField();
        c.gridx = 1; c.gridwidth = 2; form.add(txtDescripcion, c); c.gridwidth = 1; row++;

        add(form, BorderLayout.NORTH);

        // Tabla (4 columnas visibles) + id_inventario guardado internamente
        // Mostramos: ID Producto, Cantidad, Descripción, Fecha
        modelo = new DefaultTableModel(new Object[]{"ID Producto", "Cantidad", "Descripción", "Fecha"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0, 1 -> Integer.class;
                    case 3 -> java.sql.Timestamp.class;
                    default -> String.class;
                };
            }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Acciones
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnAgregar = new JButton("Agregar");
        btnMostrar = new JButton("Mostrar");
        btnEditar  = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        acciones.add(btnAgregar); acciones.add(btnMostrar);
        acciones.add(btnEditar);  acciones.add(btnEliminar);
        add(acciones, BorderLayout.SOUTH);
    }

    private void wireEvents() {
        btnElegir.addActionListener(e -> abrirSelectorProductos());
        btnAgregar.addActionListener(e -> agregar());
        btnMostrar.addActionListener(e -> cargarTabla());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onFilaSeleccionada();
        });
    }

    private void onFilaSeleccionada() {
        int row = tabla.getSelectedRow();
        if (row < 0) {
            idInventarioSeleccionado = null;
            actualizarBotones();
            return;
        }
        // Carga de campos desde la fila
        txtIdProducto.setText(String.valueOf(modelo.getValueAt(row, 0)));
        txtCantidad.setText(String.valueOf(modelo.getValueAt(row, 1)));
        Object d = modelo.getValueAt(row, 2);
        txtDescripcion.setText(d == null ? "" : String.valueOf(d));

        // Recuperar el id_inventario del registro seleccionado consultándolo por los valores visibles + fecha
        idInventarioSeleccionado = obtenerIdInventarioPorFila(row);
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean sel = idInventarioSeleccionado != null;
        btnEditar.setEnabled(sel);
        btnEliminar.setEnabled(sel);
    }

    // ============ CRUD ============
    private void agregar() {
        Integer idProd = leerEntero(txtIdProducto, "ID Producto"); if (idProd == null || idProd <= 0) return;
        Integer cant   = leerEntero(txtCantidad, "Cantidad");      if (cant == null) return;
        String desc = txtDescripcion.getText().trim(); if (desc.isEmpty()) desc = null;

        final String sql = "INSERT INTO inventario_simple (id_producto, cantidad, descripcion) VALUES (?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProd);
            ps.setInt(2, cant);
            if (desc == null) ps.setNull(3, Types.VARCHAR); else ps.setString(3, desc);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Agregado.");
            limpiarCampos();
            cargarTabla();
        } catch (SQLException e) {
            manejarSqlException("agregar", e);
        }
    }

    private void editar() {
        if (idInventarioSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un registro de la tabla.");
            return;
        }
        Integer idProd = leerEntero(txtIdProducto, "ID Producto"); if (idProd == null || idProd <= 0) return;
        Integer cant   = leerEntero(txtCantidad, "Cantidad");      if (cant == null) return;
        String desc = txtDescripcion.getText().trim(); if (desc.isEmpty()) desc = null;

        final String sql = "UPDATE inventario_simple SET id_producto=?, cantidad=?, descripcion=? WHERE id_inventario=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProd);
            ps.setInt(2, cant);
            if (desc == null) ps.setNull(3, Types.VARCHAR); else ps.setString(3, desc);
            ps.setInt(4, idInventarioSeleccionado);
            int n = ps.executeUpdate();
            if (n > 0) {
                JOptionPane.showMessageDialog(this, "Actualizado.");
                cargarTabla();
                // Re-seleccionar el registro editado
                idInventarioSeleccionado = null;
                actualizarBotones();
                limpiarCampos();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el registro para actualizar.");
            }
        } catch (SQLException e) {
            manejarSqlException("actualizar", e);
        }
    }

    private void eliminar() {
        if (idInventarioSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un registro de la tabla.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar el registro seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;

        final String sql = "DELETE FROM inventario_simple WHERE id_inventario=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInventarioSeleccionado);
            int n = ps.executeUpdate();
            if (n > 0) {
                JOptionPane.showMessageDialog(this, "Eliminado.");
                cargarTabla();
                idInventarioSeleccionado = null;
                actualizarBotones();
                limpiarCampos();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el registro para eliminar.");
            }
        } catch (SQLException e) {
            manejarSqlException("eliminar", e);
        }
    }

    // ============ Data ============
    private void cargarTabla() {
        modelo.setRowCount(0);
        for (Object[] r : listar()) modelo.addRow(r);
        idInventarioSeleccionado = null;
        actualizarBotones();
    }

    private List<Object[]> listar() {
        List<Object[]> data = new ArrayList<>();
        final String sql = "SELECT id_inventario, id_producto, cantidad, descripcion, fecha " +
                           "FROM inventario_simple ORDER BY id_inventario DESC";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // Guardamos solo las columnas visibles en la tabla; el id_inventario lo usamos aparte
                data.add(new Object[]{
                        rs.getInt("id_producto"),
                        rs.getInt("cantidad"),
                        rs.getString("descripcion"),
                        rs.getTimestamp("fecha")
                });
                // Truco: no ponemos id_inventario en el modelo, pero lo recuperaremos fiable por (id_producto,cantidad,descripcion,fecha)
                // También podríamos usar un Map fila->id_inventario si lo prefieres; esta versión evita columnas ocultas.
            }
        } catch (SQLException e) {
            manejarSqlException("mostrar", e);
        }
        return data;
    }

    // Busca id_inventario exacto de la fila seleccionada usando sus valores y la fecha
    private Integer obtenerIdInventarioPorFila(int row) {
        int idProd = (Integer) modelo.getValueAt(row, 0);
        int cant   = (Integer) modelo.getValueAt(row, 1);
        String desc = (String) modelo.getValueAt(row, 2);
        Timestamp fecha = (Timestamp) modelo.getValueAt(row, 3);

        final String sql = "SELECT id_inventario FROM inventario_simple " +
                "WHERE id_producto=? AND cantidad=? AND ((descripcion IS NULL AND ? IS NULL) OR descripcion=?) AND fecha=? " +
                "ORDER BY id_inventario DESC LIMIT 1";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProd);
            ps.setInt(2, cant);
            if (desc == null) ps.setNull(3, Types.VARCHAR); else ps.setString(3, desc);
            if (desc == null) ps.setNull(4, Types.VARCHAR); else ps.setString(4, desc);
            ps.setTimestamp(5, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo id_inventario: " + e.getMessage());
        }
        return null;
    }

    // ============ Utilidades ============
    private Integer leerEntero(JTextField campo, String nombre) {
        String s = campo.getText().trim();
        if (s.isEmpty()) { JOptionPane.showMessageDialog(this, nombre + " es obligatorio."); return null; }
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, nombre + " inválido."); return null; }
    }

    private void limpiarCampos() {
        txtIdProducto.setText("");
        txtCantidad.setText("");
        txtDescripcion.setText("");
        tabla.clearSelection();
    }

    private void manejarSqlException(String accion, SQLException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (msg.contains("doesn't exist") && msg.contains("inventario_simple")) {
            JOptionPane.showMessageDialog(this, "La tabla 'inventario_simple' no existe en la base configurada.\n" +
                    "Crea la tabla o cambia la URL de conexión a la base correcta.");
        } else if (msg.contains("foreign key")) {
            JOptionPane.showMessageDialog(this, "El ID de producto no existe en 'producto'. Crea/selecciona un producto válido.");
        } else if (msg.contains("unknown column 'descripcion'")) {
            JOptionPane.showMessageDialog(this, "Tu tabla no tiene la columna 'descripcion'.\n" +
                    "Agrega la columna con:\nALTER TABLE inventario_simple ADD COLUMN descripcion VARCHAR(255) NULL AFTER cantidad;");
        } else {
            JOptionPane.showMessageDialog(this, "Error SQL al " + accion + ": " + e.getMessage());
        }
    }

    // ============ Selector de productos ============
    private void abrirSelectorProductos() {
        ProductPicker p = new ProductPicker(this);
        p.setVisible(true);
        Integer id = p.getIdSeleccionado();
        if (id != null) txtIdProducto.setText(String.valueOf(id));
    }

    private class ProductPicker extends JDialog {
        private JTable tbl; private DefaultTableModel m; private Integer idSeleccionado;
        ProductPicker(Frame owner) {
            super(owner, "Seleccionar producto", true);
            setSize(420, 380); setLocationRelativeTo(owner); setLayout(new BorderLayout(8,8));
            m = new DefaultTableModel(new Object[]{"ID", "Nombre"}, 0) { @Override public boolean isCellEditable(int r,int c){return false;} };
            tbl = new JTable(m); add(new JScrollPane(tbl), BorderLayout.CENTER);
            JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8));
            JButton ok=new JButton("Seleccionar"), cancel=new JButton("Cancelar");
            south.add(ok); south.add(cancel); add(south, BorderLayout.SOUTH);
            ok.addActionListener(e -> {
                int r = tbl.getSelectedRow();
                if (r < 0) { JOptionPane.showMessageDialog(this, "Selecciona un producto."); return; }
                idSeleccionado = (Integer) m.getValueAt(r, 0); dispose();
            });
            cancel.addActionListener(e -> dispose());
            cargarProductos();
        }
        private void cargarProductos() {
            m.setRowCount(0);
            final String sql = "SELECT id_producto, nombre FROM producto ORDER BY id_producto DESC";
            try (Connection con = Conexion.getConexion();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) m.addRow(new Object[]{ rs.getInt(1), rs.getString(2) });
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error SQL al cargar productos: " + e.getMessage());
            }
        }
        Integer getIdSeleccionado() { return idSeleccionado; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignore) {}
            new CatInventarios().setVisible(true);
        });
    }
}