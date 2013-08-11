package androidserver;

import java.net.*;
import java.util.ArrayList;
import java.util.Random;

//Extension of thread class that just listens on the admin port
public class ManagementPortThread extends Thread{
    private int managementPortNumber;
    private ArrayList<ManagementThread> listOfAdmins = new ArrayList<ManagementThread>();
    private Random random = new Random();
    private Logger logger = new Logger();

    //Constructor takes the port number to listen on
    ManagementPortThread(int portNumber){
        managementPortNumber = portNumber;
    }

    @Override
  public void run() {
        try{
            //Create the socket that listens on the passed in port number
            final ServerSocket managementServerSocket = new ServerSocket(managementPortNumber);
            logger.logInformation("Listening for Admins on port " + managementPortNumber + ".");
            //Sit in while loop accepting connections on the port number
            while(true){
                try{
                    //Try to accept a socket connection
                    Socket managementSocket = managementServerSocket.accept();
                    //Add the first connection to the list, assign the thread a unique ID, and start the thread
                    if(listOfAdmins.isEmpty()){
                        ManagementThread managementThread = new ManagementThread(managementSocket, random.nextInt(100000));
                        listOfAdmins.add(managementThread);
                        listOfAdmins.get(0).start();
                    }else{
                        //Need to check to make sure ID is unique
                        int tmpID = random.nextInt(100000);
                        for(int x = 0; x < listOfAdmins.size(); x++){
                            if(tmpID == listOfAdmins.get(x).getId()){
                                tmpID = random.nextInt(100000);
                                x = 0;
                            }
                        }
                        //Create new thread, add it to the list, and then start the thread
                        ManagementThread managementThread = new ManagementThread(managementSocket, tmpID);
                        listOfAdmins.add(managementThread);
                        listOfAdmins.get((listOfAdmins.size()-1)).start();
                    }
                }catch(Exception e){
                    logger.logError("Failed to accept admin on port " + managementPortNumber + ".\n" + e.getMessage());
                }
            }
        }catch(Exception e){
            //Handle error
            logger.logError("Failed to create a server socket to listen on port " + managementPortNumber + ".\n" + e.getMessage());
        }
    }

    //Funciton to return the list of admin clients
    public ArrayList<ManagementThread> getListOfAdmins(){
        return listOfAdmins;
    }
}