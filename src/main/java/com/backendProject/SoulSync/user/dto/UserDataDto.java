package com.backendProject.SoulSync.user.dto;

public class UserDataDto
{
    private Integer id;
    private String name;
    private Integer age;
    private String gender;
    private String bio;
    private String location;
    private String interests;
    private String profileImageUrl;
    private boolean isLikedByMe;

    public UserDataDto() {
    }

    public boolean isLikedByMe() {
        return isLikedByMe;
    }

    public void setLikedByMe(boolean likedByMe) {
        isLikedByMe = likedByMe;
    }

    public UserDataDto(Integer id, String name, Integer age, String gender, String bio, String location, String interests, String profileImageUrl, boolean isLikedByMe) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.bio = bio;
        this.location = location;
        this.interests = interests;
        this.profileImageUrl = profileImageUrl;
        this.isLikedByMe = isLikedByMe;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
