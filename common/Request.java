
package common;


/**
 * The Request class holds all the information for all the different types of packets that can occur
 * 
 * @version 1.0 - 09/05/2020
 * @author Candidate Number: 203068
 */
public class Request {
    
    private final byte opCode;
    private final String fileName;
    private final byte[] data;
    private int blockNum;
    private int errorCode;
    private String errorMessage;
    
    /**
     * This constructor is used when a request only requires an opcode and file name.
     * 
     * @param opCode the opcode for this request
     * @param fileName the file name for this request
     */
    public Request(byte opCode, String fileName){
        
        this.opCode = opCode;
        this.fileName = fileName;
        this.data = null;
    }

    /**
     * This constructor is used when a request only requires a block number and a data-holding section
     * 
     * @param blockNum the block number of this request
     * @param data the data byte array of this request
     */
    public Request(int blockNum, byte[] data) {
        this.blockNum = blockNum;
        this.opCode = 3; //data's opcode
        this.fileName = null;
        this.data = data;
    }
    
    /**
     * This constructor is used when a request only request an opcode
     * 
     * @param opCode the opcode of this request
     */
    public Request(byte opCode){
        
        this.opCode = opCode;
        this.fileName = null;
        this.data = null;
    }
    
    /**
     * This constructor is used when a request only requires an opcode and a block number
     * 
     * @param opCode the opcode of this request
     * @param blockNum the block number of this request
     */
    public Request(byte opCode, int blockNum){
        
        this.opCode = opCode;
        this.fileName = null;
        this.data = null;
        this.blockNum = blockNum;
    }
    
    /**
     * This constructor is used when a request requires an opcode, error code and error message.
     * This constructor is only ever used when creating error packets.
     * 
     * @param opcode opcode of the error packet - always set to 5 as this is the opcode for error packets and will never change
     * @param errorCode error code of the error message (will range between 0-7 as specified on the RFC
     * @param errorMessage error message of the error packet in the form of a string
     */
    public Request(byte opcode, int errorCode, String errorMessage){
        
        this.opCode = 5;
        this.fileName = null;
        this.data = null;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        
    }
    
    /**
     * Getter method for the opcode.
     * 
     * @return the opcode in the form of a byte
     */
    public byte getOpCode() {
        return opCode;
    }

    /**
     * Getter method to get the file name of a request.
     *
     * @return the file name in the form of a string
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Getter method to get the data of a request.
     * 
     * @return the data of a request in the form of a byte array
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Getter method to get the block number of a request.
     * 
     * @return the block number as an integer
     */
    public int getBlockNum() {
        return blockNum;
    }

    /**
     * Getter method to get the error message of a request
     * 
     * @return the error code in the form of an integer
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Getter method to get the error message of a request
     * 
     * @return the error message in the form of a string
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    
    
    
    
    
}
