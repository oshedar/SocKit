/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver.bot;

import io.sockit.servertools.CommandDataReadListener;
import io.sockit.servertools.CommandDataSocket;
import io.sockit.servertools.ForceableReentrantLock;
import io.sockit.servertools.LocalCommandDataSocket;
import io.sockit.sockitserver.JsonUtil;
import io.sockit.sockitserver.Server;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * This class represents a Bot running on the server. It has methods to login the Bot to the Server, enter a game, fetch list of rooms, join a room, etc. Bots are instantiated in the setUpGame() method of the Game class
 */
public final class Bot {
    String userId;
    String sessionId;
    String name;
    int avtarId;
    String gameName;
    JsonObject gameUserData;
    CommandDataSocket socket;
    private boolean loggedIn;
    private BotEventListener botEventListener;

    volatile public int lastErrorCode=0;

    private Room joinedRoom;
    
    final AtomicBoolean loggedOut=new AtomicBoolean(false);
    final ReentrantLock instanceLock=new ForceableReentrantLock();
    
    final BotTurnDelayType turnDelayType;

    
    /**
     * indicates whether debug messages will be logged for this bot or not
     */
    public final boolean debugEnabled;
    
    private static void log(Exception ex){
        Server.log(ex);
    }
    
    /**
     * Creates a Bot
     */
    public Bot() {
        this(BotTurnDelayType.normal,false);
    }
    
    /**
     * Creates a Bot
     * @param enableDebug - specifies whether debug messages should be logged for this bot or not
     */
    public Bot(boolean enableDebug) {
        this(BotTurnDelayType.normal,enableDebug);
    }
    
    /**
     * Creates a Bot
     * @param turnDelayType - specifies the turn delay type for this bot (fast, normal, slow or no delay)
     */
    public Bot(BotTurnDelayType turnDelayType) {
        this(turnDelayType, false);
    }
    
    /**
     * creates a Bot
     * @param turnDelayType - specifies the turn delay type for this bot (fast, normal, slow or no delay)
     * @param enableDebug - specifies whether debug messages should be logged for this bot or not
     */
    public Bot(BotTurnDelayType turnDelayType,boolean enableDebug) {
        this.turnDelayType=turnDelayType;
        this.debugEnabled=enableDebug;
    }
    
    void forceInstanceLock(int waitTimeMIllis){
        ((ForceableReentrantLock)instanceLock).forceLock(waitTimeMIllis);
    }
    
    /**
     * Attempts to Login a Bot onto server with emailId and starts a new session
     * @param name - the Bot name
     * @param avtarId - the bot avatar Id
     * @param gameName - the game which should be linked with this Bot session on successful login - cannot be null
     */
    public void logIn(String name,int avtarId,String gameName){
        if(gameName==null)
            throw new NullPointerException("gameName is null");
        instanceLock.lock();
        try{
            if(loggedIn){
                debug("bot already logged in");
                logOut();
            }
            reset();
            this.name=name;
            this.avtarId=avtarId;
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            json.add("type", "bot");
            json.add("name", name);
            json.add("avtarId", avtarId);
            json.add("gameName", gameName);
            sendMesg(Commands.login, json.build().toString());
        }finally{instanceLock.unlock();}
    }
    
    //resets client data before register or login
    private void reset(){
        if(socket!=null){
            socket.close();
            socket=null;
        }        
        userId=null;
        sessionId=null;
        name=null;
        avtarId=0;
        loggedIn=false;
        joinedRoom=null;
        gameName=null;
        gameUserData=null;
    }
    
    private static final JsonObject EMPTY_JSON_OBJECT=JsonObject.EMPTY_JSON_OBJECT;

    /**
     * Attempts to Send a json message to the server
     * @param command - the message command
     * @param data - the message data as json
     */
    public void sendJsonMessage(String command,JsonObject data){
        if(!loggedIn || command==null)
            return;
        if(data==null)
            data=EMPTY_JSON_OBJECT;
        sendMesg("__" + command,data.toString());
    } 

    /**
     * Attempts to Send a text message to the server
     * @param command - the message command
     * @param data - the message data as string
     */
    public void sendTxtMessage(String command,String data){
        if(!loggedIn || command==null)
            return;
        sendMesg("--" + command,data);
    } 

    /**
     * Attempts to sends a binary message to the server
     * @param command - the message command
     * @param data - the message data as a byte array
     */
    public void sendBinaryMessage(String command,byte[] data){
        if(!loggedIn || command==null)
            return;
        sendMesg("--" + command,data);        
    }
    
    private String pollData;

    String getPollData(){
        String pollData=this.pollData;
        if(pollData!=null)
            return pollData;
        JsonObjectBuilder json=JsonUtil.createObjectBuilder();
        json.add("sessionId", Bot.this.sessionId);
        pollData=json.build().toString();
        this.pollData=pollData;
        return pollData;
    }
    void sendMesg(String command,String data){
        if(loggedOut.get())
            return;
        instanceLock.lock();
        try{
            if(socket==null || socket.isClosed()){
                LocalCommandDataSocket localSocket=LocalCommandDataSocket.newInstance();
                socket=localSocket;
                socket.setCommandDataReadListener(new BotCommandDataReadListener());
                //if command is not register or login or poll then send poll mesg first
                if(!(Commands.poll.equals(command) || Commands.login.equals(command) || Commands.register.equals(command) || Commands.rejoinSession.equals(command)))
                    socket.write(Commands.poll, getPollData());

            }
            socket.write(command, data);
        }finally{instanceLock.unlock();}
    }
    
    void sendMesg(String command,byte[] data) {
        if(loggedOut.get())
            return;
        instanceLock.lock();
        try{
            if(socket==null || socket.isClosed()){
                LocalCommandDataSocket localSocket=LocalCommandDataSocket.newInstance();
                socket=localSocket;
                socket.setCommandDataReadListener(new BotCommandDataReadListener());
                //if command is not register or login or poll then send poll mesg first
                socket.write(Commands.poll, getPollData());

            }
            socket.write(command, data);
        }finally{instanceLock.unlock();}        
    }

    /**
     * Attempts to Change the avtarId of this Bot
     * @param avtarId - the new avtar ID
     */
    public void changeAvtar(int avtarId){
        instanceLock.lock();
        try{
            if(this.avtarId==avtarId)
                return;
            if(!loggedIn)
                return;
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            json.add("avtarId", avtarId);
            sendMesg(Commands.changeAvtar, json.build().toString());
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Attempts to Logout this bot from the server
     */
    public void logOut(){
        instanceLock.lock();
        try{
            if(!loggedIn)
                return;
            //send logOut mesg
            sendMesg(Commands.logout, (String)null);
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Fetches the list of game locations from the server
     */
    public void getLocations(){
        instanceLock.lock();
        try{
            if(!loggedIn)
                return;
            sendMesg(Commands.getLocations, (String)null);
        }finally{instanceLock.unlock();}
    }

    /**
     * Fetches the list of rooms from the server
     * @param location - the location whose rooms should be fetched
     * @param roomType - the room type - normal or fast
     */
    public void getRooms(String location,RoomType roomType){
        instanceLock.lock();
        try{
            if(!loggedIn)
                return;
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            json.add("location", location);
            json.add("roomType", roomType.toString());
            sendMesg(Commands.getRooms, json.build().toString());
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Attempts to Make the Bot session join the specified room. 
     * @param roomId - the roomId of the room to join
     */
    public void joinRoom(long roomId){
        instanceLock.lock();
        try{
            if(!loggedIn)
                return;
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            json.add("roomId", roomId);
            sendMesg(Commands.joinRoom, json.build().toString());
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Attempts to refresh the room state(data) from the server
     */
    public void refreshRoomFromServer(){
        instanceLock.lock();
        try{
            if(!loggedIn)
                return;
            if(joinedRoom!=null)
                sendMesg(Commands.getRoomData, (String)null);
        }finally{instanceLock.unlock();}        
    }
    
    /**
     * Attempts to take a seat in the room. Until the Bot is seated he/she remains a spectator and cannot take part in the game play. The seat numbers are numbered 1 to n. 
     * @param roomId - the roomId of the room to join
     * @param seatNo - the seat number to sit on - starting from 1
     * @param data - additional data sent along with the take seat command to the server. For eg. in Poker this could be the chips to put on table
     */
    public void takeSeat(long roomId,int seatNo,JsonObject data){
        instanceLock.lock();
        try{
            if(!loggedIn)
                return;            
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            json.add("roomId", roomId);
            json.add("seatNo", seatNo);
            if(data==null)
                data=EMPTY_JSON_OBJECT;
            json.add("data", data);
            sendMesg(Commands.takeSeat, json.build().toString());
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Attempts to leave the seat in the room.
     */
    public void leaveSeat(){
        debug("leaving seat");
        instanceLock.lock();       
        try{
            if(!loggedIn)
                return;
            if(isSeated())
                sendMesg(Commands.leaveSeat, (String)null);
        }finally{instanceLock.unlock();}        
    }
    
    /**
     * Attempts to leave the room.
     */
    public void leaveRoom(){
        debug("leaving room");
        instanceLock.lock();
        try{
            if(!loggedIn || joinedRoom==null)
                return;
            sendMesg(Commands.leaveRoom, (String)null);
        }finally{instanceLock.unlock();}        
        
    }
    
    private void loggedOut(){        
        loggedOut.set(true);
        this.forceInstanceLock(400);
        try{
            reset();
        }finally{instanceLock.unlock();}
    }
    
    private class OnNextTurnInvoker implements Runnable{
        BotEventListener botListener;
        Bot bot;
        Player turnPlayer;
        JsonObject turnData;
        Room room;
        boolean isSelfTurn;

        public OnNextTurnInvoker(BotEventListener botListener, Bot bot, Player turnPlayer, JsonObject turnData, Room room, boolean isSelfTurn) {
            this.botListener = botListener;
            this.bot = bot;
            this.turnPlayer = turnPlayer;
            this.turnData = turnData;
            this.room = room;
            this.isSelfTurn = isSelfTurn;
        }
        
        @Override
        public void run() {
            instanceLock.lock();
            try{
                if(joinedRoom!=null && joinedRoom.isGamePlayInProgress() && joinedRoom.isCurTurn(turnPlayer))
                    botListener.onNextTurn(bot, turnPlayer, turnData, room, isSelfTurn);
            }finally{
                instanceLock.unlock();
            }
        }
        
    }   
    
    private class BotCommandDataReadListener implements CommandDataReadListener{

        @Override
        public void commandDataRead(CommandDataSocket socket, String command, String data) {
            instanceLock.lock();
            try{
                JsonObject dataAsJson=null;
                if(data!=null){
                    try{
                        dataAsJson=JsonUtil.readObject(data);
                    }catch(Throwable ex){
                        Server.logToConsole("error parsing data:" + data);
                        Server.log(new Exception(ex));
                    }
                }
                switch(Command.toEnum(command)){
                    case loggedIn:
                        Bot.this.userId=JsonUtil.getAsString(dataAsJson,"userId");
                        Bot.this.sessionId=JsonUtil.getAsString(dataAsJson,"sessionId");
                        Bot.this.name=JsonUtil.getAsString(dataAsJson,"name");
                        Bot.this.avtarId=JsonUtil.getAsInt(dataAsJson,"avtarId");
                        Bot.this.gameName=JsonUtil.getAsString(dataAsJson, "gameName");
                        Bot.this.gameUserData=JsonUtil.getAsJsonObject(dataAsJson,"gameUserData");
                        Bot.this.loggedIn=true;
                        if(botEventListener!=null)
                            botEventListener.onLoggedIn(Bot.this);
                        break;
                    case error:
                        int errorCode=JsonUtil.getAsInt(dataAsJson,"code");
                        lastErrorCode=errorCode;
                        if(botEventListener!=null){
                            if(errorCode==ErrorCodes.serverShutdownStarted)
                                botEventListener.onServerShutdown(Bot.this);
                            else
                                botEventListener.onError(Bot.this,errorCode, JsonUtil.getAsString(dataAsJson,"desc"));
                        }
                        break;
                    case loggedOut:
                        loggedOut();
                        if(botEventListener!=null)
                            botEventListener.onLoggedOut(Bot.this);
                        botEventListener=null;
                        break;
                    case sessionTimedOut:
                        Bot.this.loggedIn=false;
                        joinedRoom=null;
                        userId=null;
                        if(botEventListener!=null)
                            botEventListener.onSessionTimedOut(Bot.this);
                        break;
                    case shutDown:
                        loggedOut();
                        if(botEventListener!=null)
                            botEventListener.onServerShutdown(Bot.this);
                        botEventListener=null;
                        break;
                    case locations:
                        if(!loggedIn)
                            return;
                        {
                            List<String> locations=null;
                            String gameName=JsonUtil.getAsString(dataAsJson,"gameName");
                            JsonArray jsonArray=JsonUtil.getAsJsonArray(dataAsJson,"locations");
                            locations=new ArrayList<>(jsonArray.size());
                            for(JsonValue jsonValue:jsonArray)
                                locations.add(JsonUtil.getValueAsString(jsonValue));
                            if(botEventListener!=null)
                                botEventListener.onGetLocations(Bot.this,gameName,locations);
                        }
                        break;
                    case rooms:
                        if(!loggedIn)
                            return;
                        {
                            String gameName=JsonUtil.getAsString(dataAsJson,"gameName");
                            String location=JsonUtil.getAsString(dataAsJson,"location");
                            RoomType roomType=RoomType.normal;
                            if("fast".equals(JsonUtil.getAsString(dataAsJson,"roomType")))
                                roomType=RoomType.fast;
                            JsonArray roomsAsJson=JsonUtil.getAsJsonArray(dataAsJson,"rooms");
                            List<RoomInfo> rooms=new ArrayList<>(roomsAsJson.size());
                            JsonObject jsonObject;
                            for(JsonValue jsonValue:roomsAsJson){
                                jsonObject=(JsonObject)jsonValue;
                                rooms.add(new RoomInfo(JsonUtil.getAsLong(jsonObject,"roomId"), JsonUtil.getAsString(jsonObject,"roomName"),JsonUtil.getAsInt(jsonObject,"totalNoOfSeats"), JsonUtil.getAsInt(jsonObject,"noOfPlayers"), JsonUtil.getAsJsonObject(jsonObject,"data")));
                            }                                
                            if(botEventListener!=null)
                                botEventListener.onGetRooms(Bot.this, gameName, location, roomType, rooms);                            
                        }
                        break;
                    case roomJoined:
                        if(!loggedIn)
                            return;
                        Bot.this.gameUserData=JsonUtil.getAsJsonObject(dataAsJson,"gameUserData");
                        joinedRoom=Room.newRoom(JsonUtil.getAsJsonObject(dataAsJson,"room"),Bot.this);
                        if(botEventListener!=null)
                            botEventListener.onRoomJoined(Bot.this, joinedRoom);                        
                        break;
                    case roomData:
                        if(!loggedIn)
                            return;
                        {
                            long roomId=JsonUtil.getAsLong(dataAsJson,"roomId");
                            if(joinedRoom==null || joinedRoom.roomId!=roomId)
                                joinedRoom=Room.newRoom(dataAsJson,Bot.this);
                            else
                                joinedRoom.refreshRoomData(dataAsJson,Bot.this);                            
                            if(botEventListener!=null)
                                botEventListener.onRoomRefreshedFromServer(Bot.this, joinedRoom);
                        }
                        break;
                    case seatTaken:
                        if(!loggedIn || joinedRoom==null)
                            return;
                        Player playerSeated=Player.newPlayer(JsonUtil.getAsJsonObject(dataAsJson,"player"),Bot.this,Bot.this.joinedRoom);
                        joinedRoom.set(playerSeated.seatNo,playerSeated);
                        if(Bot.this.userId.equals(playerSeated.userId)){
                            Bot.this.gameUserData=JsonUtil.getAsJsonObject(dataAsJson,"gameUserData");
                        }
                        if(botEventListener!=null)
                            botEventListener.onSeatTaken(Bot.this,playerSeated, joinedRoom,Bot.this.userId.equals(playerSeated.userId));                        
                        break;
                    case userData:
                        Bot.this.gameUserData=dataAsJson;
                        break;
                    case seatLeft:
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            int seatNo=JsonUtil.getAsInt(dataAsJson,"seatNo");
                            String userId=JsonUtil.getAsString(dataAsJson,"userId");
                            Player playerLeft=joinedRoom.remove(seatNo);
                            if(Bot.this.userId.equals(userId))
                                Bot.this.gameUserData=JsonUtil.getAsJsonObject(dataAsJson,"gameUserData");
                            if(playerLeft!=null && botEventListener!=null)
                                botEventListener.onSeatLeft(Bot.this,playerLeft, joinedRoom,Bot.this.userId.equals(playerLeft.userId));
                        }
                        break;
                    case roomLeft:
                        if(!loggedIn || joinedRoom==null)
                            return;
                        joinedRoom=null;
                        Bot.this.gameUserData=JsonUtil.getAsJsonObject(dataAsJson,"gameUserData");
                        if(botEventListener!=null)
                            botEventListener.onRoomLeft(Bot.this);
                        break;
                    case newGame:
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            long roomId=JsonUtil.getAsLong(dataAsJson,"roomId");
                            if(joinedRoom==null || joinedRoom.roomId!=roomId)
                                joinedRoom=Room.newRoom(dataAsJson,Bot.this);
                            else
                                joinedRoom.refreshRoomData(dataAsJson,Bot.this);                            
                            if(botEventListener!=null)
                                botEventListener.onGamePlayStarted(Bot.this, joinedRoom);
                        }
                        break;
                    case nextTurn:
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            JsonObject turnPlayerAsJson=JsonUtil.getAsJsonObject(dataAsJson,"turnPlayer");
                            int seatNo=JsonUtil.getAsInt(turnPlayerAsJson,"seatNo");
                            String userId=JsonUtil.getAsString(turnPlayerAsJson,"userId");                        
                            Player turnPlayer=joinedRoom.getPlayerBySeatNo(seatNo);
                            if(turnPlayer==null){
                                turnPlayer=Player.newPlayer(turnPlayerAsJson,Bot.this,Bot.this.joinedRoom);
                                joinedRoom.set(seatNo, turnPlayer);
                            }
                            else if(!turnPlayer.userId.equals(userId)){
                                turnPlayer=Player.newPlayer(turnPlayerAsJson,Bot.this,Bot.this.joinedRoom);
                                joinedRoom.set(seatNo, turnPlayer);
                            }
                            else
                                turnPlayer.refreshPlayerData(turnPlayerAsJson,Bot.this,Bot.this.joinedRoom);
                            joinedRoom.curTurnSeatNo=turnPlayer.seatNo;
                            if(botEventListener!=null){  
                                boolean isSelf=Bot.this.userId.equals(turnPlayer.userId);
                                //call listener after ramdom delay
                                OnNextTurnInvoker invoker=new OnNextTurnInvoker(botEventListener,Bot.this,turnPlayer,JsonUtil.getAsJsonObject(dataAsJson,"data") , joinedRoom,isSelf);
                                if(!isSelf || turnDelayType==BotTurnDelayType.none)
                                    invoker.run();
                                else{
                                    int delay;
                                    switch(turnDelayType){
                                        case fast:
                                            delay=(int)(Math.random()*(joinedRoom.turnDurationMillis/4));
                                            if(delay<400)
                                                delay+=400;
                                            break;
                                        case normal:
                                            delay=(int)(Math.random()*(joinedRoom.turnDurationMillis/2));
                                            if(delay<1000)
                                                delay+=1000;
                                            break;
                                        default:
                                            delay=(int)(Math.random()*(joinedRoom.turnDurationMillis)/1.5);
                                            if(delay<2000)
                                                delay+=2000;
                                            break;
                                    }
                                    Server.execute(invoker, delay);
                                }
                            }
                        }
                        break;
                    case turnPlayed:
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            String playerAction=JsonUtil.getAsString(dataAsJson, "playerAction");
                            JsonObject actionData=JsonUtil.getAsJsonObject(dataAsJson, "actionData");
                            int turnSeatNo=JsonUtil.getAsInt(dataAsJson,"turnSeatNo");
                            JsonObject roomData=JsonUtil.getAsJsonObject(dataAsJson,"room");
                            long roomId=JsonUtil.getAsLong(roomData,"roomId");
                            if(joinedRoom==null || joinedRoom.roomId!=roomId)
                                joinedRoom=Room.newRoom(roomData,Bot.this);
                            else
                                joinedRoom.refreshRoomData(roomData,Bot.this);
                            Player turnPlayer=joinedRoom.getPlayerBySeatNo(turnSeatNo);
                            if(botEventListener!=null)
                                botEventListener.onTurnPlayed(Bot.this, turnPlayer, playerAction, actionData, joinedRoom, turnPlayer==null?false:Bot.this.userId==turnPlayer.userId);
                        }
                        break;
                    case invalidAction:
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            String action=JsonUtil.getAsString(dataAsJson, "action");
                            String desc=JsonUtil.getAsString(dataAsJson, "desc");
                            JsonObject errorData=JsonUtil.getAsJsonObject(dataAsJson, "data");
                            if(botEventListener!=null)
                                botEventListener.onInvalidAction(Bot.this, action, desc, errorData, joinedRoom, !Bot.this.isCurTurn());
                        }
                        break;
                    case gameAction:
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            String gameAction=JsonUtil.getAsString(dataAsJson, "action");
                            JsonObject actionData=JsonUtil.getAsJsonObject(dataAsJson, "data");
                            JsonObject roomData=JsonUtil.getAsJsonObject(dataAsJson,"room");
                            long roomId=JsonUtil.getAsLong(roomData,"roomId");
                            if(joinedRoom==null || joinedRoom.roomId!=roomId)
                                joinedRoom=Room.newRoom(roomData,Bot.this);
                            else
                                joinedRoom.refreshRoomData(roomData,Bot.this);
                            if(botEventListener!=null)
                                botEventListener.onGameAction(Bot.this, gameAction, actionData, joinedRoom);
                        }
                        break;
                    case outOfTurnPlayed:
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            String playerAction=JsonUtil.getAsString(dataAsJson, "playerAction");
                            JsonObject actionData=JsonUtil.getAsJsonObject(dataAsJson, "actionData");
                            JsonObject playerAsJson=JsonUtil.getAsJsonObject(dataAsJson, "player");
                            int playerSeatNo=JsonUtil.getAsInt(dataAsJson, "playerSeatNo");
                            JsonObject roomData=JsonUtil.getAsJsonObject(dataAsJson,"room");
                            Player outOfTurnPlayer=null;
                            if(playerAsJson!=null){
                                int seatNo=JsonUtil.getAsInt(playerAsJson,"seatNo");
                                String userId=JsonUtil.getAsString(playerAsJson,"userId");                        
                                outOfTurnPlayer=joinedRoom.getPlayerBySeatNo(seatNo);
                                if(outOfTurnPlayer==null){
                                    outOfTurnPlayer=Player.newPlayer(playerAsJson,Bot.this,Bot.this.joinedRoom);
                                    joinedRoom.set(seatNo, outOfTurnPlayer);
                                }
                                else if(!outOfTurnPlayer.userId.equals(userId)){
                                    outOfTurnPlayer=Player.newPlayer(playerAsJson,Bot.this,Bot.this.joinedRoom);
                                    joinedRoom.set(seatNo, outOfTurnPlayer);
                                }
                                else
                                    outOfTurnPlayer.refreshPlayerData(playerAsJson,Bot.this,Bot.this.joinedRoom);

                            }
                            else{
                                long roomId=JsonUtil.getAsLong(roomData,"roomId");
                                if(joinedRoom==null || joinedRoom.roomId!=roomId)
                                    joinedRoom=Room.newRoom(roomData,Bot.this);
                                else
                                    joinedRoom.refreshRoomData(roomData,Bot.this);
                                outOfTurnPlayer=joinedRoom.getPlayerBySeatNo(playerSeatNo);
                            }
                            if(botEventListener!=null)
                                botEventListener.onOutOfTurnPlayed(Bot.this, outOfTurnPlayer, playerAction, actionData, joinedRoom, outOfTurnPlayer==null?false:Bot.this.userId==outOfTurnPlayer.userId);
                        }
                        break;
                    case endGame:
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            JsonObject endGameData=JsonUtil.getAsJsonObject(dataAsJson, "endGameData");
                            JsonObject roomData=JsonUtil.getAsJsonObject(dataAsJson,"room");
                            long roomId=JsonUtil.getAsLong(roomData,"roomId");
                            if(joinedRoom==null || joinedRoom.roomId!=roomId)
                                joinedRoom=Room.newRoom(roomData,Bot.this);
                            else
                                joinedRoom.refreshRoomData(roomData,Bot.this);
                            try{
                                if(botEventListener!=null)
                                    botEventListener.onGamePlayEnded(Bot.this, joinedRoom, endGameData);
                            }finally{joinedRoom.gameInProgress=false;}
                        }
                        break;
                    case avtarChanged:
                        if(!loggedIn)
                            return;
                        {
                            String userId=JsonUtil.getAsString(dataAsJson,"userId");
                            int avtarId=JsonUtil.getAsInt(dataAsJson,"avtarId");
                            if(Bot.this.userId.equals(userId)){
                                Bot.this.avtarId=avtarId;
                                if(botEventListener!=null)
                                    botEventListener.onAvtarChangedOfSelf(Bot.this, avtarId);
                                break;
                            }
                            Room room=Bot.this.joinedRoom;
                            if(room==null)
                                break;
                            Player player=room.getPlayerByUserId(userId);
                            if(player==null)
                                break;
                            if(botEventListener!=null)
                                botEventListener.onAvtarChangedOfOtherPlayer(Bot.this, player, avtarId, room);                                
                        }
                        break;
                    case roomDestroyed:
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            long roomId=JsonUtil.getAsLong(dataAsJson,"roomId");
                            if(joinedRoom.roomId!=roomId)
                                return;
                            JsonObject gameUserData=JsonUtil.getAsJsonObject(dataAsJson, "gameUserData");
                            if(gameUserData!=null)
                                Bot.this.gameUserData=gameUserData;
                            Room roomDestroyed=joinedRoom;
                            joinedRoom=null;
                            if(botEventListener!=null)
                                botEventListener.onRoomDestroyed(Bot.this, roomDestroyed);
                        }
                        break;
                    case notElligibeToPlay:
                            if(botEventListener!=null)
                                botEventListener.onNotEligibleToPlay(Bot.this, Bot.this.getPlayer(), Bot.this.joinedRoom, JsonUtil.getAsString(dataAsJson, "reason"));
                            break;
                    default:
                        if(command.startsWith("__")){
                            if(botEventListener!=null){
                                botEventListener.onMessageReceivedJson(Bot.this,command.substring(2), dataAsJson);
                            }
                        }
                        else if(command.startsWith("--")){
                            if(botEventListener!=null){
                                botEventListener.onMessageReceivedString(Bot.this,command.substring(2), data);
                            }
                        }
                }
            }finally{instanceLock.unlock();}
        }

        @Override
        public void commandDataRead(CommandDataSocket socket, String command, byte[] data) {
            if(command.startsWith("--"))
                if(botEventListener!=null)
                    botEventListener.onMessageReceivedBytes(Bot.this,command.substring(2), data);            
        } 
    }
    
    /**
     * Sets the BotEventListener to which the bot's events/callbacks will be forwarded
     * @param botEventListener - the BotEventListener that will handle the Bot's events/callbacks
     */
    public final void setBotEventListener(BotEventListener botEventListener) {
        instanceLock.lock();
        try{
            this.botEventListener=botEventListener;
//            if(botListener==null){
//                this.botListener=null;
//                return;
//            }
//            if(this.botListener==null){
//                this.botListener=new AsyncBotListener(botListener);
//                return;
//            }
//            if(((AsyncBotListener)this.botListener).botListener!=botListener)
//                this.botListener=new AsyncBotListener(botListener);
        }finally{instanceLock.unlock();}
    }
        
    /**
     * Returns the user ID of the Bot.
     * @return - the user ID of the bot
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the Bot sessionId
     * @return - Bot sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns the name of the Bot
     * @return - the name of the Bot
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the avtarId of the bot
     * @return - the avtarId of the bot
     */
    public int getAvtarId() {
        return avtarId;
    }

    /**
     * returns the Name of the game that this Bot is linked to
     * @return - the Name of the game that this Bot is linked to
     */
    public String getGameName() {
        return gameName;
    }

    /**
     * Returns the Game Specific User Data of the Bot
     * @return - the Game Specific User Data of the Bot
     */
    public JsonObject getGameUserData() {
        return gameUserData;
    }

    /**
     * Returns whether this Bot is loggedIn to the server or not
     * @return - true if this Bot is loggedIn to the server
     */
    public boolean isLoggedIn() {
        instanceLock.lock();
        try{
            return loggedIn;
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Returns the room joined by this bot or null if no room has been joined
     * @return - the room joined by this bot
     */
    public Room getJoinedRoom() {
        return joinedRoom;
    }
    
    /**
     * Checks if Bot has joined a room. Returns true if this Bot has joined a room
     * @return - true if this Bot has joined a room
     */
    public boolean hasJoinedRoom(){
        return joinedRoom!=null;
    }
    
    /**
     * Checks if Bot is a spectator. Returns true if this Bot has joined a room and is a spectator
     * @return - true if this Bot has joined a room and is a spectator
     */
    public boolean isSpectator(){
        Room joinedRoom=this.joinedRoom;
        return joinedRoom!=null && !joinedRoom.isSeated(userId);
    }
    
    /**
     * Checks if this Bot is seated. Returns true if this Client is seated in a room.
     * @return - true if this Bot is seated in a room.
     */
    public boolean isSeated(){
        Room joinedRoom=this.joinedRoom;
        return joinedRoom!=null && joinedRoom.isSeated(userId);
    }
    
    /**
     * Checks if Game play is in progress. Returns true if this Bot has joined a room and Game play is in progress in the room.
     * @return - true if this Bot has joined a room and Game play is in progress in the room.
     */
    public boolean isGamePlayInProgress(){
        Room joinedRoom=this.joinedRoom;
        return joinedRoom!=null && joinedRoom.isGamePlayInProgress();        
    }
    
    /**
     * Checks if this Bot is an active player. Returns true if this Bot is seated in a room and is taking part in the current game play in the room..
     * @return - true if this Bot is seated in a room and is taking part in the current game play in the room..
     */
    public boolean isActivePlayer(){
        Player player=getPlayer();
        return player!=null && player.isActive();
    }
    
    /**
     * Returns the player object associated with this Bot or null if the Bot is not seated
     * @return - the player object associated with this Bot or null if the Bot is not seated
     */
    public Player getPlayer(){
        Room joinedRoom=this.joinedRoom;
        if(joinedRoom==null)
            return null;
        return joinedRoom.getPlayerByUserId(userId);                    
    }
    
    /**
     * Returns true if the current turn belongs to this Bot.
     * @return - true if the current turn belongs to this Boy.
     */
    public boolean isCurTurn(){
        Room joinedRoom=this.joinedRoom;
        if(joinedRoom==null || !joinedRoom.gameInProgress)
            return false;
        Player player=joinedRoom.getPlayerByUserId(userId);
        if(player==null)
            return false;
        return joinedRoom.isCurTurn(player);
    }
    
    /**
     * Attempts to Play an action. Sends a playAction message to the Server.
     * @param action - the action played
     * @param actionData - the data associated with the action
     * @throws GamePlayNotInProgressException - if GamePlay is not in progress
     */
    public final void playAction(String action,JsonObject actionData) throws GamePlayNotInProgressException{
        instanceLock.lock();
        try{
            if(joinedRoom==null)
                return;
            if(!joinedRoom.gameInProgress)
                throw new GamePlayNotInProgressException();
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            json.add("action", action);
            json.add("data", actionData);
            sendMesg(Commands.playAction, json.build().toString());
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Outputs a debug message if debug is enabled for this Bot
     * @param txt - the debug message
     */
    public final void debug(String txt){
        if(debugEnabled)
            Server.log("bot " + this.getName() + " - " + txt);
    }
}
