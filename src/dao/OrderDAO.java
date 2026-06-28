

package dao;

import model.Order;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public List<Order> getOrdersByUser(String username) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE username = ? ORDER BY order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(new Order(
                        rs.getInt("order_id"),
                        rs.getString("username"),
                        rs.getDouble("total_amount"),
                        rs.getTimestamp("order_date")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching orders: " + e.getMessage());
        }
        return orders;
    }
}
