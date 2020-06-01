package tictactoe;

import io.sockit.servertools.BasicWebHandler;
import io.sockit.sockitserver.LevelDbStore;
import io.sockit.sockitserver.Server;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception{
        File webRootFolder=new File(new File(Main.class.getResource("Main.class").toURI()).getParentFile(),"web");        
        //set the Server database
        Server.setDataStore(new LevelDbStore("../gameDB"));
        //allow Login to be Combined with Register User 
        Server.setCombineLoginWithRegisterUser(true);
        //Register the TicTacToe game
        Server.registerGame(new TicTacToeGame());
        //Set the server's web handler
        Server.addWebHandler(".*",new BasicWebHandler(webRootFolder));
        //start the server without ssl on the default http port
        Server.startServerAsHttp(0); 
        Server.logToConsole("type localhost in your browser's address bar to run the web client");
    }
}
