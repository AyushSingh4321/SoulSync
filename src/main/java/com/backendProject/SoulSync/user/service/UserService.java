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

import java.util.Arrays;
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

    @Transactional
    public List<UserDataDto> getBestMatchedUsers() {
        UserModel currentUser = getCurrentManagedUser();
        Integer currentUserId = currentUser.getId();

        String currentGender = currentUser.getGender();
        String targetGender = getTargetGender(currentGender);

        List<UserModel> others = repo.findAllExceptCurrent(currentUserId).stream()
                .filter(user -> user.getGender() != null && user.getGender().equalsIgnoreCase(targetGender))
                .toList();

        Set<UserModel> myLikes = currentUser.getLikedUsers();

        return others.stream()
                .map(other -> {
                    int score = calculateMatchScore(currentUser, other);
                    boolean liked = myLikes.contains(other);

                    return new ScoredUser(
                            new UserDataDto(
                                    other.getId(),
                                    other.getName(),
                                    other.getAge(),
                                    other.getGender(),
                                    other.getBio(),
                                    other.getLocation(),
                                    other.getInterests(),
                                    other.getProfileImageUrl(),
                                    liked
                            ),
                            score
                    );
                })
                .sorted((a, b) -> Integer.compare(b.score, a.score)) // Descending
                .map(s -> s.user)
                .collect(Collectors.toList());
    }


    private int calculateMatchScore(UserModel currentUser, UserModel other) {
        try {
            System.out.println("Matching: " + currentUser.getId() + " with " + other.getId());

            int score = 0;

            // Age similarity
            if (currentUser.getAge() != null && other.getAge() != null) {
                int ageDiff = Math.abs(currentUser.getAge() - other.getAge());
                score += Math.max(0, 10 - ageDiff);
            }

            // Location
            if (currentUser.getLocation() != null &&
                    currentUser.getLocation().equalsIgnoreCase(other.getLocation())) {
                score += 10;
            }

            // Interests
            if (currentUser.getInterests() != null && other.getInterests() != null) {
                Set<String> interests1 = Arrays.stream(currentUser.getInterests().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());

                Set<String> interests2 = Arrays.stream(other.getInterests().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());

                interests1.retainAll(interests2);
                score += interests1.size() * 5;
            }

            // Mutual Like check
            if (repo.hasLikedBack(other.getId(), currentUser.getId())) {
                score += 15;
            }

            return score;
        } catch (Exception e) {
            System.out.println("ERROR in calculateMatchScore: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private String getTargetGender(String gender) {
        if (gender == null) return null;

        return switch (gender.toLowerCase()) {
            case "male" -> "female";
            case "female" -> "male";
            default -> null; // Add more cases if you support other genders
        };
    }


    private static class ScoredUser {
        UserDataDto user;
        int score;

        public ScoredUser(UserDataDto user, int score) {
            this.user = user;
            this.score = score;
        }
    }
}
