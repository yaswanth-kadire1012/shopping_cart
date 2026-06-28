CREATE DATABASE IF NOT EXISTS shopping_cart;
USE shopping_cart;

CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER'
);

CREATE TABLE IF NOT EXISTS product (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    qty_available INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS wallet (
    username VARCHAR(50) PRIMARY KEY,
    balance DECIMAL(10, 2) NOT NULL DEFAULT 0.0
);

CREATE TABLE IF NOT EXISTS cart (
    cart_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    cart_total DECIMAL(10, 2) NOT NULL DEFAULT 0.0,
    noof_items INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cart_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

-- Add Foreign Key Constraints
ALTER TABLE wallet ADD CONSTRAINT fk_wallet_username FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE;
ALTER TABLE cart ADD CONSTRAINT fk_cart_username FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE;
ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_cart_id FOREIGN KEY (cart_id) REFERENCES cart(cart_id) ON DELETE CASCADE;
ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_product_id FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE;
ALTER TABLE orders ADD CONSTRAINT fk_orders_username FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE;
ALTER TABLE order_items ADD CONSTRAINT fk_order_items_order_id FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE;
ALTER TABLE order_items ADD CONSTRAINT fk_order_items_product_id FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE;

-- Insert a default admin so we can login initially
INSERT IGNORE INTO users (username, password, name, role) VALUES ('admin', 'admin123', 'Administrator', 'ADMIN');
