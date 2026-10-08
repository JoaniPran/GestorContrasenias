package gestorcontrasenas.interfaz;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.function.BiPredicate;

public class TableRowTransferHandler extends TransferHandler {
	private final DataFlavor flavor = new DataFlavor(Integer.class, "Integer Row Index");
	private final JTable table;
	private final BiPredicate<Integer, Integer> alReordenar;

	public TableRowTransferHandler(JTable table, BiPredicate<Integer, Integer> alReordenar) {
		this.table = table;
		this.alReordenar = alReordenar;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		return new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[]{flavor};
			}
			@Override
			public boolean isDataFlavorSupported(DataFlavor f) {
				return f.equals(flavor);
			}
			@Override
			public Object getTransferData(DataFlavor f) throws UnsupportedFlavorException {
				if (isDataFlavorSupported(f))
					return table.getSelectedRow();
				throw new UnsupportedFlavorException(f);
			}
		};
	}

	@Override
	public int getSourceActions(JComponent c) {
		return MOVE;
	}

	@Override
	public boolean canImport(TransferSupport info) {
		return info.isDrop() && info.isDataFlavorSupported(flavor);
	}

	@Override
	public boolean importData(TransferSupport info) {
		if (!canImport(info))
			return false;
		JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
		int targetRow = dl.getRow();
		try {
			int sourceRow = (Integer) info.getTransferable().getTransferData(flavor);
			if (sourceRow < 0 || sourceRow >= table.getRowCount() || targetRow < 0
					|| targetRow > table.getRowCount()) {
				return false;
			}
			return alReordenar.test(sourceRow, targetRow);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}