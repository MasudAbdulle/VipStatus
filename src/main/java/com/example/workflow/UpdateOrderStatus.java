package com.example.workflow;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import java.sql.*;



public class UpdateOrderStatus implements JavaDelegate{

    private static final String DB_CONNECTION = "jdbc:h2:file:./camunda-h2-database";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";

    @Override
    public void execute(DelegateExecution execution) throws Exception{

        try{
            //if execution variable orderFulfilledBox is true, update order status the Completed using orderID from execution variable
            if(execution.getVariable("orderFulfilledBox") != null){
                boolean orderFulfilledBox = (boolean) execution.getVariable("orderFulfilledBox");
                if(orderFulfilledBox == true) {
                    int orderID = (int) execution.getVariable("orderID");
                    updateOrderStatus(orderID);
                    System.out.println("Order status updated to Completed.");
                }else{
                    System.out.println("Order status not updated.");
                }
            }

            System.out.println("UpdateOrderStatus called");
        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
            throw new BpmnError("Error in UpdateOrderStatus", e.getMessage());
        }
    }



    public void updateOrderStatus(int orderID) throws SQLException {
        // Declare variables for JDBC objects
        Connection conn = null;
        PreparedStatement stmt = null;
        // Establish database connection


        try {
            conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
            // Define SQL query with parameters for orderID and orderStatus
            String sql = "UPDATE ORDERS SET orderstatus = ? WHERE orderID = ?";

            // Create prepared statement with SQL query
            stmt = conn.prepareStatement(sql);

            // Set values for parameters in prepared statement
            stmt.setString(1, "Completed");
            stmt.setInt(2, orderID);

            // Execute the update
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Order status updated successfully!");
            } else {
                System.out.println("No matching order found.");
            }
        } finally {
            // Close the database connection
            if (conn != null) {
                conn.close();
            }
        }
    }

}
