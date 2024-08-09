import java.io.Serializable;
import java.sql.Date;

/**
 * User class represents a user object containing details about the user such as username, password, name, etc.
 */
public class User implements Serializable {
    private String username;
    private String password;
    private String name;
    private String surname;
    private Date birthdate;
    private String gender;
    private String email;
    private boolean isAdmin;

    /**
     * Constructs a new User object.
     *
     * @param username  The username of the user.
     * @param password  The password of the user.
     * @param name      The name of the user.
     * @param surname   The surname of the user.
     * @param birthdate The birthdate of the user.
     * @param gender    The gender of the user.
     * @param email     The email of the user.
     * @param isAdmin   Indicates whether the user is an admin.
     */
    public User(String username, String password, String name, String surname, Date birthdate, String gender, String email, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.birthdate = birthdate;
        this.gender = gender;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    /**
     * Returns the username of the user.
     *
     * @return The username of the user.
     */
    public String getUsername() { return username; }

    /**
     * Returns the password of the user.
     *
     * @return The password of the user.
     */
    public String getPassword() { return password; }

    /**
     * Returns the name of the user.
     *
     * @return The name of the user.
     */
    public String getName() { return name; }

    /**
     * Returns the surname of the user.
     *
     * @return The surname of the user.
     */
    public String getSurname() { return surname; }

    /**
     * Returns the birthdate of the user.
     *
     * @return The birthdate of the user.
     */
    public Date getBirthdate() { return birthdate; }

    /**
     * Returns the gender of the user.
     *
     * @return The gender of the user.
     */
    public String getGender() { return gender; }

    /**
     * Returns the email of the user.
     *
     * @return The email of the user.
     */
    public String getEmail() { return email; }

    /**
     * Indicates whether the user is an admin.
     *
     * @return true if the user is an admin, false otherwise.
     */
    public boolean isAdmin() { return isAdmin; }

    /**
     * Returns a string representation of the User object.
     *
     * @return A string in the format "username:::password:::name:::surname:::birthdate:::gender:::email:::isAdmin".
     */
    @Override
    public String toString() {
        return username + ":::" + password + ":::" + name + ":::" + surname + ":::" + birthdate + ":::" + gender + ":::" + email + ":::" + isAdmin;
    }

}
