package com.mtjinse.myapplication.activity.models;

public class ChatMessage {
    private String uId;
    private String nickName;
    private String Image;
    private String message;
    private String dates;
    private String sendImage; //전송한 사진
    private String messageUid; //메시지 uid
    private String chatRoomUid; //채팅방 uid

    public ChatMessage() {
    }

    public ChatMessage(String uId, String nickName, String image, String message, String dates, String sendImage) {
        this.uId = uId;
        this.nickName = nickName;
        this.Image = image;
        this.message = message;
        this.dates = dates;
        this.sendImage = sendImage;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
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

    public String getSendImage() {
        return sendImage;
    }

    public void setSendImage(String sendImage) {
        this.sendImage = sendImage;
    }

    public String getMessageUid() {
        return messageUid;
    }

    public void setMessageUid(String messageUid) {
        this.messageUid = messageUid;
    }

    public String getChatRoomUid() {
        return chatRoomUid;
    }

    public void setChatRoomUid(String chatRoomUid) {
        this.chatRoomUid = chatRoomUid;
    }
}
