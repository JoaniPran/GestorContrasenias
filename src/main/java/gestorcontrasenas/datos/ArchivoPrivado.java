package gestorcontrasenas.datos;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;

final class ArchivoPrivado {
	private static final String PERMISOS_PROPIETARIO = "rw-------";

	private ArchivoPrivado() {
	}

	static void guardarAtomico(Path destino, Escritor escritor) throws IOException {
		Path rutaAbsoluta = destino.toAbsolutePath();
		Path temporal = crearTemporalPrivado(rutaAbsoluta);
		try {
			try (BufferedWriter writer = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
				escritor.escribir(writer);
			}
			restringirPermisos(temporal);
			try {
				Files.move(temporal, rutaAbsoluta, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temporal, rutaAbsoluta, StandardCopyOption.REPLACE_EXISTING);
			}
			restringirPermisos(rutaAbsoluta);
		} finally {
			Files.deleteIfExists(temporal);
		}
	}

	static void restringirPermisos(Path archivo) throws IOException {
		try {
			Files.setPosixFilePermissions(archivo, PosixFilePermissions.fromString(PERMISOS_PROPIETARIO));
		} catch (UnsupportedOperationException ignored) {
			// Sistemas de archivos no POSIX usan los permisos propios de la plataforma.
		}
	}

	private static Path crearTemporalPrivado(Path destino) throws IOException {
		Path directorio = destino.getParent();
		String prefijo = destino.getFileName().toString();
		try {
			return Files.createTempFile(directorio, prefijo, ".tmp", PosixFilePermissions
					.asFileAttribute(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)));
		} catch (UnsupportedOperationException e) {
			Path temporal = Files.createTempFile(directorio, prefijo, ".tmp");
			restringirPermisos(temporal);
			return temporal;
		}
	}

	interface Escritor {
		void escribir(BufferedWriter writer) throws IOException;
	}
}