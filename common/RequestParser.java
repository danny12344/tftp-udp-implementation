
package common;

import java.util.Arrays;

/**
 * The RequestParser class identifies the type of a packet 
 * and also holds methods that can extract meaningful data
 * from those packets
 * 
 * @version 1.0 - 09/05/2020
 * @author Candidate Number: 203068
 */
public class RequestParser {
    
    /**
     * parseRequest method is responsible for identifying the type of packet that as been received.
     * 
     * @param buffer the incoming data from a packet in the form of a byte array
     * @param length the length of the byte array from the incoming packet
     * @return a instance of the Request class with the parameters appropriate to the type of packet being processed
     */
    public Request parseRequest(byte[] buffer, int length) {

        if (buffer.length < 4) {
            return null; //request is wrong
        }

        if (buffer[1] == 1) { //RRQ
            return new Request((byte) 1, extractFileName(buffer));
        }

        if (buffer[1] == 2) { //WRQ
            return new Request((byte) 2, extractFileName(buffer));
        }

        if (buffer[1] == 3) { //DATA
            return new Request(wordToInt(buffer, 2), Arrays.copyOfRange(buffer, 4, length));
        }
        
        if(buffer[1] == 4){ //ACK
            return new Request((byte) 4, wordToInt(buffer, 2)); //4 is opcode of ack
        }
        
        if(buffer[1] == 5){ //ERROR
            return new Request((byte) 5, buffer[3], extractErrorMessage(buffer));
        }

        return null; // request is wrong
    }
    
    private int wordToInt(byte[] buffer, int start){ 
        return (buffer[start] & 0xff)*256+(buffer[start+1] & 0xff); //0xff removes the signed bit
    }
    
    /**
     * Extracts the name of a file from the byte array within a packet
     *
     * @param buffer the byte array of data passed in from a the packet from which the file name is being extracted
     * @return string representation of the file name that was extracted
     */
    public String extractFileName(byte[] buffer) {
        int endOfFileName = 4;

        for (; endOfFileName < buffer.length; endOfFileName++) {
            if (buffer[endOfFileName] == 0) {
                break;
            }
        }

        return new String(buffer, 3, endOfFileName - 3);
    }

    /**
     * Extracts the error message from an error packet.
     * Works in exactly the same was as the extract file name method
     * 
     * @param buffer byte array of data passed in from the error packet
     * @return a string representation of the error message contained inside the error packet
     */
    public String extractErrorMessage(byte[] buffer) {
        int endOfFileName = 4;

        for (; endOfFileName < buffer.length; endOfFileName++) {
            if (buffer[endOfFileName] == 0) {
                break;
            }
        }

        return new String(buffer, 4, endOfFileName - 4);
    }
    
}
