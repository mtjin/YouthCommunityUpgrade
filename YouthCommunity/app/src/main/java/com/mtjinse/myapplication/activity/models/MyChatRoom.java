package com.mtjinse.myapplication.activity.models;

public class MyChatRoom {
   private String chatRoom;
    private String friendNickName;
    private String friendImage;
    private String friendUid;

    public MyChatRoom() {
    }

    public MyChatRoom(String chatRoom, String friendNickName, String friendImage, String friendUid) {
        this.chatRoom = chatRoom;
        this.friendNickName = friendNickName;
        this.friendImage = friendImage;
        this.friendUid = friendUid;
    }

    public String getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(String chatRoom) {
        this.chatRoom = chatRoom;
    }

    public String getFriendNickName() {
        return friendNickName;
    }

    public void setFriendNickName(String friendNickName) {
        this.friendNickName = friendNickName;
    }

    public String getFriendImage() {
        return friendImage;
    }

    public void setFriendImage(String friendImage) {
        this.friendImage = friendImage;
    }

    public String getFriendUid() {
        return friendUid;
    }

    public void setFriendUid(String friendUid) {
        this.friendUid = friendUid;
    }
}
