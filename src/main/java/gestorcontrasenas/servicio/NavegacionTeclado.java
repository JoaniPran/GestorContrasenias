package gestorcontrasenas.servicio;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import java.awt.AWTKeyStroke;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import java.awt.event.ActionEvent;

public final class NavegacionTeclado {

    private NavegacionTeclado() {
    }

    public static void inicializarNavegacionGlobal() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

        Set<AWTKeyStroke> forwardKeys = new HashSet<>(
                manager.getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_DOWN, 0));
        manager.setDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);

        Set<AWTKeyStroke> backwardKeys = new HashSet<>(
                manager.getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_UP, 0));
        manager.setDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
    }

    public static void vincularBotonPorDefecto(JRootPane rootPane, JButton botonPrincipal) {
        if (rootPane != null && botonPrincipal != null) {
            rootPane.setDefaultButton(botonPrincipal);
        }
    }

    public static void habilitarSalidaTabla(JTable tabla) {
    var inputMap = tabla.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    var actionMap = tabla.getActionMap();

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "navegarArribaOSalir");
    actionMap.put("navegarArribaOSalir", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int filaSeleccionada = tabla.getSelectedRow();
            if (filaSeleccionada <= 0) {
                tabla.clearSelection();
                tabla.transferFocusBackward(); // Salir al componente anterior
            } else {
                tabla.setRowSelectionInterval(filaSeleccionada - 1, filaSeleccionada - 1);
            }
        }
    });

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "navegarAbajoOSalir");
    actionMap.put("navegarAbajoOSalir", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int filaSeleccionada = tabla.getSelectedRow();
            int totalFilas = tabla.getRowCount();
            if (filaSeleccionada == -1 || filaSeleccionada >= totalFilas - 1) {
                tabla.clearSelection();
                tabla.transferFocus(); // Salir al siguiente componente
            } else {
                tabla.setRowSelectionInterval(filaSeleccionada + 1, filaSeleccionada + 1);
            }
        }
    });

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "salirTablaSiguiente");
    actionMap.put("salirTablaSiguiente", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            tabla.transferFocus();
        }
    });

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK), "salirTablaAnterior");
    actionMap.put("salirTablaAnterior", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            tabla.transferFocusBackward();
        }
    });

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escaparTabla");
    actionMap.put("escaparTabla", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            tabla.clearSelection();
            tabla.transferFocusBackward();
        }
    });
}
}