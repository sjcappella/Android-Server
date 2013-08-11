package androidserver;

import java.io.*;
import java.net.*;

//Admin thread that extends the thread class and handles admin connections
class ManagementThread extends Thread {
    //Has a clientSocket socket and attributes of clientID and if it is running
    private Socket managementSocket;
    private int managementID = -1;
    boolean running = true;
    private String adminIPAddress;
    private Logger logger = new Logger();

    //Constructure takes a socket and Admin ID
    ManagementThread(Socket socket, int uniqueID) {
    managementSocket = socket;
    managementID = uniqueID;
    //Also set the IP Address of the admin client
    adminIPAddress = managementSocket.getInetAddress().getHostAddress();
    }

    //The runnable function, what actually happens when we run the thread
    @Override
    public void run(){
      logger.logInformation("Accepted Admin ID#  " + managementID + " : Address - " + managementSocket.getInetAddress().getHostAddress());
      try{
          //Creating the BufferedReader to read data across the socket
          BufferedReader in = new BufferedReader(new InputStreamReader(managementSocket.getInputStream()));
          //Creating the PrintWriter to send data across the socket
          PrintWriter out = new PrintWriter(new OutputStreamWriter(managementSocket.getOutputStream()));
          //Creating a new command interpreter for the management commands
          ManagementCommands commandInterpreter = new ManagementCommands(in, out, managementID);

          String managementCommand = "";

          //What happens while the thread is running
          while(running){
              try{
                //Read in the command from the admin
                managementCommand = in.readLine();
                commandInterpreter.processCommand(managementCommand);

                //Checking to see if the client command wants to 'quit'
                if(managementCommand.equals("QUIT")){
                    //Stop the thread
                   running = false;
                   logger.logAction("Stopping Admin thread for Admin ID# " + managementID);
                }
              }catch(Exception e){
                    logger.logError("Admin ID# " + managementID + " stopped with out the exit command.\n" + e.getMessage());
                    running = false;
              }
           }
        }catch(Exception e){
            //Handle the error
            logger.logError("There was an error creating the data objects.\n" + e.getMessage());
        }
    }

    //Function to return the Admin ID
    public int getID(){
        return managementID;
    }

    //Function to return the Admin IP Address
    public String getIPAddress(){
        return adminIPAddress;
    }
}