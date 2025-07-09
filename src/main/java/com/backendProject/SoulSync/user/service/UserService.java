package com.backendProject.SoulSync.user.service;

import com.backendProject.SoulSync.auth.dto.SignupRequestDto;
import com.backendProject.SoulSync.auth.service.JwtService;
import com.backendProject.SoulSync.user.dto.UserDataDto;
import com.backendProject.SoulSync.user.dto.UserProfileDto;
import com.backendProject.SoulSync.user.model.UserModel;
import com.backendProject.SoulSync.user.repo.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    UserRepo repo;

    //Used in this class only
    private UserModel getCurrentManagedUser() {
        UserModel authUser = (UserModel) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return repo.findById(authUser.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    public ResponseEntity<?> getMyProfile() {
        try {
            UserModel user = (UserModel) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching my profile data");
        }
    }

    public ResponseEntity<?> updateUserProfile(UserProfileDto userProfile) {
        try {
            UserModel user = (UserModel) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            user.setName(userProfile.getName());
            user.setAge(userProfile.getAge());
            user.setGender(userProfile.getGender());
            user.setBio(userProfile.getBio());
            user.setInterests(userProfile.getInterests());
            user.setLocation(userProfile.getLocation());
            user.setProfileImageUrl(userProfile.getProfileImageUrl());
            repo.save(user);
            return ResponseEntity.ok().body("User profile updated");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating user profile");
        }
    }

    @Transactional
    public List<UserDataDto> getAllOtherUsers() {
        UserModel currentUser = getCurrentManagedUser();
        Integer currentUserId = currentUser.getId();

        List<UserModel> otherUsers = repo.findAllExceptCurrent(currentUserId);
        Set<UserModel> likedUsers = currentUser.getLikedUsers();
        return otherUsers.stream()
                .map(user -> new UserDataDto(
                        user.getId(),
                        user.getName(),
                        user.getAge(),
                        user.getGender(),
                        user.getBio(),
                        user.getLocation(),
                        user.getInterests(),
                        user.getProfileImageUrl(),
                        likedUsers.contains(user)
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public ResponseEntity<String> toggleLikeUser(Integer likedUserId) {
        UserModel currentUser = getCurrentManagedUser();

        if (currentUser.getId().equals(likedUserId)) {
            return ResponseEntity.badRequest().body("You cannot like yourself");
        }

        Optional<UserModel> likedUserOpt = repo.findById(likedUserId);
        if (likedUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User to like not found");
        }

        UserModel likedUser = likedUserOpt.get();

        if (currentUser.getLikedUsers().contains(likedUser)) {
            currentUser.getLikedUsers().remove(likedUser);
            repo.save(currentUser);
            return ResponseEntity.ok("User unliked");
        } else {
            currentUser.getLikedUsers().add(likedUser);
            repo.save(currentUser);
            return ResponseEntity.ok("User liked");
        }
    }

    @Transactional
    public List<UserDataDto> getAllLikedUsers() {
        UserModel currentUser = getCurrentManagedUser(); // Re-fetch for lazy loading

        Set<UserModel> likedUsers = currentUser.getLikedUsers();

        return likedUsers.stream()
                .map(user -> new UserDataDto(
                        user.getId(),
                        user.getName(),
                        user.getAge(),
                        user.getGender(),
                        user.getBio(),
                        user.getLocation(),
                        user.getInterests(),
                        user.getProfileImageUrl(),
                        true // because these are all liked users
                ))
                .collect(Collectors.toList());
    }


}
