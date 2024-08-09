import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server class that handles client connections and database interactions.
 */
public class Server {
    private static Connection dbConnection;
    private static List<String> validUsernames = new ArrayList<>(); // List of currently logged in and registered users
    private static Database db;
    private static final int PORT = 8000;
    private static final int THREAD_POOL_SIZE = 10; // Adjust as needed

    /**
     * Main method to start the server.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Establish database connection
        try {
            dbConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "123456");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Execute initial script to create tables
        try {
            db = new Database("jdbc:postgresql://localhost:5432/postgres", "postgres", "123456");
            db.executeInitialScript();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Insert a default admin user (alp) if not exists
        try {
            db.insertDefaultAdmin();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Start the server socket
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
            try {
                dbConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ClientHandler class that handles individual client connections.
     */
    static class ClientHandler implements Runnable {
        private Socket socket;
        private String currentUsername;

        /**
         * Constructor for ClientHandler.
         *
         * @param socket Client socket
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Method to handle client commands.
         */
        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String command;
                while ((command = in.readLine()) != null) {
                    String[] parts = command.split(":::");
                    synchronized (db) { // Ensure thread safety for database operations
                        if (requiresValidUser(parts[0]) && !isValidUser(currentUsername)) {
                            out.println("You have been removed.");
                            continue;
                        }
                        String response;
                        switch (parts[0]) {
                            case "LOGIN":
                                String username = parts[1];
                                String password = parts[2];
                                response = db.authenticate(username, password);
                                out.println(response);
                                if (response.startsWith("Authenticated")) {
                                    currentUsername = username;
                                    synchronized (validUsernames) {
                                        validUsernames.add(username);
                                    }
                                }
                                break;
                            case "LOGOUT":
                                out.println("Logged out.");
                                synchronized (validUsernames) {
                                    validUsernames.remove(currentUsername);
                                }
                                break;
                            case "INBOX":
                                String inboxUsername = parts[1];
                                response = db.readInbox(inboxUsername);
                                out.println(response);
                                break;
                            case "OUTBOX":
                                String outboxUsername = parts[1];
                                response = db.readOutbox(outboxUsername);
                                out.println(response);
                                break;
                            case "SENDMSG":
                                String sender = parts[1];
                                String receiver = parts[2];
                                String content = parts[3];
                                Message message = new Message(sender, receiver, content, new Timestamp(System.currentTimeMillis()));
                                response = db.sendMessage(message);
                                out.println(response);
                                break;
                            case "ADDUSER":
                                User newUser = new User(parts[1], parts[2], parts[3], parts[4], Date.valueOf(parts[5]), parts[6], parts[7], Boolean.parseBoolean(parts[8]));
                                response = db.createUser(newUser);
                                out.println(response);
                                break;
                            case "UPDATEUSER":
                                String usernameToUpdate = parts[1];
                                String fieldToUpdate = parts[2];
                                String newVal = parts[3];
                                response = db.updateUser(usernameToUpdate, fieldToUpdate, newVal);
                                out.println(response);
                                break;
                            case "REMOVEUSER":
                                String userToRemove = parts[1];
                                response = db.deleteUser(userToRemove);
                                out.println(response);
                                if (response.equals("User deleted successfully.")) {
                                    synchronized (validUsernames) {
                                        validUsernames.remove(userToRemove);
                                    }
                                }
                                break;
                            case "LISTUSERS":
                                response = db.listUsers();
                                out.println(response);
                                break;
                            default:
                                out.println("Unknown command.");
                        }
                    }
                }
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Check if the username is valid.
         *
         * @param username Username to check
         * @return true if valid, false otherwise
         */
        private boolean isValidUser(String username) {
            synchronized (validUsernames) {
                return validUsernames.contains(username);
            }
        }

        /**
         * Determine if a command requires a valid user.
         *
         * @param command Command to check
         * @return true if the command requires a valid user, false otherwise
         */
        private boolean requiresValidUser(String command) {
            switch (command) {
                case "INBOX":
                case "OUTBOX":
                case "SENDMSG":
                case "ADDUSER":
                case "UPDATEUSER":
                case "REMOVEUSER":
                case "LISTUSERS":
                    return true;
                default:
                    return false;
            }
        }
    }
}
