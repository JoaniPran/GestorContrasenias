package gestorcontrasenas.seguridad;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class Cifrador {
	public static final int ITERACIONES_PBKDF2 = 600_000;
	private static final int ITERACIONES_MINIMAS = 100_000;
	private static final int ITERACIONES_MAXIMAS = 2_000_000;
	private static final int LONGITUD_SAL = 16;
	private static final int LONGITUD_NONCE_GCM = 12;
	private static final int LONGITUD_TAG_GCM_BITS = 128;
	private static final SecureRandom RANDOM = new SecureRandom();

	private Cifrador() {
	}

	public static byte[] generarBytesAleatorios(int longitud) {
		if (longitud <= 0)
			throw new IllegalArgumentException("La longitud debe ser positiva");
		byte[] bytes = new byte[longitud];
		RANDOM.nextBytes(bytes);
		return bytes;
	}

	public static String crearHashPassword(String password) throws Exception {
		byte[] salt = generarBytesAleatorios(LONGITUD_SAL);
		byte[] hash = derivarClave(password, salt, ITERACIONES_PBKDF2);
		return "pbkdf2-sha256$" + ITERACIONES_PBKDF2 + "$" + Base64.getEncoder().encodeToString(salt) + "$"
				+ Base64.getEncoder().encodeToString(hash);
	}

	public static boolean verificarPassword(String password, String hashGuardado) {
		if (hashGuardado == null)
			return false;
		if (!hashGuardado.startsWith("pbkdf2-sha256$")) {
			return MessageDigest.isEqual(hashSHA256(password).getBytes(StandardCharsets.US_ASCII),
					hashGuardado.getBytes(StandardCharsets.US_ASCII));
		}

		try {
			String[] partes = hashGuardado.split("\\$", -1);
			if (partes.length != 4 || !"pbkdf2-sha256".equals(partes[0]))
				return false;
			int iteraciones = Integer.parseInt(partes[1]);
			validarIteraciones(iteraciones);
			byte[] salt = Base64.getDecoder().decode(partes[2]);
			byte[] hashEsperado = Base64.getDecoder().decode(partes[3]);
			if (salt.length != LONGITUD_SAL || hashEsperado.length != 32)
				return false;
			byte[] hashCalculado = derivarClave(password, salt, iteraciones);
			return MessageDigest.isEqual(hashEsperado, hashCalculado);
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean esHashModerno(String hashGuardado) {
		return hashGuardado != null && hashGuardado.startsWith("pbkdf2-sha256$");
	}

	public static byte[] derivarClave(String password, byte[] salt, int iteraciones) throws Exception {
		validarIteraciones(iteraciones);
		if (salt == null || salt.length < LONGITUD_SAL) {
			throw new IllegalArgumentException("La sal criptográfica no es válida");
		}
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iteraciones, 256);
		try {
			return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
		} finally {
			spec.clearPassword();
		}
	}

	public static byte[] encriptarGcm(String texto, byte[] clave, byte[] nonce, String datosAutenticados)
			throws Exception {
		validarParametrosGcm(clave, nonce);
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(clave, "AES"),
				new GCMParameterSpec(LONGITUD_TAG_GCM_BITS, nonce));
		cipher.updateAAD(datosAutenticados.getBytes(StandardCharsets.UTF_8));
		return cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
	}

	public static String desencriptarGcm(byte[] cifrado, byte[] clave, byte[] nonce, String datosAutenticados)
			throws Exception {
		validarParametrosGcm(clave, nonce);
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(clave, "AES"),
				new GCMParameterSpec(LONGITUD_TAG_GCM_BITS, nonce));
		cipher.updateAAD(datosAutenticados.getBytes(StandardCharsets.UTF_8));
		return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
	}

	/** Compatibilidad solo para leer el formato anterior sin versión. */
	public static String desencriptarLegacy(String textoEncriptado, String clave) throws Exception {
		byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(clave.getBytes(StandardCharsets.UTF_8));
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, keySpec);
		return new String(cipher.doFinal(Base64.getDecoder().decode(textoEncriptado)), StandardCharsets.UTF_8);
	}

	public static String hashSHA256(String texto) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void validarIteraciones(int iteraciones) {
		if (iteraciones < ITERACIONES_MINIMAS || iteraciones > ITERACIONES_MAXIMAS) {
			throw new IllegalArgumentException("Cantidad de iteraciones PBKDF2 fuera de rango");
		}
	}

	private static void validarParametrosGcm(byte[] clave, byte[] nonce) {
		if (clave == null || clave.length != 32 || nonce == null || nonce.length != LONGITUD_NONCE_GCM) {
			throw new IllegalArgumentException("La clave o el nonce AES-GCM no son válidos");
		}
	}
}