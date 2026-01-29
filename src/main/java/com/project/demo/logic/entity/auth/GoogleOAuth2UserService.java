package com.project.demo.logic.entity.auth;

import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.role.TblRoleRepository;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TblRoleRepository roleRepository;

    public GoogleOAuth2UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TblRoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");

        userRepository.findByEmail(email).orElseGet(() -> createNewUser(oAuth2User, email));

        return oAuth2User;
    }

    private TblUser createNewUser(OAuth2User oAuth2User, String email) {

        TblRole pendingRole = roleRepository.findByTitle(RoleEnum.PENDING)
                .orElseThrow(() -> new RuntimeException("PENDING role not found in the database"));

        TblUser newUser = new TblUser();
        newUser.setEmail(email);
        newUser.setName(oAuth2User.getAttribute("given_name"));
        newUser.setLastName1(oAuth2User.getAttribute("family_name"));
        newUser.setPassword(passwordEncoder.encode(generateRandomSecurePassword()));
        newUser.setRole(pendingRole);
        newUser.setIdentification("GOOGLE_" + UUID.randomUUID().toString().substring(0, 8));
        newUser.setPhoneNumber("000-000-0000");
        newUser.setBirthDate(LocalDate.of(2000, 1, 1));
        newUser.setState("Active");

        userRepository.save(newUser);

        return newUser;
    }

    private String generateRandomSecurePassword() {
        return UUID.randomUUID().toString();
    }
}

