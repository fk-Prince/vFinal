package AddProduct;

import Interfaces.TableSettings;
import PriceAdjustment.PriceAdjustmentFrame;
import Product.Entity.Product;
import Product.ProductList.Ballpen;
import Product.ProductList.Bondpaper;
import Product.ProductList.Crayons;
import Product.ProductList.Notebook;
import Product.Repository.ProductRepository;
import SwingComponents.AnimateMessage;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class AddProductService {
    private final ProductRepository productRepository;
    private final TableSettings tableSettings;
    private final PriceAdjustmentFrame priceAdjustment;
    private final JPanel parentPanel;

    public AddProductService(ProductRepository productRepository, TableSettings tableSettings, PriceAdjustmentFrame priceAdjustment, JPanel parentPanel) {
        this.productRepository = productRepository;
        this.tableSettings = tableSettings;
        this.priceAdjustment = priceAdjustment;
        this.parentPanel = parentPanel;
    }

    public boolean registerProduct(String productType, int productId, String productName, double productPrice, int productQty) {
        try {
            if (productQty <= 0 || productPrice <= 0) {
                AnimateMessage.showMessage("INVALID INPUT", true, (MigLayout) parentPanel.getLayout(), parentPanel, 25, 10,50, false);
                return false;
            }

            boolean isIdDuplicate = productRepository.isIdDuplicate(productType,productId);
            if (isIdDuplicate) {
                Product existingProduct = productRepository.getProductById(productId, productType);
                double formatPrice = Double.parseDouble(String.format("%.2f", productPrice));
                if (existingProduct.getBrandName().equalsIgnoreCase(productName)
                        && formatPrice == Double.parseDouble(String.format("%.2f", existingProduct.getProductPrice()))
                        && existingProduct.getProductType().equalsIgnoreCase(productType)) {
                    AnimateMessage.showMessage("Product Restock", false, (MigLayout) parentPanel.getLayout(), parentPanel, 25, 15, 50,false);
                    productRepository.updateProduct(new Product(productType, productId, productName, productQty, formatPrice));
                } else {
                    AnimateMessage.showMessage("Duplicate ID", true, (MigLayout) parentPanel.getLayout(), parentPanel, 25, 15,50, false);
                    return false;
                }
            } else {
                Product newProduct = null;
                if (productType.equalsIgnoreCase("Ballpen")) {
                    newProduct = new Ballpen(productType, productId, productName, productQty, productPrice);
                } else if (productType.equalsIgnoreCase("Notebook")){
                    newProduct = new Notebook(productType, productId, productName, productQty, productPrice);
                } else if (productType.equalsIgnoreCase("Crayons")){
                    newProduct = new Crayons(productType, productId, productName, productQty, productPrice);
                } else if (productType.equalsIgnoreCase("Bondpaper")){
                    newProduct = new Bondpaper(productType, productId, productName, productQty, productPrice);
                }
                productRepository.addNewProduct(newProduct);
                AnimateMessage.showMessage("Product Added", false, (MigLayout) parentPanel.getLayout(), parentPanel, 25, 15, 50,false);
            }

            tableSettings.refreshComboBOx();
            tableSettings.refreshTableData();
            priceAdjustment.resetFields();
            return true;
        } catch (Exception e) {
            AnimateMessage.showMessage("ERROR", true, (MigLayout) parentPanel.getLayout(), parentPanel, 25, 15, 50,false);
            e.printStackTrace();
            return false;
        }
    }

    public boolean isDigit(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
