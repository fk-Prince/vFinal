package Cart;

import Interfaces.ResetPanel;
import Interfaces.ShoppingCart;
import Interfaces.TableSettings;
import Product.Entity.Payment;
import Product.Repository.PaymentRepository;
import SwingComponents.MyButton;
import SwingComponents.Renderer.MyTableCell;
import SwingComponents.Renderer.MyTableModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.util.Queue;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class CartFrame extends JPanel implements ResetPanel {
    private static JFrame frame;
    private final Queue<Payment> paymentQueue;
    private final CartService cartService;
    private JTextField[] fields;
    private MyButton payButton;
    private final TableSettings tableSettings;
    private final CartFrame cart;
    private final ShoppingCart shoppingCart;
    private DefaultTableModel model;

    public CartFrame(Queue<Payment> paymentQueue, PaymentRepository paymentRepository, TableSettings tableSettings, ShoppingCart shoppingCart) {
        this.paymentQueue = paymentQueue;
        this.tableSettings = tableSettings;
        this.cartService = new CartService(paymentRepository);
        this.shoppingCart = shoppingCart;
        cart = this;
        init();
        title();
        displayPayments();
        inputs();
    }

    private void init() {
        if (frame != null) frame.dispose();

        frame = new JFrame("Receipt");
        frame.setSize(600, 766);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.add(this);

        setLayout(new MigLayout("fill, insets 0"));
    }

    private void title() {
        JPanel panel = new JPanel(new MigLayout("fill,insets 0")) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.setFont(new Font("Arial", Font.BOLD, 30));
                g2d.setColor(Color.BLACK);
                String title = "Receipt";
                g2d.drawString(title, (getWidth() - fm.stringWidth(title)) / 2 - 30, 40);
                g2d.dispose();
            }
        };

        if (paymentQueue != null && !paymentQueue.isEmpty()) {
            panel.add(new JLabel("Invoice ID: " + cartService.getInvoiceId()), "pos 3% 75%");
        }

        add(panel, "dock north, h 10%");
    }

    private void displayPayments() {
        String[] columns = {"Qty", "Type", "Brandname / ID", "Price", "Subtotal", ""};
        model = new DefaultTableModel();
        for (String column : columns) {
            model.addColumn(column);
        }

        if (paymentQueue != null) {
            for (Payment payment : paymentQueue) {
                model.addRow(new Object[]{
                        payment.getProductQty(),
                        payment.getProductType(),
                        payment.getBrandName() + "  /  " + payment.getProductId(),
                        String.format("%.2f", payment.getProductPrice()),
                        String.format("%.2f", payment.computeWithoutTax())
                });
            }
        }

        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == 5) {
                    return new MyTableCell(this, paymentQueue, tableSettings, cart);
                }

                MyTableModel renderer = new MyTableModel();
                if (column == 0) {
                    renderer.setHorizontalAlignment(SwingConstants.CENTER);
                } else if (column == 1) {
                    renderer.setHorizontalAlignment(SwingConstants.LEFT);
                } else if (column == 2) {
                    renderer.setHorizontalAlignment(SwingConstants.LEFT);
                } else if (column == 3 || column == 4) {
                    renderer.setHorizontalAlignment(SwingConstants.RIGHT);
                }
                return renderer;
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == 5) {
                    return new MyTableCell(this, paymentQueue, tableSettings, cart);
                }

                return super.getCellEditor(row, column);
            }

        };

        for (int i = 0; i < columns.length; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(60);
            } else if (i == 1) {
                column.setPreferredWidth(125);
            } else if (i == 2) {
                column.setPreferredWidth(175);
            } else if (i == 3 || i == 4) {
                column.setPreferredWidth(150);
            } else {
                column.setPreferredWidth(30);
            }
        }

        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(35);
        table.setFocusable(false);
        table.setBackground(Color.WHITE);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);


        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setFont(new Font("Arial", Font.PLAIN, 14));
        header.setForeground(Color.BLACK);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, "dock center");
    }

    private void inputs() {
        JPanel inputHolder = new JPanel(new MigLayout("fill,insets 0"));
        String[] labels = {"SUBTOTAL", "VAT(0.12)", "TOTAL DUE", "CASH", "CHANGE"};
        fields = new JTextField[labels.length];

        int y = 2;
        for (int i = 0; i < fields.length; i++) {
            JLabel label = new JLabel(labels[i]);
            fields[i] = new JTextField();
            fields[i].setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 0)
                    , BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            fields[i].setHorizontalAlignment(SwingConstants.RIGHT);
            if (i != 3) {
                fields[i].setEditable(false);
                fields[i].setFocusable(false);
            }

            if (i == 3) {
                if (paymentQueue != null && paymentQueue.isEmpty()) {
                    fields[i].setEnabled(false);
                    fields[i].setFocusable(false);
                }
                inputReader(fields[i]);
            }
            inputHolder.add(label, "pos 50% " + (y + 2) + "%,wrap");
            inputHolder.add(fields[i], "pos 65% " + y + "%,wrap,w 150!,h 8%");
            y += 18;
        }
        getTotal();

        payButton = new MyButton("PAY", true, false);
        payButton.setPreferredSize(new Dimension(100, 50));
        payButton.setEnabled(false);
        payButton.addActionListener(_ -> {
            try {
                double cash = Double.parseDouble(fields[3].getText().trim());
                if (cartService.processPayment(paymentQueue, cash)) {
                    resetFields();
                    Timer timer = new Timer(2000, _ -> {
                        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                        frame.dispose();
                    });
                    timer.setDelay(0);
                    timer.setRepeats(false);
                    timer.start();
                    model.setColumnCount(model.getColumnCount() - 1);
                    shoppingCart.success(true);
                    payButton.setVisible(false);
                }
            } catch (Exception _) {

            }
        });
        inputHolder.add(payButton, "pos 10% 20%, w 150!, h 80!");

        add(inputHolder, "dock south, h 150!");
    }

    public void getTotal() {
        if (paymentQueue != null && !paymentQueue.isEmpty()) {
            double subtotal = paymentQueue.stream().mapToDouble(Payment::computeWithoutTax).sum();
            fields[0].setText(String.format("%.2f", subtotal));
            fields[1].setText(String.format("%.2f", Payment.computeTax(subtotal)));
            fields[2].setText(String.format("%.2f", Payment.computeTotalDue(subtotal)));
            try {
                double cash = Double.parseDouble(fields[3].getText().trim());
                fields[4].setText(String.format("%.2f", cash - Payment.computeTotalDue(subtotal)));
            } catch (Exception e) {
                if (fields[3].getText().trim().isEmpty()) {
                    fields[4].setText("");
                } else {
                    fields[4].setText("Invalid Input");
                }

            }
        } else {
            if (paymentQueue == null) {
                return;
            }
            fields[0].setText("");
            fields[1].setText("");
            fields[2].setText("");
            fields[4].setText("");
            payButton.setEnabled(false);
        }
    }

    private void inputReader(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateChange();
            }

            private void updateChange() {
                try {
                    double cash = Double.parseDouble(field.getText().trim());
                    double total = Double.parseDouble(fields[2].getText().trim());
                    if (cash < total) {
                        if (cash <= 0) {
                            fields[4].setText("Enter a Valid amount ");
                        } else {
                            fields[4].setText("INSUFFICIENT FUNDS");
                        }
                        payButton.setEnabled(false);
                    } else {
                        fields[4].setText(String.format("%.2f", cash - total));
                        payButton.setEnabled(true);
                    }
                } catch (NumberFormatException ex) {
                    if (paymentQueue == null || paymentQueue.isEmpty()) {
                        return;
                    }
                    if (fields[3].getText().trim().isEmpty()) {
                        fields[4].setText("");
                    } else {
                        fields[4].setText("Invalid Input");
                    }
                    payButton.setEnabled(false);
                }
            }
        });
    }

    @Override
    public void resetFields() {
        cartService.resetInvoiceId();
        
        if (paymentQueue != null) {
            paymentQueue.clear();
        }
    }

}
