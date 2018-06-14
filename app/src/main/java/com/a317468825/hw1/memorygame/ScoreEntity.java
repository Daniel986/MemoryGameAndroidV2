package com.a317468825.hw1.memorygame;

import android.location.Location;

import java.io.Serializable;

public class ScoreEntity implements Comparable<ScoreEntity>, Serializable{

    private String name;
    private float score;
    private Location location;

    public ScoreEntity(String name, float score, Location location) {
        this.name = name;
        this.score = score;
        this.location = location;
    }

    public ScoreEntity(String name, float score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "ScoreEntity{" +
                "name='" + name + '\'' +
                ", score=" + score +
                ", location=" + location +
                '}';
    }

    @Override
    public int compareTo(ScoreEntity compareScore) {
        /* For Ascending order*/
        return (int)(compareScore.getScore()-this.score);
    }
}
