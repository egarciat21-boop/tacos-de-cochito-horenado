/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Formularios;

import Clases.Conexion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

public class CatInventarios extends JFrame {

    private static final String TB_INV = "inventario";
    private static final String COL_ID_INV = "id_inventario";
    private static final String COL_ID_PROD = "id_producto";
    private static final String COL_CANT   = "cantidad";
    private static final String COL_TIPO   = "tipo";   // 'ENTRADA'/'SALIDA'
    private static final String COL_FECHA  = "fecha";  // DATE

    private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private JComboBox<String> cbFiltroCategoria, cbFiltroTipoProd, cbProducto, cbTipoMov;
    private JTextField txtCantidad, txtFecha;
    private JButton btnAgregar, btnEditar, btnEliminar, btnLimpiar, btnMostrar, btnLimpiarFiltros;
    private JTable tabla;
    private DefaultTableModel modelo;

    private Integer idSeleccionado = null;
    private final Map<String, Integer> mapaProductos = new LinkedHashMap<>();
    private Integer idProductoInicial = null;

    // NUEVO: catálogos fijos
    private static final String[] CATS = {"HOMBRE", "MUJER", "NIÑO", "NIÑA"};
    private static final String[] TIPOS_FIJOS = {"SUÉTERES", "PANTALONES", "PLAYERAS", "CAMISAS"};

    public CatInventarios() { this(null); }

    public CatInventarios(Integer idProductoInicial) {
        this.idProductoInicial = idProductoInicial;

        setTitle("Inventarios por Categoría y Producto");
        setSize(1180, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        construirUI();
        wireEvents();

        // Inicializar combos de filtro con valores controlados
        inicializarFiltros();

        // Si se recibió un producto desde CatProductos, intenta posicionar filtros/producto
        if (idProductoInicial != null) {
            // Llenamos productos con los filtros actuales (vacíos al inicio)
            cargarProductosCombo();
            seleccionarProductoPorId(idProductoInicial);
        } else {
            cargarProductosCombo();
        }

        cargarTabla();
        actualizarBotones();
    }

    private void construirUI() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        c.gridx = 0; c.gridy = row; form.add(new JLabel("Categoría (segmento):"), c);
        cbFiltroCategoria = new JComboBox<>();
        cbFiltroCategoria.setEditable(false); // NO editable
        c.gridx = 1; c.weightx = 1.0; form.add(cbFiltroCategoria, c); c.weightx = 0;

        c.gridx = 2; form.add(new JLabel("Tipo de prenda:"), c);
        cbFiltroTipoProd = new JComboBox<>();
        cbFiltroTipoProd.setEditable(false); // NO editable, dependiente de categoría
        c.gridx = 3; form.add(cbFiltroTipoProd, c);

        btnLimpiarFiltros = new JButton("Limpiar filtros");
        c.gridx = 4; form.add(btnLimpiarFiltros, c);
        row++;

        c.gridx = 0; c.gridy = row; form.add(new JLabel("Producto:"), c);
        cbProducto = new JComboBox<>();
        cbProducto.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1.0; form.add(cbProducto, c);
        c.gridwidth = 1; c.weightx = 0;

        c.gridx = 4; form.add(new JLabel("Tipo mov:"), c);
        cbTipoMov = new JComboBox<>(new String[]{"ENTRADA", "SALIDA"});
        c.gridx = 5; form.add(cbTipoMov, c);
        row++;

        c.gridx = 0; c.gridy = row; form.add(new JLabel("Cantidad:"), c);
        txtCantidad = new JTextField();
        c.gridx = 1; form.add(txtCantidad, c);

        c.gridx = 2; form.add(new JLabel("Fecha (yyyy-MM-dd):"), c);
        txtFecha = new JTextField();
        c.gridx = 3; form.add(txtFecha, c);
        row++;

        add(form, BorderLayout.NORTH);

        modelo = new DefaultTableModel(
                new Object[]{"ID", "Fecha", "Categoría", "Tipo prenda", "Producto", "Tipo mov", "Cantidad"}, 0) {
            @Override public boolean isCellEditable(int r, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return switch (col) {
                    case 0 -> Integer.class;
                    case 1 -> java.sql.Date.class;
                    case 6 -> Integer.class;
                    default -> String.class;
                };
            }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnAgregar = new JButton("Agregar");
        btnMostrar = new JButton("Mostrar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");
        acciones.add(btnAgregar); acciones.add(btnMostrar);
        acciones.add(btnEditar); acciones.add(btnEliminar);
        acciones.add(btnLimpiar);
        add(acciones, BorderLayout.SOUTH);
    }

    private void inicializarFiltros() {
       
        cbFiltroCategoria.removeAllItems();
        cbFiltroCategoria.addItem(""); // opción vacía para "todas"
        for (String cat : CATS) cbFiltroCategoria.addItem(cat);

   
        poblarTiposPorCategoriaSeleccionada();
    }

    private void poblarTiposPorCategoriaSeleccionada() {
        cbFiltroTipoProd.removeAllItems();
        cbFiltroTipoProd.addItem(""); // opción vacía para "todos"

        // Si hay categoría seleccionada específica, mostrar las 4 opciones fijas
        String cat = safeSelected(cbFiltroCategoria);
        if (cat != null && !cat.isBlank()) {
            for (String t : TIPOS_FIJOS) cbFiltroTipoProd.addItem(t);
        }
    }

    private void wireEvents() {
        btnAgregar.addActionListener(e -> agregar());
        btnMostrar.addActionListener(e -> cargarTabla());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnLimpiarFiltros.addActionListener(e -> {
            cbFiltroCategoria.setSelectedItem("");
            poblarTiposPorCategoriaSeleccionada();
            cbFiltroTipoProd.setSelectedItem("");
            cargarProductosCombo();
            cargarTabla();
        });

        cbFiltroCategoria.addActionListener(e -> {
            // Al cambiar categoría, repoblar tipos dependientes y recargar productos
            poblarTiposPorCategoriaSeleccionada();
            cbFiltroTipoProd.setSelectedItem(""); // por defecto "todos"
            cargarProductosCombo();
        });

        cbFiltroTipoProd.addActionListener(e -> {
            cargarProductosCombo();
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onFilaSeleccionada();
        });
    }

    private void cargarProductosCombo() {
        mapaProductos.clear();
        cbProducto.removeAllItems();

        String cat = safeSelected(cbFiltroCategoria);
        String tip = safeSelected(cbFiltroTipoProd);

        String where = "";
        List<String> parts = new ArrayList<>();
        if (cat != null && !cat.isBlank()) parts.add("categoria = ?");
        if (tip != null && !tip.isBlank()) parts.add("producto_categoria = ?");
        if (!parts.isEmpty()) where = " WHERE " + String.join(" AND ", parts);

        String sql = "SELECT id_producto, nombre FROM producto" + where + " ORDER BY nombre ASC";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            if (cat != null && !cat.isBlank()) ps.setString(i++, cat);
            if (tip != null && !tip.isBlank()) ps.setString(i++, tip);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mapaProductos.put(rs.getString("nombre"), rs.getInt("id_producto"));
                    cbProducto.addItem(rs.getString("nombre"));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + e.getMessage());
        }
    }

    private String safeSelected(JComboBox<String> cb) {
        Object o = cb.getSelectedItem();
        return o == null ? "" : o.toString().trim();
    }

    private void seleccionarProductoPorId(int idProducto) {
        for (Map.Entry<String,Integer> e : mapaProductos.entrySet()) {
            if (Objects.equals(e.getValue(), idProducto)) {
                cbProducto.setSelectedItem(e.getKey());
                break;
            }
        }
    }

    private void cargarTabla() {
        modelo.setRowCount(0);

        String cat = safeSelected(cbFiltroCategoria);
        String tip = safeSelected(cbFiltroTipoProd);
        String where = "";
        List<String> parts = new ArrayList<>();
        if (cat != null && !cat.isBlank()) parts.add("p.categoria = ?");
        if (tip != null && !tip.isBlank()) parts.add("p.producto_categoria = ?");
        if (!parts.isEmpty()) where = "WHERE " + String.join(" AND ", parts);

        final String sql =
            "SELECT i." + COL_ID_INV + " AS idinv, i." + COL_FECHA + " AS fecha, " +
            "       p.categoria AS categoria, p.producto_categoria AS tipo_prenda, " +
            "       p.nombre AS producto, i." + COL_TIPO + " AS tipo, i." + COL_CANT + " AS cantidad " +
            "FROM " + TB_INV + " i " +
            "JOIN producto p ON p.id_producto = i." + COL_ID_PROD + " " +
            where + " " +
            "ORDER BY p.categoria ASC, p.producto_categoria ASC, p.nombre ASC, i." + COL_FECHA + " DESC, i." + COL_ID_INV + " DESC";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            if (cat != null && !cat.isBlank()) ps.setString(i++, cat);
            if (tip != null && !tip.isBlank()) ps.setString(i++, tip);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelo.addRow(new Object[]{
                        rs.getInt("idinv"),
                        rs.getDate("fecha"),
                        rs.getString("categoria"),
                        rs.getString("tipo_prenda"),
                        rs.getString("producto"),
                        rs.getString("tipo"),
                        rs.getInt("cantidad")
                    });
                }
            }
        } catch (SQLException e) {
            manejarSqlException("mostrar", e);
        }
        idSeleccionado = null;
        actualizarBotones();
    }

    private void onFilaSeleccionada() {
        int row = tabla.getSelectedRow();
        if (row < 0) { idSeleccionado = null; actualizarBotones(); return; }
        idSeleccionado = (Integer) modelo.getValueAt(row, 0);
        txtFecha.setText(modelo.getValueAt(row, 1) == null ? "" : modelo.getValueAt(row, 1).toString());
        cbProducto.setSelectedItem(String.valueOf(modelo.getValueAt(row, 4)));
        cbTipoMov.setSelectedItem(String.valueOf(modelo.getValueAt(row, 5)));
        txtCantidad.setText(String.valueOf(modelo.getValueAt(row, 6)));
        actualizarBotones();
    }

    private void agregar() {
        Integer idProd = mapaProductos.get((String) cbProducto.getSelectedItem());
        if (idProd == null) { JOptionPane.showMessageDialog(this, "Selecciona un producto."); return; }
        Integer cant = parseEnteroPositivo(txtCantidad.getText().trim());
        if (cant == null) return;
        String tipo = cbTipoMov.getSelectedItem().toString();
        DateOrNull fecha = parseFecha(txtFecha.getText().trim());

        if ("SALIDA".equals(tipo)) {
            int stock = stockActual(idProd);
            if (cant > stock) {
                JOptionPane.showMessageDialog(this, "Stock insuficiente. Stock actual: " + stock);
                return;
            }
        }

        String cols = COL_ID_PROD + "," + COL_CANT + "," + COL_TIPO;
        String vals = "?, ?, ?";
        if (!fecha.isNull) { cols += "," + COL_FECHA; vals += ", ?"; }

        final String sql = "INSERT INTO " + TB_INV + " (" + cols + ") VALUES (" + vals + ")";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int i = 1;
            ps.setInt(i++, idProd);
            ps.setInt(i++, cant);
            ps.setString(i++, tipo);
            if (!fecha.isNull) ps.setDate(i++, fecha.value);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) idSeleccionado = rs.getInt(1); }
            JOptionPane.showMessageDialog(this, "Movimiento agregado.");
            cargarTabla();
            limpiarCampos();
        } catch (SQLException e) {
            manejarSqlException("agregar", e);
        }
    }

    private void editar() {
        if (idSeleccionado == null) { JOptionPane.showMessageDialog(this, "Selecciona un movimiento."); return; }
        Integer idProd = mapaProductos.get((String) cbProducto.getSelectedItem());
        if (idProd == null) { JOptionPane.showMessageDialog(this, "Selecciona un producto."); return; }
        Integer cant = parseEnteroPositivo(txtCantidad.getText().trim());
        if (cant == null) return;
        String tipo = cbTipoMov.getSelectedItem().toString();
        DateOrNull fecha = parseFecha(txtFecha.getText().trim());

        if ("SALIDA".equals(tipo)) {
            int stockSinActual = stockExcluyendoMovimiento(idProd, idSeleccionado);
            if (cant > stockSinActual) {
                JOptionPane.showMessageDialog(this, "Stock insuficiente tras la edición. Disponible: " + stockSinActual);
                return;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(TB_INV).append(" SET ");
        sb.append(COL_ID_PROD).append("=?, ").append(COL_CANT).append("=?, ").append(COL_TIPO).append("=?");
        if (!fecha.isNull) sb.append(", ").append(COL_FECHA).append("=?");
        sb.append(" WHERE ").append(COL_ID_INV).append("=?");

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sb.toString())) {
            int i = 1;
            ps.setInt(i++, idProd);
            ps.setInt(i++, cant);
            ps.setString(i++, tipo);
            if (!fecha.isNull) ps.setDate(i++, fecha.value);
            ps.setInt(i, idSeleccionado);
            int n = ps.executeUpdate();
            if (n > 0) {
                JOptionPane.showMessageDialog(this, "Movimiento actualizado.");
                cargarTabla();
                limpiar();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el movimiento.");
            }
        } catch (SQLException e) {
            manejarSqlException("actualizar", e);
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) { JOptionPane.showMessageDialog(this, "Selecciona un movimiento."); return; }
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar el movimiento seleccionado?", "Confirmar",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        final String sql = "DELETE FROM " + TB_INV + " WHERE " + COL_ID_INV + "=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSeleccionado);
            int n = ps.executeUpdate();
            if (n > 0) {
                JOptionPane.showMessageDialog(this, "Movimiento eliminado.");
                cargarTabla();
                limpiar();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el movimiento para eliminar.");
            }
        } catch (SQLException e) {
            manejarSqlException("eliminar", e);
        }
    }

    private int stockActual(int idProducto) {
        final String sql =
            "SELECT COALESCE(SUM(CASE WHEN " + COL_TIPO + "='ENTRADA' THEN " + COL_CANT +
            " WHEN " + COL_TIPO + "='SALIDA' THEN -" + COL_CANT + " END), 0) " +
            "FROM " + TB_INV + " WHERE " + COL_ID_PROD + "=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { manejarSqlException("stock", e); }
        return 0;
    }

    private int stockExcluyendoMovimiento(int idProducto, int idInventario) {
        final String sql =
            "SELECT COALESCE(SUM(CASE WHEN " + COL_TIPO + "='ENTRADA' THEN " + COL_CANT +
            " WHEN " + COL_TIPO + "='SALIDA' THEN -" + COL_CANT + " END), 0) " +
            "FROM " + TB_INV + " WHERE " + COL_ID_PROD + "=? AND " + COL_ID_INV + " <> ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idInventario);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { manejarSqlException("stock-excluir", e); }
        return 0;
    }

    private Integer parseEnteroPositivo(String s) {
        if (s == null || s.isBlank()) { JOptionPane.showMessageDialog(this, "Cantidad requerida."); return null; }
        try {
            int v = Integer.parseInt(s);
            if (v <= 0) { JOptionPane.showMessageDialog(this, "Cantidad debe ser > 0."); return null; }
            return v;
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Cantidad inválida."); return null; }
    }

    private static class DateOrNull { final java.sql.Date value; final boolean isNull; DateOrNull(java.sql.Date v, boolean n){value=v;isNull=n;} }
    private DateOrNull parseFecha(String s) {
        if (s == null || s.isBlank()) return new DateOrNull(null, true);
        try { LocalDate d = LocalDate.parse(s, DATE_FMT); return new DateOrNull(java.sql.Date.valueOf(d), false); }
        catch (DateTimeParseException ex) { JOptionPane.showMessageDialog(this, "Fecha inválida. Formato: yyyy-MM-dd"); return new DateOrNull(null, true); }
    }

    private void limpiarCampos() {
        cbTipoMov.setSelectedItem("ENTRADA");
        txtCantidad.setText("");
        txtFecha.setText("");
    }
    private void limpiar() {
        idSeleccionado = null;
        tabla.clearSelection();
        limpiarCampos();
        actualizarBotones();
    }
    private void actualizarBotones() {
        boolean sel = idSeleccionado != null;
        btnEditar.setEnabled(sel);
        btnEliminar.setEnabled(sel);
    }
    private void manejarSqlException(String accion, SQLException e) {
        JOptionPane.showMessageDialog(this, "Error SQL al " + accion + ": " + e.getMessage());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignore) {}
            new CatInventarios().setVisible(true);
        });
    }
}