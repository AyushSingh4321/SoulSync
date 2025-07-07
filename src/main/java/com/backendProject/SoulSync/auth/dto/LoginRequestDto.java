package com.backendProject.SoulSync.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDto
{
    @NotBlank(message = "Identifier is required (email or username)")
    private String identifier; // Can be either email or username

    @NotBlank(message = "Password is required")
    private String password;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
