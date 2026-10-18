package gestorcontrasenas.servicio;

import gestorcontrasenas.modelo.RegistroContrasena;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ServicioExportacionTest {

	@Test
	@DisplayName("Debe exportar correctamente encabezado y registros estándar en formato CSV")
	void testExportarACSV_ExitoBasico(@TempDir Path tempDir) throws IOException {
		// Arrange
		File archivoSalida = tempDir.resolve("export_test.csv").toFile();
		List<RegistroContrasena> registros = Arrays.asList(
				new RegistroContrasena("Google", "usuario1@gmail.com", "SecretPass123!"),
				new RegistroContrasena("GitHub", "dev_user", "GitSecret#2026"));

		// Act
		ServicioExportacion.exportarACSV(archivoSalida, registros);

		// Assert
		assertTrue(archivoSalida.exists(), "El archivo CSV debería haberse creado");

		List<String> lineas = Files.readAllLines(archivoSalida.toPath(), StandardCharsets.UTF_8);
		assertEquals(3, lineas.size(), "Debería haber 3 líneas (1 encabezado + 2 registros)");

		// Verificar encabezado estilo Google Passwords
		assertEquals("name,username,password", lineas.get(0));

		// Verificar datos
		assertEquals("Google,usuario1@gmail.com,SecretPass123!", lineas.get(1));
		assertEquals("GitHub,dev_user,GitSecret#2026", lineas.get(2));
	}

	@Test
	@DisplayName("Debe escribir únicamente el encabezado si la lista de registros está vacía")
	void testExportarACSV_ListaVacia(@TempDir Path tempDir) throws IOException {
		// Arrange
		File archivoSalida = tempDir.resolve("export_vacio.csv").toFile();

		// Act
		ServicioExportacion.exportarACSV(archivoSalida, Collections.emptyList());

		// Assert
		List<String> lineas = Files.readAllLines(archivoSalida.toPath(), StandardCharsets.UTF_8);
		assertEquals(1, lineas.size(), "Solo debe contener el encabezado");
		assertEquals("name,username,password", lineas.get(0));
	}

	@Test
	@DisplayName("Debe envolver entre comillas los campos que contienen comas")
	void testExportarACSV_EscaparComas(@TempDir Path tempDir) throws IOException {
		// Arrange
		File archivoSalida = tempDir.resolve("export_comas.csv").toFile();
		List<RegistroContrasena> registros = Arrays
				.asList(new RegistroContrasena("Twitter, Inc.", "user,test", "pass123"));

		// Act
		ServicioExportacion.exportarACSV(archivoSalida, registros);

		// Assert
		List<String> lineas = Files.readAllLines(archivoSalida.toPath(), StandardCharsets.UTF_8);
		assertEquals(2, lineas.size());
		assertEquals("\"Twitter, Inc.\",\"user,test\",pass123", lineas.get(1));
	}

	@Test
	@DisplayName("Debe duplicar y escapar comillas dobles internas según norma RFC 4180")
	void testExportarACSV_EscaparComillasDobles(@TempDir Path tempDir) throws IOException {
		// Arrange
		File archivoSalida = tempDir.resolve("export_comillas.csv").toFile();
		List<RegistroContrasena> registros = Arrays.asList(new RegistroContrasena("App\"Pro\"", "admin", "P\"a\"ss"));

		// Act
		ServicioExportacion.exportarACSV(archivoSalida, registros);

		// Assert
		List<String> lineas = Files.readAllLines(archivoSalida.toPath(), StandardCharsets.UTF_8);
		assertEquals(2, lineas.size());
		assertEquals("\"App\"\"Pro\"\"\",admin,\"P\"\"a\"\"ss\"", lineas.get(1));
	}

	@Test
	@DisplayName("Debe manejar campos nulos sin lanzar NullPointerException")
	void testExportarACSV_ManejoCamposNull(@TempDir Path tempDir) throws IOException {
		// Arrange
		File archivoSalida = tempDir.resolve("export_nulls.csv").toFile();
		List<RegistroContrasena> registros = Arrays.asList(new RegistroContrasena(null, "usuarioOnly", null));

		// Act & Assert
		assertDoesNotThrow(() -> ServicioExportacion.exportarACSV(archivoSalida, registros));

		List<String> lineas = Files.readAllLines(archivoSalida.toPath(), StandardCharsets.UTF_8);
		assertEquals(",usuarioOnly,", lineas.get(1));
	}
}