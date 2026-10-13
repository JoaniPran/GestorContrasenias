package gestorcontrasenas.datos;

import gestorcontrasenas.seguridad.Cifrador;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class AlmacenUsuarios {
	private static final String ARCHIVO_USUARIOS = "usuarios.csv";

	private AlmacenUsuarios() {
	}

	public static boolean existeUsuario(String user) {
		return existeUsuario(user, Paths.get(ARCHIVO_USUARIOS));
	}

	static boolean existeUsuario(String user, Path archivo) {
		if (!Files.exists(archivo))
			return false;
		try {
			ArchivoPrivado.restringirPermisos(archivo);
			for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
				String[] partes = linea.split(";", 2);
				if (partes.length >= 1 && partes[0].equalsIgnoreCase(user.trim()))
					return true;
			}
		} catch (IOException ignored) {
		}
		return false;
	}

	public static boolean esNombreUsuarioValido(String user) {
		return user != null && user.trim().matches("[A-Za-z0-9_@.-]{1,64}");
	}

	public static boolean registrarUsuario(String user, String pass) {
		return registrarUsuario(user, pass, Paths.get(ARCHIVO_USUARIOS));
	}

	static boolean registrarUsuario(String user, String pass, Path archivo) {
		String usuario = user.trim();
		String contrasena = pass;
		if (!esNombreUsuarioValido(usuario) || contrasena.length() < 12 || existeUsuario(usuario, archivo)) {
			return false;
		}
		try {
			List<String> lineas = Files.exists(archivo)
					? Files.readAllLines(archivo, StandardCharsets.UTF_8)
					: new ArrayList<>();
			lineas.add(usuario + ";" + Cifrador.crearHashPassword(contrasena));
			guardarLineas(lineas, archivo);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean validarCredenciales(String user, String pass) {
		return autenticar(user, pass, Paths.get(ARCHIVO_USUARIOS)) != null;
	}

	static boolean validarCredenciales(String user, String pass, Path archivo) {
		return autenticar(user, pass, archivo) != null;
	}

	public static ResultadoAutenticacion autenticar(String user, String pass) {
		return autenticar(user, pass, Paths.get(ARCHIVO_USUARIOS));
	}

	static ResultadoAutenticacion autenticar(String user, String pass, Path archivo) {
		if (!Files.exists(archivo))
			return null;
		try {
			ArchivoPrivado.restringirPermisos(archivo);
			List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
			for (int i = 0; i < lineas.size(); i++) {
				String[] partes = lineas.get(i).split(";", 2);
				if (partes.length >= 2) {
					String contrasena = Cifrador.esHashConEspaciosExactos(partes[1]) ? pass : pass.trim();
					if (partes[0].equalsIgnoreCase(user.trim())
							&& Cifrador.verificarPassword(contrasena, partes[1])) {
						if (!Cifrador.esHashConEspaciosExactos(partes[1])
								&& !Cifrador.esHashConEspaciosNormalizados(partes[1])) {
							try {
								lineas.set(i, partes[0] + ";" + Cifrador.crearHashPasswordNormalizado(contrasena));
								guardarLineas(lineas, archivo);
							} catch (Exception ignored) {
								// Un fallo al migrar el hash no debe bloquear el acceso al usuario legítimo.
							}
						}
						return new ResultadoAutenticacion(contrasena);
					}
				}
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	static boolean rutaBovedaLegacyCompartida(String usuario, Path archivoUsuarios) throws IOException {
		if (!Files.exists(archivoUsuarios)) {
			return false;
		}
		String rutaLegada = normalizarNombreArchivoLegacy(usuario);
		int usuariosConMismaRuta = 0;
		for (String linea : Files.readAllLines(archivoUsuarios, StandardCharsets.UTF_8)) {
			String[] partes = linea.split(";", 2);
			if (partes.length == 2 && normalizarNombreArchivoLegacy(partes[0]).equals(rutaLegada)) {
				usuariosConMismaRuta++;
				if (usuariosConMismaRuta > 1) {
					return true;
				}
			}
		}
		return false;
	}

	private static String normalizarNombreArchivoLegacy(String usuario) {
		return usuario.trim().toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-zA-Z0-9_]", "");
	}

	private static void guardarLineas(List<String> lineas, Path archivo) throws IOException {
		ArchivoPrivado.guardarAtomico(archivo, writer -> {
			for (String linea : lineas) {
				writer.write(linea);
				writer.newLine();
			}
		});
	}

	public static final class ResultadoAutenticacion {
		private final String claveMaestra;

		private ResultadoAutenticacion(String claveMaestra) {
			this.claveMaestra = claveMaestra;
		}

		public String getClaveMaestra() {
			return claveMaestra;
		}
	}
}