
package common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The Channel class represents a channel of information through which data flows.
 * This class holds all the methods to do with sending and receiving data 
 * (including acknowledgements and initial requests)
 * 
 * 
 * @version 1.0 - 09/05/2020
 * @author Candidate Number: 203068
 */
public class Channel {

    private final DatagramPacket packet;
    private final DatagramSocket socket;
    private final RequestParser parser = new RequestParser();
    private final PacketFactory factory = new PacketFactory();
    private final boolean sync;
    private BlockingQueue<Request> requests = new LinkedBlockingQueue<>(); //if empty when you try to get someting from blocking queue it will wait until there is something in it
    private static final int TIMEOUT_SECONDS = 1;

    /**
     *This constructor initialises the class variables required
     * 
     * @param packet incoming packet that will generally be passed to one of the below methods
     * @param socket socket to be used on the packet
     * @param sync if true channel will receive data directly. If false channel will receive data from the blocking queue
     */
    public Channel(DatagramPacket packet, DatagramSocket socket, boolean sync) {
        this.packet = packet;
        this.socket = socket;
        this.sync = sync;

    }

    /**
     * Receives acknowledgements and will timeout after 1 second if no ack has been received.
     * Will display an error message if the ack is not received on time or if there is an incorrect opcode.
     * 
     * @param blockNum expected block number of the incoming ack that is checked against the actual block number coming in to ensure they match
     * @throws IOException if an I/O error occurs
     */
    public void receiveAck(int blockNum) throws IOException {

        Request request;

        if (sync) {
            socket.setSoTimeout(TIMEOUT_SECONDS * 1000);
            socket.receive(packet);
            request = parser.parseRequest(packet.getData(), packet.getLength());
        } else {
            try {
                request = requests.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                throw new IllegalStateException("Did not receive ack on time"); //unexpected messages should not crash server

            }
        }

        if (request == null || request.getOpCode() != 4) {

            throw new IllegalStateException("Unexpected response");

        } else if (request.getBlockNum() != blockNum) {
            throw new IllegalStateException("Unexpected response");
        }

        System.out.println("acknowledgement received");

    }

    /**
     * Sends acknowledgements
     * 
     * @param blockNum block number to be attached to the acknowledgement being sent
     * @throws IOException if I/O error occurs
     */
    public void sendAck(int blockNum) throws IOException {
        packet.setData(factory.createAck(blockNum));

        socket.send(packet);
    }

    /**
     * Sends error packets
     * If there is an error with sending this error it is caught and a message is displayed in console.
     * @param errorCode the error code to be assigned to this packet (see RFC for details of error code meanings)
     * @param errorMessage message to be displayed as a result of this error
     */
    public void sendError(int errorCode, String errorMessage) {

        packet.setData(factory.createERROR((byte) errorCode, errorMessage));

        try {
            socket.send(packet);
        } catch (IOException ex) {
            System.out.println("Could not send error!");
        }

    }

    /**
     * Sends data packets
     * 
     * 
     * @param blockNum block number to be associated with this data packet
     * @param buf data the packet is to carry in the form of a byte array
     * @param size size of packet
     * @throws IOException if I/O error occurs
     */
    public void sendData(int blockNum, byte[] buf, int size) throws IOException {

        packet.setData(factory.createData(blockNum, buf, size));

        socket.send(packet);

    }

    /**
     * Receives incoming data from a specified file name.
     * Using file output stream to read data from files. If sync is true the channel will receive data directly.
     * If sync if false the channel will use multiple threads. Sync is only true on the client program. 
     * 
     * 
     * @param fileName file name of the file from which data is to be received
     */
    public void receiveData(String fileName) {
        try (FileOutputStream ostream = new FileOutputStream(fileName)) {

            while (true) {

                Request request;

                if (sync) {
                    packet.setData(new byte[516]); //emptying out packet after each iteration

                    socket.setSoTimeout(TIMEOUT_SECONDS * 1000);
                    socket.receive(packet); //receiving the incoming data

                    request = parser.parseRequest(packet.getData(), packet.getLength());
                } else {
                    try {
                        request = requests.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS); // getting request from the queue & times out after 1 second
                    } catch (InterruptedException ex) {
                        System.out.println("Did not receive packet on time!");
                        break; //abort receiving data
                    }
                }
                
                if(request == null){
                    System.err.print("Invalid packet - packet has null in it - 1st print statement");
                    sendError(0, "Invalid packet - packet has null in it");
                    break; //abort receiving
                }

                if (request.getOpCode() == 3) { //data opcode
                    byte[] fileData = request.getData();

                    ostream.write(fileData);

                    sendAck(request.getBlockNum());

                    if (fileData.length < 512) { //512 = termination since data is 512 plus 5 bytes for other info
                        break;
                    }
                } else if(request.getOpCode() == 5){
                    System.out.println(request.getErrorCode() + request.getErrorMessage());
                    break;
                    
                } else{
                    
                    System.err.print("Unexpected opcode" + request.getOpCode());
                    break; //stop processing file sending
                }
                
            }
        } catch (IOException e) { 
            e.printStackTrace();
        }
    }

    //send file is reading from the file system & sending content in chunks

    /**
     * Sends files in data chunks of 512 bytes.
     * If there is a file not found error this is caught in a file not found exception and handled by sending
     * and error message with the correct opcode.
     *
     * @param fileName name of file being sent
     */
    public void sendFile(String fileName) {  //reads content of file in chuncks of 512 bytes

        try (InputStream inputStream = new FileInputStream(fileName)) {
            byte[] buffer = new byte[512];
            int blockNo = 1;
            while (true) {
                int size = inputStream.read(buffer);
                if (size == -1) { //condition if there's no data and end of stream has been reached
                    break;
                }

                sendData(blockNo, buffer, size); //send to client side

                receiveAck(blockNo);

                blockNo = (blockNo + 1) & 0xffff; // Allows block num to wrap around to 0 when max capacity reached in bytes. Bitwise operation used here as simpler to implement than modulo

            }
        } catch (FileNotFoundException e) {

            sendError(1, e.getMessage());

        } catch (IOException e) {
            throw new IllegalStateException("Illegal state");
        }

    }

    /**
     * Receives requests.
     * Stores incoming requests in the blocking queue.
     * 
     * @param request request being received
     */
    public void receiveRequest(Request request) {

        requests.add(request);

    }

}
