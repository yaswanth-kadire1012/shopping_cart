package service;

import dao.AnalyticsDAO;
import dao.ProductDAO;
import dao.UserDAO;
import model.Product;
import model.Role;
import model.SalesStats;
import model.User;
import java.util.List;

public class AdminService {
    private ProductDAO productDAO = new ProductDAO();
    private AnalyticsDAO analyticsDAO = new AnalyticsDAO();
    private UserDAO userDAO = new UserDAO();

    public List<Product> getAllProducts() {
        return productDAO.listAll();
    }

    public boolean addProduct(String name, int qty, double price) {
        if (qty < 0 || price < 0) return false;
        Product p = new Product(0, name, qty, price);
        return productDAO.addProduct(p);
    }

    public boolean updateProductQuantity(int productId, int qtyToAdd) {
        return productDAO.updateQuantity(productId, qtyToAdd);
    }

    public SalesStats viewAnalytics() {
        return analyticsDAO.getOverallSalesStats();
    }
    
    public boolean addAdmin(String username, String password, String name) {
        if (userDAO.findByUsername(username) != null) {
            return false;
        }
        User admin = new User(username, password, name, Role.ADMIN);
        return userDAO.saveUser(admin);
    }
}
