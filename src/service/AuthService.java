package service;

import dao.UserDAO;
import model.User;
import model.Role;

/**
 * AuthService handles authentication-related operations such as:
 * - User login
 * - User registration
 * - Logout
 * logged in user maintains the currently logged-in user
 * It interacts with UserDAO to fetch/store user data
 * and maintains the currently logged-in user session.
 */
public class AuthService {
    // DAO layer object to interact with database for user operations
    private UserDAO userDAO = new UserDAO();
    // Stores the currently logged-in user
    private User loggedInUser = null;

    public boolean login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            loggedInUser = user;
            return true;
        }
        return false;
    }

    /**
     * Registers a new customer.
     * Creates a user record and initializes an empty wallet.
     * 
     * @param username The desired username
     * @param password The desired password
     * @param name     The full name of the user
     * @return true if registration successful, false if username already exists
     */
    public boolean register(String username, String password, String name) {
        if (userDAO.findByUsername(username) != null) {
            return false; // Username already exists
        }
        User newUser = new User(username, password, name, Role.CUSTOMER);
        if (userDAO.saveUser(newUser)) {
            userDAO.createWallet(username);
            return true;
        }
        return false;
    }

    /**
     * Logs out the current user.
     * Clears the logged-in user session.
     */
    public void logout() {
        loggedInUser = null;
    }

    /**
     * Gets the currently logged-in user.
     * 
     * @return The User object of the currently logged-in user, or null if no user
     *         is logged in
     */
    public User getLoggedInUser() {
        return loggedInUser;
    }
}
