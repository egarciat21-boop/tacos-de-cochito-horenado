/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package tu.paquete.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

public class CatProductos extends JFrame {

    private JTable tblProductos;
    private DefaultTableModel modelo;
    private JTextField txtBuscar;
    private JComboBox<String> cboGenero;    // Mapea a producto.categoria
    private JComboBox<String> cboTipo;      // Mapea a producto.producto_categoria
    private JCheckBox chkConsolidar;        // “Consolidar iguales”
    private JButton btnNuevo, btnEditar, btnEliminar, btnGuardar, btnCancelar, btnMovimientos, btnRefrescar;
    private JPanel filtrosPanel;

    private final Connection conn;
    private Integer productoSeleccionadoId = null;

    public CatProductos(Connection connection) {
        this.conn = connection;
        setTitle("Catálogo de Productos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(980, 640);
        setLocationRelativeTo(null);

        initUI();
        initEvents();

        // Cargar combos
        cargarGeneros();
        actualizarTiposPorGenero();

        // Carga inicial
        recargarTablaSegunCheckbox();
    }

    private void initUI() {
        filtrosPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.WEST;

        filtrosPanel.add(new JLabel("Buscar:"), gc);
        gc.gridx++;
        txtBuscar = new JTextField(18);
        filtrosPanel.add(txtBuscar, gc);

        gc.gridx++;
        filtrosPanel.add(new JLabel("Género:"), gc);
        gc.gridx++;
        cboGenero = new JComboBox<>();
        filtrosPanel.add(cboGenero, gc);

        gc.gridx++;
        filtrosPanel.add(new JLabel("Tipo:"), gc);
        gc.gridx++;
        cboTipo = new JComboBox<>();
        filtrosPanel.add(cboTipo, gc);

        gc.gridx++;
        chkConsolidar = new JCheckBox("Consolidar iguales");
        chkConsolidar.setSelected(true); // por defecto consolidado
        filtrosPanel.add(chkConsolidar, gc);

        modelo = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Género", "Tipo", "Stock", "Precio"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblProductos = new JTable(modelo);
        JScrollPane sp = new JScrollPane(tblProductos);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNuevo = new JButton("Nuevo");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnGuardar = new JButton("Guardar");
        btnCancelar = new JButton("Cancelar");
        btnMovimientos = new JButton("Movimientos");
        btnRefrescar = new JButton("Refrescar");

        acciones.add(btnMovimientos);
        acciones.add(btnRefrescar);
        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnEliminar);
        acciones.add(btnGuardar);
        acciones.add(btnCancelar);

        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().add(filtrosPanel, BorderLayout.NORTH);
        getContentPane().add(sp, BorderLayout.CENTER);
        getContentPane().add(acciones, BorderLayout.SOUTH);
    }

    private void initEvents() {
        txtBuscar.addActionListener(e -> recargarTablaSegunCheckbox());
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                // recargarTablaSegunCheckbox(); // activa si quieres buscar en vivo
            }
        });

        cboGenero.addActionListener(e -> {
            actualizarTiposPorGenero();
            recargarTablaSegunCheckbox();
        });

        cboTipo.addActionListener(e -> recargarTablaSegunCheckbox());

        chkConsolidar.addActionListener(e -> recargarTablaSegunCheckbox());
        btnRefrescar.addActionListener(e -> recargarTablaSegunCheckbox());

        tblProductos.getSelectionModel().addListSelectionListener(e -> {
            int row = tblProductos.getSelectedRow();
            if (!e.getValueIsAdjusting() && row >= 0) {
                Object idObj = modelo.getValueAt(row, 0);
                productoSeleccionadoId = (idObj instanceof Integer)
                        ? (Integer) idObj
                        : parseIntOrNull(Objects.toString(idObj, null));
            }
        });

        btnNuevo.addActionListener(e -> onNuevo());
        btnEditar.addActionListener(e -> onEditar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnGuardar.addActionListener(e -> onGuardar());
        btnCancelar.addActionListener(e -> onCancelar());

        btnMovimientos.addActionListener(e -> onMovimientos());
    }

    private Integer parseIntOrNull(String s) {
        try { return s == null ? null : Integer.parseInt(s); }
        catch (Exception ex) { return null; }
    }

    // Combos
    private void cargarGeneros() {
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
        m.addElement("Todos");
        m.addElement("HOMBRE");
        m.addElement("MUJER");
        m.addElement("NIÑO");
        m.addElement("NIÑA");
        cboGenero.setModel(m);
        cboGenero.setSelectedIndex(0);
    }

    private void actualizarTiposPorGenero() {
        String genero = (String) cboGenero.getSelectedItem();
        List<String> tipos = tiposPorGenero(genero);
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
        m.addElement("Todos");
        for (String t : tipos) m.addElement(t);
        cboTipo.setModel(m);
        cboTipo.setSelectedIndex(0);
    }

    private List<String> tiposPorGenero(String genero) {
        List<String> base = new ArrayList<>();
        base.add("SUÉTERES");
        base.add("PANTALONES");
        base.add("PLAYERAS");
        base.add("CAMISAS");
        return base;
    }

    private void recargarTablaSegunCheckbox() {
        if (chkConsolidar.isSelected()) {
            cargarTablaConsolidada(); // Solo vista
        } else {
            cargarTablaPorId();    // Gestión por id_producto
        }
    }

    // Opción 1: gestión por id_producto
    private void cargarTablaPorId() {
        limpiarTabla();
        String filtro = "%" + txtBuscar.getText().trim() + "%";
        String genero = valOrNullStr((String) cboGenero.getSelectedItem());
        String tipo = valOrNullStr((String) cboTipo.getSelectedItem());

        String sql =
            "SELECT p.id_producto, p.nombre, p.categoria AS genero, p.producto_categoria AS tipo, " +
            "    COALESCE(SUM(CASE WHEN i.tipo='ENTRADA' THEN i.cantidad " +
            "    WHEN i.tipo='SALIDA'  THEN -i.cantidad " +
            "    ELSE 0 END), 0) AS stock, " +
            "    p.precio " +
            "FROM producto p " +
            "LEFT JOIN inventario i ON i.id_producto = p.id_producto " +
            "WHERE (? IS NULL OR p.nombre LIKE ?) " +
            "  AND (? IS NULL OR p.categoria = ?) " +
            "  AND (? IS NULL OR p.producto_categoria = ?) " +
            "GROUP BY p.id_producto, p.nombre, p.categoria, p.producto_categoria, p.precio " +
            "ORDER BY p.id_producto DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (isBlank(txtBuscar.getText())) {
                ps.setNull(1, Types.VARCHAR);
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(1, filtro);
                ps.setString(2, filtro);
            }
            if (genero == null || genero.equalsIgnoreCase("Todos")) {
                ps.setNull(3, Types.VARCHAR);
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(3, genero);
                ps.setString(4, genero);
            }
            if (tipo == null || tipo.equalsIgnoreCase("Todos")) {
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
            } else {
                ps.setString(5, tipo);
                ps.setString(6, tipo);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[]{
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getString("genero"),
                        rs.getString("tipo"),
                        rs.getInt("stock"),
                        rs.getBigDecimal("precio")
                    };
                    modelo.addRow(fila);
                }
            }
        } catch (SQLException ex) {
            mostrarError("Error al cargar productos (Opción 1)", ex);
        }
    }

    // Opción 2: vista consolidada (por nombre + genero + tipo)
    private void cargarTablaConsolidada() {
        limpiarTabla();
        String filtro = "%" + txtBuscar.getText().trim() + "%";
        String genero = valOrNullStr((String) cboGenero.getSelectedItem());
        String tipo = valOrNullStr((String) cboTipo.getSelectedItem());

        String sql =
            "SELECT NULL AS id_producto, p.nombre, p.categoria AS genero, p.producto_categoria AS tipo, " +
            "    COALESCE(SUM(CASE WHEN i.tipo='ENTRADA' THEN i.cantidad " +
            "    WHEN i.tipo='SALIDA'  THEN -i.cantidad " +
            "    ELSE 0 END), 0) AS stock, " +
            "    AVG(p.precio) AS precio " +
            "FROM producto p " +
            "LEFT JOIN inventario i ON i.id_producto = p.id_producto " +
            "WHERE (? IS NULL OR p.nombre LIKE ?) " +
            "  AND (? IS NULL OR p.categoria = ?) " +
            "  AND (? IS NULL OR p.producto_categoria = ?) " +
            "GROUP BY p.nombre, p.categoria, p.producto_categoria " +
            "ORDER BY p.nombre ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (isBlank(txtBuscar.getText())) {
                ps.setNull(1, Types.VARCHAR);
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(1, filtro);
                ps.setString(2, filtro);
            }
            if (genero == null || genero.equalsIgnoreCase("Todos")) {
                ps.setNull(3, Types.VARCHAR);
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(3, genero);
                ps.setString(4, genero);
            }
            if (tipo == null || tipo.equalsIgnoreCase("Todos")) {
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
            } else {
                ps.setString(5, tipo);
                ps.setString(6, tipo);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[]{
                        null, // no hay id en consolidado
                        rs.getString("nombre"),
                        rs.getString("genero"),
                        rs.getString("tipo"),
                        rs.getInt("stock"),
                        rs.getBigDecimal("precio")
                    };
                    modelo.addRow(fila);
                }
            }
        } catch (SQLException ex) {
            mostrarError("Error al cargar productos (Consolidado)", ex);
        }
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private String valOrNullStr(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty() || t.equalsIgnoreCase("Todos")) return null;
        return t;
    }

    private void limpiarTabla() {
        while (modelo.getRowCount() > 0) modelo.removeRow(0);
    }

    private void mostrarError(String titulo, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(), titulo, JOptionPane.ERROR_MESSAGE);
    }

    // CRUD (placeholders)
    private void onNuevo() {
        JOptionPane.showMessageDialog(this, "Nuevo producto (implementa formulario)");
    }

    private void onEditar() {
        if (productoSeleccionadoId == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto (fila no consolidada) para editar.");
            return;
        }
        JOptionPane.showMessageDialog(this, "Editar producto id=" + productoSeleccionadoId + " (implementa formulario)");
    }

    private void onEliminar() {
        if (productoSeleccionadoId == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto (fila no consolidada) para eliminar.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "¿Eliminar producto id=" + productoSeleccionadoId + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            // TODO: DELETE FROM producto WHERE id_producto = ?
            JOptionPane.showMessageDialog(this, "Eliminar implementado por ti.");
            recargarTablaSegunCheckbox();
        }
        productoSeleccionadoId = null;
    }

    private void onGuardar() {
        JOptionPane.showMessageDialog(this, "Guardar implementado por ti.");
        recargarTablaSegunCheckbox();
    }

    private void onCancelar() {
        JOptionPane.showMessageDialog(this, "Cancelar implementado por ti.");
    }

    private void onMovimientos() {
        if (productoSeleccionadoId == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto con ID (vista no consolidada) para ver movimientos.");
            return;
        }
        JOptionPane.showMessageDialog(this, "Abrir Movimientos para id_producto=" + productoSeleccionadoId);
    }

    // Mantén este main SOLO si quieres ejecutar CatProductos directo desde NetBeans
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                String url = "jdbc:mysql://localhost:3306/proyecto"
                        + "?useSSL=false"
                        + "&allowPublicKeyRetrieval=true"
                        + "&serverTimezone=UTC"
                        + "&characterEncoding=utf8";
                Connection cn = DriverManager.getConnection(url, "root", "100110");
                new CatProductos(cn).setVisible(true);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error de conexión: " + e.getMessage(), "DB", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
