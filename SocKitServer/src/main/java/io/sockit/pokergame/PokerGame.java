/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.pokergame;

import io.sockit.sockitserver.Location;
import io.sockit.sockitserver.Game;
import io.sockit.sockitserver.JsonUtil;
import io.sockit.sockitserver.RoomType;
import io.sockit.sockitserver.bot.Bot;
import javax.json.JsonObject;
import io.sockit.sockitserver.Room;
import io.sockit.sockitserver.bot.BotTurnDelayType;
import io.sockit.sockitserver.Session;
import io.sockit.sockitserver.User;
import java.util.Collection;

/**
 *
 * @author h
 */
public class PokerGame extends Game{
    private final int turnDurationInSecs;
    private final int delayAfterGameEndSecs;
    private BotTurnDelayType botTurnDelayType;
    
    public PokerGame(int turnDurationInSecs) {
        this(turnDurationInSecs,BotTurnDelayType.none,5000);
    }

    public PokerGame(int turnDurationInSecs,BotTurnDelayType botTurnDelayType,int delayAfterGameEndSecs) {
        super("SitNGo",false);
        this.turnDurationInSecs=turnDurationInSecs;
        this.botTurnDelayType=botTurnDelayType;
        this.delayAfterGameEndSecs=delayAfterGameEndSecs;
    }
    
    private boolean disableBots=false;
    public void disableBots(){
        this.disableBots=true;
    }

    @Override
    protected void setUpGame() {
        addNewLocation("mumbai");
        addNewLocation("delhi");
        addNewLocation("banglore");
        for(Location location:this.getLocations()){
            location.addRoom(new PokerRoom(this,"RoomA", RoomType.normal,5, turnDurationInSecs,delayAfterGameEndSecs,10,false));
            location.addRoom(new PokerRoom(this,"RoomB", RoomType.normal,5, turnDurationInSecs,delayAfterGameEndSecs,20,false));
            location.addRoom(new PokerRoom(this,"RoomC", RoomType.normal,5, turnDurationInSecs,delayAfterGameEndSecs,40,false));
            location.addRoom(new PokerRoom(this,"RoomD", RoomType.normal,5, turnDurationInSecs,delayAfterGameEndSecs,2,false));
            location.addRoom(new PokerRoom(this,"RoomE", RoomType.normal,5, turnDurationInSecs,delayAfterGameEndSecs,4,false));            
        }
        if(!disableBots)
            createBots();
    }

    void createBots() {
        Bot bot;
        Pair<String,Integer> botNameAvatar;
        int ctr;
        for(Location location:getLocations()){
            Collection<Room> rooms=location.getRooms(RoomType.normal);
            ctr=1;
            for(Room room:rooms){
                if(ctr%2==1){
                    bot=new Bot(botTurnDelayType,room.debugEnabled);
                    bot.setBotEventListener(new PokerBotListener(room.roomId));
                    botNameAvatar=getRandomBotNameAvatar();
                    bot.logIn(botNameAvatar.first, botNameAvatar.second,this.gameName);
                }
                if(ctr%3==1){
                    bot=new Bot(botTurnDelayType,room.debugEnabled);
                    bot.setBotEventListener(new PokerBotListener(room.roomId));
                    botNameAvatar=getRandomBotNameAvatar();
                    bot.logIn(botNameAvatar.first, botNameAvatar.second,this.gameName);
                }
                ctr++;
            }
        }        
    }

    @Override
    public PokerRoom newRoom(String roomName, RoomType roomType, int maxNoOfPlayers, int turnDurationInSecs,JsonObject roomSettings) {
        int smallBlind=JsonUtil.getAsInt(roomSettings, "smallBlind");
        return new PokerRoom(this,roomName, roomType, maxNoOfPlayers, turnDurationInSecs,5000,smallBlind,false);
    }

    @Override
    public PokerUserData newGameUserData() {
        return new PokerUserData();
    }

    private static final int[] femaleAvatarIds={1,2,3,4,5};
    private static final int[] maleAvatarIds={6,7,8,9,10};

    private static final String[] botFNames={"Ashok","Rani","Simi","Elvis","Sally","Zubin","Kaira","Figaro","Donny","Michelle"};
    private static final byte[] botGenders={0,1,1,0,1,0,1,0,0,1};
    private static Pair<String,Integer> getRandomBotNameAvatar(){
        int index=(int)(Math.random()*botFNames.length);
        String name=botFNames[index];
        byte gender=botGenders[index];
        if(gender==0)
            return new Pair(name,Utils.getRandom(maleAvatarIds));
        return new Pair(name,Utils.getRandom(femaleAvatarIds));
            
        
    }
    
    @Override
    protected void onEnterGame(Session session,boolean firstTime) {
        User user=session.getUser();
        PokerUserData userData=(PokerUserData)session.getGameUserData();
        if(firstTime && !user.isBot){
            userData.chipsInHand=10000;
        }
    }

    @Override
    protected void onExitGame(Session session) {
    }
}
