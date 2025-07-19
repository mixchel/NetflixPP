# Java NIO Chat Application

A complete chat application with a non-blocking server using Java NIO and a GUI client using Swing. The application supports multiple chat rooms, private messaging, and user management.

## Features

- Multiple chat room support
- Private messaging between users
- Nickname management
- Non-blocking I/O server for efficient handling of multiple connections
- Graphical user interface for the client
- Command-based interaction

## Requirements

- Java 8 or higher
- Java Development Kit (JDK)
- Swing (included in JDK)

## Building and Running

### Server
1. Compile the server:
```bash
javac ChatServer.java
```

2. Run the server with a port number:
```bash
java ChatServer <port>
```
Example:
```bash
java ChatServer 8000
```

### Client
1. Compile the client:
```bash
javac ChatClient.java
```

2. Run the client, specifying server address and port:
```bash
java ChatClient <server_address> <port>
```
Example:
```bash
java ChatClient localhost 8000
```

## Client Interface

The client provides a graphical user interface with:
- A text area showing chat history
- A text input field for typing messages and commands
- Automatic scrolling to show new messages
- Window closing handling with proper server disconnection

## Available Commands

All commands start with a forward slash (/). Type commands in the client's text input field:

### 1. Set Nickname
```
/nick <nickname>
```
- Must be set before joining any room
- Nickname must be unique
- Example: `/nick robert`

### 2. Join Room
```
/join <room_name>
```
- Must set nickname before joining
- Will leave current room if already in one
- Example: `/join lobby`

### 3. Leave Room
```
/leave
```
- Leaves the current room
- No arguments needed

### 4. Send Private Message
```
/priv <nickname> <message>
```
- Sends a private message to another user
- Example: `/priv alice Hello there!`

### 5. Disconnect
```
/bye
```
- Disconnects from the server
- No arguments needed
- Also triggered when closing the client window

### 6. Regular Messages
- When in a room, type messages normally without any commands
- To send a message starting with /, use // instead
- Example: `Hello everyone!`
- Example with forward slash: `//help needed`

## Message Display Format

The client formats different types of messages for better readability:

1. Regular chat messages:
```
username: message
```

2. System messages:
- User joining: `-> username joined the room`
- User leaving: `<- username left the room`
- Nickname change: `-> oldname changed their name to newname`

3. Private messages:
```
[Private from username]: message
```

4. Error messages:
```
! Error: error_message
```

5. Command confirmation:
```
Command executed successfully
```
