package gestorcontrasenas.seguridad;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import javax.swing.JFrame;

public final class ProteccionCapturasWindows {
	private static final int WDA_EXCLUDEFROMCAPTURE = 0x11;

	public interface User32 extends StdCallLibrary {
		boolean SetWindowDisplayAffinity(Pointer ventana, int afinidad);
	}

	private ProteccionCapturasWindows() {
	}

	public static void excluirVentana(JFrame ventana) {
		long identificadorVentana = Native.getComponentID(ventana);
		if (identificadorVentana == 0) {
			throw new IllegalStateException("Windows no proporcionó el identificador de la ventana.");
		}

		User32 user32 = Native.load("user32", User32.class);
		if (!user32.SetWindowDisplayAffinity(Pointer.createConstant(identificadorVentana),
				WDA_EXCLUDEFROMCAPTURE)) {
			throw new IllegalStateException("Windows rechazó la exclusión de la ventana de las capturas.");
		}
	}
}
