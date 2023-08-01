package com.example.workflow;

import java.sql.*;

import javax.inject.Named;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Named
public class Validation implements JavaDelegate {

    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:file:./camunda-h2-database";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        String customerID = (String) execution.getVariable("customerID");
        System.out.println("customerID: " + customerID);




        try {
            if (customerID == null || customerID.isEmpty()) {
                System.out.println("name is empty");
                throw new BpmnError("form_error", "please enter a customer number");
            }
            // Retrieve customer number from the ORDERS table in the H2 database

            long lastVip = getVipAgeFromDatabase(customerID);
            if(lastVip == 0){
                throw new BpmnError("form_error", "customer number not found");
            }

            // set current time
            long currentTime = System.currentTimeMillis() / 1000L; // Divide by 1000 to convert milliseconds to seconds
            // Convert timestamps to milliseconds
            long lastVipMillis = (long) lastVip * 1000;
            long currentMillis = currentTime * 1000;
            // Calculate the difference in milliseconds
            long timeDifference = currentMillis - lastVipMillis;
            // Convert the difference to days
            long daysDifference = timeDifference / (24L * 60 * 60 * 1000);

            // Check if the difference is greater than 30 days
            boolean isMoreThan30DaysAgo;
            if(daysDifference > 30){
                isMoreThan30DaysAgo = true;
            }else{
                isMoreThan30DaysAgo = false;
            }

            if (isMoreThan30DaysAgo) {
                execution.setVariable("isValid", false);
            } else {
                execution.setVariable("isValid", true);
            }

        } catch (Exception e) {
            throw new BpmnError("form_error", e.getMessage());
        }



    }


    private long getVipAgeFromDatabase(String customerID) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        int lastVip = 0;

        try {
            Class.forName(DB_DRIVER);
            connection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);

            String query = "SELECT lastVip FROM vipCustomers WHERE customerID = ?";
            // Create prepared statement with SQL query
            statement = connection.prepareStatement(query);
            // Set values for parameters in prepared statement
            statement.setString(1, customerID);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                lastVip = resultSet.getInt("lastVip");
            }

        } finally {
            // Close resources in the reverse order of their creation
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }

        return lastVip;
    }

    public void checkPaymentDetails() throws BpmnError {
        System.out.println("checkPaymentDetails method is active");
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            Class.forName(DB_DRIVER);
            connection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);

            String query = "SELECT PAYMENT_DETAILS FROM orders WHERE CUSTOMERID = ?";


            statement = connection.prepareStatement(query);
            statement.setString(1, "123456"); // replace with your query to get the correct customer number

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                if (resultSet.getObject("PAYMENT_DETAILS") == null) {
                    throw new BpmnError("paymentRefundError", "Payment details field is null");
                }
            } else {
                throw new BpmnError("ORDER_NOT_FOUND", "Order not found for the given customer number");
            }
        } catch (ClassNotFoundException | SQLException e) {
            // Handle the exception here
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // Handle the exception here
            }
        }
    }







}
