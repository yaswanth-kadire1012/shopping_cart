package ui;

import model.CartItem;
import model.Order;
import model.Product;
import model.Role;
import model.SalesStats;
import model.User;
import service.AdminService;
import service.AuthService;
import service.UserService;

import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private AuthService authService = new AuthService();
    private UserService userService = new UserService();
    private AdminService adminService = new AdminService();
    private Scanner scanner = new Scanner(System.in);

    public void start() {
        while (true) {
            System.out.println("\n=== Welcome to Shopping System ===");
            System.out.println("1. Login");
            System.out.println("2. Register (Customer)");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            
            int choice = getIntInput();
            switch (choice) {
                case 1:
                    handleLogin();
                    break;
                case 2:
                    handleRegister();
                    break;
                case 3:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (authService.login(username, password)) {
            User user = authService.getLoggedInUser();
            System.out.println("Login successful! Welcome, " + user.getName() + ".");
            if (user.getRole() == Role.ADMIN) {
                adminMenu();
            } else {
                customerMenu();
            }
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    private void handleRegister() {
        System.out.print("Choose Username: ");
        String username = scanner.nextLine();
        System.out.print("Choose Password: ");
        String password = scanner.nextLine();
        System.out.print("Your Name: ");
        String name = scanner.nextLine();

        if (authService.register(username, password, name)) {
            System.out.println("Registration successful. You can now log in.");
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    private void adminMenu() {
        while (authService.getLoggedInUser() != null) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add Product");
            System.out.println("2. Update Product Quantity");
            System.out.println("3. View Sales Analytics");
            System.out.println("4. Add New Admin");
            System.out.println("5. View All Products");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");

            int choice = getIntInput();
            switch (choice) {
                case 1:
                    System.out.print("Product Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Quantity: ");
                    int qty = getIntInput();
                    System.out.print("Price: ");
                    double price = getDoubleInput();
                    if (adminService.addProduct(name, qty, price)) {
                        System.out.println("Product added successfully.");
                    } else {
                        System.out.println("Failed to add product.");
                    }
                    break;
                case 2:
                    System.out.print("Product ID: ");
                    int id = getIntInput();
                    System.out.print("Quantity to Add (can be negative): ");
                    int qAdd = getIntInput();
                    if (adminService.updateProductQuantity(id, qAdd)) {
                        System.out.println("Quantity updated.");
                    } else {
                        System.out.println("Failed to update quantity.");
                    }
                    break;
                case 3:
                    SalesStats stats = adminService.viewAnalytics();
                    System.out.println("\n--- Sales Analytics ---");
                    System.out.println("Total Orders: " + stats.getTotalOrders());
                    System.out.println("Total Revenue: $" + stats.getTotalRevenue());
                    System.out.println("Total Items Sold: " + stats.getItemsSold());
                    break;
                case 4:
                    System.out.print("New Admin Username: ");
                    String adminUser = scanner.nextLine();
                    System.out.print("New Admin Password: ");
                    String adminPass = scanner.nextLine();
                    System.out.print("New Admin Name: ");
                    String adminName = scanner.nextLine();
                    if (adminService.addAdmin(adminUser, adminPass, adminName)) {
                        System.out.println("Admin added successfully.");
                    } else {
                        System.out.println("Failed to add admin.");
                    }
                    break;
                case 5:
                    List<Product> adminProducts = adminService.getAllProducts();
                    System.out.println("\n--- All Products ---");
                    for (Product p : adminProducts) {
                        System.out.println(p.getProductId() + " | " + p.getProductName() + 
                                " | Qty: " + p.getQtyAvailable() + " | Price: $" + p.getPrice());
                    }
                    break;
                case 6:
                    authService.logout();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void customerMenu() {
        User user = authService.getLoggedInUser();
        while (authService.getLoggedInUser() != null) {
            System.out.println("\n--- Customer Menu ---");
            System.out.println("1. View Products");
            System.out.println("2. Add to Cart");
            System.out.println("3. View Cart");
            System.out.println("4. Checkout");
            System.out.println("5. View Orders");
            System.out.println("6. Manage Wallet");
            System.out.println("7. Logout");
            System.out.print("Choose an option: ");

            int choice = getIntInput();
            switch (choice) {
                case 1:
                    List<Product> products = userService.getAllProducts();
                    System.out.println("\n--- Products ---");
                    for (Product p : products) {
                        System.out.println(p.getProductId() + " | " + p.getProductName() + 
                                " | Qty: " + p.getQtyAvailable() + " | Price: $" + p.getPrice());
                    }
                    break;
                case 2:
                    System.out.print("Product ID: ");
                    int pId = getIntInput();
                    System.out.print("Quantity: ");
                    int qty = getIntInput();
                    if (userService.addToCart(user, pId, qty)) {
                        System.out.println("Added to cart.");
                    } else {
                        System.out.println("insufficient stock");
                    }
                    break;
                case 3:
                    List<CartItem> items = userService.viewCart(user);
                    System.out.println("\n--- Your Cart ---");
                    if (items.isEmpty()) {
                        System.out.println("Cart is empty.");
                    } else {
                        for (CartItem item : items) {
                            System.out.println("Product ID: " + item.getProductId() + 
                                    " | Qty: " + item.getQuantity() + " | Price: $" + item.getPrice());
                        }
                        System.out.println("Total: $" + userService.getCartTotal(user));
                    }
                    break;
                case 4:
                    if (userService.checkout(user)) {
                        System.out.println("Checkout successful!");
                    } else {
                        System.out.println("Checkout failed. Check your wallet balance or cart.");
                    }
                    break;
                case 5:
                    List<Order> orders = userService.getOrderHistory(user);
                    System.out.println("\n--- Your Orders ---");
                    for (Order o : orders) {
                        System.out.println("Order #" + o.getOrderId() + " | Total: $" + 
                                o.getTotalAmount() + " | Date: " + o.getOrderDate());
                    }
                    break;
                case 6:
                    System.out.println("Current Balance: $" + userService.getWalletBalance(user));
                    System.out.print("Amount to add (or 0 to cancel): $");
                    double amount = getDoubleInput();
                    if (amount > 0) {
                        if (userService.addMoneyToWallet(user, amount)) {
                            System.out.println("Balance updated.");
                        } else {
                            System.out.println("Failed to update balance.");
                        }
                    }
                    break;
                case 7:
                    authService.logout();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private double getDoubleInput() {
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }
}
