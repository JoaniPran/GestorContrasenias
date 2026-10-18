package gestorcontrasenas.interfaz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public final class Dialogos {
	private Dialogos() {
	}

	public static void configurarEstilo(Color fondo, Color texto) {
		UIManager.put("OptionPane.background", fondo);
		UIManager.put("Panel.background", fondo);
		UIManager.put("OptionPane.messageForeground", texto);
	}

	public static void mostrarMensaje(Component padre, String mensaje, String titulo, int tipo, Color fondo,
			Color texto) {
		configurarEstilo(fondo, texto);
		JOptionPane.showMessageDialog(padre, mensaje, titulo, tipo);
	}

	public static int mostrarOpcion(Component padre, Object mensaje, String titulo, int optionType, int messageType,
			Color fondo, Color texto, Object[] opciones, Object opcionInicial, Icon icono) {
		configurarEstilo(fondo, texto);
		return JOptionPane.showOptionDialog(padre, mensaje, titulo, optionType, messageType, icono, opciones,
				opcionInicial);
	}

	public static ResultadoConfirmacion confirmarConCheckbox(Component padre, String mensaje, String titulo,
			Color fondo, Color texto, String textoCheckbox) {
		JCheckBox noMostrarMas = new JCheckBox(textoCheckbox);
		noMostrarMas.setOpaque(false);
		noMostrarMas.setForeground(texto);
		JPanel contenido = new JPanel(new BorderLayout(0, 12));
		contenido.setOpaque(true);
		contenido.setBackground(fondo);
		contenido.setBorder(new EmptyBorder(12, 14, 12, 14));
		JLabel label = new JLabel(mensaje);
		label.setForeground(texto);
		contenido.add(label, BorderLayout.NORTH);
		contenido.add(noMostrarMas, BorderLayout.SOUTH);

		Object[] opciones = {"Aceptar", "Cancelar"};
		int respuesta = mostrarOpcion(padre, contenido, titulo, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
				fondo, texto, opciones, opciones[0], null);
		return new ResultadoConfirmacion(respuesta == 0, noMostrarMas.isSelected());
	}

	public static final class ResultadoConfirmacion {
		private final boolean aceptado;
		private final boolean noMostrarMas;

		public ResultadoConfirmacion(boolean aceptado, boolean noMostrarMas) {
			this.aceptado = aceptado;
			this.noMostrarMas = noMostrarMas;
		}

		public boolean isAceptado() {
			return aceptado;
		}

		public boolean isNoMostrarMas() {
			return noMostrarMas;
		}
	}
}
