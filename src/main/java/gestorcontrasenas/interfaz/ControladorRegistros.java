package gestorcontrasenas.interfaz;

import gestorcontrasenas.modelo.RegistroContrasena;
import gestorcontrasenas.servicio.ServicioCredenciales;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;

public final class ControladorRegistros {
	private final JTable tabla;
	private final ModeloCredenciales modelo;
	private final JTextField cuenta;
	private final JTextField usuario;
	private final JPasswordField contrasena;
	private final ServicioCredenciales servicio;
	private final AccesoBoveda accesoBoveda;
	private final Runnable guardarCambios;
	private final Presentador presentador;

	public ControladorRegistros(JTable tabla, ModeloCredenciales modelo, JTextField cuenta,
			JTextField usuario, JPasswordField contrasena, ServicioCredenciales servicio, AccesoBoveda accesoBoveda,
			Runnable guardarCambios, Presentador presentador) {
		this.tabla = Objects.requireNonNull(tabla, "tabla");
		this.modelo = Objects.requireNonNull(modelo, "modelo");
		this.cuenta = Objects.requireNonNull(cuenta, "cuenta");
		this.usuario = Objects.requireNonNull(usuario, "usuario");
		this.contrasena = Objects.requireNonNull(contrasena, "contrasena");
		this.servicio = Objects.requireNonNull(servicio, "servicio");
		this.accesoBoveda = Objects.requireNonNull(accesoBoveda, "accesoBoveda");
		this.guardarCambios = Objects.requireNonNull(guardarCambios, "guardarCambios");
		this.presentador = Objects.requireNonNull(presentador, "presentador");
	}

	public void instalarEventosSeleccion(JButton guardar, JButton actualizar, JButton eliminar, JButton limpiar,
			JButton importar) {
		tabla.getSelectionModel().addListSelectionListener(evento -> {
			if (!evento.getValueIsAdjusting()) {
				actualizarAcciones(guardar, actualizar, eliminar, limpiar, importar);
			}
		});
	}

	public void guardarNuevoRegistro() {
		if (!accesoBoveda.verificar()) {
			return;
		}
		RegistroContrasena registro = leerFormulario();
		if (registro == null) {
			return;
		}
		if (existeDuplicado(registro, -1)) {
			presentador.mostrar("Ya existe un registro para el servicio '" + registro.getCuenta()
					+ "' con ese usuario o correo.", "Registro duplicado", JOptionPane.WARNING_MESSAGE);
			return;
		}
		modelo.addRow(new Object[]{registro.getCuenta(), registro.getUsuario(), registro.getPassword()});
		guardarCambios.run();
		limpiarCampos();
	}

	public void actualizarRegistroSeleccionado() {
		if (!accesoBoveda.verificar()) {
			return;
		}
		int filaVista = tabla.getSelectedRow();
		if (filaVista < 0) {
			presentador.mostrar("Selecciona un elemento de la lista para editarla.",
					"Aviso", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		int fila = tabla.convertRowIndexToModel(filaVista);
		if (modelo.esFilaCabecera(fila)) {
			presentador.mostrar("Despliega el grupo y selecciona una cuenta para editarla.",
					"Grupo contraído", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		RegistroContrasena registro = leerFormulario();
		if (registro == null) {
			return;
		}
		if (existeDuplicado(registro, fila)) {
			presentador.mostrar("Ya existe otro registro para el servicio '" + registro.getCuenta()
					+ "' con ese usuario o correo.", "Registro duplicado", JOptionPane.WARNING_MESSAGE);
			return;
		}
		modelo.setValueAt(registro.getCuenta(), fila, 0);
		modelo.setValueAt(registro.getUsuario(), fila, 1);
		modelo.setValueAt(registro.getPassword(), fila, 2);
		guardarCambios.run();
		limpiarCampos();
		presentador.mostrar("Registro actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
	}

	public void eliminarRegistro() {
		if (!accesoBoveda.verificar()) {
			return;
		}
		int[] filasVista = tabla.getSelectedRows();
		if (filasVista.length > 1) {
			List<Integer> filasModelo = new ArrayList<>();
			for (int filaVista : filasVista) {
				int fila = tabla.convertRowIndexToModel(filaVista);
				if (!modelo.esFilaCabecera(fila)) {
					filasModelo.add(fila);
				}
			}
			if (filasModelo.isEmpty()) {
				presentador.mostrar("Selecciona al menos una contraseña para eliminar.",
						"Aviso", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			if (!presentador.confirmarEliminacion(filasModelo.size())) {
				return;
			}
			filasModelo.sort(Collections.reverseOrder());
			for (int fila : filasModelo) {
				modelo.removeRow(fila);
			}
			guardarCambios.run();
			limpiarCampos();
			return;
		}

		if (filasVista.length == 0) {
			presentador.mostrar("Selecciona una fila para eliminar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		int fila = tabla.convertRowIndexToModel(filasVista[0]);
		if (modelo.esFilaCabecera(fila)) {
			presentador.mostrar("Despliega el grupo y selecciona una cuenta para eliminarla.",
					"Grupo contraído", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		modelo.removeRow(fila);
		guardarCambios.run();
		limpiarCampos();
	}

	public void limpiarCampos() {
		limpiarFormulario();
		tabla.clearSelection();
	}

	private void limpiarFormulario() {
		cuenta.setText("");
		usuario.setText("");
		contrasena.setText("");
	}

	private void actualizarAcciones(JButton guardar, JButton actualizar, JButton eliminar, JButton limpiar,
			JButton importar) {
		boolean seleccionMultiple = tabla.getSelectedRowCount() > 1;
		if (seleccionMultiple) {
			limpiarFormulario();
		}
		cuenta.setEnabled(!seleccionMultiple);
		usuario.setEnabled(!seleccionMultiple);
		contrasena.setEnabled(!seleccionMultiple);
		guardar.setEnabled(!seleccionMultiple);
		actualizar.setEnabled(!seleccionMultiple);
		eliminar.setEnabled(true);
		limpiar.setEnabled(!seleccionMultiple);
		importar.setEnabled(!seleccionMultiple);
	}

	private RegistroContrasena leerFormulario() {
		String cuentaActual = cuenta.getText().trim();
		String usuarioActual = usuario.getText().trim();
		char[] passwordChars = contrasena.getPassword();
		try {
			String passwordActual = new String(passwordChars);
			if (cuentaActual.isEmpty() || usuarioActual.isEmpty() || passwordActual.isEmpty()) {
				presentador.mostrar("Por favor completa todos los campos.",
						"Campos Incompletos", JOptionPane.WARNING_MESSAGE);
				return null;
			}
			return new RegistroContrasena(cuentaActual, usuarioActual, passwordActual);
		} finally {
			Arrays.fill(passwordChars, '\0');
		}
	}

	private boolean existeDuplicado(RegistroContrasena candidato, int filaIgnorada) {
		List<RegistroContrasena> registros = new ArrayList<>();
		for (int fila = 0; fila < modelo.getRowCount(); fila++) {
			if (!modelo.esFilaCabecera(fila) && fila != filaIgnorada) {
				registros.add(modelo.registroEnFila(fila));
			}
		}
		return servicio.existeDuplicado(registros, candidato, -1);
	}

	@FunctionalInterface
	public interface AccesoBoveda {
		boolean verificar();
	}

	public interface Presentador {
		void mostrar(String mensaje, String titulo, int tipo);
		boolean confirmarEliminacion(int cantidad);
	}
}
