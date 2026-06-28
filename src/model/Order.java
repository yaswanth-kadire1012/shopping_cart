package model;

public class Order {
    private int orderId;
    private String username;
    private double totalAmount;
    private java.sql.Timestamp orderDate;

    public Order() {}

    public Order(int orderId, String username, double totalAmount, java.sql.Timestamp orderDate) {
        this.orderId = orderId;
        this.username = username;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public java.sql.Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(java.sql.Timestamp orderDate) { this.orderDate = orderDate; }
}
