package com.atul.ResumeBuilder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateResumeRequest {

    @NotBlank(message ="Title is Required")
    private String title;
}
