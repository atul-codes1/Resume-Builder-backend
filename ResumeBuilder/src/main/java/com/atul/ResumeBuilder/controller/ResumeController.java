package com.atul.ResumeBuilder.controller;

import com.atul.ResumeBuilder.document.Resume;
import com.atul.ResumeBuilder.dto.CreateResumeRequest;
import com.atul.ResumeBuilder.service.FileUploadService;
import com.atul.ResumeBuilder.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.atul.ResumeBuilder.util.AppConstants.*;

@RestController
@RequestMapping(RESUME)
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeService resumeService;
    private final FileUploadService fileUploadService;

    @PostMapping
    public ResponseEntity<?> createResume(@Valid @RequestBody CreateResumeRequest request,
                                          Authentication authentication){
        Resume newResume = resumeService.createResume(request,authentication.getPrincipal());
        return ResponseEntity.status(HttpStatus.CREATED).body(newResume);
    }
    @GetMapping
    public ResponseEntity<?> getUserResumes(Authentication authentication){
     List<Resume> resumes = resumeService.getUserResumes(authentication.getPrincipal());
     return ResponseEntity.ok(resumes);
    }
    @GetMapping(ID)
    public ResponseEntity<?> getResumeById(@PathVariable String id,Authentication authentication){
        Resume existingResume = resumeService.getResumeById(id,authentication.getPrincipal());
        return ResponseEntity.ok(existingResume);
    }

    @PutMapping(ID)
    public ResponseEntity<?> updateResume(@PathVariable String id,
                                          @RequestBody Resume updatedData,
                                          Authentication authentication){
        Resume updatedResume = resumeService.updateResume(id,updatedData,authentication.getPrincipal());
        return ResponseEntity.ok(updatedResume);
    }

    @PutMapping(UPLOAD_IMAGES)
    public ResponseEntity<?> uploadResumeImages(@PathVariable String id,
                                                @RequestPart(value ="thumbnail",required = false)MultipartFile thumbnail,
                                                @RequestPart(value ="profileImage",required = false) MultipartFile profileImage,
                                                Authentication authentication) throws IOException {
        Map<String,String> response = fileUploadService.uploadResumeImages(id,authentication.getPrincipal(),thumbnail,profileImage);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping(ID)
    public ResponseEntity<?> deleteResume(@PathVariable String id,
                                          Authentication authentication){
        resumeService.deleteResume(id,authentication.getPrincipal());
        return ResponseEntity.ok(Map.of("message","Resume deleted successfully"));
    }
}
