package com.project.demo.logic.entity.user;

public class LoginResponse {
    private String token;
    private TblUser authUser;
    private long expiresIn;
    private String message; // Campo para mensajes de error o éxito

    // Constructor vacío
    public LoginResponse() {}

    // Constructor para respuesta exitosa
    public LoginResponse(String token, TblUser authUser, long expiresIn) {
        this.token = token;
        this.authUser = authUser;
        this.expiresIn = expiresIn;
        this.message = "Login successful"; // Mensaje por defecto
    }

    // Constructor para mensajes de error
    public LoginResponse(String message) {
        this.message = message;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public TblUser getAuthUser() {
        return authUser;
    }

    public void setAuthUser(TblUser authUser) {
        this.authUser = authUser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
