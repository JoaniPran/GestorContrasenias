package gestorcontrasenas.seguridad;

import gestorcontrasenas.interfaz.MenusContextuales;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.JTable;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public final class ProtectorVentana {
	private final JFrame ventana;
	private final JTable tabla;
	private final Runnable ocultarContrasenas;
	private final Consumer<String> informarFallo;
	private boolean puedeMostrarContrasenas = true;

	public ProtectorVentana(JFrame ventana, JTable tabla, Runnable ocultarContrasenas,
			Consumer<String> informarFallo) {
		this.ventana = Objects.requireNonNull(ventana, "ventana");
		this.tabla = Objects.requireNonNull(tabla, "tabla");
		this.ocultarContrasenas = Objects.requireNonNull(ocultarContrasenas, "ocultarContrasenas");
		this.informarFallo = Objects.requireNonNull(informarFallo, "informarFallo");
		if (esWindows()) {
			puedeMostrarContrasenas = false;
		}
	}

	public boolean esWindows() {
		return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
	}

	public void configurarProteccionCapturas() {
		ventana.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evento) {
				MenusContextuales.limpiarContrasenaPendienteDelPortapapeles();
			}
		});

		if (esWindows()) {
			ventana.addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent evento) {
					try {
						ProteccionCapturasWindows.excluirVentana(ventana);
						puedeMostrarContrasenas = true;
					} catch (RuntimeException | LinkageError error) {
						ocultarContrasenas.run();
						String detalle = error.getClass().getSimpleName()
								+ (error.getMessage() == null ? "" : ": " + error.getMessage());
						String instruccion = error instanceof NoClassDefFoundError
								? "\nReconstruye con «mvn clean package» y ejecuta el JAR generado o vuelve a generar "
										+ "el paquete portable de Windows."
								: "";
						informarFallo.accept("No se pudo activar la exclusión de la ventana de las capturas de "
								+ "Windows. Las contraseñas permanecerán ocultas.\nDetalle: " + detalle + instruccion);
					}
				}
			});
		}

		ventana.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowGainedFocus(WindowEvent evento) {
			}

			@Override
			public void windowLostFocus(WindowEvent evento) {
				if (tabla.isEditing()) {
					tabla.getCellEditor().cancelCellEditing();
				}
				ocultarContrasenas.run();
			}
		});
	}

	public boolean puedeMostrarContrasenas() {
		return puedeMostrarContrasenas;
	}
}
