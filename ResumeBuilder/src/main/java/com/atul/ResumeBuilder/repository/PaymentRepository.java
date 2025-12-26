package com.atul.ResumeBuilder.repository;

import com.atul.ResumeBuilder.document.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment,String> {

  Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
  Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);
  List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);
  List<Payment> findByStatus(String status);
}
