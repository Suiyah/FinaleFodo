package org.example;


import java.util.Scanner;

class User {
    String username;
    String password;
    String role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}

class Order {
    String customerName;
    String[] foodItems;
    int[] quantities;
    int totalCost;
    String address;

    public Order(String customerName, String[] foodItems, int[] quantities, int totalCost, String address) {
        this.customerName = customerName;
        this.foodItems = foodItems;
        this.quantities = quantities;
        this.totalCost = totalCost;
        this.address = address;
    }

    public String toString() {
        StringBuilder orderDetails = new StringBuilder();
        orderDetails.append("Customer Name: ").append(customerName).append("\n");
        for (int i = 0; i < foodItems.length; i++) {
            orderDetails.append(foodItems[i]).append(": ").append(quantities[i]).append("\n");
        }
        orderDetails.append("Total Cost: ").append(totalCost).append("\n");
        orderDetails.append("Address: ").append(address).append("\n");
        return orderDetails.toString();
    }
}

