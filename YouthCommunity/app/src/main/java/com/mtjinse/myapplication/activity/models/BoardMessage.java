package com.mtjinse.myapplication.activity.models;

public class BoardMessage {
    private String nickName;
    private String uId; //작성자
    private String profileImage;
    private String messageImage;
    private String message;
    private String dates;
    private String title;
    private int recommend;
    private String boardUid; //게시글 uid
    private String boardName;// 게시글 주제이름

    public BoardMessage() {
    }

    public BoardMessage(String nickName, String uId, String profileImage, String messageImage, String message, String dates, String title, int recommend) {
        this.nickName = nickName;
        this.uId = uId;
        this.profileImage = profileImage;
        this.messageImage = messageImage;
        this.message = message;
        this.dates = dates;
        this.title = title;
        this.recommend = recommend;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getMessageImage() {
        return messageImage;
    }

    public void setMessageImage(String messageImage) {
        this.messageImage = messageImage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDates() {
        return dates;
    }

    public void setDates(String dates) {
        this.dates = dates;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRecommend() {
        return recommend;
    }

    public void setRecommend(int recommend) {
        this.recommend = recommend;
    }

    public String getBoardUid() {
        return boardUid;
    }

    public void setBoardUid(String boardUid) {
        this.boardUid = boardUid;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }
}

