package com.example.workflow;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class OrderType implements JavaDelegate{

    @Override
    public void execute(DelegateExecution execution) throws Exception{
        try{
            if(execution.getVariable("phoneOrder") != null){
                boolean phoneOrder = (boolean) execution.getVariable("phoneOrder");
                if(phoneOrder == true) {
                    execution.setVariable("isPhoneOrder", true);
                    System.out.println("Order is by phone.");
                }else{
                    execution.setVariable("isPhoneOrder", false);
                    System.out.println("Order is not by phone.");
                }
            }
        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
            throw new BpmnError("Input_invalid", e.getMessage());
        }
    }
}




