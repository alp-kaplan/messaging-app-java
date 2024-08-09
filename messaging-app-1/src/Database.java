import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Database class that handles database operations.
 */
public class Database {
    private Connection dbConnection;

    /**
     * Constructor for Database.
     *
     * @param dbUrl      Database URL
     * @param dbUser     Database user
     * @param dbPassword Database password
     * @throws SQLException if a database access error occurs
     */
    public Database(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Executes the initial SQL script to create tables.
     *
     * @throws SQLException if a database access error occurs
     */
    public void executeInitialScript() throws SQLException {
        String sql = """
            -- Create users table
            CREATE TABLE if not exists users (
                id SERIAL PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(50) NOT NULL,
                name VARCHAR(50) NOT NULL,
                surname VARCHAR(50) NOT NULL,
                birthdate DATE NOT NULL,
                gender VARCHAR(10) NOT NULL,
                email VARCHAR(100) NOT NULL,
                is_admin BOOLEAN NOT NULL
            );

            -- Create messages table
            CREATE TABLE if not exists messages (
                id SERIAL PRIMARY KEY,
                sender_username VARCHAR(50),
                receiver_username VARCHAR(50),
                content TEXT NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (sender_username) REFERENCES users(username) ON DELETE SET NULL,
                FOREIGN KEY (receiver_username) REFERENCES users(username) ON DELETE SET NULL
            );
        """;

        try (Statement stmt = dbConnection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Inserts a default admin user if not exists.
     *
     * @throws SQLException if a database access error occurs
     */
    public void insertDefaultAdmin() throws SQLException {
        try (PreparedStatement stmt = dbConnection.prepareStatement(
                "INSERT INTO users (username, password, name, surname, birthdate, gender, email, is_admin) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (username) DO NOTHING")) {
            stmt.setString(1, "alp");
            stmt.setString(2, "alp");
            stmt.setString(3, "alp");
            stmt.setString(4, "kaplan");
            stmt.setDate(5, java.sql.Date.valueOf("2003-01-01"));
            stmt.setString(6, "male");
            stmt.setString(7, "alp@domain.com");
            stmt.setBoolean(8, true);  // true for admin
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Authenticates a user.
     *
     * @param username Username
     * @param password Password
     * @return Authentication response
     */
    public String authenticate(String username, String password) {
        try (PreparedStatement stmt = dbConnection.prepareStatement("SELECT password, is_admin FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                boolean isAdmin = rs.getBoolean("is_admin");
                if (storedPassword.equals(password)) {
                    return "Authenticated:::" + isAdmin;
                }
            }
            return "Authentication Failed:::false"; // Default to non-admin for failed authentication
        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occurred during authentication.";
        }
    }

    /**
     * Joins a list of strings with ":::" as a separator.
     *
     * @param list List of strings to join
     * @return A single string with elements separated by ":::"
     */
    private String joinWithSeparator(List<String> list) {
        return String.join(":::", list);
    }

    /**
     * Reads the inbox of a user.
     *
     * @param username Username
     * @return Inbox messages as a single string separated by ":::"
     */
    public String readInbox(String username) {
        try (PreparedStatement stmt = dbConnection.prepareStatement(
                "SELECT m.content, m.sender_username, m.timestamp FROM messages m WHERE m.receiver_username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            List<String> inbox = new ArrayList<>();
            while (rs.next()) {
                Message message = new Message(rs.getString("sender_username"), username, rs.getString("content"), rs.getTimestamp("timestamp"));
                inbox.add(message.toString());
            }
            return joinWithSeparator(inbox);
        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occurred while reading the inbox.";
        }
    }

    /**
     * Reads the outbox of a user.
     *
     * @param username Username
     * @return Outbox messages as a single string separated by ":::"
     */
    public String readOutbox(String username) {
        try (PreparedStatement stmt = dbConnection.prepareStatement(
                "SELECT m.content, m.receiver_username, m.timestamp FROM messages m WHERE m.sender_username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            List<String> outbox = new ArrayList<>();
            while (rs.next()) {
                Message message = new Message(username, rs.getString("receiver_username"), rs.getString("content"), rs.getTimestamp("timestamp"));
                outbox.add(message.toString());
            }
            return joinWithSeparator(outbox);
        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occurred while reading the outbox.";
        }
    }

    /**
     * Sends a message.
     *
     * @param message Message object
     * @return Send message response
     */
    public String sendMessage(Message message) {
        try {
            // Check if the receiver exists in the database
            int receiverId = getUserIdByUsername(message.getReceiver());
            if (receiverId == -1) {
                return "Error: Receiver does not exist.";
            }

            // Insert the message into the messages table
            try (PreparedStatement stmt = dbConnection.prepareStatement(
                    "INSERT INTO messages (sender_username, receiver_username, content, timestamp) VALUES (?, ?, ?, ?)")) {
                stmt.setString(1, message.getSender());
                stmt.setString(2, message.getReceiver());
                stmt.setString(3, message.getContent());
                stmt.setTimestamp(4, message.getTimestamp());
                stmt.executeUpdate();
                return "Message sent.";
            } catch (SQLException e) {
                e.printStackTrace();
                return "An error occurred while sending the message.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occurred while sending the message.";
        }
    }

    /**
     * Gets the user ID by username.
     *
     * @param username Username
     * @return User ID or -1 if not found
     * @throws SQLException if a database access error occurs
     */
    private int getUserIdByUsername(String username) throws SQLException {
        try (PreparedStatement stmt = dbConnection.prepareStatement(
                "SELECT id FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                return -1; // Return -1 if user not found
            }
        }
    }

    /**
     * Creates a new user.
     *
     * @param user User object
     * @return Create user response
     */
    public String createUser(User user) throws SQLException {
        if (getUserIdByUsername(user.getUsername()) == -1) {
            try (PreparedStatement stmt = dbConnection.prepareStatement(
                    "INSERT INTO users (username, password, name, surname, birthdate, gender, email, is_admin) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getName());
                stmt.setString(4, user.getSurname());
                stmt.setDate(5, user.getBirthdate());
                stmt.setString(6, user.getGender());
                stmt.setString(7, user.getEmail());
                stmt.setBoolean(8, user.isAdmin());
                stmt.executeUpdate();
                return "User created successfully.";
            } catch (SQLException e) {
                e.printStackTrace();
                return "An error occurred while creating the user.";
            }
        }
        else {
            return "User with the same username already exists.";
        }
    }

    /**
     * Updates a user's information.
     *
     * @param usernameToUpdate  Username of the user to update
     * @param fieldToUpdate     Field to update
     * @param newVal            New value for the field
     * @return Update user response
     */
    public String updateUser(String usernameToUpdate, String fieldToUpdate, String newVal) throws SQLException {
        // Retrieve the user ID based on the username
        int userId = getUserIdByUsername(usernameToUpdate);

        if (userId == -1) {
            return "User not found.";
        }

        // Build the SQL query string
        String sql = "UPDATE users SET " + fieldToUpdate + " = ? WHERE id = ?";

        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            // Set the parameter values
            if(fieldToUpdate.equals("birthdate")){
                stmt.setDate(1, Date.valueOf(newVal));
            }
            else if(fieldToUpdate.equals("is_admin")){
                stmt.setBoolean(1, Boolean.parseBoolean(newVal));
            }
            else {
                stmt.setString(1, newVal);
            }
            stmt.setInt(2, userId);

            // Execute the update
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                return "User update successful.";
            } else {
                return "User update failed.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occurred while updating the user.";
        }
    }

    /**
     * Deletes a user.
     *
     * @param username Username of the user to delete
     * @return Delete user response
     */
    public String deleteUser(String username) {
        try (PreparedStatement stmt = dbConnection.prepareStatement("DELETE FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                return "User deleted successfully.";
            } else {
                return "User not found.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occurred while deleting the user.";
        }
    }

    /**
     * Lists all users.
     *
     * @return List of users as a single string separated by ":::"
     */
    public String listUsers() {
        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            List<String> users = new ArrayList<>();
            while (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getDate("birthdate"),
                        rs.getString("gender"),
                        rs.getString("email"),
                        rs.getBoolean("is_admin")
                );
                users.add(user.toString());
            }
            return joinWithSeparator(users);
        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occurred while listing the users.";
        }
    }

}
