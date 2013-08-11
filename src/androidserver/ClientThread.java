package androidserver;

import java.io.*;
import java.net.*;

//Extending the thread class to add a clientServiceThread
class ClientThread extends Thread {
    //Has a clientSocket socket and attributes of clientID and if it is running
    private Socket clientSocket;
    private int clientID = -1;
    private int numberOfTestsServed = 0;
    private int numberOfQuestionsServed = 0;
    boolean running = true;
    private boolean isAndroidDevice = false;
    private String clientIPAddress;
    private Logger logger = new Logger();

    //Constructor function that takes a socket and ID number. Also assingns the IP address of the client
    ClientThread(Socket socket, int ID){
        clientSocket = socket;
        clientID = ID;
        //Getting IP Address of connecting client
        clientIPAddress = clientSocket.getInetAddress().getHostAddress();
    }

 //The runnable function, what actually happens when we run the thread
    @Override
  public void run(){
      logger.logInformation("Accepted Client : ID - " + clientID + " : Address - " + clientSocket.getInetAddress().getHostAddress());
      try {
          //Creating new Buffered reader to read in data across the socket connection
          BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          //Creating new print writer to send data across the socket connection
          PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
          //Creating a new Command Interpreter
          ClientCommands commandProcessor = new ClientCommands(in, out, clientID);
          logger.logInformation("BufferedReader, PrintWriter, and CommandProcessor successfully created for Client ID# " + clientID);
          String clientCommand = "";

          //Thread sitts in loop trying to read in information from the socket
          while(running){
                try{
                    //Updating the number of tests and questions served
                    numberOfTestsServed = commandProcessor.getNumberOfTestsServed();
                    numberOfQuestionsServed = commandProcessor.getNumberOfQuestionsServed();

                    //Read in data from the socket
                    clientCommand = in.readLine();
                    //Send the data to the command processor to interpret the data
                    commandProcessor.processCommand(clientCommand);
                    isAndroidDevice = commandProcessor.isAndroidDevice();

                    //Checking to see if the client command wants to 'quit'
                    if(clientCommand.equalsIgnoreCase("QUIT")){
                        //Stop the thread
                        System.out.println("Stopping client thread for client : " + clientID);
                        running = false;
                    }

                }catch(Exception e){
                    logger.logInformation("Client ID# " + clientID + " closed with out the exit command.");
                    logger.logInformation("Stopping client thread for Client ID# " + clientID);
                    //Stop the thread
                    running = false;
                    
                } 
          }
        }catch(Exception e){
            //Handle the error
            logger.logError("Error in creating needed objects.\n" + e.getMessage());
        }
    }

    //Function to return the ID of the client
    public int getID(){
        return clientID;
    }

    //Function to return the number of tests served to the client
    public int getNumberOfTestsServed(){
        return numberOfTestsServed;
    }

    //Function to return the number of questions served to the client
    public int getNumberOfQuestionsServed(){
        return numberOfQuestionsServed;
    }

    //Function that returns true if the client is using an Android Device
    public boolean isAndroidDevice(){
        return isAndroidDevice;
    }

    //Function to return IP Address of the client
    public String getIPAddress(){
        return clientIPAddress;
    }
}