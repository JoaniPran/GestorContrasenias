package gestorcontrasenas.interfaz;

import static gestorcontrasenas.interfaz.PaletaColores.COLOR_TEXTO_OSCURO;
import static gestorcontrasenas.interfaz.PaletaColores.COLOR_TEXTO_SECUNDARIO_OSCURO;

import java.awt.Color;
import java.util.Objects;

public final class TemaColores {
	private final Color fondo;
	private final Color tarjeta;
	private final Color entrada;
	private final Color texto;
	private final Color textoSecundario;
	private final Color borde;

	private TemaColores(Color fondo, Color tarjeta, Color entrada, Color texto, Color textoSecundario, Color borde) {
		this.fondo = fondo;
		this.tarjeta = tarjeta;
		this.entrada = entrada;
		this.texto = texto;
		this.textoSecundario = textoSecundario;
		this.borde = borde;
	}

	public static TemaColores crear(boolean oscuro, Color resaltado) {
		Objects.requireNonNull(resaltado, "resaltado");
		if (resaltado.equals(Color.BLACK) && oscuro) {
			return new TemaColores(new Color(18, 18, 18), new Color(31, 31, 31), new Color(24, 24, 24),
					COLOR_TEXTO_OSCURO, COLOR_TEXTO_SECUNDARIO_OSCURO, new Color(62, 62, 62));
		}
		if (resaltado.equals(Color.BLACK)) {
			return new TemaColores(new Color(245, 245, 245), new Color(250, 250, 250), Color.WHITE,
					new Color(38, 40, 43), new Color(82, 85, 89), new Color(170, 170, 170));
		}
		float tono = Color.RGBtoHSB(resaltado.getRed(), resaltado.getGreen(), resaltado.getBlue(), null)[0];
		if (oscuro) {
			return new TemaColores(colorTono(tono, 0.70f, 0.60f), colorTono(tono, 0.70f, 0.30f),
					colorTono(tono, 0.35f, 0.10f), COLOR_TEXTO_OSCURO, COLOR_TEXTO_SECUNDARIO_OSCURO,
					colorTono(tono, 0.5f, 0.100f));
		}
		return new TemaColores(colorTono(tono, 0.70f, 0.94f), colorTono(tono, 0.18f, 0.96f),
				colorTono(tono, 0.35f, 0.99f), new Color(38, 40, 43), new Color(82, 85, 89),
				colorTono(tono, 0.3f, 0.64f));
	}

	public Color getFondo() {
		return fondo;
	}

	public Color getTarjeta() {
		return tarjeta;
	}

	public Color getEntrada() {
		return entrada;
	}

	public Color getTexto() {
		return texto;
	}

	public Color getTextoSecundario() {
		return textoSecundario;
	}

	public Color getBorde() {
		return borde;
	}

	public Color getColorSeleccion(boolean oscuro, Color resaltado) {
		float intensidad = oscuro ? 0.38f : 0.22f;
		return mezclar(tarjeta, resaltado, intensidad);
	}

	private static Color colorTono(float tono, float saturacion, float brillo) {
		return Color.getHSBColor(tono, saturacion, brillo);
	}

	private static Color mezclar(Color base, Color resaltado, float intensidad) {
		return new Color(
				Math.round(base.getRed() * (1 - intensidad) + resaltado.getRed() * intensidad),
				Math.round(base.getGreen() * (1 - intensidad) + resaltado.getGreen() * intensidad),
				Math.round(base.getBlue() * (1 - intensidad) + resaltado.getBlue() * intensidad));
	}
}
