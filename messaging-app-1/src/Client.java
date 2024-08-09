import java.io.*;
import java.net.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Client class that handles interaction with the chat server.
 */
public class Client {
    private static boolean isAuthenticated = false;
    private static boolean isAdmin = false;
    private static String authenticatedUser = null;

    /**
     * Main method to start the client application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            String temp;
            System.out.println("Welcome to Alp's Chat App!");
            while (true) {
                if (!isAuthenticated) {
                    System.out.println("Possible commands: LOGIN, EXIT");
                } else {
                    if(isAdmin) {
                        System.out.println("Possible commands: LOGOUT, INBOX, OUTBOX, SENDMSG, ADDUSER, UPDATEUSER, REMOVEUSER, LISTUSERS, EXIT");
                    }
                    else {
                        System.out.println("Possible commands: LOGOUT, INBOX, OUTBOX, SENDMSG, EXIT");
                    }
                }

                String command = getNonEmptyInput(scanner, "Enter command:").toUpperCase();

                switch (command) {
                    case "LOGIN":
                        if (isAuthenticated) {
                            System.out.println("Already logged in. Please logout first.");
                        } else {
                            String username = getNonEmptyInput(scanner, "Enter username:");
                            String password = getNonEmptyInput(scanner, "Enter password:");
                            out.println("LOGIN:::" + username + ":::" + password);
                            String response = in.readLine();

                            String[] responseParts = response.split(":::");
                            System.out.println(responseParts[0]);
                            if(responseParts[0].equals("Authenticated")){
                                isAuthenticated = true;
                                authenticatedUser = username;
                                isAdmin = Boolean.parseBoolean(responseParts[1]);
                            }
                        }
                        break;
                    case "LOGOUT":
                        if (isAuthenticated) {
                            out.println("LOGOUT");
                            System.out.println(in.readLine());
                            isAuthenticated = false;
                            authenticatedUser = null;
                            isAdmin = false;
                        } else {
                            System.out.println("Not logged in.");
                        }
                        break;
                    case "INBOX":
                        if (isAuthenticated) {
                            out.println("INBOX:::" + authenticatedUser);
                            temp = in.readLine();
                            if (checkRemoval(temp, out, in)) break;
                            if (!temp.isEmpty()) {
                                List<Message> inbox = parseMessages(temp);
                                printMessageTableHeader();
                                inbox.forEach(Client::printMessage);
                            } else {
                                System.out.println("Inbox is empty.");
                            }
                        } else {
                            System.out.println("Please login first.");
                        }
                        break;
                    case "OUTBOX":
                        if (isAuthenticated) {
                            out.println("OUTBOX:::" + authenticatedUser);
                            temp = in.readLine();
                            if (checkRemoval(temp, out, in)) break;
                            if (!temp.isEmpty()) {
                                List<Message> outbox = parseMessages(temp);
                                printMessageTableHeader();
                                outbox.forEach(Client::printMessage);
                            } else {
                                System.out.println("Outbox is empty.");
                            }
                        } else {
                            System.out.println("Please login first.");
                        }
                        break;
                    case "SENDMSG":
                        if (isAuthenticated) {
                            String receiver = getNonEmptyInput(scanner, "Enter receiver's username:");
                            String content = getNonEmptyInput(scanner, "Enter message content:");
                            out.println("SENDMSG:::" + authenticatedUser + ":::" + receiver + ":::" + content);
                            temp = in.readLine();
                            if (checkRemoval(temp, out, in)) break;
                            System.out.println(temp);
                        } else {
                            System.out.println("Please login first.");
                        }
                        break;
                    case "ADDUSER":
                        if (isAuthenticated) {
                            if (isAdmin) {
                                String username = getNonEmptyInput(scanner, "Enter username:");
                                String password = getNonEmptyInput(scanner, "Enter password:");
                                String name = getNonEmptyInput(scanner, "Enter name:");
                                String surname = getNonEmptyInput(scanner, "Enter surname:");
                                Date birthdate = null;
                                while (birthdate == null) {
                                    System.out.println("Enter birthdate (YYYY-MM-DD):");
                                    try {
                                        birthdate = Date.valueOf(scanner.nextLine());
                                    } catch (IllegalArgumentException e) {
                                        System.out.println("Invalid date format. Please enter the date in YYYY-MM-DD format.");
                                    }
                                }
                                String gender = getNonEmptyInput(scanner, "Enter gender:");
                                String email = getNonEmptyInput(scanner, "Enter email:");
                                Boolean isAdmin = null;
                                while (isAdmin == null) {
                                    System.out.println("Is admin (true/false):");
                                    String isAdminInput = scanner.nextLine();
                                    if (isAdminInput.equalsIgnoreCase("true")) {
                                        isAdmin = true;
                                    } else if (isAdminInput.equalsIgnoreCase("false")) {
                                        isAdmin = false;
                                    } else {
                                        System.out.println("Invalid input. Please enter 'true' or 'false'.");
                                    }
                                }
                                out.println("ADDUSER:::" + username + ":::" + password + ":::" + name + ":::" + surname + ":::" + birthdate + ":::" + gender + ":::" + email + ":::" + isAdmin);
                                temp = in.readLine();
                                if (checkRemoval(temp, out, in)) break;
                                System.out.println(temp);
                            } else {
                                System.out.println("Access denied.");
                            }
                        } else {
                            System.out.println("Please login first.");
                        }
                        break;
                    case "UPDATEUSER":
                        if (isAuthenticated) {
                            if (isAdmin) {
                                String usernameToUpdate = getNonEmptyInput(scanner, "Enter the username of the user to be updated: ");
                                boolean isLegal = false;
                                String fieldToUpdate = null;
                                String newVal = null;
                                while(!isLegal){
                                    fieldToUpdate = getNonEmptyInput(scanner, "Enter the field to update (password, name, surname, birthdate, gender, email, is_admin): ");
                                    switch(fieldToUpdate){
                                        case "password", "name", "surname", "gender", "email":
                                            isLegal = true;
                                            newVal = getNonEmptyInput(scanner, "Enter the new value:");
                                            break;
                                        case "birthdate":
                                            isLegal = true;
                                            Date birthdate = null;
                                            while (birthdate == null) {
                                                System.out.println("Enter new birthdate (YYYY-MM-DD):");
                                                try {
                                                    birthdate = Date.valueOf(scanner.nextLine());
                                                } catch (IllegalArgumentException e) {
                                                    System.out.println("Invalid date format. Please enter the date in YYYY-MM-DD format.");
                                                }
                                            }
                                            newVal = String.valueOf(birthdate);
                                            break;
                                        case "is_admin":
                                            isLegal = true;
                                            newVal = null;
                                            while (newVal == null) {
                                                System.out.println("New admin status (true/false):");
                                                newVal = scanner.nextLine();
                                                if ((!newVal.equals("true")) && (!newVal.equals("false"))) {
                                                    System.out.println("Invalid input. Please enter 'true' or 'false'.");
                                                    newVal = null;
                                                }
                                            }
                                            break;
                                        default:
                                            System.out.println("Invalid field name.");
                                    }
                                }
                                out.println("UPDATEUSER:::" + usernameToUpdate + ":::" + fieldToUpdate + ":::" + newVal);
                                temp = in.readLine();
                                if (checkRemoval(temp, out, in)) break;
                                System.out.println(temp);
                            } else {
                                System.out.println("Access denied.");
                            }
                        } else {
                            System.out.println("Please login first.");
                        }
                        break;
                    case "REMOVEUSER":
                        if (isAuthenticated) {
                            if (isAdmin) {
                                String userToDelete = getNonEmptyInput(scanner, "Enter username to delete:");
                                out.println("REMOVEUSER:::" + userToDelete);
                                temp = in.readLine();
                                if (checkRemoval(temp, out, in)) break;
                                System.out.println(temp);
                            } else {
                                System.out.println("Access denied.");
                            }
                        } else {
                            System.out.println("Please login first.");
                        }
                        break;
                    case "LISTUSERS":
                        if (isAuthenticated) {
                            if (isAdmin) {
                                out.println("LISTUSERS");
                                temp = in.readLine();
                                if (checkRemoval(temp, out, in)) break;
                                if (!temp.isEmpty()) {
                                    List<User> users = parseUsers(temp);
                                    printUserTableHeader();
                                    users.forEach(Client::printUser);
                                } else {
                                    System.out.println("No users found.");
                                }
                            } else {
                                System.out.println("Access denied.");
                            }
                        } else {
                            System.out.println("Please login first.");
                        }
                        break;
                    case "EXIT":
                        if (isAuthenticated) {
                            out.println("LOGOUT");
                            System.out.println(in.readLine());
                        }
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Unknown command.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the response indicates that the user has been removed and performs the logout process if true.
     *
     * @param response The response message to check.
     * @param out      The PrintWriter to send the logout command to the server.
     * @param in       The BufferedReader to read the server's response.
     * @return         True if the user has been removed and the logout process was performed, false otherwise.
     * @throws IOException If an I/O error occurs while reading from the BufferedReader or writing to the PrintWriter.
     */
    private static boolean checkRemoval(String response, PrintWriter out, BufferedReader in) throws IOException {
        if (response.equals("You have been removed.")) {
            System.out.println(response);
            out.println("LOGOUT");
            System.out.println(in.readLine());
            isAuthenticated = false;
            authenticatedUser = null;
            isAdmin = false;
            return true;
        }
        return false;
    }

    /**
     * Prints the header for the user table.
     */
    private static void printUserTableHeader() {
        String format = "| %-15s | %-15s | %-15s | %-15s | %-10s | %-50s | %-10s |\n";
        String line = "+-----------------+-----------------+-----------------+-----------------+------------+----------------------------------------------------+------------+\n";

        System.out.print(line);
        System.out.format(format, "USERNAME", "NAME", "SURNAME", "BIRTHDATE", "GENDER", "EMAIL", "IS ADMIN?");
        System.out.print(line);
    }

    /**
     * Prints the details of a user.
     *
     * @param user User object to be printed
     */
    private static void printUser(User user) {
        String format = "| %-15s | %-15s | %-15s | %-15s | %-10s | %-50s | %-10s |\n";
        String line = "+-----------------+-----------------+-----------------+-----------------+------------+----------------------------------------------------+------------+\n";

        System.out.format(format, user.getUsername(), user.getName(), user.getSurname(), user.getBirthdate(), user.getGender(), user.getEmail(), user.isAdmin());
        System.out.print(line);
    }

    /**
     * Prints the header for the message table.
     */
    private static void printMessageTableHeader() {
        String format = "| %-15s | %-15s | %-100s | %-20s |\n";
        String line = "+-----------------+-----------------+------------------------------------------------------------------------------------------------------+----------------------+\n";

        System.out.print(line);
        System.out.format(format, "FROM", "TO", "CONTENT", "TIMESTAMP");
        System.out.print(line);
    }

    /**
     * Prints the details of a message.
     *
     * @param message Message object to be printed
     */
    private static void printMessage(Message message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String formattedTimestamp = dateFormat.format(message.getTimestamp());

        String format = "| %-15s | %-15s | %-100s | %-20s |\n";
        String line = "+-----------------+-----------------+------------------------------------------------------------------------------------------------------+----------------------+\n";

        System.out.format(format, message.getSender(), message.getReceiver(), message.getContent(), formattedTimestamp);
        System.out.print(line);
    }

    /**
     * Parses a string representation of users and returns a list of User objects.
     *
     * @param usersString String representation of users, separated by ":::"
     * @return List of User objects
     */
    private static List<User> parseUsers(String usersString) {
        List<User> users = new ArrayList<>();

        usersString += ":::";
        while (!usersString.isEmpty()) {
            String[] temp = usersString.split(":::", 9);
            users.add(new User(temp[0], temp[1], temp[2], temp[3], Date.valueOf(temp[4]), temp[5], temp[6], Boolean.parseBoolean(temp[7])));
            usersString = temp[8];
        }
        return users;
    }

    /**
     * Parses a string representation of messages and returns a list of Message objects.
     *
     * @param messagesString String representation of messages, separated by ":::"
     * @return List of Message objects
     */
    private static List<Message> parseMessages(String messagesString) {
        List<Message> messages = new ArrayList<>();

        messagesString += ":::";
        while (!messagesString.isEmpty()) {
            String[] temp = messagesString.split(":::", 5);
            messages.add(new Message(temp[0], temp[1], temp[2], Timestamp.valueOf(temp[3])));
            messagesString = temp[4];
        }
        return messages;
    }

    /**
     * Prompts the user for input until a non-empty string is entered.
     *
     * @param scanner Scanner object for reading user input
     * @param prompt  Prompt message
     * @return Non-empty string entered by the user
     */
    private static String getNonEmptyInput(Scanner scanner, String prompt) {
        String input;
        do {
            System.out.println(prompt);
            input = scanner.nextLine();
        } while (input.trim().isEmpty());
        return input;
    }
}
