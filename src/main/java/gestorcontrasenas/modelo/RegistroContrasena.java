package gestorcontrasenas.modelo;

public final class RegistroContrasena {
	private final String cuenta;
	private final String usuario;
	private final String password;

	public RegistroContrasena(String cuenta, String usuario, String password) {
		this.cuenta = cuenta;
		this.usuario = usuario;
		this.password = password;
	}

	public String getCuenta() {
		return cuenta;
	}
	public String getUsuario() {
		return usuario;
	}
	public String getPassword() {
		return password;
	}
}