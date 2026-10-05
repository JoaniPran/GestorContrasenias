package gestorcontrasenas.datos;

import gestorcontrasenas.seguridad.Cifrador;
import gestorcontrasenas.modelo.RegistroContrasena;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

public final class AlmacenContrasenas {
	private static final String MARCADOR_V2 = "KEYVAULT2";
	private static final String AAD_VERSION = "KeyVault:v2";
	private static final int LONGITUD_SAL = 16;
	private static final int LONGITUD_NONCE = 12;
	private static final int MAXIMO_REGISTROS = 100_000;

	private final Path archivoDatosUsuario;
	private final String claveMaestra;
	private byte[] salV2;
	private byte[] claveDerivada;

	public AlmacenContrasenas(String usuarioLogueado, String claveMaestra) {
		String usuarioLimpio = usuarioLogueado.toLowerCase().replaceAll("[^a-zA-Z0-9_]", "");
		this.archivoDatosUsuario = Paths.get("contrasenas_" + usuarioLimpio + ".csv");
		this.claveMaestra = claveMaestra;
	}

	public void guardar(List<RegistroContrasena> registros) throws Exception {
		if (salV2 == null)
			salV2 = Cifrador.generarBytesAleatorios(LONGITUD_SAL);
		if (claveDerivada == null) {
			claveDerivada = Cifrador.derivarClave(claveMaestra, salV2, Cifrador.ITERACIONES_PBKDF2);
		}
		if (registros.size() > MAXIMO_REGISTROS) {
			throw new IOException("La bóveda contiene demasiados registros");
		}

		final int cantidad = registros.size();
		final String salCodificada = Base64.getEncoder().encodeToString(salV2);
		ArchivoPrivado.guardarAtomico(archivoDatosUsuario, writer -> {
			writer.write(MARCADOR_V2 + ";" + Cifrador.ITERACIONES_PBKDF2 + ";" + salCodificada + ";" + cantidad);
			writer.newLine();
			for (int indice = 0; indice < registros.size(); indice++) {
				RegistroContrasena registro = registros.get(indice);
				String contenido = codificarRegistro(registro);
				byte[] nonce = Cifrador.generarBytesAleatorios(LONGITUD_NONCE);
				String aad = crearAad(cantidad, indice);
				byte[] cifrado;
				try {
					cifrado = Cifrador.encriptarGcm(contenido, claveDerivada, nonce, aad);
				} catch (Exception e) {
					throw new IOException("No se pudo cifrar un registro de la bóveda", e);
				}
				writer.write(Base64.getEncoder().encodeToString(nonce));
				writer.write(';');
				writer.write(Base64.getEncoder().encodeToString(cifrado));
				writer.newLine();
			}
		});
	}

	public void cargar(Consumer<RegistroContrasena> alCargarRegistro) throws IOException {
		if (!Files.exists(archivoDatosUsuario))
			return;
		ArchivoPrivado.restringirPermisos(archivoDatosUsuario);
		List<String> lineas = Files.readAllLines(archivoDatosUsuario, StandardCharsets.UTF_8);
		if (!lineas.isEmpty() && lineas.get(0).startsWith(MARCADOR_V2 + ";")) {
			cargarFormatoV2(lineas, alCargarRegistro);
			return;
		}

		List<RegistroContrasena> registros = cargarFormatoLegacy(lineas);
		salV2 = Cifrador.generarBytesAleatorios(LONGITUD_SAL);
		claveDerivada = null;
		try {
			guardar(registros);
		} catch (Exception e) {
			throw new IOException(
					"No se pudo migrar la bóveda al formato cifrado actual; el archivo original se conservó.", e);
		}
		for (RegistroContrasena registro : registros)
			alCargarRegistro.accept(registro);
	}

	private void cargarFormatoV2(List<String> lineas, Consumer<RegistroContrasena> alCargarRegistro)
			throws IOException {
		try {
			String[] cabecera = lineas.get(0).split(";", -1);
			if (cabecera.length != 4 || !MARCADOR_V2.equals(cabecera[0])) {
				throw new IOException("La cabecera de la bóveda no es válida");
			}
			int iteraciones = Integer.parseInt(cabecera[1]);
			int cantidad = Integer.parseInt(cabecera[3]);
			if (cantidad < 0 || cantidad > MAXIMO_REGISTROS || lineas.size() != cantidad + 1) {
				throw new IOException("La cantidad de registros de la bóveda no coincide");
			}

			salV2 = Base64.getDecoder().decode(cabecera[2]);
			if (salV2.length != LONGITUD_SAL)
				throw new IOException("La sal de la bóveda no es válida");
			claveDerivada = Cifrador.derivarClave(claveMaestra, salV2, iteraciones);

			List<RegistroContrasena> registros = new ArrayList<>();
			for (int indice = 0; indice < cantidad; indice++) {
				String[] partes = lineas.get(indice + 1).split(";", -1);
				if (partes.length != 2)
					throw new IOException("Un registro cifrado está mal formado");
				byte[] nonce = Base64.getDecoder().decode(partes[0]);
				byte[] cifrado = Base64.getDecoder().decode(partes[1]);
				if (nonce.length != LONGITUD_NONCE)
					throw new IOException("El nonce de un registro no es válido");
				String claro = Cifrador.desencriptarGcm(cifrado, claveDerivada, nonce, crearAad(cantidad, indice));
				registros.add(decodificarRegistro(claro));
			}
			for (RegistroContrasena registro : registros)
				alCargarRegistro.accept(registro);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("No se pudo autenticar o descifrar la bóveda. El archivo no fue modificado.", e);
		}
	}

	private List<RegistroContrasena> cargarFormatoLegacy(List<String> lineas) throws IOException {
		List<RegistroContrasena> registros = new ArrayList<>();
		try {
			for (String linea : lineas) {
				if (linea.trim().isEmpty())
					continue;
				String claro = Cifrador.desencriptarLegacy(linea.trim(), claveMaestra);
				String[] partes = claro.split(";", 3);
				if (partes.length != 3)
					throw new IOException("Un registro antiguo no tiene tres campos");
				registros.add(new RegistroContrasena(partes[0], partes[1], partes[2]));
				if (registros.size() > MAXIMO_REGISTROS)
					throw new IOException("La bóveda tiene demasiados registros");
			}
			return registros;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("No se pudo descifrar la bóveda antigua. El archivo original se conservó.", e);
		}
	}

	private static String codificarRegistro(RegistroContrasena registro) {
		Base64.Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(registro.getCuenta().getBytes(StandardCharsets.UTF_8)) + ";"
				+ encoder.encodeToString(registro.getUsuario().getBytes(StandardCharsets.UTF_8)) + ";"
				+ encoder.encodeToString(registro.getPassword().getBytes(StandardCharsets.UTF_8));
	}

	private static RegistroContrasena decodificarRegistro(String contenido) throws IOException {
		String[] partes = contenido.split(";", -1);
		if (partes.length != 3)
			throw new IOException("Los campos de un registro no son válidos");
		Base64.Decoder decoder = Base64.getDecoder();
		return new RegistroContrasena(new String(decoder.decode(partes[0]), StandardCharsets.UTF_8),
				new String(decoder.decode(partes[1]), StandardCharsets.UTF_8),
				new String(decoder.decode(partes[2]), StandardCharsets.UTF_8));
	}

	private static String crearAad(int cantidad, int indice) {
		return AAD_VERSION + ":" + cantidad + ":" + indice;
	}
}