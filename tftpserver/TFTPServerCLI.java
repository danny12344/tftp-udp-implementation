
package tftpserver;

import java.io.IOException;

/**
 * This simple class calls the TFTPMultiThreadedServer class and prints a simple message stating that the server has started running
 * 
 * @version 1.0 - 09/05/2020
 * @author Candidate Number: 203068
 */
public class TFTPServerCLI {
    
    public static void main(String[] args) throws IOException {
        new TFTPMultiThreadedServer("Server start").run();
        System.out.println("Time Server Started");
    }
    
}
