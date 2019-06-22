package com.mtjinse.myapplication.activity.models;

public class Member {
    private String uId1;
    private String uId2;

    public Member() {
    }

    public Member(String uId1, String uId2) {
        this.uId1 = uId1;
        this.uId2 = uId2;
    }

    public String getuId1() {
        return uId1;
    }

    public void setuId1(String uId1) {
        this.uId1 = uId1;
    }

    public String getuId2() {
        return uId2;
    }

    public void setuId2(String uId2) {
        this.uId2 = uId2;
    }
}
