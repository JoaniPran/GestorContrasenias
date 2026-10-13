package gestorcontrasenas.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import javax.crypto.AEADBadTagException;
import org.junit.jupiter.api.Test;

class CifradorTest {
	@Test
	void aesGcmCifraYDescifraYRechazaDatosAutenticadosDistintos() throws Exception {
		byte[] clave = Cifrador.generarBytesAleatorios(32);
		byte[] nonce = Cifrador.generarBytesAleatorios(12);
		String texto = "contenido protegido";
		String aad = "boveda:registro:0";
		byte[] cifrado = Cifrador.encriptarGcm(texto, clave, nonce, aad);

		assertEquals(texto, Cifrador.desencriptarGcm(cifrado, clave, nonce, aad));
		assertThrows(AEADBadTagException.class,
				() -> Cifrador.desencriptarGcm(cifrado, clave, nonce, "aad incorrecto"));
	}

	@Test
	void aesGcmDetectaAlteracionesDelTextoCifrado() throws Exception {
		byte[] clave = Cifrador.generarBytesAleatorios(32);
		byte[] nonce = Cifrador.generarBytesAleatorios(12);
		byte[] cifrado = Cifrador.encriptarGcm("contenido", clave, nonce, "contexto");
		cifrado[0] ^= 1;

		assertThrows(AEADBadTagException.class,
				() -> Cifrador.desencriptarGcm(cifrado, clave, nonce, "contexto"));
	}

	@Test
	void hashesNuevosDistinguenContrasenasExactasDeLasNormalizadas() throws Exception {
		String claveConEspacios = "  clave fuerte  ";
		String hashExacto = Cifrador.crearHashPassword(claveConEspacios);
		String hashNormalizado = Cifrador.crearHashPasswordNormalizado(claveConEspacios);

		assertTrue(Cifrador.verificarPassword(claveConEspacios, hashExacto));
		assertFalse(Cifrador.verificarPassword(claveConEspacios.trim(), hashExacto));
		assertTrue(Cifrador.esHashConEspaciosExactos(hashExacto));
		assertTrue(Cifrador.verificarPassword(claveConEspacios.trim(), hashNormalizado));
		assertFalse(Cifrador.esHashConEspaciosExactos(hashNormalizado));
	}

	@Test
	void aceptaFormatoPbkdf2AnteriorYRechazaHashesMalformed() throws Exception {
		byte[] sal = Cifrador.generarBytesAleatorios(16);
		byte[] derivada = Cifrador.derivarClave("clave anterior", sal, Cifrador.ITERACIONES_PBKDF2);
		String hashAnterior = "pbkdf2-sha256$" + Cifrador.ITERACIONES_PBKDF2 + "$"
				+ Base64.getEncoder().encodeToString(sal) + "$" + Base64.getEncoder().encodeToString(derivada);

		assertTrue(Cifrador.verificarPassword("clave anterior", hashAnterior));
		assertFalse(Cifrador.verificarPassword("incorrecta", hashAnterior));
		assertFalse(Cifrador.verificarPassword("clave anterior", "pbkdf2-sha256-raw$0$bad$bad"));
		assertFalse(Cifrador.verificarPassword("clave anterior", "pbkdf2-sha256-raw$abc$bad$bad"));
	}

	@Test
	void rechazaClavesNoncesSalesEIteracionesFueraDeFormato() {
		byte[] clave = Cifrador.generarBytesAleatorios(32);
		byte[] nonce = Cifrador.generarBytesAleatorios(12);
		assertThrows(IllegalArgumentException.class,
				() -> Cifrador.derivarClave("clave", new byte[8], Cifrador.ITERACIONES_PBKDF2));
		assertThrows(IllegalArgumentException.class,
				() -> Cifrador.derivarClave("clave", new byte[16], 1));
		assertThrows(IllegalArgumentException.class,
				() -> Cifrador.encriptarGcm("texto", new byte[16], nonce, "aad"));
		assertThrows(IllegalArgumentException.class,
				() -> Cifrador.encriptarGcm("texto", clave, new byte[8], "aad"));
	}
}
