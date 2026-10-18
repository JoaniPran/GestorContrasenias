package gestorcontrasenas.servicio;
import gestorcontrasenas.modelo.RegistroContrasena;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ServicioExportacion {

	private ServicioExportacion() {
	}

	public static void exportarACSV(File archivoDestino, List<RegistroContrasena> registros) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(archivoDestino), StandardCharsets.UTF_8))) {

			writer.write("name,username,password");
			writer.newLine();

			for (RegistroContrasena reg : registros) {
				String linea = String.format("%s,%s,%s", escaparCampo(reg.getCuenta()), escaparCampo(reg.getUsuario()),
						escaparCampo(reg.getPassword()));
				writer.write(linea);
				writer.newLine();
			}
		}
	}

	private static String escaparCampo(String campo) {
		if (campo == null)
			return "";
		if (campo.contains(",") || campo.contains("\"") || campo.contains("\n") || campo.contains("\r")) {
			return "\"" + campo.replace("\"", "\"\"") + "\"";
		}
		return campo;
	}
}