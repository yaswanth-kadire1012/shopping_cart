package model;

public class SalesStats {
    private int totalOrders;
    private double totalRevenue;
    private int itemsSold;

    public SalesStats() {}

    public SalesStats(int totalOrders, double totalRevenue, int itemsSold) {
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.itemsSold = itemsSold;
    }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public int getItemsSold() { return itemsSold; }
    public void setItemsSold(int itemsSold) { this.itemsSold = itemsSold; }
}
