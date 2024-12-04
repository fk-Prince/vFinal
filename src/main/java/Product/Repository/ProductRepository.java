package Product.Repository;

import Product.Entity.Product;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProductRepository {

    private final File parent = new File("ProductList");

    public ProductRepository() {
        try {
            if (!parent.exists()) {
                parent.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Product> getProductByType(String productType) throws IOException {
        List<Product> products = new ArrayList<>();

        if ("ALL".equalsIgnoreCase(productType.trim())) {
            for (File productFile : parent.listFiles()) {
                System.out.println(productFile);
                products.addAll(
                        new BufferedReader(new FileReader(productFile))
                                .lines()
                                .map(line -> line.split(","))
                                .map(lines -> new Product(
                                        lines[0],
                                        Integer.parseInt(lines[1]),
                                        lines[2],
                                        Integer.parseInt(lines[3]),
                                        Double.parseDouble(lines[4])
                                ))
                                .toList()
                );
            }
        } else {
            File productFile = new File(parent, productType + ".txt");

            if (productFile.exists()) {
                products = new BufferedReader(new FileReader(productFile))
                        .lines()
                        .map(line -> line.split(","))
                        .map(lines -> new Product(
                                lines[0],
                                Integer.parseInt(lines[1]),
                                lines[2],
                                Integer.parseInt(lines[3]),
                                Double.parseDouble(lines[4])
                        ))
                        .sorted(Comparator.comparingInt(Product::getProductId))
                        .collect(Collectors.toList());
            }
        }

        return products;
    }

    public boolean isIdDuplicate(String productType, int productId) throws IOException {
        File productFile = new File(parent, productType + ".txt");

        if (!productFile.exists()) {
            return false;
        }

        return new BufferedReader(new FileReader(productFile))
                .lines()
                .map(lines -> lines.split(","))
                .anyMatch(lines -> productId == Integer.parseInt(lines[1]));
    }

    public void addNewProduct(Product newProduct) throws IOException {
        String productType = newProduct.getProductType();
        File productFile = new File(parent, productType + ".txt");
        if (!productFile.exists()) productFile.createNewFile();
        boolean duplicate = false;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(productFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] lines = line.split(",");
            if (newProduct.getProductType().equalsIgnoreCase(lines[0])
                    && newProduct.getProductId() == Integer.parseInt(lines[1])
                    && newProduct.getBrandName().equalsIgnoreCase(lines[2])
                    && newProduct.getProductPrice() == Double.parseDouble(lines[4])) {
                sb.append(newProduct.getProductType()).append(",")
                        .append(newProduct.getProductId()).append(",")
                        .append(newProduct.getBrandName()).append(",")
                        .append(Integer.parseInt(lines[3]) + newProduct.getProductQty()).append(",")
                        .append(newProduct.getProductPrice()).append("\n");
                duplicate = true;
            } else {
                sb.append(line).append("\n");
            }
        }
        if (!duplicate) {
            sb.append(newProduct.getProductType()).append(",")
                    .append(newProduct.getProductId()).append(",")
                    .append(newProduct.getBrandName()).append(",")
                    .append(newProduct.getProductQty()).append(",")
                    .append(newProduct.getProductPrice()).append("\n");
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(productFile));
        bw.write(sb.toString());
        bw.close();
    }

    public void updateProduct(Product newProduct) throws IOException {

        File productFile = new File(parent, newProduct.getProductType() + ".txt");
        if (!productFile.exists()) productFile.createNewFile();
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(productFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] lines = line.split(",");
            if (newProduct.getProductType().equalsIgnoreCase(lines[0])
                    && newProduct.getProductId() == Integer.parseInt(lines[1])
                    && newProduct.getBrandName().equalsIgnoreCase(lines[2])
                    && newProduct.getProductPrice() == Double.parseDouble(lines[4])) {
                sb.append(newProduct.getProductType()).append(",")
                        .append(newProduct.getProductId()).append(",")
                        .append(newProduct.getBrandName()).append(",")
                        .append(Integer.parseInt(lines[3]) + newProduct.getProductQty()).append(",")
                        .append(newProduct.getProductPrice()).append("\n");
            } else {
                sb.append(line).append("\n");
            }
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(productFile));
        bw.write(sb.toString());
        bw.close();
    }

    public void updateProducts(Product newProductPrice) throws IOException {
        File productFile = new File(parent, newProductPrice.getProductType() + ".txt");

        if (!productFile.exists()) {
            productFile.createNewFile();  // Ensure the file exists before proceeding.
        }

        List<String> lines = new BufferedReader(new FileReader(productFile))
                .lines()
                .toList();

        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String[] liness = line.split(",");


            if (newProductPrice.getProductId() == Integer.parseInt(liness[1])) {
                sb.append(newProductPrice).append("\n");
            } else {
                sb.append(line).append("\n");
            }
        }


        BufferedWriter bw = new BufferedWriter(new FileWriter(productFile));
        bw.write(sb.toString());
        bw.close();
    }

    public Product getProductById(int id, String productType) throws IOException {
        File productFile = new File(parent, productType + ".txt");

        if (!productFile.exists()) return null;


        return new BufferedReader(new FileReader(productFile))
                .lines()
                .map(lines -> lines.split(","))
                .filter(lines -> id == Integer.parseInt(lines[1]))
                .map(lines -> new Product(
                        lines[0],
                        Integer.parseInt(lines[1]),
                        lines[2],
                        Integer.parseInt(lines[3]),
                        Double.parseDouble(lines[4])
                ))
                .findFirst()
                .orElse(null);
    }

    public List<Product> getAllProducts() throws IOException {
        List<Product> allProducts = new ArrayList<>();
        File[] files = parent.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files != null) {
            for (File productFile : files) {
                allProducts.addAll(
                        new BufferedReader(new FileReader(productFile))
                                .lines()
                                .map(lines -> lines.split(","))
                                .map(lines -> new Product(
                                        lines[0],
                                        Integer.parseInt(lines[1]),
                                        lines[2],
                                        Integer.parseInt(lines[3]),
                                        Double.parseDouble(lines[4])
                                ))
                                .toList()
                );
            }
        }
        return allProducts;
    }
}
