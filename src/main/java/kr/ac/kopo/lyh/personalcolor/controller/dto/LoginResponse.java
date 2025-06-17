package kr.ac.kopo.lyh.personalcolor.controller.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private boolean success;
    private String message;
    private String redirectUrl;
    private String userEmail;
    private String error;
}
