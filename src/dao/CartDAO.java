package dao;

import model.CartItem;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {
    // fetches the cart id for a given username or creates a new cart if not exists
    // Fetches existing cart ID for a user or creates a new cart if none exists
    public int getOrCreateCartId(String username) {

        // SQL query to find cart_id for given username
        String selectSql = "SELECT cart_id FROM cart WHERE username = ?";

        // Open DB connection and prepare SELECT query
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

            // Set username in query (replace ?)
            selectStmt.setString(1, username);

            // Execute query and get result
            ResultSet rs = selectStmt.executeQuery();

            // If cart exists, return its ID
            if (rs.next()) {
                return rs.getInt("cart_id");
            }

            // If cart does not exist, create a new cart
            String insertSql = "INSERT INTO cart (username) VALUES (?)";

            // Prepare INSERT statement and request generated keys (cart_id)
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

                // Set username for new cart
                insertStmt.setString(1, username);

                // Execute insert
                insertStmt.executeUpdate();

                // Retrieve auto-generated cart_id
                ResultSet keys = insertStmt.getGeneratedKeys();

                // If key exists, return it
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

        } catch (SQLException e) {
            // Handle database errors
            System.out.println("Error accessing cart: " + e.getMessage());
        }

        // Return -1 if operation fails
        return -1;
    }

    // Retrieves all items in a cart
    public List<CartItem> getCartItems(int cartId) {

        // List to store cart items
        List<CartItem> items = new ArrayList<>();

        // SQL query to fetch cart items
        String sql = "SELECT * FROM cart_items WHERE cart_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set cart ID in query
            stmt.setInt(1, cartId);

            // Execute query
            ResultSet rs = stmt.executeQuery();

            // Iterate through result set
            while (rs.next()) {

                // Create CartItem object from DB row and add to list
                items.add(new CartItem(
                        rs.getInt("id"),
                        rs.getInt("cart_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching cart items: " + e.getMessage());
        }

        // Return list of items
        return items;
    }

    // Returns total price of cart
    public double getCartTotal(int cartId) {

        // SQL query to get cart total
        String sql = "SELECT cart_total FROM cart WHERE cart_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set cart ID
            stmt.setInt(1, cartId);

            // Execute query
            ResultSet rs = stmt.executeQuery();

            // If result exists, return total
            if (rs.next()) {
                return rs.getDouble("cart_total");
            }

        } catch (SQLException e) {
            System.out.println("Error fetching cart total: " + e.getMessage());
        }

        // Return 0 if not found
        return 0.0;
    }

    public boolean addItemToCart(int cartId, int productId, int quantity, double price) {

        // SQL 1: Select product quantity AND lock the row
        // FOR UPDATE → locks this product row so no other transaction can modify it
        // Prevents two users buying same stock at same time (race condition)
        String checkStockSql = "SELECT qty_available FROM product WHERE product_id = ? FOR UPDATE";

        // SQL 2: Insert item into cart_items table
        String insertSql = "INSERT INTO cart_items (cart_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";

        // SQL 3: Update cart summary (total price + number of items)
        String updateCartSql = "UPDATE cart SET cart_total = cart_total + ?, noof_items = noof_items + ? WHERE cart_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();

            // Disable auto-commit → start a transaction manually
            // Now all queries will execute as ONE UNIT (important for consistency)
            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkStockSql);
                    PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                    PreparedStatement updateStmt = conn.prepareStatement(updateCartSql)) {

                // Set productId in the SELECT query (replaces ?)
                checkStmt.setInt(1, productId);

                // Execute SELECT query
                ResultSet rs = checkStmt.executeQuery();

                // Check if product exists
                if (rs.next()) {

                    int existingQty = 0;
                    try (PreparedStatement checkCartStmt = conn.prepareStatement("SELECT SUM(quantity) as total_qty FROM cart_items WHERE cart_id = ? AND product_id = ?")) {
                        checkCartStmt.setInt(1, cartId);
                        checkCartStmt.setInt(2, productId);
                        try (ResultSet rsCart = checkCartStmt.executeQuery()) {
                            if (rsCart.next()) {
                                existingQty = rsCart.getInt("total_qty");
                            }
                        }
                    }

                    // Check if enough stock is available
                    if (rs.getInt("qty_available") < (quantity + existingQty)) {

                        // Not enough stock → undo everything
                        conn.rollback();
                        return false;
                    }
                } else {
                    // Product not found → rollback
                    conn.rollback();
                    return false;
                }

                // Insert item into cart_items table
                insertStmt.setInt(1, cartId); // cart_id
                insertStmt.setInt(2, productId); // product_id
                insertStmt.setInt(3, quantity); // quantity
                insertStmt.setDouble(4, price); // price per item

                insertStmt.executeUpdate(); // Executes INSERT

                // Calculate total price for this item
                double itemTotal = price * quantity;

                // Update cart table:
                // cart_total = previous total + this item total
                // noof_items = previous count + quantity added
                updateStmt.setDouble(1, itemTotal);
                updateStmt.setInt(2, quantity);
                updateStmt.setInt(3, cartId);

                updateStmt.executeUpdate(); // Executes UPDATE

                // Commit transaction → all changes are saved permanently
                conn.commit();
                return true;
            }
        } catch (SQLException e) {

            // If ANY error happens → rollback everything
            System.out.println("Error adding to cart: " + e.getMessage());

            if (conn != null) {
                try {
                    conn.rollback(); // Undo all DB changes in this transaction
                } catch (SQLException ex) {
                    System.out.println("Error during rollback: " + ex.getMessage());
                }
            }
            return false;

        } finally {
            if (conn != null) {
                try {
                    // Restore default behavior
                    conn.setAutoCommit(true);

                    // Close DB connection
                    conn.close();
                } catch (SQLException ex) {
                    System.out.println("Error closing connection: " + ex.getMessage());
                }
            }
        }
    }

    public boolean checkout(String username) {
        int cartId = getOrCreateCartId(username);
        double total = getCartTotal(cartId);

        if (total <= 0) {
            return false;
        }

        String selectItemsSql = "SELECT product_id, SUM(quantity) as quantity, MAX(price) as price FROM cart_items WHERE cart_id = ? GROUP BY product_id";
        String checkStockSql = "SELECT qty_available FROM product WHERE product_id = ? FOR UPDATE";
        String updateProductSql = "UPDATE product SET qty_available = qty_available - ? WHERE product_id = ?";
        String updateWalletSql = "UPDATE wallet SET balance = balance - ? WHERE username = ?";
        String insertOrderSql = "INSERT INTO orders (username, total_amount) VALUES (?, ?)";
        String insertOrderItemSql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        String clearCartItemsSql = "DELETE FROM cart_items WHERE cart_id = ?";
        String resetCartSql = "UPDATE cart SET cart_total = 0, noof_items = 0 WHERE cart_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement selectStmt = conn.prepareStatement(selectItemsSql);
                    PreparedStatement checkStockStmt = conn.prepareStatement(checkStockSql);
                    PreparedStatement updateProductStmt = conn.prepareStatement(updateProductSql);
                    PreparedStatement updateWalletStmt = conn.prepareStatement(updateWalletSql);
                    PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderSql,
                            Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement insertOrderItemStmt = conn.prepareStatement(insertOrderItemSql);
                    PreparedStatement clearItemsStmt = conn.prepareStatement(clearCartItemsSql);
                    PreparedStatement resetCartStmt = conn.prepareStatement(resetCartSql)) {

                // Process Items first to ensure stock is available
                selectStmt.setInt(1, cartId);
                ResultSet rs = selectStmt.executeQuery();
                while (rs.next()) {
                    int pId = rs.getInt("product_id");
                    int qty = rs.getInt("quantity");

                    checkStockStmt.setInt(1, pId);
                    ResultSet rsStock = checkStockStmt.executeQuery();
                    if (rsStock.next()) {
                        if (rsStock.getInt("qty_available") < qty) {
                            conn.rollback();
                            System.out.println("Insufficient stock during checkout for product ID: " + pId);
                            return false;
                        }
                    } else {
                        conn.rollback();
                        return false;
                    }
                }

                // Deduct Wallet
                updateWalletStmt.setDouble(1, total);
                updateWalletStmt.setString(2, username);
                updateWalletStmt.executeUpdate();

                // Create Order
                insertOrderStmt.setString(1, username);
                insertOrderStmt.setDouble(2, total);
                insertOrderStmt.executeUpdate();
                ResultSet keys = insertOrderStmt.getGeneratedKeys();
                keys.next();
                int orderId = keys.getInt(1);

                // Update Products and Create Order Items
                rs = selectStmt.executeQuery(); // Re-execute to iterate again for updates
                while (rs.next()) {
                    int pId = rs.getInt("product_id");
                    int qty = rs.getInt("quantity");
                    double price = rs.getDouble("price");

                    // Update Product Qty
                    updateProductStmt.setInt(1, qty);
                    updateProductStmt.setInt(2, pId);
                    updateProductStmt.executeUpdate();

                    // Create Order Item
                    insertOrderItemStmt.setInt(1, orderId);
                    insertOrderItemStmt.setInt(2, pId);
                    insertOrderItemStmt.setInt(3, qty);
                    insertOrderItemStmt.setDouble(4, price);
                    insertOrderItemStmt.executeUpdate();
                }

                // Clear Cart
                clearItemsStmt.setInt(1, cartId);
                clearItemsStmt.executeUpdate();
                resetCartStmt.setInt(1, cartId);
                resetCartStmt.executeUpdate();

                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error during checkout: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Error during rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    System.out.println("Error closing connection: " + ex.getMessage());
                }
            }
        }
    }
}
