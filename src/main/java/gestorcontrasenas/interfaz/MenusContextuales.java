package gestorcontrasenas.interfaz;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.text.JTextComponent;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class MenusContextuales {
	private MenusContextuales() {
	}

	public static void agregarMenuTexto(JTextComponent comp) {
		JPopupMenu menu = new JPopupMenu();
		JMenuItem itemCopiar = new JMenuItem("📋 Copiar");
		JMenuItem itemCortar = new JMenuItem("✂️️ Cortar");
		JMenuItem itemPegar = new JMenuItem("📌 Pegar");
		JMenuItem itemSeleccionarTodo = new JMenuItem("🔍 Seleccionar todo");

		itemCopiar.addActionListener(e -> comp.copy());
		itemCortar.addActionListener(e -> comp.cut());
		itemPegar.addActionListener(e -> comp.paste());
		itemSeleccionarTodo.addActionListener(e -> comp.selectAll());

		menu.add(itemCopiar);
		menu.add(itemCortar);
		menu.add(itemPegar);
		menu.addSeparator();
		menu.add(itemSeleccionarTodo);

		comp.addMouseListener(new MouseAdapter() {
			private void comprobarPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					boolean haySeleccion = comp.getSelectedText() != null && !comp.getSelectedText().isEmpty();
					itemCopiar.setEnabled(haySeleccion);
					itemCortar.setEnabled(haySeleccion && comp.isEditable());
					itemPegar.setEnabled(comp.isEditable());
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
				comprobarPopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				comprobarPopup(e);
			}
		});
	}

	public static void agregarMenuTabla(JTable tabla) {
		agregarMenuTabla(tabla, -1);
	}

	public static void agregarMenuTabla(JTable tabla, int columnaSensible) {
		JPopupMenu menu = new JPopupMenu();
		JMenuItem itemCopiarCelda = new JMenuItem("📋 Copiar dato de la celda");

		itemCopiarCelda.addActionListener(e -> {
			int row = tabla.getSelectedRow();
			int col = tabla.getSelectedColumn();
			if (row != -1 && col != -1 && tabla.convertColumnIndexToModel(col) != columnaSensible) {
				Object val = tabla.getValueAt(row, col);
				if (val != null) {
					StringSelection selection = new StringSelection(val.toString());
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
				}
			}
		});

		menu.add(itemCopiarCelda);

		tabla.addMouseListener(new MouseAdapter() {
			private void comprobarPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					int r = tabla.rowAtPoint(e.getPoint());
					int c = tabla.columnAtPoint(e.getPoint());
					if (r >= 0 && r < tabla.getRowCount() && c >= 0 && c < tabla.getColumnCount()) {
						tabla.setRowSelectionInterval(r, r);
						tabla.setColumnSelectionInterval(c, c);
					}
					itemCopiarCelda.setEnabled(c >= 0 && tabla.convertColumnIndexToModel(c) != columnaSensible);
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
				comprobarPopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				comprobarPopup(e);
			}
		});
	}
}