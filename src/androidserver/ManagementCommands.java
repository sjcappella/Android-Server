package androidserver;

import java.io.*;
import java.util.ArrayList;

//Class to interpret admin commands
public class ManagementCommands {
    //Variables
    private BufferedReader in;
    private PrintWriter out;
    private int adminID;
    private TXTParser txtParser = new TXTParser();
    private Boolean loginStatus = false;
    private Logger logger = new Logger();

    //Constructor takes BufferedReader, PrintWriter, and the admin ID number
    ManagementCommands(BufferedReader input, PrintWriter output, int managementID){
        in = input;
        out = output;
        adminID = managementID;
    }

    //Function to process command
    void processCommand(String command){
        //Log the admins command
        logger.logInformation("Admin ID# " + adminID + " says " + command);
        //Command to determin admin connection
        if(command.equals("ADMIN_CONNECTION")){
            logger.logInformation("New Admin connection from Admin ID# " + adminID);
        }
        //Command to login the admin
        if(command.equals("LOGIN")){
           login();
        }
        //Command to send back login status to the admin
        if(command.equals("GET_LOGIN_STATUS")){
            sendLoginStatus();
        }
        //Command to send test menu to the admin
        if(command.equals("GET_TEST_MENU")){
            sendTestMenu();
        }
        //Command to add a new test
        if(command.equals("ADD_NEW_TEST")){
            addNewTest();
        }
        //Command to load a selected test
        if(command.equals("LOAD_TEST")){
            loadTest();
        }
        //Command to update a selected test
        if(command.equals("UPDATE_TEST")){
            updateTest();
        }
        //Command to delete a selected test
        if(command.equals("DELETE_TEST")){
            deleteTest();
        }
        //Command to add a new admin
        if(command.equals("ADD_NEW_ADMIN")){
            addNewAdmin();
        }
        //Command to load test statistics
        if(command.equals("LOAD_TEST_STATISTICS")){
            loadTestStatistics();
        }
        //Command to send the log files
        if(command.equals("SEND_LOG_FILES")){
            sendLogFiles();
        }
        //Command to send the status of the server
        if(command.equals("GET_SERVER_STATUS")){
            getServerStatus();
        }
    }

    //Login function
    private void login(){
        //Variables
        String userName;
        String password;
        //Read in admin username and password
        try{
            userName = in.readLine();
            password = in.readLine();
            if(txtParser.validateAdmin(userName, password)){
                logger.logInformation("Login successful for Admin ID# " + adminID);
                loginStatus = true;
            }else{
                logger.logError("Login failed for Admin ID# " + adminID);
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("Error occured when reading in Admin ID# " + adminID + " username and password.\n" + e.getMessage());
        }
    }

    //Function to send the login status to the admin
    private void sendLoginStatus(){
        logger.logAction("Sending login status to Admin ID# " + adminID);
        if(loginStatus){
            //Logged in
            out.println("TRUE");
            out.flush();
        }else{
            //Not logged in
            out.println("FALSE");
            out.flush();
        }
    }

    //Function to send the test menu to the admin
    private void sendTestMenu(){
        //Populate the test menu
        txtParser.populateTestList();
        //Get the number of tests
        int numTests = txtParser.getNumberOfTests();
        logger.logAction("Sending number of Tests - " + numTests + " to Admin ID# " + adminID);
        //Send number of tests
        out.println(numTests);
        out.flush();
        //Create and fill list of tests
        ArrayList<String> listOfTests = new ArrayList<String>();
        logger.logAction("Sending the test to Admin ID# " + adminID);
        for(int x = 0; x < txtParser.getTestArrayList().size(); x++){
            listOfTests.add(txtParser.getTestArrayList().get(x));
            //Send each test
            out.println(listOfTests.get(x));
            out.flush();
        }
    }

    //Function to add a new test
    private void addNewTest(){
        //Declare all the variables
        String question = "", answerA = "", answerB = "", answerC = "", answerD = "", correctAnswer = "";
        //Create an array list of question objects
        ArrayList<Question> testToAdd = new ArrayList<Question>();
        try{
            //Get how many quesitons there will be
            int numberOfQuestions = Integer.parseInt(in.readLine());
            //Get name of test
            String testName = in.readLine();
            //For every question, get each field
            for(int x = 0; x < numberOfQuestions; x++){
                question = in.readLine();
                answerA = in.readLine();
                answerB = in.readLine();
                answerC = in.readLine();
                answerD = in.readLine();
                correctAnswer = in.readLine();
                //Create a new question object
                Question questionObject = new Question(question, answerA, answerB, answerC, answerD, correctAnswer);
                //Add question to the test
                testToAdd.add(questionObject);
            }
            //Add the test through the text parser
            if(txtParser.addTest(testName, testToAdd)){
                //Success
                out.println("TEST_ADD_SUCCESSFUL");
                out.flush();
                logger.logInformation("Adding new test for Admin ID# " + adminID + " was successful.");
            }else{
                //Fail
                out.println("TEST_ADD_FAILED");
                out.flush();
                logger.logError("Adding new test for Admin ID# " + adminID + " failed.");
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("There was an error in reading new test data from Admin ID# " + adminID + "\n" + e.getMessage());
        }
    }

    //Function to load a selected test
    private void loadTest(){
        //name of the test
        String testName;
        try{
            //Get the admin selected test by neame
            testName = in.readLine();
            //Remove all spaces from the test name to get the file
            txtParser.loadTest(testName.replaceAll(" ", ""));
            //Error if the test is empty
            if(txtParser.getLoadedTest().isEmpty()){
                out.println("ERROR");
                out.flush();
            }else{
                //Sending test size
                out.println(txtParser.getLoadedTest().size());
                out.flush();
                //Sending all the fields of each question
                for(int x = 0; x < txtParser.getLoadedTest().size(); x++){
                    out.println(txtParser.getLoadedTest().get(x).getQuestion());
                    out.flush();
                    out.println(txtParser.getLoadedTest().get(x).getAnswerA());
                    out.flush();
                    out.println(txtParser.getLoadedTest().get(x).getAnswerB());
                    out.flush();
                    out.println(txtParser.getLoadedTest().get(x).getAnswerC());
                    out.flush();
                    out.println(txtParser.getLoadedTest().get(x).getAnswerD());
                    out.flush();
                    out.println(txtParser.getLoadedTest().get(x).getCorrectAnswer());
                    out.flush();
                }
                //Clean up the text parser afterwards
                txtParser.cleanUp();
            }
        }catch(Exception e){
            //Handle the error
            out.println("ERROR");
            out.flush();
            logger.logError("There was an error reading the test data for Admin ID# " + adminID + "\n" + e.getMessage());
        }
    }

    //Funciton to update an edited test
    private void updateTest(){
        //Declaring all the variables
        String question = "", answerA = "", answerB = "", answerC = "", answerD = "", correctAnswer = "";
        //Array list of question objects
        ArrayList<Question> testToUpdate = new ArrayList<Question>();
        try{
            //Get number of questions in the test
            int numberOfQuestions = Integer.parseInt(in.readLine());
            //Get the name of the test
            String testName = in.readLine();
            //Read in all the question details
            for(int x = 0; x < numberOfQuestions; x++){
                question = in.readLine();
                answerA = in.readLine();
                answerB = in.readLine();
                answerC = in.readLine();
                answerD = in.readLine();
                correctAnswer = in.readLine();
                //Create new question object
                Question questionObject = new Question(question, answerA, answerB, answerC, answerD, correctAnswer);
                //Add question to test
                testToUpdate.add(questionObject);
            }
            //Update the test through the text parser
            if(txtParser.updateTest(testName, testToUpdate)){
                //Success
                out.println("TEST_UPDATE_SUCCESSFUL");
                out.flush();
                logger.logInformation("Updating the test for Admin ID# " + adminID + " was successful.");
            }else{
                //Fail
                out.println("TEST_UPDATE_FAILED");
                out.flush();
                logger.logError("Updating the test for Admin ID# " + adminID + " failed.");
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("There was an error reading the test data for Admin ID# " + adminID + "\n" + e.getMessage());
        }
    }

    //Function to delete selected test
    private void deleteTest(){
        try{
            //Read in the test name
            //Pass to the text parser and will return true if successful
            if(txtParser.deleteTest(in.readLine())){
                //Success
                out.println("DELETE_TEST_SUCCESSFUL");
                out.flush();
                logger.logInformation("Deleting the test for Admin ID# " + adminID + " was successful.");
            }else{
                //Failure
                out.println("DELETE_TEST_UNSUCCESSFUL");
                out.flush();
                logger.logError("Deleting the test for Admin ID# " + adminID + " failed.");
            }
        }catch(Exception e){
            out.println("DELETE_TEST_UNSUCCESSFUL");
            out.flush();
            logger.logError("There was an error reading the test data for Admin ID# " + adminID + "\n" + e.getMessage());
        }
    }

    //Function to add a new admin to the server
    private void addNewAdmin(){
        //Variables for admin username and password
        String adminUserName;
        String adminPassword;
        try{
            //Read in admin username and password
            adminUserName = in.readLine();
            adminPassword = in.readLine();
            //Pass credentials to the text parse, will return true if successful in adding new admin
            if(txtParser.addNewAdmin(adminUserName, adminPassword)){
                //Success
                out.println("ADD_NEW_ADMIN_SUCCESSFUL");
                out.flush();
                logger.logInformation("Successfully added the new Admin '" + adminUserName +"'");
            }else{
                //Failure
                out.println("ADD_NEW_ADMIN_UNSUCCESSFUL");
                out.flush();
                logger.logError("Failed to add new Admin.");
            }
        }catch(Exception e){
            //Handle the error
            out.println("ADD_NEW_ADMIN_UNSUCCESSFUL");
            out.flush();
            logger.logError("There was an error reading the admin data for Admin ID# " + adminID + "\n" + e.getMessage());
        }
    }

    //Function to load the test statistics
    private void loadTestStatistics(){
        try{
            //Get the test name
            String testName = in.readLine();
            //Tell the text parser to load all the test scores for selected test
            txtParser.loadAllTestScores(testName);
            //Send number of scores
            out.println(txtParser.getAllTestScores().size());
            out.flush();
            //Send all the scores
            for(int x = 0; x < txtParser.getAllTestScores().size(); x++){
                out.println(txtParser.getAllTestScores().get(x));
                out.flush();
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("There was an error reading the data for Admin ID# " + adminID + "\n" + e.getMessage());
        }
    }

    //Function to send log files to the admin
    private void sendLogFiles(){
        //Text parser loads the log files
        txtParser.loadLogFiles();
        //Send the number of lines
        out.println(txtParser.getLogFiles().size());
        out.flush();
        //Send the loaded lines
        for(int x = 0; x < txtParser.getLogFiles().size(); x++){
            out.println(txtParser.getLogFiles().get(x));
            out.flush();
        }
    }

    //Function to get the status of the server for the admin
    private void getServerStatus(){
        //Text parser loads all the active connection
        txtParser.loadActiveConnections();
        //Send the number of active connections
        out.println(txtParser.getActiveConnections().size());
        out.flush();
        //Send all the connections
        for(int x = 0; x <txtParser.getActiveConnections().size(); x++){
            out.println(txtParser.getActiveConnections().get(x));
            out.flush();
        }
    }

    //Function to return the login status of the admin
    public boolean getLoginStatus(){
        return loginStatus;
    }

}