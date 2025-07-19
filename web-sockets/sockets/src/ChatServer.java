import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    static private final ByteBuffer buffer = ByteBuffer.allocate(16384);
    static private final Charset charset = Charset.forName("UTF8");
    static private final CharsetDecoder decoder = charset.newDecoder();
    static private final CharsetEncoder encoder = charset.newEncoder();

    static private final Map<SocketChannel, ClientInfo> clients = new ConcurrentHashMap<>();
    static private final Map<String, Set<SocketChannel>> rooms = new ConcurrentHashMap<>();
    static private final Map<SocketChannel, StringBuilder> partialMessages = new ConcurrentHashMap<>();
    static private final Set<String> userNames = ConcurrentHashMap.newKeySet();

    static class ClientInfo {
        String state = "init";  // init, outside, inside
        String name = null;
        String room = null;

        ClientInfo() {}
    }


    /**
     * Main entry point for the chat server.
     * Initializes the server socket and starts listening for client connections.
     */
    static public void main(String args[]) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java ChatServer <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        try {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ServerSocket ss = ssc.socket();
            InetSocketAddress isa = new InetSocketAddress(port);
            ss.bind(isa);
            Selector selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server listening on port " + port);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isAcceptable()) {
                        handleAccept(selector, key);
                    } else if (key.isReadable()) {
                        handleRead(selector, key);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }


    /**
     * Handles new client connection requests.
     * Sets up the client channel for non-blocking operation and initializes client data structures.
     */
    static private void handleAccept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        clients.put(clientChannel, new ClientInfo());
        partialMessages.put(clientChannel, new StringBuilder());
        System.out.println("New client connected: " + clientChannel.getRemoteAddress());
    }


    /**
     * Handles incoming data from clients.
     * Processes complete messages and maintains partial message buffers.
     */
    static private void handleRead(Selector selector, SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        buffer.clear();
        int bytesRead;

        try {
            bytesRead = clientChannel.read(buffer);
        } catch (IOException e) {
            handleClientDisconnection(clientChannel);
            return;
        }

        if (bytesRead == -1) {
            handleClientDisconnection(clientChannel);
            return;
        }

        buffer.flip();
        String received = decoder.decode(buffer).toString();
        StringBuilder messageBuilder = partialMessages.get(clientChannel);
        messageBuilder.append(received);

        // Process complete messages
        String messages = messageBuilder.toString();
        int newlineIndex;
        while ((newlineIndex = messages.indexOf('\n')) != -1) {
            String message = messages.substring(0, newlineIndex).trim();
            messages = messages.substring(newlineIndex + 1);
            processMessage(clientChannel, message);
        }
        messageBuilder.setLength(0);
        messageBuilder.append(messages);
    }


    /**
     * Processes a complete message received from a client.
     * Handles both commands (starting with /) and regular chat messages.
     */
    static private void processMessage(SocketChannel clientChannel, String message) throws IOException {
        ClientInfo client = clients.get(clientChannel);

        if (message.startsWith("/")) {
            String[] parts = message.split(" ", 2);
            String command = parts[0];
            String argument = parts.length > 1 ? parts[1] : "";

            switch (command) {
                case "/nick":
                    handleNickCommand(clientChannel, argument);
                    break;
                case "/join":
                    handleJoinCommand(clientChannel, argument);
                    break;
                case "/leave":
                    handleLeaveCommand(clientChannel);
                    break;
                case "/bye":
                    handleByeCommand(clientChannel);
                    break;
                case "/priv":
                    handlePrivateMessage(clientChannel, argument);
                    break;
                default:
                    if (client.state.equals("inside") && message.startsWith("//")) {
                        broadcastMessage(clientChannel, message.substring(1));
                    } else {
                        sendError(clientChannel, "Invalid command");
                    }
            }
        } else if (client.state.equals("inside")) {
            broadcastMessage(clientChannel, message);
        } else {
            sendError(clientChannel, "You must be in a room to send messages");
        }
    }


    /**
     * Broadcasts a message to all clients in a room.
     */
    static private void broadcastMessage(SocketChannel sender, String message) throws IOException {
        ClientInfo client = clients.get(sender);
        if (client.state.equals("inside") && client.room != null) {
            String formattedMessage = "MESSAGE " + client.name + " " + message;
            Set<SocketChannel> roomMembers = rooms.get(client.room);
            if (roomMembers != null) {
                for (SocketChannel member : roomMembers) {
                    sendToClient(member, formattedMessage);
                }
            }
        }
    }


    /**
     * Handles private messages between clients.
     * Format: /priv targetNick message
     */
    static private void handlePrivateMessage(SocketChannel sender, String message) throws IOException {
        String[] parts = message.split(" ", 2);
        if (parts.length != 2) {
            sendError(sender, "Invalid private message format. Use: /priv nickname message");
            return;
        }

        String targetNick = parts[0];
        String messageContent = parts[1];
        ClientInfo senderInfo = clients.get(sender);

        if (senderInfo.name == null) {
            sendError(sender, "You must set a nickname first");
            return;
        }

        Optional<SocketChannel> targetChannel = clients.entrySet().stream()
                .filter(e -> targetNick.equals(e.getValue().name))
                .map(Map.Entry::getKey)
                .findFirst();

        if (targetChannel.isPresent()) {
            sendToClient(targetChannel.get(), "PRIVATE " + senderInfo.name + " " + messageContent);
            sendToClient(sender, "OK");
        } else {
            sendError(sender, "User not found: " + targetNick);
        }
    }


    /**
     * Handles the /nick command for setting or changing a client's nickname.
     */
    static private void handleNickCommand(SocketChannel channel, String newNick) throws IOException {
        if (newNick.isEmpty()) {
            sendError(channel, "Nick cannot be empty");
            return;
        }

        ClientInfo client = clients.get(channel);
        if (userNames.contains(newNick) && (client.name == null || !client.name.equals(newNick))) {
            sendError(channel, "Nick already in use");
            return;
        }

        String oldNick = client.name;
        if (oldNick != null) {
            userNames.remove(oldNick);
        }

        client.name = newNick;
        userNames.add(newNick);
        sendToClient(channel, "OK");

        if (client.state.equals("init")) {
            client.state = "outside";
        }

        if (client.state.equals("inside")) {
            for (SocketChannel ch : rooms.get(client.room)) {
                if (ch != channel) {
                    sendToClient(ch, "NEWNICK " + oldNick + " " + newNick);
                }
            }
        }
    }


    /**
     * Handles the /join command for entering a chat room.
     */
    static private void handleJoinCommand(SocketChannel channel, String roomName) throws IOException {
        if (roomName.isEmpty()) {
            sendError(channel, "Room name cannot be empty");
            return;
        }

        ClientInfo client = clients.get(channel);
        if (client.name == null) {
            sendError(channel, "Must set nick first");
            return;
        }

        if (client.state.equals("inside")) {
            leaveCurrentRoom(channel);
        }

        rooms.computeIfAbsent(roomName, k -> ConcurrentHashMap.newKeySet()).add(channel);
        client.room = roomName;
        client.state = "inside";
        sendToClient(channel, "OK");

        for (SocketChannel ch : rooms.get(roomName)) {
            if (ch != channel) {
                sendToClient(ch, "JOINED " + client.name);
            }
        }
    }


    /**
     * Handles the /leave command for exiting a chat room.
     */
    static private void handleLeaveCommand(SocketChannel channel) throws IOException {
        ClientInfo client = clients.get(channel);
        if (!client.state.equals("inside")) {
            sendError(channel, "Not in a room");
            return;
        }

        leaveCurrentRoom(channel);
        client.state = "outside";
        client.room = null;
        sendToClient(channel, "OK");
    }


    /**
     * Handles the /bye command for client disconnection.
     */
    static private void handleByeCommand(SocketChannel channel) throws IOException {
        ClientInfo client = clients.get(channel);
        if (client.state.equals("inside")) {
            leaveCurrentRoom(channel);
        }
        sendToClient(channel, "BYE");
        cleanup(channel);
    }


    /**
     * Removes a client from their current room and notifies other room members.
     */
    static private void leaveCurrentRoom(SocketChannel channel) throws IOException {
        ClientInfo client = clients.get(channel);
        if (client.room != null && client.name != null) {
            Set<SocketChannel> roomMembers = rooms.get(client.room);
            if (roomMembers != null) {
                roomMembers.remove(channel);

                // Notify others in the room
                for (SocketChannel ch : roomMembers) {
                    sendToClient(ch, "LEFT " + client.name);
                }

                // Remove room if empty
                if (roomMembers.isEmpty()) {
                    rooms.remove(client.room);
                }
            }
        }
    }


    /**
     * Handles unexpected client disconnections.
     */
    static private void handleClientDisconnection(SocketChannel channel) throws IOException {
        ClientInfo client = clients.get(channel);
        if (client.state.equals("inside")) {
            leaveCurrentRoom(channel);
        }
        cleanup(channel);
    }


    /**
     * Cleans up resources associated with a disconnected client.
     */
    static private void cleanup(SocketChannel channel) throws IOException {
        ClientInfo client = clients.get(channel);
        if (client.name != null) {
            userNames.remove(client.name);
        }
        clients.remove(channel);
        partialMessages.remove(channel);
        channel.close();
    }


    /**
     * Sends a message to a specific client.
     */
    static private void sendToClient(SocketChannel channel, String message) throws IOException {
        try {
            ByteBuffer msgBuf = encoder.encode(CharBuffer.wrap(message + "\n"));
            channel.write(msgBuf);
        } catch (IOException e) {
            System.err.println("Error sending message to client: " + e.getMessage());
            handleClientDisconnection(channel);
        }
    }


    /**
     * Sends an error message to a client.
     */
    static private void sendError(SocketChannel channel, String message) throws IOException {
        sendToClient(channel, "ERROR " + message);
    }
}