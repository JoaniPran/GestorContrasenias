package gestorcontrasenas.interfaz;

import gestorcontrasenas.modelo.RegistroContrasena;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class ControladorEventosTabla {
	private final JTable tabla;
	private final ModeloCredenciales modelo;
	private final JTextField busqueda;
	private final Runnable aplicarFiltro;
	private final Consumer<RegistroContrasena> seleccionarRegistro;
	private final Consumer<String> alternarGrupo;

	public ControladorEventosTabla(JTable tabla, ModeloCredenciales modelo, JTextField busqueda,
			Runnable aplicarFiltro, Consumer<RegistroContrasena> seleccionarRegistro,
			Consumer<String> alternarGrupo) {
		this.tabla = Objects.requireNonNull(tabla, "tabla");
		this.modelo = Objects.requireNonNull(modelo, "modelo");
		this.busqueda = Objects.requireNonNull(busqueda, "busqueda");
		this.aplicarFiltro = Objects.requireNonNull(aplicarFiltro, "aplicarFiltro");
		this.seleccionarRegistro = Objects.requireNonNull(seleccionarRegistro, "seleccionarRegistro");
		this.alternarGrupo = Objects.requireNonNull(alternarGrupo, "alternarGrupo");
	}

	public void instalar() {
		busqueda.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent evento) {
				aplicarFiltro.run();
			}

			@Override
			public void removeUpdate(DocumentEvent evento) {
				aplicarFiltro.run();
			}

			@Override
			public void changedUpdate(DocumentEvent evento) {
				aplicarFiltro.run();
			}
		});
		tabla.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evento) {
				if (evento.getButton() != MouseEvent.BUTTON1 || tabla.getSelectedRowCount() > 1) {
					return;
				}
				int filaVista = tabla.getSelectedRow();
				if (filaVista < 0) {
					return;
				}
				int filaModelo = tabla.convertRowIndexToModel(filaVista);
				if (modelo.esFilaCabecera(filaModelo)) {
					if (tabla.columnAtPoint(evento.getPoint()) == 0 && evento.getX() < 36) {
						alternarGrupo.accept(ModeloCredenciales.normalizarServicio(
								modelo.getValueAt(filaModelo, 0).toString()));
					}
					return;
				}
				seleccionarRegistro.accept(modelo.registroEnFila(filaModelo));
			}
		});
	}
}
