package gestorcontrasenas.interfaz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JTable;
import org.junit.jupiter.api.Test;

class ModeloCredencialesTest {
	@Test
	void agrupaServiciosDuplicadosYConservaOrdenDeLasCredenciales() {
		ModeloCredenciales modelo = new ModeloCredenciales(() -> 0);
		modelo.addRow(new Object[]{"Correo", "uno", "uno"});
		modelo.addRow(new Object[]{" correo ", "dos", "dos"});
		modelo.addRow(new Object[]{"Banco", "tres", "tres"});

		modelo.normalizarOrdenGrupos();
		modelo.reconstruirGruposServicios();

		assertEquals(4, modelo.getRowCount());
		assertTrue(modelo.esFilaCabecera(0));
		assertEquals("Correo", modelo.getValueAt(0, 0));
		assertEquals("uno", modelo.getValueAt(1, 1));
		assertEquals("dos", modelo.getValueAt(2, 1));
		assertFalse(modelo.esFilaCabecera(3));
		assertEquals(2, modelo.cantidadPorServicio(" CORREO "));
	}

	@Test
	void unaCuentaPorServicioNoCreaEncabezadoDeGrupo() {
		ModeloCredenciales modelo = new ModeloCredenciales(() -> 0);
		modelo.addRow(new Object[]{"Banco", "uno", "clave"});

		modelo.normalizarOrdenGrupos();
		modelo.reconstruirGruposServicios();

		assertEquals(1, modelo.getRowCount());
		assertFalse(modelo.esFilaCabecera(0));
		assertEquals(1, modelo.cantidadPorServicio("Banco"));
	}

	@Test
	void soloLaColumnaDeContrasenaYLasFilasDeCuentaSonEditablesSinSeleccionMultiple() {
		AtomicInteger seleccion = new AtomicInteger();
		ModeloCredenciales modelo = new ModeloCredenciales(seleccion::get);
		modelo.addRow(new Object[]{"Correo", "uno", "uno"});
		modelo.addRow(new Object[]{"Correo", "dos", "dos"});
		modelo.normalizarOrdenGrupos();
		modelo.reconstruirGruposServicios();

		assertFalse(modelo.isCellEditable(0, 2));
		modelo.alternarGrupoServicio("correo");
		assertTrue(modelo.isCellEditable(1, 2));
		seleccion.set(2);
		assertFalse(modelo.isCellEditable(1, 2));
		assertFalse(modelo.isCellEditable(0, 2));
	}

	@Test
	void permiteReordenarGruposDesdeLaTablaSinPerderCredenciales() {
		ModeloCredenciales modelo = new ModeloCredenciales(() -> 0);
		modelo.addRow(new Object[]{"Correo", "uno", "clave1"});
		modelo.addRow(new Object[]{"Correo", "dos", "clave2"});
		modelo.addRow(new Object[]{"Banco", "tres", "clave3"});
		modelo.normalizarOrdenGrupos();
		modelo.reconstruirGruposServicios();
		JTable tabla = new JTable(modelo);

		assertTrue(modelo.reordenarGrupoDesdeVista(tabla, 3, 0));

		assertEquals("Banco", modelo.getValueAt(0, 0));
		assertEquals("Correo", modelo.getValueAt(1, 0));
		assertTrue(modelo.esFilaCabecera(1));
		assertEquals("clave1", modelo.getValueAt(2, 2));
		assertEquals("clave2", modelo.getValueAt(3, 2));
	}
}
