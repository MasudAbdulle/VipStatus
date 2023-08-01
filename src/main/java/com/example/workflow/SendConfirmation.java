package com.example.workflow;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class SendConfirmation implements JavaDelegate{

    @Override
    public void execute(DelegateExecution execution) throws Exception{

        try{
            execution.setVariable("confirmationSent", true);
        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
            throw new BpmnError("Error in SendConfirmation", e.getMessage());
        }
    }
}