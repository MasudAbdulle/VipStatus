package com.example.workflow;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class SendToWh implements JavaDelegate{

    @Override
    public void execute(DelegateExecution execution) throws Exception{
        try{
            // if execution variable xmlCreated is not null and is true, set xmlSent to true
            if(execution.getVariable("xmlCreated") != null){
                boolean xmlCreated = (boolean) execution.getVariable("xmlCreated");
                if(xmlCreated == true) {
                    execution.setVariable("xmlSent", true);
                    System.out.println("XML sent to warehouse.");
                }else{
                    execution.setVariable("xmlSent", false);
                    System.out.println("XML not sent to warehouse.");
                }
            }
        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
            throw new BpmnError("Error in SendToWh", e.getMessage());
        }
    }
}

