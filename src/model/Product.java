package model;

public class Product {
    private int productId;
    private String productName;
    private int qtyAvailable;
    private double price;

    public Product() {}

    public Product(int productId, String productName, int qtyAvailable, double price) {
        this.productId = productId;
        this.productName = productName;
        this.qtyAvailable = qtyAvailable;
        this.price = price;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQtyAvailable() { return qtyAvailable; }
    public void setQtyAvailable(int qtyAvailable) { this.qtyAvailable = qtyAvailable; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
