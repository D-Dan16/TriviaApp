package stav_gordeev.triviaapp.Helpers;


/**
 * Represents a user of the trivia application.
 * This class stores user-specific data such as their unique ID, username,
 * game statistics, and permission level.
 */
public class User {
    private String phoneNumber;

    private String uid;

    private String userName;
    private int highestScore;
    private int totalGamesPlayed;
    private String permission;
    // a new user is created with default values

    public User(String uid, String userName, String phoneNum) {
        this.setUid(uid);
        this.setUserName(userName);
        this.setHighestScore(0);
        this.setTotalGamesPlayed(0);
        this.setPermission("user");
        this.setPhoneNumber(phoneNum);
    }
    // empty constructor for when i fetch a user at login

    public User() {
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }

    public int getTotalGamesPlayed() {
        return totalGamesPlayed;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setTotalGamesPlayed(int totalGamesPlayed) {
        this.totalGamesPlayed = totalGamesPlayed;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
