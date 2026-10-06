package gestorcontrasenas.interfaz;

import static gestorcontrasenas.interfaz.PaletaColores.*;

import gestorcontrasenas.datos.AlmacenUsuarios;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;

public class DialogoRegistroUsuario extends JDialog {
	private final JTextField txtUsuario = new JTextField();
	private final JPasswordField txtContrasena = new JPasswordField();
	private final JPasswordField txtConfirmarContrasena = new JPasswordField();
	private String usuarioRegistrado;

	public DialogoRegistroUsuario(DialogoAutenticacion propietario) {
		super(propietario, "Crear usuario", true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		UIManager.put("OptionPane.background", COLOR_PANEL_OSCURO);
		UIManager.put("Panel.background", COLOR_PANEL_OSCURO);
		UIManager.put("OptionPane.messageForeground", COLOR_TEXTO_OSCURO);

		JPanel root = new JPanel(new BorderLayout(12, 12));
		root.setBorder(new EmptyBorder(18, 18, 18, 18));
		root.setBackground(COLOR_FONDO_OSCURO);

		JLabel titulo = new JLabel("Crear una cuenta");
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
		titulo.setForeground(COLOR_TEXTO_OSCURO);
		root.add(titulo, BorderLayout.NORTH);

		JPanel formulario = new JPanel(new GridLayout(6, 1, 6, 4));
		formulario.setOpaque(false);
		agregarCampo(formulario, "Usuario:", txtUsuario);
		agregarCampo(formulario, "Contraseña maestra:", txtContrasena);
		agregarCampo(formulario, "Confirmar contraseña:", txtConfirmarContrasena);
		root.add(formulario, BorderLayout.CENTER);

		JButton btnCancelar = new BotonEstilizado("Cancelar", COLOR_BOTON, COLOR_BOTON_HOVER, 8);
		JButton btnCrear = new BotonEstilizado("Crear usuario", COLOR_SUCCESS, COLOR_SUCCESS_HOVER, 8);
		btnCancelar.addActionListener(e -> dispose());
		btnCrear.addActionListener(e -> registrarUsuario());

		JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		acciones.setOpaque(false);
		acciones.add(btnCancelar);
		acciones.add(btnCrear);
		root.add(acciones, BorderLayout.SOUTH);

		setContentPane(root);
		setSize(390, 350);
		setResizable(false);
		setLocationRelativeTo(propietario);
	}

	private void agregarCampo(JPanel formulario, String texto, JTextField campo) {
		JLabel etiqueta = new JLabel(texto);
		etiqueta.setForeground(COLOR_TEXTO_SECUNDARIO_OSCURO);
		etiqueta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		campo.setBackground(COLOR_CAMPO_OSCURO);
		campo.setForeground(COLOR_TEXTO_OSCURO);
		campo.setCaretColor(COLOR_TEXTO_OSCURO);
		campo.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(COLOR_BORDE_OSCURO),
				new EmptyBorder(6, 8, 6, 8)));
		formulario.add(etiqueta);
		formulario.add(campo);
	}

	private void registrarUsuario() {
		String usuario = txtUsuario.getText().trim();
		String contrasena = new String(txtContrasena.getPassword()).trim();
		String confirmarContrasena = new String(txtConfirmarContrasena.getPassword()).trim();

		if (usuario.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
			mostrarError("Completa todos los campos.", "Campos incompletos");
			return;
		}
		if (!usuario.matches("[A-Za-z0-9_.-]{1,64}")) {
			mostrarError("El usuario debe tener hasta 64 caracteres: letras, números, punto, guion o guion bajo.",
					"Usuario no válido");
			return;
		}
		if (AlmacenUsuarios.existeUsuario(usuario)) {
			mostrarError("Ya existe un usuario con ese nombre.", "Usuario duplicado");
			return;
		}
		if (contrasena.length() < 12) {
			mostrarError("La contraseña maestra debe tener al menos 12 caracteres.", "Contraseña débil");
			return;
		}
		if (!contrasena.equals(confirmarContrasena)) {
			mostrarError("Las contraseñas no coinciden.", "Confirmación incorrecta");
			return;
		}
		if (!AlmacenUsuarios.registrarUsuario(usuario, contrasena)) {
			mostrarError("No se pudo crear el usuario. Comprueba si ya existe e inténtalo de nuevo.",
					"Error de registro");
			return;
		}

		usuarioRegistrado = usuario;
		JOptionPane.showMessageDialog(this, "Usuario creado. Ya puedes iniciar sesión.", "Registro completado",
				JOptionPane.INFORMATION_MESSAGE);
		dispose();
	}

	private void mostrarError(String mensaje, String titulo) {
		JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.WARNING_MESSAGE);
	}

	public String getUsuarioRegistrado() {
		return usuarioRegistrado;
	}
}
