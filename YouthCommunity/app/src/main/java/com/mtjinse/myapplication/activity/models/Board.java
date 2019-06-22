package com.mtjinse.myapplication.activity.models;

public class Board {
    private String boardName;

    public Board() {
    }

    public Board(String boardName) {
        this.boardName = boardName;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }
}
