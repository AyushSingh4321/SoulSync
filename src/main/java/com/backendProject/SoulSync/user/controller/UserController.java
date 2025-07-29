package com.backendProject.SoulSync.user.controller;

import com.backendProject.SoulSync.user.dto.UserDataDto;
import com.backendProject.SoulSync.user.dto.UserProfileDto;
import com.backendProject.SoulSync.user.model.UserModel;
import com.backendProject.SoulSync.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserService userService;

////    =================Controllers for User Chat data================
//    @MessageMapping("/user.addUser")
//    @SendTo("/user/topic")
//    public UserModel addUser(@Payload UserModel user)
//    {
//        userService.saveUser(user);
//        return user;
//    }
//
//    @MessageMapping("/user.disconnectUser")
//    @SendTo("/user/topic")
//    public UserModel disconnect(@Payload UserModel user)
//    {
//        userService.disconnect(user);
//        return user;
//    }
//
    @GetMapping("/users")
    public ResponseEntity<List<UserModel>> findConnectedUsers()
    {
        return ResponseEntity.ok(userService.findConnectedUsers());
    }

//    ================Controllers for other user operations==========
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
//      To fetch all the users on the main screen in the order of their compatibility
    @GetMapping("/discover")
    public ResponseEntity<?> discoverUsers() {
        try {
            return ResponseEntity.ok(userService.getBestMatchedUsers());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error discovering/fetching all users according to compatibility");
        }
    }

//    To add search functionality
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword)
    {
        System.out.println("searching with "+keyword);
        List<UserDataDto> userList=userService.searchUsers(keyword);
        return new ResponseEntity<>(userList, HttpStatus.OK);
    }


}
