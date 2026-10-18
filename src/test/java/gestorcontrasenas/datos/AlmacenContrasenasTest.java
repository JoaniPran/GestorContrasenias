package gestorcontrasenas.datos;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import gestorcontrasenas.modelo.RegistroContrasena;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AlmacenContrasenasTest {
	@TempDir
	Path directorio;

	@Test
	void usuariosConNombresQueAntesColisionabanObtienenBovedasSeparadas() throws Exception {
		AlmacenContrasenas anaConPunto = new AlmacenContrasenas("ana.perez", "clave maestra segura", directorio);
		AlmacenContrasenas anaSinPunto = new AlmacenContrasenas("anaperez", "otra clave segura", directorio);
		anaConPunto.guardar(registros("Cuenta A", "usuario-a", "clave-a"));
		anaSinPunto.guardar(registros("Cuenta B", "usuario-b", "clave-b"));

		assertRegistro(cargar(anaConPunto), "Cuenta A", "usuario-a", "clave-a");
		assertRegistro(cargar(anaSinPunto), "Cuenta B", "usuario-b", "clave-b");
	}

	@Test
	void rechazaMigrarUnArchivoLegacyAmbiguoYLoConserva() throws Exception {
		AlmacenContrasenas nueva = new AlmacenContrasenas("ana.perez", "clave maestra segura", directorio);
		nueva.guardar(registros("Cuenta A", "usuario-a", "clave-a"));
		Path archivoNuevo = localizarArchivoUnico();
		Path archivoLegacy = directorio.resolve("contrasenas_anaperez.csv");
		Files.move(archivoNuevo, archivoLegacy, StandardCopyOption.REPLACE_EXISTING);
		Files.write(directorio.resolve("usuarios.csv"),
				("ana.perez;hash-a" + System.lineSeparator() + "anaperez;hash-b" + System.lineSeparator())
						.getBytes(StandardCharsets.UTF_8));

		AlmacenContrasenas almacenAntiguo = new AlmacenContrasenas("ana.perez", "clave maestra segura", directorio);
		assertThrows(IOException.class, () -> almacenAntiguo.cargar(registro -> {
		}));
		assertTrue(Files.exists(archivoLegacy));
	}

	@Test
	void migraElArchivoLegacyCuandoElNombreNoEsAmbiguo() throws Exception {
		AlmacenContrasenas nuevo = new AlmacenContrasenas("ana.perez", "clave maestra segura", directorio);
		nuevo.guardar(registros("Cuenta A", "usuario-a", "clave-a"));
		Path archivoLegacy = directorio.resolve("contrasenas_anaperez.csv");
		Files.move(localizarArchivoUnico(), archivoLegacy, StandardCopyOption.REPLACE_EXISTING);
		Files.write(directorio.resolve("usuarios.csv"), "ana.perez;hash-a".getBytes(StandardCharsets.UTF_8));

		AlmacenContrasenas almacenMigrado = new AlmacenContrasenas("ana.perez", "clave maestra segura", directorio);
		assertRegistro(cargar(almacenMigrado), "Cuenta A", "usuario-a", "clave-a");
		assertTrue(Files.exists(localizarArchivoUnico()));
		assertTrue(Files.exists(archivoLegacy));
	}

	@Test
	void contraseñaIncorrectaNoEntregaRegistrosNiAlteraElArchivo() throws Exception {
		AlmacenContrasenas almacen = new AlmacenContrasenas("persona", "clave correcta", directorio);
		almacen.guardar(registros("Cuenta", "usuario", "secreto"));
		Path archivo = localizarArchivoUnico();
		byte[] original = Files.readAllBytes(archivo);
		AlmacenContrasenas conClaveIncorrecta = new AlmacenContrasenas("persona", "clave incorrecta", directorio);
		List<RegistroContrasena> cargados = new ArrayList<>();

		assertThrows(IOException.class, () -> conClaveIncorrecta.cargar(cargados::add));

		assertTrue(cargados.isEmpty());
		assertArrayEquals(original, Files.readAllBytes(archivo));
	}

	@Test
	void detectarAlteracionDelArchivoNoEntregaRegistrosNiLoReescribe() throws Exception {
		AlmacenContrasenas almacen = new AlmacenContrasenas("persona", "clave correcta", directorio);
		almacen.guardar(registros("Cuenta", "usuario", "secreto"));
		Path archivo = localizarArchivoUnico();
		List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
		String[] campos = lineas.get(1).split(";", -1);
		char primerCaracter = campos[1].charAt(0);
		campos[1] = (primerCaracter == 'A' ? "B" : "A") + campos[1].substring(1);
		lineas.set(1, campos[0] + ";" + campos[1]);
		Files.write(archivo, lineas, StandardCharsets.UTF_8);
		byte[] archivoAlterado = Files.readAllBytes(archivo);
		List<RegistroContrasena> cargados = new ArrayList<>();

		assertThrows(IOException.class,
				() -> new AlmacenContrasenas("persona", "clave correcta", directorio).cargar(cargados::add));

		assertTrue(cargados.isEmpty());
		assertArrayEquals(archivoAlterado, Files.readAllBytes(archivo));
	}

	@Test
	void rechazaExcederElLimiteDeRegistrosAntesDeCrearElArchivo() {
		AlmacenContrasenas almacen = new AlmacenContrasenas("persona", "clave", directorio);
		List<RegistroContrasena> demasiados = Collections.nCopies(100_001,
				new RegistroContrasena("Cuenta", "usuario", "secreto"));

		assertThrows(IOException.class, () -> almacen.guardar(demasiados));
		assertFalse(Files.exists(directorio.resolve("contrasenas_usuario_cGVyc29uYQ.csv")));
	}

	@Test
	void migraContenidoCifradoConElFormatoAesAnterior() throws Exception {
		String clave = "clave antigua";
		String contenidoPlano = "Cuenta antigua;usuario;secreto";
		byte[] claveAes = MessageDigest.getInstance("SHA-256").digest(clave.getBytes(StandardCharsets.UTF_8));
		Cipher cifrador = Cipher.getInstance("AES");
		cifrador.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(claveAes, "AES"));
		String contenidoCifrado = java.util.Base64.getEncoder()
				.encodeToString(cifrador.doFinal(contenidoPlano.getBytes(StandardCharsets.UTF_8)));
		Path archivoLegacy = directorio.resolve("contrasenas_persona.csv");
		Files.write(archivoLegacy, contenidoCifrado.getBytes(StandardCharsets.UTF_8));

		AlmacenContrasenas almacen = new AlmacenContrasenas("persona", clave, directorio);

		assertRegistro(cargar(almacen), "Cuenta antigua", "usuario", "secreto");
		assertTrue(Files.exists(localizarArchivoUnico()));
		assertTrue(Files.exists(archivoLegacy));
	}

	private List<RegistroContrasena> cargar(AlmacenContrasenas almacen) throws IOException {
		List<RegistroContrasena> registros = new ArrayList<>();
		almacen.cargar(registros::add);
		return registros;
	}

	private Path localizarArchivoUnico() throws IOException {
		try (Stream<Path> archivos = Files.list(directorio)) {
			return archivos.filter(path -> path.getFileName().toString().startsWith("contrasenas_usuario_")).findFirst()
					.orElseThrow(() -> new AssertionError("No se creó la bóveda del usuario"));
		}
	}

	private static List<RegistroContrasena> registros(String cuenta, String usuario, String password) {
		List<RegistroContrasena> registros = new ArrayList<>();
		registros.add(new RegistroContrasena(cuenta, usuario, password));
		return registros;
	}

	private static void assertRegistro(List<RegistroContrasena> registros, String cuenta, String usuario,
			String password) {
		assertEquals(1, registros.size());
		assertEquals(cuenta, registros.get(0).getCuenta());
		assertEquals(usuario, registros.get(0).getUsuario());
		assertEquals(password, registros.get(0).getPassword());
	}
}
