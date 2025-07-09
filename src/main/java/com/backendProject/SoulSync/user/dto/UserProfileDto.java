package com.backendProject.SoulSync.user.dto;

public class UserProfileDto {
    private String name;
    private Integer age;
    private String gender;
    private String bio;
    private String location;
    private String interests;
    private String profileImageUrl;

    public UserProfileDto() {
    }

    public UserProfileDto(String name, Integer age, String gender, String bio, String location, String interests, String profileImageUrl) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.bio = bio;
        this.location = location;
        this.interests = interests;
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

}
