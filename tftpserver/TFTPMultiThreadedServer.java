package tftpserver;

import common.Channel;
import common.Request;
import common.RequestParser;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

/**
 * The TFTPMultiThreadedServer class is primarily responsible for keeping track of the different open threads.
 * It is also responsible for error handling and sending acknowledgements.
 * 
 * @version 1.0 - 09/05/2020
 * @author Candidate Number: 203068
 */
public class TFTPMultiThreadedServer {

    /**
     *
     */
    protected DatagramSocket socket = null;
    RequestParser parseR = new RequestParser();

    
    public TFTPMultiThreadedServer() throws SocketException {
        this("TFTPSocketServer");
    }

    /**
     * This constructor initialises this class by giving it a port number
     * 
     * @param name
     * @throws SocketException
     */
    public TFTPMultiThreadedServer(String name) throws SocketException {
        socket = new DatagramSocket(9000);
    }

    /**
     *
     * This run method contains the bulk of the logic for this class. 
     * In this method the central data structure is the Hash Map which is used to keep track of the different threads. 
     * It stores the TIDs of each thread as an instance of channel and these threads are retrieved by looking them up by their
     * corresponding keys.
     * TIDs consist of the packet's address' host name combined with the packet's port number to create an identifier that is unique for every packet
     * 
     */
    public void run() {
       
        Map<String, Channel> channels = new HashMap<>(); //storing the different threads - string is key & channel is the transfer identifier

            while (true) {
                
                byte[] recvBuf = new byte[516];     // a byte array that will store the data received by the client
                
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                
                
                
            try {
                socket.receive(packet);
            } catch (IOException ex) {
                System.out.println("Could not receive packet");
            }

                String key = String.format("%s:%s", packet.getAddress().getHostName(), packet.getPort()); //creating a unique TID

                Request request = parseR.parseRequest(recvBuf, packet.getLength());

                if (request == null) {
                 
                    new Channel(packet, socket, true).sendError(0, "Invalid packet");
                    
                    continue;
                }

                if (request.getOpCode() == 1) {

                    Channel channel = new Channel(packet, socket, false);

                    channels.put(key, channel); //storing channels in map to keep track of them 

                    
                    
                    new Thread(() -> channel.sendFile(request.getFileName())).start(); //channel send file not executed until thread started due to lambda expression

                } else if (request.getOpCode() == 2) { //WRQ

                    Channel channel = new Channel(packet, socket, false);

                    channels.put(key, channel); //storing channels
                    try {
                        channel.sendAck(0);
                        new Thread(() -> channel.receiveData(request.getFileName())).start(); //receiving file from write request sent by client
                    } catch (IOException ex) { //if sending ack throws exception it will not create thread because it wont be reached
                        System.out.println("Could not send ack for write request");
                    }
                } else {

                    if (channels.containsKey(key)) {
                        channels.get(key).receiveRequest(request);
                    } else {
                        System.err.println("Unknown client: " + key);
                    }

                }

            }
    }

}
