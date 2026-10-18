package gestorcontrasenas.interfaz;

import static gestorcontrasenas.interfaz.PaletaColores.COLOR_BOTON;
import static gestorcontrasenas.interfaz.PaletaColores.COLOR_BOTON_HOVER;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class DialogoImportacionGoogle {
	private final JDialog dialogo;
	private final Color colorCard;
	private final Color colorInput;
	private final Color colorTextPrimary;
	private final Color colorBorder;
	private final ImportadorArchivo importador;

	public DialogoImportacionGoogle(JDialog dialogo, Color colorCard, Color colorInput, Color colorTextPrimary,
			Color colorBorder, ImportadorArchivo importador) {
		this.dialogo = Objects.requireNonNull(dialogo, "dialogo");
		this.colorCard = Objects.requireNonNull(colorCard, "colorCard");
		this.colorInput = Objects.requireNonNull(colorInput, "colorInput");
		this.colorTextPrimary = Objects.requireNonNull(colorTextPrimary, "colorTextPrimary");
		this.colorBorder = Objects.requireNonNull(colorBorder, "colorBorder");
		this.importador = Objects.requireNonNull(importador, "importador");
	}

	public void mostrar() {
		JPanel contenido = new JPanel(new BorderLayout(12, 16));
		contenido.setBorder(new EmptyBorder(18, 20, 16, 20));
		contenido.setBackground(colorCard);

		JLabel instrucciones = new JLabel("<html>Arrastra aquí un archivo .csv de Google Password Manager.<br>"
				+ "Se leerán las columnas name, username y password.</html>");
		instrucciones.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		instrucciones.setForeground(colorTextPrimary);
		instrucciones.setHorizontalAlignment(SwingConstants.CENTER);
		instrucciones.setOpaque(true);
		instrucciones.setBackground(colorInput);
		instrucciones.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(colorBorder, 1, true),
				new EmptyBorder(24, 18, 24, 18)));
		instrucciones.setTransferHandler(new TransferHandler() {
			@Override
			public boolean canImport(TransferSupport support) {
				return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
			}

			@Override
			public boolean importData(TransferSupport support) {
				if (!canImport(support)) {
					return false;
				}
				try {
					Transferable transferable = support.getTransferable();
					Object datos = transferable.getTransferData(DataFlavor.javaFileListFlavor);
					if (!(datos instanceof List<?>) || ((List<?>) datos).isEmpty()
							|| !(((List<?>) datos).get(0) instanceof File)) {
						return false;
					}
					return importador.importar(((File) ((List<?>) datos).get(0)).toPath(), dialogo);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(dialogo, "No se pudo leer el archivo soltado: " + e.getMessage(),
							"Error de importación", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		});
		contenido.add(instrucciones, BorderLayout.CENTER);

		BotonEstilizado seleccionar = new BotonEstilizado("Seleccionar archivo .csv", COLOR_BOTON, COLOR_BOTON_HOVER,
				8);
		seleccionar.setIcon(crearIcono("icons/folder.svg", 16));
		seleccionar.addActionListener(e -> {
			JFileChooser selector = new JFileChooser();
			selector.setFileFilter(new FileNameExtensionFilter("CSV de Google Password Manager (*.csv)", "csv"));
			if (selector.showOpenDialog(dialogo) == JFileChooser.APPROVE_OPTION
					&& importador.importar(selector.getSelectedFile().toPath(), dialogo)) {
				dialogo.dispose();
			}
		});

		BotonEstilizado cancelar = new BotonEstilizado("Cancelar", COLOR_BOTON, COLOR_BOTON_HOVER, 8);
		cancelar.setIcon(crearIcono("icons/x.svg", 16));
		cancelar.addActionListener(e -> dialogo.dispose());

		JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		acciones.setOpaque(false);
		acciones.add(cancelar);
		acciones.add(seleccionar);
		contenido.add(acciones, BorderLayout.SOUTH);

		dialogo.setContentPane(contenido);
		dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialogo.setMinimumSize(new Dimension(480, 210));
		dialogo.pack();
		dialogo.setLocationRelativeTo(dialogo.getOwner());
		dialogo.setVisible(true);
	}

	private FlatSVGIcon crearIcono(String recurso, int tamano) {
		FlatSVGIcon icono = new FlatSVGIcon(recurso, tamano, tamano);
		icono.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
		return icono;
	}

	@FunctionalInterface
	public interface ImportadorArchivo {
		boolean importar(Path archivo, JDialog dialogo);
	}
}
