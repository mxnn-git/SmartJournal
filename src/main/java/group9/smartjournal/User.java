package group9.smartjournal;

public class User {
    private String email;
    private String displayName;
    private String password;

    // Constructor to initialise the User object
    public User (String email, String displayName, String password) {
        this.email = email;
        this.displayName = displayName;
        this.password = password;
    }

    // Getters to access private fields
    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPassword() {
        return password;
    }

    // Debugging: A toString method
    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                " displayName+'" + displayName + '\'' +
                '}';
    }
}
