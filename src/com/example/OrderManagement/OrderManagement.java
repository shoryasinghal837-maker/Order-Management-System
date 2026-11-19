package com.example.OrderManagement;

import java.sql.*;
import java.util.Scanner;
public class OrderManagement {

    // MySQL connection details
    static final String URL =
            "jdbc:mysql://localhost:3306/ecommerce_db";

    static final String USER = "root";
    static final String PASSWORD = "XYZ";

    static Scanner sc = new Scanner(System.in);

    // Establish database connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Display order details
    public static void displayOrder(ResultSet rs) throws SQLException {

        System.out.println("--------------------------------");
        System.out.println("Order ID: " + rs.getInt("order_id"));
        System.out.println("Customer: " + rs.getString("customer_name"));
        System.out.println("Product: " + rs.getString("product_name"));
        System.out.println("Quantity: " + rs.getInt("quantity"));
        System.out.println("Unit Price: " + rs.getDouble("unit_price"));
        System.out.println("Status: " + rs.getString("status"));
        System.out.println("--------------------------------");
    }

    // Q1(a): Insert at least three orders
    public static void insertOrders() {

        String sql = "INSERT INTO Orders "
                + "(order_id, customer_name, product_name, quantity, unit_price, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        Object[][] orders = {
            {101, "Rahul", "Laptop", 2, 55000.0, "Pending"},
            {102, "Aman", "Mouse", 5, 500.0, "Shipped"},
            {103, "Priya", "Keyboard", 3, 1200.0, "Cancelled"}
        };

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (Object[] order : orders) {

                ps.setInt(1, (Integer) order[0]);
                ps.setString(2, (String) order[1]);
                ps.setString(3, (String) order[2]);
                ps.setInt(4, (Integer) order[3]);
                ps.setDouble(5, (Double) order[4]);
                ps.setString(6, (String) order[5]);

                try {
                    ps.executeUpdate();
                    System.out.println("Order inserted: " + order[0]);

                } catch (SQLException e) {

                    if (e.getErrorCode() == 1062) {
                        System.out.println(
                            "Duplicate order ID: " + order[0]);
                    } else {
                        System.out.println(e.getMessage());
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }

    // Q1(b): Search orders by status and quantity > 2
    // Sort unit price in descending order
    public static void searchOrders() {

        System.out.print("Enter order status: ");
        String status = sc.nextLine();

        String sql = "SELECT * FROM Orders "
                + "WHERE status = ? AND quantity > 2 "
                + "ORDER BY unit_price DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                while (rs.next()) {
                    displayOrder(rs);
                    found = true;
                }

                if (!found) {
                    System.out.println("No matching orders found.");
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Q1(c): Increase quantity of an existing order
    public static void increaseQuantity() {

        System.out.print("Enter order ID: ");
        int id = Integer.parseInt(sc.nextLine());

        System.out.print("Enter additional quantity: ");
        int amount = Integer.parseInt(sc.nextLine());

        if (amount <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return;
        }

        String sql = "UPDATE Orders "
                + "SET quantity = quantity + ? "
                + "WHERE order_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, amount);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Quantity updated successfully.");
            } else {
                System.out.println("Order does not exist.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Q1(d): Update status if order exists and status is valid
    public static void updateStatus() {

        System.out.print("Enter order ID: ");
        int id = Integer.parseInt(sc.nextLine());

        System.out.print("Enter new status: ");
        String status = sc.nextLine();

        if (!status.equalsIgnoreCase("Pending")
                && !status.equalsIgnoreCase("Shipped")
                && !status.equalsIgnoreCase("Delivered")) {

            System.out.println("Invalid status.");
            System.out.println("Valid: Pending, Shipped, Delivered");
            return;
        }

        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                System.out.println("Status updated successfully.");

                String selectSql =
                        "SELECT * FROM Orders WHERE order_id = ?";

                try (PreparedStatement selectPs =
                             con.prepareStatement(selectSql)) {

                    selectPs.setInt(1, id);

                    try (ResultSet rs = selectPs.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("Updated order details:");
                            displayOrder(rs);
                        }
                    }
                }

            } else {
                System.out.println("Order does not exist.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Q1(e): Calculate total order value
    public static void displayTotalValue() {

        String sql = "SELECT SUM(quantity * unit_price) AS total "
                + "FROM Orders";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.println(
                    "Total order value: " + rs.getDouble("total"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Q1(e): Delete order only if status is Cancelled
    public static void deleteCancelledOrder() {

        System.out.print("Enter order ID to delete: ");
        int id = Integer.parseInt(sc.nextLine());

        String sql = "DELETE FROM Orders "
                + "WHERE order_id = ? AND status = 'Cancelled'";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Cancelled order deleted successfully.");
            } else {
                System.out.println(
                    "Order not found or its status is not Cancelled.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Main menu
    public static void main(String[] args) {

        while (true) {

            System.out.println("\n===== ORDER MANAGEMENT SYSTEM =====");
            System.out.println("1. Insert three orders");
            System.out.println("2. Search orders by status");
            System.out.println("3. Increase order quantity");
            System.out.println("4. Update order status");
            System.out.println("5. Display total order value");
            System.out.println("6. Delete cancelled order");
            System.out.println("7. Exit");

            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(sc.nextLine());

                switch (choice) {

                    case 1:
                        insertOrders();
                        break;

                    case 2:
                        searchOrders();
                        break;

                    case 3:
                        increaseQuantity();
                        break;

                    case 4:
                        updateStatus();
                        break;

                    case 5:
                        displayTotalValue();
                        break;

                    case 6:
                        deleteCancelledOrder();
                        break;

                    case 7:
                        System.out.println("Application closed.");
                        sc.close();
                        return;

                    default:
                        System.out.println("Invalid choice.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
