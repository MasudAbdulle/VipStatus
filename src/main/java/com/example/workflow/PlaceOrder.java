package com.example.workflow;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.sql.*;

public class PlaceOrder implements JavaDelegate{

    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:file:./camunda-h2-database";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";


    @Override
    public void execute(DelegateExecution execution) throws Exception{
        //Sent data to db
        try{
            // if membershipValid is not null and is false set orderStatus to "Cancelled" else set to "In Progress"
            String orderStatus = "unknown";
            if(execution.getVariable("membershipValid") != null){
                boolean membershipValid = (boolean) execution.getVariable("membershipValid");
                if(membershipValid == false) {
                    orderStatus = "Cancelled";
                }else{
                    orderStatus = "In Progress";
                }

            }else {
                orderStatus = "In Progress";
            }

            //get current Unix time
            String timeOrdered = String.valueOf(System.currentTimeMillis() / 1000L);

            //get products
            String products = execution.getVariable("itemCodes").toString();
            System.out.println("Products: " + products);
            //get customer number
            String customerID = execution.getVariable("customerID").toString();
            System.out.println("customerID: " + customerID);
            //get order type
            String deliveryType = execution.getVariable("deliveryType").toString();
            System.out.println("deliveryType: " + deliveryType);

            String deliveryPrice = "";

            //if vipYes boolean is true, set customerType to "vip" else set to "standard"
            String customerType = "unknown";
            boolean vipYes = (boolean) execution.getVariable("vipYes");
            //if vipYes boolean is true, set customerType to "vip" else set to "standard"
            if(vipYes){

                //if delivery type is standard, set delivery price to 0
                if(deliveryType.equals("standard")){
                    deliveryPrice = "0";
                }
                //else if delivery type is next-day, set delivery price to 9.95
                else if(deliveryType.equals("next-day")){
                    deliveryPrice = "3.95";
                }
            }else{

                //if delivery type is standard, set delivery price to 6.95
                if(deliveryType.equals("standard")){
                    deliveryPrice = "6.95";
                }
                //else if delivery type is next-day, set delivery price to 9.95
                else if(deliveryType.equals("next-day")){
                    deliveryPrice = "9.95";
                }

            }


            //connect to database and insert new customer
            int orderID = pushToDb(products, orderStatus, timeOrdered, customerID, deliveryType, vipYes, deliveryPrice);
            //set execution variable for orderID
            execution.setVariable("orderID", orderID);
        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
            throw new BpmnError("Error in PlaceOrder", e.getMessage());
        }
    }

    private int pushToDb(String products, String orderStatus, String timeOrdered, String customerID, String deliveryType, boolean customerType, String deliveryPrice) throws SQLException, ClassNotFoundException{

        // Declare variables for JDBC objects
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Load the H2 driver class
            Class.forName("org.h2.Driver");

            // Establish database connection
            conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);

            // Define SQL query with parameters for new customer data
            String sql = "INSERT INTO orders (products, orderstatus, timeOrdered, customerID, deliveryType, vip, deliveryPrice) VALUES (?, ?, ?, ?, ?, ?, ?)";
            String[] generatedColumns = {"orderID"};
            stmt = conn.prepareStatement(sql, generatedColumns);

            // Set values for parameters in prepared statement
            stmt.setString(1, products);
            stmt.setString(2, orderStatus);
            stmt.setString(3, timeOrdered);
            stmt.setString(4, customerID);
            stmt.setString(5, deliveryType);
            stmt.setBoolean(6, customerType);
            stmt.setString(7, deliveryPrice);

            // Execute SQL statement to insert new record
            int rowsAffected = stmt.executeUpdate();

            // Retrieve the generated orderID
            int orderID = 0;
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                orderID = rs.getInt(1);
                System.out.println("New record inserted with orderID: " + orderID);
                //set execution variable orderID
            }

            // Check number of rows affected by SQL statement
            if (rowsAffected > 0) {
                System.out.println("Order details: " + "\nOrder ID: " + orderID + "\n Products: " + products + "\n Time Ordered: " + timeOrdered
                        + "\n Customer ID: " + customerID + "\n Order Status: " + orderStatus + "\n Delivery Type: " + deliveryType + "\n Customer Type: " + customerType);
            } else {
                System.out.println("Failed to insert new record.");
            }
            return orderID;

        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        } finally {
            // Close JDBC objects in reverse order of creation
            try {
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
        return 0;
    }


}


