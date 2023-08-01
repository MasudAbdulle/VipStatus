package com.example.workflow;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import java.sql.*;



public class CheckForAccount implements JavaDelegate{

    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:file:./camunda-h2-database";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";

    @Override
    public void execute(DelegateExecution execution) throws Exception{
        boolean customerExists = false;
        try{
            // set alreadyVip to false
            execution.setVariable("alreadyVip", false);
            // Get the input variables
            String emailAddress = execution.getVariable("emailAddress").toString();
            String lastName = execution.getVariable("lastName").toString();
            String postcode = execution.getVariable("postcode").toString();

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

                // Check if there is any matching record in the result set
                if(rs.next()){
                    customerExists = true;
                    String customerID = rs.getString("customerID");
                    boolean vip = rs.getBoolean("isVip");
                    System.out.println("Customer Found! Customer ID: " + customerID + " VIP: " + vip);
                    execution.setVariable("customerID", customerID);
                    execution.setVariable("vipYes", vip);
                }

            } catch (SQLException e) {
                // Handle any errors that occur during database connection or query execution
                System.out.println(e.getMessage());
                customerExists = false;
            }

            execution.setVariable("customerExists", customerExists);
        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
            throw new BpmnError("Error in CheckForAccount", e.getMessage());
        }
    }
    

}
