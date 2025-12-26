package com.atul.ResumeBuilder.service;

import com.atul.ResumeBuilder.document.User;
import com.atul.ResumeBuilder.dto.AuthResponse;
import com.atul.ResumeBuilder.dto.LoginRequest;
import com.atul.ResumeBuilder.dto.RegisterRequest;
import com.atul.ResumeBuilder.exception.ResourceExistsException;
import com.atul.ResumeBuilder.repository.UserRepository;
import com.atul.ResumeBuilder.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.base.url}")
    private String appBaseUrl;

    public AuthResponse register(RegisterRequest request){
        log.info("Inside AuthService: register() {}",request);

        if(userRepository.existsByEmail(request.getEmail())){
            throw new ResourceExistsException(" User already exists with this email");
        }
        User newUser = toDocument(request);
        userRepository.save(newUser);
        sendVerificationEmail(newUser);

        return toResponse(newUser);
    }

    private void sendVerificationEmail(User newUser) {
        log.info("Inside AuthService - sendVerificationEmail(): {}",newUser);
        try {
                String link = appBaseUrl+"/api/auth/verify-email?token="+newUser.getVerificationToken();

                String html ="<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; background-color: #fff; padding: 20px; border-radius: 5px;'>" +
                        "<h2>Hello " + newUser.getName() + ",</h2>" +
                        "<p>Thank you for registering. Please verify your email by clicking the button below or copying the link:</p>" +

                        // Clickable button
                        "<div style='text-align: center; margin: 20px 0;'>" +
                        "<a href='" + link + "' style='padding: 10px 20px; background-color: #007bff; color: #fff; text-decoration: none; border-radius: 5px;'>Verify Email</a>" +
                        "</div>" +

                        // Display link for copy-paste
                        "<p>If the button does not work, copy and paste this link into your browser:</p>" +
                        "<div style='word-break: break-all;'>" +
                        "<a href='" + link + "'>" + link + "</a>" +
                        "</div>" +

                        "<p>This link will expire in 24 hours.</p>" +
                        "<p>Best regards,<br>Your ResumeBuilder Team</p>" +
                        "</div>" +
                        "</div>";
                emailService.sendHtmlMail(newUser.getEmail(), "Verify your email",html);
        } catch (Exception e) {
            log.error("Exception occured at sendVerificationEmail(): {}",e.getMessage());
            throw new RuntimeException("Failed to send verification email: "+e.getMessage());
        }
    }

    private AuthResponse toResponse(User newUser){
        return AuthResponse.builder()
                .id(newUser.getId())
                .name(newUser.getName())
                .email(newUser.getEmail())
                .profileImageUrl((newUser.getProfileImageUrl()))
                .emailVerified(newUser.isEmailVerified())
                .subscriptionPlan(newUser.getSubscriptionPlan())
                .createdAt(newUser.getCreatedAt())
                .updatedAt(newUser.getUpdatedAt())
                .build();
    }

    private User toDocument(RegisterRequest request){
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .profileImageUrl(request.getProfileImageUrl())
                .subscriptionPlan("Basic")
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .verificationExpires(LocalDateTime.now().plusHours(24))
                .build();
    }

    public void verifyEmail(String token){
        log.info("Inside AuthService: verifyEmail(): {}",token);
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(()-> new RuntimeException("Invalid or expired verification token"));

            if(user.getVerificationExpires() !=null && user.getVerificationExpires().isBefore(LocalDateTime.now())){
                throw new RuntimeException("Oops! Verification token has expired.Please request new token.");
            }
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            user.setVerificationExpires(null);
            userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request){
       User existingUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));
        if(!passwordEncoder.matches(request.getPassword(), existingUser.getPassword()))
            throw new UsernameNotFoundException("Invalid email or password");

        if(!existingUser.isEmailVerified()){
            throw new RuntimeException("Please verify your email before logging in.");
        }
        String token = jwtUtil.generateToken(existingUser.getId());
        AuthResponse response = toResponse(existingUser);
        response.setToken(token);

        return response;
    }

    public void resendVerification(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("User not found"));

        if(user.isEmailVerified()){
            throw new RuntimeException("Email is already verified");
        }
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationExpires(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        sendVerificationEmail(user);

    }

    public AuthResponse getProfile(Object principalObject) {

        User existingUser=(User) principalObject;
        return toResponse(existingUser);
    }
}
