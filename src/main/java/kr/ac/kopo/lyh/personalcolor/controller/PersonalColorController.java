package kr.ac.kopo.lyh.personalcolor.controller;

import kr.ac.kopo.lyh.personalcolor.controller.dto.ApiError;
import kr.ac.kopo.lyh.personalcolor.controller.dto.PersonalColorResult;
import kr.ac.kopo.lyh.personalcolor.service.PersonalColorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/personal-color")
@Slf4j
public class PersonalColorController {
    private final PersonalColorService personalColorService;

    public PersonalColorController(PersonalColorService personalColorService) {
        this.personalColorService = personalColorService;
    }

    @GetMapping
    public String showUploadForm() {
        return "upload-form";
    }

    @PostMapping("/analyze")
    @ResponseBody
    public ResponseEntity<?> analyzeImage(@RequestParam("image") MultipartFile image) {
        try {
            if (image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiError("No image uploaded", "BAD_REQUEST", 400));
            }

            PersonalColorResult result = personalColorService.analyzeImage(image);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error processing image: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Error processing image", "INTERNAL_SERVER_ERROR", 500));
        }
    }
}
