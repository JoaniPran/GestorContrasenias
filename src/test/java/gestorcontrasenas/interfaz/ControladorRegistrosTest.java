package gestorcontrasenas.interfaz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gestorcontrasenas.servicio.ServicioCredenciales;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

class ControladorRegistrosTest {
	@Test
	void guardarRegistroAgregaLaFilaPersisteYLimpiaElFormulario() {
		Fixture fixture = new Fixture();
		fixture.cuenta.setText("Correo");
		fixture.usuario.setText("persona@example.com");
		fixture.contrasena.setText("secreto");

		fixture.controlador.guardarNuevoRegistro();

		assertEquals(1, fixture.modelo.getRowCount());
		assertEquals("Correo", fixture.modelo.getValueAt(0, 0));
		assertEquals("persona@example.com", fixture.modelo.getValueAt(0, 1));
		assertEquals("secreto", fixture.modelo.getValueAt(0, 2));
		assertEquals(1, fixture.guardados.get());
		assertEquals("", fixture.cuenta.getText());
	}

	@Test
	void guardarRegistroConservaLosEspaciosDeLaContrasena() {
		Fixture fixture = new Fixture();
		fixture.cuenta.setText("Correo");
		fixture.usuario.setText("persona@example.com");
		fixture.contrasena.setText("  secreto  ");

		fixture.controlador.guardarNuevoRegistro();

		assertEquals("  secreto  ", fixture.modelo.getValueAt(0, 2));
	}

	@Test
	void guardarRegistroDuplicadoNoModificaLaTablaNiPersiste() {
		Fixture fixture = new Fixture();
		fixture.modelo.addRow(new Object[]{"Correo", "persona@example.com", "anterior"});
		fixture.cuenta.setText(" correo ");
		fixture.usuario.setText("PERSONA@example.com");
		fixture.contrasena.setText("nuevo");

		fixture.controlador.guardarNuevoRegistro();

		assertEquals(1, fixture.modelo.getRowCount());
		assertEquals("anterior", fixture.modelo.getValueAt(0, 2));
		assertEquals(0, fixture.guardados.get());
	}

	@Test
	void eliminarRegistroSeleccionadoActualizaModeloYPersiste() {
		Fixture fixture = new Fixture();
		fixture.modelo.addRow(new Object[]{"Correo", "persona@example.com", "secreto"});
		fixture.tabla.setRowSelectionInterval(0, 0);

		fixture.controlador.eliminarRegistro();

		assertEquals(0, fixture.modelo.getRowCount());
		assertEquals(1, fixture.guardados.get());
	}

	private static final class Fixture {
		private final ModeloCredenciales modelo = new ModeloCredenciales(() -> 0);
		private final JTable tabla = new JTable(modelo);
		private final JTextField cuenta = new JTextField();
		private final JTextField usuario = new JTextField();
		private final JPasswordField contrasena = new JPasswordField();
		private final AtomicInteger guardados = new AtomicInteger();
		private final ControladorRegistros controlador = new ControladorRegistros(tabla, modelo, cuenta, usuario,
				contrasena, new ServicioCredenciales(), () -> true, guardados::incrementAndGet,
				new ControladorRegistros.Presentador() {
					@Override
					public void mostrar(String mensaje, String titulo, int tipo) {
					}

					@Override
					public boolean confirmarEliminacion(int cantidad) {
						return true;
					}
				});
	}
}
