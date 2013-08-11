package androidserver;

import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

//Logging class
public class Logger {
    //Declaring variables
    private String pathToLogFile = "../logFile.txt";
    private BufferedWriter out; 
    private Calendar calendar;
    private DateFormat dateFormat;

    //Constructor
    Logger(){
        try{
            //Tell the logger where the logfile is and how to format a time stamp
            out = new BufferedWriter(new FileWriter(pathToLogFile, true));
            calendar = Calendar.getInstance();
            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        }catch(Exception e){
            //Handle the error
        }
    }

    //Function to log an error message
    public void logError(String message){
        try{
            out = new BufferedWriter(new FileWriter(pathToLogFile, true));
            out.write("An error has occured! " + dateFormat.format(calendar.getTime()));
            out.newLine();
            out.write(message);
            out.newLine();
            out.newLine();
            out.close();
        }catch(Exception e){
            //Handle the error
        }   
    }

    //Function to log and information message
    public void logInformation(String message){
        try{
            out = new BufferedWriter(new FileWriter(pathToLogFile, true));
            out.write("The following information was logged by the server: " + dateFormat.format(calendar.getTime()));
            out.newLine();
            out.write(message);
            out.newLine();
            out.newLine();
            out.close();
        }catch(Exception e){
            //Handle the error
        }
    }

    //Function to log an action message
    public void logAction(String message){
        try{
            out = new BufferedWriter(new FileWriter(pathToLogFile, true));
            out.write("The following action was performed: " + dateFormat.format(calendar.getTime()));
            out.newLine();
            out.write(message);
            out.newLine();
            out.newLine();
            out.close();
        }catch(Exception e){
            //Handle the error
        }
    } 
}
