package com.example.workflow;


import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.util.Calendar;
import java.util.Date;

public class expiry implements JavaDelegate {

    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:file:./camunda-h2-database";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Retrieve the process instance ID
        String processInstanceId = execution.getProcessInstanceId();

        // Retrieve the customerId from process variables
        String customerId = (String) execution.getVariable("customerID");

        try {
            // Get the database connection from the Camunda process engine
            Connection connection = ProcessEngines.getDefaultProcessEngine().getProcessEngineConfiguration().getDataSource().getConnection();

            try {
                // Get the current Unix timestamp in seconds
                long currentUnixTimestamp = new Date().getTime() / 1000;

                // Calculate the Unix timestamp for 6 months from now
                long sixMonthsLaterUnixTimestamp = currentUnixTimestamp + (6L * 30L * 24L * 60L * 60L);

                // Prepare the SQL statement to update the expiry date
                String updateSql = "UPDATE VIPCUSTOMERS SET expiryDate = ? WHERE customerId = ?";

                try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                    // Set the expiry date and customer ID values in the SQL statement
                    statement.setLong(1, sixMonthsLaterUnixTimestamp);
                    statement.setString(2, customerId);

                    // Execute the SQL statement
                    statement.executeUpdate();

                    System.out.println("Expiry date updated successfully for customer: " + customerId);
                } catch (SQLException e) {
                    System.out.println("Failed to update expiry date: " + e.getMessage());
                }
            } finally {
                // Close the database connection
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Failed to obtain database connection: " + e.getMessage());
        }
    }

}