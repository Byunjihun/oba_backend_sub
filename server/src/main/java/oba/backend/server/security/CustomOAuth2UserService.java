package oba.backend.server.security;

import lombok.RequiredArgsConstructor;
import oba.backend.server.domain.user.ProviderInfo;
import oba.backend.server.domain.user.Role;
import oba.backend.server.domain.user.User;
import oba.backend.server.domain.user.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // google, kakao, naver
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 각 소셜별 정보 파싱
        String id;
        String email;
        String name;
        String picture;

        if ("google".equals(registrationId)) {
            id = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            picture = (String) attributes.get("picture");
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            id = String.valueOf(attributes.get("id"));
            email = (String) kakaoAccount.get("email");
            name = (String) profile.get("nickname");
            picture = (String) profile.get("profile_image_url");
        } else if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");

            id = (String) response.get("id");
            email = (String) response.get("email");
            name = (String) response.get("name");
            picture = (String) response.get("profile_image");
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 로그인 타입: " + registrationId);
        }

        // DB 저장 (신규 유저면 insert, 있으면 update)
        User user = userRepository.findByIdentifier(id)
                .orElse(User.builder()
                        .identifier(id)
                        .provider(ProviderInfo.valueOf(registrationId.toUpperCase()))
                        .role(Role.USER)
                        .build());

        user.updateInfo(email, name, picture);

        userRepository.save(user);

        return new UserPrincipal(id, email, attributes);
    }
}
