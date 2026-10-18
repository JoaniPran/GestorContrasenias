package gestorcontrasenas.interfaz;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.border.EmptyBorder;

public final class EstiloTablaCredenciales {
	private final Color colorCard;
	private final Color colorBorder;
	private final Color colorTextPrimary;
	private final Color colorTextMuted;
	private final Color colorSeleccion;

	public EstiloTablaCredenciales(Color colorCard, Color colorBorder, Color colorTextPrimary, Color colorTextMuted,
			Color colorSeleccion) {
		this.colorCard = colorCard;
		this.colorBorder = colorBorder;
		this.colorTextPrimary = colorTextPrimary;
		this.colorTextMuted = colorTextMuted;
		this.colorSeleccion = colorSeleccion;
	}

	public void aplicar(JTable tabla, JScrollPane scrollPane, ModeloCredenciales modelo) {
		tabla.setBackground(colorCard);
		tabla.setForeground(colorTextPrimary);
		tabla.setGridColor(colorBorder);
		tabla.setRowHeight(48);
		tabla.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
		tabla.setSelectionBackground(colorSeleccion);
		tabla.setSelectionForeground(colorTextPrimary);
		tabla.setShowGrid(false);
		tabla.setIntercellSpacing(new Dimension(0, 0));

		JTableHeader header = tabla.getTableHeader();
		header.setBackground(colorCard);
		header.setForeground(colorTextMuted);
		header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
		header.setPreferredSize(new Dimension(100, 34));
		header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, colorBorder));
		scrollPane.getViewport().setBackground(colorCard);

		DefaultTableCellRenderer rendererBase = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean seleccionado,
					boolean tieneFoco, int fila, int columna) {
				Component componente = super.getTableCellRendererComponent(table, value, seleccionado, tieneFoco, fila,
						columna);
				componente.setBackground(seleccionado ? table.getSelectionBackground() : colorCard);
				setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, colorBorder),
						new EmptyBorder(0, 10, 0, 10)));
				return componente;
			}
		};
		for (int columna = 0; columna < tabla.getColumnCount(); columna++) {
			tabla.getColumnModel().getColumn(columna).setCellRenderer(rendererBase);
		}
		tabla.getColumnModel().getColumn(0).setCellRenderer(new RenderizadorServicio(modelo));
		tabla.getColumnModel().getColumn(1).setCellRenderer(new RenderizadorCuenta(modelo));
	}

	private final class RenderizadorCuenta extends DefaultTableCellRenderer {
		private final ModeloCredenciales modelo;

		private RenderizadorCuenta(ModeloCredenciales modelo) {
			this.modelo = modelo;
		}

		@Override
		public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
				boolean tieneFoco, int fila, int columna) {
			Component componente = super.getTableCellRendererComponent(tabla, valor, seleccionado, tieneFoco, fila,
					columna);
			int filaModelo = tabla.convertRowIndexToModel(fila);
			setText(modelo.esFilaCabecera(filaModelo) ? "" : String.valueOf(valor));
			setBackground(seleccionado ? tabla.getSelectionBackground() : colorCard);
			setBorder(borde(10));
			return componente;
		}
	}

	private final class RenderizadorServicio extends DefaultTableCellRenderer {
		private final ModeloCredenciales modelo;
		private final Icon iconoExpandido = new FlechaGrupoIcon(true);
		private final Icon iconoContraido = new FlechaGrupoIcon(false);

		private RenderizadorServicio(ModeloCredenciales modelo) {
			this.modelo = modelo;
		}

		@Override
		public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
				boolean tieneFoco, int fila, int columna) {
			Component componente = super.getTableCellRendererComponent(tabla, valor, seleccionado, tieneFoco, fila,
					columna);
			int filaModelo = tabla.convertRowIndexToModel(fila);
			String servicio = ModeloCredenciales.normalizarServicio(String.valueOf(valor));
			int cantidad = modelo.cantidadPorServicio(servicio);
			if (modelo.esFilaCabecera(filaModelo)) {
				boolean expandido = modelo.servicioExpandido(servicio);
				setIcon(expandido ? iconoExpandido : iconoContraido);
				setIconTextGap(6);
				setText(valor + " · " + cantidad + " cuentas");
				setToolTipText("Clic en la flecha para " + (expandido ? "contraer" : "desplegar") + " las cuentas");
				setBorder(borde(10));
			} else {
				setIcon(null);
				setText(String.valueOf(valor));
				setToolTipText(null);
				setBorder(borde(cantidad > 1 ? 26 : 10));
			}
			setBackground(seleccionado ? tabla.getSelectionBackground() : colorCard);
			return componente;
		}
	}

	private EmptyBorder borde(int izquierda) {
		return new EmptyBorder(0, izquierda, 0, 10);
	}

	private static final class FlechaGrupoIcon implements Icon {
		private final boolean expandido;

		private FlechaGrupoIcon(boolean expandido) {
			this.expandido = expandido;
		}

		@Override
		public int getIconWidth() {
			return 10;
		}

		@Override
		public int getIconHeight() {
			return 10;
		}

		@Override
		public void paintIcon(Component componente, java.awt.Graphics graphics, int x, int y) {
			java.awt.Graphics2D g2 = (java.awt.Graphics2D) graphics.create();
			g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(componente.getForeground());
			if (expandido) {
				g2.fillPolygon(new int[]{x + 1, x + 9, x + 5}, new int[]{y + 3, y + 3, y + 8}, 3);
			} else {
				g2.fillPolygon(new int[]{x + 3, x + 3, x + 8}, new int[]{y + 1, y + 9, y + 5}, 3);
			}
			g2.dispose();
		}
	}
}
