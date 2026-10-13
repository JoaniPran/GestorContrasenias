package gestorcontrasenas.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CifradorPasswordTest {

    @Test
    void verificarPasswordConCharArrayDebeCoincidir() throws Exception {
        char[] clave = "MiPasswordSegura!2026".toCharArray();
        String hash = Cifrador.crearHashPassword(clave);
        assertTrue(Cifrador.verificarPassword(clave, hash));
        java.util.Arrays.fill(clave, '\0');
    }

    @Test
    void crearHashPasswordDebeSerReproducibleParaLaMismaClave() throws Exception {
        char[] clave = "OtraClaveMuySegura123".toCharArray();
        String hash1 = Cifrador.crearHashPassword(clave);
        String hash2 = Cifrador.crearHashPassword(clave);
        assertEquals(hash1.split("\\$")[0], hash2.split("\\$")[0]);
        assertTrue(hash1.startsWith("pbkdf2-sha256-raw$"));
        java.util.Arrays.fill(clave, '\0');
    }
}
