package com.example.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class InformCustomer implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
System.out.println("inform customer about return status");
    }
}
