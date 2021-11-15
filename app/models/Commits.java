package models;

import java.util.*;

public class Commits {
    String userName;
    int addition;
    int deletion;
    int maxAddition;
    int minAddtion;
    int avgAddtion;
    int maxDeletion;
    int minDeletion;
    int avgDeletion;
    int count;

    public Commits(String userName, int addition, int deletion) {
        this.userName = userName;
        this.addition = addition;
        this.deletion = deletion;
        this.count = 1;
    }

    public int getAddition() {
        return addition;
    }

    public void setAddition(int addition) {
        this.addition = addition;
    }

    public int getDeletion() {
        return deletion;
    }

    public void setDeletion(int deletion) {
        this.deletion = deletion;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getMaxAddition() {
        return maxAddition;
    }

    public void setMaxAddition(int maxAddition) {
        this.maxAddition = maxAddition;
    }

    public int getMinAddtion() {
        return minAddtion;
    }

    public void setMinAddtion(int minAddtion) {
        this.minAddtion = minAddtion;
    }

    public int getAvgAddtion() {
        return avgAddtion;
    }

    public void setAvgAddtion(int avgAddtion) {
        this.avgAddtion = avgAddtion;
    }

    public int getMaxDeletion() {
        return maxDeletion;
    }

    public void setMaxDeletion(int maxDeletion) {
        this.maxDeletion = maxDeletion;
    }

    public int getMinDeletion() {
        return minDeletion;
    }

    public void setMinDeletion(int minDeletion) {
        this.minDeletion = minDeletion;
    }

    public int getAvgDeletion() {
        return avgDeletion;
    }

    public void setAvgDeletion(int avgDeletion) {
        this.avgDeletion = avgDeletion;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "Commits{" +
                "userName='" + userName + '\'' +
                ", addition=" + addition +
                ", deletion=" + deletion +
                ", maxAddition=" + maxAddition +
                ", minAddtion=" + minAddtion +
                ", avgAddtion=" + avgAddtion +
                ", maxDeletion=" + maxDeletion +
                ", minDeletion=" + minDeletion +
                ", avgDeletion=" + avgDeletion +
                ", count=" + count +
                "}\n";
    }
}
