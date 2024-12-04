package PointOfSale;

import Cart.CartFrame;
import Interfaces.*;
import LoginFrame.MainFrame;
import Numpad.Numpad;
import Product.Entity.Payment;
import Product.Entity.Product;
import Product.Repository.PaymentRepository;
import Product.Repository.ProductRepository;
import SwingComponents.AnimateMessage;
import SwingComponents.MyButton;
import SwingComponents.MyTextField2;
import SwingComponents.Renderer.MyComboBox;
import SwingComponents.Renderer.MyTableModel;
import User.User;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.*;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Queue;

public class ProductListTable extends JPanel implements TableSettings, ShoppingCart {

    private final PaymentRepository paymentRepository;
    private ProductSelected productSelected;
    private final ProductRepository productRepository;
    private final Numpad numpad;
    private final SwitchPanel switchPanel;

    private MyTextField2 field;
    private JTable table;
    private DefaultTableModel model;
    private List<Product> productList;
    private JComboBox<String> productType;
    private Queue<Payment> paymentQueue;
    private CartFrame cart;
    private static MyButton currentButton;
    private ResetPanel reset;
    private final User user;


    public ProductListTable(User user, SwitchPanel switchPanel, Numpad numpad) {
        this.numpad = numpad;
        this.user = user;
        this.switchPanel = switchPanel;
        paymentRepository = new PaymentRepository();
        productRepository = new ProductRepository();
        setLayout(new MigLayout("fill, insets 10", "[grow]", "[grow, fill]"));
        setOpaque(false);

        buttons();
        productChoice();
        displayData();
        displayUser();
    }

    private void displayUser() {
        JPanel panel = new JPanel(new MigLayout("fill,insets 0")) {
            private final Image image = new ImageIcon(getClass().getResource("/Images/user2.png")).getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setColor(new Color(18, 60, 70));
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 16));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString("Admin", 80, (getHeight() + fm.getAscent()) / 2);

                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(20, (getHeight() - image.getHeight(null)) / 2 - 5, image.getWidth(null) + 10, image.getHeight(null) + 10, 200, 200);
                g2d.drawImage(image, 25, (getHeight() - image.getHeight(null)) / 2, this);
                g2d.dispose();
            }
        };
        JButton button = new JButton();
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(25, 25));
        Image image = new ImageIcon(getClass().getResource("/Images/logout.png")).getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        button.addActionListener(_ -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.dispose();
            SwingUtilities.invokeLater(MainFrame::new);
        });
        button.setIcon(new ImageIcon(image));
        button.setFocusable(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        panel.add(button, "pos 80% 30%");


        JPanel panel1 = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(Color.BLACK);


                Font font;
                try {
                    font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/Font/Digital-7.ttf")).deriveFont(24f);
                } catch (Exception e) {
                    font = new Font("Arial", Font.BOLD, 20);
                }
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String date = now.format(dateFormatter);
                String time = now.format(timeFormatter);
                g2d.drawString(date, (getWidth() - fm.stringWidth(date)) / 2, (getHeight() - fm.getAscent()) / 2);
                g2d.drawString(time, (getWidth() - fm.stringWidth(time)) / 2, (getHeight() - fm.getAscent()) / 2 + fm.getHeight());

                g2d.dispose();
            }
        };
        panel1.setBackground(new Color(255, 255, 240));
        add(panel1);
        Timer timer = new Timer(1000, _ -> panel1.repaint());
        timer.start();

        add(panel1, "pos 33% 2%,w 23%, align center, h 70!");
        add(panel, "pos 0 2%, w 250!, align left, h 70!");
    }


    private void buttons() {
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10, alignx right"));
        buttonPanel.setBackground(new Color(255, 255, 240));

        MyButton addButton = createButton("Add", false, "ADD_PRODUCT");
        MyButton buyButton = createButton("Buy", true, "BUY_PRODUCT");
        currentButton = buyButton;
        MyButton markButton = createButton("Update", false, "PRICE_ADJUSTMENT");
        MyButton salesButton = createButton("Sales", false, null);
        MyButton cartButton = createButton("Receipt", false, null);
        MyButton resetButton = createButton("Clear", false, null);

        buttonPanel.add(addButton, "cell 0 0");
        buttonPanel.add(buyButton, "cell 1 0");
        buttonPanel.add(markButton, "cell 2 0");
        buttonPanel.add(salesButton, "cell 0 1");
        buttonPanel.add(cartButton, "cell 1 1");
        buttonPanel.add(resetButton, "cell 2 1");


        add(buttonPanel, "cell 0 0, w 50%, h 100!, align right");
    }


    private MyButton createButton(String label, boolean isPrimary, String panelName) {
        MyButton button = new MyButton(label, isPrimary, false);
        button.setPreferredSize(new Dimension(100, 50));
        if (panelName != null) {
            button.addActionListener(_ -> {
                switchPanelTo(panelName);
                highlightButton(button);
            });
        } else {
            button.addActionListener(_ -> {
                if (label.equalsIgnoreCase("Clear")) {
                    if (paymentQueue != null && !paymentQueue.isEmpty()) {
                        AnimateMessage.showMessage("Cart Reset", false, (MigLayout) getLayout(), this, 65, 18, 25, false);
                        paymentQueue.clear();
                    }
                    productType.setSelectedItem(0);
                    refreshTableData();
                    if (cart != null) {
                        cart.resetFields();
                    }
                    reset.resetFields();
                }
                if (label.equalsIgnoreCase("Receipt")) {
                    cart = new CartFrame(paymentQueue, paymentRepository, this, this);
                }
                if (label.equalsIgnoreCase("Sales")) {
                    new SalesPanel(paymentRepository);
                }
            });
        }
        return button;
    }

    private void highlightButton(MyButton button) {
        if (currentButton != null) {
            currentButton.setPrimary(false);
        }
        currentButton = button;
        currentButton.setPrimary(true);
    }

    private void productChoice() {
        JPanel panel = new JPanel(new MigLayout("insets 0, gap 10, fillx", "[grow, fill]", "[]10[]"));
        panel.setOpaque(false);

        JLabel label = new JLabel("Select Product Type: ");
        panel.add(label, "split 2");

        productType = new JComboBox<>();
        productType.setRenderer(new MyComboBox());
        productType.setUI(new BasicComboBoxUI() {
            @Override
            protected ComboPopup createPopup() {
                ComboPopup popup = super.createPopup();
                JList<?> list = popup.getList();
                list.setBackground(Color.LIGHT_GRAY);
                list.setSelectionBackground(productType.getBackground());
                list.setSelectionForeground(Color.BLACK);
                list.setFont(new Font("Arial", Font.PLAIN, 15));
                list.setFixedCellHeight(25);
                return popup;
            }
        });
        productType.addItem("ALL");
        productType.addItem("Ballpen");
        productType.addItem("Crayons");
        productType.addItem("Notebook");
        productType.addItem("Bondpaper");
        productType.setPreferredSize(new Dimension(150, 25));
        productType.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        productType.addActionListener(_ -> refreshTable());

        panel.add(productType, "wrap");

        JLabel label2 = new JLabel("Search by Product Id:");
        panel.add(label2, "split 2");

        field = new MyTextField2();
        field.setFocusListener(numpad);
        field.setPreferredSize(new Dimension(150, 30));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        panel.add(field, "wrap");
        add(panel, "cell 0 1, w 50%");
    }

    private void displayData() {
        model = new DefaultTableModel();
        model.addColumn("PRODUCT TYPE");
        model.addColumn("PRODUCT ID");
        model.addColumn("BRAND NAME");
        model.addColumn("STOCK");
        model.addColumn("PRICE");

        table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                DefaultTableCellRenderer renderer;

                if (table.isEnabled()) {
                    renderer = new MyTableModel();
                } else {
                    renderer = new DefaultTableCellRenderer();
                }

                renderer.setHorizontalAlignment(SwingConstants.CENTER);
                return renderer;
            }
        };
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(150);
            } else if (i == 1) {
                column.setPreferredWidth(100);
            } else if (i == 2) {
                column.setPreferredWidth(200);
            } else if (i == 3 || i == 4) {
                column.setPreferredWidth(80);
            }
        }


        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row == -1) return;
                if (productSelected == null) return;

                int id = (int) model.getValueAt(row, 1);
                String type = model.getValueAt(row, 0).toString();
                for (Product product : productList) {
                    if (product.getProductId() == id && product.getProductType().equalsIgnoreCase(type)) {
                        productSelected.productSelected(product);
                        break;
                    }
                }
            }
        });

        table.setRowHeight(35);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);
        table.setBackground(Color.WHITE);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);

        displayProductType();

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setFont(new Font("Arial", Font.PLAIN, 14));
        header.setForeground(Color.BLACK);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        add(scroll, "cell 0 2, w 100%, h 100%");
    }

    public void displayProductType() {
        try {
            productList = productRepository.getProductByType(productType.getSelectedItem().toString().trim());
        } catch (IOException _) {

        }
        model.setRowCount(0);
        productList.forEach(product -> model.addRow(
                new Object[]{product.getProductType(),
                        product.getProductId(),
                        product.getBrandName(),
                        product.getProductQty(),
                        String.format("%.2f", product.getProductPrice())
                }));
    }


    @Override
    public void refreshTableData() {
        model.setRowCount(0);
        String selectedProductType = "All";

        try {
            productList = productRepository.getProductByType(selectedProductType);
            for (Product product : productList) {
                if (productType.getSelectedItem().toString().equalsIgnoreCase(product.getProductType())) {
                    model.addRow(new Object[]{
                            product.getProductType(),
                            product.getProductId(),
                            product.getBrandName(),
                            product.getProductQty(),
                            String.format("%.2f", product.getProductPrice())
                    });
                }
                if (productType.getSelectedItem().toString().equalsIgnoreCase("All")) {
                    model.addRow(new Object[]{
                            product.getProductType(),
                            product.getProductId(),
                            product.getBrandName(),
                            product.getProductQty(),
                            String.format("%.2f", product.getProductPrice())
                    });
                }
            }
        } catch (Exception _) {

        }
    }

    @Override
    public void refreshComboBOx() {
        productType.setSelectedItem("All");
    }

    @Override
    public void addCanceledQuantity(Product productCancelled) {
        int i = 0;
        for (Product product : productList) {
            if (product.getProductType().equalsIgnoreCase(productCancelled.getProductType()) && product.getProductId() == productCancelled.getProductId()) {
                productList.set(i, new Product(productCancelled.getProductType(),
                        productCancelled.getProductId(),
                        productCancelled.getBrandName(),
                        product.getProductQty() + productCancelled.getProductQty(),
                        productCancelled.getProductPrice()));
            }
            i++;
        }
    }

    @Override
    public void reduceQtyInTable(Product product) {
        int i = 0;
        for (Product p : productList) {
            if (product.getProductId() == p.getProductId() && product.getProductType().equalsIgnoreCase(p.getProductType())) {
                int stock = p.getProductQty() - product.getProductQty();
                productList.set(i, new Product(p.getProductType(), p.getProductId(), p.getBrandName(), stock, p.getProductPrice()));
                refreshTable();
                break;
            }
            i++;
        }
    }

    @Override
    public void refreshTable() {
        model.setRowCount(0);
        if (productType == null) return;

        String selectedType = (productType.getSelectedItem() != null)
                ? productType.getSelectedItem().toString().trim()
                : "ALL";

        if (productList == null) return;
        for (Product product : productList) {

            String searchText = field.getText().trim();

            if ((selectedType.equalsIgnoreCase("ALL") || product.getProductType().equalsIgnoreCase(selectedType))
                    && (searchText.isEmpty() || Integer.toString(product.getProductId()).startsWith(searchText))) {
                model.addRow(new Object[]{
                        product.getProductType(),
                        product.getProductId(),
                        product.getBrandName(),
                        product.getProductQty(),
                        String.format("%.2f", product.getProductPrice())
                });
            }
        }
    }

    public void setListener(ProductSelected productSelected, ResetPanel resetPanel) {
        this.productSelected = productSelected;
        this.reset = resetPanel;
    }

    @Override
    public void addToCart(Queue<Payment> paymentQueue) {
        this.paymentQueue = paymentQueue;
    }

    @Override
    public void success(boolean bln) {
        if (bln) {
            AnimateMessage.showMessage("Payment Successfully", false, (MigLayout) getLayout(), this, 65, 18, 25, false);
        } else {
            AnimateMessage.showMessage("Payment Failed", false, (MigLayout) getLayout(), this, 65, 18, 25, false);
        }
    }

    @Override
    public void removeAll() {
        if (paymentQueue != null) {
            paymentQueue.clear();
        }
    }

    public void switchPanelTo(String panelName) {
        refreshComboBOx();
        refreshTable();
        removeAll();

        switchPanel.switchPanelTo(panelName);
        reset.resetFields();
    }
}
