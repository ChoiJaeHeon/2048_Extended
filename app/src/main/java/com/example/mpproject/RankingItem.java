package com.example.mpproject;

public class RankingItem {

    private int id;
    private String playerName;
    private int gameMode;
    private int score;
    private String createdAt;

    public RankingItem(int id, String playerName, int gameMode, int score, String createdAt) {
        this.id = id;
        this.playerName = playerName;
        this.gameMode = gameMode;
        this.score = score;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getGameMode() {
        return gameMode;
    }

    public int getScore() {
        return score;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}