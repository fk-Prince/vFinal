package SwingComponents.Renderer;

import Cart.CartFrame;
import Interfaces.TableSettings;
import Product.Entity.Payment;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Queue;

public class MyTableCell implements TableCellRenderer, TableCellEditor {

    private JButton button;
    private boolean isRowSelected;


    public MyTableCell(JTable table, Queue<Payment> paymentQueue, TableSettings tableSettings, CartFrame cart) {

        button = new JButton() {
            private final Image image = new ImageIcon(getClass().getResource("/Images/remove.png")).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                if (isRowSelected) {
                    g2d.setColor(new Color(56, 161, 107));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }

                g2d.drawImage(image, (getWidth() - 10) / 2, (getHeight() - 10) / 2, 10, 10, this);
                g2d.dispose();
            }
        };


        button.addActionListener(e -> {
            Point point = button.getLocation();
            int row = table.rowAtPoint(point);

            if (row != -1 && row < table.getRowCount()) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.removeRow(row);

                Payment paymentToRemove = null;
                int index = 0;
                Queue<Payment> updatedQueue = new LinkedList<>();
                for (Payment payment : paymentQueue) {
                    if (index == row) {
                        paymentToRemove = payment;
                    } else {
                        updatedQueue.add(payment);
                    }
                    index++;
                }

                if (paymentToRemove != null) {
                    tableSettings.addCanceledQuantity(paymentToRemove);
                    paymentQueue.clear();
                    paymentQueue.addAll(updatedQueue);
                    tableSettings.refreshTable();
                    isRowSelected = false;
                }
                cart.getTotal();
            }
        });

        button.setPreferredSize(new Dimension(12, 12));
        button.setFocusable(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        return true;
    }

    @Override
    public void cancelCellEditing() {
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        isRowSelected = table.isRowSelected(row);
        button.repaint();
        return button;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        isRowSelected = table.isRowSelected(row);
        button.repaint();
        return button;
    }
}
