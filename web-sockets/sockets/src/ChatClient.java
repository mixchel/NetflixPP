import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected = false;
    private Thread receiverThread;
    JFrame frame = new JFrame("Chat Client");
    private JTextField chatBox = new JTextField();
    private JTextArea chatArea = new JTextArea();


    /**
     * Construtor for the ChatClient class. Establishes a connection to the server
     */
    public ChatClient(String server, int port) throws IOException {
        try {
            socket = new Socket(server, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            connected = true;
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
            throw e;
        }

        setupUI();
    }


    /**
     * Initializes the GUI components and sets up event listeners.
     * Creates a window with a text area for chat history and a text field for
     * message input. Handles window events and message sending.
     */
    private void setupUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chatBox);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        chatArea.setEditable(false);
        chatBox.setEditable(true);
        frame.setSize(500, 300);

        chatBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = chatBox.getText();
                if (!message.isEmpty()) {
                    try {
                        newMessage(message);
                    } catch (IOException ex) {
                        printMessage("Error sending message: " + ex.getMessage() + "\n");
                    }
                    chatBox.setText("");
                }
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                chatBox.requestFocusInWindow();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (connected) {
                    try {
                        out.println("/bye");
                        socket.close();
                    } catch (IOException ex) {
                        System.out.println("Error closing connection: " + ex.getMessage());
                    }
                }
            }
        });

        frame.setVisible(true);
    }


    /**
     * Starts the client operation by initiating a thread to receive messages from the server by a wile loop.
     * This method handles the continuous reception of messages and processes them accordingly.
     */
    public void run() throws IOException {
        System.out.println("Starting client...");

        receiverThread = new Thread(() -> {
            try {
                String message;
                while (connected && (message = in.readLine()) != null) {
                    System.out.println("Received from server: " + message);
                    processServerMessage(message);
                }
            } catch (IOException e) {
                if (connected) {
                    printMessage("Lost connection to server: " + e.getMessage() + "\n");
                }
            } finally {
                connected = false;
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }
            }
        });
        receiverThread.start();
    }


    /**
     * Sends a new message to the server, handling special cases for commands.
     * Messages starting with '/' are treated as commands, while '//' is escaped to send
     * a message starting with '/'.
     */
    private void newMessage(String message) throws IOException {
        if (message.startsWith("/")) {
            // Handle commands normally
            out.println(message);
        } else if (message.startsWith("//")) {
            // Message starts with /, needs escaping
            out.println("/" + message);
        } else {
            // Regular message
            out.println(message);
        }
    }


    /**
     * Formats server messages for display in the chat window.
     */
    private String formatServerMessage(String message) {
        String[] parts = message.split(" ", 3);
        String type = parts[0];

        switch (type) {
            case "MESSAGE":
                if (parts.length >= 3) {
                    return parts[1] + ": " + parts[2];
                }
                break;
            case "NEWNICK":
                if (parts.length >= 3) {
                    return "-> " + parts[1] + " changed their name to " + parts[2];
                }
                break;
            case "JOINED":
                if (parts.length >= 2) {
                    return "-> " + parts[1] + " joined the room";
                }
                break;
            case "LEFT":
                if (parts.length >= 2) {
                    return "<- " + parts[1] + " left the room";
                }
                break;
            case "PRIVATE":
                if (parts.length >= 3) {
                    return "[Private from " + parts[1] + "]: " + parts[2];
                }
                break;
            case "ERROR":
                return "! Error: " + (parts.length > 1 ? message.substring(6) : "Unknown error");
            case "OK":
                return "Command executed successfully";
            case "BYE":
                return "Disconnected from server";
        }
        return message;
    }


    /**
     * Processes messages received from the server.
     * Formats the message for display and handles special cases like disconnection.
     */
    private void processServerMessage(String message) {
        String formattedMessage = formatServerMessage(message);
        if (formattedMessage != null) {
            printMessage(formattedMessage + "\n");
        }

        if (message.equals("BYE")) {
            connected = false;
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
            frame.dispose();
        }
    }


    /**
     * Adds a message to the chat display area.
     * Ensures thread safety when updating the GUI and automatically scrolls
     * to show the latest message.
     */
    public void printMessage(final String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message);
            // Auto-scroll to bottom
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }


    /**
     * Initial point of the ChatClient.
     * Validates command line arguments and starts the client.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java ChatClient <server> <port>");
            System.exit(1);
        }

        ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));
        client.run();
    }
}