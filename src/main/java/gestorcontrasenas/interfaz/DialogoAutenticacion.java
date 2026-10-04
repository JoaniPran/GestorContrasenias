package gestorcontrasenas.interfaz;

import gestorcontrasenas.datos.AlmacenUsuarios;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

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

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(new Color(15, 23, 42));

        JLabel lblTitulo = new JLabel("🛡️ Iniciar Sesión", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        JPanel form = new JPanel(new GridLayout(4, 1, 8, 8));
        form.setOpaque(false);

        JLabel lblU = new JLabel("Usuario:");
        lblU.setForeground(Color.LIGHT_GRAY);
        JTextField txtU = new JTextField();

        JLabel lblP = new JLabel("Contraseña Maestra:");
        lblP.setForeground(Color.LIGHT_GRAY);
        JPasswordField txtP = new JPasswordField();

        form.add(lblU);
        form.add(txtU);
        form.add(lblP);
        form.add(txtP);

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
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnReg.addActionListener(e -> {
            String u = txtU.getText().trim();
            String p = new String(txtP.getPassword()).trim();
            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Completa todos los campos", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!u.matches("[A-Za-z0-9_.-]{1,64}")) {
                JOptionPane.showMessageDialog(this,
                        "El usuario debe tener hasta 64 caracteres: letras, números, punto, guion o guion bajo.",
                        "Usuario no válido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (p.length() < 12) {
                JOptionPane.showMessageDialog(this,
                        "La contraseña maestra debe tener al menos 12 caracteres.",
                        "Contraseña débil", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (AlmacenUsuarios.registrarUsuario(u, p)) {
                JOptionPane.showMessageDialog(this, "Usuario registrado con éxito. Ya puedes entrar.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "El usuario ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        pBtns.add(btnLogin);
        pBtns.add(btnReg);

        root.add(lblTitulo, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(pBtns, BorderLayout.SOUTH);

        setContentPane(root);
    }
}