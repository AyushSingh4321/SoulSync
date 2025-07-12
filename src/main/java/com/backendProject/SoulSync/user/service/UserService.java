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

import java.util.*;
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
            UserProfileDto dto = new UserProfileDto();
            dto.setName(user.getName());
            dto.setAge(user.getAge());
            dto.setGender(user.getGender());
            dto.setBio(user.getBio());
            dto.setLocation(user.getLocation());
            dto.setInterests(user.getInterests());
            dto.setProfileImageUrl(user.getProfileImageUrl());
            dto.setHeight(user.getHeight());
            dto.setSports(user.getSports());
            dto.setGames(user.getGames());
            dto.setRelationshipType(user.getRelationshipType());
            dto.setGoesGym(user.getGoesGym());
            dto.setShortHair(user.getShortHair());
            dto.setWearGlasses(user.getWearGlasses());
            dto.setDrink(user.getDrink());
            dto.setSmoke(user.getSmoke());

            return ResponseEntity.ok(dto);

//            return ResponseEntity.ok(user);
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
            user.setHeight(userProfile.getHeight());
            user.setGames(userProfile.getGames());
            user.setSports(userProfile.getSports());
            user.setRelationshipType(userProfile.getRelationshipType());
            user.setGoesGym(userProfile.getGoesGym());
            user.setShortHair(userProfile.getShortHair());
            user.setWearGlasses(userProfile.getWearGlasses());
            user.setDrink(userProfile.getDrink());
            user.setSmoke(userProfile.getSmoke());
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
                        true,// because these are all liked users
                        user.getHeight(),
                        user.getSports(),
                        user.getGames(),
                        user.getRelationshipType(),
                        user.getGoesGym(),
                        user.getShortHair(),
                        user.getWearGlasses(),
                        user.getDrink(),
                        user.getSmoke()
                        ))
                .collect(Collectors.toList());
    }

    public List<UserDataDto> searchUsers(String keyword) {
        keyword=keyword.trim();
        String searchPattern = "%" + keyword.toLowerCase() + "%";
        String targetGender = null;
        try {
            UserModel currentUser = getCurrentManagedUser(); // already exists
            targetGender = getTargetGender(currentUser.getGender()); // already exists
        } catch (Exception ignored) {
            // If not logged in or something fails, fallback to original behavior
        }
        System.out.println("Final search pattern = " + searchPattern);
        return repo.searchUsers(searchPattern,targetGender);
    }


    @Transactional
    public List<UserDataDto> getBestMatchedUsers() {
        UserModel currentUser  = getCurrentManagedUser();
        Integer    currentId   = currentUser.getId();
        String     myGender    = currentUser.getGender();
        String     targetGender = getTargetGender(myGender);
        Set<UserModel> myLikes = currentUser.getLikedUsers();

        // -----------------------------------------------------------------
        // 1) No gender?  fall back to "everyone except me" (score = 0)
        // -----------------------------------------------------------------
        if (myGender == null || targetGender == null) {
            return repo.findAllExceptCurrent(currentId)
                    .stream()
                    .map(u -> new UserDataDto(
                            u.getId(), u.getName(), u.getAge(), u.getGender(), u.getBio(),
                            u.getLocation(), u.getInterests(), u.getProfileImageUrl(),
                            myLikes.contains(u),
                            u.getHeight(), u.getSports(), u.getGames(), u.getRelationshipType(),
                            u.getGoesGym(), u.getShortHair(), u.getWearGlasses(),
                            u.getDrink(), u.getSmoke(),
                            0                 // no score calculated
                    ))
                    .collect(Collectors.toList());
        }

        // -----------------------------------------------------------------
        // 2) Normal match flow
        // -----------------------------------------------------------------
        List<UserModel> candidates = repo.findAllExceptCurrent(currentId)
                .stream()
                .filter(u -> Objects.equals(
                        Optional.ofNullable(u.getGender())
                                .map(String::toLowerCase)
                                .orElse(null),
                        targetGender.toLowerCase()))
                .toList();

        return candidates.stream()
                .map(u -> {
                    int score  = calculateMatchScore(currentUser, u);
                    boolean liked = myLikes.contains(u);

                    return new UserDataDto(
                            u.getId(), u.getName(), u.getAge(), u.getGender(), u.getBio(),
                            u.getLocation(), u.getInterests(), u.getProfileImageUrl(),
                            liked,
                            u.getHeight(), u.getSports(), u.getGames(), u.getRelationshipType(),
                            u.getGoesGym(), u.getShortHair(), u.getWearGlasses(),
                            u.getDrink(), u.getSmoke(),
                            score                                // NEW
                    );
                })
                .sorted(Comparator.comparingInt(UserDataDto::getScore).reversed())
                .collect(Collectors.toList());
    }
//Helper functions and classes below
    private int calculateMatchScore(UserModel currentUser, UserModel other) {
        try {
            System.out.println("Matching: " + currentUser.getId() + " with " + other.getId());

            int score = 0;

            // Age similarity (Max 10 points)
            if (currentUser.getAge() != null && other.getAge() != null) {
                int ageDiff = Math.abs(currentUser.getAge() - other.getAge());
                score += Math.max(0, 10 - ageDiff);
            }

            // Location match (10 points)
            if (currentUser.getLocation() != null &&
                    currentUser.getLocation().equalsIgnoreCase(other.getLocation())) {
                score += 10;
            }

            // Interests overlap (5 points per shared interest)
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

            // Sports match (5 points)
            if (currentUser.getSports() != null && currentUser.getSports().equalsIgnoreCase(other.getSports())) {
                score += 5;
            }

            // Games match (5 points)
            if (currentUser.getGames() != null && currentUser.getGames().equalsIgnoreCase(other.getGames())) {
                score += 5;
            }

            // Relationship preference match (10 points)
            if (currentUser.getRelationshipType() != null &&
                    currentUser.getRelationshipType().equalsIgnoreCase(other.getRelationshipType())) {
                score += 10;
            }

            // Lifestyle/Physical features matches (3 points each)
            if (currentUser.getGoesGym() != null && other.getGoesGym() != null &&
                    currentUser.getGoesGym().equals(other.getGoesGym())) {
                score += 3;
            }

            if (currentUser.getShortHair() != null && other.getShortHair() != null &&
                    currentUser.getShortHair().equals(other.getShortHair())) {
                score += 3;
            }

            if (currentUser.getWearGlasses() != null && other.getWearGlasses() != null &&
                    currentUser.getWearGlasses().equals(other.getWearGlasses())) {
                score += 3;
            }

            if (currentUser.getDrink() != null && other.getDrink() != null &&
                    currentUser.getDrink().equals(other.getDrink())) {
                score += 3;
            }

            if (currentUser.getSmoke() != null && other.getSmoke() != null &&
                    currentUser.getSmoke().equals(other.getSmoke())) {
                score += 3;
            }

            // Height similarity (max 5 points if difference <= 10 cm)
            if (currentUser.getHeight() != null && other.getHeight() != null) {
                double heightDiff = Math.abs(currentUser.getHeight() - other.getHeight());
                if (heightDiff <= 10) score += 5;
            }

            // Mutual like (15 points)
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





//    private static class ScoredUser {
//        UserDataDto user;
//        int score;
//
//        public ScoredUser(UserDataDto user, int score) {
//            this.user = user;
//            this.score = score;
//        }
//    }
}
