package gestorcontrasenas.datos;

import java.io.IOException;
import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;

public final class AlmacenPreferencias {
	private static final Path ARCHIVO_PREFERENCIAS = Paths.get("preferencias.properties");
	private static final String PREFIJO_TEMA = "temaOscuro.";
	private static final String PREFIJO_CONFIRMAR_PASSWORD = "confirmarMostrarContrasena.";
	private static final String PREFIJO_COLOR_RESALTADO = "colorResaltado.";
	private static final Color COLOR_RESALTADO_PREDETERMINADO = new Color(0, 122, 204);

	private AlmacenPreferencias() {
	}

	public static synchronized boolean cargarModoOscuro(String usuario) {
		return cargarPreferencia(claveTema(usuario), true);
	}

	public static synchronized boolean cargarConfirmacionMostrarContrasena(String usuario) {
		return cargarPreferencia(claveConfirmacionPassword(usuario), true);
	}

	public static synchronized Color cargarColorResaltado(String usuario) {
		if (!Files.exists(ARCHIVO_PREFERENCIAS)) {
			return COLOR_RESALTADO_PREDETERMINADO;
		}
		try {
			ArchivoPrivado.restringirPermisos(ARCHIVO_PREFERENCIAS);
			Properties preferencias = new Properties();
			try (java.io.Reader reader = Files.newBufferedReader(ARCHIVO_PREFERENCIAS, StandardCharsets.UTF_8)) {
				preferencias.load(reader);
			}
			String valor = preferencias.getProperty(claveColorResaltado(usuario));
			if (valor == null || !valor.matches("[0-9a-fA-F]{6}")) {
				return COLOR_RESALTADO_PREDETERMINADO;
			}
			return new Color(Integer.parseInt(valor, 16));
		} catch (IOException | NumberFormatException e) {
			return COLOR_RESALTADO_PREDETERMINADO;
		}
	}

	private static boolean cargarPreferencia(String clave, boolean valorPredeterminado) {
		if (!Files.exists(ARCHIVO_PREFERENCIAS))
			return valorPredeterminado;
		try {
			ArchivoPrivado.restringirPermisos(ARCHIVO_PREFERENCIAS);
			Properties preferencias = new Properties();
			try (java.io.Reader reader = Files.newBufferedReader(ARCHIVO_PREFERENCIAS, StandardCharsets.UTF_8)) {
				preferencias.load(reader);
			}
			return Boolean.parseBoolean(preferencias.getProperty(clave, Boolean.toString(valorPredeterminado)));
		} catch (IOException e) {
			return valorPredeterminado;
		}
	}

	public static synchronized void guardarModoOscuro(String usuario, boolean modoOscuro) throws IOException {
		Properties preferencias = cargarPropiedades();
		preferencias.setProperty(claveTema(usuario), Boolean.toString(modoOscuro));
		guardarPropiedades(preferencias);
	}

	public static synchronized void guardarPreferencias(String usuario, boolean modoOscuro,
			boolean confirmarMostrarContrasena) throws IOException {
		Properties preferencias = cargarPropiedades();
		preferencias.setProperty(claveTema(usuario), Boolean.toString(modoOscuro));
		preferencias.setProperty(claveConfirmacionPassword(usuario), Boolean.toString(confirmarMostrarContrasena));
		guardarPropiedades(preferencias);
	}

	public static synchronized void guardarPreferencias(String usuario, boolean modoOscuro,
			boolean confirmarMostrarContrasena, Color colorResaltado) throws IOException {
		Properties preferencias = cargarPropiedades();
		preferencias.setProperty(claveTema(usuario), Boolean.toString(modoOscuro));
		preferencias.setProperty(claveConfirmacionPassword(usuario), Boolean.toString(confirmarMostrarContrasena));
		preferencias.setProperty(claveColorResaltado(usuario),
				String.format(Locale.ROOT, "%06x", colorResaltado.getRGB() & 0xFFFFFF));
		guardarPropiedades(preferencias);
	}

	private static Properties cargarPropiedades() throws IOException {
		Properties preferencias = new Properties();
		if (Files.exists(ARCHIVO_PREFERENCIAS)) {
			ArchivoPrivado.restringirPermisos(ARCHIVO_PREFERENCIAS);
			try (java.io.Reader reader = Files.newBufferedReader(ARCHIVO_PREFERENCIAS, StandardCharsets.UTF_8)) {
				preferencias.load(reader);
			}
		}
		return preferencias;
	}

	private static void guardarPropiedades(Properties preferencias) throws IOException {
		ArchivoPrivado.guardarAtomico(ARCHIVO_PREFERENCIAS,
				writer -> preferencias.store(writer, "Preferencias locales de KeyVault"));
	}

	private static String claveTema(String usuario) {
		return PREFIJO_TEMA + usuario.trim().toLowerCase(Locale.ROOT);
	}

	private static String claveConfirmacionPassword(String usuario) {
		return PREFIJO_CONFIRMAR_PASSWORD + usuario.trim().toLowerCase(Locale.ROOT);
	}

	private static String claveColorResaltado(String usuario) {
		return PREFIJO_COLOR_RESALTADO + usuario.trim().toLowerCase(Locale.ROOT);
	}
}
