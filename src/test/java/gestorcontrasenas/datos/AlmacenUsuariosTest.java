package gestorcontrasenas.datos;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gestorcontrasenas.seguridad.Cifrador;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AlmacenUsuariosTest {
	@TempDir
	Path directorio;

	@Test
	void autenticaLaContrasenaConservandoLosEspaciosDeLosExtremos() {
		Path archivoUsuarios = directorio.resolve("usuarios.csv");
		String contrasena = "  clave maestra  ";

		assertTrue(AlmacenUsuarios.registrarUsuario("persona", contrasena, archivoUsuarios));
		assertTrue(AlmacenUsuarios.validarCredenciales("persona", contrasena, archivoUsuarios));
		assertFalse(AlmacenUsuarios.validarCredenciales("persona", contrasena.trim(), archivoUsuarios));
	}

	@Test
	void lasCuentasAntiguasConservanLaClaveMaestraEfectivaAlMigrar() throws Exception {
		Path archivoUsuarios = directorio.resolve("usuarios.csv");
		Files.write(archivoUsuarios, ("persona;" + Cifrador.hashSHA256("clave antigua") + System.lineSeparator())
				.getBytes(StandardCharsets.UTF_8));

		AlmacenUsuarios.ResultadoAutenticacion resultado = AlmacenUsuarios.autenticar("persona", "  clave antigua  ",
				archivoUsuarios);

		assertEquals("clave antigua", resultado.getClaveMaestra());
		String hashMigrado = Files.readAllLines(archivoUsuarios, StandardCharsets.UTF_8).get(0).split(";", 2)[1];
		assertTrue(hashMigrado.startsWith("pbkdf2-sha256-trim$"));
		assertEquals("clave antigua",
				AlmacenUsuarios.autenticar("persona", "  clave antigua  ", archivoUsuarios).getClaveMaestra());
	}
}
