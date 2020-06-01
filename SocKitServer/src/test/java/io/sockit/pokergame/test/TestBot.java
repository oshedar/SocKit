/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.pokergame.test;

import io.sockit.sockitserver.Server;
import io.sockit.sockitserver.Game;
import io.sockit.sockitserver.JsonUtil;
import io.sockit.sockitserver.bot.Bot;
import io.sockit.sockitserver.bot.Player;
import io.sockit.sockitserver.bot.Room;
import io.sockit.sockitserver.bot.RoomType;
import io.sockit.sockitserver.bot.RoomInfo;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import io.sockit.sockitserver.LevelDbStore;
import io.sockit.sockitserver.bot.BotEventAdapter;
import io.sockit.sockitserver.bot.BotTurnDelayType;
import io.sockit.pokergame.PokerGame;
import java.util.List;
import javax.json.JsonObjectBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author h
 */
public class TestBot extends BotEventAdapter{
    
    public TestBot(){
        
    }
    
    static Game game;
    
    @BeforeAll
    public static void setUpClass() throws Exception {
        game=new PokerGame(1, BotTurnDelayType.none, 0);
        Server.registerGame(game);
        Server.setInitialUsersCacheSize(2000);
        Server.setDataStore(new LevelDbStore("../testdb"));
        System.out.println("starting server");
        Server.startServerAsHttp(2014);        
    }
    
    @AfterAll
    public static void tearDownClass() {
        Server.stopServer();
    }
    
     private static JsonObject takeSeatData;
     static{
         
         takeSeatData=JsonUtil.createObjectBuilder().add("chipsOnTable", 40).build();
     }
     @Test
     public void nextRound(){ 
         Bot bot1=new Bot(BotTurnDelayType.none);
         bot1.setBotEventListener(this);
         bot1.logIn("hoshi1", 1,game.gameName);
         try{ Thread.sleep(60); }catch(InterruptedException ex){}
         Server.logToConsole("getting rooms");
         bot1.getRooms("mumbai",io.sockit.sockitserver.bot.RoomType.normal);
         try{ Thread.sleep(60); }catch(InterruptedException ex){}
         bot1.joinRoom(rooms.get(0).roomId);
         try{ Thread.sleep(50); }catch(InterruptedException ex){}
         bot1.takeSeat(bot1.getJoinedRoom().roomId,1,takeSeatData);
         try{ Thread.sleep(50); }catch(InterruptedException ex){}
         Bot bot2=new Bot(BotTurnDelayType.none);
         bot2.setBotEventListener(this);
         bot2.logIn("hoshi2", 1,game.gameName);         
         try{ Thread.sleep(50); }catch(InterruptedException ex){}
         bot2.takeSeat(bot1.getJoinedRoom().roomId, 2, takeSeatData);         
         try{ Thread.sleep(10000); }catch(InterruptedException ex){}         
     }
     
    private List<RoomInfo> rooms;
    @Override
    public void onError(Bot bot, int errorCode, String errorDesc) {
        System.out.println("error from server: code=" + errorCode + "; desc=" + errorDesc);
    }

    @Override
    public void onGetRooms(Bot bot, String gameName, String location, RoomType roomtype, List<RoomInfo> rooms) {
//        Server.logToConsole("in onGetRooms noOfRooms=" + rooms.size());
//        Server.logToConsole("" + rooms.get(0).roomId);
        this.rooms=rooms;
    }

    @Override
    public void onNextTurn(Bot bot, Player turnPlayer, JsonObject turnData, Room room, boolean isSelfTurn) {
        if(isSelfTurn){
            JsonArray tableCards=JsonUtil.getAsJsonArray(room.getData(),"tableCards");
            if(tableCards.size()>4)
                return;
            int callValue=JsonUtil.getAsInt(turnData,"callValue");
            JsonObjectBuilder data=JsonUtil.createObjectBuilder();
            if(((int)(Math.random()*3)==2))
                data.add("betAmt", callValue+2);
            else
                data.add("betAmt", callValue);
            bot.playAction("bet", data.build());
        }
    }

    @Override
    public void onTurnPlayed(Bot bot, Player turnPlayer, String playerAction, JsonObject actionData, Room room, boolean isSelf) {        
        if(bot.getName().equals("hoshi2")){
            int amtBet=JsonUtil.getAsInt(actionData, "amtBet");
            System.out.println(turnPlayer.getName() + " " + playerAction + ", amtBet=" + amtBet);
        }
    }

    @Override
    public void onGameAction(Bot bot, String gameAction, JsonObject actionData, Room room) {
        if(bot.getName().equals("hoshi2")){
            System.out.println(gameAction + "-" + actionData);
        }
    }

    @Override
    public void onGamePlayEnded(Bot bot, Room room, JsonObject endGameData) {
        if(bot.getName().equals("hoshi2")){
            System.out.println("game over");
        }
    }

    @Override
    public void onInvalidAction(Bot bot, String action, String description, JsonObject errorData, Room room, boolean isOutOfTurn) {
        System.out.println("error " + errorData);
    }

}
