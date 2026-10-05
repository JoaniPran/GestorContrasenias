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
		Path archivo = Paths.get(ARCHIVO_USUARIOS);
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

	public static boolean registrarUsuario(String user, String pass) {
		String usuario = user.trim();
		String contrasena = pass.trim();
		if (!usuario.matches("[A-Za-z0-9_.-]{1,64}") || contrasena.length() < 12 || existeUsuario(usuario)) {
			return false;
		}
		try {
			List<String> lineas = Files.exists(Paths.get(ARCHIVO_USUARIOS))
					? Files.readAllLines(Paths.get(ARCHIVO_USUARIOS), StandardCharsets.UTF_8)
					: new ArrayList<>();
			lineas.add(usuario + ";" + Cifrador.crearHashPassword(contrasena));
			guardarLineas(lineas);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean validarCredenciales(String user, String pass) {
		Path archivo = Paths.get(ARCHIVO_USUARIOS);
		if (!Files.exists(archivo))
			return false;
		String contrasena = pass.trim();
		try {
			ArchivoPrivado.restringirPermisos(archivo);
			List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
			for (int i = 0; i < lineas.size(); i++) {
				String[] partes = lineas.get(i).split(";", 2);
				if (partes.length >= 2) {
					if (partes[0].equalsIgnoreCase(user.trim()) && Cifrador.verificarPassword(contrasena, partes[1])) {
						if (!Cifrador.esHashModerno(partes[1])) {
							try {
								lineas.set(i, partes[0] + ";" + Cifrador.crearHashPassword(contrasena));
								guardarLineas(lineas);
							} catch (Exception ignored) {
								// Un fallo al migrar el hash no debe bloquear el acceso al usuario legítimo.
							}
						}
						return true;
					}
				}
			}
		} catch (Exception ignored) {
		}
		return false;
	}

	private static void guardarLineas(List<String> lineas) throws IOException {
		ArchivoPrivado.guardarAtomico(Paths.get(ARCHIVO_USUARIOS), writer -> {
			for (String linea : lineas) {
				writer.write(linea);
				writer.newLine();
			}
		});
	}
}