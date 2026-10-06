package gestorcontrasenas.interfaz;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BotonEstilizado extends JButton {
	private final Color normalColor;
	private final Color hoverColor;
	private final int radio;
	private boolean plano;

	public BotonEstilizado(String texto, Color normal, Color hover, int radio) {
		super(texto);
		this.normalColor = normal;
		this.hoverColor = hover;
		this.radio = radio;

		setFont(new Font("Segoe UI", Font.BOLD, 12));
		setForeground(new Color(238, 238, 238));
		setFocusPainted(false);
		setBorderPainted(false);
		setContentAreaFilled(false);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setBackground(normalColor);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setBackground(hoverColor);
				repaint();
			}
			@Override
			public void mouseExited(MouseEvent e) {
				setBackground(normalColor);
				repaint();
			}
		});
	}

	public void setPlano(boolean plano) {
		this.plano = plano;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (!plano) {
			g2.setColor(getBackground());
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), radio, radio);
		}
		g2.dispose();
		super.paintComponent(g);
	}
}