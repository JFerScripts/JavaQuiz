public class User {
    private int userID;
    private String username;
    private String password;
    private int totalPoints;

    public User(int userID, String username, String password) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.totalPoints = 0;
    }

    public boolean login(String password) {
        return this.password.equals(password);
    }

    public int getUserID() {
        return this.userID;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    public int getTotalPoints() {
        return this.totalPoints;
    }

    public void addPoints(int points) {
        this.totalPoints += points;
    }

    @Override
    public String toString() {
        return "Username: " + this.username + "\n" +
               "Total Points: " + this.totalPoints;
    }
}
