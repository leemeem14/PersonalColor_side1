package kr.ac.kopo.lyh.personalcolor.service;

import kr.ac.kopo.lyh.personalcolor.entity.User;
import kr.ac.kopo.lyh.personalcolor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 회원가입 처리
     *
     * @param user 회원가입할 사용자 정보
     * @return 저장된 사용자 정보
     * @throws RuntimeException 중복된 사용자가 있을 경우
     */
    @Transactional
    public User signup(User user) {
        log.info("회원가입 시도 - 사용자명: {}", user.getUsername());

        // 중복 사용자 확인
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            log.warn("중복된 사용자명으로 회원가입 시도: {}", user.getUsername());
            throw new RuntimeException("이미 존재하는 사용자명입니다: " + user.getUsername());
        }

        // 이메일 중복 확인 (이메일 필드가 있는 경우)
        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()).isPresent()) {
            log.warn("중복된 이메일로 회원가입 시도: {}", user.getEmail());
            throw new RuntimeException("이미 존재하는 이메일입니다: " + user.getEmail());
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // 기본 역할 설정
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }

        // 사용자 저장
        User savedUser = userRepository.save(user);
        log.info("회원가입 완료 - 사용자 ID: {}, 사용자명: {}", savedUser.getId(), savedUser.getUsername());

        return savedUser;
    }

    /**
     * 사용자명으로 사용자 조회
     *
     * @param username 사용자명
     * @return 사용자 정보 (Optional)
     */
    public User findByUsername(String username) {
        log.debug("사용자 조회 - 사용자명: {}", username);
        return userRepository.findByUsername(username)
                .orElse(null);
    }

    /**
     * 이메일로 사용자 조회
     *
     * @param email 이메일
     * @return 사용자 정보 (Optional)
     */
    public User findByEmail(String email) {
        log.debug("사용자 조회 - 이메일: {}", email);
        return userRepository.findByEmail(email)
                .orElse(null);
    }

    /**
     * 비밀번호 확인
     *
     * @param rawPassword 원본 비밀번호
     * @param encodedPassword 암호화된 비밀번호
     * @return 비밀번호 일치 여부
     */
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 사용자 정보 업데이트
     *
     * @param user 업데이트할 사용자 정보
     * @return 업데이트된 사용자 정보
     */
    @Transactional
    public User updateUser(User user) {
        log.info("사용자 정보 업데이트 - 사용자 ID: {}", user.getId());

        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + user.getId()));

        // 필요한 필드 업데이트 (비밀번호는 별도 메서드로 처리)
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }

        return userRepository.save(existingUser);
    }

    /**
     * 비밀번호 변경
     *
     * @param userId 사용자 ID
     * @param newPassword 새 비밀번호
     */
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        log.info("비밀번호 변경 - 사용자 ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        userRepository.save(user);
        log.info("비밀번호 변경 완료 - 사용자 ID: {}", userId);
    }

    /**
     * 사용자 삭제
     *
     * @param userId 삭제할 사용자 ID
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("사용자 삭제 - 사용자 ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + userId);
        }

        userRepository.deleteById(userId);
        log.info("사용자 삭제 완료 - 사용자 ID: {}", userId);
    }
}