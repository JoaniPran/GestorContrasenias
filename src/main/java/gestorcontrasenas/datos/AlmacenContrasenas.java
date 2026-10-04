package gestorcontrasenas.datos;

import gestorcontrasenas.seguridad.Cifrador;
import gestorcontrasenas.modelo.RegistroContrasena;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

public final class AlmacenContrasenas {
    private final String archivoDatosUsuario;
    private final String claveMaestra;

    public AlmacenContrasenas(String usuarioLogueado, String claveMaestra) {
        String usuarioLimpio = usuarioLogueado.toLowerCase().replaceAll("[^a-zA-Z0-9_]", "");
        this.archivoDatosUsuario = "contrasenas_" + usuarioLimpio + ".csv";
        this.claveMaestra = claveMaestra;
    }

    public void guardar(List<RegistroContrasena> registros) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(archivoDatosUsuario), StandardCharsets.UTF_8))) {
            for (RegistroContrasena registro : registros) {
                String cifrado = Cifrador.encriptar(registro.getCuenta() + ";" + registro.getUsuario()
                        + ";" + registro.getPassword(), claveMaestra);
                writer.write(cifrado);
                writer.newLine();
            }
        }
    }

    public void cargar(Consumer<RegistroContrasena> alCargarRegistro) throws IOException {
        File archivo = new File(archivoDatosUsuario);
        if (!archivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                try {
                    String descifrado = Cifrador.desencriptar(linea.trim(), claveMaestra);
                    String[] partes = descifrado.split(";");
                    if (partes.length == 3) {
                        alCargarRegistro.accept(new RegistroContrasena(partes[0], partes[1], partes[2]));
                    }
                } catch (Exception ignored) {}
            }
        }
    }
}