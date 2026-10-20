package com.cnjava.book_store.Payment;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PaymentSandboxService {
  public record PaymentResult(boolean approved,String reference,String message){}

  public PaymentResult authorize(String method,String paymentCode,BigDecimal amount){
    if(amount==null||amount.signum()<0) return new PaymentResult(false,null,"Invalid payment amount.");
    if(amount.signum()==0) return new PaymentResult(true,"DEMO-ZERO-"+shortId(),"No charge required after discount.");
    if(!"DEMO_CARD".equalsIgnoreCase(method)) return new PaymentResult(false,null,"Select DemoPay Sandbox.");
    String digits=paymentCode==null?"":paymentCode.replaceAll("\\D","");
    if("4242424242424242".equals(digits)) return new PaymentResult(true,"DEMO-"+shortId(),"DemoPay approved the transaction.");
    if("4000000000000002".equals(digits)) return new PaymentResult(false,null,"DemoPay declined this test card.");
    return new PaymentResult(false,null,"Use sandbox card 4242 4242 4242 4242 for success or 4000 0000 0000 0002 to test a decline.");
  }

  private String shortId(){return UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase();}
}
