package com.example.workflow;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.io.File;
import java.sql.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;



public class CreateXML implements JavaDelegate {

    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:file:./camunda-h2-database";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            //get orderID from execution variable
            int orderID = (int) execution.getVariable("orderID");

            //call createXML method
            createXML(orderID);
            System.out.println("XML created");

            //set execution variable xmlCreated to true
            execution.setVariable("xmlCreated", true);

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            throw new BpmnError("Error in CreateXML", e.getMessage());
        }
    }
    public void createXML(int orderID) throws SQLException, ParserConfigurationException, TransformerException {
        // Connect to the H2 database
        Connection conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
        // Declare variables for JDBC objects
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Define SQL query with parameter for orderID
            String sql = "SELECT o.products, o.orderID, o.deliveryType, vc.firstName, vc.lastName, vc.address, vc.postcode FROM orders o JOIN vipCustomers vc ON o.customerID = vc.customerID WHERE o.orderID = ?";
            stmt = conn.prepareStatement(sql);
            // Set the value for the orderID parameter
            stmt.setInt(1, orderID);

            // Execute the SQL statement and get the result set
            rs = stmt.executeQuery();

            // Create a new DOM document
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            // Create the root element
            Element rootElement = doc.createElement("orders");
            doc.appendChild(rootElement);

            // Iterate over the result set and create XML elements for each column
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                Element orderElement = doc.createElement("order");
                rootElement.appendChild(orderElement);

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnValue = rs.getString(i);

                    Element columnElement = doc.createElement(columnName);
                    columnElement.appendChild(doc.createTextNode(columnValue));
                    orderElement.appendChild(columnElement);
                }
            }

            // Write the XML document to a file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(orderID + ".xml"));
            transformer.transform(source, result);

            // Close the result set and statement
            rs.close();
            stmt.close();
        } finally {
            // Close the database connection
            if (conn != null) {
                conn.close();
            }
        }
    }

}


