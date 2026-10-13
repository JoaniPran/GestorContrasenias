package gestorcontrasenas.interfaz;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.AbstractCellEditor;

public final class ColumnaContrasena {
	private ColumnaContrasena() {
	}

	public static void instalar(JTable tabla, AccionesContrasena acciones) {
		Objects.requireNonNull(tabla, "tabla");
		Objects.requireNonNull(acciones, "acciones");
		tabla.getColumnModel().getColumn(2).setCellRenderer(new Renderizador(acciones));
		tabla.getColumnModel().getColumn(2).setCellEditor(new Editor(tabla, acciones));
	}

	public interface AccionesContrasena {
		boolean esEncabezado(int filaModelo);
		String claveRegistro(int filaModelo);
		boolean estaVisible(String clave);
		void alternarVisibilidad(String clave);
		boolean puedeMostrar();
		boolean confirmarVisualizacion();
		String ocultar(String contrasena);
		Color colorAcciones();
		FlatSVGIcon icono(String recurso, int tamano);
		void copiar(String contrasena, JButton boton);
	}

	private static final class Renderizador extends JPanel implements TableCellRenderer {
		private final AccionesContrasena acciones;
		private final JLabel contrasena = new JLabel();
		private final JButton visibilidad = crearBoton("Mostrar u ocultar contraseña");
		private final JButton copiar = crearBoton("Copiar contraseña");
		private final JPanel botones = new JPanel(new FlowLayout(FlowLayout.TRAILING, 2, 0));

		private Renderizador(AccionesContrasena acciones) {
			super(new BorderLayout(8, 0));
			this.acciones = acciones;
			setOpaque(true);
			setBorder(new EmptyBorder(0, 10, 0, 6));
			botones.setOpaque(false);
			botones.add(copiar);
			botones.add(visibilidad);
			add(contrasena, BorderLayout.CENTER);
			add(botones, BorderLayout.EAST);
		}

		@Override
		public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
				boolean tieneFoco, int fila, int columna) {
			int filaModelo = tabla.convertRowIndexToModel(fila);
			boolean encabezado = acciones.esEncabezado(filaModelo);
			String clave = encabezado ? "" : acciones.claveRegistro(filaModelo);
			boolean visible = !encabezado && acciones.estaVisible(clave);
			String texto = String.valueOf(valor);
			contrasena.setText(encabezado ? "" : visible ? texto : acciones.ocultar(texto));
			botones.setVisible(!encabezado);
			visibilidad.setIcon(encabezado ? null : acciones.icono(
					visible ? "icons/eye-off.svg" : "icons/eye.svg", 16));
			copiar.setIcon(encabezado ? null : acciones.icono("icons/copy.svg", 16));
			Color foreground = seleccionado ? tabla.getSelectionForeground() : tabla.getForeground();
			setBackground(seleccionado ? tabla.getSelectionBackground() : tabla.getBackground());
			contrasena.setForeground(foreground);
			contrasena.setFont(tabla.getFont());
			visibilidad.setForeground(acciones.colorAcciones());
			copiar.setForeground(acciones.colorAcciones());
			return this;
		}
	}

	private static final class Editor extends AbstractCellEditor implements TableCellEditor {
		private final JTable tabla;
		private final AccionesContrasena acciones;
		private final JPanel panel = new JPanel(new BorderLayout(8, 0));
		private final JLabel contrasena = new JLabel();
		private final JButton visibilidad = crearBoton("Mostrar u ocultar contraseña");
		private final JButton copiar = crearBoton("Copiar contraseña");
		private final JPanel botones = new JPanel(new FlowLayout(FlowLayout.TRAILING, 2, 0));
		private String contrasenaActual;
		private String claveActual;

		private Editor(JTable tabla, AccionesContrasena acciones) {
			this.tabla = tabla;
			this.acciones = acciones;
			panel.setBorder(new EmptyBorder(0, 10, 0, 6));
			botones.setOpaque(false);
			botones.add(copiar);
			botones.add(visibilidad);
			panel.add(contrasena, BorderLayout.CENTER);
			panel.add(botones, BorderLayout.EAST);
			copiar.addActionListener(e -> {
				if (tabla.getSelectedRowCount() > 1) {
					fireEditingCanceled();
					return;
				}
				acciones.copiar(contrasenaActual, copiar);
			});
			visibilidad.addActionListener(e -> alternarVisibilidad());
		}

		private void alternarVisibilidad() {
			if (tabla.getSelectedRowCount() > 1) {
				fireEditingCanceled();
				return;
			}
			if (!acciones.estaVisible(claveActual)) {
				if (!acciones.puedeMostrar() || !acciones.confirmarVisualizacion()) {
					fireEditingCanceled();
					return;
				}
			}
			acciones.alternarVisibilidad(claveActual);
			actualizarContenido();
			fireEditingStopped();
		}

		@Override
		public Component getTableCellEditorComponent(JTable tabla, Object valor, boolean seleccionado, int fila,
				int columna) {
			int filaModelo = tabla.convertRowIndexToModel(fila);
			claveActual = acciones.claveRegistro(filaModelo);
			contrasenaActual = String.valueOf(valor);
			actualizarContenido();
			panel.setBackground(tabla.getSelectionBackground());
			contrasena.setForeground(tabla.getSelectionForeground());
			contrasena.setFont(tabla.getFont());
			visibilidad.setForeground(acciones.colorAcciones());
			copiar.setForeground(acciones.colorAcciones());
			return panel;
		}

		private void actualizarContenido() {
			boolean visible = acciones.estaVisible(claveActual);
			contrasena.setText(visible ? contrasenaActual : acciones.ocultar(contrasenaActual));
			visibilidad.setIcon(acciones.icono(visible ? "icons/eye-off.svg" : "icons/eye.svg", 16));
			copiar.setIcon(acciones.icono("icons/copy.svg", 16));
		}

		@Override
		public Object getCellEditorValue() {
			return contrasenaActual;
		}
	}

	private static JButton crearBoton(String descripcion) {
		JButton boton = new JButton();
		boton.setFocusable(false);
		boton.setBorderPainted(false);
		boton.setToolTipText(descripcion);
		boton.setContentAreaFilled(false);
		boton.setOpaque(false);
		boton.setPreferredSize(new Dimension(28, 28));
		return boton;
	}
}
