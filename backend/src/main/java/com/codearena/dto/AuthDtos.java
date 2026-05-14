package com.codearena.dto;

public class AuthDtos {

    public static class SignupRequest {
        public String username;
        public String email;
        public String password;
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class AuthResponse {
        public String token;
        public Long userId;
        public String username;
        public String role;

        public AuthResponse(String token, Long userId, String username, String role) {
            this.token = token;
            this.userId = userId;
            this.username = username;
            this.role = role;
        }
    }
}
