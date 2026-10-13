package gestorcontrasenas.interfaz;

import gestorcontrasenas.modelo.RegistroContrasena;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.function.IntSupplier;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public final class ModeloCredenciales extends DefaultTableModel {
	private static final String[] COLUMNAS = {"Servicio / Aplicación", "Usuario / Email", "Contraseña"};
	private final IntSupplier filasSeleccionadas;
	private final Set<String> serviciosExpandidos = new HashSet<>();
	private final Map<String, Integer> cantidadPorServicio = new HashMap<>();

	public ModeloCredenciales(IntSupplier filasSeleccionadas) {
		super(COLUMNAS, 0);
		this.filasSeleccionadas = Objects.requireNonNull(filasSeleccionadas, "filasSeleccionadas");
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return column == 2 && filasSeleccionadas.getAsInt() <= 1
				&& !esFilaCabecera(row) && !esEncabezadoContraido(row);
	}

	public boolean esFilaCabecera(int fila) {
		return fila >= 0 && fila < getRowCount()
				&& getValueAt(fila, 1) == null && getValueAt(fila, 2) == null;
	}

	public boolean esEncabezadoContraido(int fila) {
		if (!esFilaCabecera(fila)) {
			return false;
		}
		return !serviciosExpandidos.contains(normalizarServicio(getValueAt(fila, 0).toString()));
	}

	public String claveRegistro(int fila) {
		return getValueAt(fila, 0).toString() + '\u0000' + getValueAt(fila, 1).toString();
	}

	public int cantidadPorServicio(String servicio) {
		return cantidadPorServicio.getOrDefault(normalizarServicio(servicio), 0);
	}

	public boolean servicioExpandido(String servicio) {
		return serviciosExpandidos.contains(normalizarServicio(servicio));
	}

	public void alternarGrupoServicio(String servicio) {
		String clave = normalizarServicio(servicio);
		if (!serviciosExpandidos.remove(clave)) {
			serviciosExpandidos.add(clave);
		}
	}

	public void reconstruirGruposServicios() {
		cantidadPorServicio.clear();
		for (int fila = 0; fila < getRowCount(); fila++) {
			if (esFilaCabecera(fila)) {
				continue;
			}
			String servicio = normalizarServicio(getValueAt(fila, 0).toString());
			if (!servicio.isEmpty()) {
				cantidadPorServicio.put(servicio, cantidadPorServicio.getOrDefault(servicio, 0) + 1);
			}
		}
		serviciosExpandidos.retainAll(cantidadPorServicio.keySet());
	}

	public void actualizarFiltro(JTable tabla, TableRowSorter<ModeloCredenciales> sorter, String consulta) {
		String consultaNormalizada = consulta.trim().toLowerCase(Locale.ROOT);
		Set<String> serviciosCoincidentes = new HashSet<>();
		for (int fila = 0; fila < getRowCount(); fila++) {
			if (!esFilaCabecera(fila) && coincideConBusqueda(fila, consultaNormalizada)) {
				serviciosCoincidentes.add(normalizarServicio(getValueAt(fila, 0).toString()));
			}
		}

		sorter.setRowFilter(new RowFilter<ModeloCredenciales, Integer>() {
			@Override
			public boolean include(Entry<? extends ModeloCredenciales, ? extends Integer> entry) {
				int fila = entry.getIdentifier();
				String servicio = normalizarServicio(getValueAt(fila, 0).toString());
				boolean coincide = coincideConBusqueda(fila, consultaNormalizada);
				boolean esCabecera = esFilaCabecera(fila);
				int cantidad = cantidadPorServicio(servicio);
				if (cantidad < 2) {
					return !esCabecera && coincide;
				}
				if (consultaNormalizada.isEmpty()) {
					return esCabecera || serviciosExpandidos.contains(servicio);
				}
				if (!serviciosCoincidentes.contains(servicio)) {
					return false;
				}
				return esCabecera || (serviciosExpandidos.contains(servicio) && coincide);
			}
		});
		tabla.setDragEnabled(consultaNormalizada.isEmpty());
	}

	public boolean reordenarGrupoDesdeVista(JTable tabla, int filaOrigenVista, int filaDestinoVista) {
		if (filaOrigenVista < 0 || filaOrigenVista >= tabla.getRowCount()
				|| filaDestinoVista < 0 || filaDestinoVista > tabla.getRowCount()) {
			return false;
		}

		int filaOrigen = tabla.convertRowIndexToModel(filaOrigenVista);
		String servicioOrigen = normalizarServicio(getValueAt(filaOrigen, 0).toString());
		if (servicioOrigen.isEmpty()) {
			return false;
		}
		String servicioDestino = null;
		if (filaDestinoVista < tabla.getRowCount()) {
			int filaDestino = tabla.convertRowIndexToModel(filaDestinoVista);
			servicioDestino = normalizarServicio(getValueAt(filaDestino, 0).toString());
		}
		if (servicioOrigen.equals(servicioDestino)) {
			return false;
		}

		LinkedHashMap<String, List<Object[]>> grupos = agruparRegistros();
		List<String> orden = new ArrayList<>(grupos.keySet());
		List<Object[]> grupoMovido = grupos.remove(servicioOrigen);
		int indiceDestino = servicioDestino == null ? orden.size() : orden.indexOf(servicioDestino);
		if (indiceDestino < 0) {
			return false;
		}
		orden.remove(servicioOrigen);
		if (servicioDestino != null && orden.indexOf(servicioDestino) < indiceDestino) {
			indiceDestino--;
		}
		orden.add(Math.min(indiceDestino, orden.size()), servicioOrigen);
		reemplazarConOrden(orden, grupos, servicioOrigen, grupoMovido);
		return true;
	}

	public void normalizarOrdenGrupos() {
		LinkedHashMap<String, List<Object[]>> grupos = agruparRegistros();
		reemplazarConOrden(new ArrayList<>(grupos.keySet()), grupos, null, null);
	}

	public List<RegistroContrasena> obtenerRegistros() {
		List<RegistroContrasena> registros = new ArrayList<>();
		for (int fila = 0; fila < getRowCount(); fila++) {
			if (!esFilaCabecera(fila)) {
				registros.add(registroEnFila(fila));
			}
		}
		return registros;
	}

	public RegistroContrasena registroEnFila(int fila) {
		return new RegistroContrasena(getValueAt(fila, 0).toString(),
				getValueAt(fila, 1).toString(), getValueAt(fila, 2).toString());
	}

	public static String normalizarServicio(String servicio) {
		return servicio == null ? "" : servicio.trim().toLowerCase(Locale.ROOT);
	}

	private boolean coincideConBusqueda(int fila, String consulta) {
		if (consulta.isEmpty()) {
			return !esFilaCabecera(fila);
		}
		if (esFilaCabecera(fila)) {
			return false;
		}
		String servicio = getValueAt(fila, 0).toString().toLowerCase(Locale.ROOT);
		String usuario = getValueAt(fila, 1).toString().toLowerCase(Locale.ROOT);
		return servicio.contains(consulta) || usuario.contains(consulta);
	}

	private LinkedHashMap<String, List<Object[]>> agruparRegistros() {
		LinkedHashMap<String, List<Object[]>> grupos = new LinkedHashMap<>();
		for (int fila = 0; fila < getRowCount(); fila++) {
			if (esFilaCabecera(fila)) {
				continue;
			}
			String servicio = normalizarServicio(getValueAt(fila, 0).toString());
			grupos.computeIfAbsent(servicio, clave -> new ArrayList<>()).add(new Object[]{
					getValueAt(fila, 0), getValueAt(fila, 1), getValueAt(fila, 2)});
		}
		return grupos;
	}

	private void reemplazarConOrden(List<String> orden, Map<String, List<Object[]>> grupos,
			String servicioMovido, List<Object[]> grupoMovido) {
		setRowCount(0);
		for (String servicio : orden) {
			List<Object[]> registros = servicio.equals(servicioMovido) ? grupoMovido : grupos.get(servicio);
			if (registros.size() > 1) {
				addRow(new Object[]{registros.get(0)[0], null, null});
			}
			for (Object[] registro : registros) {
				addRow(registro);
			}
		}
	}
}
