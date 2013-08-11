package androidserver;

import java.net.*;
import java.util.ArrayList;
import java.util.Random;

//Extended thread class that listens on a specific port for clients
public class ClientPortThread extends Thread {
    //Declaring variables
    private int clientPortNumber;
    private ArrayList<ClientThread> listOfClients = new ArrayList<ClientThread>();
    private Random random = new Random();
    private Logger logger = new Logger();

    //Constructor take port number to listen on
    ClientPortThread(int portNumber){
        clientPortNumber = portNumber;
    }

    @Override
  public void run() {
        try{
            //Create a server socket on the port number passed in
            final ServerSocket clientServerSocket = new ServerSocket(clientPortNumber);
            while(true){
                    try{
                        logger.logAction("Listening for Clients on port " + clientPortNumber + ".");
                        //Block and listen for a connection
                        Socket clientSocket = clientServerSocket.accept();
                        //Add first client to the list
                        if(listOfClients.isEmpty()){
                            //Spawn new client thread
                            ClientThread clientThread = new ClientThread(clientSocket, random.nextInt(100000));
                            listOfClients.add(clientThread);
                            listOfClients.get(0).start();
                            logger.logInformation("New client thread was started.");
                        }else{
                            //Rest of the clients and making sure their IDS are unique
                            int tmpID = random.nextInt(100000);
                            for(int x = 0; x < listOfClients.size(); x++){
                                if(tmpID == listOfClients.get(x).getID()){
                                    tmpID = random.nextInt(100000);
                                    x = 0;
                                }
                            }
                            //Spawn new client thread
                            ClientThread clientThread = new ClientThread(clientSocket, tmpID);
                            listOfClients.add(clientThread);
                            listOfClients.get((listOfClients.size()-1)).start();
                            logger.logInformation("New client thread was started.");
                        }
                    }catch(Exception e){
                        //Handle the error
                        logger.logError("Error accepting socket.\n" + e.getMessage());
                    }
                }
        }catch(Exception e){
            //Handle error
            logger.logError("Error creating the server socket.\n" + e.getMessage());
        }
    }

    //Return a list of all the connected client threads
    public ArrayList<ClientThread> getListOfClients(){
        return listOfClients;
    }
}