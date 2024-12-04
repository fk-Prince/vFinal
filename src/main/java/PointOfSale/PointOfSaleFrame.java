package PointOfSale;

import AddProduct.AddProductFrame;
import BuyProduct.BuyProductFrame;
import Interfaces.SwitchPanel;
import Numpad.Numpad;
import PriceAdjustment.PriceAdjustmentFrame;
import Product.Repository.PaymentRepository;
import Product.Repository.ProductRepository;
import User.User;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PointOfSaleFrame extends JFrame implements SwitchPanel {

    private CardLayout cardLayout;
    private JPanel cardPanel, mainPanel;
    private MigLayout migLayout;

    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private AddProductFrame addProduct;
    private BuyProductFrame buyProduct;
    private ProductListTable productListTable;
    private PriceAdjustmentFrame priceAdjustment;
    //private SideBar sideBar;
    private User user;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PointOfSaleFrame(null));
    }

    public PointOfSaleFrame(User user) {
        this.user = user;
        productRepository = new ProductRepository();
        paymentRepository = new PaymentRepository();
        init();
        initComponent();
        setVisible(true);
    }

    private void init() {
        int WIDTH = 1300;
        int HEIGHT = 700;

        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setUndecorated(true);
        //setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void initComponent() {
        migLayout = new MigLayout("fill, insets 0 0 0 0");
        mainPanel = new JPanel(migLayout);
        mainPanel.setBackground(new Color(255, 255, 240));
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
        cardPanel = new JPanel(new CardLayout());
        cardPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        cardLayout = (CardLayout) cardPanel.getLayout();


        Numpad numpad = new Numpad();
        numpad.setBorder(new EmptyBorder(0, 10, 10, 10));

        productListTable = new ProductListTable(user, this, numpad);
        // sideBar = new SideBar(this, this, productListTable,productListTable);

        priceAdjustment = new PriceAdjustmentFrame(productListTable, productRepository, paymentRepository, numpad);
        addProduct = new AddProductFrame(productListTable, productRepository, numpad, priceAdjustment);
        buyProduct = new BuyProductFrame(productListTable, numpad);

        productListTable.setListener(buyProduct, buyProduct);
        cardPanel.add(addProduct, "ADD_PRODUCT");
        cardPanel.add(buyProduct, "BUY_PRODUCT");
        cardPanel.add(priceAdjustment, "PRICE_ADJUSTMENT");
        cardLayout.show(cardPanel, "BUY_PRODUCT");


        mainPanel.add(productListTable, "w 60%, pos 0 0, h 100%");
        mainPanel.add(cardPanel, "w 39%, pos 60% 0, h 70%");
        mainPanel.add(numpad, "w 39%, pos 60% 70%, h 29%");
        add(mainPanel);
    }


    @Override
    public void switchPanelTo(String panelName) {
        cardLayout.show(cardPanel, panelName);

        if (panelName.equalsIgnoreCase("BUY_PRODUCT")) {
            productListTable.setListener(buyProduct, buyProduct);
        } else if (panelName.equalsIgnoreCase("ADD_PRODUCT")) {
            productListTable.setListener(addProduct, addProduct);
        } else if (panelName.equalsIgnoreCase("PRICE_ADJUSTMENT")) {
            productListTable.setListener(priceAdjustment, priceAdjustment);
        }
        mainPanel.revalidate();
        productListTable.refreshTableData();
    }
}
