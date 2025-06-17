/**
 * PersonalColor 애플리케이션의 인증 관련 유틸리티
 * 로그인, 로그아웃, 인증 상태 확인 등의 공통 기능 제공
 */

// CSRF 토큰 가져오기
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

/**
 * 현재 사용자의 인증 상태를 확인
 * @returns {Promise<{isAuthenticated: boolean, userEmail: string|null}>}
 */
async function checkAuthStatus() {
    try {
        const response = await fetch('/api/user/current');
        const data = await response.json();

        if (data.success) {
            return {
                isAuthenticated: data.isAuthenticated,
                userEmail: data.userEmail || null
            };
        }
        return { isAuthenticated: false, userEmail: null };
    } catch (error) {
        console.error('인증 상태 확인 실패:', error);
        return { isAuthenticated: false, userEmail: null };
    }
}

/**
 * 로그인 처리
 * @param {string} email - 사용자 이메일
 * @param {string} password - 사용자 비밀번호
 * @returns {Promise<{success: boolean, message: string, redirectUrl: string|null}>}
 */
async function login(email, password) {
    try {
        const response = await fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ email, password })
        });

        return await response.json();
    } catch (error) {
        console.error('로그인 요청 실패:', error);
        return {
            success: false,
            message: '로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
        };
    }
}

/**
 * 로그아웃 처리
 * @returns {Promise<{success: boolean, message: string, redirectUrl: string|null}>}
 */
async function logout() {
    try {
        const response = await fetch('/api/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        });

        const data = await response.json();

        // 클라이언트 측 세션 스토리지 정리
        sessionStorage.removeItem('isLoggedIn');
        sessionStorage.removeItem('userEmail');

        return data;
    } catch (error) {
        console.error('로그아웃 요청 실패:', error);

        // 에러가 발생해도 Spring Security의 기본 로그아웃으로 처리
        window.location.href = '/logout';

        return {
            success: false,
            message: '로그아웃 중 오류가 발생했습니다.'
        };
    }
}

/**
 * 회원가입 처리
 * @param {Object} userData - 사용자 데이터
 * @param {string} userData.name - 사용자 이름
 * @param {string} userData.email - 사용자 이메일
 * @param {string} userData.password - 사용자 비밀번호
 * @param {string} userData.passwordConfirm - 비밀번호 확인
 * @returns {Promise<{success: boolean, message: string, redirectUrl: string|null}>}
 */
async function signup(userData) {
    try {
        // 비밀번호 일치 확인
        if (userData.password !== userData.passwordConfirm) {
            return {
                success: false,
                message: '비밀번호가 일치하지 않습니다.'
            };
        }

        const response = await fetch('/api/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(userData)
        });

        return await response.json();
    } catch (error) {
        console.error('회원가입 요청 실패:', error);
        return {
            success: false,
            message: '회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
        };
    }
}

/**
 * 메시지 표시 유틸리티
 */
const messageUtil = {
    /**
     * 에러 메시지 표시
     * @param {string} message - 표시할 메시지
     * @param {string} elementId - 메시지를 표시할 요소 ID (기본값: 'errorMessage')
     * @param {number} timeout - 자동으로 숨기는 시간(ms) (기본값: 5000)
     */
    showError: function(message, elementId = 'errorMessage', timeout = 5000) {
        const errorElement = document.getElementById(elementId);
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';

            if (timeout > 0) {
                setTimeout(() => {
                    errorElement.style.display = 'none';
                }, timeout);
            }
        }
    },

    /**
     * 성공 메시지 표시
     * @param {string} message - 표시할 메시지
     * @param {string} elementId - 메시지를 표시할 요소 ID (기본값: 'successMessage')
     * @param {number} timeout - 자동으로 숨기는 시간(ms) (기본값: 5000)
     */
    showSuccess: function(message, elementId = 'successMessage', timeout = 5000) {
        const successElement = document.getElementById(elementId);
        if (successElement) {
            successElement.textContent = message;
            successElement.style.display = 'block';

            if (timeout > 0) {
                setTimeout(() => {
                    successElement.style.display = 'none';
                }, timeout);
            }
        }
    },

    /**
     * 메시지 숨김
     * @param {...string} elementIds - 숨길 요소의 ID 목록
     */
    hideMessages: function(...elementIds) {
        elementIds.forEach(id => {
            const element = document.getElementById(id);
            if (element) {
                element.style.display = 'none';
            }
        });
    }
};

/**
 * 폼 유효성 검사 유틸리티
 */
const validationUtil = {
    /**
     * 이메일 유효성 검사
     * @param {string} email - 검사할 이메일 주소
     * @returns {boolean} 유효한 이메일이면 true
     */
    isValidEmail: function(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    },

    /**
     * 비밀번호 강도 검사
     * @param {string} password - 검사할 비밀번호
     * @returns {string} 비밀번호 강도 ('weak', 'medium', 'strong')
     */
    checkPasswordStrength: function(password) {
        if (!password || password.length < 6) {
            return 'weak';
        }

        const hasUpperCase = /[A-Z]/.test(password);
        const hasLowerCase = /[a-z]/.test(password);
        const hasNumbers = /\d/.test(password);
        const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);

        const strength = [hasUpperCase, hasLowerCase, hasNumbers, hasSpecial].filter(Boolean).length;

        if (strength >= 3 && password.length >= 8) {
            return 'strong';
        } else if (strength >= 2 && password.length >= 6) {
            return 'medium';
        }
        return 'weak';
    }
};

/**
 * 로딩 상태 관리 유틸리티
 */
const loadingUtil = {
    /**
     * 로딩 상태 설정
     * @param {HTMLElement|string} element - 로딩 상태를 적용할 요소 또는 요소 ID
     * @param {boolean} isLoading - 로딩 중인지 여부
     * @param {string} loadingText - 로딩 중 표시할 텍스트 (기본값: '로딩 중...')
     * @param {string} defaultText - 로딩이 아닐 때 표시할 텍스트
     */
    setLoading: function(element, isLoading, loadingText = '로딩 중...', defaultText = '') {
        const targetElement = typeof element === 'string' ? document.getElementById(element) : element;

        if (!targetElement) return;

        if (isLoading) {
            targetElement.classList.add('loading');
            if (targetElement.tagName === 'BUTTON' || targetElement.tagName === 'INPUT') {
                if (defaultText === '' && !targetElement.dataset.originalText) {
                    targetElement.dataset.originalText = targetElement.textContent || targetElement.value;
                }
                if (targetElement.tagName === 'BUTTON') {
                    targetElement.textContent = loadingText;
                } else {
                    targetElement.value = loadingText;
                }
                targetElement.disabled = true;
            }
        } else {
            targetElement.classList.remove('loading');
            if (targetElement.tagName === 'BUTTON' || targetElement.tagName === 'INPUT') {
                const originalText = defaultText || targetElement.dataset.originalText || '';
                if (targetElement.tagName === 'BUTTON') {
                    targetElement.textContent = originalText;
                } else {
                    targetElement.value = originalText;
                }
                targetElement.disabled = false;
            }
        }
    }
};

// 모듈 내보내기
window.authUtil = {
    checkAuthStatus,
    login,
    logout,
    signup,
    message: messageUtil,
    validation: validationUtil,
    loading: loadingUtil
};
