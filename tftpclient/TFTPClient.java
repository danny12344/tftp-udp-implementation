package tftpclient;

import common.Channel;
import common.PacketFactory;
import common.RequestParser;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * The TFTPClient class is responsible for setting up and initiating the actual
 * communication between the server and the clients
 *
 * @version 1.0 - 09/05/2020
 * @author Candidate Number: 203068
 */
public class TFTPClient {

    PacketFactory packetFactory = new PacketFactory();
    RequestParser parser = new RequestParser();
    private final int portNumServer;
    private final int portNumClient;

    /**
     * This constructor initialises the port number for both the client and the
     * sever
     *
     * @param portNumServer port number of the server
     * @param portNumClient port number of the client
     */
    public TFTPClient(int portNumServer, int portNumClient) {
        this.portNumServer = portNumServer;
        this.portNumClient = portNumClient;
    }

    /**
     * The get file method executes read requests by sending a packet created
     * with the help of the PacketFactory class
     *
     * @param remoteFileName name of the file being read
     * @param localFileName name of the file being written to the server
     */
    public void getFile(String remoteFileName, String localFileName) {

        //try with resources will automatically close socket at end of try statement
        try (DatagramSocket socket = new DatagramSocket(portNumClient)) {
            DatagramPacket packet;

            PacketFactory factory = new PacketFactory();
            byte[] rrq = factory.createRRQ(remoteFileName);

            InetAddress address = InetAddress.getByName("localhost");

            packet = new DatagramPacket(rrq, rrq.length, address, portNumServer); //creating packet to be sent

            socket.send(packet);

            Channel channel = new Channel(packet, socket, true);

            channel.receiveData(localFileName);
        } catch (IOException ex) {
            throw new IllegalStateException("No socket!");
        }

    }

    /**
     * The send file method implements write requests to the server with the
     * help of the PacketFactory class
     *
     * @param localFileName name of the file on the client-side to be written to
     * the server
     * @param remoteFileName name of the file that will be written to the server
     */
    public void sendFile(String localFileName, String remoteFileName) {

        try (DatagramSocket socket = new DatagramSocket(portNumClient)) {
            DatagramPacket packet;

            PacketFactory factory = new PacketFactory();
            byte[] wrq = factory.createWRQ(remoteFileName);

            InetAddress address = InetAddress.getByName("localhost");

            packet = new DatagramPacket(wrq, wrq.length, address, portNumServer); //creating packet to be sent

            socket.send(packet);

            Channel channel = new Channel(packet, socket, true);
            
            channel.receiveAck(0);

            channel.sendFile(localFileName);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not send file");
        }

    }

}
