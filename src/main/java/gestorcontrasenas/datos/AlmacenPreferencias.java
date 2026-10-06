package gestorcontrasenas.datos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;

public final class AlmacenPreferencias {
	private static final Path ARCHIVO_PREFERENCIAS = Paths.get("preferencias.properties");
	private static final String PREFIJO_TEMA = "temaOscuro.";

	private AlmacenPreferencias() {
	}

	public static synchronized boolean cargarModoOscuro(String usuario) {
		if (!Files.exists(ARCHIVO_PREFERENCIAS))
			return true;
		try {
			ArchivoPrivado.restringirPermisos(ARCHIVO_PREFERENCIAS);
			Properties preferencias = new Properties();
			try (java.io.Reader reader = Files.newBufferedReader(ARCHIVO_PREFERENCIAS, StandardCharsets.UTF_8)) {
				preferencias.load(reader);
			}
			return Boolean.parseBoolean(preferencias.getProperty(claveTema(usuario), "true"));
		} catch (IOException e) {
			return true;
		}
	}

	public static synchronized void guardarModoOscuro(String usuario, boolean modoOscuro) throws IOException {
		Properties preferencias = new Properties();
		if (Files.exists(ARCHIVO_PREFERENCIAS)) {
			ArchivoPrivado.restringirPermisos(ARCHIVO_PREFERENCIAS);
			try (java.io.Reader reader = Files.newBufferedReader(ARCHIVO_PREFERENCIAS, StandardCharsets.UTF_8)) {
				preferencias.load(reader);
			}
		}
		preferencias.setProperty(claveTema(usuario), Boolean.toString(modoOscuro));
		ArchivoPrivado.guardarAtomico(ARCHIVO_PREFERENCIAS,
				writer -> preferencias.store(writer, "Preferencias locales de KeyVault"));
	}

	private static String claveTema(String usuario) {
		return PREFIJO_TEMA + usuario.trim().toLowerCase(Locale.ROOT);
	}
}
