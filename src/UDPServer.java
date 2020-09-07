import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {
    private static final int serverPort = 7777;

    // buffers for the messages and size for image
    private static byte[] dataIn = new byte[1024];
    private static byte[] dataOut = new byte[1024];

    // In UDP messages are encapsulated in packages and sent over sockets
    private static DatagramPacket requestPacket;
    private static DatagramPacket responsePacket;
    private static DatagramSocket serverSocket;

    public static void main(String[] args) throws Exception {
        String messageIn, messageOut;
        try {
            String serverIP = InetAddress.getLocalHost().getHostAddress();
            // Opens socket for accepting requests
            serverSocket = new DatagramSocket(serverPort);
            while (true) {
                System.out.println("Server " + serverIP + " running ...");
                messageIn = receiveRequest();
                if (messageIn.equals("stop")) break;
                messageOut = processRequest(messageIn);
                sendResponse_message(messageOut);

                BufferedImage bufferedImage = receiveRequest_image();
                sendResponse_image(bufferedImage);
            }
        } catch (Exception e) {
            System.out.println(" Connection fails: " + e);
        } finally {
            serverSocket.close();
            System.out.println("Server port closed");
        }
    }

    public static BufferedImage receiveRequest_image() {
        requestPacket = new DatagramPacket(dataIn, dataIn.length);
        BufferedImage img = null;
        try {
            serverSocket.receive(requestPacket);
            byte[] data = requestPacket.getData();
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            img = ImageIO.read(bais);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    public static String receiveRequest() throws IOException {
        requestPacket = new DatagramPacket(dataIn, dataIn.length);
        serverSocket.receive(requestPacket);
        String message = new String(requestPacket.getData(), 0, requestPacket.getLength());
        System.out.println("Request: " + message);
        return message;
    }

    public static String processRequest(String message) {
        return message.toUpperCase();
    }

    public static void sendResponse_message(String message) throws IOException {
        InetAddress clientIP;
        int clientPort;
        clientIP = requestPacket.getAddress();
        clientPort = requestPacket.getPort();

        dataOut = message.getBytes();
        responsePacket = new DatagramPacket(dataOut, dataOut.length, clientIP, clientPort);
        serverSocket.send(responsePacket);
        System.out.println("Message sent back " + message);
    }

    public static void sendResponse_image(BufferedImage bufferedImage) throws IOException {
        InetAddress clientIP;
        int clientPort;
        clientIP = requestPacket.getAddress();
        clientPort = requestPacket.getPort();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        baos.flush();
        byte[] image_buffer = baos.toByteArray();
        DatagramPacket packet_image = new DatagramPacket(image_buffer, image_buffer.length, clientIP, clientPort);
        serverSocket.send(packet_image);
        System.out.println("image sent back");
    }
}
