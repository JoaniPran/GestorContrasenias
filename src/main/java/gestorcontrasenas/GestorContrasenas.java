package gestorcontrasenas;

import static gestorcontrasenas.interfaz.PaletaColores.*;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import gestorcontrasenas.datos.AlmacenContrasenas;
import gestorcontrasenas.datos.ImportadorContrasenasGoogle;
import gestorcontrasenas.datos.AlmacenPreferencias;
import gestorcontrasenas.interfaz.BotonEstilizado;
import gestorcontrasenas.interfaz.ColumnaContrasena;
import gestorcontrasenas.interfaz.ControladorEventosTabla;
import gestorcontrasenas.interfaz.ControladorRegistros;
import gestorcontrasenas.interfaz.DialogoAutenticacion;
import gestorcontrasenas.interfaz.DialogoConfiguracion;
import gestorcontrasenas.interfaz.DialogoImportacionGoogle;
import gestorcontrasenas.interfaz.EstiloTablaCredenciales;
import gestorcontrasenas.interfaz.MenusContextuales;
import gestorcontrasenas.interfaz.ModeloCredenciales;
import gestorcontrasenas.interfaz.TemaColores;
import gestorcontrasenas.interfaz.VistaGestorContrasenas;
import gestorcontrasenas.modelo.RegistroContrasena;
import gestorcontrasenas.servicio.ServicioCredenciales;
import gestorcontrasenas.servicio.ServicioCredenciales.ResultadoImportacion;
import gestorcontrasenas.seguridad.ProtectorVentana;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
	private ModeloCredenciales modeloTabla;
	private TableRowSorter<ModeloCredenciales> ordenadorTabla;
	private JLabel lblEstadoBoveda, lblDetalleEstado, lblContadorCredenciales;
	private JButton btnConfiguracion;
	private BotonEstilizado btnImportarGoogle;
	private BotonEstilizado btnCerrarSesion;
	private JScrollPane scrollPane;
	private VistaGestorContrasenas vista;

	private final AlmacenContrasenas almacenContrasenas;
	private final ServicioCredenciales servicioCredenciales = new ServicioCredenciales();
	private final String usuarioLogueado;
	private boolean confirmarAntesDeMostrarContrasena;
	private ProtectorVentana protectorVentana;

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
		vista.aplicarColoresInterfaz(colorBg, colorCard, colorInput, colorTextPrimary, colorTextMuted, colorBorder,
				colorResaltado, modoOscuro, almacenamientoDisponible, this::estilarTabla);
		cargarDatosGuardados();
		protectorVentana = new ProtectorVentana(this, tabla, () -> {
			if (!cuentasConPasswordVisible.isEmpty()) {
				cuentasConPasswordVisible.clear();
				tabla.repaint();
			}
		}, mensaje -> JOptionPane.showMessageDialog(this, mensaje, "Protección de capturas no disponible",
				JOptionPane.WARNING_MESSAGE));
		protectorVentana.configurarProteccionCapturas();
	}

	private void construirInterfaz(String usuarioLogueado) {
		setTitle("Seguseña - " + usuarioLogueado);
		setSize(980, 700);
		setMinimumSize(new Dimension(740, 580));
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		FlatSVGIcon iconoAplicacion = new FlatSVGIcon("icons/shield.svg", 32, 32);
		iconoAplicacion.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE.equals(color)
				? COLOR_PRIMARY : color));
		setIconImage(iconoAplicacion.getImage());
		vista = new VistaGestorContrasenas(usuarioLogueado, colorCard, colorBorder,
				colorResaltado, this::crearIconoEncabezado, this::reordenarGrupoDesdeVista);
		setContentPane(vista.construir());
		modeloTabla = vista.getModelo();
		tabla = vista.getTabla();
		ordenadorTabla = vista.getOrdenador();
		lblEstadoBoveda = vista.getLblEstadoBoveda();
		lblDetalleEstado = vista.getLblDetalleEstado();
		lblContadorCredenciales = vista.getLblContadorCredenciales();
		btnConfiguracion = vista.getBtnConfiguracion();
		btnImportarGoogle = vista.getBtnImportarGoogle();
		btnCerrarSesion = vista.getBtnCerrarSesion();
		txtCuenta = vista.getTxtCuenta();
		txtUsuario = vista.getTxtUsuario();
		txtBuscar = vista.getTxtBuscar();
		txtPassword = vista.getTxtPassword();
		scrollPane = vista.getScrollPane();
		modeloTabla.addTableModelListener(e -> actualizarResumenBoveda());
		actualizarResumenBoveda();
		configurarEventos(vista.getBtnGuardar(), vista.getBtnActualizar(), vista.getBtnEliminar(),
				vista.getBtnLimpiar(), btnImportarGoogle, btnConfiguracion, btnCerrarSesion);
	}

	private void configurarEventos(BotonEstilizado btnGuardar, BotonEstilizado btnActualizar,
			BotonEstilizado btnEliminar, BotonEstilizado btnLimpiar, BotonEstilizado btnImportarGoogle,
			JButton btnConfiguracion,
			BotonEstilizado btnCerrarSesion) {
		ControladorRegistros controlador = new ControladorRegistros(tabla, modeloTabla, txtCuenta, txtUsuario,
				txtPassword, servicioCredenciales, this::verificarAlmacenamientoDisponible,
				this::guardarEnArchivo, new ControladorRegistros.Presentador() {
					@Override
					public void mostrar(String mensaje, String titulo, int tipo) {
						mostrarMensaje(mensaje, titulo, tipo);
					}

					@Override
					public boolean confirmarEliminacion(int cantidad) {
						return JOptionPane.showConfirmDialog(GestorContrasenas.this,
								"¿Eliminar las " + cantidad + " contraseñas seleccionadas?",
								"Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
								== JOptionPane.YES_OPTION;
					}
				});
		MenusContextuales.agregarMenuTexto(txtCuenta);
		MenusContextuales.agregarMenuTexto(txtUsuario);
		MenusContextuales.agregarMenuPassword(txtPassword);
		MenusContextuales.agregarMenuTabla(tabla, 2);

		btnConfiguracion.addActionListener(e -> mostrarConfiguracion());
		btnCerrarSesion.addActionListener(e -> cerrarSesion());
		btnGuardar.addActionListener(e -> controlador.guardarNuevoRegistro());
		btnActualizar.addActionListener(e -> controlador.actualizarRegistroSeleccionado());
		btnEliminar.addActionListener(e -> controlador.eliminarRegistro());
		btnLimpiar.addActionListener(e -> controlador.limpiarCampos());
		btnImportarGoogle.addActionListener(e -> mostrarImportacionGoogle());
		new ControladorEventosTabla(tabla, modeloTabla, txtBuscar, this::actualizarFiltroTabla, registro -> {
			txtCuenta.setText(registro.getCuenta());
			txtUsuario.setText(registro.getUsuario());
			txtPassword.setText(registro.getPassword());
		}, this::alternarGrupoServicio).instalar();
		controlador.instalarEventosSeleccion(btnGuardar, btnActualizar, btnEliminar, btnLimpiar, btnImportarGoogle);

	}

	private void actualizarFiltroTabla() {
		modeloTabla.actualizarFiltro(tabla, ordenadorTabla, txtBuscar.getText());
	}

	private boolean esFilaCabecera(int fila) {
		return modeloTabla.esFilaCabecera(fila);
	}

	private String claveRegistro(int fila) {
		return modeloTabla.claveRegistro(fila);
	}

	private void reconstruirGruposServicios() {
		modeloTabla.reconstruirGruposServicios();
	}

	private boolean reordenarGrupoDesdeVista(int filaOrigenVista, int filaDestinoVista) {
		if (!modeloTabla.reordenarGrupoDesdeVista(tabla, filaOrigenVista, filaDestinoVista)) {
			return false;
		}
		guardarEnArchivo();
		return true;
	}

	private void normalizarOrdenGrupos() {
		modeloTabla.normalizarOrdenGrupos();
	}

	private void alternarGrupoServicio(String servicio) {
		modeloTabla.alternarGrupoServicio(servicio);
		actualizarFiltroTabla();
	}

	private void cerrarSesion() {
		MenusContextuales.limpiarContrasenaPendienteDelPortapapeles();
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
		TemaColores tema = TemaColores.crear(modoOscuro, colorResaltado);
		colorBg = tema.getFondo();
		colorCard = tema.getTarjeta();
		colorInput = tema.getEntrada();
		colorTextPrimary = tema.getTexto();
		colorTextMuted = tema.getTextoSecundario();
		colorBorder = tema.getBorde();
	}

	private FlatSVGIcon crearIconoEncabezado(String recurso, int tamano) {
		Color tinta = modoOscuro ? Color.WHITE : Color.BLACK;
		FlatSVGIcon icono = new FlatSVGIcon(recurso, tamano, tamano);
		icono.setColorFilter(new FlatSVGIcon.ColorFilter(
				color -> Color.WHITE.equals(color) ? tinta : color));
		return icono;
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
		vista.aplicarColoresInterfaz(colorBg, colorCard, colorInput, colorTextPrimary, colorTextMuted, colorBorder,
				colorResaltado, modoOscuro, almacenamientoDisponible, this::estilarTabla);
		repaint();
		revalidate();
		return true;
	}

	private void mostrarConfiguracion() {
		new DialogoConfiguracion(this, modoOscuro, confirmarAntesDeMostrarContrasena, colorResaltado, colorCard,
				colorInput, colorTextPrimary, colorTextMuted, this::establecerPreferencias).mostrar();
	}

	private void actualizarResumenBoveda() {
		int cantidad = 0;
		for (int fila = 0; fila < modeloTabla.getRowCount(); fila++) {
			if (!esFilaCabecera(fila)) {
				cantidad++;
			}
		}
		lblContadorCredenciales.setText(cantidad + (cantidad == 1 ? " credencial" : " credenciales"));
		lblEstadoBoveda.setText(almacenamientoDisponible ? "Bóveda protegida" : "Bóveda bloqueada");
		lblDetalleEstado.setText(almacenamientoDisponible
				? "Cifrado AES-256-GCM · Almacenamiento local"
				: "No se pudo cargar la bóveda; las modificaciones están bloqueadas.");
		lblEstadoBoveda.setForeground(almacenamientoDisponible ? colorTextPrimary : COLOR_DANGER);
	}

	private void estilarTabla() {
		new EstiloTablaCredenciales(colorCard, colorBorder, colorTextPrimary, colorTextMuted,
				colorSeleccionTabla()).aplicar(tabla, scrollPane, modeloTabla);
		ColumnaContrasena.instalar(tabla, new ColumnaContrasena.AccionesContrasena() {
			@Override
			public boolean esEncabezado(int filaModelo) {
				return esFilaCabecera(filaModelo);
			}

			@Override
			public String claveRegistro(int filaModelo) {
				return GestorContrasenas.this.claveRegistro(filaModelo);
			}

			@Override
			public boolean estaVisible(String clave) {
				return cuentasConPasswordVisible.contains(clave);
			}

			@Override
			public void alternarVisibilidad(String clave) {
				if (!cuentasConPasswordVisible.remove(clave)) {
					cuentasConPasswordVisible.add(clave);
				}
			}

			@Override
			public boolean puedeMostrar() {
				return protectorVentana.puedeMostrarContrasenas();
			}

			@Override
			public boolean confirmarVisualizacion() {
				return confirmarVisualizacionContrasena();
			}

			@Override
			public String ocultar(String contrasena) {
				int cantidad = contrasena.codePointCount(0, contrasena.length());
				StringBuilder mascara = new StringBuilder(cantidad);
				for (int i = 0; i < cantidad; i++) {
					mascara.append('•');
				}
				return mascara.toString();
			}

			@Override
			public Color colorAcciones() {
				return modoOscuro ? Color.WHITE : Color.BLACK;
			}

			@Override
			public FlatSVGIcon icono(String recurso, int tamano) {
				return crearIconoEncabezado(recurso, tamano);
			}

			@Override
			public void copiar(String contrasena, JButton boton) {
				MenusContextuales.copiarContrasenaAlPortapapeles(contrasena, boton,
						crearIconoEncabezado("icons/check.svg", 16));
			}
		});
	}

	private Color colorSeleccionTabla() {
		return TemaColores.crear(modoOscuro, colorResaltado).getColorSeleccion(modoOscuro, colorResaltado);
	}

	private void mostrarImportacionGoogle() {
		JDialog dialogo = new JDialog(this, "Importar contraseñas de Google", true);
		new DialogoImportacionGoogle(dialogo, colorCard, colorInput, colorTextPrimary, colorBorder,
				this::importarArchivoGoogle).mostrar();
	}

	private boolean importarArchivoGoogle(Path archivo, JDialog dialogo) {
		if (!archivo.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
			mostrarMensajeEnDialogo(dialogo, "Selecciona un archivo con extensión .csv.",
					"Formato no válido", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		if (!verificarAlmacenamientoDisponible()) {
			dialogo.dispose();
			return false;
		}

		ImportadorContrasenasGoogle.Resultado resultado;
		try {
			resultado = ImportadorContrasenasGoogle.leer(archivo);
		} catch (IOException e) {
			mostrarMensajeEnDialogo(dialogo, e.getMessage(), "CSV no válido", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		List<RegistroContrasena> importados = resultado.getRegistros();
		if (importados.isEmpty()) {
			mostrarMensajeEnDialogo(dialogo,
					"No se encontraron filas con servicio, usuario y contraseña completos."
							+ (resultado.getOmitidos() == 0 ? "" : "\nFilas omitidas: " + resultado.getOmitidos()),
					"Sin contraseñas para importar", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}

		List<RegistroContrasena> registrosActuales = new ArrayList<>();
		for (int fila = 0; fila < modeloTabla.getRowCount(); fila++) {
			if (!esFilaCabecera(fila)) {
				registrosActuales.add(registroEnFila(fila));
			}
		}
		ResultadoImportacion importacion = servicioCredenciales.combinarImportacion(registrosActuales, importados);
		List<RegistroContrasena> registrosActualizados = importacion.getRegistros();

		try {
			almacenContrasenas.guardar(registrosActualizados);
		} catch (Exception e) {
			mostrarMensajeEnDialogo(dialogo, "No se pudo guardar la importación: " + e.getMessage(),
					"Error al guardar", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		modeloTabla.setRowCount(0);
		for (RegistroContrasena registro : registrosActualizados) {
			modeloTabla.addRow(new Object[]{registro.getCuenta(), registro.getUsuario(), registro.getPassword()});
		}
		normalizarOrdenGrupos();
		reconstruirGruposServicios();
		actualizarFiltroTabla();

		mostrarMensajeEnDialogo(dialogo, "Importación completada."
				+ "\nNuevas contraseñas: " + importacion.getAgregados()
				+ "\nContraseñas reemplazadas: " + importacion.getReemplazados()
				+ "\nFilas omitidas por campos vacíos: " + resultado.getOmitidos(),
				"Importación de Google", JOptionPane.INFORMATION_MESSAGE);
		dialogo.dispose();
		return true;
	}

	private void mostrarMensajeEnDialogo(JDialog dialogo, String mensaje, String titulo, int tipo) {
		UIManager.put("OptionPane.background", colorCard);
		UIManager.put("Panel.background", colorCard);
		UIManager.put("OptionPane.messageForeground", colorTextPrimary);
		JOptionPane.showMessageDialog(dialogo, mensaje, titulo, tipo);
	}

	private void guardarEnArchivo() {
		normalizarOrdenGrupos();
		reconstruirGruposServicios();
		actualizarFiltroTabla();
		if (!almacenamientoDisponible)
			return;
		try {
			List<RegistroContrasena> registros = new ArrayList<>();
			for (int i = 0; i < modeloTabla.getRowCount(); i++) {
				if (esFilaCabecera(i)) {
					continue;
				}
				registros.add(new RegistroContrasena(modeloTabla.getValueAt(i, 0).toString(),
						modeloTabla.getValueAt(i, 1).toString(), modeloTabla.getValueAt(i, 2).toString()));
			}
			almacenContrasenas.guardar(registros);
		} catch (Exception e) {
			mostrarMensaje("Error al guardar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private RegistroContrasena registroEnFila(int fila) {
		return new RegistroContrasena(modeloTabla.getValueAt(fila, 0).toString(),
				modeloTabla.getValueAt(fila, 1).toString(), modeloTabla.getValueAt(fila, 2).toString());
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
		normalizarOrdenGrupos();
		reconstruirGruposServicios();
		actualizarFiltroTabla();
		actualizarResumenBoveda();
	}

	private boolean verificarAlmacenamientoDisponible() {
		if (almacenamientoDisponible)
			return true;
		mostrarMensaje(
				"La bóveda no se cargó correctamente. No se permiten cambios para proteger los datos existentes.",
				"Bóveda bloqueada", JOptionPane.ERROR_MESSAGE);
		return false;
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