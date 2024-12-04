package AddProduct;

import Interfaces.ProductSelected;
import Interfaces.ResetPanel;
import Interfaces.TableSettings;
import Numpad.Numpad;
import PriceAdjustment.PriceAdjustmentFrame;
import Product.Entity.Product;
import Product.Repository.ProductRepository;
import SwingComponents.AnimateMessage;
import SwingComponents.MyButton;
import SwingComponents.MyTextField2;
import SwingComponents.Renderer.MyComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;

public class AddProductFrame extends JPanel implements ResetPanel, ProductSelected {

    private final MyTextField2[] fields;
    private final JPanel inputHolder;
    private final AddProductService productService;
    private JComboBox<String> productType;
    private final TableSettings tableSettings;

    public AddProductFrame(TableSettings tableSettings, ProductRepository productRepository, Numpad numpad, PriceAdjustmentFrame priceAdjustment) {
        this.tableSettings = tableSettings;
        setLayout(new MigLayout("fill, insets 0"));
        title();
        inputHolder = new JPanel(new MigLayout("insets 0, fill,center"));
        this.productService = new AddProductService(productRepository, tableSettings, priceAdjustment, this);
        String[] labels = {"Product Type", "Product ID", "Product BrandName", "Product Price", "Product Quantity"};
        fields = new MyTextField2[4];

        int y = 18;
        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            inputHolder.add(label, "pos 10% " + (y + 2) + "%,wrap");
            if (i == 0) {
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
                productType.addItem("Ballpen");
                productType.addItem("Crayons");
                productType.addItem("Notebook");
                productType.addItem("Bondpaper");
                productType.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                inputHolder.add(productType, "pos 40% " + y + "%,wrap,w 50%,h 8%!");
            } else {
                fields[i - 1] = new MyTextField2();
                fields[i - 1].setFocusListener(numpad);
                inputHolder.add(fields[i - 1], "pos 40% " + y + "%,wrap,w 50%,h 8%!");
            }
            y += 12;
        }

        JLabel help = new JLabel();
        Image image = new ImageIcon(getClass().getResource("/Images/help.png")).getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        help.setIcon(new ImageIcon(image));
        help.setToolTipText("<html><p style='width:300px; font-size:14px; color: white; background-color: black; padding: 10px; border-radius: 8px;'>" +
                "Pick a product from the table to add quantity.<br>"+
                "or add a new product to the list." +
                "</p></html>");
        ToolTipManager.sharedInstance().setInitialDelay(100);
        ToolTipManager.sharedInstance().setDismissDelay(5000);
        help.setFocusable(false);
        inputHolder.add(help, "pos 95% 19%");

        MyButton registerButton = new MyButton("ADD PRODUCT", true, false);
        registerButton.addActionListener(_ -> registerProduct());
        inputHolder.add(registerButton, "pos 35% " + (y + 5) + "%, w 30%, h 60!");
        add(inputHolder, "dock center");


    }

    private void registerProduct() {
        try {
            for (JTextField field : fields) {
                if (field != null && field.getText().trim().isEmpty()) {
                    AnimateMessage.showMessage("Please Fill all Fields", true, (MigLayout) getLayout(), inputHolder, 25, 15, 50, false);
                    return;
                }
            }

            String productType = this.productType.getSelectedItem().toString().trim();
            if (!productService.isDigit(fields[0].getText().trim())) {
                AnimateMessage.showMessage("PRODUCT ID SHOULD BE A NUMBER", true, (MigLayout) getLayout(), inputHolder, 25, 15, 50, false);
                return;
            }
            int productId = Integer.parseInt(fields[0].getText().trim());
            String productName = fields[1].getText().trim();
            double productPrice = Double.parseDouble(fields[2].getText().trim());
            int productQty = Integer.parseInt(fields[3].getText().trim());

            boolean success = productService.registerProduct(productType, productId, productName, productPrice, productQty);

            if (success) {
                resetFields();
                tableSettings.refreshTableData();
            }
        } catch (Exception e) {
            AnimateMessage.showMessage("INVALID INPUT", true, (MigLayout) getLayout(), inputHolder, 25, 15, 50, false);
        }
    }


    private void title() {
        JLabel label = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                FontMetrics fm = g2d.getFontMetrics();
                String title = "Register Product";
                g2d.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, 35);
                g2d.dispose();
            }
        };

        label.setFocusable(false);
        label.setFont(new Font("Arial", Font.BOLD, 30));
        label.setForeground(Color.BLACK);
        label.setPreferredSize(new Dimension(1, 50));

        add(label, "dock north");
    }


    @Override
    public void resetFields() {
        if (fields != null) {
            for (JTextField field : fields) {
                field.setText("");
            }
        }


    }

    @Override
    public void productSelected(Product product) {
        resetFields();
        if (product != null) {
            productType.setSelectedItem(product.getProductType());
            fields[0].setText(String.valueOf(product.getProductId()));
            fields[1].setText(product.getBrandName());
            fields[2].setText(String.format("%.2f", product.getProductPrice()));
        }
    }
}
