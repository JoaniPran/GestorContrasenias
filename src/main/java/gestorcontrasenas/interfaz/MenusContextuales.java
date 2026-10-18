package gestorcontrasenas.interfaz;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;

public final class MenusContextuales {
	private static final Logger LOGGER = Logger.getLogger(MenusContextuales.class.getName());
	private static final String ICONO_COPIA_ORIGINAL = "iconoCopiaOriginal";
	private static final String TEMPORIZADOR_COPIA = "temporizadorCopia";
	private static final Object BLOQUEO_PORTAPAPELES = new Object();
	private static Clipboard portapapelesContrasenaPendiente;
	private static StringSelection seleccionContrasenaPendiente;

	private MenusContextuales() {
	}

	public static void agregarMenuTexto(JTextComponent comp) {
		JPopupMenu menu = new JPopupMenu();
		JMenuItem itemCopiar = new JMenuItem("Copiar", new FlatSVGIcon("icons/copy.svg", 16, 16));
		JMenuItem itemCortar = new JMenuItem("Cortar", new FlatSVGIcon("icons/scissors.svg", 16, 16));
		JMenuItem itemPegar = new JMenuItem("Pegar", new FlatSVGIcon("icons/clipboard.svg", 16, 16));
		JMenuItem itemSeleccionarTodo = new JMenuItem("Seleccionar todo",
				new FlatSVGIcon("icons/check-square.svg", 16, 16));

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
		JMenuItem itemCopiar = new JMenuItem("Copiar contraseña", new FlatSVGIcon("icons/copy.svg", 16, 16));
		itemCopiar.addActionListener(e -> copiarContrasenaAlPortapapeles(new String(campo.getPassword()), itemCopiar));
		itemPegar.addActionListener(e -> campo.paste());
		itemSeleccionarTodo.addActionListener(e -> campo.selectAll());
		menu.add(itemCopiar);
		menu.addSeparator();
		menu.add(itemPegar);
		menu.addSeparator();
		menu.add(itemSeleccionarTodo);

		campo.addMouseListener(new MouseAdapter() {
			private void comprobarPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					itemCopiar.setEnabled(campo.getPassword().length > 0);
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
		JMenuItem itemCopiarCelda = new JMenuItem("Copiar dato de la celda", new FlatSVGIcon("icons/copy.svg", 16, 16));
		JMenuItem itemCopiarContrasena = new JMenuItem("Copiar contraseña", new FlatSVGIcon("icons/copy.svg", 16, 16));

		itemCopiarCelda.addActionListener(e -> {
			int row = tabla.getSelectedRow();
			int col = tabla.getSelectedColumn();
			if (row != -1 && col != -1 && tabla.convertColumnIndexToModel(col) != columnaSensible) {
				Object val = tabla.getValueAt(row, col);
				if (val != null) {
					copiarAlPortapapeles(val.toString(), itemCopiarCelda);
				}
			}
		});

		itemCopiarContrasena.addActionListener(e -> {
			int filaVista = tabla.getSelectedRow();
			if (filaVista == -1 || columnaSensible < 0) {
				return;
			}
			int filaModelo = tabla.convertRowIndexToModel(filaVista);
			Object contrasena = tabla.getModel().getValueAt(filaModelo, columnaSensible);
			if (contrasena != null) {
				copiarContrasenaAlPortapapeles(contrasena.toString(), itemCopiarContrasena);
			}
		});

		menu.add(itemCopiarCelda);
		menu.add(itemCopiarContrasena);

		tabla.addMouseListener(new MouseAdapter() {
			private void comprobarPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					if (tabla.getSelectedRowCount() > 1) {
						return;
					}
					int r = tabla.rowAtPoint(e.getPoint());
					int c = tabla.columnAtPoint(e.getPoint());
					if (r >= 0 && r < tabla.getRowCount() && c >= 0 && c < tabla.getColumnCount()) {
						tabla.setRowSelectionInterval(r, r);
						tabla.setColumnSelectionInterval(c, c);
					}
					itemCopiarCelda.setEnabled(c >= 0 && tabla.convertColumnIndexToModel(c) != columnaSensible);
					int filaModelo = r >= 0 ? tabla.convertRowIndexToModel(r) : -1;
					boolean esContrasena = c >= 0 && tabla.convertColumnIndexToModel(c) == columnaSensible;
					boolean hayContrasena = esContrasena && filaModelo >= 0
							&& tabla.getModel().getValueAt(filaModelo, columnaSensible) != null;
					itemCopiarCelda.setVisible(!esContrasena);
					itemCopiarContrasena.setVisible(esContrasena);
					itemCopiarContrasena.setEnabled(hayContrasena);
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

	public static void copiarAlPortapapeles(String texto) {
		StringSelection selection = new StringSelection(texto);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
	}

	public static void copiarAlPortapapeles(String texto, AbstractButton boton) {
		copiarAlPortapapeles(texto, boton, new FlatSVGIcon("icons/check.svg", 16, 16));
	}

	public static void copiarAlPortapapeles(String texto, AbstractButton boton, Icon iconoConfirmacion) {
		copiarAlPortapapeles(texto);
		mostrarConfirmacionCopia(boton, iconoConfirmacion);
	}

	public static void copiarContrasenaAlPortapapeles(String texto, AbstractButton boton) {
		copiarContrasenaAlPortapapeles(texto, boton, new FlatSVGIcon("icons/check.svg", 16, 16));
	}

	public static void copiarContrasenaAlPortapapeles(String texto, AbstractButton boton, Icon iconoConfirmacion) {
		Clipboard portapapeles = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection seleccion = new StringSelection(texto);
		synchronized (BLOQUEO_PORTAPAPELES) {
			portapapeles.setContents(seleccion, null);
			portapapelesContrasenaPendiente = portapapeles;
			seleccionContrasenaPendiente = seleccion;
		}

		Timer temporizadorLimpieza = new Timer(30_000, e -> limpiarPortapapelesSiCoincide(portapapeles, seleccion));
		temporizadorLimpieza.setRepeats(false);
		temporizadorLimpieza.start();

		mostrarConfirmacionCopia(boton, iconoConfirmacion);
	}

	private static void limpiarPortapapelesSiCoincide(Clipboard portapapeles, StringSelection seleccion) {
		synchronized (BLOQUEO_PORTAPAPELES) {
			Transferable contenido = portapapeles.getContents(null);
			if (contenido == seleccion || contieneLaMismaContrasena(contenido, seleccion)) {
				portapapeles.setContents(new StringSelection(""), null);
			}
			if (portapapelesContrasenaPendiente == portapapeles && seleccionContrasenaPendiente == seleccion) {
				portapapelesContrasenaPendiente = null;
				seleccionContrasenaPendiente = null;
			}
		}
	}

	private static boolean contieneLaMismaContrasena(Transferable contenido, StringSelection seleccion) {
		if (contenido == null || !contenido.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			return false;
		}
		try {
			return seleccion.getTransferData(DataFlavor.stringFlavor)
					.equals(contenido.getTransferData(DataFlavor.stringFlavor));
		} catch (UnsupportedFlavorException | IOException e) {
			LOGGER.log(Level.WARNING, "No se pudo verificar el contenido del portapapeles.", e);
			return false;
		}
	}

	public static void limpiarContrasenaPendienteDelPortapapeles() {
		synchronized (BLOQUEO_PORTAPAPELES) {
			if (portapapelesContrasenaPendiente != null && seleccionContrasenaPendiente != null) {
				limpiarPortapapelesSiCoincide(portapapelesContrasenaPendiente, seleccionContrasenaPendiente);
			}
		}
	}

	private static void mostrarConfirmacionCopia(AbstractButton boton, Icon iconoConfirmacion) {
		Icon iconoOriginal = (Icon) boton.getClientProperty(ICONO_COPIA_ORIGINAL);
		if (iconoOriginal == null) {
			iconoOriginal = boton.getIcon();
			boton.putClientProperty(ICONO_COPIA_ORIGINAL, iconoOriginal);
		}
		boton.setIcon(iconoConfirmacion);

		Object temporizadorExistente = boton.getClientProperty(TEMPORIZADOR_COPIA);
		if (temporizadorExistente instanceof Timer) {
			((Timer) temporizadorExistente).stop();
		}
		final Icon iconoRestaurar = iconoOriginal;
		Timer temporizador = new Timer(1000, e -> {
			boton.setIcon(iconoRestaurar);
			boton.putClientProperty(TEMPORIZADOR_COPIA, null);
		});
		temporizador.setRepeats(false);
		boton.putClientProperty(TEMPORIZADOR_COPIA, temporizador);
		temporizador.start();
	}
}