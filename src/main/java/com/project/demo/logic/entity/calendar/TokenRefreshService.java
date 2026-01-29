package com.project.demo.logic.entity.calendar;

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.TokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
public class TokenRefreshService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private final UserRepository userRepository;

    public TokenRefreshService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Devuelve un token de acceso válido para un usuario especificado. Si el token
     * de acceso actual ha expirado, lo refresca antes de devolverlo.
     *
     * @param userId El identificador único del usuario para el que se solicita el token.
     * @return El token de acceso de Google actualizado o existente para el usuario.
     * @throws RuntimeException Si el usuario no se encuentra en el repositorio o si hay un error
     *                          al refrescar el token de acceso.
     */
    public String getValidAccessToken(Long userId) {
        TblUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (isTokenExpired(user)) {
            refreshAccessToken(user);
        }
        return user.getGoogleAccessToken();
    }

    /**
     * Verifica si el token de acceso de un usuario ha expirado.
     *
     * @param user El usuario cuyo token de acceso se está verificando.
     *             Debe contener la información de expiración del token de acceso.
     * @return true si el token de acceso ha expirado o no se ha establecido;
     * false si el token de acceso aún es válido.
     */
    private boolean isTokenExpired(TblUser user) {
        return user.getTokenExpiration() == null ||
                LocalDateTime.now().isAfter(user.getTokenExpiration());
    }

    /**
     * Refresca el token de acceso OAuth2 del usuario especificado utilizando su token de actualización
     * de Google. Actualiza el token de acceso y la fecha de expiración en el objeto del usuario.
     *
     * @param user El usuario cuyo token de acceso necesita ser refrescado.
     *             Debe contener un token de actualización válido de Google.
     * @throws RuntimeException Si ocurre un error al intentar refrescar el token de acceso.
     */
    private void refreshAccessToken(TblUser user) {
        try {
            var request = new TokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    new GenericUrl("https://oauth2.googleapis.com/token"),
                    "refresh_token");

            request.setClientAuthentication(
                    new ClientParametersAuthentication(clientId, clientSecret));
            request.put("refresh_token", user.getGoogleRefreshToken());
            request.put("grant_type", "refresh_token");

            TokenResponse response = request.execute();

            user.setGoogleAccessToken(response.getAccessToken());
            user.setTokenExpiration(
                    LocalDateTime.now().plusSeconds(response.getExpiresInSeconds()));

            userRepository.save(user);

            log.info("Token refreshed successfully for user {}. Expires at: {}",
                    user.getId(), user.getTokenExpiration());

        } catch (IOException e) {
            log.error("Error refreshing access token", e);
            throw new RuntimeException("Failed to refresh access token", e);
        }
    }
}