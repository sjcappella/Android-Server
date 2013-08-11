package androidserver;

import java.io.*;
import java.util.*;
import java.text.*;

//Text parser class that handles all the file IO
public class TXTParser {
    //File paths, variables, and attributes
    private String managementFilePath = "../listOfAdmins.txt";
    private String clientFilePath = "../listOfClients.txt";
    private String tests = "../Tests/";
    private String listOfTests = "../listOfTests.txt";
    private int numTests = 0;
    private ArrayList<String> listOfTestsArrayList = new ArrayList<String>();
    private ArrayList<Question> test = new ArrayList<Question>();
    private ArrayList<String> scores = new ArrayList<String>();
    private ArrayList<String> allTestScores = new ArrayList<String>();
    private ArrayList<String> logFile = new ArrayList<String>();
    private ArrayList<String> activeConnections = new ArrayList<String>();
    private Logger logger = new Logger();

    //Constructor
    TXTParser(){}

    //Function to validate an admin log in. Will return true if found
    boolean validateAdmin(String name, String password){
        String lineIn;
        String signature = "";
        boolean defaultValue = false;
        try{
            //Create the signature
            signature = SimpleMD5.MD5(name) + ":" + SimpleMD5.MD5(password);
            logger.logAction("Looking for " + signature);
        }catch(Exception e){
            logger.logError("Failed to generate Admin Signature.\n" + e.getMessage());
            return false;
        }
        try{
            logger.logAction("Trying to look for Admin.");
            BufferedReader reader = new BufferedReader(new FileReader(managementFilePath));
            while ((lineIn = reader.readLine()) != null){
                //If true, found the Admin signature
                if(signature.equals(lineIn)){
                    return true;
                }
            }
        }catch(Exception e){
            logger.logError("Error reading Admin File.\n" + e.getMessage());
            return false;
        }
        return defaultValue;  
    }

    //Function to add a new admin. Will return true if successful
    boolean addNewAdmin(String adminUserName, String adminPassword){
        try{
            //Generate Admin signature
            String adminSignature = SimpleMD5.MD5(adminUserName) + ":" + SimpleMD5.MD5(adminPassword);
            FileInputStream fStream = new FileInputStream(managementFilePath);
            DataInputStream dataIn = new DataInputStream(fStream);
            BufferedReader bReader = new BufferedReader(new InputStreamReader(dataIn));
            String readFromFile;
            boolean adminRegistered = false;
            while((readFromFile = bReader.readLine()) != null){
                //If found, the admin is already registered
                if(readFromFile.equals(adminSignature)){
                    logger.logError("Admin is already registered.");
                    adminRegistered = true;
                }
            }
            //If not already registered, append the admin signature
            if(!adminRegistered){
                //Write admin to the file
                PrintWriter appendAdmin = new PrintWriter(new FileOutputStream(managementFilePath, true), true);
                appendAdmin.println(adminSignature);
                appendAdmin.flush();
                appendAdmin.close();
                logger.logAction("Added admin to list.");
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
            //Handle the error
            logger.logError(e.getMessage());
            return false;
        }
    }

    //Add the new student to the list of registered student
    //Return false if there is an error or the student is already in the list
    boolean addNewStudent(String name, String ESUUserName, String password, String deviceUID){
        try{
            //Generate the student signature
            String studentEntry = SimpleMD5.MD5(name) + ":" + SimpleMD5.MD5(ESUUserName) + ":" + SimpleMD5.MD5(password) + ":" + SimpleMD5.MD5(deviceUID);
            //Read in the file of registered clients
            try{
                FileInputStream fStream = new FileInputStream(clientFilePath);
                DataInputStream dataIn = new DataInputStream(fStream);
                BufferedReader bReader = new BufferedReader(new InputStreamReader(dataIn));
                String readFromFile;
                boolean studentRegistered = false;
                //Parsing the input strings looking for ESU Username
                String tmp1 = studentEntry.substring(66, ((studentEntry.length()-33)));
                String tmp2 = "";
                while((readFromFile = bReader.readLine()) != null){
                    tmp2 = readFromFile.substring(66, ((readFromFile.length()-33)));
                    //If its a match than the user name is already registered
                    if(tmp1.equals(tmp2)){
                        logger.logError("Student is already registered.");
                        studentRegistered = true;
                    }
                }
                //If the client is not already registered, add them to the file
                if(!studentRegistered){
                    //Write student to the file
                    PrintWriter appendStudent = new PrintWriter(new FileOutputStream(clientFilePath, true), true);
                    appendStudent.println(studentEntry);
                    appendStudent.flush();
                    appendStudent.close();
                    createStatsFile(ESUUserName);
                    logger.logAction("Added student to list.");
                    return true;
                }else{
                    return false;
                }

            }catch(Exception e){
                logger.logError("An error occured: " + e.getMessage());
                return false;
            }
        }catch(Exception e){
            logger.logError("Failed to generate student entry.\n" + e.getMessage());
            return false;
        }
    }

    //Function to validate a student client
    public boolean validateStudent(String name, String ESUUserName, String password, String deviceUID){
        try{
            String studentEntry = SimpleMD5.MD5(name) + ":" + SimpleMD5.MD5(ESUUserName) + ":" + SimpleMD5.MD5(password) + ":" + SimpleMD5.MD5(deviceUID);
            logger.logAction("Signature generated for " + name + ".");
            //Trying to find the student in the file
            try{
                FileInputStream fStream = new FileInputStream(clientFilePath);
                DataInputStream dataIn = new DataInputStream(fStream);
                BufferedReader bReader = new BufferedReader(new InputStreamReader(dataIn));
                String readFromFile;

                //Looking for the student's user name and password to validate
                String tmp1 = studentEntry.substring(0, ((studentEntry.length()-32)));
                String tmp2 = "";
                while((readFromFile = bReader.readLine()) != null){
                    //Check every line in the file for the student's signature
                    tmp2 = readFromFile.substring(0, ((readFromFile.length()-32)));
                    if(tmp1.equals(tmp2)){
                        //Found the student in the file
                        logger.logAction("Found student.");
                        return true;
                    }
                }
            }catch(Exception e){
                //Handle the error
                logger.logError("Couldn't read the student file.\n" + e.getMessage());
                return false;
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("Couldn't generate student signatures.\n" + e.getMessage());
            return false;
        }
        return false;
    }

    //Function to populate the test list
    public void populateTestList(){
        //Clear the test list of any old tests and reset the count
        listOfTestsArrayList.clear();
        numTests = 0;
        try{
            //Try to read in the test names
            FileInputStream fStream = new FileInputStream(listOfTests);
            DataInputStream dataIn = new DataInputStream(fStream);
            BufferedReader bReader = new BufferedReader(new InputStreamReader(dataIn));
            String readFromFile;
            logger.logAction("Reading in the Test List.");
            while((readFromFile = bReader.readLine()) != null){
                //Add the name of the test to the list and increment the count
                listOfTestsArrayList.add(readFromFile);
                numTests++;
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("An error occured while trying to read in the List of Tests.\n" + e.getMessage());
        }
    }

    //Function to return the number of tests
    public int getNumberOfTests(){
        return numTests;
    }

    //Function to return the array list of test names
    public ArrayList<String> getTestArrayList(){
        return listOfTestsArrayList;
    }

    //Function to load a user selected test
    public void loadTest(String testName){
        //Clear the old test out and define the pathname for the test
        test.clear();
        String testPath = tests + testName + ".txt";
        //Read in the selected test from file
        try{
            FileInputStream fStream = new FileInputStream(testPath);
            DataInputStream dataIn = new DataInputStream(fStream);
            BufferedReader bReader = new BufferedReader(new InputStreamReader(dataIn));
            String readFromFile;
            logger.logAction("Loading test.");
            int x = 0;
            //Declare question attributes
            String question = "", answerA = "", answerB = "", answerC = "", answerD = "", correctAnswer = "";
            //Read in the test questions until there aren't anymore
            while((readFromFile = bReader.readLine()) != null){
                if(x%6 == 0){
                    question = readFromFile;
                }
                if(x%6 == 1){
                    answerA = readFromFile;
                }
                if(x%6 == 2){
                    answerB = readFromFile;
                }
                if(x%6 == 3){
                    answerC = readFromFile;
                }
                if(x%6 == 4){
                    answerD = readFromFile;
                }
                if(x%6 == 5){
                    correctAnswer = readFromFile;
                    //Create question object with new attribute values
                    Question testQuestion = new Question(question, answerA, answerB, answerC, answerD, correctAnswer);
                    //Add the new question object to the test
                    test.add(testQuestion);
                }
                x++;
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("Couldn't find the test.\n" + e.getMessage());
        }
    }

    //return the loaded test array list with all the question objects
    public ArrayList<Question> getLoadedTest(){
        return test;
    }

    //Function to clean up all the loaded array lists
    public void cleanUp(){
        numTests = 0;
        listOfTestsArrayList.clear();
        test.clear();
        scores.clear();
        allTestScores.clear();
        logFile.clear();
        activeConnections.clear();
    }

    //Function to create a new stats file
    private void createStatsFile(String ESUUserName){
        //This function is called when a new student is registered with the server for the first time
        try{
            //Create the file name based of MD5 of unique user name
            ESUUserName = SimpleMD5.MD5(ESUUserName);
            String filePath = "../Statistics/" + ESUUserName + ".txt";
            File file = new File(filePath);
            if(file.exists()){
                logger.logError("User statistics file already exists for " + ESUUserName + ". Can not create the file.");
            }else{
                try{
                    //If the filed doesn't exists, create it
                    file.createNewFile();
                    logger.logAction("Created statistics file for " + ESUUserName +".");
                }catch(Exception e){
                    //Handle the error
                    logger.logError("Couldn't create statistics file for " + ESUUserName + ".\n" + e.getMessage());
                }
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("Couldn't generate file name for statistics file.\n" + e.getMessage());
        }
    }

    //Function to load the stats of a specific test for a user
    public void loadTestStats(String userNameHash, String testName){
        //Creating file path
        String filePath = "../Statistics/" + userNameHash + ".txt";
        String readFromFile;
        try{
            //Read in the file
            FileInputStream fStream = new FileInputStream(filePath);
            DataInputStream dataIn = new DataInputStream(fStream);
            BufferedReader bReader = new BufferedReader(new InputStreamReader(dataIn));
            //Parse the data
             while((readFromFile = bReader.readLine()) != null){
                 if(readFromFile.contains(testName)){
                     logger.logAction("Found the test statistics in the .txt file.");
                     //Variables needed to parse data
                     int positionOfScores = readFromFile.lastIndexOf("$");
                     int lengthOfString = readFromFile.length();
                     int numberOfScores = ((lengthOfString+1) - positionOfScores)/4;
                     String score;
                     positionOfScores += 1;
                     //Parsing through the 'score string'
                     for(int x = 0; x < numberOfScores; x++){
                         score = readFromFile.substring(positionOfScores, positionOfScores+3);
                         scores.add(score);
                         positionOfScores += 4;
                     }
                 }
             }
        }catch(Exception e){
            //Handle the error
            logger.logError("Couldn't file statistics file.\n" + e.getMessage());
        }
    }

    //Function to update the stats file after the user takes a test
    public void updateStatsFile(String userNameHash, String testName, double score){
        //Need a format to format the new score
        DecimalFormat f = new DecimalFormat("###.##");
        //Create a temporary file to read in the old statistics file
        ArrayList<String> temporaryFile = new ArrayList<String>();
        //Generate file path
        String filePath = "../Statistics/" + userNameHash + ".txt";
        String readFromFile;
        //Create and format score to append
        String appendScore = f.format(score);
        boolean testPreviouslyTaken = false;

        if(appendScore.length() == 1){
            appendScore = "00" + appendScore;
        }
        if(appendScore.length() == 2){
            appendScore = "0" + appendScore;
        }

        try{
            //Reading in the data from the original statistics file
            FileInputStream fStream = new FileInputStream(filePath);
            DataInputStream dataIn = new DataInputStream(fStream);
            BufferedReader bReader = new BufferedReader(new InputStreamReader(dataIn));
            //Parse the data
            while((readFromFile = bReader.readLine()) != null){
                //Add the current line to the temp file
                temporaryFile.add(readFromFile);    
            }

            //If the temporary file is not empty, we need to find the specific test
            if(!temporaryFile.isEmpty()){
                for(int x = 0; x < temporaryFile.size(); x++){
                    //If we find this substring, then we know the test has been previously taken
                    if(temporaryFile.get(x).substring(5, (5+testName.length())).equals(testName)){
                        //Append the new score to the current list of scores for that test
                        temporaryFile.set(x, temporaryFile.get(x) + appendScore + ",");
                        //Previously taken is true
                        testPreviouslyTaken = true;
                    }
                }

                //If the test has not been previously taken, we can just append the statistics
                //string to the bottom of the statistics file
                if(!testPreviouslyTaken){
                    //Add new score
                    temporaryFile.add("test:" + testName + "::scores$" + appendScore + ",");
                }
                logger.logAction("Added new score to the statistics file.");
                //Create a new file with the same name
                File file = new File(filePath);
                //Delete the old file
                file.delete();
                //Create the new file
                File newFile = new File(filePath);
                newFile.createNewFile();
                //Write the temporary file to the newly created empty file
                BufferedWriter out = new BufferedWriter(new FileWriter(filePath, true));
                for(int x = 0; x < temporaryFile.size(); x++){
                    out.write(temporaryFile.get(x));
                    out.newLine();
                }
                out.close();
                logger.logAction("Appending and creating statistics file finished.");
            }else{
                //If this is the first time taking a test, still creating a
                //new file, while deleting the old one, and appending new score
                File file = new File(filePath);
                file.delete();
                File newFile = new File(filePath);
                newFile.createNewFile();
                BufferedWriter out = new BufferedWriter(new FileWriter(filePath, true));
                out.write("test:" + testName + "::scores$" + appendScore + ",");
                out.newLine();
                out.close();
                logger.logAction("Appending and creating statistics file finshed.");
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("Couldn't append statistics.\n" + e.getMessage());
        }
    }

    //Function to add a new test to the test list. Returns true is adding test was successful
    public boolean addTest(String nameOfTest, ArrayList<Question> test){
        //Formatting the test name for the file path
        String testName = nameOfTest.replaceAll(" ", "");
        try{
            //Creating a new file for the new test
            String filePath = tests + testName + ".txt";
            File file = new File(filePath);
            //If test already exists, return false
            if(file.exists()){
                logger.logError("Test already exists.");
                return false;
            }else{
                try{
                    //If test doesn't exist, create it
                    file.createNewFile();
                    //Writing the test to the newly created file
                    BufferedWriter out = new BufferedWriter(new FileWriter(filePath, true));
                    for(int x = 0; x < test.size(); x++){
                        out.write(test.get(x).getQuestion());
                        out.newLine();
                        out.write(test.get(x).getAnswerA());
                        out.newLine();
                        out.write(test.get(x).getAnswerB());
                        out.newLine();
                        out.write(test.get(x).getAnswerC());
                        out.newLine();
                        out.write(test.get(x).getAnswerD());
                        out.newLine();
                        out.write(test.get(x).getCorrectAnswer());
                        out.newLine();
                    }
                    out.close();
                    //Once we create the actual test file, we need to update the list of tests file to show
                    //the serve now has a new test available
                    BufferedWriter appendListOfTests = new BufferedWriter(new FileWriter(listOfTests, true));
                    appendListOfTests.write(nameOfTest);
                    appendListOfTests.newLine();
                    appendListOfTests.close();
                    logger.logAction("Successfully added the " + nameOfTest + " test.");
                    //Return successfully
                    return true;
                }catch(Exception e){
                    //Handle the error
                    logger.logError("An error occured while writing the new test to the file.\n" + e.getMessage());
                    return false;
                }
            }
        }catch(Exception e){
            //Handle the error
            logger.logError("An error occured while looking for the test file.\n" + e.getMessage());
            return false;
        }
    }

    //Function to update a test after admin edits it. Returns true if succesful
    public boolean updateTest(String nameOfTest, ArrayList<Question> test){
        //Format the test name to its file path
        String filePath = tests + nameOfTest.replaceAll(" ", "") + ".txt";
        //Find the old test, delete it, recreate the empty file
        File file = new File(filePath);
        file.delete();
        File newFile = new File(filePath);
        //Write out the new test to the new file
        try{
            newFile.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath, true));
            for(int x = 0; x < test.size(); x++){
                out.write(test.get(x).getQuestion());
                out.newLine();
                out.write(test.get(x).getAnswerA());
                out.newLine();
                out.write(test.get(x).getAnswerB());
                out.newLine();
                out.write(test.get(x).getAnswerC());
                out.newLine();
                out.write(test.get(x).getAnswerD());
                out.newLine();
                out.write(test.get(x).getCorrectAnswer());
                out.newLine();
            }
            out.close();
            logger.logAction("Editted the test " + nameOfTest + ".");
        }catch(Exception e){
            //Failure
            logger.logError("An error occured in attempting to update the test " + nameOfTest + ".\n" + e.getMessage());
            return false;
        }
        //Success
        return true;
    }

    //Function to delete an admin selected test. Return true if successful
    public boolean deleteTest(String testName){
        ArrayList<String> listOfTestsFile = new ArrayList<String>();
        try{
            //Read in the list of tests
            FileInputStream fStream = new FileInputStream(listOfTests);
            DataInputStream dataIn = new DataInputStream(fStream);
            BufferedReader bReader = new BufferedReader(new InputStreamReader(dataIn));
            String readFromFile;
            while((readFromFile = bReader.readLine()) != null){
                //Load the file into temporary file
                listOfTestsFile.add(readFromFile);
            }
            logger.logAction("Read in the list of tests file.");
            
            //Delete the list of tests file
            File file = new File(listOfTests);
            file.delete();
            //Create a new, empty file
            File newFile = new File(listOfTests);
            try{
                newFile.createNewFile();
                logger.logAction("Created new list of tests file.");
                BufferedWriter out = new BufferedWriter(new FileWriter(listOfTests, true));
                //Write the temporary file to the new file
                for(int x = 0; x < listOfTestsFile.size(); x++){
                    if(!listOfTestsFile.get(x).equals(testName)){
                        out.write(listOfTestsFile.get(x));
                        out.newLine();
                    }
                }
                out.close();
                logger.logAction("Wrote the new list of tets file.");
            }catch(Exception e){
                //Error occured
                logger.logError("Error occured trying to create new file.\n" + e.getMessage());
                //Return failure
                return false;
            }
            //Delete the actual file containing the test
            File testFile = new File(tests + testName.replaceAll(" ", "") + ".txt");
            testFile.delete();
            logger.logAction("Deleted old test file.");
            //Return success
            return true;

        }catch(Exception e){
            //Error occured
            logger.logError("Error occured reading in the test file.\n" + e.getMessage());
            //Return failure
            return false;
        }
    }

    //Function to load all of the scores from all users for a specific test
    public void loadAllTestScores(String nameOfTest){
        //Clear all the old scores
        allTestScores.clear();
        //Point to the statistiscs directory and fill the array with all the
        //files in that directory (all the students)
        File testDirectory = new File("../Statistics");
        File listOfFiles[] = testDirectory.listFiles();
        //Iterate through all the files
        for(int x= 0; x < listOfFiles.length; x++){
             try{
                 //Read in data from all the files
                 FileInputStream fStream = new FileInputStream("../Statistics/" + listOfFiles[x].getName());
                 DataInputStream dataIn = new DataInputStream(fStream);
                 BufferedReader bReader = new BufferedReader(new InputStreamReader(dataIn));
                 String readFromFile;
                 //Read until end of file
                 while((readFromFile = bReader.readLine()) != null){
                     //Check to see if student has taken the test we are looking for
                     if(readFromFile.contains(nameOfTest)){
                          int positionOfScores = readFromFile.lastIndexOf("$");
                          int lengthOfString = readFromFile.length();
                          int numberOfScores = ((lengthOfString+1) - positionOfScores)/4;
                          String score;
                          positionOfScores += 1;
                          //Loop to get all their scores on the selected test
                          for(int y = 0; y < numberOfScores; y++){
                              //Append their score to the scores list
                              score = readFromFile.substring(positionOfScores, positionOfScores+3);
                              allTestScores.add(score);
                              positionOfScores += 4;
                          }
                     }
                 }
              logger.logAction("Loaded all the test scores for " + nameOfTest + ".");
              }catch(Exception e){
                  //Handle the error
                  logger.logError(("An error occured trying to read a file.\n" + e.getMessage()));
            }
        }
    }

    //Function to load all the log files
    public void loadLogFiles(){
        //Clear all the old log files
        logFile.clear();
        try{
            //Find the file path and reader
            FileInputStream fStream = new FileInputStream("../logFile.txt");
            DataInputStream dataIn = new DataInputStream(fStream);
            BufferedReader bReader = new BufferedReader(new InputStreamReader(dataIn));
            String readFromFile;
            //Read in all the log file lines
            while((readFromFile = bReader.readLine()) != null){
                logFile.add(readFromFile);
            }
            logger.logAction("Loaded log files.");
        }catch(Exception e){
            //Handle the error
            logger.logError("Error occured in loading log files.\n" + e.getMessage());
        }
    }

    //Function to load all the active connections to the server
    public void loadActiveConnections(){
        //Clear all the old connections
        activeConnections.clear();
        try{
            //Find the file and reader
            FileInputStream fStream = new FileInputStream("../serverStatus.txt");
            DataInputStream dataIn = new DataInputStream(fStream);
            BufferedReader bReader = new BufferedReader(new InputStreamReader(dataIn));
            String readFromFile;
            //Read in all the connections until there aren't any more
            while((readFromFile = bReader.readLine()) != null){
                //Only add the connections that are active
                if(readFromFile.contains("ACTIVE")){
                    activeConnections.add(readFromFile);
                }
            }
            logger.logAction("Loaded active connections.");
        }catch(Exception e){
            //Handle the error
            logger.logError("An error occured in loading the active connections.\n" + e.getMessage());
        }
    }

    //Function to return all of the test statistics
    public ArrayList<String> getTestStats(){
        return scores;
    }

    //Function to return all of the test scores
    public ArrayList<String> getAllTestScores(){
        return allTestScores;
    }

    //Function to return all of the log files
    public ArrayList<String> getLogFiles(){
        return logFile;
    }

    //Function to return all of the active connections
    public ArrayList<String> getActiveConnections(){
        return activeConnections;
    }
}
