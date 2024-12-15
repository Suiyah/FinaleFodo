package org.example;

import java.sql.*;
import java.util.Scanner;

public class Food {
    static Scanner scanner = new Scanner(System.in);
    static String dbUrl = "jdbc:postgresql://localhost:5433/fodo";
    static String dbUser = "postgres";
    static String dbPassword = "6789";

    public static void main(String[] args) {
        while (true) {
            System.out.println("Enter Username: ");
            String username = scanner.next();
            System.out.println("Enter Password: ");
            String password = scanner.next();

            User user = authenticate(username, password);
            if (user != null) {
                switch (user.getRole()) {
                    case "customer":
                        handleCustomer(user);
                        break;
                    case "courier":
                        handleCourier();
                        break;
                    case "admin":
                        handleAdmin();
                        break;
                }
            } else {
                System.out.println("Invalid login, please try again.");
            }
        }
    }

    public static User authenticate(String username, String password) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "SELECT username, password, role FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("username"), rs.getString("password"), rs.getString("role"));
            }
        } catch (SQLException e) {
            System.out.println("Error during authentication: " + e.getMessage());
        }
        return null;
    }

    public static void handleCustomer(User user) {
        String customerName = user.getUsername();
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String[] orderedItems = new String[100];
            int[] quantities = new int[100];
            int totalCost = 0;
            int itemCount = 0;

            while (true) {
                System.out.println("Menu:");
                String query = "SELECT food_id, food_name, price, stock FROM fooditems";
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                    int foodId = rs.getInt("food_id");
                    String foodName = rs.getString("food_name");
                    int price = rs.getInt("price");
                    int stock = rs.getInt("stock");

                    if (stock > 0) {
                        System.out.println(foodId + ". " + foodName + " = $" + price + " (" + stock + " available)");
                    }
                }

                System.out.println("Enter the number of the food item you want to order:");
                int choice = scanner.nextInt();

                System.out.println("How many?");
                int quantity = scanner.nextInt();

                String selectQuery = "SELECT price, stock FROM fooditems WHERE food_id = ?";
                PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
                selectStmt.setInt(1, choice);
                ResultSet selectedItem = selectStmt.executeQuery();

                if (selectedItem.next()) {
                    int price = selectedItem.getInt("price");
                    int stock = selectedItem.getInt("stock");

                    if (quantity > stock) {
                        System.out.println("Oh sorry, we don't have that much.");
                        continue;
                    }

                    String updateQuery = "UPDATE fooditems SET stock = stock - ? WHERE food_id = ?";
                    PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                    updateStmt.setInt(1, quantity);
                    updateStmt.setInt(2, choice);
                    updateStmt.executeUpdate();

                    orderedItems[itemCount] = "Food ID: " + choice;
                    quantities[itemCount] = quantity;
                    totalCost += quantity * price;
                    itemCount++;
                }

                System.out.println("Anything else? (Y/N)");
                String more = scanner.next();
                if (more.equalsIgnoreCase("N")) {
                    break;
                }
            }

            System.out.println("Total Cost: $" + totalCost);
            System.out.println("Enter your address:");
            scanner.nextLine(); // consume the leftover newline
            String address = scanner.nextLine();

            String insertOrderQuery = "INSERT INTO orders (user_id, total_price, delivery_address) VALUES ((SELECT user_id FROM users WHERE username = ?), ?, ?)";
            PreparedStatement insertOrderStmt = connection.prepareStatement(insertOrderQuery);
            insertOrderStmt.setString(1, customerName);
            insertOrderStmt.setInt(2, totalCost);
            insertOrderStmt.setString(3, address);
            insertOrderStmt.executeUpdate();

            System.out.println("Thank you for ordering! Your food will arrive soon.");

        } catch (SQLException e) {
            System.out.println("Error while processing the order: " + e.getMessage());
        }
    }


    public static void handleCourier() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "SELECT order_id, delivery_address, total_price FROM orders";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("Orders:");
            while (rs.next()) {
                System.out.println("Order ID: " + rs.getInt("order_id") + ", Address: " + rs.getString("delivery_address") + ", Total: $" + rs.getInt("total_price"));
            }
        } catch (SQLException e) {
            System.out.println("Error while retrieving orders: " + e.getMessage());
        }
    }


    public static void handleAdmin() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            while (true) {
                System.out.println("Admin Menu:");
                System.out.println("1. Show information of orders placed by the customers");
                System.out.println("2. Show and manage food stock");
                System.out.println("3. Exit to login page");

                int choice = scanner.nextInt();
                if (choice == 1) {
                    String query = "SELECT * FROM orders";
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    System.out.println("Orders:");
                    while (rs.next()) {
                        System.out.println("Order ID: " + rs.getInt("order_id") + ", User ID: " + rs.getInt("user_id") + ", Total Price: $" + rs.getInt("total_price"));;
                    }
                } else if (choice == 2) {
                    while (true) {
                        System.out.println("Food Management:");

                        String foodQuery = "SELECT food_id, food_name, stock FROM fooditems";
                        Statement stmt = connection.createStatement();
                        ResultSet rs = stmt.executeQuery(foodQuery);

                        while (rs.next()) {
                            int foodId = rs.getInt("food_id");
                            String foodName = rs.getString("food_name");
                            int stock = rs.getInt("stock");
                            System.out.println(foodId + ". " + foodName + " = " + stock);
                        }

                        System.out.println("Enter the ID of the food item to increase stock followed by the quantity (e.g., '1 5' to add 5 items):");
                        int foodId = scanner.nextInt();
                        int quantity = scanner.nextInt();

                        String updateQuery = "UPDATE fooditems SET stock = stock + ? WHERE food_id = ?";
                        PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                        updateStmt.setInt(1, quantity);
                        updateStmt.setInt(2, foodId);

                        int rowsUpdated = updateStmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            System.out.println("Stock updated successfully!");
                        } else {
                            System.out.println("Invalid food ID.");
                        }

                        System.out.println("Enter Y to leave to Admin page");
                        String leave = scanner.next();
                        if (leave.equalsIgnoreCase("Y")) {
                            break;
                        }

                    }
                } else if (choice == 3) {
                    break;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
