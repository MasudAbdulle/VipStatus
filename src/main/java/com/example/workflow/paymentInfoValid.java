package com.example.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class paymentInfoValid implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {



        System.out.println("payment details okayokayokay!!!!!!");
    }
}
