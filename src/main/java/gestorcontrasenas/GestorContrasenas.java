package gestorcontrasenas;

import static gestorcontrasenas.interfaz.PaletaColores.*;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import gestorcontrasenas.datos.AlmacenContrasenas;
import gestorcontrasenas.datos.AlmacenPreferencias;
import gestorcontrasenas.interfaz.BotonEstilizado;
import gestorcontrasenas.interfaz.DialogoAutenticacion;
import gestorcontrasenas.interfaz.MenusContextuales;
import gestorcontrasenas.interfaz.PanelRedondeado;
import gestorcontrasenas.interfaz.TableRowTransferHandler;
import gestorcontrasenas.modelo.RegistroContrasena;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

public class GestorContrasenas extends JFrame {

	// Modo y Colores (Estilo Web Slate/Zinc)
	private boolean modoOscuro = true;

	private Color colorBg;
	private Color colorCard;
	private Color colorInput;
	private Color colorTextPrimary;
	private Color colorTextMuted;
	private Color colorBorder;
	private Color colorResaltado;

	private JTextField txtCuenta;
	private JTextField txtUsuario;
	private JTextField txtBuscar;
	private JPasswordField txtPassword;
	private JTable tabla;
	private DefaultTableModel modeloTabla;
	private TableRowSorter<DefaultTableModel> ordenadorTabla;
	private PanelRedondeado panelFormulario;
	private JLabel lblTitulo, lblUserBadge, lblCuenta, lblUsuario, lblPassword, lblBuscar;
	private JScrollPane scrollPane;

	private final AlmacenContrasenas almacenContrasenas;
	private final String usuarioLogueado;
	private boolean confirmarAntesDeMostrarContrasena;

	private final List<JComponent> componentesEstilizados = new ArrayList<>();
	private final Set<String> cuentasConPasswordVisible = new HashSet<>();
	private boolean almacenamientoDisponible = true;

	public GestorContrasenas(String usuarioLogueado, String claveMaestra) {
		this.usuarioLogueado = usuarioLogueado;
		this.almacenContrasenas = new AlmacenContrasenas(usuarioLogueado, claveMaestra);
		this.modoOscuro = AlmacenPreferencias.cargarModoOscuro(usuarioLogueado);
		this.colorResaltado = AlmacenPreferencias.cargarColorResaltado(usuarioLogueado);
		this.confirmarAntesDeMostrarContrasena = AlmacenPreferencias
				.cargarConfirmacionMostrarContrasena(usuarioLogueado);
		actualizarColoresTema();
		construirInterfaz(usuarioLogueado);
		aplicarColoresInterfaz();
		cargarDatosGuardados();
	}

	private void construirInterfaz(String usuarioLogueado) {
		// Ventana nativa controlada por el Sistema Operativo
		setTitle("Gestor de Contraseñas - " + usuarioLogueado);
		setSize(780, 580);
		setMinimumSize(new Dimension(680, 480));
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		JPanel panelPrincipal = new JPanel(new BorderLayout(12, 12));
		panelPrincipal.setBorder(new EmptyBorder(10, 16, 14, 16));
		setContentPane(panelPrincipal);

		// --- HEADER DE APLICACIÓN ---
		JPanel panelHeader = new JPanel(new BorderLayout());
		panelHeader.setOpaque(false);

		lblTitulo = new JLabel("Gestor de Contraseñas");
		lblTitulo.setIcon(new FlatSVGIcon("icons/shield.svg", 20, 20));
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

		lblUserBadge = new JLabel(" " + usuarioLogueado + " ");
		lblUserBadge.setIcon(new FlatSVGIcon("icons/user.svg", 14, 14));
		lblUserBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblUserBadge.setOpaque(true);

		JButton btnConfiguracion = new JButton();
		btnConfiguracion.setIcon(new FlatSVGIcon("icons/settings.svg", 16, 16));
		btnConfiguracion.setToolTipText("Configuración");
		btnConfiguracion.setPreferredSize(new Dimension(36, 36));
		btnConfiguracion.setFocusable(false);
		btnConfiguracion.setBorderPainted(false);
		btnConfiguracion.setContentAreaFilled(false);
		btnConfiguracion.setOpaque(false);
		btnConfiguracion.setCursor(new Cursor(Cursor.HAND_CURSOR));

		BotonEstilizado btnCerrarSesion = new BotonEstilizado("Salir", COLOR_DANGER, COLOR_DANGER_HOVER, 8);
		btnCerrarSesion.setIcon(new FlatSVGIcon("icons/external-link.svg", 14, 14));
		btnCerrarSesion.setPlano(true);

		JPanel panelAccionesHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		panelAccionesHeader.setOpaque(false);
		panelAccionesHeader.add(lblUserBadge);
		panelAccionesHeader.add(btnCerrarSesion);
		panelAccionesHeader.add(btnConfiguracion);

		panelHeader.add(lblTitulo, BorderLayout.WEST);
		panelHeader.add(panelAccionesHeader, BorderLayout.EAST);
		panelPrincipal.add(panelHeader, BorderLayout.NORTH);

		// --- FORMULARIO Y TABLA ---
		JPanel panelContenido = new JPanel(new BorderLayout(12, 12));
		panelContenido.setOpaque(false);

		panelFormulario = new PanelRedondeado(14, colorCard, colorBorder);
		panelFormulario.setLayout(new GridBagLayout());
		panelFormulario.setBorder(new EmptyBorder(12, 14, 12, 14));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 4, 4, 4);

		lblCuenta = crearEtiqueta("Servicio / App:");
		gbc.gridx = 0;
		gbc.gridy = 0;
		panelFormulario.add(lblCuenta, gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		txtCuenta = crearCampoTexto();
		panelFormulario.add(txtCuenta, gbc);

		lblUsuario = crearEtiqueta("Usuario / Email:");
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		panelFormulario.add(lblUsuario, gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		txtUsuario = crearCampoTexto();
		panelFormulario.add(txtUsuario, gbc);

		lblPassword = crearEtiqueta("Contraseña:");
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		panelFormulario.add(lblPassword, gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		txtPassword = crearCampoPassword();
		panelFormulario.add(txtPassword, gbc);

		// --- BOTONES FORMULARIO ---
		BotonEstilizado btnGuardar = new BotonEstilizado("Guardar", COLOR_PRIMARY, COLOR_PRIMARY_HOVER, 8);
		btnGuardar.setIcon(new FlatSVGIcon("icons/save.svg", 16, 16));

		BotonEstilizado btnActualizar = new BotonEstilizado("Editar", COLOR_SUCCESS, COLOR_SUCCESS_HOVER, 8);
		btnActualizar.setIcon(new FlatSVGIcon("icons/edit.svg", 16, 16));

		BotonEstilizado btnEliminar = new BotonEstilizado("Eliminar", COLOR_DANGER, COLOR_DANGER_HOVER, 8);
		btnEliminar.setIcon(new FlatSVGIcon("icons/trash.svg", 16, 16));

		BotonEstilizado btnLimpiar = new BotonEstilizado("Limpiar", COLOR_BOTON, COLOR_BOTON_HOVER, 8);
		btnLimpiar.setIcon(new FlatSVGIcon("icons/delete.svg", 16, 16));

		JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		panelBotones.setOpaque(false);
		panelBotones.add(btnGuardar);
		panelBotones.add(btnActualizar);
		panelBotones.add(btnEliminar);
		panelBotones.add(btnLimpiar);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(8, 4, 2, 4);
		panelFormulario.add(panelBotones, gbc);

		// --- BÚSQUEDA Y TABLA ---
		JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
		panelCentro.setOpaque(false);
		panelCentro.add(panelFormulario, BorderLayout.NORTH);

		String[] columnas = {"Servicio / Aplicación", "Usuario / Email", "Contraseña"};
		modeloTabla = new DefaultTableModel(columnas, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 2;
			}
		};

		tabla = new JTable(modeloTabla);
		tabla.setDragEnabled(true);
		tabla.setDropMode(DropMode.INSERT_ROWS);
		tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabla.setTransferHandler(new TableRowTransferHandler(tabla, this::guardarEnArchivo));

		ordenadorTabla = new TableRowSorter<>(modeloTabla);
		for (int columna = 0; columna < modeloTabla.getColumnCount(); columna++) {
			ordenadorTabla.setSortable(columna, false);
		}
		tabla.setRowSorter(ordenadorTabla);

		JPanel panelBusqueda = new JPanel(new BorderLayout(8, 0));
		panelBusqueda.setOpaque(false);
		lblBuscar = crearEtiqueta("Buscar servicio o correo:");
		lblBuscar.setIcon(new FlatSVGIcon("icons/search.svg", 14, 14));

		txtBuscar = crearCampoTexto();
		txtBuscar.setToolTipText("Filtra por servicio/aplicación o correo electrónico");
		panelBusqueda.add(lblBuscar, BorderLayout.WEST);
		panelBusqueda.add(txtBuscar, BorderLayout.CENTER);

		JPanel panelTabla = new JPanel(new BorderLayout(8, 8));
		panelTabla.setOpaque(false);
		panelTabla.add(panelBusqueda, BorderLayout.NORTH);

		scrollPane = new JScrollPane(tabla);
		panelTabla.add(scrollPane, BorderLayout.CENTER);
		panelCentro.add(panelTabla, BorderLayout.CENTER);

		panelContenido.add(panelCentro, BorderLayout.CENTER);
		panelPrincipal.add(panelContenido, BorderLayout.CENTER);

		configurarEventos(btnGuardar, btnActualizar, btnEliminar, btnLimpiar, btnConfiguracion, btnCerrarSesion);
	}

	private void configurarEventos(BotonEstilizado btnGuardar, BotonEstilizado btnActualizar,
			BotonEstilizado btnEliminar, BotonEstilizado btnLimpiar, JButton btnConfiguracion,
			BotonEstilizado btnCerrarSesion) {
		MenusContextuales.agregarMenuTexto(txtCuenta);
		MenusContextuales.agregarMenuTexto(txtUsuario);
		MenusContextuales.agregarMenuPassword(txtPassword);
		MenusContextuales.agregarMenuTabla(tabla, 2);

		btnConfiguracion.addActionListener(e -> mostrarConfiguracion());
		btnCerrarSesion.addActionListener(e -> cerrarSesion());
		btnGuardar.addActionListener(e -> guardarNuevoRegistro());
		btnActualizar.addActionListener(e -> actualizarRegistroSeleccionado());
		btnEliminar.addActionListener(e -> eliminarRegistro());
		btnLimpiar.addActionListener(e -> limpiarCampos());

		txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			@Override
			public void insertUpdate(javax.swing.event.DocumentEvent e) {
				actualizarFiltroTabla();
			}
			@Override
			public void removeUpdate(javax.swing.event.DocumentEvent e) {
				actualizarFiltroTabla();
			}
			@Override
			public void changedUpdate(javax.swing.event.DocumentEvent e) {
				actualizarFiltroTabla();
			}
		});

		tabla.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					int filaVista = tabla.getSelectedRow();
					if (filaVista != -1) {
						int fila = tabla.convertRowIndexToModel(filaVista);
						txtCuenta.setText(modeloTabla.getValueAt(fila, 0).toString());
						txtUsuario.setText(modeloTabla.getValueAt(fila, 1).toString());
						txtPassword.setText(modeloTabla.getValueAt(fila, 2).toString());
					}
				}
			}
		});
	}

	private void actualizarFiltroTabla() {
		String consulta = txtBuscar.getText().trim();
		if (consulta.isEmpty()) {
			ordenadorTabla.setRowFilter(null);
			tabla.setDragEnabled(true);
		} else {
			ordenadorTabla.setRowFilter(RowFilter.regexFilter("(?iu)" + Pattern.quote(consulta), 0, 1));
			tabla.setDragEnabled(false);
		}
	}

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

	private void actualizarColoresTema() {
		float tono = Color.RGBtoHSB(colorResaltado.getRed(), colorResaltado.getGreen(), colorResaltado.getBlue(),
				null)[0];
		if (modoOscuro) {
			colorBg = colorTono(tono, 0.42f, 0.16f);
			colorCard = colorTono(tono, 0.32f, 0.29f);
			colorInput = colorTono(tono, 0.26f, 0.24f);
			colorTextPrimary = COLOR_TEXTO_OSCURO;
			colorTextMuted = COLOR_TEXTO_SECUNDARIO_OSCURO;
			colorBorder = colorTono(tono, 0.28f, 0.44f);
		} else {
			colorBg = colorTono(tono, 0.24f, 0.60f);
			colorCard = colorTono(tono, 0.18f, 0.93f);
			colorInput = colorTono(tono, 0.14f, 0.98f);
			colorTextPrimary = new Color(38, 40, 43);
			colorTextMuted = new Color(82, 85, 89);
			colorBorder = colorTono(tono, 0.16f, 0.72f);
		}
	}

	private Color colorTono(float tono, float saturacion, float brillo) {
		return Color.getHSBColor(tono, saturacion, brillo);
	}

	private boolean establecerPreferencias(boolean oscuro, boolean confirmarMostrarContrasena, Color resaltado) {
		try {
			AlmacenPreferencias.guardarPreferencias(usuarioLogueado, oscuro, confirmarMostrarContrasena, resaltado);
		} catch (IOException e) {
			mostrarMensaje("No se pudieron guardar las preferencias: " + e.getMessage(), "Error de configuración",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		confirmarAntesDeMostrarContrasena = confirmarMostrarContrasena;
		colorResaltado = resaltado;
		modoOscuro = oscuro;
		actualizarColoresTema();
		aplicarColoresInterfaz();
		repaint();
		revalidate();
		return true;
	}

	private void mostrarConfiguracion() {
		JDialog dialogo = new JDialog(this, "Configuración", true);
		JPanel panel = new JPanel(new BorderLayout(12, 18));
		panel.setBorder(new EmptyBorder(20, 22, 18, 22));
		panel.setBackground(colorCard);

		JLabel lblTema = new JLabel("Tema de la aplicación");
		lblTema.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lblTema.setForeground(colorTextPrimary);
		panel.add(lblTema, BorderLayout.NORTH);

		JPanel opciones = new JPanel();
		opciones.setLayout(new BoxLayout(opciones, BoxLayout.Y_AXIS));
		opciones.setOpaque(false);
		JLabel lblDescripcion = new JLabel("Usa el aspecto oscuro o claro");
		lblDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblDescripcion.setForeground(colorTextMuted);
		lblDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
		opciones.add(lblDescripcion);
		opciones.add(Box.createVerticalStrut(8));

		JLabel lblModo = new JLabel(modoOscuro ? "Modo oscuro" : "Modo claro");
		lblModo.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblModo.setForeground(colorTextPrimary);

		InterruptorTema interruptor = new InterruptorTema(modoOscuro, colorResaltado);
		interruptor.setToolTipText("Cambiar entre modo oscuro y modo claro");
		interruptor.getAccessibleContext().setAccessibleName("Cambiar tema");
		interruptor.addActionListener(e -> lblModo.setText(interruptor.isSelected() ? "Modo oscuro" : "Modo claro"));

		JPanel selector = new JPanel(new BorderLayout(10, 0));
		selector.setOpaque(true);
		selector.setBorder(new EmptyBorder(10, 12, 10, 12));
		selector.setBackground(colorInput);
		selector.add(lblModo, BorderLayout.CENTER);
		selector.add(interruptor, BorderLayout.EAST);
		selector.setAlignmentX(Component.LEFT_ALIGNMENT);
		opciones.add(selector);
		opciones.add(Box.createVerticalStrut(14));

		JLabel lblColorResaltado = new JLabel("Color de resaltado");
		lblColorResaltado.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblColorResaltado.setForeground(colorTextPrimary);
		lblColorResaltado.setAlignmentX(Component.LEFT_ALIGNMENT);
		opciones.add(lblColorResaltado);
		opciones.add(Box.createVerticalStrut(6));

		Color[] colorElegido = {colorResaltado};
		JPanel muestras = new JPanel(new FlowLayout(FlowLayout.CENTER, 7, 5));
		muestras.setOpaque(true);
		muestras.setBackground(colorInput);
		muestras.setBorder(new EmptyBorder(5, 6, 5, 6));
		ButtonGroup grupoColores = new ButtonGroup();
		for (int i = 0; i < COLORES_RESALTADO.length; i++) {
			Color color = COLORES_RESALTADO[i];
			SelectorColor selectorColor = new SelectorColor(color, NOMBRES_RESALTADO[i], color.equals(colorResaltado));
			grupoColores.add(selectorColor);
			muestras.add(selectorColor);
			selectorColor.addActionListener(e -> {
				colorElegido[0] = color;
				interruptor.setColorResaltado(color);
			});
		}
		muestras.setAlignmentX(Component.LEFT_ALIGNMENT);
		opciones.add(muestras);
		opciones.add(Box.createVerticalStrut(10));

		JCheckBox confirmarPassword = new JCheckBox("Confirmar antes de mostrar contraseñas",
				confirmarAntesDeMostrarContrasena);
		confirmarPassword.setOpaque(false);
		confirmarPassword.setForeground(colorTextPrimary);
		confirmarPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		confirmarPassword.setToolTipText("Puedes cambiar esta opción en cualquier momento desde Configuración");
		confirmarPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
		opciones.add(confirmarPassword);
		panel.add(opciones, BorderLayout.CENTER);

		BotonEstilizado btnAplicar = new BotonEstilizado("Aplicar", COLOR_PRIMARY, COLOR_PRIMARY_HOVER, 8);
		btnAplicar.addActionListener(e -> {
			if (establecerPreferencias(interruptor.isSelected(), confirmarPassword.isSelected(), colorElegido[0])) {
				dialogo.dispose();
			}
		});
		JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		acciones.setOpaque(false);
		acciones.add(btnAplicar);
		panel.add(acciones, BorderLayout.SOUTH);

		dialogo.setContentPane(panel);
		dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialogo.setResizable(false);
		dialogo.pack();
		dialogo.setLocationRelativeTo(this);
		dialogo.setVisible(true);
	}

	private static final Color[] COLORES_RESALTADO = {new Color(0, 122, 204), new Color(46, 139, 139),
			new Color(76, 145, 0), new Color(202, 145, 0), new Color(225, 77, 35), new Color(220, 45, 67),
			new Color(184, 65, 165), new Color(112, 91, 205), new Color(94, 119, 102), new Color(164, 129, 88)};
	private static final String[] NOMBRES_RESALTADO = {"Azul", "Turquesa", "Verde", "Amarillo", "Naranja", "Rojo",
			"Rosa", "Violeta", "Verde grisáceo", "Arena"};

	private static class SelectorColor extends JToggleButton {
		private final Color color;

		private SelectorColor(Color color, String nombre, boolean seleccionado) {
			this.color = color;
			setSelected(seleccionado);
			setPreferredSize(new Dimension(30, 30));
			setToolTipText(nombre);
			getAccessibleContext().setAccessibleName(nombre);
			setOpaque(false);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int diameter = 20;
			int x = (getWidth() - diameter) / 2;
			int y = (getHeight() - diameter) / 2;
			if (isSelected()) {
				g2.setColor(color);
				g2.setStroke(new BasicStroke(2.5f));
				g2.drawOval(x - 4, y - 4, diameter + 8, diameter + 8);
			} else if (getModel().isRollover()) {
				g2.setColor(color.brighter());
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawOval(x - 2, y - 2, diameter + 4, diameter + 4);
			}
			g2.setColor(color);
			g2.fillOval(x, y, diameter, diameter);
			g2.dispose();
		}
	}

	private static class InterruptorTema extends JToggleButton {
		private Color colorResaltado;

		private InterruptorTema(boolean seleccionado, Color colorResaltado) {
			this.colorResaltado = colorResaltado;
			setSelected(seleccionado);
			setPreferredSize(new Dimension(52, 30));
			setToolTipText("Cambiar tema");
			setOpaque(false);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		private void setColorResaltado(Color colorResaltado) {
			this.colorResaltado = colorResaltado;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int trackWidth = 46;
			int trackHeight = 24;
			int x = (getWidth() - trackWidth) / 2;
			int y = (getHeight() - trackHeight) / 2;
			Color track = isSelected() ? colorResaltado : new Color(76, 76, 76);
			if (getModel().isRollover()) {
				track = track.brighter();
			}
			g2.setColor(track);
			g2.fillRoundRect(x, y, trackWidth, trackHeight, trackHeight, trackHeight);

			int knobX = isSelected() ? x + trackWidth - 21 : x + 3;
			g2.setColor(new Color(226, 226, 226));
			g2.fillOval(knobX, y + 3, 18, 18);

			if (hasFocus()) {
				g2.setColor(COLOR_TEXTO_SECUNDARIO_OSCURO);
				g2.drawRoundRect(x, y, trackWidth, trackHeight, trackHeight, trackHeight);
			}
			g2.dispose();
		}
	}

	private void aplicarColoresInterfaz() {
		getContentPane().setBackground(colorBg);

		lblTitulo.setForeground(colorTextPrimary);
		lblCuenta.setForeground(colorTextPrimary);
		lblUsuario.setForeground(colorTextPrimary);
		lblPassword.setForeground(colorTextPrimary);
		lblBuscar.setForeground(colorTextPrimary);

		lblUserBadge.setOpaque(false);
		lblUserBadge.setForeground(colorTextMuted);

		panelFormulario.setColores(colorCard, colorBorder);

		for (JComponent comp : componentesEstilizados) {
			comp.setBackground(colorInput);
			comp.setForeground(colorTextPrimary);
			if (comp instanceof JTextField) {
				((JTextField) comp).setCaretColor(colorTextPrimary);
			}
			comp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(colorBorder, 1, true),
					new EmptyBorder(6, 8, 6, 8)));
		}

		estilarTabla();
	}

	private void estilarTabla() {
		tabla.setBackground(colorCard);
		tabla.setForeground(colorTextPrimary);
		tabla.setGridColor(colorBorder);
		tabla.setRowHeight(38);
		tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		tabla.setSelectionBackground(colorSeleccionTabla());
		tabla.setSelectionForeground(colorTextPrimary);
		tabla.setShowGrid(false);
		tabla.setIntercellSpacing(new Dimension(0, 0));

		JTableHeader header = tabla.getTableHeader();
		header.setBackground(colorTono(
				Color.RGBtoHSB(colorResaltado.getRed(), colorResaltado.getGreen(), colorResaltado.getBlue(), null)[0],
				modoOscuro ? 0.34f : 0.16f, modoOscuro ? 0.21f : 0.84f));
		header.setForeground(colorTextMuted);
		header.setFont(new Font("Segoe UI", Font.BOLD, 12));
		header.setPreferredSize(new Dimension(100, 36));
		header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, colorBorder));

		scrollPane.getViewport().setBackground(colorCard);
		scrollPane.setBorder(BorderFactory.createLineBorder(colorBorder, 1, true));

		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, colorBorder),
						new EmptyBorder(0, 10, 0, 10)));
				if (!isSelected) {
					c.setBackground(row % 2 == 0
							? colorCard
							: colorTono(
									Color.RGBtoHSB(colorResaltado.getRed(), colorResaltado.getGreen(),
											colorResaltado.getBlue(), null)[0],
									modoOscuro ? 0.28f : 0.13f, modoOscuro ? 0.32f : 0.86f));
				}
				return c;
			}
		};

		for (int i = 0; i < tabla.getColumnCount(); i++) {
			tabla.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
		}
		tabla.getColumnModel().getColumn(2).setCellRenderer(new PasswordCellRenderer());
		tabla.getColumnModel().getColumn(2).setCellEditor(new PasswordCellEditor());
	}

	private Color colorSeleccionTabla() {
		Color base = colorCard;
		float intensidad = modoOscuro ? 0.38f : 0.22f;
		return new Color(Math.round(base.getRed() * (1 - intensidad) + colorResaltado.getRed() * intensidad),
				Math.round(base.getGreen() * (1 - intensidad) + colorResaltado.getGreen() * intensidad),
				Math.round(base.getBlue() * (1 - intensidad) + colorResaltado.getBlue() * intensidad));
	}

	private class PasswordCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
		private final JLabel lblPassword = new JLabel();
		private final JButton btnVisibilidad = new JButton();

		private PasswordCellRenderer() {
			super(new BorderLayout(8, 0));
			setOpaque(true);
			setBorder(new EmptyBorder(0, 10, 0, 6));
			btnVisibilidad.setFocusable(false);
			btnVisibilidad.setBorderPainted(false);
			estilarBotonIcono(btnVisibilidad);
			btnVisibilidad.setPreferredSize(new Dimension(32, 28));
			add(lblPassword, BorderLayout.CENTER);
			add(btnVisibilidad, BorderLayout.EAST);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			int modelRow = table.convertRowIndexToModel(row);
			String cuenta = modeloTabla.getValueAt(modelRow, 0).toString();
			boolean visible = cuentasConPasswordVisible.contains(cuenta);
			lblPassword.setText(visible ? String.valueOf(value) : ocultarContrasena(String.valueOf(value)));
			btnVisibilidad.setIcon(new FlatSVGIcon(visible ? "icons/eye-off.svg" : "icons/eye.svg", 16, 16));
			btnVisibilidad.setText("");

			Color fondo = isSelected
					? table.getSelectionBackground()
					: (row % 2 == 0
							? colorCard
							: colorTono(
									Color.RGBtoHSB(colorResaltado.getRed(), colorResaltado.getGreen(),
											colorResaltado.getBlue(), null)[0],
									modoOscuro ? 0.28f : 0.13f, modoOscuro ? 0.32f : 0.86f));
			Color texto = isSelected ? table.getSelectionForeground() : colorTextPrimary;
			setBackground(fondo);
			lblPassword.setForeground(texto);
			btnVisibilidad.setForeground(texto);
			lblPassword.setFont(table.getFont());
			return this;
		}
	}

	private class PasswordCellEditor extends AbstractCellEditor implements TableCellEditor {
		private final JPanel panel = new JPanel(new BorderLayout(8, 0));
		private final JLabel lblPassword = new JLabel();
		private final JButton btnVisibilidad = new JButton();
		private String passwordActual;
		private String cuentaActual;

		private PasswordCellEditor() {
			panel.setBorder(new EmptyBorder(0, 10, 0, 6));
			btnVisibilidad.setFocusable(false);
			btnVisibilidad.setBorderPainted(false);
			estilarBotonIcono(btnVisibilidad);
			btnVisibilidad.setPreferredSize(new Dimension(32, 28));
			panel.add(lblPassword, BorderLayout.CENTER);
			panel.add(btnVisibilidad, BorderLayout.EAST);
			btnVisibilidad.addActionListener(e -> {
				if (cuentasConPasswordVisible.contains(cuentaActual)) {
					cuentasConPasswordVisible.remove(cuentaActual);
				} else {
					if (!confirmarVisualizacionContrasena()) {
						fireEditingCanceled();
						return;
					}
					cuentasConPasswordVisible.add(cuentaActual);
				}
				actualizarContenido();
				fireEditingStopped();
			});
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			int modelRow = table.convertRowIndexToModel(row);
			cuentaActual = modeloTabla.getValueAt(modelRow, 0).toString();
			passwordActual = String.valueOf(value);
			actualizarContenido();
			panel.setBackground(table.getSelectionBackground());
			lblPassword.setForeground(table.getSelectionForeground());
			btnVisibilidad.setForeground(table.getSelectionForeground());
			lblPassword.setFont(table.getFont());
			return panel;
		}

		private void actualizarContenido() {
			boolean visible = cuentasConPasswordVisible.contains(cuentaActual);
			lblPassword.setText(visible ? passwordActual : ocultarContrasena(passwordActual));
			btnVisibilidad.setIcon(new FlatSVGIcon(visible ? "icons/eye-off.svg" : "icons/eye.svg", 16, 16));
			btnVisibilidad.setText("");
		}

		@Override
		public Object getCellEditorValue() {
			return passwordActual;
		}
	}

	private String ocultarContrasena(String contrasena) {
		int cantidadCaracteres = contrasena.codePointCount(0, contrasena.length());
		StringBuilder mascara = new StringBuilder(cantidadCaracteres);
		for (int i = 0; i < cantidadCaracteres; i++) {
			mascara.append('•');
		}
		return mascara.toString();
	}

	private boolean existeServicioConCorreo(String cuenta, String usuario, int filaIgnorada) {
		for (int i = 0; i < modeloTabla.getRowCount(); i++) {
			if (i == filaIgnorada)
				continue;
			String cuentaExistente = modeloTabla.getValueAt(i, 0).toString().trim();
			String usuarioExistente = modeloTabla.getValueAt(i, 1).toString().trim();
			if (cuentaExistente.equalsIgnoreCase(cuenta) && usuarioExistente.equalsIgnoreCase(usuario))
				return true;
		}
		return false;
	}

	private void guardarNuevoRegistro() {
		if (!verificarAlmacenamientoDisponible())
			return;
		String cuenta = txtCuenta.getText().trim();
		String usuario = txtUsuario.getText().trim();
		String password = new String(txtPassword.getPassword()).trim();

		if (cuenta.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
			mostrarMensaje("Por favor completa todos los campos.", "Campos Incompletos", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (existeServicioConCorreo(cuenta, usuario, -1)) {
			mostrarMensaje("Ya existe un registro para el servicio '" + cuenta + "' con ese usuario o correo.",
					"Registro duplicado", JOptionPane.WARNING_MESSAGE);
			return;
		}

		modeloTabla.addRow(new Object[]{cuenta, usuario, password});
		guardarEnArchivo();
		limpiarCampos();
	}

	private void actualizarRegistroSeleccionado() {
		if (!verificarAlmacenamientoDisponible())
			return;
		int filaVista = tabla.getSelectedRow();
		if (filaVista == -1) {
			mostrarMensaje("Selecciona un elemento de la lista para editar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		int fila = tabla.convertRowIndexToModel(filaVista);

		String cuenta = txtCuenta.getText().trim();
		String usuario = txtUsuario.getText().trim();
		String password = new String(txtPassword.getPassword()).trim();

		if (cuenta.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
			mostrarMensaje("Por favor completa todos los campos.", "Campos Incompletos", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (existeServicioConCorreo(cuenta, usuario, fila)) {
			mostrarMensaje("Ya existe otro registro para el servicio '" + cuenta + "' con ese usuario o correo.",
					"Registro duplicado", JOptionPane.WARNING_MESSAGE);
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
		if (!verificarAlmacenamientoDisponible())
			return;
		int filaVista = tabla.getSelectedRow();
		if (filaVista != -1) {
			int fila = tabla.convertRowIndexToModel(filaVista);
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
		if (!almacenamientoDisponible)
			return;
		try {
			List<RegistroContrasena> registros = new ArrayList<>();
			for (int i = 0; i < modeloTabla.getRowCount(); i++) {
				registros.add(new RegistroContrasena(modeloTabla.getValueAt(i, 0).toString(),
						modeloTabla.getValueAt(i, 1).toString(), modeloTabla.getValueAt(i, 2).toString()));
			}
			almacenContrasenas.guardar(registros);
		} catch (Exception e) {
			mostrarMensaje("Error al guardar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void cargarDatosGuardados() {
		try {
			almacenContrasenas.cargar(registro -> modeloTabla
					.addRow(new Object[]{registro.getCuenta(), registro.getUsuario(), registro.getPassword()}));
		} catch (IOException e) {
			almacenamientoDisponible = false;
			mostrarMensaje(
					"No se pudo cargar la bóveda: " + e.getMessage()
							+ "\nPor seguridad, se bloquearon las modificaciones para no sobrescribir el archivo.",
					"Error de bóveda", JOptionPane.ERROR_MESSAGE);
		}
	}

	private boolean verificarAlmacenamientoDisponible() {
		if (almacenamientoDisponible)
			return true;
		mostrarMensaje(
				"La bóveda no se cargó correctamente. No se permiten cambios para proteger los datos existentes.",
				"Bóveda bloqueada", JOptionPane.ERROR_MESSAGE);
		return false;
	}

	private JLabel crearEtiqueta(String texto) {
		JLabel label = new JLabel(texto);
		label.setFont(new Font("Segoe UI", Font.BOLD, 12));
		return label;
	}

	private void estilarBotonIcono(JButton boton) {
		boton.setBackground(COLOR_BOTON);
		boton.setForeground(colorTextPrimary);
		boton.setContentAreaFilled(false);
		boton.setOpaque(false);
		boton.setBorderPainted(false);
	}

	private boolean confirmarVisualizacionContrasena() {
		if (!confirmarAntesDeMostrarContrasena) {
			return true;
		}

		JCheckBox noMostrarMas = new JCheckBox("No volver a mostrar este mensaje");
		noMostrarMas.setOpaque(false);
		noMostrarMas.setForeground(colorTextPrimary);
		JPanel contenido = new JPanel(new BorderLayout(0, 12));
		contenido.setOpaque(true);
		contenido.setBackground(colorCard);
		contenido.setBorder(new EmptyBorder(12, 14, 12, 14));
		JLabel mensaje = new JLabel("¿Está seguro que quiere mostrar la contraseña?");
		mensaje.setForeground(colorTextPrimary);
		contenido.add(mensaje, BorderLayout.NORTH);
		contenido.add(noMostrarMas, BorderLayout.SOUTH);

		UIManager.put("OptionPane.background", colorCard);
		UIManager.put("Panel.background", colorCard);
		UIManager.put("OptionPane.messageForeground", colorTextPrimary);
		Object[] opciones = {"Aceptar", "Cancelar"};
		int respuesta = JOptionPane.showOptionDialog(this, contenido, "Mostrar contraseña", JOptionPane.DEFAULT_OPTION,
				JOptionPane.WARNING_MESSAGE, null, opciones, opciones[0]);
		if (respuesta != 0) {
			return false;
		}

		if (noMostrarMas.isSelected()) {
			try {
				AlmacenPreferencias.guardarPreferencias(usuarioLogueado, modoOscuro, false);
				confirmarAntesDeMostrarContrasena = false;
			} catch (IOException e) {
				mostrarMensaje("No se pudo guardar la preferencia: " + e.getMessage(), "Error de configuración",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
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

	public static void main(String[] args) {
		FlatDarkLaf.setup();

		SwingUtilities.invokeLater(() -> {
			DialogoAutenticacion diag = new DialogoAutenticacion(false);
			diag.setVisible(true);
			if (diag.exito) {
				new GestorContrasenas(diag.usuarioAutenticado, diag.claveMaestra).setVisible(true);
			}
		});
	}
}