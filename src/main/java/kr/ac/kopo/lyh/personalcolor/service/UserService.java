package kr.ac.kopo.lyh.personalcolor.service;

import kr.ac.kopo.lyh.personalcolor.entity.User;
import kr.ac.kopo.lyh.personalcolor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 생성
     */
    public User createUser(String email, String password, String name) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 사용자 생성 및 저장
        User user = new User(email, encodedPassword, name);
        User savedUser = userRepository.save(user);

        log.info("새 사용자 생성: {}", email);
        return savedUser;
    }

    /**
     * 사용자 인증
     */
    @Transactional(readOnly = true)
    public User authenticate(String email, String password) {
        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        // 로그인 시간 업데이트 (비동기로 처리)
        updateLastLoginTime(user.getId());

        log.info("사용자 로그인: {}", email);
        return user;
    }

    /**
     * 이메일로 사용자 조회
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    /**
     * 이메일 중복 검사
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    private void updateLastLoginTime(Long userId) {
        try {
            userRepository.updateLastLoginTime(userId, LocalDateTime.now());
        } catch (Exception e) {
            log.warn("로그인 시간 업데이트 실패: userId={}", userId, e);
        }
    }

    /**
     * 사용자 비활성화
     */
    public void deactivateUser(String email) {
        User user = findByEmail(email);
        user.setIsActive(false);
        userRepository.save(user);
        log.info("사용자 비활성화: {}", email);
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = findByEmail(email);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호가 올바르지 않습니다.");
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);
        userRepository.save(user);

        log.info("사용자 비밀번호 변경: {}", email);
    }
}