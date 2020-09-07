import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {
    // Client needs to know server identification, <IP:port>
    private static final int serverPort = 7777;

    // buffers for the messages
    public static String message;
    private static byte[] dataIn = new byte[1024];
    private static byte[] dataOut = new byte[1024];

    // In UDP messages are encapsulated in packages and sent over sockets
    private static DatagramPacket requestPacket;
    private static DatagramPacket responsePacket;
    private static DatagramSocket clientSocket;

    public static void main(String[] args) throws IOException {
        clientSocket = new DatagramSocket();
        //runs client and server on same
        String serverIP_str = InetAddress.getLocalHost().getHostAddress();
        InetAddress serverIP = InetAddress.getByName(serverIP_str);
        System.out.println(serverIP);

        Scanner scan = new Scanner(System.in);
        System.out.println("Type message: ");

        while ((message = scan.nextLine()) != null) {
            sendRequest(serverIP);
        }
        clientSocket.close();
    }

    public static void sendRequest(InetAddress serverIP) throws IOException {
        //clientSocket = new DatagramSocket();
        dataOut = message.getBytes();
        requestPacket = new DatagramPacket(dataOut, dataOut.length, serverIP, serverPort);
        clientSocket.send(requestPacket);
        receiveResponse();


        BufferedImage img = ImageIO.read(new File("E:/intellijig projects software dev/whoisDemo/src/default.png"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        baos.flush();
        byte[] image_buffer = baos.toByteArray();
        DatagramPacket packet_image = new DatagramPacket(image_buffer, image_buffer.length, serverIP, serverPort);
        //System.out.println("image_buffer.length: " + image_buffer.length);
        clientSocket.send(packet_image);
        receive_image();
    }

    public static void receive_image() throws IOException {
        responsePacket = new DatagramPacket(dataIn, dataIn.length);
        clientSocket.receive(responsePacket);
        System.out.println("reached image sendback to client");
        byte[] data = responsePacket.getData();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        BufferedImage img = ImageIO.read(bais);


        //https://stackoverflow.com/questions/52818654/java-graphics2d-drawimage-and-clip-how-to-apply-antialiasing source
        JFrame frame = new JFrame();
        frame.add(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.translate(250, 250);
                g2d.drawImage(img, 0, 0, this);
            }
        });
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void receiveResponse() throws IOException {
        responsePacket = new DatagramPacket(dataIn, dataIn.length);
        clientSocket.receive(responsePacket);
        String message = new String(responsePacket.getData(), 0, responsePacket.getLength());
        System.out.println("Response from Server: " + message);
    }
}
