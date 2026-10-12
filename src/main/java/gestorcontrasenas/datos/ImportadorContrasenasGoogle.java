package gestorcontrasenas.datos;

import gestorcontrasenas.modelo.RegistroContrasena;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ImportadorContrasenasGoogle {
	private static final long TAMANO_MAXIMO_BYTES = 50L * 1024 * 1024;
	private static final int MAXIMO_REGISTROS = 100_000;
	private static final List<String> COLUMNAS_GOOGLE = Arrays.asList("name", "url", "username", "password", "note");

	private ImportadorContrasenasGoogle() {
	}

	public static Resultado leer(Path archivo) throws IOException {
		long tamano = Files.size(archivo);
		if (tamano > TAMANO_MAXIMO_BYTES) {
			throw new IOException("El archivo CSV supera el tamaño máximo de 50 MB.");
		}

		byte[] bytes = Files.readAllBytes(archivo);
		if (bytes.length > TAMANO_MAXIMO_BYTES) {
			throw new IOException("El archivo CSV supera el tamaño máximo de 50 MB.");
		}
		String contenido;
		try {
			contenido = StandardCharsets.UTF_8.newDecoder()
					.onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT)
					.decode(ByteBuffer.wrap(bytes)).toString();
		} catch (CharacterCodingException e) {
			throw new IOException("El archivo no contiene texto UTF-8 válido.", e);
		}
		if (!contenido.isEmpty() && contenido.charAt(0) == '\uFEFF') {
			contenido = contenido.substring(1);
		}

		List<List<String>> filas = analizarCsv(contenido);
		int indiceCabecera = 0;
		while (indiceCabecera < filas.size() && esFilaVacia(filas.get(indiceCabecera))) {
			indiceCabecera++;
		}
		if (indiceCabecera == filas.size()) {
			throw new IOException("El archivo CSV está vacío.");
		}

		List<String> cabeceras = filas.get(indiceCabecera);
		Map<String, Integer> columnas = new HashMap<>();
		for (int indice = 0; indice < cabeceras.size(); indice++) {
			String cabecera = cabeceras.get(indice).trim().toLowerCase(Locale.ROOT);
			if (columnas.put(cabecera, indice) != null) {
				throw new IOException("La columna '" + cabecera + "' está repetida en la cabecera.");
			}
		}
		for (String columna : COLUMNAS_GOOGLE) {
			if (!columnas.containsKey(columna)) {
				throw new IOException("El CSV no tiene la estructura de Google Password Manager. "
						+ "Falta la columna '" + columna + "'.");
			}
		}

		int indiceName = columnas.get("name");
		int indiceUsername = columnas.get("username");
		int indicePassword = columnas.get("password");
		List<RegistroContrasena> registros = new ArrayList<>();
		int omitidos = 0;
		for (int indice = indiceCabecera + 1; indice < filas.size(); indice++) {
			List<String> fila = filas.get(indice);
			if (esFilaVacia(fila)) {
				continue;
			}
			if (fila.size() != cabeceras.size()) {
				throw new IOException("La fila " + (indice + 1) + " tiene " + fila.size()
						+ " campos; se esperaban " + cabeceras.size() + ". No se importó ningún dato.");
			}

			String nombre = fila.get(indiceName).trim();
			String usuario = fila.get(indiceUsername).trim();
			String contrasena = fila.get(indicePassword);
			if (nombre.isEmpty() || usuario.isEmpty() || contrasena.isEmpty()) {
				omitidos++;
				continue;
			}
			if (registros.size() == MAXIMO_REGISTROS) {
				throw new IOException("El CSV contiene más de 100.000 registros válidos.");
			}
			registros.add(new RegistroContrasena(nombre, usuario, contrasena));
		}
		return new Resultado(registros, omitidos);
	}

	private static List<List<String>> analizarCsv(String contenido) throws IOException {
		List<List<String>> filas = new ArrayList<>();
		List<String> fila = new ArrayList<>();
		StringBuilder campo = new StringBuilder();
		boolean entreComillas = false;
		boolean comillasCerradas = false;
		boolean hayContenidoFila = false;

		for (int indice = 0; indice < contenido.length(); indice++) {
			char caracter = contenido.charAt(indice);
			if (entreComillas) {
				if (caracter == '"') {
					if (indice + 1 < contenido.length() && contenido.charAt(indice + 1) == '"') {
						campo.append('"');
						indice++;
					} else {
						entreComillas = false;
						comillasCerradas = true;
					}
				} else {
					campo.append(caracter);
				}
				continue;
			}

			if (comillasCerradas && caracter != ',' && caracter != '\r' && caracter != '\n'
					&& !Character.isWhitespace(caracter)) {
				throw new IOException("El CSV contiene caracteres después de cerrar un campo entre comillas.");
			}
			if (caracter == '"' && campo.length() == 0 && !comillasCerradas) {
				entreComillas = true;
				hayContenidoFila = true;
			} else if (caracter == '"') {
				throw new IOException("El CSV contiene comillas en una posición no válida.");
			} else if (caracter == ',') {
				fila.add(campo.toString());
				campo.setLength(0);
				comillasCerradas = false;
				hayContenidoFila = true;
			} else if (caracter == '\r' || caracter == '\n') {
				fila.add(campo.toString());
				agregarFila(filas, fila, hayContenidoFila);
				fila = new ArrayList<>();
				campo.setLength(0);
				comillasCerradas = false;
				hayContenidoFila = false;
				if (caracter == '\r' && indice + 1 < contenido.length()
						&& contenido.charAt(indice + 1) == '\n') {
					indice++;
				}
			} else {
				if (comillasCerradas && Character.isWhitespace(caracter)) {
					continue;
				}
				campo.append(caracter);
				hayContenidoFila = true;
			}
		}

		if (entreComillas) {
			throw new IOException("El CSV tiene un campo entre comillas sin cerrar.");
		}
		if (hayContenidoFila || campo.length() > 0 || !fila.isEmpty()) {
			fila.add(campo.toString());
			agregarFila(filas, fila, true);
		}
		return filas;
	}

	private static void agregarFila(List<List<String>> filas, List<String> fila, boolean tieneContenido) {
		if (tieneContenido || !esFilaVacia(fila)) {
			filas.add(fila);
		}
	}

	private static boolean esFilaVacia(List<String> fila) {
		return fila.size() == 1 && fila.get(0).trim().isEmpty();
	}

	public static final class Resultado {
		private final List<RegistroContrasena> registros;
		private final int omitidos;

		private Resultado(List<RegistroContrasena> registros, int omitidos) {
			this.registros = new ArrayList<>(registros);
			this.omitidos = omitidos;
		}

		public List<RegistroContrasena> getRegistros() {
			return new ArrayList<>(registros);
		}

		public int getOmitidos() {
			return omitidos;
		}
	}
}
