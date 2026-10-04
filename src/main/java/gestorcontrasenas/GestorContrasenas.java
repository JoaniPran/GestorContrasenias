package gestorcontrasenas;

import gestorcontrasenas.datos.AlmacenContrasenas;
import gestorcontrasenas.interfaz.BotonEstilizado;
import gestorcontrasenas.interfaz.DialogoAutenticacion;
import gestorcontrasenas.interfaz.MenusContextuales;
import gestorcontrasenas.interfaz.PanelRedondeado;
import gestorcontrasenas.interfaz.TableRowTransferHandler;
import gestorcontrasenas.modelo.RegistroContrasena;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gestorcontrasenas.interfaz.PaletaColores.*;

public class GestorContrasenas extends JFrame {

    // Modo y Colores (Estilo Web Slate/Zinc)
    private boolean modoOscuro = true;

    private Color colorBg;
    private Color colorCard;
    private Color colorInput;
    private Color colorTextPrimary;
    private Color colorTextMuted;
    private Color colorBorder;

    private JTextField txtCuenta;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private PanelRedondeado panelFormulario;
    private JLabel lblTitulo, lblUserBadge, lblCuenta, lblUsuario, lblPassword;
    private JScrollPane scrollPane;
    private BotonEstilizado btnTema;

    private final AlmacenContrasenas almacenContrasenas;

    private final List<JComponent> componentesEstilizados = new ArrayList<>();

    public GestorContrasenas(String usuarioLogueado, String claveMaestra) {
        this.almacenContrasenas = new AlmacenContrasenas(usuarioLogueado, claveMaestra);
        actualizarColoresTema();
        construirInterfaz(usuarioLogueado);
        aplicarColoresInterfaz();
        cargarDatosGuardados();
    }

    private void construirInterfaz(String usuarioLogueado) {
        // Ventana nativa controlada por el Sistema Operativo
        setTitle("KeyVault - " + usuarioLogueado);
        setSize(780, 580);
        setMinimumSize(new Dimension(680, 480));
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout(12, 12));
        panelPrincipal.setBorder(new EmptyBorder(10, 0, 14, 0));
        setContentPane(panelPrincipal);

        // --- HEADER DE APLICACIÓN ---
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setOpaque(false);
        panelHeader.setBorder(new EmptyBorder(4, 16, 4, 16));

        lblTitulo = new JLabel("🛡️ KeyVault");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel panelDerechaHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelDerechaHeader.setOpaque(false);

        lblUserBadge = new JLabel("  👤 " + usuarioLogueado + "  ");
        lblUserBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUserBadge.setOpaque(true);

        btnTema = new BotonEstilizado("☀️ Claro", COLOR_PRIMARY, COLOR_PRIMARY_HOVER, 8);

        BotonEstilizado btnCerrarSesion = new BotonEstilizado("🚪 Salir", COLOR_DANGER, COLOR_DANGER_HOVER, 8);

        panelDerechaHeader.add(lblUserBadge);
        panelDerechaHeader.add(btnTema);
        panelDerechaHeader.add(btnCerrarSesion);

        panelHeader.add(lblTitulo, BorderLayout.WEST);
        panelHeader.add(panelDerechaHeader, BorderLayout.EAST);

        add(panelHeader, BorderLayout.NORTH);

        // --- FORMULARIO Y TABLA ---
        JPanel panelContenido = new JPanel(new BorderLayout(12, 12));
        panelContenido.setOpaque(false);
        panelContenido.setBorder(new EmptyBorder(0, 16, 0, 16));

        panelFormulario = new PanelRedondeado(14, colorCard, colorBorder);
        panelFormulario.setLayout(new GridBagLayout());
        panelFormulario.setBorder(new EmptyBorder(12, 14, 12, 14));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        lblCuenta = crearEtiqueta("Servicio / App:");
        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(lblCuenta, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtCuenta = crearCampoTexto();
        panelFormulario.add(txtCuenta, gbc);

        lblUsuario = crearEtiqueta("Usuario / Email:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelFormulario.add(lblUsuario, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtUsuario = crearCampoTexto();
        panelFormulario.add(txtUsuario, gbc);

        lblPassword = crearEtiqueta("Contraseña:");
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panelFormulario.add(lblPassword, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPassword = crearCampoPassword();
        panelFormulario.add(txtPassword, gbc);

        // Botones Formulario
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelBotones.setOpaque(false);

        BotonEstilizado btnGuardar = new BotonEstilizado("➕ Guardar", COLOR_PRIMARY, COLOR_PRIMARY_HOVER, 8);
        BotonEstilizado btnActualizar = new BotonEstilizado("✏️ Editar", COLOR_SUCCESS, COLOR_SUCCESS_HOVER, 8);
        BotonEstilizado btnEliminar = new BotonEstilizado("🗑️ Eliminar", COLOR_DANGER, COLOR_DANGER_HOVER, 8);
        BotonEstilizado btnLimpiar = new BotonEstilizado("🧹 Limpiar", new Color(100, 116, 139), new Color(71, 85, 105), 8);

        panelBotones.add(btnGuardar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 4, 2, 4);
        panelFormulario.add(panelBotones, gbc);

        // --- TABLA ---
        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setOpaque(false);
        panelCentro.add(panelFormulario, BorderLayout.NORTH);

        String[] columnas = {"Servicio / Aplicación", "Usuario / Email", "Contraseña"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modeloTabla);
        
        // Habilitar Arrastrar y Soltar Filas (Drag & Drop)
        tabla.setDragEnabled(true);
        tabla.setDropMode(DropMode.INSERT_ROWS);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setTransferHandler(new TableRowTransferHandler(tabla, this::guardarEnArchivo));

        scrollPane = new JScrollPane(tabla);
        panelCentro.add(scrollPane, BorderLayout.CENTER);

        panelContenido.add(panelCentro, BorderLayout.CENTER);
        add(panelContenido, BorderLayout.CENTER);

        configurarEventos(btnGuardar, btnActualizar, btnEliminar, btnLimpiar, btnCerrarSesion);
    }

    private void configurarEventos(BotonEstilizado btnGuardar, BotonEstilizado btnActualizar,
                                   BotonEstilizado btnEliminar, BotonEstilizado btnLimpiar,
                                   BotonEstilizado btnCerrarSesion) {
        MenusContextuales.agregarMenuTexto(txtCuenta);
        MenusContextuales.agregarMenuTexto(txtUsuario);
        MenusContextuales.agregarMenuTexto(txtPassword);
        MenusContextuales.agregarMenuTabla(tabla);

        btnTema.addActionListener(e -> alternarTema());
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
        btnGuardar.addActionListener(e -> guardarNuevoRegistro());
        btnActualizar.addActionListener(e -> actualizarRegistroSeleccionado());
        btnEliminar.addActionListener(e -> eliminarRegistro());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    int fila = tabla.getSelectedRow();
                    if (fila != -1) {
                        txtCuenta.setText(modeloTabla.getValueAt(fila, 0).toString());
                        txtUsuario.setText(modeloTabla.getValueAt(fila, 1).toString());
                        txtPassword.setText(modeloTabla.getValueAt(fila, 2).toString());
                    }
                }
            }
        });
    }

    // --- CERRAR SESIÓN ---
    private void cerrarSesion() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            DialogoAutenticacion diagAuth = new DialogoAutenticacion(false);
            diagAuth.setVisible(true);
            if (diagAuth.exito) {
                new GestorContrasenas(diagAuth.usuarioAutenticado, diagAuth.claveMaestra).setVisible(true);
            }
        });
    }

    // --- TEMAS SLATE ---
    private void actualizarColoresTema() {
        if (modoOscuro) {
            colorBg = new Color(15, 23, 42);          // Slate 900
            colorCard = new Color(30, 41, 59);        // Slate 800
            colorInput = new Color(15, 23, 42);       // Slate 900
            colorTextPrimary = new Color(248, 250, 252); // Slate 50
            colorTextMuted = new Color(148, 163, 184); // Slate 400
            colorBorder = new Color(51, 65, 85);       // Slate 700
        } else {
            colorBg = new Color(248, 250, 252);       // Slate 50
            colorCard = new Color(255, 255, 255);     // White
            colorInput = new Color(241, 245, 249);    // Slate 100
            colorTextPrimary = new Color(15, 23, 42); // Slate 900
            colorTextMuted = new Color(100, 116, 139); // Slate 500
            colorBorder = new Color(226, 232, 240);   // Slate 200
        }
    }

    private void alternarTema() {
        modoOscuro = !modoOscuro;
        actualizarColoresTema();
        btnTema.setText(modoOscuro ? "☀️️ Claro" : "🌙 Oscuro");
        aplicarColoresInterfaz();
        repaint();
        revalidate();
    }

    private void aplicarColoresInterfaz() {
        getContentPane().setBackground(colorBg);

        lblTitulo.setForeground(colorTextPrimary);
        lblCuenta.setForeground(colorTextPrimary);
        lblUsuario.setForeground(colorTextPrimary);
        lblPassword.setForeground(colorTextPrimary);

        lblUserBadge.setBackground(modoOscuro ? new Color(30, 41, 59) : new Color(226, 232, 240));
        lblUserBadge.setForeground(COLOR_PRIMARY);

        panelFormulario.setColores(colorCard, colorBorder);

        for (JComponent comp : componentesEstilizados) {
            comp.setBackground(colorInput);
            comp.setForeground(colorTextPrimary);
            if (comp instanceof JTextField) {
                ((JTextField) comp).setCaretColor(colorTextPrimary);
            }
            comp.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(colorBorder, 1, true),
                    new EmptyBorder(6, 8, 6, 8)
            ));
        }

        estilarTabla();
    }

    private void estilarTabla() {
        tabla.setBackground(colorCard);
        tabla.setForeground(colorTextPrimary);
        tabla.setGridColor(colorBorder);
        tabla.setRowHeight(38);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setSelectionBackground(modoOscuro ? new Color(49, 65, 96) : new Color(224, 231, 255));
        tabla.setSelectionForeground(modoOscuro ? colorTextPrimary : COLOR_PRIMARY);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(modoOscuro ? new Color(20, 29, 47) : new Color(241, 245, 249));
        header.setForeground(colorTextMuted);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(100, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, colorBorder));

        scrollPane.getViewport().setBackground(colorCard);
        scrollPane.setBorder(BorderFactory.createLineBorder(colorBorder, 1, true));

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, colorBorder),
                        new EmptyBorder(0, 10, 0, 10)
                ));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? colorCard : (modoOscuro ? new Color(24, 34, 50) : new Color(248, 250, 252)));
                }
                return c;
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    // --- DATOS Y VALIDACIONES ---
    private boolean existeNombreCuenta(String cuenta, int filaIgnorada) {
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if (i == filaIgnorada) continue;
            String cuentaExistente = modeloTabla.getValueAt(i, 0).toString().trim();
            if (cuentaExistente.equalsIgnoreCase(cuenta)) return true;
        }
        return false;
    }

    private void guardarNuevoRegistro() {
        String cuenta = txtCuenta.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (cuenta.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Por favor completa todos los campos.", "Campos Incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (existeNombreCuenta(cuenta, -1)) {
            mostrarMensaje("Ya existe un registro con el nombre '" + cuenta + "'.", "Duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modeloTabla.addRow(new Object[]{cuenta, usuario, password});
        guardarEnArchivo();
        limpiarCampos();
    }

    private void actualizarRegistroSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            mostrarMensaje("Selecciona un elemento de la lista para editar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String cuenta = txtCuenta.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (cuenta.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Por favor completa todos los campos.", "Campos Incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (existeNombreCuenta(cuenta, fila)) {
            mostrarMensaje("Ya existe otro registro con el nombre '" + cuenta + "'.", "Nombre Duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modeloTabla.setValueAt(cuenta, fila, 0);
        modeloTabla.setValueAt(usuario, fila, 1);
        modeloTabla.setValueAt(password, fila, 2);

        guardarEnArchivo();
        limpiarCampos();
        mostrarMensaje("Registro actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void eliminarRegistro() {
        int fila = tabla.getSelectedRow();
        if (fila != -1) {
            modeloTabla.removeRow(fila);
            guardarEnArchivo();
            limpiarCampos();
        } else {
            mostrarMensaje("Selecciona una fila para eliminar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtCuenta.setText("");
        txtUsuario.setText("");
        txtPassword.setText("");
        tabla.clearSelection();
    }

    private void guardarEnArchivo() {
        try {
            List<RegistroContrasena> registros = new ArrayList<>();
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                registros.add(new RegistroContrasena(
                        modeloTabla.getValueAt(i, 0).toString(),
                        modeloTabla.getValueAt(i, 1).toString(),
                        modeloTabla.getValueAt(i, 2).toString()));
            }
            almacenContrasenas.guardar(registros);
        } catch (Exception e) {
            mostrarMensaje("Error al guardar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatosGuardados() {
        try {
            almacenContrasenas.cargar(registro -> modeloTabla.addRow(new Object[]{
                    registro.getCuenta(), registro.getUsuario(), registro.getPassword()}));
        } catch (IOException e) {
            mostrarMensaje("Error al cargar datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- COMPONENTES AUXILIARES ---
    private JLabel crearEtiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }

    private JTextField crearCampoTexto() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        componentesEstilizados.add(field);
        return field;
    }

    private JPasswordField crearCampoPassword() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        componentesEstilizados.add(field);
        return field;
    }

    private void mostrarMensaje(String msg, String titulo, int tipo) {
        UIManager.put("OptionPane.background", colorCard);
        UIManager.put("Panel.background", colorCard);
        UIManager.put("OptionPane.messageForeground", colorTextPrimary);
        JOptionPane.showMessageDialog(this, msg, titulo, tipo);
    }

    // --- MÉTODO MAIN ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DialogoAutenticacion diag = new DialogoAutenticacion(false);
            diag.setVisible(true);
            if (diag.exito) {
                new GestorContrasenas(diag.usuarioAutenticado, diag.claveMaestra).setVisible(true);
            }
        });
    }
}