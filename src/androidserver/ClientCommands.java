package androidserver;
import java.io.*;
import java.util.*;

public class ClientCommands {
    //Declaring all the variables needed for this class
    private BufferedReader in;
    private PrintWriter out;
    private int ID;
    private int numberOfTestsServed = 0;
    private int numberOfQuestionsServed = 0;
    private String ESUUserNameHash;
    private String nameOfTheTest;
    private TXTParser txtParser = new TXTParser();
    private boolean registrationStatus;
    private boolean loginStatus = false;
    private boolean synchronize = false;
    private boolean isAndroid = false;
    private String sessionID;
    private ArrayList<Question> loadedTest = new ArrayList<Question>();
    private ArrayList<String> loadedTestAnswers = new ArrayList<String>();
    private ArrayList<String> loadedTestStats = new ArrayList<String>();
    private Logger logger = new Logger();

    //Constructor function
    ClientCommands(BufferedReader input, PrintWriter output, int clientID){
        in = input;
        out = output;
        ID = clientID;
    }

    //This function processes all the commands sent by the client
    void processCommand(String command){
        //Log the clients command
        logger.logInformation("Client ID# " + ID + " says " + command);
        //Command to register the client
        if(command.equals("REGISTER")){
            register();
        }
        //Command to distinguish client on Android device
        if(command.equals("ANDROID_DEVICE")){
            isAndroid = true;
            logger.logInformation("Client #" + ID + " is connected with an Android Device.");
        }
        //Command to get client's registrations status
        if(command.equals("GET_REGISTRATION_STATUS")){
            sendRegistrationStatus();
        }
        //Command to log the client in to the server
        if(command.equals("LOGIN")){
            login();
        }
        //Command to get the client's login status
        if(command.equals("GET_LOGIN_STATUS")){
            sendLoginStatus();
        }
        //Command to get the current session ID for this connection
        if(command.equals("GET_SESSION_ID")){
            sendSessionID();
        }
        //Command to get the test menu
        if(command.equals("GET_TEST_MENU")){
            sendTestMenu();
        }
        //Command to load a test
        if(command.equals("LOAD_TEST")){
            loadTest();
        }
        //Command to get the loaded test
        if(command.equals("GET_TEST")){
            getTest();
        }
        //Command to grade a test from the client
        if(command.equals("GRADE_TEST")){
            gradeTest();
        }
        //Command to load the test statistics
        if(command.equals("LOAD_TEST_STATISTICS")){
            loadTestStatistics();
        }
        //Command to clean up some objects
        if(command.equals("CLEAN_UP")){
            cleanUp();
        }
        //End synchronization command
        if(command.equals("END_SYNCHRONIZE")){
            synchronize = false;
        }
        //Quit command to end the session
        if(command.equals("QUIT")){
            synchronize = false;
        }
        //Catch case for no command
        if(command.isEmpty()){
            synchronize = false;
        }
    }

    //Function to register the client with the server
    private void register(){
        //Declaring variables
        String name = "";
        String ESUUserName = "";
        String password = "";
        String deviceUID = "";

        //Telling the client to send their credentials for registration
        out.println("SEND_REGISTRATION_CREDENTIALS");
        out.flush();
        logger.logAction("Requested registration credentials from Client ID# " + ID);
        //Trying to parse in credentials sent from client
        try{
            name = in.readLine();
            ESUUserName = in.readLine();
            password = in.readLine();
            deviceUID = in.readLine();
        }catch(Exception e){
            //Handle the error
            logger.logError("Could not read in registration credentials for Client ID# " + ID + "\n" + e.getMessage());
        }
        //Checking to make sure all fields have value
        if((!name.isEmpty()) && (!ESUUserName.isEmpty()) && (!password.isEmpty()) && (!deviceUID.isEmpty())){
            //This makes a call to add a new student, if it returns true, the student was successfully added
            if(txtParser.addNewStudent(name, ESUUserName, password, deviceUID)){
                logger.logInformation("Successfully registered " + name + " under ClientID# " + ID);
                registrationStatus = true;
            }else{
                //Error in adding the student
                logger.logInformation("Failed to register the student '" + name + "'");
                registrationStatus = false;
            }
        }
    
    }

    //Function to log the student into the server
    private void login(){
        //Declaring variables
        String name = "";
        String ESUUserName = "";
        String password = "";
        String deviceUID = "";
        logger.logAction("Requesting login credentials from Client ID# " + ID);
        out.println("SEND_LOGIN_CREDENTIALS");
        out.flush();
        //Try to parse in the login credentials sent from the client
        try{
            name = in.readLine();
            ESUUserName = in.readLine();
            password = in.readLine();
            deviceUID = in.readLine();
        }catch(Exception e){
            //Handle the error
            logger.logError("Could not read in login credentials for Client ID# " + ID + "\n" + e.getMessage());
        }
        //Checking to make sure all fields have value
        if((!name.isEmpty()) && (!ESUUserName.isEmpty()) && (!password.isEmpty()) && (!deviceUID.isEmpty())){
            //Function to validate student, if it returns true, the student was succesfully logged in
            if(txtParser.validateStudent(name, ESUUserName, password, deviceUID)){
                logger.logInformation("Successfully Logged-In " + name + " under ClientID# " + ID);
                loginStatus = true;
                try{
                    //Creating the session ID
                    sessionID = SimpleMD5.MD5(Integer.toString(ID));
                    logger.logInformation("Successfully created Session ID for Client ID# " + ID);
                }catch(Exception e){
                    //Handle error
                    logger.logError("Could not create Session ID for Client ID# " + ID + "\n" + e.getMessage());
                }
            }else{
                //Failed to log the student in
                logger.logError("Failed to Log-In Client ID# " + ID);
                loginStatus = false;
            }
        }
        //Need to create hash of username for later use
        try{
        ESUUserNameHash = SimpleMD5.MD5(ESUUserName);
        logger.logInformation("Successfully created username hash for Client ID# " + ID);
        }catch(Exception e){
            //Handle the error
            logger.logError("Failed to generate username hash for Client ID# " + ID + "\n" + e.getMessage());
        }
    }

    //Function to send the registration status back to the client
    private void sendRegistrationStatus(){
        logger.logAction("Sending registration status for Client ID#" + ID);
        if(registrationStatus){
            out.println("REGISTRATION_SUCCESSFUL");
            out.flush();
        }else{
            out.println("REGISTRATION_FAILED");
            out.flush();
        }
    }

    //Function to send login status back to the client
    private void sendLoginStatus(){
        logger.logAction("Sending login status for Client ID# " + ID);
        if(loginStatus){
            out.println("LOGIN_SUCCESSFUL");
            out.flush();
        }else{
            out.println("LOGIN_FAILED");
            out.flush();
        }
    }

    //Function to send the session ID back to the client
    private void sendSessionID(){
        logger.logAction("Sending session ID for Client ID# " + ID);
        out.println(sessionID);
        out.flush();
    }

    //Function to send the test menu to the client
    private void sendTestMenu(){
        //Load and populate the test menu
        txtParser.populateTestList();
        //Get the number of tests available
        int numTests = txtParser.getNumberOfTests();
        synchronize = true;
        while(synchronize){
            logger.logAction("Sending number of tests - " + numTests + " to Client ID# " + ID);
            out.println(Integer.toString(numTests));
            out.flush();
            try{
                processCommand(in.readLine());
            }catch(Exception e){
                //Handle the error
                logger.logError(e.getMessage());
            }
        }
        out.flush();
        ArrayList<String> listOfTestsArrayList = new ArrayList<String>();
        //Sending all the names of the tests to the client
        logger.logAction("Sending all the test names to Client ID# " + ID);
        for(int x = 0; x < txtParser.getTestArrayList().size(); x++){
             listOfTestsArrayList.add(txtParser.getTestArrayList().get(x));
             out.println(listOfTestsArrayList.get(x));
             out.flush();
        }
    }

    //Function to load the selected test
    private void loadTest(){
        //Try to read in the name of the test
        try{
            String testName = in.readLine();
            nameOfTheTest = testName;
            logger.logInformation("Client #" + ID + " wants the " + testName + " test.");
            testName = testName.replaceAll(" ", "");
            //Loading the text into the TxtParser
            txtParser.loadTest(testName);
            for(int x = 0; x < txtParser.getLoadedTest().size(); x++){
                loadedTest.add(txtParser.getLoadedTest().get(x));
            }
            logger.equals("Test was loaded for Client ID# " + ID);
            if(txtParser.getLoadedTest().isEmpty()){
                //The test has no questions or an error occured
                out.println("ERROR");
                out.flush();
                logger.logError("An error occured after loading the test for Client ID# " + ID);
            }else{
                out.println("TEST_LOADED");
                out.flush();
                logger.logInformation("Test was successfully loaded for Client ID# " + ID);
            }
        }catch(Exception e){
            //Handle the error
            out.println("ERROR");
            out.flush();
            logger.logError("An error occured during loading of the test for Client ID# " + ID + "\n" + e.getMessage());
        }
    }

    //Function to send all the test questions to the client
    private void getTest(){
        //Declaring variables
        int numQuestions = loadedTest.size();
        if(numQuestions == 0){
            //Error occurs if the number of questions equals 0
            out.println("ERROR");
            out.flush();
            logger.logError("Error sending the test to Client ID# " + ID + ". 0 questions in test.");
        }else{
            logger.logInformation("Client ID# " + ID + " requested test with " + numQuestions + " questions");
            //Sending number of questions
            out.println(numQuestions);
            out.flush();
            for(int x = 0; x < numQuestions; x++){
                //Sending only the question and the 4 possible answers to the client
                logger.logAction("Sending question and 4 choices to Client ID# " + ID);
                out.println(loadedTest.get(x).getQuestion());
                out.flush();
                out.println(loadedTest.get(x).getAnswerA());
                out.flush();
                out.println(loadedTest.get(x).getAnswerB());
                out.flush();
                out.println(loadedTest.get(x).getAnswerC());
                out.flush();
                out.println(loadedTest.get(x).getAnswerD());
                out.flush();
                //Saving the correct answers for grading later on
                loadedTestAnswers.add(loadedTest.get(x).getCorrectAnswer());
                logger.logAction("Saved the correct answers for Client ID# " + ID);
            }
            //Increment stats
            numberOfQuestionsServed += loadedTest.size();
            numberOfTestsServed++;
        }
    }

    //Function to grade the test
    private void gradeTest(){
        //No correct answers in beginning
        int numberCorrect = 0;
        //For every loaded test answer, we will try to read in the answer sent in by
        //the client and compare it to our stored answer
        logger.logAction("Grading test for Client ID# " + ID);
        for(int x = 0; x < loadedTestAnswers.size(); x++){
            try{
                //If they are equal, number of correct answers increases by 1
                if(loadedTestAnswers.get(x).equals(in.readLine())){
                    numberCorrect++;
                }
            }catch(Exception e){
                //Handle the error
                logger.logError("Error reading in the answers sent by Client ID# " + ID + "\n" + e.getMessage());
            }
        }
        //Verifying session ID to confirm it is still the original client
        try{
            logger.logInformation("Checking session ID for Client ID# " + ID);
            //If they match, send back the number correct so client can see grade
            if(sessionID.equals(in.readLine())){
                out.println(numberCorrect);
                out.flush();
                //Calculate the score and update the clients test statistics
                double x = ((double)numberCorrect/(double)loadedTestAnswers.size())*100;
                txtParser.updateStatsFile(ESUUserNameHash, nameOfTheTest, x);
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("There was an error validating the session ID for Client ID# " + ID + "\n" + e.getMessage());
        }
    }

    //Function to load the clients test statistics
    private void loadTestStatistics(){
        String testName;
        try{
            testName = in.readLine();
            //Load the test stats
            txtParser.loadTestStats(ESUUserNameHash, testName);
            //Make access of the test stats easier
            for(int x = 0; x < txtParser.getTestStats().size(); x++){
                loadedTestStats.add(txtParser.getTestStats().get(x));
            }
            try{
                //Send how many times the client took the test
                out.println(loadedTestStats.size());
                out.flush();
                //Send all the scores
                for(int x = 0; x < loadedTestStats.size(); x++){
                    out.println(loadedTestStats.get(x));
                    out.flush();
                }
                //Perform some cleanup
                nameOfTheTest = testName;
                txtParser.cleanUp();
                cleanUp();
            }catch(Exception e){
                //Handle the error
                logger.logError("Error sending the test statistics to Client ID# " + ID + "\n" + e.getMessage());
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("Error " + e.getMessage());
        }
    }

    //Clean up function to clear all the array lists
    private void cleanUp(){
        loadedTest.clear();
        loadedTestAnswers.clear();
        loadedTestStats.clear();
        txtParser.cleanUp();
    }

    //Return the number of tests that have been sent to the client
    public int getNumberOfTestsServed(){
        return numberOfTestsServed;
    }

    //Return the number of questions that have been sent to the client
    public int getNumberOfQuestionsServed(){
        return numberOfQuestionsServed;
    }

    //Return true if the client connected with and Android Device
    public boolean isAndroidDevice(){
        return isAndroid;
    }
}