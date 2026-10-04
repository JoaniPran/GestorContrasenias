package gestorcontrasenas.interfaz;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class PanelRedondeado extends JPanel {
    private final int radio;
    private Color colorFondo;
    private Color colorBorde;

    public PanelRedondeado(int radio, Color fondo, Color borde) {
        this.radio = radio;
        this.colorFondo = fondo;
        this.colorBorde = borde;
        setOpaque(false);
    }

    public void setColores(Color fondo, Color borde) {
        this.colorFondo = fondo;
        this.colorBorde = borde;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(colorFondo);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
        g2.setColor(colorBorde);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
        g2.dispose();
        super.paintComponent(g);
    }
}