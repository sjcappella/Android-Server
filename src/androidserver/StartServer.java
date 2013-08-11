package androidserver;

import java.io.*;
import java.util.ArrayList;

//Class to get the server up and running
public class StartServer {
    //Variables
    private int clientPort;
    private int adminPort;
    private Logger logger = new Logger();

    //Constructor takes the client port and the admin port to listen on
    StartServer(int clientPort, int adminPort){
        this.clientPort = clientPort;
        this.adminPort = adminPort;
        createLogFile();
        createClientList();
        createAdminList();
        createListOfTests();
        createServerStatus();
        createStatisticsDirectory();
        createTestsDirectory();        
    }

    //Function to create a log file. Will create a new one every time
    private void createLogFile(){
        try{
             File logFile = new File("../logFile.txt");
             //If the file exists, delete it, otherwise, create a new one
             if(logFile.exists()){
                 logFile.delete();
                 logFile.createNewFile();
                 logger.logInformation("Log File was successfully Created!");
             }else{
                logFile.createNewFile();
                logger.logInformation("Log File was successfully Created!");
             }
        }catch(Exception e){
            //Handle the error
            logger.logError("Some error occured trying to create the Log File!\n" + e.getMessage());
        }           
    }

    //Function to create a list of registered clients
    private void createClientList(){
        try{
            File clientList = new File("../listOfClients.txt");
            //If file does exist, do nothing
            if(clientList.exists()){
                logger.logInformation("List of Clients file already exists! Doing nothing.");
            }else{
                //Create the file otherwise
                clientList.createNewFile();
                logger.logInformation("List of Clients file was successfully created!");
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("Some error occured trying to create the Client List File!\n" + e.getMessage());
        }
    }

    //Function to create a list of adminst
    private void createAdminList(){
        try{
            File adminList = new File("../listOfAdmins.txt");
            //If the file does exist, do nothing
            if(adminList.exists()){
                logger.logInformation("List of Admins file already exists! Doing nothing.");
            }else{
                //Create the file otherwise
                adminList.createNewFile();
                logger.logInformation("List of Admins file was successfully created!");
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("Some error occured trying to create the Admin List File!\n" + e.getMessage());
        }
    }

    //Function to create a list of tests file
    private void createListOfTests(){
        try{
            File listOfTests = new File("../listOfTests.txt");
            //If the file does exists, do nothing
            if(listOfTests.exists()){
                logger.logInformation("List of Tests file already exists! Doing nothing.");
            }else{
                //Create the file otherwise
                listOfTests.createNewFile();
                logger.logInformation("List of Tests file was successfully created!");
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("Some error occured trying to create the Test List File!\n" + e.getMessage());
        }
    }

    //Function to create server status file
    private void createServerStatus(){
        try{
            File serverStatus = new File("../serverStatus.txt");
            //If file exists, it will be deleted and a new one will be created
            if(serverStatus.exists()){
                logger.logInformation("Server Status file already exists. Deleting and creating a new one!");
                serverStatus.delete();
                serverStatus.createNewFile();
                logger.logInformation("Server Status file was successfully created!");
            }else{
                serverStatus.createNewFile();
                logger.logInformation("Server Status file was successfully created!");
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("Some error occured while trying to create the Server Status file!\n" + e.getMessage());
        }
    }

    //Function to create the directory that holds all the client statistics files
    private void createStatisticsDirectory(){
        try{
            //If the directory exists, do nothing
            File statistics = new File("../Statistics");
            if(statistics.exists()){
                logger.logInformation("Statistics directory already exists! Doing nothing.");
            }else{
                //Otherwise create the directory
                statistics.mkdir();
                logger.logInformation("Statistics directory was successfully created!");
            }
        }catch(Exception e){
            logger.logError("Some error occured trying to create the Statistics directory!\n" + e.getMessage());
        }
    }

    //Function to create the directory that holds the tests
    private void createTestsDirectory(){
        try{
            File tests = new File("../Tests");
            //If the directory exists, do nothing
            if(tests.exists()){
                logger.logInformation("Tests directory already exists! Doing nothing.");
            }else{
                //Create the directory otherwise
                tests.mkdir();
                logger.logInformation("Tests directory was successfully created!");
            }
        }catch(Exception e){
            logger.logError("Some error occured trying to create the Tests directory!\n" + e.getMessage());
        }
    }

    //Function to update the list of active connections
    private void updateServerStatus(ClientPortThread clients, ManagementPortThread admins){
        ArrayList<String> tempFile = new ArrayList<String>();
        //See how many clients are connected
        for(int x = 0; x < clients.getListOfClients().size(); x++){
            //Is the client running?
            if(clients.getListOfClients().get(x).running){
                //Entry for an Android client
                if(clients.getListOfClients().get(x).isAndroidDevice()){
                    tempFile.add("Client Connection: Client ID# " + clients.getListOfClients().get(x).getID() + " - Android Device - IP-Address: " + clients.getListOfClients().get(x).getIPAddress() + " ACTIVE");
                }else{
                    //Entry for a regular client
                    tempFile.add("Client Connection: Client ID# " + clients.getListOfClients().get(x).getID() + " - IP-Address: " + clients.getListOfClients().get(x).getIPAddress() + " ACTIVE");
                }
            }else{
                //Inactive client connection
                tempFile.add("Client Connection: Client ID#" + clients.getListOfClients().get(x).getID());
            }
        }
        //See how many admins are connected
        for(int x = 0; x < admins.getListOfAdmins().size(); x++){
            //Check to see if admin is running
            if(admins.getListOfAdmins().get(x).running){
                //Entry for running admin
                tempFile.add("Admin Connection: Admin ID# " + admins.getListOfAdmins().get(x).getID() + " - IP-Address: " + admins.getListOfAdmins().get(x).getIPAddress() + " ACTIVE");
            }else{
                //Entry for inactive admin
                tempFile.add("Admin Connection: Admin ID #" + admins.getListOfAdmins().get(x).getID());
            }
        }

        //Create a new Server Status text file
        createServerStatus();
        logger.logAction("Updating active client connections.");
        //Write the temporary file out
        try{
            BufferedWriter out = new BufferedWriter(new FileWriter("../serverStatus.txt", true));
            for(int x = 0; x < tempFile.size(); x++){
                out.write(tempFile.get(x));
                out.newLine();
            }
            out.close();
        }catch(Exception e){
            //Handle the error
            logger.logError("Error in writing server status file.\n" + e.getMessage());
        }
    }

    //Run function that gets the server up and off the ground
    public void run(){
        //Create a thread that will listen on the client port
        ClientPortThread clientPortThread = new ClientPortThread(clientPort);
        //Create a thread that will listen on the admin port
        ManagementPortThread adminPortThread = new ManagementPortThread(adminPort);
        //Start the client port listener
        clientPortThread.start();
        //Start the admin port listener
        adminPortThread.start();
        //Log the status of the client port listener
        if(clientPortThread.isAlive()){
            logger.logInformation("Client port is up and listening on port " + clientPort + "...");
        }else{
            logger.logError("Client port is not up and running!");
        }
        //Log the status of the admin port listener
        if(adminPortThread.isAlive()){
            logger.logInformation("Admin port is up and listening on port " + adminPort + "...");
        }else{
            logger.logError("Admin port is not up and running!");
        }
        if(clientPortThread.isAlive() && adminPortThread.isAlive()){
            logger.logInformation("Server is successfully up and running!");
        }else{
            logger.logError("An error occured starting the server!");
        }
        //Check for new admin and client connections
        int numberOfAdmins = 0, tmpAdminNumber = 0, numberOfClients = 0, tmpClientNumber = 0;
        while(true){
            tmpClientNumber = clientPortThread.getListOfClients().size();
            //If the number is different, update the server status
            if(numberOfClients != tmpClientNumber){
                updateServerStatus(clientPortThread, adminPortThread);
                numberOfClients = tmpClientNumber;
            }
            //If the number is different, update the server status
            tmpAdminNumber = adminPortThread.getListOfAdmins().size();
            if(numberOfAdmins != tmpAdminNumber){
                updateServerStatus(clientPortThread, adminPortThread);
                numberOfAdmins = tmpAdminNumber;
            }
        }
    }
}
