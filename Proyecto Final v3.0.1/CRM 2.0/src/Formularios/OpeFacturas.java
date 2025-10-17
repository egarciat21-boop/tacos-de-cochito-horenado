/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Formularios;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OpeFacturas extends JFrame {

    private final Connection conn;

    // Filtros
    private JTextField txtDesde, txtHasta, txtTexto, txtIdVenta;
    private JButton btnBuscar, btnLimpiar, btnAnterior, btnSiguiente, btnImprimir, btnExportar, btnDetalle;

    // Tabla
    private JTable tbl;
    private DefaultTableModel model;

    // Estado
    private int page = 0;
    private final int pageSize = 50;
    private int totalRows = 0;

    public OpeFacturas(Connection cn) {
        this.conn = cn;
        setTitle("Historial de Facturas");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        getContentPane().add(buildRoot());

        // Acciones
        btnBuscar.addActionListener(e -> { page = 0; consultar(); });
        btnLimpiar.addActionListener(e -> limpiarFiltros());
        btnAnterior.addActionListener(e -> { if (page > 0) { page--; consultar(); }});
        btnSiguiente.addActionListener(e -> {
            if ((page+1) * pageSize < totalRows) { page++; consultar(); }
        });
        btnDetalle.addActionListener(e -> verDetalleSeleccionado());
        btnImprimir.addActionListener(e -> imprimirSeleccionado());
        btnExportar.addActionListener(e -> exportarCSV());

        tbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) verDetalleSeleccionado();
            }
        });

        // Inicial
        setFechasHoy();
        consultar();
    }

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        root.add(buildFiltros(), BorderLayout.NORTH);
        root.add(buildTabla(), BorderLayout.CENTER);
        root.add(buildPie(), BorderLayout.SOUTH);
        return root;
    }

    private JComponent buildFiltros() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(titled("Filtros"));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4,8,4,8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx=0; g.gridy=0;

        p.add(new JLabel("Desde (yyyy-MM-dd):"), g);
        g.gridx=1; txtDesde = new JTextField(12); p.add(txtDesde, g);

        g.gridx=2; p.add(new JLabel("Hasta (yyyy-MM-dd):"), g);
        g.gridx=3; txtHasta = new JTextField(12); p.add(txtHasta, g);

        g.gridx=0; g.gridy=1; p.add(new JLabel("Texto (comprobante, cliente, NIT, email):"), g);
        g.gridx=1; g.gridwidth=3; txtTexto = new JTextField(); p.add(txtTexto, g);
        g.gridwidth=1;

        g.gridx=0; g.gridy=2; p.add(new JLabel("ID Venta:"), g);
        g.gridx=1; txtIdVenta = new JTextField(10); p.add(txtIdVenta, g);

        g.gridx=2; btnBuscar = new JButton("Buscar"); p.add(btnBuscar, g);
        g.gridx=3; btnLimpiar = new JButton("Limpiar"); p.add(btnLimpiar, g);

        return p;
    }

    private JComponent buildTabla() {
        model = new DefaultTableModel(new Object[]{
                "ID", "Fecha", "Cliente", "NIT", "Email", "Comprobante",
                "Subtotal", "IVA", "Total", "Resumen"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> Integer.class;
                    case 6,7,8 -> BigDecimal.class;
                    default -> String.class;
                };
            }
        };
        tbl = new JTable(model);
        tbl.setRowHeight(22);
        tbl.getColumnModel().getColumn(9).setPreferredWidth(180);
        return new JScrollPane(tbl);
    }

    private JComponent buildPie() {
        JPanel p = new JPanel(new BorderLayout());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,8,6));
        btnAnterior = new JButton("Anterior");
        btnSiguiente = new JButton("Siguiente");
        left.add(btnAnterior);
        left.add(btnSiguiente);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,6));
        btnDetalle = new JButton("Ver Detalle");
        btnImprimir = new JButton("Imprimir");
        btnExportar = new JButton("Exportar CSV");
        right.add(btnDetalle);
        right.add(btnImprimir);
        right.add(btnExportar);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private TitledBorder titled(String t) {
        TitledBorder tb = BorderFactory.createTitledBorder(t);
        tb.setTitleFont(tb.getTitleFont().deriveFont(Font.BOLD, 12f));
        return tb;
    }

    private void setFechasHoy() {
        String hoy = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        txtDesde.setText(hoy);
        txtHasta.setText(hoy);
    }

    private void limpiarFiltros() {
        setFechasHoy();
        txtTexto.setText("");
        txtIdVenta.setText("");
        page = 0;
        consultar();
    }

    private void consultar() {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");

        // Rango fechas: la columna fecha es VARCHAR(45) en tu DB; convertimos con STR_TO_DATE
        String desde = txtDesde.getText().trim();
        String hasta = txtHasta.getText().trim();
        if (!desde.isBlank() && !hasta.isBlank()) {
            where.append(" AND DATE(STR_TO_DATE(v.fecha, '%Y-%m-%d %H:%i:%s')) BETWEEN ? AND ? ");
            params.add(desde);
            params.add(hasta);
        } else if (!desde.isBlank()) {
            where.append(" AND DATE(STR_TO_DATE(v.fecha, '%Y-%m-%d %H:%i:%s')) >= ? ");
            params.add(desde);
        } else if (!hasta.isBlank()) {
            where.append(" AND DATE(STR_TO_DATE(v.fecha, '%Y-%m-%d %H:%i:%s')) <= ? ");
            params.add(hasta);
        }

        String texto = txtTexto.getText().trim();
        if (!texto.isBlank()) {
            where.append(" AND (v.comprobante LIKE ? OR c.nombre LIKE ? OR IFNULL(c.nit,'') LIKE ? OR IFNULL(c.email,'') LIKE ?) ");
            String like = "%" + texto + "%";
            params.add(like); params.add(like); params.add(like); params.add(like);
        }

        String idVenta = txtIdVenta.getText().trim();
        if (!idVenta.isBlank()) {
            where.append(" AND v.id_venta = ? ");
            try { params.add(Integer.parseInt(idVenta)); } catch (Exception ignored) {}
        }

        // Conteo total
        String countSql = "SELECT COUNT(*) " +
                "FROM venta v LEFT JOIN cliente c ON c.id_cliente = v.id_cliente " + where;
        totalRows = runCount(countSql, params);

        // Consulta de página
        String sql = "SELECT v.id_venta, v.fecha, IFNULL(c.nombre,''), IFNULL(c.nit,''), IFNULL(c.email,''), " +
                "v.comprobante, v.ventas, v.total " +
                "FROM venta v LEFT JOIN cliente c ON c.id_cliente = v.id_cliente " +
                where + " ORDER BY v.id_venta DESC LIMIT ? OFFSET ?";

        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(pageSize);
        pageParams.add(page * pageSize);

        fillTable(sql, pageParams);
        actualizarTitulo();
    }

    private int runCount(String sql, List<Object> params) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            showErr("Contar filas", e);
        }
        return 0;
    }

    private void fillTable(String sql, List<Object> params) {
        model.setRowCount(0);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String fecha = rs.getString(2);
                    String cliente = rs.getString(3);
                    String nit = rs.getString(4);
                    String email = rs.getString(5);
                    String comp = rs.getString(6);
                    String resumen = rs.getString(7); // 'ventas' columna
                    BigDecimal total = getBig(rs, 8);

                    // Parse del resumen para mostrar Subtotal e IVA
                    BigDecimal sub = BigDecimal.ZERO, iva = BigDecimal.ZERO;
                    if (resumen != null) {
                        try {
                            for (String part : resumen.split("\\|")) {
                                String[] kv = part.split("=");
                                if (kv.length == 2) {
                                    String k = kv[0].trim().toUpperCase();
                                    String v = kv[1].replace("Q","").trim();
                                    BigDecimal bd = new BigDecimal(v);
                                    if (k.startsWith("SUB")) sub = bd;
                                    else if (k.startsWith("IVA")) iva = bd;
                                }
                            }
                        } catch (Exception ignored) {}
                    }

                    model.addRow(new Object[]{
                            id, fecha, cliente, nit, email, comp,
                            sub.setScale(2, RoundingMode.HALF_UP),
                            iva.setScale(2, RoundingMode.HALF_UP),
                            total == null ? BigDecimal.ZERO : total.setScale(2, RoundingMode.HALF_UP),
                            resumen
                    });
                }
            }
        } catch (SQLException e) {
            showErr("Consultar historial", e);
        }
    }

    private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object p = params.get(i);
            if (p instanceof Integer) ps.setInt(i+1, (Integer)p);
            else ps.setString(i+1, String.valueOf(p));
        }
    }

    private void actualizarTitulo() {
        int from = page * pageSize + 1;
        int to = Math.min((page+1) * pageSize, totalRows);
        String rango = totalRows == 0 ? "0 de 0" : (from + " - " + to + " de " + totalRows);
        setTitle("Historial de Facturas — " + rango);
    }

    private void verDetalleSeleccionado() {
        int row = tbl.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona una factura."); return; }

        int id = (Integer) model.getValueAt(row, 0);
        String fecha = String.valueOf(model.getValueAt(row, 1));
        String cliente = String.valueOf(model.getValueAt(row, 2));
        String nit = String.valueOf(model.getValueAt(row, 3));
        String email = String.valueOf(model.getValueAt(row, 4));
        String comp = String.valueOf(model.getValueAt(row, 5));
        BigDecimal sub = (BigDecimal) model.getValueAt(row, 6);
        BigDecimal iva = (BigDecimal) model.getValueAt(row, 7);
        BigDecimal total = (BigDecimal) model.getValueAt(row, 8);
        String resumen = String.valueOf(model.getValueAt(row, 9));

        String texto = formatearComprobante(id, comp, fecha, cliente, nit, email, sub, iva, total, resumen);
        JTextArea area = new JTextArea(texto, 24, 60);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);

        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Detalle de Factura", JOptionPane.INFORMATION_MESSAGE);
    }

    private void imprimirSeleccionado() {
        int row = tbl.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona una factura."); return; }

        int id = (Integer) model.getValueAt(row, 0);
        String fecha = String.valueOf(model.getValueAt(row, 1));
        String cliente = String.valueOf(model.getValueAt(row, 2));
        String nit = String.valueOf(model.getValueAt(row, 3));
        String email = String.valueOf(model.getValueAt(row, 4));
        String comp = String.valueOf(model.getValueAt(row, 5));
        BigDecimal sub = (BigDecimal) model.getValueAt(row, 6);
        BigDecimal iva = (BigDecimal) model.getValueAt(row, 7);
        BigDecimal total = (BigDecimal) model.getValueAt(row, 8);
        String resumen = String.valueOf(model.getValueAt(row, 9));

        String texto = formatearComprobante(id, comp, fecha, cliente, nit, email, sub, iva, total, resumen);
        imprimirTexto(texto, "Factura " + comp);
    }

    private void exportarCSV() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exportar CSV");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".csv")) {
                f = new java.io.File(f.getParentFile(), f.getName() + ".csv");
            }
            try (FileWriter w = new FileWriter(f)) {
                // encabezados
                for (int c = 0; c < model.getColumnCount(); c++) {
                    if (c > 0) w.write(",");
                    w.write(escapeCsv(model.getColumnName(c)));
                }
                w.write("\n");
                // filas
                for (int r = 0; r < model.getRowCount(); r++) {
                    for (int c = 0; c < model.getColumnCount(); c++) {
                        if (c > 0) w.write(",");
                        Object val = model.getValueAt(r, c);
                        w.write(escapeCsv(val==null?"":val.toString()));
                    }
                    w.write("\n");
                }
                w.flush();
                JOptionPane.showMessageDialog(this, "CSV exportado: " + f.getAbsolutePath());
            } catch (Exception ex) {
                showErr("Exportar CSV", ex);
            }
        }
    }

    // ======= Utilidades =======
    private String formatearComprobante(int id, String comp, String fecha, String cliente, String nit, String email,
                                        BigDecimal sub, BigDecimal iva, BigDecimal total, String resumen) {
        String empresa = "MI TIENDA S.A.";
        String ruc = "NIT EMPRESA: 1234567-8";
        String telEmp = "Tel: 5555-5555";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-40s%n", empresa));
        sb.append(String.format("%-40s%n", ruc));
        sb.append(String.format("%-40s%n", telEmp));
        sb.append("========================================\n");
        sb.append("COMPROBANTE: ").append(comp).append('\n');
        sb.append("VENTA ID:    ").append(id).append('\n');
        sb.append("FECHA:       ").append(fecha).append('\n');
        sb.append("========================================\n");
        sb.append("CLIENTE\n");
        sb.append("Nombre:  ").append(nullToDash(cliente)).append('\n');
        sb.append("NIT:     ").append(nullToDash(nit)).append('\n');
        sb.append("Email:   ").append(nullToDash(email)).append('\n');
        sb.append("========================================\n");
        sb.append("RESUMEN: ").append(nullToDash(resumen)).append('\n');
        sb.append("========================================\n");
        sb.append(String.format("%-20s %15s%n", "SUBTOTAL:", sub.setScale(2, RoundingMode.HALF_UP).toPlainString()));
        sb.append(String.format("%-20s %15s%n", "IVA (12%):", iva.setScale(2, RoundingMode.HALF_UP).toPlainString()));
        sb.append(String.format("%-20s %15s%n", "TOTAL:", total.setScale(2, RoundingMode.HALF_UP).toPlainString()));
        sb.append("========================================\n");
        sb.append("Gracias por su compra.");
        return sb.toString();
    }

    private static BigDecimal getBig(ResultSet rs, int idx) throws SQLException {
        String s = rs.getString(idx);
        if (s == null) return null;
        try { return new BigDecimal(s); } catch (Exception e) { return null; }
    }

    private static String nullToDash(String s) { return s == null || s.isBlank() ? "-" : s; }

    private static String escapeCsv(String s) {
        boolean need = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (!need) return s;
        return "\"" + s.replace("\"","\"\"") + "\"";
    }

    private void imprimirTexto(String texto, String titulo) {
        JTextArea area = new JTextArea(texto);
        area.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        try {
            boolean ok = area.print(
                new java.text.MessageFormat(titulo),
                new java.text.MessageFormat("Página {0}"),
                true,
                null,
                null,
                true
            );
            if (!ok) JOptionPane.showMessageDialog(this, "La impresión fue cancelada.");
        } catch (Exception ex) {
            showErr("Imprimir", ex);
        }
    }

    private void showErr(String where, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error en " + where, JOptionPane.ERROR_MESSAGE);
    }

    // Método de prueba rápida: abrir la ventana con una conexión
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection cn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/proyecto?useSSL=false&serverTimezone=America/Guatemala",
                        "root", "100110"
                );
                new OpeFacturas(cn).setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        });
    }
}