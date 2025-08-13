package oba.backend.server.service;

import oba.backend.server.dto.AuthDto.LoginRequest;
import oba.backend.server.dto.AuthDto.SignUpRequest;
import oba.backend.server.dto.AuthDto.TokenResponse;
import oba.backend.server.entity.Member;
import oba.backend.server.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional // 각 테스트가 끝난 후, 데이터베이스 변경사항을 원래대로 되돌려(롤백) 다음 테스트에 영향을 주지 않게 합니다.
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private SignUpRequest signUpRequest;

    @BeforeEach
    void setUp() {
        // 각 테스트가 실행되기 전에 공통적으로 사용할 회원가입 데이터를 미리 준비합니다.
        signUpRequest = new SignUpRequest("testuser", "Password123!");
    }

    @Test
    @DisplayName("회원가입 성공: 사용자가 올바른 정보로 가입을 시도하면 DB에 정상적으로 저장된다.")
    void signUp_success() {
        // given (주어진 상황)
        // signUpRequest는 @BeforeEach에서 준비됨

        // when (무엇을 할 때)
        authService.signUp(signUpRequest);

        // then (결과는 이래야 한다)
        // DB에서 방금 가입한 사용자를 찾는다.
        Member foundMember = memberRepository.findByUsername("testuser")
                .orElseThrow(() -> new AssertionError("테스트 실패: 사용자를 찾을 수 없습니다."));

        // 사용자 이름이 일치하는지 확인한다.
        assertThat(foundMember.getUsername()).isEqualTo("testuser");
        // 비밀번호가 암호화되어 저장되었는지 확인한다.
        assertTrue(passwordEncoder.matches("Password123!", foundMember.getPassword()));
    }

    @Test
    @DisplayName("회원가입 실패: 이미 존재하는 아이디로 가입을 시도하면 예외가 발생한다.")
    void signUp_fail_duplicateUsername() {
        // given
        // 먼저 사용자를 한 명 가입시켜 놓는다.
        authService.signUp(signUpRequest);

        // when & then
        // 똑같은 아이디로 다시 가입을 시도하면 RuntimeException이 발생하는지 확인한다.
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.signUp(signUpRequest);
        });

        // 예외 메시지가 "이미 사용 중인 아이디입니다."와 일치하는지 확인한다.
        assertThat(exception.getMessage()).isEqualTo("이미 사용 중인 아이디입니다.");
    }


    @Test
    @DisplayName("로그인 성공: 올바른 아이디와 비밀번호로 로그인하면 토큰이 발급된다.")
    void login_success() {
        // given
        // 먼저 회원가입을 시켜놓는다.
        authService.signUp(signUpRequest);
        LoginRequest loginRequest = new LoginRequest("testuser", "Password123!");

        // when
        TokenResponse tokenResponse = authService.login(loginRequest);

        // then
        // 토큰이 정상적으로 발급되었는지 확인한다.
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.accessToken()).isNotBlank();
        assertThat(tokenResponse.refreshToken()).isNotBlank();

        // DB에 Refresh Token이 잘 저장되었는지 확인한다.
        Member member = memberRepository.findByUsername("testuser").get();
        assertThat(member.getRefreshToken()).isEqualTo(tokenResponse.refreshToken());
    }

    @Test
    @DisplayName("로그인 실패: 잘못된 비밀번호로 로그인하면 예외가 발생한다.")
    void login_fail_wrongPassword() {
        // given
        authService.signUp(signUpRequest);
        LoginRequest loginRequest = new LoginRequest("testuser", "WrongPassword123!");

        // when & then
        // assertThrows를 사용하여 BadCredentialsException이 발생하는 것이
        // 이 테스트의 '성공' 조건임을 명시합니다.
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    @DisplayName("로그인 성공 디버깅")
    void login_success_debugging() {
        // --- GIVEN ---
        System.out.println("1. 회원가입 요청 데이터: " + signUpRequest);

        // 회원가입 실행
        authService.signUp(signUpRequest);

        // DB에 저장된 암호화된 비밀번호 직접 확인
        Member savedMember = memberRepository.findByUsername("testuser")
                .orElseThrow(() -> new AssertionError("DB에서 사용자를 찾지 못했습니다."));
        String savedEncodedPassword = savedMember.getPassword();
        System.out.println("2. DB에 저장된 암호화된 비밀번호: " + savedEncodedPassword);
        System.out.println("   (길이: " + savedEncodedPassword.length() + ")");


        // 로그인 요청 객체 생성
        LoginRequest loginRequest = new LoginRequest("testuser", "Password123!");
        System.out.println("3. 로그인 요청 데이터: " + loginRequest);


        // --- WHEN ---
        // 테스트 코드에서 직접 비밀번호를 비교해보기
        // 이 값이 반드시 true로 나와야 합니다.
        boolean isMatchInTest = passwordEncoder.matches(loginRequest.password(), savedEncodedPassword);
        System.out.println("4. 테스트 코드에서 직접 비교 결과: " + isMatchInTest);


        // --- THEN ---
        // 실제 로그인 로직 실행
        System.out.println("5. 실제 AuthService.login() 로직 실행...");
        try {
            TokenResponse tokenResponse = authService.login(loginRequest);
            System.out.println("6. 로그인 성공! 토큰: " + tokenResponse.accessToken());
            assertThat(tokenResponse).isNotNull();
        } catch (Exception e) {
            System.err.println("6. 로그인 실패! 발생한 예외: " + e.getClass().getName());
            // 예외의 전체 내용을 보고 싶으면 아래 줄의 주석을 푸세요.
            // e.printStackTrace();
            throw e; // 테스트가 실패하도록 예외를 다시 던짐
        }
    }
}