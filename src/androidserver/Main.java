package androidserver;

//Main class
public class Main {

    //Main function
    public static void main(String[] args) throws Exception {
        //Declare new server object and tell it what ports to listen on
        StartServer androidServer = new StartServer(9041, 9045);
        //Start the server
        androidServer.run();
    }
}