/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.pokergame;

import io.sockit.sockitserver.JsonUtil;
import io.sockit.sockitserver.bot.ErrorCodes;
import io.sockit.sockitserver.bot.Bot;
import io.sockit.sockitserver.bot.BotEventAdapter;
import io.sockit.sockitserver.bot.Player;
import io.sockit.sockitserver.bot.Room;
import io.sockit.sockitserver.bot.RoomType;
import io.sockit.sockitserver.bot.RoomInfo;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.List;
import javax.json.JsonObjectBuilder;
import io.sockit.sockitserver.bot.BotEventListener;

/**
 *
 * @author h
 */
public class PokerBotListener extends BotEventAdapter{           
    String location;
    RoomType roomType;
    long roomId=-1;
    boolean loggedInOnce=false;
    
    public PokerBotListener(String location,RoomType roomType) {
        this.location = location;
        this.roomType=roomType;
    }
    
    public PokerBotListener(long roomId){
       this.roomId=roomId;
       
    }
    
    @Override
    public void onError(Bot bot, int errorCode, String errorDesc) {
        bot.debug("Error errorCode=" + errorCode + ", desc=" + errorDesc);
        if(errorCode==ErrorCodes.seatNotFree){
            Room joinedRoom=bot.getJoinedRoom();
            if(joinedRoom!=null)
                onRoomJoined(bot, joinedRoom);
            else 
                bot.logOut();
                
            return;
        }
        if(errorCode==ErrorCodes.roomIdDoesNotExist){
            bot.logOut();
            return;
        }
    }

    @Override
    public void onLoggedIn(Bot bot) {
        loggedInOnce=true;
        if(roomId==-1)
            bot.getRooms(location, roomType);
        else
            bot.joinRoom(roomId);
    }

    @Override
    public void onLoggedOut(Bot bot) {
        bot.setBotEventListener(null);
    }

    @Override
    public void onGetRooms(Bot bot, String gameName, String location, RoomType roomtype, List<RoomInfo> rooms) {
        JsonObject userData=bot.getGameUserData();
        int chipsInHand=JsonUtil.getAsInt(userData, "chipsInhand", 0);
        if(chipsInHand<=0){
            bot.debug("chipsInHand < 0");
            bot.logOut();
            return;
        }
        int noOfRooms=rooms.size();
        if(noOfRooms<1){
            bot.debug("noOfRooms < 1");
            bot.logOut();
            return;
        }
        //join a random room 
        RoomInfo room=rooms.get(Utils.randomInt(0, noOfRooms-1));
        bot.joinRoom(room.roomId);
    }
    
    int consecutiveSeatsNotAvailableCount=0;
    @Override
    public void onRoomJoined(Bot bot, Room room) {
        bot.debug("on room joined");
        //take available Seat
        int freeSeatNo=room.getFreeSeatNo();
        if(freeSeatNo==0){
            if(consecutiveSeatsNotAvailableCount<3){
                consecutiveSeatsNotAvailableCount++;
                bot.leaveRoom();
            }
            else
                bot.logOut();
            return;
        }
        consecutiveSeatsNotAvailableCount=0;
        JsonObject botUserData=bot.getGameUserData();
        int chipsInHand=JsonUtil.getAsInt(botUserData, "chipsInHand");
        JsonObject roomData=room.getData();
        int minChipsToTakeSeat=JsonUtil.getAsInt(roomData, "minChipsToTakeSeat");
        if(chipsInHand<minChipsToTakeSeat){
            bot.debug("chipsinHand < minChipsToTakeSeat");
            bot.logOut();
            return;
        }
        int chipsOnTable=minChipsToTakeSeat*4;
        if(chipsOnTable>chipsInHand)
            chipsOnTable=chipsInHand;        
        JsonObjectBuilder takeSeatData=JsonUtil.createObjectBuilder();
        takeSeatData.add("chipsOnTable",chipsOnTable);
        bot.debug("taking seat");
        bot.takeSeat(room.roomId, freeSeatNo, takeSeatData.build());
    }

    @Override
    public void onRoomLeft(Bot bot) {
        bot.debug("on room left");
        this.onLoggedIn(bot);
    }

    @Override
    public void onSeatLeft(Bot bot, Player playerLeft, Room room,boolean isSelf) {
        if(isSelf){
            bot.debug("on seat left");
            bot.leaveRoom();
        }
    }

    @Override
    public void onNextTurn(Bot bot, Player turnPlayer, JsonObject turnData, Room room, boolean isSelfTurn) {
        if(isSelfTurn){            
            JsonArray tableCards=JsonUtil.getAsJsonArray(room.getData(),"tableCards");
            int callValue=JsonUtil.getAsInt(turnData,"callValue");
            int bigBlind=JsonUtil.getAsInt(room.getData(),"smallBlind")*2;
            JsonObjectBuilder data=JsonUtil.createObjectBuilder();
            if(((int)(Math.random()*3)==2)){
                int minRaiseBetAmt=callValue*2;
                if(minRaiseBetAmt<callValue+bigBlind)
                    minRaiseBetAmt=callValue+bigBlind;
                int betAmt=callValue * 2;
                if(betAmt<minRaiseBetAmt)
                    betAmt=minRaiseBetAmt;
                data.add("betAmt", betAmt);
            }
            else
                data.add("betAmt", callValue);
            bot.playAction("bet", data.build());
        }
    }

    @Override
    public void onRoomDestroyed(Bot bot, Room room) {
        this.onLoggedIn(bot);
    }

    @Override
    public void onInvalidAction(Bot bot, String action, String description, JsonObject errorData, Room room, boolean isOutOfTurn) {
        bot.debug("Invalid Action : " + description);
    }

    
}
