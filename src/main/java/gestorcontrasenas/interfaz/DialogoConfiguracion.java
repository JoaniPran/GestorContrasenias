package gestorcontrasenas.interfaz;

import static gestorcontrasenas.interfaz.PaletaColores.COLOR_PRIMARY;
import static gestorcontrasenas.interfaz.PaletaColores.COLOR_PRIMARY_HOVER;
import static gestorcontrasenas.interfaz.PaletaColores.COLOR_TEXTO_SECUNDARIO_OSCURO;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.util.Objects;
import java.util.function.BiFunction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public final class DialogoConfiguracion {
	private static final Color[] COLORES_RESALTADO = {new Color(0, 122, 204), new Color(46, 139, 139),
			new Color(76, 145, 0), new Color(202, 145, 0), new Color(225, 77, 35), new Color(220, 45, 67),
			new Color(184, 65, 165), new Color(112, 91, 205), new Color(94, 119, 102), new Color(164, 129, 88),
			Color.BLACK};
	private static final String[] NOMBRES_RESALTADO = {"Azul", "Turquesa", "Verde", "Amarillo", "Naranja", "Rojo",
			"Rosa", "Violeta", "Verde grisáceo", "Arena", "Negro"};

	private final Window propietario;
	private final boolean modoOscuro;
	private final boolean confirmarAntesDeMostrarContrasena;
	private final Color colorResaltado;
	private final Color colorCard;
	private final Color colorInput;
	private final Color colorTextPrimary;
	private final Color colorTextMuted;
	private final AplicadorPreferencias aplicador;
	private final Runnable accionExportar;
	private final BiFunction<String, Integer, ? extends Icon> crearIcono; // <-- Recibimos la función reutilizada

	public DialogoConfiguracion(Window propietario, boolean modoOscuro, boolean confirmarAntesDeMostrarContrasena,
			Color colorResaltado, Color colorCard, Color colorInput, Color colorTextPrimary, Color colorTextMuted,
			AplicadorPreferencias aplicador, Runnable accionExportar,
			BiFunction<String, Integer, ? extends Icon> crearIcono) {
		this.propietario = Objects.requireNonNull(propietario, "propietario");
		this.modoOscuro = modoOscuro;
		this.confirmarAntesDeMostrarContrasena = confirmarAntesDeMostrarContrasena;
		this.colorResaltado = Objects.requireNonNull(colorResaltado, "colorResaltado");
		this.colorCard = Objects.requireNonNull(colorCard, "colorCard");
		this.colorInput = Objects.requireNonNull(colorInput, "colorInput");
		this.colorTextPrimary = Objects.requireNonNull(colorTextPrimary, "colorTextPrimary");
		this.colorTextMuted = Objects.requireNonNull(colorTextMuted, "colorTextMuted");
		this.aplicador = Objects.requireNonNull(aplicador, "aplicador");
		this.accionExportar = accionExportar;
		this.crearIcono = crearIcono;
	}

	public void mostrar() {
		JDialog dialogo = new JDialog(propietario, "Configuración", JDialog.ModalityType.APPLICATION_MODAL);
		JPanel panel = new JPanel(new BorderLayout(12, 18));
		panel.setBorder(new EmptyBorder(20, 22, 18, 22));
		panel.setBackground(colorCard);

		JLabel titulo = new JLabel("Tema de la aplicación");
		titulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
		titulo.setForeground(colorTextPrimary);
		panel.add(titulo, BorderLayout.NORTH);

		JPanel opciones = new JPanel();
		opciones.setLayout(new BoxLayout(opciones, BoxLayout.Y_AXIS));
		opciones.setOpaque(false);
		JLabel descripcion = new JLabel("Usa el aspecto oscuro o claro");
		descripcion.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
		descripcion.setForeground(colorTextMuted);
		descripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
		opciones.add(descripcion);
		opciones.add(Box.createVerticalStrut(8));

		JLabel etiquetaModo = new JLabel(modoOscuro ? "Modo oscuro" : "Modo claro");
		etiquetaModo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
		etiquetaModo.setForeground(colorTextPrimary);
		InterruptorTema interruptor = new InterruptorTema(modoOscuro, colorResaltado);
		interruptor.setToolTipText("Cambiar entre modo oscuro y modo claro");
		interruptor.getAccessibleContext().setAccessibleName("Cambiar tema");
		interruptor
				.addActionListener(e -> etiquetaModo.setText(interruptor.isSelected() ? "Modo oscuro" : "Modo claro"));

		JPanel selector = new JPanel(new BorderLayout(10, 0));
		selector.setOpaque(true);
		selector.setBorder(new EmptyBorder(10, 12, 10, 12));
		selector.setBackground(colorInput);
		selector.add(etiquetaModo, BorderLayout.CENTER);
		selector.add(interruptor, BorderLayout.EAST);
		selector.setAlignmentX(Component.LEFT_ALIGNMENT);
		opciones.add(selector);
		opciones.add(Box.createVerticalStrut(14));

		JLabel etiquetaColor = new JLabel("Color de resaltado");
		etiquetaColor.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
		etiquetaColor.setForeground(colorTextPrimary);
		etiquetaColor.setAlignmentX(Component.LEFT_ALIGNMENT);
		opciones.add(etiquetaColor);
		opciones.add(Box.createVerticalStrut(6));

		Color[] colorElegido = {colorResaltado};
		JPanel muestras = new JPanel(new FlowLayout(FlowLayout.CENTER, 7, 5));
		muestras.setOpaque(true);
		muestras.setBackground(colorInput);
		muestras.setBorder(new EmptyBorder(5, 6, 5, 6));
		ButtonGroup grupoColores = new ButtonGroup();
		for (int i = 0; i < COLORES_RESALTADO.length; i++) {
			Color color = COLORES_RESALTADO[i];
			SelectorColor selectorColor = new SelectorColor(color, NOMBRES_RESALTADO[i], color.equals(colorResaltado));
			grupoColores.add(selectorColor);
			muestras.add(selectorColor);
			selectorColor.addActionListener(e -> {
				colorElegido[0] = color;
				interruptor.setColorResaltado(color);
			});
		}
		muestras.setAlignmentX(Component.LEFT_ALIGNMENT);
		opciones.add(muestras);
		opciones.add(Box.createVerticalStrut(10));

		JCheckBox confirmarPassword = new JCheckBox("Confirmar antes de mostrar contraseñas",
				confirmarAntesDeMostrarContrasena);
		confirmarPassword.setOpaque(false);
		confirmarPassword.setForeground(colorTextPrimary);
		confirmarPassword.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
		confirmarPassword.setToolTipText("Puedes cambiar esta opción en cualquier momento desde Configuración");
		confirmarPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
		opciones.add(confirmarPassword);

		// --- SECCIÓN DE COPIA DE SEGURIDAD ---
		if (accionExportar != null) {
			opciones.add(Box.createVerticalStrut(16));

			JLabel etiquetaExportar = new JLabel("Copia de seguridad");
			etiquetaExportar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
			etiquetaExportar.setForeground(colorTextPrimary);
			etiquetaExportar.setAlignmentX(Component.LEFT_ALIGNMENT);
			opciones.add(etiquetaExportar);
			opciones.add(Box.createVerticalStrut(6));

			BotonEstilizado btnExportar = new BotonEstilizado(" Exportar contraseñas (.csv)", COLOR_PRIMARY,
					COLOR_PRIMARY_HOVER, 8);
			btnExportar.setAlignmentX(Component.LEFT_ALIGNMENT);


			if (crearIcono != null) {
				btnExportar.setIcon(crearIcono.apply("icons/external-link.svg", 14));
			}

			btnExportar.addActionListener(e -> accionExportar.run());
			opciones.add(btnExportar);
		}

		panel.add(opciones, BorderLayout.CENTER);

		BotonEstilizado aplicar = new BotonEstilizado("Aplicar", COLOR_PRIMARY, COLOR_PRIMARY_HOVER, 8);
		aplicar.addActionListener(e -> {
			if (aplicador.aplicar(interruptor.isSelected(), confirmarPassword.isSelected(), colorElegido[0])) {
				dialogo.dispose();
			}
		});
		JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		acciones.setOpaque(false);
		acciones.add(aplicar);
		panel.add(acciones, BorderLayout.SOUTH);

		dialogo.setContentPane(panel);
		dialogo.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialogo.setResizable(false);
		dialogo.pack();
		dialogo.setLocationRelativeTo(propietario);
		dialogo.setVisible(true);
	}

	@FunctionalInterface
	public interface AplicadorPreferencias {
		boolean aplicar(boolean modoOscuro, boolean confirmarAntesDeMostrarContrasena, Color colorResaltado);
	}

	private static final class SelectorColor extends JToggleButton {
		private final Color color;

		private SelectorColor(Color color, String nombre, boolean seleccionado) {
			this.color = color;
			setSelected(seleccionado);
			setPreferredSize(new Dimension(30, 30));
			setToolTipText(nombre);
			getAccessibleContext().setAccessibleName(nombre);
			setOpaque(false);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int diameter = 20;
			int x = (getWidth() - diameter) / 2;
			int y = (getHeight() - diameter) / 2;
			if (isSelected()) {
				g2.setColor(Color.BLACK.equals(color) ? Color.WHITE : color);
				g2.setStroke(new BasicStroke(2.5f));
				g2.drawOval(x - 4, y - 4, diameter + 8, diameter + 8);
			} else if (getModel().isRollover()) {
				g2.setColor(color.brighter());
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawOval(x - 2, y - 2, diameter + 4, diameter + 4);
			}
			g2.setColor(color);
			g2.fillOval(x, y, diameter, diameter);
			g2.dispose();
		}
	}

	private static final class InterruptorTema extends JToggleButton {
		private Color colorResaltado;

		private InterruptorTema(boolean seleccionado, Color colorResaltado) {
			this.colorResaltado = colorResaltado;
			setSelected(seleccionado);
			setPreferredSize(new Dimension(52, 30));
			setToolTipText("Cambiar tema");
			setOpaque(false);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		private void setColorResaltado(Color colorResaltado) {
			this.colorResaltado = colorResaltado;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int trackWidth = 46;
			int trackHeight = 24;
			int x = (getWidth() - trackWidth) / 2;
			int y = (getHeight() - trackHeight) / 2;
			Color track = isSelected() ? colorResaltado : new Color(76, 76, 76);
			if (getModel().isRollover()) {
				track = track.brighter();
			}
			g2.setColor(track);
			g2.fillRoundRect(x, y, trackWidth, trackHeight, trackHeight, trackHeight);
			int knobX = isSelected() ? x + trackWidth - 21 : x + 3;
			g2.setColor(new Color(226, 226, 226));
			g2.fillOval(knobX, y + 3, 18, 18);
			if (hasFocus()) {
				g2.setColor(COLOR_TEXTO_SECUNDARIO_OSCURO);
				g2.drawRoundRect(x, y, trackWidth, trackHeight, trackHeight, trackHeight);
			}
			g2.dispose();
		}
	}
}