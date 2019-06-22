package com.mtjinse.myapplication.activity.models;

public class Comment {
    private String profileImage;
    private String nickName;
    private String dates;
    private String comment;
    private String uid;

    public Comment() {

    }

    public Comment(String profileImage, String nickName, String dates, String comment, String uid) {
        this.profileImage = profileImage;
        this.nickName = nickName;
        this.dates = dates;
        this.comment = comment;
        this.uid = uid;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getDates() {
        return dates;
    }

    public void setDates(String dates) {
        this.dates = dates;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
