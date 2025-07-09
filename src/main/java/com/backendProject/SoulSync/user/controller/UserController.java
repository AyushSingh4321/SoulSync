package com.backendProject.SoulSync.user.controller;

import com.backendProject.SoulSync.user.dto.UserDataDto;
import com.backendProject.SoulSync.user.dto.UserProfileDto;
import com.backendProject.SoulSync.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    //    To get the current logged-in user profile
    @GetMapping("/myProfile")
    public ResponseEntity<?> getMyProfile() {
        return userService.getMyProfile();
    }

    //   To update the profile of current logged-in user
    @PostMapping("/updateUserProfile")
    public ResponseEntity<?> updateUserProfile(@RequestBody UserProfileDto userProfile) {
        return userService.updateUserProfile(userProfile);
    }

    //    To get all other users
    @GetMapping("/getAllOtherUsers")
    public ResponseEntity<?> getAllOtherUsers() {
        try {
            return ResponseEntity.ok(userService.getAllOtherUsers());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading users");
        }
    }

    //    To like other users by current user
    @PostMapping("/like/{userId}")   //userId is the id of user to be liked
    public ResponseEntity<?> toggleLike(@PathVariable("userId") Integer userId) {
        try {
            return userService.toggleLikeUser(userId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error liking user");
        }
    }

    //      To get all the users liked by the current user
    @GetMapping("/liked")
    public ResponseEntity<?> getAllLikedUsers() {
        try {
            return ResponseEntity.ok(userService.getAllLikedUsers());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting all the liked users");
        }
    }


}
