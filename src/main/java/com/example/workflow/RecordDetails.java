package com.example.workflow;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.sql.*;

public class RecordDetails implements JavaDelegate{

    private static final String DB_CONNECTION = "jdbc:h2:file:./camunda-h2-database";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";


    @Override
    public void execute(DelegateExecution execution) throws Exception{
        //Sent data to db

        try {
            boolean alreadyVip = (boolean) execution.getVariable("alreadyVip");
            if (!alreadyVip) {
                String firstName = execution.getVariable("firstName").toString();
                String lastName = execution.getVariable("lastName").toString();
                String address = execution.getVariable("address").toString();
                String postCode = execution.getVariable("postcode").toString();
                String phoneNumber = execution.getVariable("phoneNumber").toString();
                String emailAddress = execution.getVariable("emailAddress").toString();
                boolean vipYes = (boolean) execution.getVariable("vipYes");
                //if vip yes is true, make timestamp current timestamp, else set to 1
                String lastVip = "1";
                if(vipYes){
                    lastVip = String.valueOf(System.currentTimeMillis() / 1000L);
                }
                //connect to database and insert new customer
                String customerId = pushToDb(firstName, lastName, address, postCode, phoneNumber, emailAddress, lastVip, vipYes);
                System.out.println("Customer ID: " + customerId + " added to database");
                execution.setVariable("customerID", customerId);
            }
            }catch(Exception e){
                System.out.println("Exception: " + e.getMessage());
                throw new BpmnError("Error in RecordDetails", e.getMessage());
            }

    }

    private String pushToDb(String firstName, String lastName, String address, String postCode, String phoneNumber, String emailAddress, String lastVip, boolean vipYes) throws SQLException, ClassNotFoundException {
        // Declare variables for JDBC objects
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String customerId = "Error";

        try {
            // Load the H2 driver class
            Class.forName("org.h2.Driver");

            // Establish database connection
            conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);

            // Define SQL query with parameters for new customer data
            String sql = "INSERT INTO vipCustomers (firstName, lastName, address, postCode, phoneNumber, emailAddress, lastVip, isVip, paymentDetails) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // Create prepared statement with SQL query and flag for auto-generated keys
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Set values for parameters in prepared statement
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, address);
            stmt.setString(4, postCode);
            stmt.setString(5, phoneNumber);
            stmt.setString(6, emailAddress);
            //add Unix time code
            stmt.setString(7, lastVip);
            stmt.setBoolean(8, vipYes);
            stmt.setString(9, "1234");

            // Execute SQL statement to insert new record
            int rowsAffected = stmt.executeUpdate();

            // Check number of rows affected by SQL statement
            if (rowsAffected > 0) {
                // Retrieve auto-generated keys
                rs = stmt.getGeneratedKeys();

                if (rs.next()) {
                    // Get the value of the auto-generated key (customerId)
                    customerId = rs.getString(1);
                    System.out.println("New record inserted successfully!");
                    System.out.println("Customer details: " + "\n First Name: " + firstName + "\n Last Name: " + lastName
                            + "\n Address: " + address + " " + postCode + "\n Phone Number: " + phoneNumber + "\n Email Address: " + emailAddress + "\n Last VIP timestamp: " + lastVip + "/n VIP Status: VIP");
                    System.out.println("Customer ID: " + customerId);
                }
            } else {
                System.out.println("Failed to insert new record.");
            }

        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        } finally {
            // Close JDBC objects in reverse order of creation
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return customerId;
    }

}


