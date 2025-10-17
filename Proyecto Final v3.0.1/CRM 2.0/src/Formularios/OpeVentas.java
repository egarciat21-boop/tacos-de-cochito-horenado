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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class OpeVentas extends JFrame {

    private final Connection conn;

    // Listas
    private JTextField txtBuscarCliente, txtBuscarProducto;
    private JList<Cliente> lstClientes;
    private JList<Producto> lstProductos;
    private DefaultListModel<Cliente> clientesModel;
    private DefaultListModel<Producto> productosModel;

    // Datos cliente
    private JTextField txtCliId, txtCliNombre, txtCliTelefono, txtCliDireccion, txtCliNit, txtCliEmail;
    private JTextArea txtFacturaCliente;
    private JButton btnCopiarFactura, btnLimpiarFactura, btnImprimirFactura;

    // Datos producto (sin Talla)
    private JTextField txtProdId, txtProdStock, txtProdNombre, txtProdEstado, txtProdCategoria, txtProdPrecio;
    private JTextArea txtProdDescripcion;

    // Precio y cantidades
    private JTextField txtPrecioVenta, txtCantidadVenta;
    private JTextField txtCantidadComprar; // NUEVO
    private JButton btnHabilitar, btnDeshabilitar, btnAgregar, btnImprimirDetalle;

    // Resumen de ventas
    private JTable tblDetalle;
    private DefaultTableModel detalleModel;
    private JLabel lblUltimaFactura, lblIVA, lblTotal, lblComprobante;

    // Estado
    private Integer clienteSelId = null;
    private BigDecimal subtotalAcum = BigDecimal.ZERO;
    private static final BigDecimal IVA_PORC = new BigDecimal("0.12");

    // Modelos simples
    private static class Cliente {
        int id; String nombre; String telefono; String nit; String email; String direccion;
        @Override public String toString() { return id + " - " + nombre; }
    }
    private static class Producto {
        int id; String nombre; String categoria; String tipo; int stock; BigDecimal precio;
        String estado;
        @Override public String toString() {
            return id + " - " + nombre + " | " + nz(categoria) + "/" + nz(tipo) + " | Stock: " + stock + " | Q" + (precio==null?"0.00":precio.toPlainString());
        }
    }

    public OpeVentas(Connection cn) {
        this.conn = Objects.requireNonNull(cn, "Connection no puede ser null");
        construirUI();
        cargarInicial();
    }

    private void construirUI() {
        setTitle("Operación de Ventas");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel pTop = new JPanel(new GridLayout(1, 2, 12, 0));
        pTop.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        pTop.add(wrapCard(panelClientesDisponibles(), "Clientes"));
        pTop.add(wrapCard(panelProductosDisponibles(), "Productos"));

        JPanel pMid = new JPanel(new GridBagLayout());
        pMid.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.BOTH;
        g.weightx = 1; g.weighty = 1;

        JPanel c1 = wrapCard(panelDatosCliente(), "Datos del Cliente");
        JPanel c2 = wrapCard(panelDatosProducto(), "Datos del Producto");
        JPanel c3 = wrapCard(panelPrecioCantidad(), "Precio y Cantidad");

        g.gridx=0; g.gridy=0; pMid.add(c1,g);
        g.gridx=1; g.gridy=0; pMid.add(c2,g);
        g.gridx=2; g.gridy=0; pMid.add(c3,g);

        JPanel pBottom = wrapCard(panelResumenVentas(), "Resumen de Ventas");

        JPanel root = new JPanel(new BorderLayout(6,6));
        root.add(pTop, BorderLayout.NORTH);
        root.add(pMid, BorderLayout.CENTER);
        root.add(pBottom, BorderLayout.SOUTH);

        JScrollPane sc = new JScrollPane(root);
        sc.setBorder(BorderFactory.createEmptyBorder());
        getContentPane().add(sc);

        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Búsquedas en vivo
        txtBuscarCliente.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                String q = txtBuscarCliente.getText().trim();
                if (q.length() >= 2) buscarClientes(q); else clientesModel.clear();
            }
        });
        txtBuscarProducto.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                String q = txtBuscarProducto.getText().trim();
                if (q.length() >= 2) buscarProductos(q); else productosModel.clear();
            }
        });
    }

    private JPanel wrapCard(JComponent inner, String title) {
        JPanel card = new JPanel(new BorderLayout());
        TitledBorder tb = BorderFactory.createTitledBorder(title);
        tb.setTitleFont(tb.getTitleFont().deriveFont(Font.BOLD, 12f));
        card.setBorder(tb);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel panelClientesDisponibles() {
        JPanel p = new JPanel(new BorderLayout(6,6));
        JPanel top = new JPanel(new BorderLayout(6,6));
        top.add(new JLabel("Buscar:"), BorderLayout.WEST);
        txtBuscarCliente = new JTextField();
        top.add(txtBuscarCliente, BorderLayout.CENTER);
        JButton btnSel = new JButton("Seleccionar");
        btnSel.addActionListener(e -> onSeleccionarCliente());
        top.add(btnSel, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        clientesModel = new DefaultListModel<>();
        lstClientes = new JList<>(clientesModel);
        lstClientes.setVisibleRowCount(8);
        lstClientes.addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                Cliente c = lstClientes.getSelectedValue();
                if (c != null) cargarClientePorId(c.id);
            }
        });
        lstClientes.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount()==2) onSeleccionarCliente(); }
        });
        p.add(new JScrollPane(lstClientes), BorderLayout.CENTER);
        return p;
    }

    private JPanel panelProductosDisponibles() {
        JPanel p = new JPanel(new BorderLayout(6,6));
        JPanel top = new JPanel(new BorderLayout(6,6));
        top.add(new JLabel("Buscar:"), BorderLayout.WEST);
        txtBuscarProducto = new JTextField();
        top.add(txtBuscarProducto, BorderLayout.CENTER);
        JButton btnSel = new JButton("Seleccionar");
        btnSel.addActionListener(e -> onSeleccionarProducto());
        top.add(btnSel, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        productosModel = new DefaultListModel<>();
        lstProductos = new JList<>(productosModel);
        lstProductos.setVisibleRowCount(8);
        lstProductos.addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                Producto pr = lstProductos.getSelectedValue();
                if (pr != null) cargarProductoPorId(pr.id);
            }
        });
        lstProductos.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount()==2) onSeleccionarProducto(); }
        });
        p.add(new JScrollPane(lstProductos), BorderLayout.CENTER);
        return p;
    }

    private JPanel panelDatosCliente() {
        JPanel container = new JPanel(new BorderLayout(6,6));
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = gbc();

        addRow(p, gc, "ID",        txtCliId = ro());
        addRow(p, gc, "Nombre",    txtCliNombre = ro());
        addRow(p, gc, "Teléfono",  txtCliTelefono = ro());
        addRow(p, gc, "Dirección", txtCliDireccion = ro());
        addRow(p, gc, "NIT",       txtCliNit = ro());
        addRow(p, gc, "E-mail",    txtCliEmail = ro());

        container.add(p, BorderLayout.NORTH);

        JPanel factura = new JPanel(new BorderLayout(6,6));
        txtFacturaCliente = new JTextArea(8,28);
        txtFacturaCliente.setEditable(false);
        txtFacturaCliente.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        factura.add(new JScrollPane(txtFacturaCliente), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,6));
        btnImprimirFactura = new JButton("Imprimir");
        btnCopiarFactura   = new JButton("Copiar");
        btnLimpiarFactura  = new JButton("Limpiar");
        acciones.add(btnImprimirFactura);
        acciones.add(btnCopiarFactura);
        acciones.add(btnLimpiarFactura);
        factura.add(acciones, BorderLayout.SOUTH);

        btnCopiarFactura.addActionListener(e -> {
            String texto = txtFacturaCliente.getText();
            if (texto != null && !texto.isBlank()) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new java.awt.datatransfer.StringSelection(texto), null);
                JOptionPane.showMessageDialog(this, "Ficha copiada al portapapeles.");
            }
        });
        btnLimpiarFactura.addActionListener(e -> txtFacturaCliente.setText(""));
        btnImprimirFactura.addActionListener(e -> imprimirArea(txtFacturaCliente));

        container.add(factura, BorderLayout.CENTER);
        return container;
    }

    private JPanel panelDatosProducto() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = gbc();

        txtProdId = new JTextField(12);        txtProdId.setEditable(false);
        txtProdStock = new JTextField(12);     txtProdStock.setEditable(false);
        txtProdNombre = new JTextField(18);    txtProdNombre.setEditable(false);
        txtProdEstado = new JTextField(12);    txtProdEstado.setEditable(false);
        txtProdCategoria = new JTextField(18); txtProdCategoria.setEditable(false);
        txtProdPrecio = new JTextField(12);    txtProdPrecio.setEditable(false);

        addRow(p, gc, "ID",        txtProdId);
        addRow(p, gc, "Stock",     txtProdStock);
        addRow(p, gc, "Nombre",    txtProdNombre);
        addRow(p, gc, "Estado",    txtProdEstado);
        addRow(p, gc, "Categoría", txtProdCategoria);
        addRow(p, gc, "Precio",    txtProdPrecio);

        gc.gridx = 0; p.add(new JLabel("Descripción"), gc);
        gc.gridx = 1;
        txtProdDescripcion = new JTextArea(3,22);
        txtProdDescripcion.setLineWrap(true);
        txtProdDescripcion.setWrapStyleWord(true);
        txtProdDescripcion.setEditable(false);
        p.add(new JScrollPane(txtProdDescripcion), gc);
        gc.gridy++;

        return p;
    }

    private JPanel panelPrecioCantidad() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = gbc();

        addRow(p, gc, "Precio de Venta", txtPrecioVenta = new JTextField(10));
        txtPrecioVenta.setEditable(false);

        addRow(p, gc, "Cantidad", txtCantidadVenta = new JTextField(10));

        addRow(p, gc, "Cantidad a comprar", txtCantidadComprar = new JTextField(10));
        txtCantidadComprar.setToolTipText("Cantidad que se agregará a la tabla de venta");

        // Botones
        btnHabilitar = new JButton("Editar precio");
        btnDeshabilitar = new JButton("Restaurar");
        gc.gridx = 0; gc.gridwidth = 1; p.add(new JLabel(""), gc);
        gc.gridx = 1; p.add(btnHabilitar, gc);
        gc.gridx = 2; p.add(btnDeshabilitar, gc);
        gc.gridy++;

        gc.gridx = 0; gc.gridwidth = 3;
        btnAgregar = new JButton("Agregar Producto");
        p.add(btnAgregar, gc); gc.gridy++;

        btnImprimirDetalle = new JButton("Imprimir Cotización");
        p.add(btnImprimirDetalle, gc);

        // Acciones
        btnHabilitar.addActionListener(e -> txtPrecioVenta.setEditable(true));
        btnDeshabilitar.addActionListener(e -> {
            txtPrecioVenta.setEditable(false);
            if (!txtProdPrecio.getText().isBlank()) txtPrecioVenta.setText(txtProdPrecio.getText());
        });
        btnAgregar.addActionListener(e -> onAgregarProducto());
        btnImprimirDetalle.addActionListener(e -> imprimirTexto(generarTextoCotizacion(), "Cotización"));

        return p;
    }

    private JPanel panelResumenVentas() {
        JPanel outer = new JPanel(new BorderLayout(8,8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,12,6));
        top.add(new JLabel("Última Factura:"));
        lblUltimaFactura = new JLabel("--------"); top.add(lblUltimaFactura);
        top.add(new JLabel("Comprobante:"));
        lblComprobante = new JLabel("--------"); top.add(lblComprobante);
        outer.add(top, BorderLayout.NORTH);

        detalleModel = new DefaultTableModel(new Object[]{"ID", "Producto", "Cantidad", "Precio", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> Integer.class;
                    case 2 -> Integer.class;
                    case 3, 4 -> BigDecimal.class;
                    default -> String.class;
                };
            }
        };
        tblDetalle = new JTable(detalleModel);
        tblDetalle.setRowHeight(22);
        JScrollPane sp = new JScrollPane(tblDetalle);
        sp.setPreferredSize(new Dimension(200, 220));
        outer.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(6,6));

        JPanel totals = new JPanel(new GridBagLayout());
        GridBagConstraints gc = gbc();
        gc.gridx=0; gc.gridy=0; totals.add(new JLabel("IVA (12%):"), gc);
        gc.gridx=1; lblIVA = new JLabel("Q 0.00"); totals.add(lblIVA, gc);
        gc.gridx=0; gc.gridy=1; totals.add(new JLabel("Total:"), gc);
        gc.gridx=1; lblTotal = new JLabel("Q 0.00"); totals.add(lblTotal, gc);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,6));
        JButton btnSeleccionar = new JButton("Seleccionar línea");
        JButton btnEliminar = new JButton("Eliminar línea");
        JButton btnCobrar = new JButton("COBRAR");
        btnCobrar.setFont(btnCobrar.getFont().deriveFont(Font.BOLD, 16f));
        btnSeleccionar.addActionListener(e -> tblDetalle.requestFocus());
        btnEliminar.addActionListener(e -> onEliminarLinea());
        btnCobrar.addActionListener(e -> onCobrar());
        rightBtns.add(btnSeleccionar);
        rightBtns.add(btnEliminar);
        rightBtns.add(btnCobrar);

        bottom.add(totals, BorderLayout.WEST);
        bottom.add(rightBtns, BorderLayout.EAST);
        outer.add(bottom, BorderLayout.SOUTH);

        return outer;
    }

    // ===================== Lógica =====================
    private void cargarInicial() {
        clientesModel.clear();
        productosModel.clear();
        recalcularTotales();
    }

    private void buscarClientes(String q) {
        clientesModel.clear();
        String like = "%" + q + "%";
        String sql = """
            SELECT id_cliente, nombre, telefono, nit, email, direccion
            FROM cliente
            WHERE nombre LIKE ? OR telefono LIKE ? OR nit LIKE ?
            ORDER BY nombre ASC
            LIMIT 100
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cliente c = new Cliente();
                    c.id = rs.getInt(1);
                    c.nombre = rs.getString(2);
                    c.telefono = rs.getString(3);
                    c.nit = rs.getString(4);
                    c.email = rs.getString(5);
                    c.direccion = rs.getString(6);
                    clientesModel.addElement(c);
                }
            }
        } catch (SQLException e) { showErr("Búsqueda de clientes", e); }
    }

    private void cargarClientePorId(int idCliente) {
        String sql = "SELECT id_cliente, nombre, telefono, nit, email, direccion FROM cliente WHERE id_cliente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_cliente");
                    String nombre = nz(rs.getString("nombre"));
                    String tel = nz(rs.getString("telefono"));
                    String nit = nz(rs.getString("nit"));
                    String email = nz(rs.getString("email"));
                    String dir = nz(rs.getString("direccion"));

                    txtCliId.setText(String.valueOf(id));
                    txtCliNombre.setText(nombre);
                    txtCliTelefono.setText(tel);
                    txtCliDireccion.setText(dir);
                    txtCliNit.setText(nit);
                    txtCliEmail.setText(email);
                    clienteSelId = id;

                    String facturaTxt = formatearFacturaCliente(id, nombre, tel, email, nit, dir);
                    txtFacturaCliente.setText(facturaTxt);
                } else {
                    JOptionPane.showMessageDialog(this, "Cliente no encontrado.");
                }
            }
        } catch (SQLException e) { showErr("Cargar cliente", e); }
    }

    private void onSeleccionarCliente() {
        Cliente c = lstClientes.getSelectedValue();
        if (c == null) { JOptionPane.showMessageDialog(this, "Selecciona un cliente de la lista."); return; }
        cargarClientePorId(c.id);
    }

    private void buscarProductos(String q) {
        productosModel.clear();
        String like = "%" + q + "%";
        String sql = """
            SELECT p.id_producto, p.nombre, p.categoria, p.producto_categoria, p.precio,
                   COALESCE(SUM(CASE WHEN i.tipo='ENTRADA' THEN i.cantidad
                                     WHEN i.tipo='SALIDA'  THEN -i.cantidad
                                     ELSE 0 END), 0) AS stock
            FROM producto p
            LEFT JOIN inventario i ON i.id_producto = p.id_producto
            WHERE p.nombre LIKE ? OR p.categoria LIKE ? OR p.producto_categoria LIKE ?
            GROUP BY p.id_producto, p.nombre, p.categoria, p.producto_categoria, p.precio
            ORDER BY p.nombre ASC
            LIMIT 200
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto p = new Producto();
                    p.id = rs.getInt("id_producto");
                    p.nombre = rs.getString("nombre");
                    p.categoria = rs.getString("categoria");
                    p.tipo = rs.getString("producto_categoria");
                    p.precio = rs.getBigDecimal("precio");
                    p.stock = rs.getInt("stock");
                    productosModel.addElement(p);
                }
            }
        } catch (SQLException e) { showErr("Búsqueda de productos", e); }
    }

    private void cargarProductoPorId(int idProducto) {
        String sql = """
            SELECT p.id_producto, p.nombre, p.categoria, p.producto_categoria, p.precio,
                   p.estado,
                   COALESCE(SUM(CASE WHEN i.tipo='ENTRADA' THEN i.cantidad
                                     WHEN i.tipo='SALIDA'  THEN -i.cantidad
                                     ELSE 0 END), 0) AS stock
            FROM producto p
            LEFT JOIN inventario i ON i.id_producto = p.id_producto
            WHERE p.id_producto = ?
            GROUP BY p.id_producto, p.nombre, p.categoria, p.producto_categoria, p.precio, p.estado
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_producto");
                    String nombre = nz(rs.getString("nombre"));
                    String estado = nz(rs.getString("estado"));
                    String categoria = nz(rs.getString("categoria"));
                    String tipo = nz(rs.getString("producto_categoria"));
                    BigDecimal precio = rs.getBigDecimal("precio");
                    int stock = rs.getInt("stock");

                    txtProdId.setText(String.valueOf(id));
                    txtProdStock.setText(String.valueOf(stock));
                    txtProdNombre.setText(nombre);
                    txtProdEstado.setText(estado);
                    txtProdCategoria.setText(categoria + " / " + tipo);
                    txtProdPrecio.setText(precio == null ? "0.00" : precio.setScale(2, RoundingMode.HALF_UP).toPlainString());

                    String desc = String.format(
                        "Producto: %s%nCategoría: %s%nTipo: %s%nEstado: %s%nPrecio: Q%s%nStock: %d",
                        nombre,
                        categoria.isBlank() ? "-" : categoria,
                        tipo.isBlank() ? "-" : tipo,
                        estado.isBlank() ? "-" : estado,
                        (precio == null ? "0.00" : precio.setScale(2, RoundingMode.HALF_UP).toPlainString()),
                        stock
                    );
                    txtProdDescripcion.setText(desc);
                    txtProdNombre.setToolTipText(desc);

                    // set defaults
                    txtPrecioVenta.setText(txtProdPrecio.getText());
                    if (txtCantidadComprar != null) txtCantidadComprar.setText("1");
                    if (txtCantidadComprar != null) SwingUtilities.invokeLater(() -> txtCantidadComprar.requestFocusInWindow());
                } else {
                    JOptionPane.showMessageDialog(this, "Producto no encontrado.");
                }
            }
        } catch (SQLException e) { showErr("Cargar producto", e); }
    }

    private void onSeleccionarProducto() {
        Producto p = lstProductos.getSelectedValue();
        if (p == null) { JOptionPane.showMessageDialog(this, "Selecciona un producto de la lista."); return; }
        cargarProductoPorId(p.id);
    }

    private void onAgregarProducto() {
        Integer id = tryParseInt(txtProdId.getText());
        if (id == null) { JOptionPane.showMessageDialog(this, "Selecciona un producto."); return; }

        int stock = tryParseInt(txtProdStock.getText()) == null ? 0 : tryParseInt(txtProdStock.getText());

        Integer c = tryParseInt(txtCantidadComprar != null ? txtCantidadComprar.getText() : txtCantidadVenta.getText());
        int cantNueva = (c == null ? 0 : c);

        if (cantNueva <= 0) {
            JOptionPane.showMessageDialog(this, "Ingresa una cantidad válida (> 0).");
            if (txtCantidadComprar != null) txtCantidadComprar.requestFocus();
            return;
        }

        BigDecimal precioUnit = tryParseBig(txtPrecioVenta.getText()).setScale(2, RoundingMode.HALF_UP);
        if (precioUnit.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "El precio unitario es inválido.");
            return;
        }

        int rowFound = -1;
        for (int i = 0; i < detalleModel.getRowCount(); i++) {
            int idRow = (Integer) detalleModel.getValueAt(i, 0);
            if (idRow == id) { rowFound = i; break; }
        }

        int cantTotal = cantNueva;
        if (rowFound >= 0) {
            int cantActual = (Integer) detalleModel.getValueAt(rowFound, 2);
            cantTotal = cantActual + cantNueva;
        }

        if (cantTotal > stock) {
            JOptionPane.showMessageDialog(this,
                    "Mercadería insuficiente.\nStock disponible: " + stock + "\nCantidad solicitada: " + cantTotal,
                    "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (rowFound >= 0) {
            BigDecimal subActual = (BigDecimal) detalleModel.getValueAt(rowFound, 4);
            BigDecimal subNuevo = precioUnit.multiply(new BigDecimal(cantTotal)).setScale(2, RoundingMode.HALF_UP);

            detalleModel.setValueAt(cantTotal, rowFound, 2);
            detalleModel.setValueAt(precioUnit, rowFound, 3);
            detalleModel.setValueAt(subNuevo, rowFound, 4);

            subtotalAcum = subtotalAcum.subtract(subActual).add(subNuevo);
        } else {
            String nombre = txtProdNombre.getText();
            BigDecimal subLinea = precioUnit.multiply(new BigDecimal(cantNueva)).setScale(2, RoundingMode.HALF_UP);
            detalleModel.addRow(new Object[]{ id, nombre, cantNueva, precioUnit, subLinea });
            subtotalAcum = subtotalAcum.add(subLinea);
        }

        recalcularTotales();
        if (txtCantidadComprar != null) txtCantidadComprar.setText("1");
    }

    private void onEliminarLinea() {
        int row = tblDetalle.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona una línea a eliminar."); return; }
        BigDecimal sub = (BigDecimal) detalleModel.getValueAt(row, 4);
        subtotalAcum = subtotalAcum.subtract(sub);
        detalleModel.removeRow(row);
        recalcularTotales();
    }

    private void onCobrar() {
        if (clienteSelId == null) { JOptionPane.showMessageDialog(this, "Selecciona un cliente."); return; }
        if (detalleModel.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "No hay productos en la venta."); return; }

        BigDecimal iva = subtotalAcum.multiply(IVA_PORC).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotalAcum.add(iva).setScale(2, RoundingMode.HALF_UP);

        try {
            String comprobante = generarComprobante();

            conn.setAutoCommit(false);

            int ventaId = insertarVenta(clienteSelId, null, subtotalAcum, iva, total, comprobante);

            conn.commit();
            conn.setAutoCommit(true);

            lblUltimaFactura.setText(String.valueOf(ventaId));
            lblComprobante.setText(comprobante);

            String comprobanteTexto = generarComprobanteVentaTexto(
                    comprobante, ventaId,
                    txtCliId.getText(), txtCliNombre.getText(), txtCliTelefono.getText(),
                    txtCliEmail.getText(), txtCliNit.getText(), txtCliDireccion.getText(),
                    detalleModel, subtotalAcum, iva, total
            );

            JOptionPane.showMessageDialog(this, "Venta registrada. Comprobante: " + comprobante);
            imprimirTexto(comprobanteTexto, "Comprobante de Venta");

            detalleModel.setRowCount(0);
            subtotalAcum = BigDecimal.ZERO;
            recalcularTotales();

        } catch (Exception ex) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            showErr("Cobrar", ex);
        }
    }

    // Genera folio robusto con fecha VARCHAR y tabla 'venta' (singular)
    private String generarComprobante() throws SQLException {
        String fecha = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int sec = 0;
        String q1 = "SELECT COUNT(*) FROM venta WHERE DATE(STR_TO_DATE(fecha, '%Y-%m-%d %H:%i:%s')) = CURDATE()";
        try (PreparedStatement ps = conn.prepareStatement(q1); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) sec = rs.getInt(1);
        } catch (SQLException ex) {
            try (PreparedStatement ps2 = conn.prepareStatement("SELECT COUNT(*) FROM venta");
                 ResultSet rs2 = ps2.executeQuery()) {
                if (rs2.next()) sec = rs2.getInt(1);
            }
        }
        sec += 1;
        return "V-" + fecha + "-" + String.format("%06d", sec);
    }

    // Inserta en la tabla 'venta' (singular). Columna 'ventas' guarda resumen SUB/IVA/TOTAL
    private int insertarVenta(int idCliente, Integer idEmpleado,
                              BigDecimal subtotal, BigDecimal iva,
                              BigDecimal total, String comprobante) throws SQLException {
        String nowTxt = java.time.LocalDateTime.now().toString().replace('T',' ').substring(0,19);
        String resumen = String.format("SUB=Q%s|IVA=Q%s|TOTAL=Q%s",
                subtotal.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                iva.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                total.setScale(2, RoundingMode.HALF_UP).toPlainString());

        String sql = "INSERT INTO venta (id_cliente, id_empleado, fecha, total, comprobante, ventas) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idCliente);
            if (idEmpleado == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, idEmpleado);
            ps.setString(3, nowTxt);            // fecha VARCHAR(45)
            ps.setBigDecimal(4, total);         // total FLOAT en DB — BigDecimal se convierte ok
            ps.setString(5, comprobante);       // comprobante
            ps.setString(6, resumen);           // columna 'ventas' con el resumen
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("No se generó ID de venta.");
    }

    // Impresión
    private void imprimirArea(JTextArea area) {
        try {
            boolean ok = area.print(
                new java.text.MessageFormat("Datos del Cliente"),
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
            if (!ok) {
                JOptionPane.showMessageDialog(this, "La impresión fue cancelada.");
            }
        } catch (Exception ex) {
            showErr("Imprimir texto", ex);
        }
    }

    private String generarComprobanteVentaTexto(
            String comprobante,
            int ventaId,
            String cliId, String cliNombre, String cliTelefono, String cliEmail, String cliNit, String cliDireccion,
            DefaultTableModel modelo,
            BigDecimal subtotal, BigDecimal iva, BigDecimal total
    ) {
        String empresa = "MI TIENDA S.A.";
        String ruc = "NIT EMPRESA: 1234567-8";
        String telEmp = "Tel: 5555-5555";
        String hoy = java.time.LocalDateTime.now().toString().replace('T',' ').substring(0,19);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-40s%n", empresa));
        sb.append(String.format("%-40s%n", ruc));
        sb.append(String.format("%-40s%n", telEmp));
        sb.append(repeat("-", 40)).append('\n');
        sb.append("COMPROBANTE: ").append(comprobante).append('\n');
        sb.append("VENTA ID:    ").append(ventaId).append('\n');
        sb.append("FECHA:       ").append(hoy).append('\n');
        sb.append(repeat("-", 40)).append('\n');
        sb.append("CLIENTE\n");
        sb.append(String.format("ID:        %s%n", nz(cliId)));
        sb.append(String.format("Nombre:    %s%n", nz(cliNombre)));
        sb.append(String.format("Teléfono:  %s%n", nz(cliTelefono)));
        sb.append(String.format("E-mail:    %s%n", nz(cliEmail)));
        sb.append(String.format("NIT:       %s%n", nz(cliNit)));
        sb.append(String.format("Dirección: %s%n", nz(cliDireccion)));
        sb.append(repeat("-", 40)).append('\n');
        sb.append(String.format("%-4s %-20s %5s %8s %9s%n", "ID", "Producto", "Cant", "Precio", "Subtotal"));
        sb.append(repeat("-", 40)).append('\n');

        for (int i = 0; i < modelo.getRowCount(); i++) {
            int idProd = (int) modelo.getValueAt(i, 0);
            String nombre = String.valueOf(modelo.getValueAt(i, 1));
            int cant = (int) modelo.getValueAt(i, 2);
            BigDecimal precio = (BigDecimal) modelo.getValueAt(i, 3);
            BigDecimal sub = (BigDecimal) modelo.getValueAt(i, 4);
            String nombre20 = nombre.length() > 20 ? nombre.substring(0, 20) : nombre;
            sb.append(String.format("%-4d %-20s %5d %8s %9s%n",
                    idProd, nombre20, cant, precio.toPlainString(), sub.toPlainString()));
        }
        sb.append(repeat("-", 40)).append('\n');
        sb.append(String.format("%-30s %9s%n", "SUBTOTAL:", subtotal.setScale(2, RoundingMode.HALF_UP).toPlainString()));
        sb.append(String.format("%-30s %9s%n", "IVA (12%):", iva.setScale(2, RoundingMode.HALF_UP).toPlainString()));
        sb.append(String.format("%-30s %9s%n", "TOTAL:", total.setScale(2, RoundingMode.HALF_UP).toPlainString()));
        sb.append(repeat("-", 40)).append('\n');
        sb.append("¡Gracias por su compra!");
        return sb.toString();
    }

    private String generarTextoCotizacion() {
        BigDecimal iva = subtotalAcum.multiply(IVA_PORC).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotalAcum.add(iva).setScale(2, RoundingMode.HALF_UP);
        return generarComprobanteVentaTexto(
                "COT-" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")),
                0,
                txtCliId.getText(), txtCliNombre.getText(), txtCliTelefono.getText(),
                txtCliEmail.getText(), txtCliNit.getText(), txtCliDireccion.getText(),
                detalleModel, subtotalAcum, iva, total
        );
    }

    // ===================== Helpers =====================
    private static GridBagConstraints gbc() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 8, 4, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;
        return gc;
    }
    private static void addRow(JPanel p, GridBagConstraints gc, String label, JComponent comp) {
        gc.gridx = 0; p.add(new JLabel(label + ":"), gc);
        gc.gridx = 1; p.add(comp, gc);
        gc.gridy++;
    }
    private static JTextField ro() { JTextField t = new JTextField(18); t.setEditable(false); return t; }
    private static String nz(String s) { return s == null ? "" : s; }
    private static Integer tryParseInt(String s) { try { return s==null||s.isBlank()?null:Integer.parseInt(s.trim()); } catch (Exception e) { return null; } }
    private static BigDecimal tryParseBig(String s) { try { return new BigDecimal(s.trim()); } catch (Exception e) { return BigDecimal.ZERO; } }
    private static String repeat(String s, int n) { return s.repeat(Math.max(0, n)); }

    private void recalcularTotales() {
        BigDecimal iva = subtotalAcum.multiply(IVA_PORC).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotalAcum.add(iva).setScale(2, RoundingMode.HALF_UP);
        lblIVA.setText("Q " + iva.toPlainString());
        lblTotal.setText("Q " + total.toPlainString());
    }

    private void showErr(String donde, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error en " + donde, JOptionPane.ERROR_MESSAGE);
    }

    private String formatearFacturaCliente(int id, String nombre, String telefono, String email, String nit, String direccion) {
        String empresa = "MI TIENDA S.A.";
        String ruc = "NIT EMPRESA: 1234567-8";
        String telEmp = "Tel: 5555-5555";
        String hoy = java.time.LocalDate.now().toString();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-36s%n", empresa));
        sb.append(String.format("%-36s%n", ruc));
        sb.append(String.format("%-36s%n", telEmp));
        sb.append(repeat("-", 36)).append('\n');
        sb.append("FECHA: ").append(hoy).append('\n');
        sb.append(repeat("-", 36)).append('\n');
        sb.append("CLIENTE\n");
        sb.append("ID:        ").append(id).append('\n');
        sb.append("Nombre:    ").append(nz(nombre)).append('\n');
        sb.append("Teléfono:  ").append(nz(telefono)).append('\n');
        sb.append("E-mail:    ").append(nz(email)).append('\n');
        sb.append("NIT:       ").append(nz(nit)).append('\n');
        sb.append("Dirección: ").append(nz(direccion)).append('\n');
        sb.append(repeat("-", 36)).append('\n');
        sb.append("Use Imprimir o Copiar para la factura.");
        return sb.toString();
    }
}