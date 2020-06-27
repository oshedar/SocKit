/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import io.sockit.servertools.Utils;
import io.sockit.servertools.Executor;
import io.sockit.servertools.ForceableReentrantLock;
import io.sockit.servertools.WaitRunnable;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

/**
 * This class is the super class for all Game Rooms. It has methods to start gamePlay, iterate players, etc. The seats in the room are like a rounded queue such that the next seat after the last seat will be the first seat. The Game developer should extend this class and override the various event/callback methods such as <code>onRoomJoined(), onSeatTaken(), beforeGameStarted()</code>, etc. to provide the game logic. See example below 
 * <pre>
 * {@code
 *    public class TicTacToeRoom  extends Room{
 *        
 *        CellState[] grid;
 *        
 *        public TicTacToeRoom(Game game, String roomName, RoomType roomType, int turnDurationInSecs, int delayAfterGameEnded, boolean enableDebug) {
 *                super(game, roomName, roomType, 2, turnDurationInSecs, delayAfterGameEnded, enableDebug);
 *                grid = new CellState[9];
 *                Arrays.fill(grid, CellState.unmarked);
 *        }
 *
 *        {@literal @}Override
 *        protected Player newPlayer(Session session, int seatNo, JsonObject jo) {
 *                return new TicTacToePlayer(session, seatNo);
 *        }
 *
 *        {@literal @}Override
 *        protected void resetData() {
 *                Arrays.fill(grid, CellState.unmarked);                
 *        }
 *
 *        {@literal @}Override
 *        protected JsonObject prepareJsonForClient() {
 *                JsonObjectBuilder json =Json.createObjectBuilder();
 *                JsonArrayBuilder jsonGrid = Json.createArrayBuilder();
 *                for(int counter=0; counter < grid.length; counter++){
 *                        jsonGrid.add(grid[counter].toString());
 *                }
 *                json.add("grid", jsonGrid);
 *                
 *                return json.build();
 *        }
 *
 *        {@literal @}Override
 *        protected void onPlayerAction(Player player, String action, JsonObject data, boolean isOutOfTurn) { 
 *                if(isOutOfTurn){
 *                        return;
 *                }
 *                if(action.equals("cellClicked")){
 *                        int cellIndex = data.getInt("cellIndex");
 *
 *                        if(cellIndex > 8 || cellIndex < 0){
 *                                this.invalidateAction("Cell Dosen't exist", null);
 *                                return;
 *                        }
 *
 *                        JsonObjectBuilder sendData = Json.createObjectBuilder();
 *
 *                        if(grid[cellIndex] != CellState.unmarked){
 *                                sendData.add("cellIndex", cellIndex);
 *                                sendData.add("cellValue", grid[cellIndex].toString());
 *                                this.invalidateAction("Can't Click That!", sendData.build());
 *                                return;
 *
 *                        }
 *
 *                        TicTacToePlayer ticTacToePlayer = (TicTacToePlayer) player;
 *                        grid[cellIndex] = ticTacToePlayer.playerToken;
 *
 *                        sendData.add("cellIndex", cellIndex);
 *                        sendData.add("cellValue", grid[cellIndex].toString());
 *                        this.actionPlayed("cellIndex", sendData.build());
 *                }
 *            }
 *        //.......
 *    }
 * }
 * </pre>
 */
public abstract class Room implements Comparable<Room>{       
    private static final AtomicLong roomIdGnerator=new AtomicLong();
    private static final Map<Long,Room> rooms= new ConcurrentHashMap(500, 0.9f,7);
    final static Comparator<Room> roomComparator=new RoomComparator();
    private static final JsonObject EMPTY_JSON_OBJECT=JsonObject.EMPTY_JSON_OBJECT;
    private static AtomicBoolean shutDownStarted=new AtomicBoolean(false);
    static final void register(Room room){
        rooms.put(room.roomId, room);
    }
    /**
     * Returns the room with the specified room ID
     * @param roomId - the room Id
     * @return Room - the room with the specified roomId
     */
    static Room getRoom(Long roomId){
        return rooms.get(roomId);
    }
    static void doShutDown(){
        List<Room> rooms=null;
        if(!shutDownStarted.compareAndSet(false, true))
            return;
        rooms=new ArrayList<>(Room.rooms.values());
        Room.rooms.clear();
        for(Room room:rooms){
            try{
                room.shutDown();
            }catch(Exception ex){Utils.log(ex);}
        }
    }
    static void unregister(Long roomId){
        rooms.remove(roomId);
    }
    String ownerUserId;

    /**
     * the room ID.
     */
    public final Long roomId;
    /**
     * the room name.
     */
    public final String roomName;
    /**
     * the room type.
     */
    public RoomType roomType;
    Location location;
    
    /**
     * The Game to which this room belongs
     */
    public final Game game;
    private final ForceableReentrantLock instanceLock=new ForceableReentrantLock();
      
    private Set<Session> spectators=new HashSet<Session>(8, 0.8f);
    
    private Seats<Player> players;
    private Seats<Player> activePlayers;
    private List<Player> playersWhoPlayedGame;
            
    private int totalNoOfSeats;    

    /**
     * the min number of players required to start a new game. Once the min number of players take seat a new game automatically starts
     */
    public final int minNoOfPlayersForGame;
    
    private volatile int gameNo=1;// incremented in startGame and used in delayed run commands
    private boolean gameInProgress=false;
    private int turnDurationMillis;
    private int curTurnSeatNo=0;
    private boolean destroyed=false;
    private boolean gameEnding=false;
    private int delayBeforeFirstTurnMillis=0;
    private int delayAfterGameEndedMillis=0;

    /**
     * whether logging of debug messages is enabled for this player or not
     */
    public final boolean debugEnabled;
    private AtomicBoolean cancelAutoStartGame=new AtomicBoolean(false);    
    private AtomicBoolean cancelStartGame=new AtomicBoolean(false);
    private boolean delayedStartGameInvoked=false;
    private WaitRunnable turnTimeOutChecker=null;
    private String playerAction;
    private JsonObject data;
    private String errorDesc;
    private Player leaveSeatPlayer=null;
    private IterableForSepectators iterableForSepectators=new IterableForSepectators();
    private IterableForPlayersWhoPlayed iterableForPlayersWhoPlayed=new IterableForPlayersWhoPlayed();
    private RemovePrivateRoom removePrivateRoomRunnable=null;
    private Runnable removePrivateRoomWaitRunnable=null;
    private long whenPrivateRoomStarted=0;
    
    /**
     * Creates a room.
     * @param game - the Game to which the room belongs
     * @param roomName - the room name
     * @param roomType - the room type
     * @param totalNoOfSeats - the max number of players (total number of seats in the room)
     * @param turnDurationInSecs - the duration of 1 turn in seconds
     * @param delayAfterGameEndedInSecs - the delay between end game and start of a new game.
     */
    protected Room(Game game,String roomName,RoomType roomType,int totalNoOfSeats,int turnDurationInSecs,int delayAfterGameEndedInSecs){
        this(game, roomName, roomType, totalNoOfSeats, turnDurationInSecs, delayAfterGameEndedInSecs,false, 2);
    }

    /**
     * Creates a room
     * @param game - the Game to which the room belongs
     * @param roomName - the room name
     * @param roomType - the room type
     * @param totalNoOfSeats - the max number of players (total number of seats in the room)
     * @param turnDurationInSecs - the duration of 1 turn in seconds
     * @param delayAfterGameEndedInSecs - the delay between end game and start of a new game.
     * @param enableDebug - whether logging of debug messages should be enabled or not for this room
     */
    protected Room(Game game,String roomName,RoomType roomType,int totalNoOfSeats,int turnDurationInSecs,int delayAfterGameEndedInSecs,boolean enableDebug){
        this(game, roomName, roomType, totalNoOfSeats, turnDurationInSecs, delayAfterGameEndedInSecs,enableDebug, 2);
    }
    //for public rooms userId will be null

    /**
     *
     * @param game - the Game to which the room belongs
     * @param roomName - the room name
     * @param roomType - the room type
     * @param totalNoOfSeats - the max number of players (total number of seats in the room)
     * @param turnDurationInSecs - the duration of 1 turn in seconds
     * @param delayAfterGameEndedInSecs - the delay between end game and start of a new game.
     * @param enableDebug - whether logging of debug messages should be enabled or not for this room
     * @param minNoOfPlayersForGame - the min number of players required to start a new game. Once the min number of players take seat a new game automatically starts
     */
    protected Room(Game game,String roomName,RoomType roomType,int totalNoOfSeats,int turnDurationInSecs,int delayAfterGameEndedInSecs,boolean enableDebug,int minNoOfPlayersForGame){
        if(game==null)
            throw new NullPointerException("game is null");
        if(roomName==null)
            throw new NullPointerException("roomName is null");
        if(roomType==null)
            throw new NullPointerException("roomType is null");
        this.roomId=roomIdGnerator.incrementAndGet();
        this.game=game;
        this.roomName = roomName;
        this.roomType=roomType;
        this.totalNoOfSeats=totalNoOfSeats;
        players=new Seats(totalNoOfSeats);
        activePlayers=new Seats(totalNoOfSeats);
        playersWhoPlayedGame=new LinkedList();
        if(turnDurationInSecs<1)
            turnDurationInSecs=1;
        if(turnDurationInSecs<Integer.MAX_VALUE/1000)
            turnDurationMillis = turnDurationInSecs*1000;
        else
            turnDurationMillis=Integer.MAX_VALUE;
        this.minNoOfPlayersForGame=minNoOfPlayersForGame;
        this.debugEnabled=enableDebug;
        this.delayAfterGameEndedMillis=delayAfterGameEndedInSecs*1000;
    }
    /**
     * Compares this object with the specified object for order. Called to sort the rooms in the location. This method should be overridden in the child class
     * @param room - the room to compare to
     * @return int - a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Room room){
        return this.roomName.compareTo(room.roomName);
    }
    /**
     * Factory method called to instantiate a new player. When a client session takes seat in the room, this method is called to create a player. Note: till the client session is not seated, the session is considered a spectator. After seat is taken the session is considered a player
     * @param session - the client session of the player
     * @param SeatNo - the seat number of the player
     * @param data - any additional actionData required to initialize the player in json format
     * @return - a new player object linked to the specified session
     */
    protected abstract Player newPlayer(Session session,int SeatNo,JsonObject data);

    /**
     * Returns the maximum number of players who can play in the room (total number of seats in the room)
     * @return int - total number of seats in the room
     */
    public final int getTotalNoOfSeats() {
        return totalNoOfSeats;
    }
            
    /**
     * Returns user Id of this room's owner if this room is private else null
     * @return String - user Id of this room's owner if this room is private else null
     */
    public final String getOwnerUserId(){
        return this.ownerUserId;
    }
    
    /**
     * Returns whether room is private or not
     * @return boolean - true if the room is private
     */
    public final boolean isPrivate(){
        return ownerUserId!=null;
    }

    /**
     * Returns the delay between game start and first turn in seconds.
     * @return int - the delayBeforeFirstTurn
     */
    public int getDelayBeforeFirstTurnInSecs() {
        return delayBeforeFirstTurnMillis<1000?0:delayBeforeFirstTurnMillis/1000;
    }

    /**
     * Returns the delay between end game play and start game play in seconds.
     * @return int - the delayAfterGameEnded
     */
    public int getDelayAfterGamePlayEndedInSecs() {
        return delayAfterGameEndedMillis<1000?0:delayAfterGameEndedMillis/1000;
    }
    
    /**
     * Sets the delay between game play start and first turn in seconds.
     * @param delayInSecs - the delay between game play start and first turn in seconds
     */
    public void setDelayBeforeFirstTurnInSecs(int delayInSecs) {
        instanceLock.lock();
        try{
            this.delayBeforeFirstTurnMillis = delayInSecs*1000;
        }finally{
            instanceLock.unlock();
        }
    }
      
    
    final JsonObject toShortJson(){
        JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
        jsonBuilder.add("roomId", roomId);
        jsonBuilder.add("roomName", roomName);
        jsonBuilder.add("totalNoOfSeats", totalNoOfSeats);
        jsonBuilder.add("noOfPlayers", playerCount());
        JsonObject shortJsondata;
        instanceLock.lock();
        try{
            shortJsondata=getRoomInfo();
        }finally{instanceLock.unlock();}
        jsonBuilder.add("data", shortJsondata==null?EMPTY_JSON_OBJECT:shortJsondata);
        return jsonBuilder.build();
    }    
    
    final void createPlayersTmpJson(){
        if(players!=null){
            for(Player player:players)
                player.createTmpJson();
        }
    }
    
    final void clearPlayersTmpJson(){
        if(players!=null){
            for(Player player:players)
                player.clearTmpJson();
        }        
    }
    
    final JsonObject toJson(Player forPlayer){
        JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
        jsonBuilder.add("gameName", game.gameName);
        jsonBuilder.add("location", location==null?(String)null:location.name);
        jsonBuilder.add("ownerUserId", ownerUserId);
        jsonBuilder.add("roomType", roomType.toString());
        jsonBuilder.add("roomId", roomId);
        jsonBuilder.add("roomName", roomName);
        jsonBuilder.add("totalNoOfSeats", totalNoOfSeats);
        jsonBuilder.add("noOfPlayers", playerCount());        
        jsonBuilder.add("gameInProgress", gameInProgress);
        jsonBuilder.add("gameNo", gameNo);
        jsonBuilder.add("turnDurationMillis", turnDurationMillis);        
        jsonBuilder.add("curTurnSeatNo", curTurnSeatNo);        
        jsonBuilder.add("gameEnding", gameEnding);        
        JsonObject jsonData=prepareJsonForClient();
        jsonBuilder.add("data", jsonData==null?EMPTY_JSON_OBJECT:jsonData);
        
        JsonArrayBuilder playersArrayBuilder=JsonUtil.createArrayBuilder();
        if(players!=null){
            for(Player player:players){
                if(player==forPlayer)
                    playersArrayBuilder.add(player.toJson(false));
                else
                    playersArrayBuilder.add(player.toJson(true));
            }
        }
        jsonBuilder.add("players", playersArrayBuilder);
        return jsonBuilder.build();
    }
    
    JsonObject getRoomAsJson(Session forSession){
     instanceLock.lock();
     try{
         return toJson(getPlayerFromSession(forSession));
     }finally{instanceLock.unlock();}
    }
    
    /**
     * Returns the number of the currently active game play in the room. When a new game play starts, the gamePlay number is incremented by 1. The game number of the first game play is 1. This value is mainly for debugging purposes to identify which game play number the debug message belongs to
     * @return int - the game play number of the currently active game play in the room.
     */
    public int getCurrentGamePlayNo(){
        return gameNo;
    }
    
    Player getPlayerFromSession(Session session){
        Room sessionRoom=session.room;
        int sessionSeatNo=session.seatNo;
        instanceLock.lock();
        try{
            if(players!=null && sessionRoom==this && sessionSeatNo>0){
                Player player=players.get(sessionSeatNo);
                if(player!=null && player.session==session)
                    return player;
            }
            return null;
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Adds a client session to the room. This session is simply a spectator and cannot take part in the game play till the user takes a seat.
     * @param session - the user session that wants to join the room
     */
    void join(Session session){        
        instanceLock.lock();
        try{
            if(destroyed){
                session.sendError(ErrorCodes.roomDestroyed, ErrorDescriptions.roomDestroyed);
                return;
            }
            //see if session has already joined room
            Room oldRoom=session.room;
            JsonObjectBuilder dataToSendBuilder;
            if(oldRoom==this){
                dataToSendBuilder=JsonUtil.createObjectBuilder();
                dataToSendBuilder.add("gameUserData", session.getGameUserDataForClientAsJson());
                dataToSendBuilder.add("room", toJson(getPlayerFromSession(session)));
                session.sendMesg(Commands.roomJoined, dataToSendBuilder.build().toString());
                return;
            }
            if(oldRoom!=null){                
                oldRoom.leave(session, false);
            }
            //add session to set
            spectators.add(session);
            session.setRoom(this);
            onRoomJoined(session);
            dataToSendBuilder=JsonUtil.createObjectBuilder();
            dataToSendBuilder.add("gameUserData", session.getGameUserDataForClientAsJson());
            dataToSendBuilder.add("room", toJson(null));
            session.sendMesg(Commands.roomJoined, dataToSendBuilder.build().toString());
        }
        finally{instanceLock.unlock();}
    }
    
    /**
     * Sends Room Data to all the client sessions who have joined the room (both spectators and players)
     */
    public void sendRoomDataToAll(){
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            if(this.game.isPlayerDataSameForAllClients)
                sendMesgToAll(Commands.roomData, toJson(null).toString(), null);
            else{
                try{
                    createPlayersTmpJson();
                    sendMesgToSpectators(Commands.roomData, toJson(null).toString(), null);
                    for(Player player:players)
                        player.session.sendMesg(Commands.roomData, toJson(player).toString());
                }finally{clearPlayersTmpJson();}
            }
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Sends Room Data to the specified session
     * @param session - the session to which the room actionData should be sent
     */
    public void sendRoomDataToSession(Session session){
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            session.sendMesg(Commands.roomData, toJson(getPlayerFromSession(session)).toString());
        }finally{instanceLock.unlock();}            
    }
    
    /**
     * Sends Room Data to the specified player's client session
     * @param player - the player to whose client the room actionData should be sent
     */
    public void sendRoomDataToSession(Player player){
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            player.session.sendMesg(Commands.roomData, toJson(player).toString());
        }finally{instanceLock.unlock();}
    }
    
    
    /**
     * Prevents new game play from auto starting after a Game play ends. Should be called in afterGamePlayEnded() event to prevent a new game play from starting
     */
    public final void cancelAutoStartGamePlay(){
        cancelAutoStartGame.set(true);
    }
    
    /**
     * Seats a client session in the room which transforms the client session from a spectator to a player.
     * @param session - the client session
     * @param seatNo - the seat number to be seated on
     * @param takeSeatData - additional actionData as json. For example in poker the additional actionData could be the number of chips to add to the table.
     */
    void takeSeat(Session session,int seatNo,JsonObject takeSeatData){
        instanceLock.lock();
        try{
            if(destroyed){
                session.sendError(ErrorCodes.roomDestroyed, ErrorDescriptions.roomDestroyed);
                return;
            }
            if(seatNo<1 && seatNo>totalNoOfSeats){
                session.sendError(ErrorCodes.invalidSeatNo, ErrorDescriptions.invalidSeatNo + "! Should be between 1 and " + totalNoOfSeats);
                return;
            }
            if(session.room!=this)
                this.join(session);
            if(session.room!=this)
                return;
            //if seat already taken by user then send mesg to player return 
            if(session.seatNo>0){                
                Player player=players.get(session.seatNo);
                if(player!=null && player.session==session){
                    JsonObjectBuilder dataToSendBuilder=JsonUtil.createObjectBuilder();
                    dataToSendBuilder.add("gameUserData", session.getGameUserDataForClientAsJson());
                    dataToSendBuilder.add("player", player.toJson(false));
                    session.sendMesg(Commands.seatTaken, dataToSendBuilder.build().toString());
                    return;
                }
            }            
            //test if allowed to take seat
            if(!players.isSeatFree(seatNo)){
                session.sendError(ErrorCodes.seatNotFree, ErrorDescriptions.seatNotFree);
                return;
            }
            errorDesc=null;
            if(!canSeatBeTaken(session,seatNo,takeSeatData)){
                session.sendError(ErrorCodes.inElligibleToTakeSeat, errorDesc);
                return;
            }
            Player newPlayer=newPlayer(session,seatNo,takeSeatData);
            if(newPlayer==null)
                throw new NullPointerException(Room.this.getClass().getSimpleName() + ".newPlayer() cannot return null");
            if(players.add(newPlayer, seatNo)){
                spectators.remove(session);
                session.seatNo=seatNo;
                onSeatTaken(newPlayer);
                //send mesg 
                JsonObjectBuilder dataToSendBuilder=JsonUtil.createObjectBuilder();
                dataToSendBuilder.add("gameUserData", JsonValue.NULL);
                dataToSendBuilder.add("player", newPlayer.toJson(true));
                sendMesgToAll(Commands.seatTaken, dataToSendBuilder.build().toString(), session);

                dataToSendBuilder=JsonUtil.createObjectBuilder();
                dataToSendBuilder.add("gameUserData", session.getGameUserDataForClientAsJson());
                dataToSendBuilder.add("player", newPlayer.toJson(false));
                session.sendMesg(Commands.seatTaken, dataToSendBuilder.build().toString());
                cancelAutoStartGame.set(false);
                afterSeatTaken(newPlayer);
                session.gameDataModified();
                if(!gameInProgress && !destroyed && !cancelAutoStartGame.get() && elligiblePlayerCount()>=minNoOfPlayersForGame){
                    int nonBotUserCount=nonBotPlayerCount() + spectatorCount();
                    //if there is at least 1 non bot user in the room
                    if(nonBotUserCount>0)
                        delayedStartGame(100);
                }
            }            
        }finally{instanceLock.unlock();}
                
    }
    
    
    /**
     * Starts a new game play in the room if no game play is running. If a game play is already running in the room then nothing is done.
     */
    public final void startGamePlay(){
        instanceLock.lock();
        try{
            delayedStartGameInvoked=false;
            if(destroyed ||  gameInProgress ){
                return;
            }
            if(turnTimeOutChecker!=null)
                Executor.removeExecuteWait(turnTimeOutChecker);
            turnTimeOutChecker=null;

            activePlayers.clear();
            List<Player> playersNotElligibleToPlay=new ArrayList(3);
            //add to activePlayers those who are elligible and kick out those who are not elligible
            for(Player player:players){
                if(player.canJoinGamePlay())
                    activePlayers.add(player, player.seatNo);
                else
                    playersNotElligibleToPlay.add(player);
            }
            String reason;
            for(Player player:playersNotElligibleToPlay){
                reason=player.onCantJoinGamePlay();
                if(reason!=null){
                    JsonObjectBuilder json=JsonUtil.createObjectBuilder();
                    json.add("reason", reason);                    
                    player.session.sendMesg(Commands.notElligibeToPlay, json.build().toString());
                }
            }
            if(activePlayers.getOccupiedCount()<minNoOfPlayersForGame){
                activePlayers.clear();
                sendRoomDataToAll();
                return;
            }
            cancelStartGame.set(false);
            //call beforeGameStarted
            beforeGamePlayStarted();
            if(cancelStartGame.get()){
                activePlayers.clear();
                return;
            }
            gameNo++;
            playersWhoPlayedGame.clear();
            for(Player player:activePlayers)
                playersWhoPlayedGame.add(player);
            gameInProgress=true;
            //send new game mesg to all players and spectators
            if(this.game.isPlayerDataSameForAllClients)
                sendMesgToAll(Commands.newGame,toJson(null).toString(),null);
            else{
                try{
                    createPlayersTmpJson();
                    for(Player player:players)
                        player.session.sendMesg(Commands.newGame, toJson(player).toString());
                    sendMesgToSpectators(Commands.newGame, toJson(null).toString(), null);
                }finally{clearPlayersTmpJson();}
            }
            delayedAfterGameStarted(delayBeforeFirstTurnMillis);
        }
        catch(Exception ex){
            gameInProgress=false;
            Utils.log(ex);
        }
        finally{instanceLock.unlock();}
    }
    
    /**
     * Cancels new Game play before first turn. Should be called in the beforeGameStarted event to cancel the new Game play.
     */
    public final void cancelStartGamePlay(){
        cancelStartGame.set(true);
    }
    
    private final void delayedStartGame(int delayInMillis){
        instanceLock.lock();
        try{
            if(delayedStartGameInvoked)
                return;
            delayedStartGameInvoked=true;
        }
        finally{
            instanceLock.unlock();
        }
        if(delayInMillis<1){
            startGamePlay();
            return;
        }
        Executor.executeWait(new DelayedStartGame(), delayInMillis);
            
    }
    
    
    
    private void delayedAfterGameStarted(int delayInMillis) throws PlayerNotActiveException{
        if(delayInMillis<1){
            Player nextPlayer=afterGamePlayStarted();
            if(nextPlayer==null)
                throw new NullPointerException(Room.this.getClass().getSimpleName() + ".afterGamePlayStarted() cannot return null");
            nextTurn(nextPlayer, getNextTurnData(nextPlayer));            
            return;
        }
        Executor.executeWait(new DelayedAfterGameStarted(gameNo), delayInMillis);            
    }
    
    
    private final void nextTurn(Player nextPlayer,JsonObject turnData) throws PlayerNotActiveException{
        instanceLock.lock();
        try{
            if(destroyed || !gameInProgress)
                return;
            turnTimeOutChecker=null;
            if(!nextPlayer.isActive())
                throw new PlayerNotActiveException();
            //reset prevTurn time left millis etc
           curTurnSeatNo=nextPlayer.seatNo;
           nextPlayer.turnEndTimeInMillis=System.currentTimeMillis()+turnDurationMillis;
           if(turnData==null)
               turnData=EMPTY_JSON_OBJECT;
           //send next turn to everyone
            JsonObject jsonGameData=this.prepareJsonForClient();
            if(jsonGameData==null)
                jsonGameData=EMPTY_JSON_OBJECT;

        if(this.game.isPlayerDataSameForAllClients){
                JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
                jsonBuilder.add("data", jsonGameData);
                jsonBuilder.add("turnData", turnData);
                jsonBuilder.add("turnPlayer", nextPlayer.toJson(false));
                sendMesgToAll(Commands.nextTurn, jsonBuilder.build().toString(), null);
           }
           else{
                JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
                jsonBuilder.add("data", jsonGameData);
                jsonBuilder.add("turnData", turnData);
                jsonBuilder.add("turnPlayer", nextPlayer.toJson(false));
                nextPlayer.session.sendMesg(Commands.nextTurn, jsonBuilder.build().toString());
                jsonBuilder=JsonUtil.createObjectBuilder();
                jsonBuilder.add("data", jsonGameData);
                jsonBuilder.add("turnData", turnData);
                jsonBuilder.add("turnPlayer", nextPlayer.toJson(true));
                sendMesgToAll(Commands.nextTurn, jsonBuilder.build().toString(), nextPlayer.session);
           }
           //start turn time out interval
           if(this.turnTimeOutChecker!=null)
               Executor.removeExecuteWait(this.turnTimeOutChecker);
           this.turnTimeOutChecker=Executor.executeWait(new TurnTimeOutChecker(nextPlayer), turnDurationMillis+600);
        }finally{instanceLock.unlock();}
    }
    

    /**
     * Creates and sends a gameAction event/message to all the client's (client sessions - both spectators and players) connected to this room. for eg. in poker this method can be called when the flop, turn and river card/s is dealt
     * @param action - the action
     * @param data - the actionData of the action in json format. For example in poker if the action is 'flop', the actionData will be the value of the flop cards
     */
    public void gameAction(String action,JsonObject data){
        instanceLock.lock();
        try{
            if(destroyed || !gameInProgress)
                return;            
            if(data==null)
                data=EMPTY_JSON_OBJECT;
            //send turnPlayed to everyone
           JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
           jsonBuilder.add("action", action);
           jsonBuilder.add("data", data);
            if(this.game.isPlayerDataSameForAllClients){
                jsonBuilder.add("room", toJson(null));
                sendMesgToAll(Commands.gameAction, jsonBuilder.build().toString(), null);
            }
           else{
                try{
                    createPlayersTmpJson();
                    jsonBuilder.add("room", toJson(null));
                    sendMesgToSpectators(Commands.gameAction, jsonBuilder.build().toString(), null);                
                    for(Player player:players){
                        jsonBuilder=JsonUtil.createObjectBuilder();
                        jsonBuilder.add("action", action);
                        jsonBuilder.add("data", data);
                        jsonBuilder.add("room", toJson(player));                    
                        player.session.sendMesg(Commands.gameAction, jsonBuilder.build().toString());
                    }
                }finally{clearPlayersTmpJson();}
           }
        }finally{instanceLock.unlock();}                
    }
    
    
    /**
     * Sends turnPlayed or outOfTurnPlayed event/message to clients. Should be called in onPlayerAction() event after the player's action has been successfully processed
     * @param playerAction - the player's processed action. for example in poker it could be  'folded' or 'raised'. Cannot be null. Can be empty String
     * @param actionData - the action data in json. for eg data could be {"amtBet":48,"raisedBy":20}
     * @throws NullPointerException - if playerAction is null
     */
    public final void actionPlayed(String playerAction,JsonObject actionData){
        if(playerAction==null)
            throw new NullPointerException("playerAction cannot be null");
        this.playerAction=playerAction;
        this.data=actionData;
    }
    
    /**
     * Sets the error description to be sent to client. For example in canSeatBeTaken() if chips are too less to take seat  you can set the error description to 'Chips too less to take seat. Min chips required 100'
     * @param errorDescription - the error description
     */
    public void setErrorDescription(String errorDescription){
        errorDesc=errorDescription;
    }

    
    /**
     * Sets the errorDescription and errorData that is sent to client when the action is not valid. Call this method in onPlayerAction() event to invalidate the action.  For example in poker if the amt bet is too less, you can invalidate the action with an error description of 'Bet value too less.' and errorData as {"minBet":50,"actualBet":20}
     * @param errorDescription - the error description 
     * @param errorData - the error data
     */
    public void invalidateAction(String errorDescription,JsonObject errorData){
        this.errorDesc=errorDescription;
        this.data=errorData;
    }
    
    
    final void forceAction(Session session,JsonObject dataAsJsonObject){
        instanceLock.lock();
        try{
            if(destroyed || !gameInProgress){
                return;
            }
            Player player=getPlayerFromSession(session);
            if(player==null)
                return;
            forceAction(player, JsonUtil.getAsString(dataAsJsonObject, "action"), JsonUtil.getAsJsonObject(dataAsJsonObject, "data"), false,false,false);
        }
        finally{instanceLock.unlock();}
    }

    /**
     * Forces an action on the specified player. Usually called in turnTimedOut() event to play a default action on a player whose turn has timed out. For example in poker you can call this method in onTurnTimedOut() event to force the default action of 'check' or 'fold' on the player.
     * @param player - the player on whom to force the action
     * @param action - the action to be forced
     * @param actionData - the action data
     */
    public final void forceAction(Player player,String action,JsonObject actionData){
        if(player==leaveSeatPlayer){
            this.playerAction=action;
            this.data=actionData;
            return;
        }
        forceAction(player, action, actionData, false, false, false);
    }
    
    final void forceAction(Player player,String action,JsonObject actionData,boolean isLeavingSeat,boolean isLeavingRoom,boolean isLoggingOff){
        instanceLock.lock();
        try{
            if(destroyed || !gameInProgress){
                return;
            }
            boolean outOfTurn=getCurTurnPlayer()!=player;
            this.playerAction=null;
            this.data=null;
            this.errorDesc=null;
            onPlayerAction(player, action, actionData, outOfTurn);            
            if(this.playerAction==null){
                JsonObjectBuilder invalidActionData=JsonUtil.createObjectBuilder();
                invalidActionData.add("action", action);
                invalidActionData.add("desc", this.errorDesc);
                invalidActionData.add("data", this.data);
                //send invalid action mesg and return
                if(!outOfTurn)
                    player.session.sendMesg(Commands.invalidAction,invalidActionData.build().toString());
                if(isLeavingSeat)
                    removePlayerFromSeat(player, isLeavingRoom, isLoggingOff);
                return;
            }
            if(!outOfTurn && turnTimeOutChecker!=null){
                Executor.removeExecuteWait(turnTimeOutChecker);
                turnTimeOutChecker=null;
            }
            //send turnplayed or outOfTurnPlayed mesg to spectators and players
            JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
            jsonBuilder.add("playerAction", this.playerAction);
            jsonBuilder.add("actionData", this.data);
            jsonBuilder.add(outOfTurn?"playerSeatNo":"turnSeatNo", player.seatNo);
            if(this.game.isPlayerDataSameForAllClients){
                jsonBuilder.add("room", toJson(null));
                sendMesgToAll(outOfTurn?Commands.outOfTurnPlayed:Commands.turnPlayed, jsonBuilder.build().toString(), null);
            }
            else{
                try{
                    createPlayersTmpJson();
                    jsonBuilder.add("room", toJson(null));
                    sendMesgToSpectators(outOfTurn?Commands.outOfTurnPlayed:Commands.turnPlayed, jsonBuilder.build().toString(), null);                
                    for(Player playerToMesg:players){
                        jsonBuilder=JsonUtil.createObjectBuilder();
                        jsonBuilder.add("playerAction", this.playerAction);
                        jsonBuilder.add("actionData", this.data);
                        jsonBuilder.add(outOfTurn?"playerSeatNo":"turnSeatNo", player.seatNo);
                        jsonBuilder.add("room", toJson(playerToMesg));                 
                        playerToMesg.session.sendMesg(outOfTurn?Commands.outOfTurnPlayed:Commands.turnPlayed, jsonBuilder.build().toString());
                    }
                }finally{clearPlayersTmpJson();}
           }
           //if leaving seat call leaveSeat
           int gameNo=this.gameNo;
           if(isLeavingSeat)
               removePlayerFromSeat(player, isLeavingRoom, isLeavingRoom);
           if(!gameInProgress || gameNo!=this.gameNo)
               return;
            //if activePlayers < minNoOfPlayers game over return
            if(activePlayers.getOccupiedCount()<this.minNoOfPlayersForGame){
                this.endGamePlay();
                return;
            }
            if(outOfTurn)
                return;
            Player nextPlayer=afterTurnPlayed(player, this.playerAction, this.data);
            if(gameNo!=this.gameNo || nextPlayer==null)
                return;
            this.nextTurn(nextPlayer, getNextTurnData(nextPlayer));
        }catch(Exception ex){
            Utils.log(ex);
        }
        finally{instanceLock.unlock();}        
    }
    
    /**
     * Checks whether Game play is ending or not. For example in poker this method can be called in Player.getGameDataToJsonForOthers() method to decide if a player's hole cards should be sent to other clients or not because on GameEnd your cards are visible to all players
     * @return boolean - true is Game is ending
     */
    public final boolean isGamePlayEnding(){
        return gameEnding;
    }
    
    /**
     * Ends the currently running game play in the room
     */
    public final void endGamePlay(){
        boolean shouldCancelAutoStartGame=false;
        int nonBotUserCount=0;
        instanceLock.lock();
        try{
            if(!gameInProgress || destroyed){
                return;
            }
            if(turnTimeOutChecker!=null)
                Executor.removeExecuteWait(turnTimeOutChecker);
            turnTimeOutChecker=null;
            gameEnding=true;
            //call beforeEndGame
            JsonObject endGameData=beforeGamePlayEnded();
            //reset curTurn Player
            curTurnSeatNo=0;
            //send endGame mesg
            JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
            jsonBuilder.add("endGameData", endGameData);
            if(this.game.isPlayerDataSameForAllClients){
                createPlayersTmpJson();
                jsonBuilder.add("room", toJson(null));
                sendMesgToAll(Commands.endGame, jsonBuilder.build().toString(), null);
            }
            else
            {
                try{
                    createPlayersTmpJson();
                    jsonBuilder.add("room", toJson(null));
                    sendMesgToSpectators(Commands.endGame, jsonBuilder.build().toString(), null);                
                    for(Player player:players){
                        jsonBuilder=JsonUtil.createObjectBuilder();
                        jsonBuilder.add("endGameData", endGameData);
                        jsonBuilder.add("room", toJson(player));                    
                        player.session.sendMesg(Commands.endGame, jsonBuilder.build().toString());
                    }
                }finally{clearPlayersTmpJson();}
            }            
            //clear players in game and reset game in progress
            gameInProgress=false;
            activePlayers.clear();
            this.cancelAutoStartGame.set(false);
            afterGamePlayEnded();
            shouldCancelAutoStartGame=this.cancelAutoStartGame.get();
            for(Player player:playersWhoPlayedGame){
                player.session.gameDataModified();
            }
            //reset player actionData and game actionData
            for(Player player:players){
                if(player!=null)
                    player.resetData();
            }
            this.resetData();
            if(!shouldCancelAutoStartGame){
                nonBotUserCount=nonBotPlayerCount() + spectatorCount();
            }
        }finally{
            gameEnding=false;
            gameInProgress=false;
            instanceLock.unlock();
        }
        if(!shouldCancelAutoStartGame && nonBotUserCount>0){
            delayedStartGame(delayAfterGameEndedMillis);
        }

    }
    
    /**
     * Called after End Game play to reset game play data. This method should be overridden in the child class
     */
    abstract protected void resetData();//called in endGame to reset game actionData
         
    void exitGamePlay(Player player){
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            if(activePlayers.get(player.seatNo)==player)
                activePlayers.remove(player.seatNo);
        }finally{instanceLock.unlock();}
    }
            
    /**
     * Evicts the specified client session from his/her seat
     * @param session - the client session to be evicted
     */
    public final void leaveSeat(Session session){
        leaveSeat(session,false,false);
    }
    
    /**
     * Evicts the specified player from his/her seat. This method is similar to leaveSeat(Session session)
     * @param player - the player to be evicted
     */
    public final void leaveSeat(Player player){
        leaveSeat(player.session, false,false);
    }
    
    
    private void leaveSeat(Session session,boolean isLeavingRoom,boolean isLoggingOff){
      instanceLock.lock();
      try{
        if(destroyed)
            return;
        Player player=players.get(session.seatNo);
        if(player==null || player.session!=session)
            return;
        int curTurnSeatNo=this.curTurnSeatNo;
        this.playerAction=null;
        this.data=null;
        this.leaveSeatPlayer=player;
        beforeSeatLeft(player,player.seatNo==curTurnSeatNo,isLeavingRoom, isLoggingOff);
        this.leaveSeatPlayer=null;
        if(this.playerAction!=null && gameInProgress)
            forceAction(player, this.playerAction, this.data, true, isLeavingRoom, isLoggingOff);
        else
            removePlayerFromSeat(player, isLeavingRoom, isLoggingOff);
      }finally{this.leaveSeatPlayer=null;instanceLock.unlock();}
    }
    
    private void removePlayerFromSeat(Player player,boolean isLeavingRoom,boolean isLoggingOff){
        Session session=player.session;
        //remove from players in Game and players
        activePlayers.remove(player.seatNo);
        players.remove(player.seatNo);
        session.seatNo=0;
        //add player to spectator
        if(!isLeavingRoom)
            spectators.add(session);
        //send left command to all players
        //if leavingRoom then dont send to cur player
        JsonObjectBuilder dataToSend=JsonUtil.createObjectBuilder();
        dataToSend.add("seatNo", player.seatNo);
        dataToSend.add("userId", session.getUser().userId);
        if(isLeavingRoom){
            JsonObject json=dataToSend.build();
            sendMesgToAll(Commands.seatLeft, json.toString(), session);
        }
        else {
            JsonObject json=dataToSend.build();
            sendMesgToAll(Commands.seatLeft, json.toString(), session);
            dataToSend=JsonUtil.createObjectBuilder(json);
            dataToSend.add("gameUserData", session.getGameUserDataForClientAsJson());
            json=dataToSend.build();            
            session.sendMesg(Commands.seatLeft, json.toString());
        }
        afterSeatLeft(player, player.seatNo==curTurnSeatNo, activePlayers.getOccupiedCount(),isLeavingRoom, isLoggingOff);
        session.gameDataModified();
    }
     
    /**
     * Evicts the specified client session from the room. 
     * @param session - the client session to be evicted
     */
    public final void leave(Session session){
        leave(session,false);
    }
    
    void leave(Session session,boolean isLogingOff){        
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            boolean isPlayer;            
            Player player=session.seatNo>0?players.get(session.seatNo):null;
            isPlayer=player!=null && player.session==session;
            if(isPlayer){
                leaveSeat(session,true,isLogingOff);
            }
            
            session.removeRoom(this);
            boolean isSpectator=spectators.remove(session);
            if(isSpectator || isPlayer)
                onRoomLeft(session,isLogingOff);
            if(!isLogingOff){
                JsonObjectBuilder dataToSend=JsonUtil.createObjectBuilder();
                dataToSend.add("gameUserData", session.getGameUserDataForClientAsJson());
                session.sendMesg(Commands.roomLeft, dataToSend.build().toString());
            }
            if(ownerUserId!=null){
                if(spectatorCount()==0 && playerCount()==0)
                    removePrivateRoom();
            }
        }finally{instanceLock.unlock();}
    } 

    
    private void shutDown(){
        instanceLock.forceLock(400);
        try{
            if(destroyed)
                return;
        //set gameInProgress to false
            gameInProgress=false;
            for(Player player:players){
                player.onShutDown();
                player.session.removeRoom(this);
                player.session.gameDataModified();
            }
            for(Session session:spectators)
                session.removeRoom(this);
            spectators.clear();                        
            destroyed=true;
            if(ownerUserId!=null)
                GameDB.savePrivateRoom(ownerUserId, game.gameName, toJsonForPrivateRoomSave());
                
        }finally{instanceLock.unlock();}        
    }
           
    /**
     * Returns whther a game play is in progress or not in the room
     * @return boolean - true is a game play is in progress
     */
    public final boolean isGamePlayInProgress(){
        return gameInProgress;
    }
    
    
    /**
     * returns all the client session who are spectators
     * @return Iterable&lt;Session&gt; - the spectators as an iterable
     */
    public final Iterable<Session> getSpectators(){
        return iterableForSepectators;
    }
    
    
    /**
     * Returns all the players who took part in the current game play. 
     * @return Iterable&lt;Player&gt; - the players who took part in the current game play
     */
    public final Iterable<Player> getPlayersWhoPlayed(){
        return iterableForPlayersWhoPlayed;
    }
    
    /**
     * Returns whether the specified client session is seated or not
     * @param session - the client session
     * @return boolean - true if the specified client session is seated
     */
    public final boolean isSeated(Session session){
        return getPlayerFromSession(session)!=null;
    }
    
    /**
     * returns whether specified client session is a spectator or not. A spectator is a client session which is not seated
     * @param session - the client session
     * @return boolean - true if the specified session is a spectator
     */
    public final boolean isSpectator(Session session){
        return spectators.contains(session);
    }
    
    /**
     * Checks whether the specified player is still active in the current game play. For example in a game like poker player will remain active till the player folds or the game ends
     * @param player - the player
     * @return boolean - true if the specified player is active in the current game play
     */
     final boolean isActive(Player player){
        return activePlayers.get(player.seatNo)==player;
    }
    
    /**
     * checks whether player was or is active in the current game play - returns true if a player was active when the current game play began.
     * @param player - the player
     * @return boolean - true if the specified player is active in the current game
     */
     final boolean wasActive(Player player){
        return playersWhoPlayedGame.contains(player);
    }
    
    /**
     * the total number of seats in the room
     * @return int - total number of seats
     */
    public final int totalNoOfSeats(){
        return players!=null?players.getCapacity():0;
    }
    
    /**
     * the number of players in the room ( the number of session seated in the room)
     * @return int - number of players
     */
    public final int playerCount(){
        return players!=null?players.getOccupiedCount():0;
    }
    
    /**
     * The number of players active (i.e. playing) in the current game play. (i.e number of players who have not exited the game play)
     * @return int - number of players active in the current game play
     */
    public final int activePlayerCount(){
        return activePlayers!=null?activePlayers.getOccupiedCount():0;
    }
    
    /**
     * The number of non bot players in the room. (the number of non bot sessions seated in the room)
     * @return int - The number of non bot players in the room
     */
    public final int nonBotPlayerCount(){
        int count=0;
        if(players==null)
            return 0;
        for(Player player:players){
            if(!player.session.user.isBot)
                count++;
        }
        return count;
    }
    
    /**
     * The number of non bot players active in the current game play. (i.e number of non bot players who have not exited the game play)
     * @return int - number of non bot players active in the current game play
     */
    public final int activeNonBotPlayerCount(){
        int count=0;
        if(activePlayers==null)
            return 0;
        for(Player player:activePlayers){
            if(!player.session.user.isBot)
                count++;
        }
        return count;
    }
    
    /**
     * The number of bot players in the room. (the number of bot sessions seated in the room)
     * @return int - The number of bot players in the room
     */
    public final int botPlayerCount(){
        int count=0;
        if(players==null)
            return 0;
        for(Player player:players){
            if(player.session.user.isBot)
                count++;
        }
        return count;
    }
    
    /**
     * The number of bot players active in the current game play - (i.e number of bot players who have not exited the game play)
     * @return int - number of bot players in the current game
     */
    public final int activeBotPlayerCount(){
        int count=0;
        if(activePlayers==null)
            return 0;
        for(Player player:activePlayers){
            if(player.session.user.isBot)
                count++;
        }
        return count;
    }
    
    /**
     * The number of spectators (sessions not seated)
     * @return int - the number of spectators
     */
    public final int spectatorCount(){
        return spectators!=null?spectators.size():0;
    }
    
    /**
     * Returns the room's location
     * @return Location - the room's location
     */
    public final Location getLocation() {
        return location;
    }

    /**
     * Returns the turn duration in seconds
     * @return int - the turn duration in seconds
     */
    public final int getTurnDurationInSecs() {
        return turnDurationMillis/1000;
    }

    
    private void deInitialize(){
        this.location=null;
        this.players.clear();
        this.activePlayers.clear();
        this.playersWhoPlayedGame.clear();
        this.spectators.clear();
        players=null;
        activePlayers=null;
        spectators=null;
    }
    
    /**
     * Destroys the room.
     */
    public final void destroy(){
        //destroy
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            if(ownerUserId==null){//is a public room
                location.removeRoom(this);
            }
            else
                game.removePrivateRoom(ownerUserId, true);
            gameInProgress=false;
            destroyed=true;
            onRoomDestroyed();
            JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
            jsonBuilder.add("roomId", roomId);
            jsonBuilder.add("gameUserData", JsonValue.NULL);
            String dataToSend=jsonBuilder.build().toString();
            for(Session session:spectators){
                session.sendMesg(Commands.roomDestroyed, dataToSend);
                session.removeRoom(this);
            }
            JsonObject playerData;
            for(Player player:players){
                player.onRoomDestroyed();
                player.session.gameDataModified();
                jsonBuilder=JsonUtil.createObjectBuilder();
                jsonBuilder.add("roomId", roomId);
                jsonBuilder.add("gameUserData", player.session.getGameUserDataForClientAsJson());
                player.session.sendMesg(Commands.roomDestroyed, jsonBuilder.build().toString());
                player.session.removeRoom(this);
            }
            deInitialize();
        }finally{instanceLock.unlock();}
        unregister(roomId);
    }
    
    void removePrivateRoom(){
        instanceLock.lock();
        try{
            if(ownerUserId==null || spectatorCount()!=0 || playerCount()!=0)
                return;
            long currentTime=System.currentTimeMillis();
            if(removePrivateRoomRunnable!=null){
                if(currentTime-whenPrivateRoomStarted < 300000)
                    return;
                Executor.removeExecuteWait(removePrivateRoomWaitRunnable);
            }
            removePrivateRoomRunnable=new RemovePrivateRoom();            
            removePrivateRoomWaitRunnable=Executor.executeWait(removePrivateRoomRunnable,1200000);
            whenPrivateRoomStarted=currentTime;
        }finally{instanceLock.unlock();}
    }
    
    
    void sendMesgToAll(String command,String data,Session sessionNotToSend){
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            for(Session session:spectators){
                if(session==sessionNotToSend)
                    continue;
                session.sendMesg(command, data);
            }
            for(Player player:players){
                if(player.session==sessionNotToSend)
                    continue;
                player.session.sendMesg(command, data);
            }
                
        }finally{instanceLock.unlock();}
    }
    
    void sendMesgToSpectators(String command,String data,Session spectatorNotToSend){
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            for(Session session:spectators){
                if(session==spectatorNotToSend)
                    continue;
                session.sendMesg(command, data);
            }                
        }finally{instanceLock.unlock();}
        
    }
    
    void sendMesgToPlayers(String command,String data,Session sessionNotToSend){
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            for(Player player:players){
                if(player.session==sessionNotToSend)
                    continue;
                player.session.sendMesg(command, data);
            }
                
        }finally{instanceLock.unlock();}
    }
    
    void sendMesgToPlayers(String command,String data,Player playerNotToSend){
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            for(Player player:players){
                if(player==playerNotToSend)
                    continue;
                player.session.sendMesg(command, data);
            }
                
        }finally{instanceLock.unlock();}
    }
            
    /**
     * Sends a Json message to all sessions in the room except the one specified.
     * @param command - the message command
     * @param data - the message data as json
     * @param sessionNotToSend - the session to which message should not be sent. Can be null.
     */
    public final void sendJsonMessageToAll(String command,JsonObject data,Session sessionNotToSend){
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            for(Session session:spectators){
                if(session==sessionNotToSend)
                    continue;
                session.sendMessage(command, data);
            }
            for(Player player:players){
                if(player.session==sessionNotToSend)
                    continue;
                player.session.sendMessage(command, data);
            }
                
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Sends a Json message to all spectators in the room except the one specified.
     * @param command - the message command
     * @param data - the message data as json
     * @param sessionNotToSend - the session to which message should not be sent. Can be null.
     */
    public final void sendJsonMessageToSpectators(String command,JsonObject data,Session sessionNotToSend){
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            for(Session session:spectators){
                if(session==sessionNotToSend)
                    continue;
                session.sendMessage(command, data);
            }                
        }finally{instanceLock.unlock();}
        
    }
    
    /**
     * Sends a Json message to all players in the room except the one specified.
     * @param command - the message command
     * @param data - the message data as json
     * @param sessionNotToSend - the session to which message should not be sent. Can be null.
     */
    public final void sendJsonMessageToPlayers(String command,JsonObject data,Session sessionNotToSend){
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            for(Player player:players){
                if(player.session==sessionNotToSend)
                    continue;
                player.session.sendMessage(command, data);
            }
                
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Sends a text message to all sessions in the room except the one specified.
     * @param command - the message command
     * @param data - the message data/text 
     * @param sessionNotToSend - the session to which message should not be sent. Can be null.
     */
    public final void sendTxtMessageToAll(String command,String data,Session sessionNotToSend){
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            for(Session session:spectators){
                if(session==sessionNotToSend)
                    continue;
                session.sendMessage(command, data);
            }
            for(Player player:players){
                if(player.session==sessionNotToSend)
                    continue;
                player.session.sendMessage(command, data);
            }
                
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Sends a text message to all spectators in the room except the one specified.
     * @param command - the message command
     * @param data - the message data/text 
     * @param sessionNotToSend - the session to which message should not be sent. Can be null.
     */
    public final void sendTxtMessageToSpectators(String command,String data,Session sessionNotToSend){
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            for(Session session:spectators){
                if(session==sessionNotToSend)
                    continue;
                session.sendMessage(command, data);
            }                
        }finally{instanceLock.unlock();}
        
    }
    
    /**
     * Sends a text message to all players in the room except the one specified.
     * @param command - the message command
     * @param data - the message data/text
     * @param sessionNotToSend - the session to which message should not be sent. Can be null.
     */
    public final void sendTxtMessageToPlayers(String command,String data,Session sessionNotToSend){
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            for(Player player:players){
                if(player.session==sessionNotToSend)
                    continue;
                player.session.sendMessage(command, data);
            }
                
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Returns all the players in the room
     * @return Iterable@lt;Player&gt; - all the players in the room
     */
    public final Iterable<Player> getPlayers(){
        return players!=null?players.getIterable():null;
    }
    
    /**
     * Returns all the players active in the current game play
     * @return Iterable@lt;Player&gt; - all the players active in the current game play
     */
    public final Iterable<Player> getActivePlayers(){
        return activePlayers!=null?activePlayers.getIterable():null;
    }
    
    /**
     * returns the next active player seated after the current turn player.
     * @return Player - the first active player after the current turn player
     */
    public final Player getNextActivePlayer(){
        return activePlayers!=null?activePlayers.getNextOccupant(curTurnSeatNo):null;
   }
    
    /**
     * returns the next active player seated after the specified seat number.
     * @param prevSeatNo - the seat number after which to start searching
     * @return Player - the first active player after the specified seat number
     */
    public final Player getNextActivePlayer(int prevSeatNo){
        return activePlayers!=null?activePlayers.getNextOccupant(prevSeatNo):null;
   }
    
    final int getNextActiveSeatNo(int prevSeatNo){
        return activePlayers!=null?activePlayers.getNextOccupiedSeatNo(prevSeatNo):0;
    }    
    
    /**
     * Returns the first active player 
     * @return Player - the first active player
     */
    public final Player getFirstActivePlayer(){
        return activePlayers!=null?activePlayers.getFirstOccupant():null;
    }
    
    /**
     * Returns the first player in the room
     * @return Player - the first player in the room
     */
    public final Player getFirstPlayer(){
        return players!=null?players.getFirstOccupant():null;
    }
    
    /**
     * Returns the next player seated after the specified seat number
     * @param prevSeatNo - the previous seat number
     * @return Player - the next player seated after the specified seat number
     */
    public final Player getNextPlayer(int prevSeatNo){
        return players!=null?players.getNextOccupant(prevSeatNo):null;
    }
    
    /**
     * Returns the next active player after the current player who can play a turn (player.canPlayTurn() returns true). For example in poker if a player is allIn then he/she cannot play another turn in the game.
     * @return Player - the next active player who can play a turn
     */
    public final Player getNextActivePlayerWhoCanPlayTurn(){
        return getNextActivePlayerWhoCanPlayTurn(curTurnSeatNo);
    }
        
    /**
     * Returns the next active player after the specified seat number who can play a turn (player.canPlayTurn() returns true). For example in poker if a player is allIn then he/she cannot play another turn in the game.
     * @param prevSeatNo - the previous seat number
     * @return Player - the next turn player after the specified seat number who can play a turn
     */
    public final Player getNextActivePlayerWhoCanPlayTurn(int prevSeatNo){
        
        int firstNextPlayerSeatNo=this.getNextActiveSeatNo(prevSeatNo);
        int nextPlayerSeatNo=firstNextPlayerSeatNo;
        Player player;
        while(nextPlayerSeatNo!=prevSeatNo && nextPlayerSeatNo!=0){
            player=getPlayerBySeatNo(nextPlayerSeatNo);            
            if(player.canPlayTurn()){
                return player;
            }
            nextPlayerSeatNo=this.getNextActiveSeatNo(nextPlayerSeatNo);
            if(nextPlayerSeatNo==firstNextPlayerSeatNo)
                break;
        }
        return null;        
    }
    
    /**
     * Returns player seated at the specified seat number
     * @param seatNo - the seat number of the player
     * @return Player - player seated at the specified seat number
     */
    public final Player getPlayerBySeatNo(int seatNo){
        return players!=null?players.get(seatNo):null;
    }
    
    /**
     * Returns the seat number of the player whose turn is the current turn 
     * @return int - the seat number of the player whose turn is the current turn 
     */
    public int getCurTurnSeatNo() {
        return curTurnSeatNo;
    }
    
    /**
     * Returns the player whose turn is the current turn
     * @return Player - player whose turn is the current turn
     */
    public Player getCurTurnPlayer(){
        int curTurnSeatNo=this.curTurnSeatNo;
        if(!gameInProgress || curTurnSeatNo<1)
            return null;
        return activePlayers!=null?activePlayers.get(curTurnSeatNo):null;
    }
        
    /**
     * Returns the first free (not occupied) seat number.
     * @return int - the first free seat number
     */
    public int getFirstFreeSeatno(){
        return players.getFirstFreeSeatNo();
    }
    
    /**
     * Returns the first free (not occupied) seat number after the specified seat number
     * @param prevSeatNo - the previous seat number
     * @return int - the first free seat number after prevSeatNo
     */
    public int getNextFreeSeatNo(int prevSeatNo){
        return players.getNextFreeSeatNo(prevSeatNo);
    }
    
    JsonObject toJsonForPrivateRoomSave(){
        JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
        jsonBuilder.add("roomName", roomName);
        jsonBuilder.add("roomType", roomType.toString());
        jsonBuilder.add("totalNoOfSeats", totalNoOfSeats);
        jsonBuilder.add("turnDurationSecs", turnDurationMillis/1000);
        jsonBuilder.add("additionalConfig", getAdditionalRoomConfig());
        return jsonBuilder.build();
    }     
    
    /**
     * Logs debug messages
     * @param txt - the message to be logged
     */
    public void debug(String txt){
        if(debugEnabled)
            Utils.log(this.location.name + "-" + this.roomName + ": " + txt);
    }
    
    /**
     * Logs debug messages along with the user name when debug is enabled
     * @param session - the session whose user name should be prepended to the debug message
     * @param txt - the message to be logged
     */
    public void debug(Session session,String txt){
        if(debugEnabled){
            debug(session.getUser().getName() + " - " +  txt);
        }
    }
    
    int elligiblePlayerCount(){ //The number of players that can join a new game. The the count of players whose canJoinGame() method returns true
        int count=0;
        for(Player player:players){
            if(player.canJoinGamePlay())
                count++;
        }
        return count;
    }
    
    /**
     * Returns room Data to be sent to the client as part of the room state. Do not include fields defined in the base class Room such as roomId, roomName, etc  For example in poker this data would be the table cards (flop,turn and river), the dealer seatNo, the pot values, the smallBlind etc
     * @return JsonObject - the room data to be sent to client as json
     */
    abstract protected JsonObject prepareJsonForClient();
    
    /**
     * Returns RoomInfo as json. RoomInfo is data that is sent to the client as part of the room list when client requests list of rooms in a given location. For example in poker this could be the smallBlind and the minimum chips to take seat
     * @return JsonObject - the RoomInfo as json
     */
    protected JsonObject getRoomInfo(){
        return null;
    }
    
    /**
     * Event-Handler/Callback called after session is added to room but before joinedRoom message is sent to the client.
     * @param session - the session that has joined the room
     */
    protected void onRoomJoined(Session session){
        
    }
        
    /**
     * Callback called when a session takes seat to check whether session should be allowed to take seat or not. For example in poker if the chips the user wants to put on table is less than the minimum chips to take seat then this method should return false
     * @param session - the session which is taking a seat
     * @param seatNo - the seat number
     * @param data - the data sent by the client with the takeSeat message: For example in poker this could be chipsToPutOnTable
     * @return boolean - true to allow player to take seat
     */
    protected boolean canSeatBeTaken(Session session,int seatNo,JsonObject data){
        return true;
    }

    /**
     * Event-Handler/Callback called after player is seated but before seatTaken message is sent to clients. For example in poker here you could deduct the chip on table from the user's chips in Hand
     * @param player - the player who took seat
     */
    protected void onSeatTaken(Player player){
    }
        
    /**
     * Event-Handler/Callback called after seatTaken message is sent. In this event you could call cancelAutoStartGame() to prevent a game autostart. A new game starts automatically if the minimum players required to start game are seated
     * @param player - the player who took seat
     */
    protected void afterSeatTaken(Player player){        
    }
        
    /**
     * Event-Handler/Callback called before player leaves/evicted from the seat.  For eg. for a game like poker in this event you can add the player's chips on table to user's chips in hand. If the player leaving is in Game you can also play the default action of fold on the player
     * @param player - the player who is leaving seat
     * @param isCurTurnPlayer - whether current turn is of the leaving player
     * @param isLeavingRoom - is the leave seat triggered as a result of leaving the room
     * @param isLoggingOut - is the leave seat triggered as a result of user loging out
     */
    protected void beforeSeatLeft(Player player, boolean isCurTurnPlayer, boolean isLeavingRoom,boolean isLoggingOut){//called before seatNo is set to 0 and player is removed from activePlayers and players list  and before seat left mesg is sent         
    }
        
    /**
     * Event-Handler/Callback called after seat left message is sent
     * @param player - the player who left seat
     * @param isCurTurnPlayer - whether current turn is of player which left
     * @param noOfActivePlayers - number of in game players
     * @param isLeavingRoom - is the leave seat triggered as a result of leaving the room
     * @param isLoggingOut - is the leave seat triggered as a result of user loging out
     */
    protected void afterSeatLeft(Player player, boolean isCurTurnPlayer, int noOfActivePlayers,boolean isLeavingRoom,boolean isLoggingOut){        
    }
        
    /**
     * Event-Handler/Callback called after  room Left but before room Left message is sent to player
     * @param session - session which left room
     * @param isLoggingOut - is the leave room triggered as a result of user loging out
     */
    protected void onRoomLeft(Session session,boolean isLoggingOut){        
    }
        
    /**
     * Event-Handler/Callback called before game play starts and new game play message is sent. In a game like poker you can deal the first cards and play the small blind and big blind in this event
     */
    protected void beforeGamePlayStarted(){ //called before gameInProgress is set to true and new game mesg is sent- here playersInActive will already be set- deal the first cards etc        
    }
        
    /**
     * Event-Handler/Callback called after new game play message is sent - should return the first turn player
     * @return Player - the player whose turn is first
     */
    protected Player afterGamePlayStarted(){
        return this.getFirstActivePlayer();
    }
    
    /**
     * Event-Handler/Callback called when player's turn times out. In this event you should play an action such as fold else turn will not move to the next player
     * @param turnPlayer - the player whose turn timed out
     */
    protected void onTurnTimedOut(Player turnPlayer){
        forceAction(turnPlayer, "", null);
    }
    
    /**
     * Event-Handler/Callback called when player plays turn - should call actionPlayed() (this will send turnPlayed or outOfTurnPlayed event/message to clients)
     * @param player - the player who played the action
     * @param action - the action played
     * @param data - the action data
     * @param isOutOfTurn - is the action out of turn
     */
    protected void onPlayerAction(Player player, String action, JsonObject data, boolean isOutOfTurn){
        this.actionPlayed(action, data);
    }

    /**
     * Event-Handler/Callback called after player plays turn. Its is in this event that you should perform actions to be done after turn has completed. For example in poker if the betting round has completed you could deal the turn card. This method should return the player whose turn is next or null if the game has ended.
     * @param player - the player who played the action
     * @param action - the action played
     * @param actionData - the action data
     * @return - the player whose turn is next or null if the game has ended. Default value is the next active player who can play turn
     */
    protected Player afterTurnPlayed(Player player, String action,JsonObject actionData){
        return this.getNextActivePlayerWhoCanPlayTurn();
    }
    
    /**
     * returns the data to be sent to the client along with the nextTurn event/message to clients. for example in poker this method could return the call value
     * @param nextTurnPlayer - the player whose turn is next. 
     * @return JsonObject - the data to be sent to clients. for eg. {"callValue":20}
     */
    abstract protected JsonObject getNextTurnData(Player nextTurnPlayer);
    
    /**
     * Event-Handler/Callback after endGamePlay() is called but before game play is ended. Should return endGame data to be sent to clients. For eg. the data could contain the winner, winning cards, etc
     * @return JsonObject - the data to be sent to clients along with the endGame event/message. 
     */
    protected JsonObject beforeGamePlayEnded(){
        return null;
    }
    
    /**
     * Event-Handler/Callback called after game play has ended and endGamePlay message has been sent to clients. Here you can do actions such as adding the amount won to the winner's chips on table
     */
    protected void afterGamePlayEnded(){
        
    }
        
    /**
     * Should return the additional configuration data for the room. This method is called when a private room is saved to the database
     * @return JsonObject - returns the additional configuration data for the room as json
     */
    abstract protected JsonObject getAdditionalRoomConfig();//
       
    /**
     * Should set the additional configuration data for the room. This method is called when a private room's configuration is changed
     * @param additionalRoomConfig - the additional configuration data for the room
     */
    abstract protected void setAdditionalRoomConfig(JsonObject additionalRoomConfig);
    
    
    /**
     * Event-Handler/Callback called when room is destroyed but before event/message is sent to clients. For eg in poker you could restore player's chips in hand in this event
     */
    protected void onRoomDestroyed(){
        
    }
        
    /**
     * Event-Handler/Callback called after Room is Destroyed and destroyed event/message is sent to clients. Do deinitialization here
     */
    protected void afterRoomDestroyed(){
        
    }
    
    private static class RoomComparator implements Comparator<Room>{
        @Override
        public int compare(Room o1, Room o2) {
            int result=o1.compareTo(o2);
            if(result!=0)
                return result;
            if(o1.roomId>o2.roomId)
                return 1;
            else if(o1.roomId<o2.roomId)
                return -1;
            return 0;
        }
    }
    private class DelayedStartGame implements Runnable{
        int expectedGameNo;
        
        DelayedStartGame() {
            expectedGameNo=Room.this.gameNo;
        }
        
        @Override
        public void run() {
            if(expectedGameNo!=Room.this.gameNo){
                Utils.log("new game started. delayedStartGame GameNo does not match current gameNo");
                return;
            }
            try{
                startGamePlay();
            }catch(Exception ex){
                Utils.log(ex);
            }
        }
        
    }
    private class DelayedAfterGameStarted implements Runnable{
        int gameNo;
        
        public DelayedAfterGameStarted(int gameNo) {
            this.gameNo = gameNo;
        }
        
        @Override
        public void run() {
            instanceLock.lock();
            try{
                if(!gameInProgress || destroyed || gameNo!=Room.this.gameNo){
                    return;
                }
                if(activePlayers.getOccupiedCount()<minNoOfPlayersForGame){
                    endGamePlay();
                    return;
                }
                Player nextPlayer=afterGamePlayStarted();
                if(nextPlayer==null)
                    throw new NullPointerException(Room.this.getClass().getSimpleName() + ".afterGamePlayStarted() cannot return null");
                nextTurn(nextPlayer, getNextTurnData(nextPlayer));
            }catch(Exception ex){
                gameInProgress=false;
                Utils.log(ex);
            }finally{
                instanceLock.unlock();
            }
        }
    }
    private class TurnTimeOutChecker implements Runnable{
        private Player turnPlayer;
        
        TurnTimeOutChecker(Player turnPlayer) {
            this.turnPlayer = turnPlayer;
        }
        
        @Override
        public void run() {
            instanceLock.lock();
            try{
                if(turnTimeOutChecker==null || turnTimeOutChecker.task!=this || !gameInProgress || destroyed)
                    return;
                turnTimeOutChecker=null;
                if(curTurnSeatNo<1 || activePlayers.get(curTurnSeatNo)!=turnPlayer){
                    return;
                }
                Room.this.turnTimeOutChecker=null;
                //call turnTimedOut
                onTurnTimedOut(turnPlayer);
                
            }finally{instanceLock.unlock();}
        }
        
    }
    private class IterableForSepectators implements Iterable<Session>{
        
        @Override
        public Iterator<Session> iterator() {
            return spectators.iterator();
        }
    }
    private class IterableForPlayersWhoPlayed implements Iterable<Player>{
        @Override
        public Iterator<Player> iterator() {
            return playersWhoPlayedGame.iterator();
        }
    }
    class RemovePrivateRoom implements Runnable{
        @Override
        public void run() {
            instanceLock.lock();
            try{
                if(destroyed || removePrivateRoomRunnable!=this)
                    return;
                if(spectatorCount()!=0 || playerCount()!=0){
                    removePrivateRoomRunnable=null;
                    return;
                }
                String ownerUserId=Room.this.ownerUserId;
                Game game=Room.this.game;
                if(game==null || ownerUserId==null)
                    return;
                game.removePrivateRoom(ownerUserId, false);
                destroyed=true;
                gameInProgress=false;
                deInitialize();
            }finally{instanceLock.unlock();}
            unregister(roomId);
        }
    }
    
    final void configureRoom(int totalNoOfSeats,int turnDurationInSecs,JsonObject additionalRoomConfig) throws SeatsNotEmptyException,GamePlayInProgressException{
        instanceLock.lock();
        try{
            if(this.gameInProgress)
                throw new GamePlayInProgressException();
            if(this.totalNoOfSeats!=totalNoOfSeats){
                if(players!=null && players.getOccupiedCount()>0)
                    throw new SeatsNotEmptyException();                
                this.totalNoOfSeats=totalNoOfSeats;
                players=new Seats(totalNoOfSeats);
                activePlayers=new Seats(totalNoOfSeats);
                playersWhoPlayedGame=new LinkedList();
            }
            if(turnDurationInSecs<1)
                turnDurationInSecs=1;
            if(turnDurationInSecs<Integer.MAX_VALUE/1000)
                turnDurationMillis = turnDurationInSecs*1000;
            else
                turnDurationMillis=Integer.MAX_VALUE;
            setAdditionalRoomConfig(additionalRoomConfig);
        }finally{instanceLock.unlock();}
    }
}
