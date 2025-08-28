package oba.backend.server.service;

import lombok.RequiredArgsConstructor;
import oba.backend.server.dto.LoginRequest;
import oba.backend.server.dto.SignUpRequest;
import oba.backend.server.dto.TokenResponse;
import oba.backend.server.entity.Member;
import oba.backend.server.entity.Role;
import oba.backend.server.jwt.JwtProvider;
import oba.backend.server.repository.MemberRepository;
import oba.backend.server.security.SecurityUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final SecurityUtil securityUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void signUp(SignUpRequest signUpRequest) {
        // --- [변경 1] username -> email ---
        // 이메일 중복 여부를 확인합니다.
        if (memberRepository.existsByEmail(signUpRequest.email())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signUpRequest.password());

        // --- [변경 2] Builder에 email, nickname 전달 ---
        Member member = Member.builder()
                .email(signUpRequest.email())
                .nickname(signUpRequest.nickname())
                .password(encodedPassword)
                .role(Role.USER)
                .build();
        memberRepository.save(member);
    }

    @Transactional
    public void deleteMember() {
        Member member = securityUtil.getCurrentMember();
        memberRepository.delete(member);
    }

    @Transactional
    public TokenResponse login(LoginRequest loginRequest) {
        // --- [변경 3] username -> email ---
        // Spring Security 인증 시 email을 ID로 사용합니다.
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        TokenResponse tokenResponse = jwtProvider.generateToken(authentication);

        // --- [변경 4] findByUsernameOrThrow -> findByEmailOrThrow ---
        Member member = memberRepository.findByEmailOrThrow(authentication.getName());
        member.updateRefreshToken(tokenResponse.refreshToken());

        return tokenResponse;
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 Refresh Token 입니다.");
        }

        // findByRefreshToken은 그대로 사용 가능합니다.
        Member member = memberRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다. 다시 로그인해주세요."));

        // --- [변경 5] getUsername -> getEmail ---
        // 토큰을 생성할 주체(subject)로 email을 사용합니다.
        Authentication authentication = jwtProvider.getAuthentication(member.getEmail());
        TokenResponse tokenResponse = jwtProvider.generateToken(authentication);

        member.updateRefreshToken(tokenResponse.refreshToken());

        return tokenResponse;
    }

    @Transactional
    public void logout() {
        Member member = securityUtil.getCurrentMember();
        member.updateRefreshToken(null);
    }
}
