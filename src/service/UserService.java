package service;

import dao.CartDAO;
import dao.OrderDAO;
import dao.ProductDAO;
import dao.UserDAO;
import model.CartItem;
import model.Order;
import model.Product;
import model.User;

import java.util.List;

public class UserService {
    private ProductDAO productDAO = new ProductDAO();
    private CartDAO cartDAO = new CartDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private UserDAO userDAO = new UserDAO();

    public List<Product> getAllProducts() {
        return productDAO.listAll();
    }

    public boolean addToCart(User user, int productId, int quantity) {
        Product product = productDAO.getProductById(productId);
        if (product == null || product.getQtyAvailable() < quantity) {
            return false;
        }
        int cartId = cartDAO.getOrCreateCartId(user.getUsername());
        return cartDAO.addItemToCart(cartId, productId, quantity, product.getPrice());
    }

    public List<CartItem> viewCart(User user) {
        int cartId = cartDAO.getOrCreateCartId(user.getUsername());
        return cartDAO.getCartItems(cartId);
    }
    
    public double getCartTotal(User user) {
        int cartId = cartDAO.getOrCreateCartId(user.getUsername());
        return cartDAO.getCartTotal(cartId);
    }

    public boolean checkout(User user) {
        int cartId = cartDAO.getOrCreateCartId(user.getUsername());
        double total = cartDAO.getCartTotal(cartId);
        double balance = userDAO.getWalletBalance(user.getUsername());
        
        if (total > 0 && balance >= total) {
            return cartDAO.checkout(user.getUsername());
        }
        return false;
    }

    public List<Order> getOrderHistory(User user) {
        return orderDAO.getOrdersByUser(user.getUsername());
    }

    public double getWalletBalance(User user) {
        return userDAO.getWalletBalance(user.getUsername());
    }

    public boolean addMoneyToWallet(User user, double amount) {
        if (amount > 0) {
            return userDAO.updateWalletBalance(user.getUsername(), amount);
        }
        return false;
    }
}
