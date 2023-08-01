package com.example.workflow;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class CheckForCode implements JavaDelegate{

    @Override
    public void execute(DelegateExecution execution) throws Exception{
        String itemCodeString = execution.getVariable("itemCodes").toString();
        try{
            String itemCodes[] = itemCodeString.split(",");
            for(int i = 0; i < itemCodes.length; i++){
                System.out.println(itemCodes[i]);
                if(itemCodes[i].contains("1111")){
                    execution.setVariable("vipYes", true);
                    System.out.println("Customer wishes to become VIP member!");
                    break;
                }else{
                    execution.setVariable("vipYes", false);
                    System.out.println("Customer does not wish to become VIP member!");
                }
            }
        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
            throw new BpmnError("Input_invalid", e.getMessage());
        }
    }
}
