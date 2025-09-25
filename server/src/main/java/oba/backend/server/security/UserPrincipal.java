package oba.backend.server.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class UserPrincipal implements OAuth2User, UserDetails {

    private final String id;
    private final String email;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String id,
                         String email,
                         Map<String, Object> attributes,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        // ✅ null 방지 처리 (빈 컬렉션/맵으로 초기화)
        this.attributes = (attributes == null) ? Map.of() : Map.copyOf(attributes);
        this.authorities = (authorities == null) ? List.of() : List.copyOf(authorities);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // 소셜 로그인 사용 시 패스워드 불필요
    }

    @Override
    public String getUsername() {
        return email;
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
