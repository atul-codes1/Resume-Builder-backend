package com.atul.ResumeBuilder.controller;

import com.atul.ResumeBuilder.document.Payment;
import com.atul.ResumeBuilder.service.PaymentService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.atul.ResumeBuilder.util.AppConstants.PREMIUM;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, String> request,
                                         Authentication authentication) throws RazorpayException {
        String planType = request.get("planType");
        if(!PREMIUM.equalsIgnoreCase(planType)){
            return ResponseEntity.badRequest().body((Map.of("message","Invalid plan type")));
        }
        Payment payment = paymentService.createOrder(authentication.getPrincipal(),planType);
        Map<String,Object> response =Map.of(
                "orderId",payment.getRazorpayOrderId(),
                "amount",payment.getAmount(),
                "currency",payment.getCurrency(),
                "receipt",payment.getReceipt());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> request) throws RazorpayException {
           String razorpayOrderId = request.get("razorpay_order_id");
           String razorpayPaymentId = request.get("razorpay_payment_id");
           String razorpaySignature  =request.get("razorpay_signature");

           if(Objects.isNull(razorpayOrderId)||
                   Objects.isNull(razorpayPaymentId)||
                   Objects.isNull(razorpaySignature)){
               return ResponseEntity.badRequest().body(Map.of("message","Missing required payment parameters"));
           }
           boolean isValid = paymentService.verifyPayment(razorpayOrderId,razorpayPaymentId,razorpaySignature);

           if(!isValid){
               return ResponseEntity.ok(Map.of(
                       "message","payment verified successfully",
                       "status","success"
               ));
           }else{
               return ResponseEntity.badRequest().body(Map.of("message","Payment verification failed"));
           }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory(Authentication authentication) {

        List<Payment> payments = paymentService.getUserPayments(authentication.getPrincipal());
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId) {

        Payment paymentDetails = paymentService.getPaymentDetails(orderId);
        return ResponseEntity.ok(paymentDetails);
    }
}