package com.example.workflow;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class UpdateMembership implements JavaDelegate{

    @Override
    public void execute(DelegateExecution execution) throws Exception{

        try{
            if(true == true){
                execution.setVariable("membershipUpdated", true);
            }else{
                execution.setVariable("membershipUpdated", false);
            }
        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
            throw new BpmnError("Error in UpdateMemebrship", e.getMessage());
        }
    }
}
