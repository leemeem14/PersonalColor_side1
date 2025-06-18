package kr.ac.kopo.lyh.personalcolor.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러
 * 애플리케이션 전반의 예외를 일관되게 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 인증 실패 예외 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException e, HttpServletRequest request) {
        log.warn("인증 실패 - 요청 URI: {}, 메시지: {}", request.getRequestURI(), e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "AUTHENTICATION_FAILED");
        response.put("message", "인증에 실패했습니다. 다시 로그인해주세요.");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 잘못된 자격 증명 예외 처리
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException e, HttpServletRequest request) {
        log.warn("잘못된 자격 증명 - 요청 URI: {}", request.getRequestURI());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "BAD_CREDENTIALS");
        response.put("message", "아이디 또는 비밀번호가 올바르지 않습니다.");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 접근 권한 부족 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
        log.warn("접근 권한 부족 - 요청 URI: {}, 사용자: {}",
                request.getRequestURI(), request.getRemoteUser());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "ACCESS_DENIED");
        response.put("message", "해당 리소스에 접근할 권한이 없습니다.");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 핸들러를 찾을 수 없는 예외 처리 (404 에러)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(
            NoHandlerFoundException e, HttpServletRequest request) {

        // favicon.ico 요청은 로그 레벨을 낮춤
        if (request.getRequestURI().contains("favicon.ico")) {
            log.debug("Favicon 요청 - URI: {}", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        log.warn("존재하지 않는 엔드포인트 요청 - URI: {}, 메서드: {}",
                request.getRequestURI(), request.getMethod());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "NOT_FOUND");
        response.put("message", "요청하신 페이지를 찾을 수 없습니다.");
        response.put("path", request.getRequestURI());
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 유효성 검증 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException e) {
        log.warn("유효성 검증 실패: {}", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        response.put("error", "VALIDATION_FAILED");
        response.put("message", "입력 데이터 검증에 실패했습니다.");
        response.put("errors", errors);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 런타임 예외 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException e, HttpServletRequest request) {
        log.error("런타임 예외 발생 - 요청 URI: {}, 메시지: {}",
                request.getRequestURI(), e.getMessage(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "RUNTIME_ERROR");
        response.put("message", e.getMessage() != null ? e.getMessage() : "처리 중 오류가 발생했습니다.");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 일반적인 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception e, HttpServletRequest request) {
        log.error("예상치 못한 예외 발생 - 요청 URI: {}", request.getRequestURI(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "INTERNAL_SERVER_ERROR");
        response.put("message", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}