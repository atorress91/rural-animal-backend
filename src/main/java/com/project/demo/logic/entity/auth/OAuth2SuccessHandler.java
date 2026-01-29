package com.project.demo.logic.entity.auth;

import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${frontend.url}")
    private String frontendUrl;

    private final TokenStorage tokenStorage;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public OAuth2SuccessHandler(
            JwtService jwtService,
            UserRepository userRepository,
            TokenStorage tokenStorage,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.tokenStorage = tokenStorage;
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");

            TblUser user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found after OAuth2 login"));

            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );

            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            String refreshToken = authorizedClient.getRefreshToken() != null
                    ? authorizedClient.getRefreshToken().getTokenValue()
                    : null;
            Instant accessTokenExpiresAt = authorizedClient.getAccessToken().getExpiresAt();

            if (accessToken != null) {
                user.setGoogleAccessToken(accessToken);
                user.setGoogleRefreshToken(refreshToken);
                user.setTokenExpiration(LocalDateTime.ofInstant(accessTokenExpiresAt, ZoneId.systemDefault()));
                userRepository.save(user);
            }

            String token = jwtService.generateToken(user);
            String sessionId = tokenStorage.storeToken(token);
            String redirectUrl = buildRedirectUrl(user, sessionId);

            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            handleAuthenticationError(response, e);
        }
    }

    private String buildRedirectUrl(TblUser user, String sessionId) {
        String path = (user.getRole() != null && user.getRole().getTitle() == RoleEnum.PENDING)
                ? "/role-selection?session="
                : "/oauth/callback?session=";
        return frontendUrl + path + sessionId;
    }

    private void handleAuthenticationError(HttpServletResponse response, Exception e) throws IOException {
        response.sendRedirect(frontendUrl + "/login?error=authentication_failed");
    }
}
