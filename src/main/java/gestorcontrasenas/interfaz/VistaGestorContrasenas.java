package gestorcontrasenas.interfaz;

import static gestorcontrasenas.interfaz.PaletaColores.COLOR_BOTON;
import static gestorcontrasenas.interfaz.PaletaColores.COLOR_BOTON_HOVER;
import static gestorcontrasenas.interfaz.PaletaColores.COLOR_DANGER;
import static gestorcontrasenas.interfaz.PaletaColores.COLOR_DANGER_HOVER;
import static gestorcontrasenas.interfaz.PaletaColores.COLOR_PRIMARY;
import static gestorcontrasenas.interfaz.PaletaColores.COLOR_PRIMARY_HOVER;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import gestorcontrasenas.servicio.NavegacionTeclado;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.BiFunction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

public final class VistaGestorContrasenas {
	private static final String NOMBRE_APLICACION = "Seguseña";

	private final String usuario;
	private Color colorTarjeta;
	private Color colorBorde;
	private Color colorResaltado;
	private final BiFunction<String, Integer, FlatSVGIcon> crearIcono;
	private final BiPredicate<Integer, Integer> reordenarGrupo;
	private final List<JComponent> componentesEstilizados = new ArrayList<>();
	private ModeloCredenciales modelo;
	private JTable tabla;
	private TableRowSorter<ModeloCredenciales> ordenador;
	private PanelRedondeado panelFormulario;
	private PanelRedondeado panelEstadoBoveda;
	private PanelRedondeado panelTarjetaCredenciales;
	private JLabel lblTitulo;
	private JLabel lblIcono;
	private JLabel lblSubtitulo;
	private JLabel lblUserBadge;
	private JLabel lblCuenta;
	private JLabel lblUsuario;
	private JLabel lblPassword;
	private JLabel lblBuscar;
	private JLabel lblLock;
	private JLabel lblEstadoBoveda;
	private JLabel lblDetalleEstado;
	private JLabel lblContadorCredenciales;
	private JLabel lblTituloCredenciales;
	private JLabel lblTituloFormulario;
	private JButton btnConfiguracion;
	private BotonEstilizado btnGuardar;
	private BotonEstilizado btnActualizar;
	private BotonEstilizado btnEliminar;
	private BotonEstilizado btnLimpiar;
	private BotonEstilizado btnImportarGoogle;
	private BotonEstilizado btnCerrarSesion;
	private JTextField txtCuenta;
	private JTextField txtUsuario;
	private JTextField txtBuscar;
	private JPasswordField txtPassword;
	private JScrollPane scrollPane;
	private JPanel panelPrincipal;

	public VistaGestorContrasenas(String usuario, Color colorTarjeta, Color colorBorde, Color colorResaltado,
			BiFunction<String, Integer, FlatSVGIcon> crearIcono, BiPredicate<Integer, Integer> reordenarGrupo) {
		this.usuario = Objects.requireNonNull(usuario, "usuario");
		this.colorTarjeta = Objects.requireNonNull(colorTarjeta, "colorTarjeta");
		this.colorBorde = Objects.requireNonNull(colorBorde, "colorBorde");
		this.colorResaltado = Objects.requireNonNull(colorResaltado, "colorResaltado");
		this.crearIcono = Objects.requireNonNull(crearIcono, "crearIcono");
		this.reordenarGrupo = Objects.requireNonNull(reordenarGrupo, "reordenarGrupo");
	}

	public JPanel construir() {
		panelPrincipal = new JPanel(new BorderLayout(16, 18));
		panelPrincipal.setBorder(new EmptyBorder(18, 24, 22, 24));

		panelPrincipal.add(crearPanelHeader(), BorderLayout.NORTH);
		panelPrincipal.add(crearPanelContenido(), BorderLayout.CENTER);
		configurarComportamientoTeclado();
		return panelPrincipal;
	}

	private JPanel crearPanelHeader() {
		JPanel panelHeader = new JPanel(new BorderLayout(14, 0));
		panelHeader.setOpaque(false);
		panelHeader.add(crearIdentidadApp(), BorderLayout.WEST);
		panelHeader.add(crearPanelAccionesHeader(), BorderLayout.EAST);
		return panelHeader;
	}

	private JPanel crearIdentidadApp() {
		JPanel identidadApp = new JPanel(new BorderLayout(12, 0));
		identidadApp.setOpaque(false);

		// Icono directo en JLabel sin bordes ni paneles alrededor
		lblIcono = new JLabel(crearIcono.apply("icons/shield.svg", 28));
		lblIcono.setBorder(null);

		JPanel textosApp = new JPanel();
		textosApp.setOpaque(false);
		textosApp.setLayout(new BoxLayout(textosApp, BoxLayout.Y_AXIS));

		lblTitulo = new JLabel(NOMBRE_APLICACION);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 21));

		lblSubtitulo = new JLabel("Tu espacio personal para tus contraseñas");
		lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

		textosApp.add(lblTitulo);
		textosApp.add(Box.createVerticalStrut(3));
		textosApp.add(lblSubtitulo);

		identidadApp.add(lblIcono, BorderLayout.WEST);
		identidadApp.add(textosApp, BorderLayout.CENTER);

		return identidadApp;
	}

	private JPanel crearPanelAccionesHeader() {
		lblUserBadge = new JLabel(" " + usuario + " ");
		lblUserBadge.setIcon(new FlatSVGIcon("icons/user.svg", 14, 14));
		lblUserBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblUserBadge.setOpaque(true);
		lblUserBadge.setForeground(Color.WHITE);

		btnConfiguracion = new JButton();
		btnConfiguracion.setIcon(crearIcono.apply("icons/settings.svg", 16));
		btnConfiguracion.setToolTipText("Configuración");
		btnConfiguracion.setPreferredSize(new Dimension(36, 36));
		btnConfiguracion.setFocusable(true);
		btnConfiguracion.setBorderPainted(true);
		btnConfiguracion.setContentAreaFilled(false);
		btnConfiguracion.setOpaque(false);
		btnConfiguracion.setCursor(new Cursor(Cursor.HAND_CURSOR));

		btnCerrarSesion = new BotonEstilizado("Salir", COLOR_BOTON, COLOR_BOTON_HOVER, 12);

		FlatSVGIcon iconoSalir = crearIcono.apply("icons/external-link.svg", 14);
		if (iconoSalir != null) {
			iconoSalir.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
		}
		btnCerrarSesion.setIcon(iconoSalir);
		btnCerrarSesion.setForeground(Color.WHITE);
		btnCerrarSesion.setFocusable(true);

		JPanel panelAccionesHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		panelAccionesHeader.setOpaque(false);
		panelAccionesHeader.add(lblUserBadge);
		panelAccionesHeader.add(btnConfiguracion);
		panelAccionesHeader.add(btnCerrarSesion);
		return panelAccionesHeader;
	}

	private JPanel crearPanelContenido() {
		JPanel panelContenido = new JPanel(new BorderLayout(12, 12));
		panelContenido.setOpaque(false);
		crearBloqueEstadoBoveda();
		crearBloqueFormulario();
		construirTablaYCredenciales();
		panelContenido.add(crearPanelSuperior(), BorderLayout.NORTH);
		panelContenido.add(panelTarjetaCredenciales, BorderLayout.CENTER);
		return panelContenido;
	}

	private void crearBloqueEstadoBoveda() {
		panelEstadoBoveda = new PanelRedondeado(16, colorTarjeta, colorBorde);
		panelEstadoBoveda.setLayout(new BorderLayout(14, 0));
		panelEstadoBoveda.setBorder(new EmptyBorder(14, 18, 14, 18));
		lblLock = crearEtiqueta("");
		lblLock.setIcon(new FlatSVGIcon("icons/lock.svg", 24, 24));
		JPanel textoEstado = new JPanel();
		textoEstado.setOpaque(false);
		textoEstado.setLayout(new BoxLayout(textoEstado, BoxLayout.Y_AXIS));
		lblEstadoBoveda = new JLabel("Bóveda protegida");
		lblEstadoBoveda.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblDetalleEstado = new JLabel("Cifrado AES-256-GCM · Almacenamiento local");
		lblDetalleEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		textoEstado.add(lblEstadoBoveda);
		textoEstado.add(Box.createVerticalStrut(4));
		textoEstado.add(lblDetalleEstado);
		lblContadorCredenciales = new JLabel("0 credenciales");
		lblContadorCredenciales.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblContadorCredenciales.setBorder(new EmptyBorder(8, 12, 8, 12));
		panelEstadoBoveda.add(lblLock, BorderLayout.WEST);
		panelEstadoBoveda.add(textoEstado, BorderLayout.CENTER);
		panelEstadoBoveda.add(lblContadorCredenciales, BorderLayout.EAST);
	}

	private void crearBloqueFormulario() {
		panelFormulario = new PanelRedondeado(14, colorTarjeta, colorBorde);
		panelFormulario.setLayout(new GridBagLayout());
		panelFormulario.setBorder(new EmptyBorder(14, 16, 14, 16));
		BotonEstilizado[] botonesFormulario = construirFormulario();
		btnGuardar = botonesFormulario[0];
		btnActualizar = botonesFormulario[1];
		btnEliminar = botonesFormulario[2];
		btnLimpiar = botonesFormulario[3];
		btnImportarGoogle = botonesFormulario[4];
	}

	private JPanel crearPanelSuperior() {
		JPanel panelSuperior = new JPanel();
		panelSuperior.setOpaque(false);
		panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
		panelEstadoBoveda.setAlignmentX(Component.LEFT_ALIGNMENT);
		panelFormulario.setAlignmentX(Component.LEFT_ALIGNMENT);
		panelSuperior.add(panelEstadoBoveda);
		panelSuperior.add(Box.createVerticalStrut(12));
		panelSuperior.add(panelFormulario);
		return panelSuperior;
	}

	public void aplicarColoresInterfaz(Color colorFondo, Color colorTarjeta, Color colorEntrada, Color colorTexto,
			Color colorTextoSecundario, Color colorBorde, Color colorResaltado, boolean modoOscuro,
			boolean almacenamientoDisponible, Runnable estilizarTabla) {
		this.colorTarjeta = colorTarjeta;
		this.colorBorde = colorBorde;
		this.colorResaltado = colorResaltado;
		panelPrincipal.setBackground(colorFondo);
		aplicarColoresTexto(colorTexto, colorTextoSecundario, almacenamientoDisponible, modoOscuro);
		panelEstadoBoveda.setColores(colorTarjeta, colorBorde);
		panelFormulario.setColores(colorTarjeta, colorBorde);
		panelTarjetaCredenciales.setColores(colorTarjeta, colorBorde);
		aplicarEstiloCampos(colorEntrada, colorTexto, colorBorde);
		estilizarTabla.run();
	}

	private void aplicarColoresTexto(Color colorTexto, Color colorTextoSecundario, boolean almacenamientoDisponible,
			boolean modoOscuro) {
		lblTitulo.setForeground(colorTexto);
		lblSubtitulo.setForeground(colorTextoSecundario);
		lblTituloFormulario.setForeground(colorTexto);
		lblTituloCredenciales.setForeground(colorTexto);
		lblCuenta.setForeground(colorTexto);
		lblUsuario.setForeground(colorTexto);
		lblPassword.setForeground(colorTexto);
		lblBuscar.setForeground(colorTexto);

		if (lblIcono != null) {
			lblIcono.setForeground(colorTexto);
			lblIcono.setIcon(crearIcono.apply("icons/shield.svg", 28));
		}

		lblLock.setForeground(colorTexto);
		lblEstadoBoveda.setForeground(almacenamientoDisponible ? colorTexto : COLOR_DANGER);
		lblDetalleEstado.setForeground(colorTextoSecundario);
		lblContadorCredenciales.setForeground(colorTextoSecundario);

		lblUserBadge.setOpaque(false);
		Color tintaEncabezado = modoOscuro ? Color.WHITE : Color.BLACK;
		lblUserBadge.setForeground(tintaEncabezado);
		lblUserBadge.setIcon(crearIcono.apply("icons/user.svg", 14));
		btnConfiguracion.setForeground(tintaEncabezado);
		btnConfiguracion.setIcon(crearIcono.apply("icons/settings.svg", 16));
		lblBuscar.setIcon(crearIcono.apply("icons/search.svg", 14));
		lblLock.setForeground(tintaEncabezado);
		lblLock.setIcon(crearIcono.apply("icons/lock.svg", 24));
	}

	private void aplicarEstiloCampos(Color colorEntrada, Color colorTexto, Color colorBorde) {
		for (JComponent componente : componentesEstilizados) {
			componente.setBackground(colorEntrada);
			componente.setForeground(colorTexto);
			if (componente instanceof JTextField) {
				((JTextField) componente).setCaretColor(colorTexto);
			}
			componente.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(colorBorde, 1, true),
					new EmptyBorder(6, 8, 6, 8)));
		}
	}

	private BotonEstilizado[] construirFormulario() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 5, 3, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0;
		lblTituloFormulario = new JLabel("Agregar credencial");
		lblTituloFormulario.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panelFormulario.add(lblTituloFormulario, gbc);

		lblCuenta = crearEtiqueta("Servicio / App:");
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		panelFormulario.add(lblCuenta, gbc);
		lblUsuario = crearEtiqueta("Usuario / Email:");
		gbc.gridx = 1;
		panelFormulario.add(lblUsuario, gbc);
		lblPassword = crearEtiqueta("Contraseña:");
		gbc.gridx = 2;
		panelFormulario.add(lblPassword, gbc);

		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		txtCuenta = crearCampoTexto();
		panelFormulario.add(txtCuenta, gbc);
		gbc.gridx = 1;
		txtUsuario = crearCampoTexto();
		panelFormulario.add(txtUsuario, gbc);
		gbc.gridx = 2;
		txtPassword = crearCampoPassword();
		panelFormulario.add(txtPassword, gbc);

		BotonEstilizado guardar = new BotonEstilizado("Guardar", COLOR_PRIMARY, COLOR_PRIMARY_HOVER, 8);
		guardar.setIcon(new FlatSVGIcon("icons/save.svg", 16, 16));
		BotonEstilizado actualizar = new BotonEstilizado("Editar", COLOR_BOTON, COLOR_BOTON_HOVER, 8);
		actualizar.setIcon(new FlatSVGIcon("icons/edit.svg", 16, 16));
		BotonEstilizado eliminar = new BotonEstilizado("Eliminar", COLOR_DANGER, COLOR_DANGER_HOVER, 8);
		eliminar.setIcon(new FlatSVGIcon("icons/trash.svg", 16, 16));
		BotonEstilizado limpiar = new BotonEstilizado("Limpiar", COLOR_BOTON, COLOR_BOTON_HOVER, 8);
		limpiar.setIcon(new FlatSVGIcon("icons/delete.svg", 16, 16));
		BotonEstilizado importar = new BotonEstilizado("Importar Constraseñas", COLOR_BOTON, COLOR_BOTON_HOVER, 8);
		importar.setIcon(new FlatSVGIcon("icons/upload.svg", 16, 16));

		JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		panelBotones.setOpaque(false);
		panelBotones.add(guardar);
		panelBotones.add(actualizar);
		panelBotones.add(eliminar);
		panelBotones.add(limpiar);
		panelBotones.add(importar);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		gbc.insets = new Insets(8, 4, 2, 4);
		panelFormulario.add(panelBotones, gbc);
		return new BotonEstilizado[]{guardar, actualizar, eliminar, limpiar, importar, btnCerrarSesion};
	}

	private void construirTablaYCredenciales() {
		modelo = new ModeloCredenciales(() -> tabla == null ? 0 : tabla.getSelectedRowCount());
		tabla = crearTabla();
		lblTituloCredenciales = new JLabel("Tus credenciales");
		lblTituloCredenciales.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblBuscar = crearEtiqueta("Buscar");
		lblBuscar.setIcon(new FlatSVGIcon("icons/search.svg", 14, 14));
		txtBuscar = crearCampoTexto();
		txtBuscar.setToolTipText("Filtra por servicio/aplicación o correo electrónico");
		JPanel contenidoCredenciales = new JPanel(new BorderLayout(10, 12));
		contenidoCredenciales.setOpaque(false);
		contenidoCredenciales.setBorder(new EmptyBorder(14, 16, 14, 16));
		contenidoCredenciales.add(crearEncabezadoCredenciales(), BorderLayout.NORTH);
		scrollPane = new JScrollPane(tabla);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		contenidoCredenciales.add(scrollPane, BorderLayout.CENTER);
		panelTarjetaCredenciales = new PanelRedondeado(14, colorTarjeta, colorBorde);
		panelTarjetaCredenciales.setLayout(new BorderLayout());
		panelTarjetaCredenciales.add(contenidoCredenciales, BorderLayout.CENTER);
	}

	private JTable crearTabla() {
		JTable nuevaTabla = new JTable(modelo);
		nuevaTabla.setDragEnabled(true);
		nuevaTabla.setDropMode(DropMode.INSERT_ROWS);
		nuevaTabla.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		nuevaTabla.setTransferHandler(new TableRowTransferHandler(nuevaTabla, reordenarGrupo));
		ordenador = new TableRowSorter<>(modelo);
		for (int columna = 0; columna < modelo.getColumnCount(); columna++) {
			ordenador.setSortable(columna, false);
		}
		nuevaTabla.setRowSorter(ordenador);
		NavegacionTeclado.habilitarSalidaTabla(nuevaTabla);
		return nuevaTabla;
	}

	private JPanel crearEncabezadoCredenciales() {
		JPanel encabezadoCredenciales = new JPanel(new BorderLayout(12, 0));
		encabezadoCredenciales.setOpaque(false);
		encabezadoCredenciales.add(lblTituloCredenciales, BorderLayout.NORTH);
		encabezadoCredenciales.add(crearPanelBusqueda(), BorderLayout.CENTER);
		return encabezadoCredenciales;
	}

	private JPanel crearPanelBusqueda() {
		JPanel panelBusqueda = new JPanel(new BorderLayout(10, 0));
		panelBusqueda.setOpaque(false);
		panelBusqueda.add(lblBuscar, BorderLayout.WEST);
		panelBusqueda.add(txtBuscar, BorderLayout.CENTER);
		return panelBusqueda;
	}

	private JLabel crearEtiqueta(String texto) {
		JLabel etiqueta = new JLabel(texto);
		etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 12));
		return etiqueta;
	}

	private JTextField crearCampoTexto() {
		JTextField campo = new JTextField();
		campo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		componentesEstilizados.add(campo);
		return campo;
	}

	private JPasswordField crearCampoPassword() {
		JPasswordField campo = new JPasswordField();
		campo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		componentesEstilizados.add(campo);
		return campo;
	}

	private void configurarComportamientoTeclado() {
		txtCuenta.addActionListener(e -> btnGuardar.doClick());
		txtUsuario.addActionListener(e -> btnGuardar.doClick());
		txtPassword.addActionListener(e -> btnGuardar.doClick());

		// Vincular el botón "Guardar" como botón predeterminado de la ventana
		SwingUtilities.invokeLater(() -> {
			JRootPane rootPane = panelPrincipal.getRootPane();
			if (rootPane != null) {
				NavegacionTeclado.vincularBotonPorDefecto(rootPane, btnGuardar);
			}
		});
	}

	public ModeloCredenciales getModelo() {
		return modelo;
	}
	public JTable getTabla() {
		return tabla;
	}
	public TableRowSorter<ModeloCredenciales> getOrdenador() {
		return ordenador;
	}
	public JLabel getLblEstadoBoveda() {
		return lblEstadoBoveda;
	}
	public JLabel getLblDetalleEstado() {
		return lblDetalleEstado;
	}
	public JLabel getLblContadorCredenciales() {
		return lblContadorCredenciales;
	}
	public JButton getBtnConfiguracion() {
		return btnConfiguracion;
	}
	public BotonEstilizado getBtnGuardar() {
		return btnGuardar;
	}
	public BotonEstilizado getBtnActualizar() {
		return btnActualizar;
	}
	public BotonEstilizado getBtnEliminar() {
		return btnEliminar;
	}
	public BotonEstilizado getBtnLimpiar() {
		return btnLimpiar;
	}
	public BotonEstilizado getBtnImportarGoogle() {
		return btnImportarGoogle;
	}
	public BotonEstilizado getBtnCerrarSesion() {
		return btnCerrarSesion;
	}
	public JTextField getTxtCuenta() {
		return txtCuenta;
	}
	public JTextField getTxtUsuario() {
		return txtUsuario;
	}
	public JTextField getTxtBuscar() {
		return txtBuscar;
	}
	public JPasswordField getTxtPassword() {
		return txtPassword;
	}
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	public List<JComponent> getComponentesEstilizados() {
		return new ArrayList<>(componentesEstilizados);
	}

	public BiFunction<String, Integer, FlatSVGIcon> getCrearIcono() {
		return crearIcono;
	}
}