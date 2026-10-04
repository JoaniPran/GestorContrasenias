package gestorcontrasenas.datos;

import gestorcontrasenas.seguridad.Cifrador;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public final class AlmacenUsuarios {
    private static final String ARCHIVO_USUARIOS = "usuarios.csv";

    private AlmacenUsuarios() {}

    public static boolean existeUsuario(String user) {
        File archivo = new File(ARCHIVO_USUARIOS);
        if (!archivo.exists()) return false;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(";");
                if (partes.length >= 1 && partes[0].equalsIgnoreCase(user.trim())) return true;
            }
        } catch (IOException ignored) {}
        return false;
    }

    public static boolean registrarUsuario(String user, String pass) {
        if (existeUsuario(user)) return false;
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(ARCHIVO_USUARIOS, true), StandardCharsets.UTF_8))) {
            writer.write(user.trim() + ";" + Cifrador.hashSHA256(pass.trim()));
            writer.newLine();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean validarCredenciales(String user, String pass) {
        File archivo = new File(ARCHIVO_USUARIOS);
        if (!archivo.exists()) return false;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(";");
                if (partes.length >= 2) {
                    if (partes[0].equalsIgnoreCase(user.trim())
                            && partes[1].equals(Cifrador.hashSHA256(pass.trim()))) {
                        return true;
                    }
                }
            }
        } catch (IOException ignored) {}
        return false;
    }
}