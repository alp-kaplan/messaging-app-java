# Java Messaging Application

A client-server messaging application built in Java with PostgreSQL database integration. This application supports real-time messaging, user management, and role-based access control.

## 🚀 Features

- **Real-time Messaging**: Send and receive messages between users
- **User Authentication**: Secure login system with password verification
- **Role-based Access Control**: Admin and regular user privileges
- **User Management**: Admins can create, update, and delete users
- **Message History**: View inbox and outbox messages
- **Multi-threaded Server**: Supports multiple concurrent client connections
- **Database Persistence**: PostgreSQL database for reliable data storage

## 🏗️ Architecture

### Components

- **Server.java**: Multi-threaded server handling client connections and database operations
- **Client.java**: Command-line client providing user interface
- **Database.java**: Database abstraction layer for PostgreSQL operations
- **Message.java**: Data model for message objects
- **User.java**: Data model for user objects

### Database Schema

```sql
-- Users table
CREATE TABLE users (
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

-- Messages table
CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    sender_username VARCHAR(50),
    receiver_username VARCHAR(50),
    content TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_username) REFERENCES users(username) ON DELETE SET NULL,
    FOREIGN KEY (receiver_username) REFERENCES users(username) ON DELETE SET NULL
);
```

## 📋 Prerequisites

- **Java Development Kit (JDK) 8 or higher**
- **PostgreSQL Database** (version 10 or higher)
- **PostgreSQL JDBC Driver** (included in project dependencies)

## 🛠️ Setup Instructions

### 1. Database Setup

1. Install PostgreSQL on your system
2. Create a database named `postgres` (or modify connection settings in code)
3. Create a user with username `postgres` and password `123456` (or modify in `Server.java` line 27)
4. Ensure PostgreSQL is running on `localhost:5432`

### 2. Project Setup

1. Clone or download the project
2. Navigate to the project directory:
   ```bash
   cd messaging-app-java-main/messaging-app-1
   ```

3. Compile the Java files:
   ```bash
   javac -cp ".:postgresql-42.x.x.jar" src/*.java
   ```
   *Note: Replace `postgresql-42.x.x.jar` with your actual PostgreSQL JDBC driver version*

### 3. Running the Application

#### Start the Server

```bash
cd src
java -cp ".:../postgresql-42.x.x.jar" Server
```

The server will:
- Connect to the PostgreSQL database
- Create necessary tables automatically
- Create a default admin user (username: `alp`, password: `alp`)
- Start listening on port `8000`

#### Start the Client

In a new terminal window:

```bash
cd src
java -cp ".:../postgresql-42.x.x.jar" Client
```

## 🎮 Usage

### Authentication

1. **Login**: Use the `LOGIN` command with your username and password
2. **Default Admin**: Username: `alp`, Password: `alp`

### Available Commands

#### For All Users:
- `LOGIN` - Authenticate with username and password
- `LOGOUT` - Log out from the current session
- `INBOX` - View received messages
- `OUTBOX` - View sent messages
- `SENDMSG` - Send a message to another user
- `EXIT` - Close the client application

#### For Admin Users Only:
- `ADDUSER` - Create a new user account
- `UPDATEUSER` - Modify existing user information
- `REMOVEUSER` - Delete a user account
- `LISTUSERS` - View all registered users

### Example Session

```
Welcome to Alp's Chat App!
Possible commands: LOGIN, EXIT
Enter command: LOGIN
Enter username: alp
Enter password: alp
Authenticated

Possible commands: LOGOUT, INBOX, OUTBOX, SENDMSG, ADDUSER, UPDATEUSER, REMOVEUSER, LISTUSERS, EXIT
Enter command: SENDMSG
Enter receiver's username: john
Enter message content: Hello, how are you?
Message sent.
```

## 🔧 Configuration

### Database Configuration

To modify database connection settings, edit the following in `Server.java`:

```java
// Line 27
dbConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "123456");

// Line 35
db = new Database("jdbc:postgresql://localhost:5432/postgres", "postgres", "123456");
```

### Server Configuration

- **Port**: Default port is `8000` (modify `PORT` constant in `Server.java`)
- **Thread Pool Size**: Default is `10` concurrent connections (modify `THREAD_POOL_SIZE` in `Server.java`)

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Ensure PostgreSQL is running
   - Verify database credentials
   - Check if the database exists

2. **Port Already in Use**
   - Change the port number in `Server.java`
   - Or stop any process using port 8000

3. **Compilation Errors**
   - Ensure you have the correct PostgreSQL JDBC driver
   - Verify Java version compatibility
   - Check classpath includes the JDBC driver

4. **User Removal During Session**
   - If an admin removes a user while they're logged in, the user will be automatically logged out

## 🏃‍♂️ Development

### Project Structure

```
messaging-app-1/
├── src/
│   ├── Server.java      # Multi-threaded server
│   ├── Client.java      # Command-line client
│   ├── Database.java    # Database operations
│   ├── Message.java     # Message data model
│   └── User.java        # User data model
├── hw1.iml             # IntelliJ IDEA module file
└── out/                # Compiled classes (auto-generated)
```

### Key Design Patterns

- **Client-Server Architecture**: Separation of client interface and server logic
- **Thread Pool Pattern**: Efficient handling of multiple client connections
- **Data Access Object (DAO)**: Database class encapsulates all database operations
- **Command Pattern**: Server processes commands sent by clients
