/* Server program for the OnlineOrder app

   @author Brett Swanson

   @version CS 391 - Spring 2018 - A2
*/

import java.net.*;
import java.io.*;
import java.util.*;

public class OOServer {

    static ServerSocket serverSocket = null;  // listening socket
    static int portNumber = 55555;
    // port on which server listens
    static Socket clientSocket = null;        // socket to a client

    /* Start the server then repeatedly wait for a connection
    request, accept, and start a new thread to handle one
    online order
    */
    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server started: " + serverSocket);
            while (true) {
                clientSocket = serverSocket.accept();
                System.out.println("New connection established: " +
                clientSocket);
                (new Thread(new OO(clientSocket))).start();
            }
        } catch (IOException e) {
            System.out.println
            ("Server encountered an error. Shutting down...");
        }
	
    }// main method

}// OOServer class

class OO implements Runnable {

    static final int MAIN = 0;          // M state
    static final int PIZZA_SLICE = 1;   // PS state
    static final int HOT_SUB = 2;       // HS state
    static final int DISPLAY_ORDER = 3; // DO state
    static final Menu mm =              // Main Menu
        new Menu( new String[] { "Main Menu:", "Pizza Slices",
        "Hot Subs", "Display order" } );
    static final Menu psm =             // Pizza Slice menu
        new Menu( new String[] { "Choose a Pizza Slice:", "Cheese",
        "Pepperoni", "Sausage", "Back to Main Menu",
        "Display Order" } );
    static final Menu hsm =             // Hot Sub menu
        new Menu( new String[] { "Choose a Hot Sub:", "Italian",
        "Meatballs", "Back to Main Menu", "Display Order"  } );
    static final Menu dom =             // Display Order menu
        new Menu( new String[] { "What next?",
        "Proceed to check out", "Go Back to Main Menu"  } );
    int state;                          // current state
    Order order;                        // current order
    Socket clientSocket = null;         // socket to a client
    DataInputStream in = null;          // input stream from client
    DataOutputStream out = null;        // output stream to client

    /* Init client socket, current state, and order, and
    open the necessary streams
     */
    OO(Socket clientSocket)  {

        this.clientSocket = clientSocket;
        state = MAIN;
        order = new Order();
        try {
            openStreams(clientSocket);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
	
    }// OO constuctor

    /* each execution of this thread corresponds to one online
       ordering session
     */
    public void run() {

       try {
           placeOrder();
       } catch(EOFException e) {
           System.out.println("Client died unexpectedly");
           System.out.println(clientSocket);
       }catch (IOException e) {
           System.out.println("blah blah blah " + e);
       }
	
    }// run method

    /* implement the OO protocol as described by the FSM in
       the handout Note that, before reading the first query
       (i.e., option), the server must display the welcome message
       shown in the trace in the handout,  followed by the main menu.
     */
    void placeOrder() throws IOException {
        String request, reply;
        String line = "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n";
        String welcome = "     Welcome to Hot Subs & Wedges!\n";
        reply = line + welcome + line;
        out.writeUTF(reply);
        boolean first = true;
        boolean end = false;
        while (true) {
            switch(state) {
                case MAIN:
                    if (first || reply.equals("Invalid Option!")) {
                        reply = mm.toString();
                        first = false;
                    }
                    else {
                        reply = line + mm.toString();
                    }
                    out.writeUTF(reply);
                    request = in.readUTF();
                    switch(request) {
                        case "1":
                            state = PIZZA_SLICE;
                            break;
                        case "2":
                            state = HOT_SUB;
                            break;
                        case "3":
                            state = DISPLAY_ORDER;
                            break;
                        default:
                            reply = "Invalid Option!";
                            out.writeUTF(reply);
                    }
                    break;
                case PIZZA_SLICE:
                    reply = line + psm.toString();
                    out.writeUTF(reply);
                    request = in.readUTF();
                    switch(request) {
                        case "1":
                            order.addItem("Cheese pizza");
                            break;
                        case "2":
                            order.addItem("Pepperoni pizza");
                            break;
                        case "3":
                            order.addItem("Sausage pizza");
                            break;
                        case "4":
                            state = MAIN;
                            break;
                        case "5":
                            state = DISPLAY_ORDER;
                            break;
                        default:
                            reply = "Invalid Option!";
                            out.writeUTF(reply);
                    }
                    break;
                case HOT_SUB:
                    reply = line + hsm.toString();
                    out.writeUTF(reply);
                    request = in.readUTF();
                    switch(request) {
                        case "1":
                            order.addItem("Italian sub");
                            break;
                        case "2":
                            order.addItem("Meatballs sub");
                            break;
                        case "3":
                            state = MAIN;
                            break;
                        case "4":
                            state = DISPLAY_ORDER;
                            break;
                        default:
                            reply = "Invalid Option!";
                            out.writeUTF(reply);

                    }
                    break;
                case DISPLAY_ORDER:
                    reply = line + order.toString() + "\n" + line +
                    dom.toString();
                    out.writeUTF(reply);
                    request = in.readUTF();
                    switch(request) {
                        case "1":
                            out.writeUTF
                            ("Thank you for your Visit!");
                            System.out.println
                            ("One More Order Processed!");
                            close();
                            end = true;
                        case "2":
                            state = MAIN;
                            break;
                        default:
                            reply = "Invalid Option!";
                            out.writeUTF(reply);
                    }
                    break;
            }
            if (end) {
                break;
            }
        }

    }// placeOrder method

   /* open the necessary I/O streams and initialize the in and out
      static variables; this method does not catch any exceptions
    */
    void openStreams(Socket socket) throws IOException {

       in = new DataInputStream(clientSocket.getInputStream());
       out = new DataOutputStream(clientSocket.getOutputStream());
    }// openStreams method

    /* close all open I/O streams and sockets
     */
    void close() {
        try {
            if (in != null)           { in.close();          }
            if (out != null)          { out.close();         }
            if (clientSocket != null) { clientSocket.close();}
        } catch(IOException e) {
            System.err.println("Error in close(): " +
            e.getMessage());
        }
	
    }// close method

}// OO class
