import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TCPS {
    public static final int PORT = 7777;
    public static ServerSocket serverSocket = null; // Server gets found

    public static void configureServer() throws UnknownHostException, IOException {
        // get server's own IP address
        String serverIP = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Server ip: " + serverIP);

        // create a socket at the predefined port
        serverSocket = new ServerSocket(PORT);

        // Start listening and accepting requests on the serverSocket port, blocked until a request arrives
        while (true) {
            System.out.println("prior to creating new thread for client");
            /*
            final variables should in anonymous functions work like instance variables to know which socket instance
            to close for each thread
             */
            final Socket openSocket = serverSocket.accept();
            new Thread(() -> {
                try {
                    System.out.println("Server accepts requests at: " + openSocket);
                    connectClient(openSocket);
                    openSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Closed client with thread ID: " + Thread.currentThread().getId());
            }).start();
        }
    }

    public static void connectClient(Socket openSocket) {
        String request, response;

        // two I/O streams attached to the server socket:
        Scanner in;         // Scanner is the incoming stream (requests from a client)
        PrintWriter out;    // PrintWriter is the outcoming stream (the response of the server)
        try {
            in = new Scanner(openSocket.getInputStream());
            out = new PrintWriter(openSocket.getOutputStream(), true);
            // Parameter true ensures automatic flushing of the output buffer

            // Server keeps listening for request and reading data from the Client,
            // until the Client sends "stop" requests
            while (in.hasNextLine()) {
                request = in.nextLine();
                System.out.println("client with thread.getId(): " + Thread.currentThread().getId());
                if (request.equals("stop")) {
                    out.println("Good bye, client!");
                    System.out.println("Log: " + request + " client!");
                    break;
                } else {
                    // server responses
                    response = new StringBuilder(request).reverse().toString();
                    out.println(response);
                    // Log response on the server's console, too
                    System.out.println("Log: " + response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            configureServer();
        } catch (Exception e) {
            System.out.println(" Connection fails: " + e);
        } finally {
            serverSocket.close();
            System.out.println("Server port closed");
        }

    }
}
