package gestorcontrasenas.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gestorcontrasenas.modelo.RegistroContrasena;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServicioCredencialesTest {
	private final ServicioCredenciales servicio = new ServicioCredenciales();

	@Test
	void detectaDuplicadosIgnorandoMayusculasYEspacios() {
		List<RegistroContrasena> registros = Collections.singletonList(
				new RegistroContrasena("  Correo ", " Persona@ejemplo.com ", "secreta"));

		assertTrue(servicio.existeDuplicado(registros,
				new RegistroContrasena("correo", "persona@EJEMPLO.com", "otra"), -1));
		assertFalse(servicio.existeDuplicado(registros,
				new RegistroContrasena("correo", "otra@ejemplo.com", "otra"), -1));
	}

	@Test
	void permiteEditarElRegistroIgnoradoSinMarcarloComoDuplicado() {
		List<RegistroContrasena> registros = Arrays.asList(
				new RegistroContrasena("Correo", "persona@ejemplo.com", "secreta"),
				new RegistroContrasena("Banco", "persona@ejemplo.com", "secreta"));

		assertFalse(servicio.existeDuplicado(registros,
				new RegistroContrasena(" correo ", "PERSONA@ejemplo.com", "nueva"), 0));
		assertTrue(servicio.existeDuplicado(registros,
				new RegistroContrasena("BANCO", "persona@ejemplo.com", "nueva"), 0));
	}

	@Test
	void importaNuevosYReemplazaLaClaveExistenteSinAlterarLaListaOriginal() {
		List<RegistroContrasena> actuales = Collections.singletonList(
				new RegistroContrasena("Correo", "persona@ejemplo.com", "anterior"));
		List<RegistroContrasena> importados = Arrays.asList(
				new RegistroContrasena(" correo ", " PERSONA@ejemplo.com ", "actualizada"),
				new RegistroContrasena("Banco", "persona@ejemplo.com", "nueva"));

		ServicioCredenciales.ResultadoImportacion resultado = servicio.combinarImportacion(actuales, importados);

		assertEquals(1, resultado.getAgregados());
		assertEquals(1, resultado.getReemplazados());
		assertEquals(2, resultado.getRegistros().size());
		assertEquals("actualizada", resultado.getRegistros().get(0).getPassword());
		assertEquals("anterior", actuales.get(0).getPassword());
		assertNotSame(actuales, resultado.getRegistros());
	}

	@Test
	void reemplazosRepetidosEnElArchivoSeAplicanEnOrden() {
		List<RegistroContrasena> importados = Arrays.asList(
				new RegistroContrasena("Servicio", "usuario", "primera"),
				new RegistroContrasena("servicio", " USUARIO ", "ultima"));

		ServicioCredenciales.ResultadoImportacion resultado = servicio.combinarImportacion(
				Collections.emptyList(), importados);

		assertEquals(1, resultado.getAgregados());
		assertEquals(1, resultado.getReemplazados());
		assertEquals("ultima", resultado.getRegistros().get(0).getPassword());
	}
}
