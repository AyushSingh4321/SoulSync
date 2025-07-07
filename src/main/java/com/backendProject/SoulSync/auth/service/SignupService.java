package com.backendProject.SoulSync.auth.service;

import com.backendProject.SoulSync.auth.dto.SignupRequestDto;
import com.backendProject.SoulSync.user.model.UserModel;
import com.backendProject.SoulSync.user.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignupService
{
    @Autowired
    private UserRepo repo;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registerUserAndReturnToken(SignupRequestDto dto) {
        try {
            UserModel user = new UserModel();
            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            repo.save(user);
            return jwtService.generateToken(user.getEmail());

        } catch (Exception e) {
            return "Error generating token due to : " + e.getMessage();
        }
    }

}
