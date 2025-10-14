package Formularios;

import Clases.Empleados;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TablaEmpleados extends JFrame {

    private JTable tabla;
    private DefaultTableModel modelo;

    public TablaEmpleados() {
        setTitle("Gestión de Empleados");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        modelo = new DefaultTableModel(new Object[]{"id_empleado", "nombre", "teléfono", "dirección", "salario", "email", "puesto", "jornada"}, 0);
        tabla = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabla);
        add(scroll, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel();
        JButton btnAgregar = new JButton("Agregar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnActualizar = new JButton("Actualizar Lista");

        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnActualizar);
        add(panelBotones, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> abrirFormulario(null));
        btnEditar.addActionListener(e -> editarSeleccionado());
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        btnActualizar.addActionListener(e -> cargarDatos());

        cargarDatos();
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        List<Empleados> lista = Empleados.obtenerTodos();
        for (Empleados emp : lista) {
            modelo.addRow(new Object[]{
                emp.getId_empleado(),
                emp.getNombre(),
                emp.getTelefono(),
                emp.getDireccion(),
                emp.getSalario(),
                emp.getEmail(),
                emp.getPuesto(),
                emp.getJornada()
            });
        }
    }

    private void eliminarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado para eliminar");
            return;
        }
        int id = (int) modelo.getValueAt(fila, 0);
        if (JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar este empleado?") == 0) {
            if (Empleados.eliminar(id)) {
                cargarDatos();
                JOptionPane.showMessageDialog(this, "Empleado eliminado correctamente");
            }
        }
    }

    private void editarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado para editar");
            return;
        }
        Empleados emp = new Empleados();
        emp.setId_empleado((int) modelo.getValueAt(fila, 0));
        emp.setNombre((String) modelo.getValueAt(fila, 1));
        emp.setTelefono((String) modelo.getValueAt(fila, 2));
        emp.setDireccion((String) modelo.getValueAt(fila, 3));
        emp.setSalario(Float.parseFloat(modelo.getValueAt(fila, 4).toString()));
        emp.setEmail((String) modelo.getValueAt(fila, 5));
        emp.setPuesto((String) modelo.getValueAt(fila, 6));
        emp.setJornada((String) modelo.getValueAt(fila, 7));
        abrirFormulario(emp);
    }

    private void abrirFormulario(Empleados emp) {
        JDialog dialogo = new JDialog(this, emp == null ? "Agregar Empleado" : "Editar Empleado", true);
        dialogo.setSize(400, 400);
        dialogo.setLayout(new GridLayout(9, 2, 5, 5));
        dialogo.setLocationRelativeTo(this);

        JTextField txtNombre = new JTextField(emp == null ? "" : emp.getNombre());
        JTextField txtTelefono = new JTextField(emp == null ? "" : emp.getTelefono());
        JTextField txtDireccion = new JTextField(emp == null ? "" : emp.getDireccion());
        JTextField txtSalario = new JTextField(emp == null ? "" : String.valueOf(emp.getSalario()));
        JTextField txtEmail = new JTextField(emp == null ? "" : emp.getEmail());
        JTextField txtPuesto = new JTextField(emp == null ? "" : emp.getPuesto());
        JTextField txtJornada = new JTextField(emp == null ? "" : emp.getJornada());

        dialogo.add(new JLabel("nombre:")); dialogo.add(txtNombre);
        dialogo.add(new JLabel("teléfono:")); dialogo.add(txtTelefono);
        dialogo.add(new JLabel("dirección:")); dialogo.add(txtDireccion);
        dialogo.add(new JLabel("salario:")); dialogo.add(txtSalario);
        dialogo.add(new JLabel("email:")); dialogo.add(txtEmail);
        dialogo.add(new JLabel("puesto:")); dialogo.add(txtPuesto);
        dialogo.add(new JLabel("jornada:")); dialogo.add(txtJornada);

        JButton btnGuardar = new JButton("Guardar");
        dialogo.add(new JLabel());
        dialogo.add(btnGuardar);

        btnGuardar.addActionListener(e -> {
            Empleados nuevo = (emp == null) ? new Empleados() : emp;
            nuevo.setNombre(txtNombre.getText());
            nuevo.setTelefono(txtTelefono.getText());
            nuevo.setDireccion(txtDireccion.getText());
            nuevo.setSalario(Float.parseFloat(txtSalario.getText()));
            nuevo.setEmail(txtEmail.getText());
            nuevo.setPuesto(txtPuesto.getText());
            nuevo.setJornada(txtJornada.getText());

            boolean ok = (emp == null) ? nuevo.guardar() : nuevo.actualizar();
            if (ok) {
                JOptionPane.showMessageDialog(dialogo, "Empleado guardado correctamente");
                dialogo.dispose();
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(dialogo, "Error al guardar empleado");
            }
        });

        dialogo.setVisible(true);
    }
}
