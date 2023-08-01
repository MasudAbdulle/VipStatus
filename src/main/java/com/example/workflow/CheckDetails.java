package com.example.workflow;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class CheckDetails implements JavaDelegate{

    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:file:./camunda-h2-database";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";

    @Override
    public void execute(DelegateExecution execution) throws Exception{
        try{
            //set execution variable alreadyVip to false
            execution.setVariable("alreadyVip", false);

            //get variables to search db
            String emailAddress = execution.getVariable("emailAddress").toString();
            String lastName = execution.getVariable("lastName").toString();
            String postcode = execution.getVariable("postCode").toString();

            //check whether customer exists and if it has been over 6 months since they were last a VIP member
            String matchingCustomer = checkDetails(emailAddress, lastName, postcode);

            //if matchingCustomer is NoMatch, set membershipValid to true
            if(matchingCustomer == "NoMatch"){
                execution.setVariable("membershipValid", true);
            // if there is a matching customer, but their account is over 6 months old
            }else if(matchingCustomer.contains("VALID")){
                //create String by splitting matchingCustomer with delimited | and get second element
                String[] customerDetails = matchingCustomer.split("\\|");
                //get customer number
                matchingCustomer = customerDetails[1];
                //get vip status
                boolean vipStatus = getVipStatus(matchingCustomer);
                String items = execution.getVariable("itemCodes").toString();
                if (items.contains("1111") && vipStatus == false){
                    // membership is valid, and customer is not already a VIP member
                    execution.setVariable("membershipValid", true);
                    System.out.println("Customer is not a VIP member. They wish to become a VIP member.");
                    //set alreadyVip to false
                    execution.setVariable("alreadyVip", true);
                    updateVipStatus(matchingCustomer);
                    updateLastVip(matchingCustomer);
                    execution.setVariable("customerID", matchingCustomer);
                }
            }else{
                //get items from execution
                String items = execution.getVariable("itemCodes").toString();
                //if customer wishes to become a vip member but is already a vip member, remove item "1111," from items
                boolean vipStatus = getVipStatus(matchingCustomer);
                if(items.contains("1111") && (vipStatus == true)){
                    // membership is valid, but customer is already a VIP member
                    execution.setVariable("membershipValid", true);
                    System.out.println("Customer is already a VIP member. Removing VIP item code from order.");
                    //remove item "1111," from items
                    items = items.replace("1111,", "");
                    //set items to new items
                    execution.setVariable("itemCodes", items);
                    //set alreadyVip to true
                    execution.setVariable("alreadyVip", true);
                // if customer is not a vip member and has been a member in the last 6 months set membershipValid to false
                }else{
                    execution.setVariable("membershipValid", false);
                    System.out.println("Customer has been a VIP member in the last 6 months. \nMembership is not valid. \n Customer ID: " + matchingCustomer + "\n Order Cancelled.");
                }
                execution.setVariable("customerID", matchingCustomer);

            }
        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
            throw new BpmnError("Error in CheckDetails", e.getMessage());
        }
    }

    public String checkDetails(String emailAddress, String lastName, String postcode) throws SQLException, ClassNotFoundException{

        // Load the H2 driver class
        Class.forName("org.h2.Driver");

        // Define the SQL query to select the relevant records from the database
        String sql = "SELECT * FROM vipCustomers WHERE emailAddress = ? OR (lastName = ? AND postCode = ?)";

        try (Connection conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the query parameters
            pstmt.setString(1, emailAddress);
            pstmt.setString(2, lastName);
            pstmt.setString(3, postcode);

            // Execute the query and get the result set
            ResultSet rs = pstmt.executeQuery();

            // Loop through the result set and check the lastVip column
            while (rs.next()) {
                long lastVip = rs.getLong("lastVip");
                boolean isVip = rs.getBoolean("isVip");
                long now = System.currentTimeMillis();
                long sixMonthsAgo = now - (6 * 30 * 24 * 60 * 60 * 1000L); // 6 months in milliseconds

                if (lastVip > sixMonthsAgo || isVip) {
                    // The lastVip date is less than 6 months ago
                    String customerID = rs.getString("customerId");
                    return customerID;
                } else if(lastVip < sixMonthsAgo){
                    // last vip date is more than 6 months ago
                    String customerID = rs.getString("customerId");
                    System.out.println("Triggered lastVip > sixMonthsAgo");
                    return "VALID|" + customerID;
                }else {
                    // The lastVip date is less than 6 months ago
                    return "NoMatch";
                }
            }

            // No matching records found
            return "NoMatch";

        } catch (SQLException e) {
            // Handle any errors that occur during database connection or query execution
            System.out.println(e.getMessage());
            return "NoMatch";
        }
    }

    public boolean getVipStatus(String customerID) throws ClassNotFoundException {
        //search h2 database table vipCustomers for customerID and return value of isVip
        // Load the H2 driver class
        Class.forName("org.h2.Driver");

        // Define the SQL query to select the relevant records from the database
        String sql = "SELECT * FROM vipCustomers WHERE customerId = ?";

        try (Connection conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the query parameters
            pstmt.setString(1, customerID);

            // Execute the query and get the result set
            ResultSet rs = pstmt.executeQuery();

            // Loop through the result set and check the lastVip column
            while (rs.next()) {
                boolean isVip = rs.getBoolean("isVip");
                return isVip;
            }
            System.out.println("No matching customer found 1");
        } catch (SQLException e) {
            // Handle any errors that occur during database connection or query execution
            System.out.println(e.getMessage());
        }
        return false;
    }

    public void updateVipStatus(String customerID) throws SQLException {
        // Declare variables for JDBC objects
        Connection conn = null;
        PreparedStatement stmt = null;
        // Establish database connection


        try {
            conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
            // Define SQL query with parameters for orderID and orderStatus
            String sql = "UPDATE VIPCUSTOMERS SET isVip = ? WHERE customerID = ?";

            // Create prepared statement with SQL query
            stmt = conn.prepareStatement(sql);

            // Set values for parameters in prepared statement
            stmt.setBoolean(1, true);
            stmt.setInt(2, Integer.parseInt(customerID));

            // Execute the update
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("VIP status updated successfully!");
            } else {
                System.out.println("No matching customer found 2.");
            }
        } finally {
            // Close the database connection
            if (conn != null) {
                conn.close();
            }
        }

    }

    public void updateLastVip(String customerID) throws SQLException {
        // Declare variables for JDBC objects
        Connection conn = null;
        PreparedStatement stmt = null;
        //get current unix time and set to variable called currentTime
        long currentTime = System.currentTimeMillis();
        // Establish database connection


        try {
            conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
            // Define SQL query with parameters for orderID and orderStatus
            String sql = "UPDATE VIPCUSTOMERS SET lastVip = ? WHERE customerID = ?";

            // Create prepared statement with SQL query
            stmt = conn.prepareStatement(sql);

            // Set values for parameters in prepared statement
            stmt.setLong(1, currentTime);
            stmt.setInt(2, Integer.parseInt(customerID));

            // Execute the update
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("VIP status updated successfully!");
            } else {
                System.out.println("No matching customer found 3.");
            }
        } finally {
            // Close the database connection
            if (conn != null) {
                conn.close();
            }
        }

    }

}
