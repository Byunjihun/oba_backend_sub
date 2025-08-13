package oba.backend.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oba.backend.server.dto.AuthDto.LoginRequest;
import oba.backend.server.dto.AuthDto.ReissueRequest;
import oba.backend.server.dto.AuthDto.SignUpRequest;
import oba.backend.server.dto.AuthDto.TokenResponse;
import oba.backend.server.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입 API
     * @param signUpRequest 아이디, 비밀번호
     * @return 성공 메시지
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        authService.signUp(signUpRequest);
        return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
    }

    /**
     * 로그인 API
     * @param loginRequest 아이디, 비밀번호
     * @return Access Token, Refresh Token
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse = authService.login(loginRequest);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * 토큰 재발급 API
     * @param reissueRequest 기존 Refresh Token
     * @return 새로운 Access Token, Refresh Token
     */
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestBody ReissueRequest reissueRequest) {
        TokenResponse tokenResponse = authService.reissue(reissueRequest.refreshToken());
        return ResponseEntity.ok(tokenResponse);
    }
}