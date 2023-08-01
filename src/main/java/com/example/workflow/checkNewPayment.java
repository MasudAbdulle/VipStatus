package com.example.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class checkNewPayment implements JavaDelegate {
    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:file:./camunda-h2-database";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        // Retrieve the customerID and AccountHolder variables from process variables
        String customerId = (String) delegateExecution.getVariable("customerID");
        String accountHolder = (String) delegateExecution.getVariable("paymentDetails");

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            Class.forName(DB_DRIVER);
            connection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);

            // Prepare the SQL statement to update the PaymentDetails field
            String updateSql = "UPDATE VIPCUSTOMERS SET PaymentDetails = ? WHERE customerId = ?";

            statement = connection.prepareStatement(updateSql);
            statement.setString(1, accountHolder);
            statement.setString(2, customerId);

            // Execute the SQL statement
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Payment details updated successfully for customer: " + customerId);
            } else {
                System.out.println("No customer found with ID: " + customerId);
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Failed to update payment details: " + e.getMessage());
        } finally {
            // Close the statement and database connection
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    System.out.println("Failed to close statement: " + e.getMessage());
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.out.println("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }


    }

