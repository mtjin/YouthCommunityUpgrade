package com.mtjinse.myapplication.activity.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Profile {
    private String nickName;
    private String profileImage;
    private String email;
    private String age;
    private String introduce;
    private String uId;

    public Profile() {
    }

    public Profile(String nickName, String profileImage, String email, String age, String introduce, String uId) {
        this.nickName = nickName;
        this.profileImage = profileImage;
        this.email = email;
        this.age = age;
        this.introduce = introduce;
        this.uId = uId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("nickName", nickName);
        result.put("profileImage", profileImage);
        result.put("email", email);
        result.put("age", age);
        result.put("introduce", introduce);
        result.put("uId", uId);
        return result;
    }
}
