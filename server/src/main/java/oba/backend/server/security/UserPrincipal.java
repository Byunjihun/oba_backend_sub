package oba.backend.server.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 소셜 로그인으로 받아온 사용자 정보를 Spring Security 인증 객체로 감싸는 클래스
 */
public class UserPrincipal implements OAuth2User, UserDetails {

    private final String id;
    private final String email;
    private final Map<String, Object> attributes;

    public UserPrincipal(String id, String email, Map<String, Object> attributes) {
        this.id = id;
        this.email = email;
        this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return (id != null && !id.isEmpty()) ? id : email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 권한 없음
    }

    // UserDetails 구현부
    @Override
    public String getPassword() {
        return null; // 소셜 로그인은 비밀번호 없음
    }

    @Override
    public String getUsername() {
        return email != null ? email : id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
