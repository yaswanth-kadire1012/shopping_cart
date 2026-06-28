package dao;

import model.SalesStats;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AnalyticsDAO {

    public SalesStats getOverallSalesStats() {
        String sql = "SELECT COUNT(DISTINCT o.order_id) as total_orders, " +
                     "SUM(o.total_amount) as total_revenue, " +
                     "SUM(oi.quantity) as items_sold " +
                     "FROM orders o " +
                     "LEFT JOIN order_items oi ON o.order_id = oi.order_id";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return new SalesStats(
                    rs.getInt("total_orders"),
                    rs.getDouble("total_revenue"),
                    rs.getInt("items_sold")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error fetching sales stats: " + e.getMessage());
        }
        return new SalesStats(0, 0.0, 0);
    }
}
