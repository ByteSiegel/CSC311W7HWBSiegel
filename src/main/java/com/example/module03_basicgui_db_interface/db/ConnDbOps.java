package com.example.module03_basicgui_db_interface.db;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Handles database operations such as connecting to the database,
 * and performing CRUD operations on user data.
 */
public class ConnDbOps {
    public final String MYSQL_SERVER_URL = "jdbc:mysql://siegelcsc311server.mysql.database.azure.com";
    public final String DB_URL = "jdbc:mysql://siegelcsc311server.mysql.database.azure.com/DBname";
    public final String USERNAME = "siegbn6";
    public final String PASSWORD = "tafze9-Febmid-dekfij";

    /**
     * Connects to the database and sets up the required tables.
     */
    public void connectToDatabase() {
        boolean hasRegisteredUsers = false;

        try {
            // Connect to MySQL server and create the database if it doesn't exist
            Connection conn = DriverManager.getConnection(MYSQL_SERVER_URL, USERNAME, PASSWORD);
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS DBname");
            statement.close();
            conn.close();

            // Connect to the database and create the users table if it doesn't exist
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            statement = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INT(10) NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                    + "name VARCHAR(200) NOT NULL,"
                    + "email VARCHAR(200) NOT NULL UNIQUE,"
                    + "phone VARCHAR(200),"
                    + "address VARCHAR(200),"
                    + "password VARCHAR(200) NOT NULL,"
                    + "profile_picture VARCHAR(500)"
                    + ")";
            statement.executeUpdate(sql);

            // Check if there are users in the users table
            statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");

            if (resultSet.next()) {
                int numUsers = resultSet.getInt(1);
                if (numUsers > 0) {
                    hasRegisteredUsers = true;
                }
            }

            statement.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Hashes the password using SHA-256 algorithm.
     *
     * @param password The plain-text password to hash.
     * @return The hashed password as a Base64 encoded string.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Inserts a new user into the database.
     *
     * @param name               User's name.
     * @param email              User's email.
     * @param phone              User's phone number.
     * @param address            User's address.
     * @param password           User's password.
     * @param profilePicturePath Path to the user's profile picture.
     */
    public void insertUser(String name, String email, String phone, String address, String password, String profilePicturePath) {
        String sql = "INSERT INTO users (name, email, phone, address, password, profile_picture) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            String hashedPassword = hashPassword(password);

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, phone);
            preparedStatement.setString(4, address);
            preparedStatement.setString(5, hashedPassword);
            preparedStatement.setString(6, profilePicturePath);

            int row = preparedStatement.executeUpdate();

            if (row > 0) {
                System.out.println("A new user was inserted successfully.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing user's information in the database.
     *
     * @param id                 User's ID.
     * @param name               Updated name.
     * @param email              Updated email.
     * @param phone              Updated phone number.
     * @param address            Updated address.
     * @param password           Updated password.
     * @param profilePicturePath Updated profile picture path.
     */
    public void updateUser(int id, String name, String email, String phone, String address, String password, String profilePicturePath) {
        String sql = "UPDATE users SET name = ?, email = ?, phone = ?, address = ?, password = ?, profile_picture = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String hashedPassword = hashPassword(password);

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, address);
            pstmt.setString(5, hashedPassword);
            pstmt.setString(6, profilePicturePath);
            pstmt.setInt(7, id);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("User updated successfully.");
            } else {
                System.out.println("No user found with the provided ID.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a user from the database based on ID.
     *
     * @param id The ID of the user to delete.
     */
    public void deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("No user found with the provided ID.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Additional methods if needed
}
