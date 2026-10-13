package gestorcontrasenas.interfaz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Color;
import org.junit.jupiter.api.Test;

class TemaColoresTest {
	@Test
	void temaOscuroConColorNegroUsaPaletaNeutraOscura() {
		TemaColores tema = TemaColores.crear(true, Color.BLACK);

		assertEquals(new Color(18, 18, 18), tema.getFondo());
		assertEquals(new Color(31, 31, 31), tema.getTarjeta());
		assertEquals(new Color(24, 24, 24), tema.getEntrada());
	}

	@Test
	void elColorDeResaltadoCambiaLaPaletaYLaSeleccion() {
		TemaColores azul = TemaColores.crear(true, new Color(0, 122, 204));
		TemaColores verde = TemaColores.crear(true, new Color(76, 145, 0));

		assertNotEquals(azul.getFondo(), verde.getFondo());
		assertNotEquals(azul.getColorSeleccion(true, new Color(0, 122, 204)),
				verde.getColorSeleccion(true, new Color(76, 145, 0)));
	}

	@Test
	void paletaClaraConNegroMantieneFondosClaros() {
		TemaColores tema = TemaColores.crear(false, Color.BLACK);

		assertEquals(new Color(245, 245, 245), tema.getFondo());
		assertEquals(Color.WHITE, tema.getEntrada());
	}
}
