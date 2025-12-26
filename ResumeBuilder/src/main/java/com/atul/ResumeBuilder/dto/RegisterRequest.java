package com.atul.ResumeBuilder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @Email(message = "Email should be Valid")
    @NotBlank(message = "Email is Required")
    private String email;
    @NotBlank(message = "Name is Required")
    @Size(min = 2,max =15,message = "Name should be between 2 and 15 characters")
    private String name;
    @NotBlank(message = "Password is Required")
    @Size(min =6,max = 15,message = "Password must be between 6 and 15 characters")
    private String password;
    private String profileImageUrl;
}
