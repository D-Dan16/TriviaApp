package stav_gordeev.triviaapp.database;

public class User {
    public String uid;
    public String userName;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String userName) {
        this.uid = uid;
        this.userName = userName;
    }
}
