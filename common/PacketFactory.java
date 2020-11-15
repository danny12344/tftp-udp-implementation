package common;

/**
 * The PacketFactory class contains all the methods request to prepare the
 * different types of packets required, ready for sending.
 *
 * @version 1.0 - 09/05/2020
 * @author Candidate Number: 203068
 */
public class PacketFactory {

    //Create RRQ or WRQ method below holds the shared logic for the two child methods as most of the logic is common
    private byte[] createRRQorWRQ(byte opCode, String fileName) {

        byte[] stringBytes = fileName.getBytes();
        byte[] octetBytes = "octet".getBytes();

        byte[] packet = new byte[2 + stringBytes.length + 1 + 5 + 1];
        packet[1] = opCode; //1 for opcode of RRQ/WRQ
        System.arraycopy(stringBytes, 0, packet, 3, stringBytes.length); //default values for bytes in array is 0

        System.arraycopy(octetBytes, 0, packet, 3 + stringBytes.length + 1, octetBytes.length);

        return packet;

    }

    /**
     * Creates a read request. Creates a read request using the logic in the
     * above "createRRQorWRQ" method
     *
     * @param fileName name of file being requested from the server
     * @return the data for the packet to be sent in the form of a byte array.
     */
    public byte[] createRRQ(String fileName) {

        return createRRQorWRQ((byte) 1, fileName);

    }

    /**
     * Creates a write request. Creates a write request using the logic from the
     * "createRRQorWRQ" method.
     *
     * @param fileName name of file to be written to the server
     * @return the data for the packet to be sent in the form of a byte array
     */
    public byte[] createWRQ(String fileName) {
        return createRRQorWRQ((byte) 2, fileName);
    }

    /**
     * Creates an error packet.
     *
     *
     * @param errorCode byte representing the appropriate error code for the
     * error that is about to be created
     * @param errMsg string message to be displayed in the console when this
     * error is thrown.
     * @return the data for the error packet to be sent in the form of a byte
     * array
     */
    public byte[] createERROR(byte errorCode, String errMsg) {

        byte[] errMsgBytes = errMsg.getBytes(); //convert error message string to bytes
        byte[] packet = new byte[2 + 2 + errMsgBytes.length + 1]; //2 bytes for opcode, 2 bytes for error code, size of string for errMsg and 1 byte to signal end of pakcet

        packet[1] = 5; //opcode 05 for errors

        System.arraycopy(errMsgBytes, 0, packet, 4, errMsgBytes.length); //copy error mesage byte array into correct position in packet byte array

        return packet;
    }

    /**
     * Creates a data packet. Creates a data packet by creating a byte array
     * that stores all the required information at the correct positions in the
     * byte array. In total full data packets will have a size of 516 bytes.
     *
     * @param blockNum the block number of the data packet to be sent
     * @param data the data of the current chunk to be sent - in the form of a
     * byte array
     * @param size the size of the data packet to be sent
     * @return a byte array containing the data to be sent in a packet
     */
    public byte[] createData(int blockNum, byte[] data, int size) {

        byte[] packet = new byte[2 + 2 + size];

        packet[1] = 3; //opcode for data

        packet[3] = (byte) (blockNum & 0xff);

        packet[2] = (byte) ((blockNum >> 8) & 0xff);

        System.arraycopy(data, 0, packet, 4, size);

        return packet;

    }

    /**
     * Creates an acknowledgement.
     * Creates an acknowledgement by passing all appropriate information into a byte array.
     * Requires correct block number from package being acknowledged to be passed into it.
     * 
     * @param blockNum block number of the acknowledgement
     * @return a byte array of the data to be stored in the ack packet being created
     */
    public byte[] createAck(int blockNum) {

        byte[] packet = new byte[2 + 2];

        packet[1] = 4; //opcode for data

        packet[3] = (byte) (blockNum & 0xff); //bitwise operation - 0xff another way to write 255

        packet[2] = (byte) ((blockNum >> 8) & 0xff); //bitwise operation shifting 8 bits to the right to shift out lowest 8 bits & then ended with 255 to keep only those bits

        return packet;

    }

}
