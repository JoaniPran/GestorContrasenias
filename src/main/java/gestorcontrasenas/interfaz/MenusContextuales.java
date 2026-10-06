package gestorcontrasenas.interfaz;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JPasswordField;
import javax.swing.text.JTextComponent;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.TransferHandler;
import javax.swing.KeyStroke;

public final class MenusContextuales {
	private MenusContextuales() {
	}

	public static void agregarMenuTexto(JTextComponent comp) {
		JPopupMenu menu = new JPopupMenu();
		JMenuItem itemCopiar = new JMenuItem("Copiar");
		JMenuItem itemCortar = new JMenuItem("Cortar");
		JMenuItem itemPegar = new JMenuItem("Pegar");
		JMenuItem itemSeleccionarTodo = new JMenuItem("Seleccionar todo");

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

	public static void agregarMenuPassword(JPasswordField campo) {
		campo.setTransferHandler(new TransferHandler() {
			@Override
			public int getSourceActions(javax.swing.JComponent componente) {
				return NONE;
			}

			@Override
			public void exportToClipboard(javax.swing.JComponent componente, java.awt.datatransfer.Clipboard clipboard,
					int accion) {
			}

			@Override
			public boolean canImport(javax.swing.JComponent componente, DataFlavor[] sabores) {
				for (DataFlavor sabor : sabores) {
					if (DataFlavor.stringFlavor.equals(sabor)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public boolean importData(javax.swing.JComponent componente, Transferable datos) {
				if (!canImport(componente, datos.getTransferDataFlavors())) {
					return false;
				}
				try {
					campo.replaceSelection((String) datos.getTransferData(DataFlavor.stringFlavor));
					return true;
				} catch (UnsupportedFlavorException | IOException e) {
					return false;
				}
			}
		});

		InputMap inputMap = campo.getInputMap();
		ActionMap actionMap = campo.getActionMap();
		String accionBloqueada = "bloquearCopiaContrasena";
		actionMap.put(accionBloqueada, new AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
			}
		});
		for (String atajo : new String[]{"ctrl C", "ctrl X", "ctrl INSERT", "shift DELETE"}) {
			inputMap.put(KeyStroke.getKeyStroke(atajo), accionBloqueada);
		}

		JPopupMenu menu = new JPopupMenu();
		JMenuItem itemPegar = new JMenuItem("📌 Pegar");
		JMenuItem itemSeleccionarTodo = new JMenuItem("🔍 Seleccionar todo");
		itemPegar.addActionListener(e -> campo.paste());
		itemSeleccionarTodo.addActionListener(e -> campo.selectAll());
		menu.add(itemPegar);
		menu.addSeparator();
		menu.add(itemSeleccionarTodo);

		campo.addMouseListener(new MouseAdapter() {
			private void comprobarPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					itemPegar.setEnabled(campo.isEditable());
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