/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.tictactoe.test;

import io.sockit.servertools.BasicWebHandler;
import io.sockit.sockitserver.LevelDbStore;
import io.sockit.sockitserver.Server;
import javax.json.JsonObject;
import io.sockit.gameclient.Client;
import io.sockit.gameclient.ClientEventAdapter;
import io.sockit.gameclient.Player;
import io.sockit.gameclient.Room;
import io.sockit.gameclient.RoomType;
import io.sockit.gameclient.RoomInfo;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tictactoe.Main;
import tictactoe.TicTacToeGame;

/**
 *
 * @author test
 */
public class TestServer extends ClientEventAdapter { 
    
    List<RoomInfo> roomList;
    
    public TestServer() {
    }
    
    @BeforeAll
    public static void setUpClass() throws Exception {
        File webRootFolder=new File(new File(Main.class.getResource("Main.class").toURI()).getParentFile(),"web");        
        //Set Database
        Server.setDataStore(new LevelDbStore("../gameDB"));
        Server.registerGame(new TicTacToeGame());
        Server.setInitialUsersCacheSize(2000);
        Server.setCombineLoginWithRegisterUser(true);
        Server.addWebHandler("*",".*",new BasicWebHandler(webRootFolder));
        Server.startServerAsHttp(0);
    }
    
    @AfterAll
    public static void tearDownClass() {
        Server.stopServer();
    }

    @Test
    public void testGamePlayStart(){
        Client client = new Client("ws://localhost");
        client.setClientEventListener(this);
        client.registerWithEmailId("a@a.com", "123", "Rohan", "TicTacToe");
        
        try{ Thread.sleep(200);}catch(Exception ex) {};
        client.getRooms("Mumbai", RoomType.normal);
        
        try{ Thread.sleep(200);}catch(Exception ex) {};
        assert(roomList!=null && roomList.size()>0); 
        client.joinRoom(roomList.get(0).roomId);
        
        try{ Thread.sleep(200);}catch(Exception ex) {};
        assert(client.hasJoinedRoom());
        
        try{ Thread.sleep(200);}catch(Exception ex) {};
        Room joinedRoom=client.getJoinedRoom();
        client.takeSeat(joinedRoom.roomId, joinedRoom.getFreeSeatNo(), null);
        try{ Thread.sleep(200);}catch(Exception ex) {};
        assert(client.isSeated());
        
        Client client2 = new Client("ws://localhost");
        client2.setClientEventListener(this);
        client2.registerWithEmailId("c@cd.com", "123", "Ashok", "TicTacToe");
        
        try{ Thread.sleep(200);}catch(Exception ex) {};
        client2.takeSeat(joinedRoom.roomId, joinedRoom.getFreeSeatNo(), null);
        
        try{ Thread.sleep(200);}catch(Exception ex) {};
        assert(client.isGamePlayInProgress());
        assert(client.isCurTurn());
        assert(!client2.isCurTurn());
        try{ Thread.sleep(4000);}catch(Exception ex) {};
        
        client.logOut();
        client2.logOut();
    }

    @Override
    public void onGetRooms(Client client, String gameName, String location, RoomType roomtype, List<RoomInfo> rooms) {        
        roomList=rooms;
    }

    @Override
    public void onNextTurn(Client client, Player player, JsonObject jo, Room room, boolean bln) {
        if(client.isCurTurn())
            Server.logToConsole("next turn " + player.getName());
    }

    @Override
    public void onLoggedIn(Client client, boolean isGameSelected) {
        client.getRooms( "Mumbai", RoomType.normal);
    }
}
