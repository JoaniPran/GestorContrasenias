package gestorcontrasenas.interfaz;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import gestorcontrasenas.datos.AlmacenUsuarios;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import static gestorcontrasenas.interfaz.PaletaColores.*;

public class DialogoAutenticacion extends JDialog {
	public boolean exito = false;
	public String usuarioAutenticado = "";
	public String claveMaestra = "";

	public DialogoAutenticacion(boolean esRegistro) {
		setTitle("Autenticación - Gestos de Contraseñas");
		setModal(true);
		setSize(360, 320);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		UIManager.put("OptionPane.background", COLOR_PANEL_OSCURO);
		UIManager.put("Panel.background", COLOR_PANEL_OSCURO);
		UIManager.put("OptionPane.messageForeground", COLOR_TEXTO_OSCURO);

		JPanel root = new JPanel(new BorderLayout(10, 10));
		root.setBorder(new EmptyBorder(16, 16, 16, 16));
		root.setBackground(COLOR_FONDO_OSCURO);

		JLabel lblTitulo = new JLabel("Iniciar Sesión", new FlatSVGIcon("icons/shield.svg", 20, 20),
				SwingConstants.CENTER);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
		lblTitulo.setForeground(COLOR_TEXTO_OSCURO);

		JPanel form = new JPanel(new GridBagLayout());
		form.setOpaque(false);

		JLabel lblU = new JLabel("Usuario:");
		lblU.setForeground(COLOR_TEXTO_SECUNDARIO_OSCURO);
		JTextField txtU = new JTextField();
		estilizarCampo(txtU);

		JLabel lblP = new JLabel("Contraseña Maestra:");
		lblP.setForeground(COLOR_TEXTO_SECUNDARIO_OSCURO);
		JPasswordField txtP = new JPasswordField();
		estilizarCampo(txtP);

		agregarCampo(form, lblU, 0);
		agregarCampo(form, txtU, 1);
		agregarCampo(form, lblP, 2);
		agregarCampo(form, txtP, 3);

		JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		pBtns.setOpaque(false);

		JButton btnLogin = new BotonEstilizado("Entrar", COLOR_PRIMARY, COLOR_PRIMARY_HOVER, 8);
		JButton btnReg = new BotonEstilizado("Registrar", COLOR_SUCCESS, COLOR_SUCCESS_HOVER, 8);

		btnLogin.addActionListener(e -> {
			String u = txtU.getText().trim();
			String p = new String(txtP.getPassword()).trim();
			if (AlmacenUsuarios.validarCredenciales(u, p)) {
				usuarioAutenticado = u;
				claveMaestra = p;
				exito = true;
				dispose();
			} else {
				JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		btnReg.addActionListener(e -> {
			DialogoRegistroUsuario dialogoRegistro = new DialogoRegistroUsuario(this);
			dialogoRegistro.setVisible(true);
			if (dialogoRegistro.getUsuarioRegistrado() != null) {
				txtU.setText(dialogoRegistro.getUsuarioRegistrado());
				txtP.setText("");
			}
		});

		pBtns.add(btnLogin);
		pBtns.add(btnReg);

		root.add(lblTitulo, BorderLayout.NORTH);
		root.add(form, BorderLayout.CENTER);
		root.add(pBtns, BorderLayout.SOUTH);

		setContentPane(root);
		setMinimumSize(getSize());
		setResizable(true);
		setLocationRelativeTo(null);
	}

	private void estilizarCampo(JTextField campo) {
		Dimension dimensionesFijas = new Dimension(300, 34);
		campo.setPreferredSize(dimensionesFijas);
		campo.setMinimumSize(dimensionesFijas);
		campo.setMaximumSize(dimensionesFijas);
		campo.setBackground(COLOR_CAMPO_OSCURO);
		campo.setForeground(COLOR_TEXTO_OSCURO);
		campo.setCaretColor(COLOR_TEXTO_OSCURO);
		campo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDE_OSCURO),
				new EmptyBorder(6, 8, 6, 8)));
	}

	private void agregarCampo(JPanel formulario, java.awt.Component componente, int fila) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = fila;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(fila % 2 == 0 ? 4 : 2, 0, fila % 2 == 0 ? 2 : 8, 0);
		formulario.add(componente, gbc);
	}
}