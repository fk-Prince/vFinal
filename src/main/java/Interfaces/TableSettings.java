package Interfaces;

import Product.Entity.Product;

public interface TableSettings {
    void refreshComboBOx();

    void addCanceledQuantity(Product productCancelled);

    void reduceQtyInTable(Product product);

    void refreshTable();

    void refreshTableData();
}
