package com.backendProject.SoulSync.user.service;

import com.backendProject.SoulSync.auth.dto.SignupRequestDto;
import com.backendProject.SoulSync.auth.service.JwtService;
import com.backendProject.SoulSync.user.model.UserModel;
import com.backendProject.SoulSync.user.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {


}
