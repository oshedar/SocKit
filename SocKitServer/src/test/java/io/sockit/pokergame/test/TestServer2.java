/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.pokergame.test;

import io.sockit.servertools.Console;
import io.sockit.sockitserver.Server;
import io.sockit.pokergame.PokerGame;
import io.sockit.gameclient.Client;
import io.sockit.gameclient.Player;
import io.sockit.gameclient.Room;
import io.sockit.gameclient.RoomType;
import io.sockit.gameclient.RoomInfo;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import io.sockit.gameclient.ClientEventAdapter;
import io.sockit.gameclient.ServerMessageType;
import io.sockit.sockitserver.LevelDbStore;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import io.sockit.sockitserver.Game;
import io.sockit.sockitserver.JsonUtil;
import io.sockit.sockitserver.bot.BotTurnDelayType;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author h
 */
public class TestServer2 extends ClientEventAdapter{
    
    public TestServer2(){
        
    }
    
    static Game game;
    
    @BeforeAll
    public static void setUpClass() throws Exception {
        PokerGame pokerGame=new PokerGame(1,BotTurnDelayType.none,0);//new PokerGame(5,true);
        pokerGame.disableBots();
        game=pokerGame;
        Server.registerGame(game);
        Server.setInitialUsersCacheSize(2000);
//        new File("../temp/game_server/testdb").delete();
        Server.setCombineLoginWithRegisterUser(true);
        Server.setDataStore(new LevelDbStore("../../testdb"));
        Server.startServerAsHttp(2014,-1,false);   
    }
    
    @AfterAll
    public static void tearDownClass() {
        System.out.println("in teardown1");
        try{Thread.sleep(1000);}catch(Exception ex){}
        System.out.println("in teardown");
        Server.stopServer();
    }

//    @Test
    public void registerUser(){
         Client client=new Client("ws://localhost:2014");
         client.registerWithEmailId("aa@a.com", "123", "test", game.gameName);
         try{ Thread.sleep(500); }catch(InterruptedException ex){}
         assertTrue(client.isLoggedIn());
    }
    
//     @Test
     public void joinRoom(){
         Client client=new Client("ws://localhost:2014");
         client.setClientEventListener(this);
         client.logInWithOtherId("fb123", "hoshi",game.gameName);
         try{ Thread.sleep(500); }catch(InterruptedException ex){}
         assertTrue(client.isLoggedIn());
         client.getRooms("Delhi",io.sockit.gameclient.RoomType.normal);
         try{ Thread.sleep(500); }catch(InterruptedException ex){}         
         assertTrue(this.lastMessageType==ServerMessageType.roomList);
         client.joinRoom(rooms.get(0).roomId);
         try{ Thread.sleep(500); }catch(InterruptedException ex){}
         assertTrue(client.hasJoinedRoom());
     }

//     @Test
     public void takeSeat(){
         Client client=new Client("ws://localhost:2014");
         client.setClientEventListener(this);
         client.logInWithOtherId("fb123", "hoshi",game.gameName);
         try{ Thread.sleep(500); }catch(InterruptedException ex){}
         assertTrue(client.isLoggedIn());
         client.getRooms("Delhi",io.sockit.gameclient.RoomType.normal);
         try{ Thread.sleep(500); }catch(InterruptedException ex){}
         assertTrue(this.lastMessageType==ServerMessageType.roomList);
         client.takeSeat(rooms.get(0).roomId,1,null);
         try{ Thread.sleep(500); }catch(InterruptedException ex){}
         assertTrue(this.lastMessageType==ServerMessageType.seatTaken);
         assertTrue(client.getJoinedRoom().getPlayerCount()>0);
     }

//     @Test
     public void leaveSeatAndLeaveRoom(){
         Client client=new Client("ws://localhost:2014");
         client.setClientEventListener(this);
         client.logInWithOtherId("fb123", "hoshi",game.gameName);
         try{ Thread.sleep(500); }catch(InterruptedException ex){}
         assertTrue(client.isLoggedIn());
         client.getRooms("Delhi",io.sockit.gameclient.RoomType.normal);
         try{ Thread.sleep(500); }catch(InterruptedException ex){}
         assertTrue(this.lastMessageType==ServerMessageType.roomList);
         client.joinRoom(rooms.get(0).roomId);
         try{ Thread.sleep(500); }catch(InterruptedException ex){}
         assertTrue(client.hasJoinedRoom());
         client.takeSeat(client.getJoinedRoom().roomId,1,null);
         try{ Thread.sleep(500); }catch(InterruptedException ex){}
         assertTrue(client.isSeated());
         client.leaveSeat();
         try{ Thread.sleep(500); }catch(InterruptedException ex){}
         assertTrue(!client.isSeated());
         client.leaveRoom();
         try{ Thread.sleep(500); }catch(InterruptedException ex){}
         assertTrue(!client.hasJoinedRoom());
     }

     @Test
     public void startGameAndNextTurn(){
         Client client1=new Client("ws://localhost:2014");
         client1.setClientEventListener(this);
         client1.registerWithEmailId("hoshi3@gmail.com", "123","hoshi3",game.gameName);
         try{ Thread.sleep(200); }catch(InterruptedException ex){}
         client1.getRooms("Delhi",io.sockit.gameclient.RoomType.normal);
         try{ Thread.sleep(200); }catch(InterruptedException ex){}
         client1.joinRoom(rooms.get(0).roomId);
         try{ Thread.sleep(200); }catch(InterruptedException ex){}
         client1.takeSeat(client1.getJoinedRoom().roomId,client1.getJoinedRoom().getFreeSeatNo(),takeSeatData);
         try{ Thread.sleep(200); }catch(InterruptedException ex){}
         assertTrue(client1.isSeated());
         Client client2=new Client("ws://localhost:2014");
//         client2.setClientEventListener(this);
         client2.registerWithEmailId("dilshad@gmail.com", "123","Dilshad",game.gameName);
         try{ Thread.sleep(200); }catch(InterruptedException ex){}
         assertTrue(client2.isLoggedIn());
         client2.takeSeat(client1.getJoinedRoom().roomId, client1.getJoinedRoom().getFreeSeatNo(), takeSeatData);         
         try{ Thread.sleep(200); }catch(InterruptedException ex){}
         assertTrue(client2.isSeated());
         assertTrue(client2.isGamePlayInProgress());
//         Console.log("lastmesg=" + this.lastMessageType.toString());
         assertTrue(this.lastMessageType==ServerMessageType.nextTurn);
         try{ Thread.sleep(200); }catch(InterruptedException ex){}         
     }  
     

     private static JsonObject takeSeatData;
     static{
         takeSeatData=JsonUtil.createObjectBuilder().add("chipsOnTable", 200).build();
     }
     
     @Test
     public void nextRound(){ 
         Client client1=new Client("ws://localhost:2014");
         client1.setClientEventListener(this);
         client1.registerWithEmailId("hoshi3@gmail.com", "123","hoshi3",game.gameName);
         try{ Thread.sleep(150); }catch(InterruptedException ex){}
         assertTrue(client1.isLoggedIn());
         client1.getRooms("Delhi",io.sockit.gameclient.RoomType.normal);
         try{ Thread.sleep(100); }catch(InterruptedException ex){}
         client1.joinRoom(rooms.get(1).roomId);
         try{ Thread.sleep(100); }catch(InterruptedException ex){}
         client1.takeSeat(client1.getJoinedRoom().roomId,client1.getJoinedRoom().getFreeSeatNo(),takeSeatData);
         try{ Thread.sleep(100); }catch(InterruptedException ex){}
         Client client2=new Client("ws://localhost:2014");
         client2.setClientEventListener(this);
         client2.logInWithOtherId("fb124", "Dilshad",game.gameName);         
         try{ Thread.sleep(100); }catch(InterruptedException ex){}
         client2.takeSeat(client1.getJoinedRoom().roomId, client1.getJoinedRoom().getFreeSeatNo(), takeSeatData);         
         try{ Thread.sleep(3000); }catch(InterruptedException ex){ex.printStackTrace();} 
     }
     
//     @Test
     public void loginWithGoogle(){ 
         Client client1=new Client("ws://localhost:2014");
         client1.setClientEventListener(this);
         client1.logInWithGoogle("eyJhbGciOiJSUzI1NiIsImtpZCI6IjVkODg3ZjI2Y2UzMjU3N2M0YjVhOGExZTFhNTJlMTlkMzAxZjgxODEiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiYXpwIjoiNDI0NTMxOTk0OTAtdGpsOTduNzZ2cHU1aXMwM2ttMmg5MjBmazJrZ2ZmOHMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI0MjQ1MzE5OTQ5MC10amw5N243NnZwdTVpczAza20yaDkyMGZrMmtnZmY4cy5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsInN1YiI6IjExMDIxNzU4MDIyODc1NzYyMzgyMyIsImVtYWlsIjoib3NoZWRhckBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6ImY2UW5fNlVpekpIcmg0WGZocy0wWUEiLCJuYW1lIjoiSG9zaGVkYXIgSXJhbmkiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tLy1PTVlLUXBkN2E4TS9BQUFBQUFBQUFBSS9BQUFBQUFBQUFCdy8zWmxTYUxRc3Q3QS9zOTYtYy9waG90by5qcGciLCJnaXZlbl9uYW1lIjoiSG9zaGVkYXIiLCJmYW1pbHlfbmFtZSI6IklyYW5pIiwibG9jYWxlIjoiZW4iLCJpYXQiOjE1NTY1MzAxOTIsImV4cCI6MTU1NjUzMzc5MiwianRpIjoiMWYzZjNlMWNmMzRmZTc0MzdhMjMwYmZmYzZlZWYzNGUzMzE3YjAyYiJ9.lQi6uyDSmSDQASMv5AYIJ8GifwGber3o1TuWpcO7kwfnwPQRtzPn9a_glu0tcKS9n_q_RusJQ8R1JVIa5EaeEt7uvRXDg8nKx_zLiDkboZgsLyPLO0r-uzrw-RXQcZ5un87qP5nG9FwaHnXpW38LNJP5GZJuIJBkgyH74BjGqjNvSHPb0058CTj8oJilBC_sbAQYlU4h1Xbr-j7s75C8_n7BRJ7X3ATXYg01wy6f2Geg_IwB7H_CfP5zmqthG1j21L2jX5JLVw9VX8EDnGTGHmXf8fenMBkL8XD446dZcgKgA7b915W41ThzW2c6zNIO9f8JvAh3vm04xt_drdZSWQ",game.gameName);
         try{ Thread.sleep(150); }catch(InterruptedException ex){}
         assertTrue(client1.isLoggedIn());
         client1.logOut();
         try{ Thread.sleep(100); }catch(InterruptedException ex){}
         if(1==1)
             return;
         client1.getRooms("Delhi",io.sockit.gameclient.RoomType.normal);
         try{ Thread.sleep(100); }catch(InterruptedException ex){}
         client1.joinRoom(rooms.get(0).roomId);
         try{ Thread.sleep(100); }catch(InterruptedException ex){}
         client1.takeSeat(client1.getJoinedRoom().roomId,client1.getJoinedRoom().getFreeSeatNo(),takeSeatData);
         try{ Thread.sleep(100); }catch(InterruptedException ex){}
         Client client2=new Client("ws://localhost:2014");
         client2.setClientEventListener(this);
         client2.logInWithOtherId("fb124", "Dilshad",game.gameName);         
         try{ Thread.sleep(100); }catch(InterruptedException ex){}
         client2.takeSeat(client1.getJoinedRoom().roomId, client1.getJoinedRoom().getFreeSeatNo(), takeSeatData);         
         try{ Thread.sleep(7000); }catch(InterruptedException ex){ex.printStackTrace();} 
     }
     
    private List<RoomInfo> rooms;

    @Override
    public void onLoggedIn(Client client,boolean firstTime) {
        println("client logged in");
    }

    @Override
    public void onGetRooms(Client client, String gameName, String location, RoomType roomtype, List<RoomInfo> rooms) {
//        print("got roomList from server");
        println("" + rooms.size());
        this.rooms=rooms;
        
    }

    @Override
    public void onSeatTaken(Client client, Player playerSeated, Room room,boolean isSelf) {
        if(isDebugClient(client))
            println("seatTaken: " + playerSeated.getName());
    }

    @Override
    public void onGamePlayStarted(Client client, Room room) {
        if(isDebugClient(client)){
            print("game started : ");
            printPlayerHands(room);
            print("  ");
            printTableData(room);
            println("");
        }
    }

    @Override
    public void onNextTurn(Client client, Player turnPlayer, JsonObject turnData, Room room, boolean isSelfTurn) {
        if(isSelfTurn){
            int callValue=JsonUtil.getAsInt(turnData,"callValue");
            int betAmt;
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            if(((int)(Math.random()*3)==2)){                
                betAmt=callValue*2;
                int bigBlind=JsonUtil.getAsInt(room.getData(),"smallBlind")*2;
                if(betAmt<callValue+ bigBlind)
                    betAmt=callValue+bigBlind;
            }
            else
                betAmt=callValue;
            json.add("betAmt", betAmt);
//            try{Thread.sleep(4000);}catch(Exception ex){}
            client.playAction("bet", json.build());
//            Console.log(client.getName() + " bet " + betAmt + ", calvalue=" + callValue);
//            Executor.executeWait(new DelayedPlay(client, data), 4000);
        }
    }

    static class DelayedPlay implements Runnable{
        Client client;
        JsonObject data;

        public DelayedPlay(Client client, JsonObject data) {
            this.client = client;
            this.data = data;
        }

        @Override
        public void run() {
            this.client.playAction("bet", data);
        }
        
    }

    @Override
    public void onTurnPlayed(Client client, Player turnPlayer, String playerAction, JsonObject actionData, Room room, boolean isSelf) {        
        if(isDebugClient(client)){
            int amtBet=JsonUtil.getAsInt(actionData, "amtBet");
            print(turnPlayer.getName() + " " + playerAction + ", amtBet=" + amtBet);
            print("\t");
            println("chipsInPot=" + room.getData().get("pots"));
        }
    }

    @Override
    public void onOutOfTurnPlayed(Client client, Player outOfTurnPlayer, String playerAction, JsonObject actionData, Room room, boolean isSelf) {
        if(isDebugClient(client))
            println("out of turn played by " + outOfTurnPlayer.getName());
    }

    @Override
    public void onGameAction(Client client, String gameAction, JsonObject actionData, Room room) {
            if(isDebugClient(client)){
                print(gameAction + "-" + actionData);
                print("\t");
                printTableCards(room);
                println("");
            }
    }

    @Override
    public void onGamePlayEnded(Client client, Room room, JsonObject endGameData) {
        if(isDebugClient(client)){
            JsonArray jsonWinners;
            JsonObject jsonWinner;
            JsonArray jsonPots=JsonUtil.getAsJsonArray(endGameData, "pots");
            for(JsonValue jsonPot:jsonPots){
                print("potValue:"+ JsonUtil.getAsInt((JsonObject)jsonPot, "potValue"));
                print(", winners: ");
                jsonWinners=JsonUtil.getAsJsonArray((JsonObject)jsonPot, "winners");
                for(JsonValue jsonValue:jsonWinners){
                    jsonWinner=(JsonObject)jsonValue;
                    print("{" + jsonWinner.toString() + "} ");
                }
            }
            println("");
        }
    }

    static boolean isDebugClient(Client client){
        return client.getName().equalsIgnoreCase(debugClientName);
    }
    
    private static String debugClientName="hufriz";
    
    static void print(String s){
//        if(1==1) return;
        System.out.print(s);
        System.out.flush();
    }
   
    static void println(String s){
//        if(1==1) return;
        System.out.println(s);
    }
   
    static void printTableData(Room room){
        print( "" + room.getData());
    }
    
    static void printPlayerData(Room room){        
        for(Player player:room)
            print(player.getName() + ":" + player.getData());
    }

    static void printPlayerData(Player player){
        print(player.getName() + ":" + player.getData());
    }
    
   static void printTableCards(Room room){
       print(room.getData().get("tableCards").toString());
   }
   
   static void printPlayerHands(Room room){
        for(Player player:room)
            print(player.getName() + ":" + player.getData().get("hand") + "  ");       
   }
   
   static void printPlayerHand(Player player){
        print(player.getName() + ":" + player.getData().get("hand"));              
   }

    @Override
    public void onConnectionDisconnected(Client client) {
        println("connection disconnected");
    }
   

    public static void main(String[] args) throws Exception{
        setUpClass();
         Client client1=new Client("ws://localhost:2014");
         TestServer2 test2=new TestServer2();
         client1.setClientEventListener(test2);
//         client1.register("hoshi3@gmail.com", "","hoshi3");
         client1.logInWithEmailId("hoshi3", "",game.gameName);
         try{ Thread.sleep(1000*120); }catch(InterruptedException ex){}
        
    }
     int lastErrorCode=-1;
     ServerMessageType lastMessageType;
    @Override
    public void onError(Client client, int errorCode, String errorDesc) {
        Console.log(errorDesc);
        lastErrorCode=errorCode;
    }

    @Override
    public void afterServerMessageProcessed(Client client, ServerMessageType serverMessageType, String customMessageCommand, boolean isBinary, Object data) {
        lastMessageType=serverMessageType;
        if(lastMessageType==ServerMessageType.invalidAction)
            Console.log(data.toString());
    }
    
    void setClientListener(Client client){
        lastErrorCode=-1;
        lastMessageType=null;
        client.setClientEventListener(this);
    }
}
