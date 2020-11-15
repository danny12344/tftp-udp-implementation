/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tftpclient;

import java.io.IOException;
import java.util.Scanner;

/**
 * This class holes the TFTP-UDP-Client project's main method. It takes input
 * from the args[0] parameter of the project which I have left empty for
 * submission purposes. To make read or write requests ensure you have a file
 * name set as a parameter in the default config setup and that you are entering
 * a capital "R" or a capital "W" otherwise the project will not run correctly.
 * If no parameter is passed for args[0] the program will exit and a message explaining what has caused this will print in the console.
 *
 * @version 1.0 - 09/05/2020
 * @author Candidate Number: 203068
 */
public class TFTPClientCLI {

    /**
     * This is the main method for the whole TFTP-UDP-Client project and it
     * takes input from the command line
     *
     * @param args input taken from the default config parameter in netbeans
     * @throws IOException if there is an I/O error
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("ERROR: Expected a file name parameter for args[0]. Please pass a file name as a parameter using the default config menu!!");
            System.exit(1);
        }

        TFTPClient client1 = new TFTPClient(9000, 10000);

        TFTPClient client2 = new TFTPClient(9000, 10002);
        
        TFTPClient client3 = new TFTPClient(9000, 10003);
        
        TFTPClient client4 = new TFTPClient(9000, 10004);

        Scanner in = new Scanner(System.in);

        System.out.print("Enter R to generate a read request or W to generate a write request: ");

        String s = in.nextLine();

        if (s.equals("R")) { //RRQ

            new Thread(() -> client1.getFile(args[0], "test1.pdf")).start();
            new Thread(() -> client2.getFile(args[0], "test2.pdf")).start(); //2 clients run here to demonstrate multi-threading capability
        }

        if (s.equals("W")) { //WRQ

            new Thread(() -> client3.sendFile(args[0], "test3.pdf")).start();
            new Thread(() -> client4.sendFile(args[0], "test4.pdf")).start(); //2 clients run here to demonstrate multi-threading capability

        }

    }
}
