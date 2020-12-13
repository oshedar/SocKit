/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import io.sockit.servertools.Utils;
import io.sockit.servertools.RandomStringGenerator;
import io.sockit.servertools.Executor;
import io.sockit.servertools.ForceableReentrantLock;
import io.sockit.servertools.CommandDataReadListener;
import io.sockit.servertools.CommandDataSocket;
import javax.json.JsonObject;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.json.JsonObjectBuilder;

/**
 * Represents a client session - i.e. a client connected to the server. 
 * 
 */
public class Session {
    static int maxSessionsPerUser=5;
    final User user;
    private String sessionId;
    private static int expectedMaxActiveSessions=1000;
    private static Map<String,Session> sessions;
    private static Map<String,List<Session>> userIdSessions;
    CommandDataSocket socket;
    volatile Room room;
    int seatNo;
    volatile boolean sessionClosed=false;
    final ForceableReentrantLock lock=new ForceableReentrantLock();
    final Lock socketLock=new ReentrantLock();
    private final int idleSessionTimeOutMillis=50000;//1 min
    private final int idleCheckerInterval=idleSessionTimeOutMillis/5;
    private volatile long lastReadTime;
    private final Runnable idleChecker;
    private volatile GameObj gameObj;
    private volatile GameUserData gameUserData;
    private static SessionListener defaultSessionListener=new SessionListenerAdapter();
    private volatile SessionListener sessionListener;
    private volatile boolean gameDataIsNew=false;
    static SessionListenerFactory sessionListenerFactory=null;
    static final AtomicInteger sessionCount=new AtomicInteger(0);
    static final AtomicInteger nonBotSessionCount=new AtomicInteger(0);
    //session timeOut
    
    static void initSessionsMap(int expectedMaxActiveSessions){ 
        if(sessions==null){
            synchronized(Session.class){
                if(sessions==null){
                    if(expectedMaxActiveSessions>Session.expectedMaxActiveSessions)
                        Session.expectedMaxActiveSessions=expectedMaxActiveSessions;
                    userIdSessions=new ConcurrentHashMap((int)(Session.expectedMaxActiveSessions*1.25));
                    sessions=new ConcurrentHashMap((int)(Session.expectedMaxActiveSessions*1.25));
                }
            }
        }
    }        
    
    private Session(User user,CommandDataSocket socket){
        this.user=user;
        if(user==null)
            throw new NullPointerException();
        this.sessionListener=sessionListenerFactory==null?defaultSessionListener:sessionListenerFactory.getSessionListener();
        this.socket=socket;
        socket.setCommandDataReadListener(readListener);
        lastReadTime=System.currentTimeMillis();
        //dont detect timeout for bots because they are local
        if(!user.isBot){
            idleChecker=Executor.newWaitRunnable(new IdleChecker());
            Executor.executeWait(idleChecker, idleCheckerInterval);
        }
        else
            idleChecker=null;
    }

    /**
     * Returns the session ID - a unique identifier for each session
     * @return String - the session ID
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Returns the name of the game to which this session is linked to
     * @return String - the name of the game to which this session is linked to
     */
    public String getGameName(){
        return gameObj==null?null:gameObj.name;
    }

    boolean isGameDataNew() {
        return gameDataIsNew;
    }
    
    /**
     * Marks the session user's game data as modified so that the changes will get saved to the game database. The game user data is the additional game data saved for each user such as chipsInHand
     */
    public final void gameDataModified(){
        this.gameUserData.modified();
    }
    
    GameObj setGame(GameObj gameObj){
        if(this.gameObj==gameObj)
            return this.gameObj;
        GameObj oldValue=this.gameObj;
        if(gameObj==null){
            this.gameUserData=null;
            this.gameObj=null;
            return oldValue;
        }
        //fetch and set user game data
        synchronized(user.userId){
            List<Session> sessions=userIdSessions.get(user.userId);
            if(sessions!=null){
                Session session;
                Iterator<Session> iterator=sessions.iterator();
                while(iterator.hasNext()){
                    session=iterator.next();
                    if(session.gameObj==gameObj && session.gameUserData!=null){
                        this.gameUserData=session.gameUserData;
                        this.gameDataIsNew=false;
                        break;
                    }
                }
            }
            if(this.gameUserData==null){
                this.gameUserData=gameObj.getGameUserData(user.userId);
                this.gameDataIsNew=this.gameUserData.isNew;
            }
        }
        this.gameObj=gameObj;
        return oldValue;
    }

    /**
     * Returns the game user data of the session user. The game user data is the additional game data saved for each user such as chipsInHand
     * @return GameUserData - the game user data of the session's user
     */
    public CompressibleData getGameUserData() {
        return gameUserData==null?null:gameUserData.data;
    }
    
    static Session newSession(User user,CommandDataSocket socket,GameObj gameObj) throws TooManySessionsException{
        if(sessions==null)
            initSessionsMap(-1);
        if(userSessionsCount(user)>=maxSessionsPerUser)
            throw new TooManySessionsException();
        Session session=new  Session(user, socket);
        session.sessionId=RandomStringGenerator.randomAlphanumeric(16);
        Session oldSession=sessions.put(session.sessionId, session);
        while(oldSession!=null){
            sessions.put(oldSession.sessionId, oldSession);
            session.sessionId=RandomStringGenerator.randomAlphanumeric(16);
            oldSession=sessions.put(session.sessionId, session);            
        }
        Session.sessionCount.incrementAndGet();
        if(!session.user.isBot)
            Session.nonBotSessionCount.incrementAndGet();
        session.setGame(gameObj);
        addSessionToUserIdMap(session);
        return session;
    }
    
    static int userSessionsCount(User user){
        if(userIdSessions==null)
            return 0;
        List<Session> sessions=userIdSessions.get(user.userId);
        return sessions==null?0:sessions.size();
    }
    
    static void addSessionToUserIdMap(Session session){
        synchronized(session.user.userId){
            List<Session> sessions=userIdSessions.get(session.user.userId);
            if(sessions==null){
                sessions=new LinkedList();
                userIdSessions.put(session.user.userId, sessions);
            }
            sessions.add(session);
        }
    }
    
    static void removeSessionFromUserIdMap(Session session){
        synchronized(session.user.userId){
            List<Session> sessions=userIdSessions.get(session.user.userId);
            if(sessions!=null){
                Iterator<Session> iterator=sessions.iterator();
                while(iterator.hasNext()){
                    if(iterator.next().sessionId==session.sessionId){
                        iterator.remove();
                        break;
                    }
                }
                if(sessions.isEmpty())
                    userIdSessions.remove(session.user.userId);
            }
        }        
    }
    
    static Session getSessionById(String sessionId){
        return sessions!=null?sessions.get(sessionId):null;
    }
    
    class IdleChecker implements Runnable{
        @Override
        public void run() {
            if(sessionClosed)
                return;
            if(System.currentTimeMillis()-lastReadTime>idleSessionTimeOutMillis){                
                Session.this.onSessionTimedOut();
                Session.this.sendMesg(Commands.sessionTimedOut, (String)null);
                Session.this.close();
            }
            else
                Executor.executeWait(idleChecker, idleCheckerInterval);
        }        
    }

    private CommandReadListener readListener=new CommandReadListener();
        
    private class CommandReadListener implements CommandDataReadListener{
            
        @Override
        public final void commandDataRead(CommandDataSocket socket,String command, String data) {
            try{
                processCommand(command, data);
            }catch(Exception ex){
                Utils.log(ex);
            }
            lastReadTime=System.currentTimeMillis();
        }

        @Override
        public final void commandDataRead(CommandDataSocket socket,String command, byte[] data) {
            try{
                processCommand(command, data);
            }catch(Exception ex){
                Utils.log(ex);
            }
            lastReadTime=System.currentTimeMillis();
        }
    }
    
    void processCommand(String command,String data){
        JsonObject dataAsJson=data==null?EMPTY_JSON_OBJECT:JsonUtil.readObject(data);
        switch(Command.toEnum(command)){
            case logout:
                this.onLoggedOff();
                sendMesg(Commands.loggedOut,(String)null); 
                this.close();
                break;
            case poll:
                break;
            case selectGame:
                {
                    GameObj gameObj=Games.getGame(JsonUtil.getAsString(dataAsJson, "gameName"));
                    if(gameObj==null){
                        sendError(ErrorCodes.invalidGameName, ErrorDescriptions.invalidGameName);
                        break;
                    }  
                    //if room is already selected send room data and break
                    Room room=this.room;
                    if(room!=null){
                        sendUserData();
                        sendMesg(Commands.roomData, room.getRoomAsJson(this).toString());
                        sendError(ErrorCodes.isStillInRoom, ErrorDescriptions.isStillInRoom);
                        break;
                    }
                    GameObj oldGameObj=this.setGame(gameObj);
                    if(oldGameObj!=gameObj){
                        if(oldGameObj!=null)
                            oldGameObj.game.onExitGame(this);
                        gameObj.game.onEnterGame(this, this.gameDataIsNew);
                    }
                    JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
                    jsonBuilder.add("gameName", gameObj.name);
                    jsonBuilder.add("gameUserData", this.getGameUserDataForClientAsJson());
                    sendMesg(Commands.gameSelected, jsonBuilder.build().toString());
                }
                break;
            case deselectGame:
                {
                    //if room is already selected send room data and error and break
                    Room room=this.room;
                    if(room!=null){
                        sendUserData();
                        sendMesg(Commands.roomData, room.getRoomAsJson(this).toString());
                        sendError(ErrorCodes.isStillInRoom, ErrorDescriptions.isStillInRoom);
                        break;
                    }                    
                    GameObj deselectedGame=this.setGame(null);
                    if(deselectedGame!=null)
                        deselectedGame.game.onExitGame(this);
                    JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
                    jsonBuilder.add("gameUserData", this.getGameUserDataForClientAsJson());
                    sendMesg(Commands.gameSelected, jsonBuilder.build().toString());
                }
                break;
            case getLocations:
                {
                    if(gameObj==null){
                        sendError(ErrorCodes.gameNotSelected, ErrorDescriptions.gameNotSelected);
                        break;
                    }                    
                    String locationsAsJsonString=gameObj.game.getLocationsAsString();
                    sendMesg(Commands.locations, locationsAsJsonString);
                }
                break;
            case getRooms:
                try{
                    if(gameObj==null){
                        sendError(ErrorCodes.gameNotSelected, ErrorDescriptions.gameNotSelected);
                        break;
                    }                    
                    RoomType roomType=RoomType.normal;
                    if("fast".equalsIgnoreCase(JsonUtil.getAsString(dataAsJson, "roomType")))
                        roomType=RoomType.fast;
                    String locationName=JsonUtil.getAsString(dataAsJson, "location");
                    sendMesg(Commands.rooms, gameObj.game.getRoomsAsJson(locationName, roomType).toString());                                        
                }catch(InvalidLocationNameException ex){
                    sendError(ErrorCodes.invalidLocation, ErrorDescriptions.invalidLocation);
                }
                break;
            case joinRoom:
                {
                    if(gameObj==null){
                        sendError(ErrorCodes.gameNotSelected, ErrorDescriptions.gameNotSelected);
                        break;
                    }                    
                    Room room=Room.getRoom(JsonUtil.getAsLong(dataAsJson,"roomId"));
                    if(room==null){
                        sendError(ErrorCodes.roomIdDoesNotExist, ErrorDescriptions.roomIdDoesNotExist);
                        break;
                    }
                    room.join(this);
                }
                break;
            case getRoomData:
                {
                    Room room=this.room;
                    if(room==null){
                        sendError(ErrorCodes.noRoomJoined, ErrorDescriptions.noRoomJoined);
                        break;
                    }
                    sendMesg(Commands.roomData, room.getRoomAsJson(this).toString());
                }
                break;
            case takeSeat:
                {
                    Room room=Room.getRoom(JsonUtil.getAsLong(dataAsJson,"roomId"));
                    if(room==null){
                        sendError(ErrorCodes.roomIdDoesNotExist, ErrorDescriptions.roomIdDoesNotExist);
                        break;
                    }
                    room.takeSeat(this, JsonUtil.getAsInt(dataAsJson,"seatNo"),JsonUtil.getAsJsonObject(dataAsJson,"data"));
                }
                break;
            case leaveSeat:
                {
                    Room room=Session.this.room;
                    if(room==null){
                        sendError(ErrorCodes.noRoomJoined, ErrorDescriptions.noRoomJoined);
                        break;
                    }
                    room.leaveSeat(this);
                }
                break;
            case leaveRoom:
                {                    
                    Room room=Session.this.room;
                    if(room==null){
                        JsonObjectBuilder dataToSend=JsonUtil.createObjectBuilder();
                        dataToSend.add("gameUserData", Session.this.getGameUserDataForClientAsJson());
                        Session.this.sendMesg(Commands.roomLeft, dataToSend.build().toString());
                        break;
                    }
                    room.leave(this, false);
                }
                break;
            case playAction:
                {
                    Room room=Session.this.room;
                    if(room==null){
                        sendError(ErrorCodes.noRoomJoined, ErrorDescriptions.noRoomJoined);
                        break;
                    }
                    room.forceAction(this, dataAsJson);
                }
                break;
            case changeAvtar:
                {
                    int avtarId=JsonUtil.getAsInt(dataAsJson, "avtarId", Integer.MIN_VALUE);
                    if(avtarId==Integer.MIN_VALUE)
                        break;
                    Session.this.user.avtarId=avtarId;
                    JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
                    jsonBuilder.add("userId", Session.this.user.userId);
                    jsonBuilder.add("avtarId", avtarId);
                    String dataToSend=jsonBuilder.build().toString();
                    Session.this.sendMesg(Commands.avtarChanged, dataToSend);
                    Room room=Session.this.room;
                    if(room==null)
                        break;
                    room.sendMesgToAll(Commands.avtarChanged, dataToSend, Session.this);
                }
                break;
            default:
                if(command.startsWith("__")){
                    sessionListener.onMesg(this, command.substring(2), dataAsJson==null?EMPTY_JSON_OBJECT:dataAsJson);
                }
                if(command.startsWith("--"))
                    sessionListener.onMesg(this, command.substring(2), data);
        }
    }
    
    void processCommand(String command,byte[] data){
        if(command.startsWith("--"))
            sessionListener.onMesg(this, command.substring(2), data);
    }
    
    InetAddress remoteAddress;
    int remotePort;
    final void setSocket(CommandDataSocket socket){
        if(sessionClosed)
            return;
        socketLock.lock();
        try{
            if(this.socket!=null && this.socket!=socket)
                this.socket.close();
            this.lastReadTime=System.currentTimeMillis();
            this.socket=socket;
            remoteAddress=socket.getRemoteAddress();
            remotePort=socket.getRemotePort();
            socket.setCommandDataReadListener(readListener);
        }finally{socketLock.unlock();}
    }
   
    /**
     * Evicts the session from the room that it has joined
     */
    public final void leaveRoom(){
        Room room=this.room;
        if(room!=null){
            room.leave(this, false);
        }
    }
    
    private void closeSocket(){
        socketLock.lock();
        try{
            if(socket!=null){                
                socket.close();
                socket=null;
            }
        }finally{socketLock.unlock();}
    }
    
    /**
     * Closes the session and disconnects the client
     */
    public final void close(){
        lock.lock();
        try{
            if(sessionClosed)
                return;
            Room room=this.room;
            this.room=null;
            if(room!=null){
                room.leave(this,true);
            }
            if(sessions!=null){
                removeSessionFromUserIdMap(this);
                sessions.remove(this.sessionId);
                Session.sessionCount.decrementAndGet();
                if(!this.user.isBot)
                    Session.nonBotSessionCount.decrementAndGet();

            }
            closeSocket();
            sessionClosed=true;
        }finally{lock.unlock();}
    }
    
    private final Lock roomLock=new ReentrantLock();
    final void setRoom(Room room){
        roomLock.lock();
        try{
            this.room=room;
        }finally{roomLock.unlock();}
    }
    
    final void removeRoom(Room room){
        roomLock.lock();
        try{
            if(this.room==room){
                this.room=null;
                this.seatNo=0;
            }
        }finally{roomLock.unlock();}        
    }
    
    /**
     * Makes the session join the specified room
     * @param room - the room to join
     */
    public final void joinRoom(Room room){
        room.join(this);
    }
    
    /**
     * Makes the session take the specified seat 
     * @param seatNo - the seat number to sit on
     * @param takeSeatData - the additional data required to take seat as JsonUtil. for example in poker this could the chips to put on table
     */
    public final void takeSeat(int seatNo,JsonObject takeSeatData){
        Room room=this.room;
        if(room!=null)
            room.takeSeat(this, seatNo, takeSeatData);
    }
    
    /**
     * Evicts the session from its seat.
     */
    public final void leaveSeat(){
        Room room=this.room;
        if(room!=null)
            room.leaveSeat(this);
    }
    
    final void closeOnShutDown(){
        if(sessionClosed)
            return;
        this.onShutDown();
        lock.forceLock(400);
        try{
            if(sessionClosed)
                return;
            sendMesg(Commands.shutDown, (String)null);
            sessionClosed=true;
            closeSocket();
            this.room=null;
        }finally{lock.unlock();}
    }
    
    /**
     * Closes the session and disconnects the client after the specified delay
     * @param delayInMillisecs - the delay in milliseconds
     */
    public final void delayedClose(int delayInMillisecs){
        Executor.executeWait(new DelayedClose(), delayInMillisecs);
    }
    
    private static final JsonObject EMPTY_JSON_OBJECT=JsonObject.EMPTY_JSON_OBJECT;

    /**
     * Sends a message to the session's client
     * @param command - the message command
     * @param data - the message data as JsonUtil
     */
    public final void sendMessage(String command,JsonObject data){
        if(sessionClosed)
            return;
        if(data==null)
            data=EMPTY_JSON_OBJECT;
        sendMesg("__" +command, data.toString());        
    }
    
    /**
     * Sends a message to the session's client
     * @param command - the message command
     * @param data - the message data as a String
     */
    public final void sendMessage(String command, String data){
        if(sessionClosed)
            return;
        sendMesg("--" +command, data);
    }
    
    /**
     * Sends a message to the session's client
     * @param command - the message command
     * @param data - the message data as a byte array
     */
    public final void sendMessage(String command, byte[] data){
        if(sessionClosed)
            return;
        sendMesg("--" + command, data);
    }
    
    
    /**
     * Sends the session user's data to the session's client 
     */
    public final void sendUserData(){
        this.sendMesg(Commands.userData, this.getGameUserDataForClientAsJson().toString());
    }
    
    /**
     * Sends a message to the session's client after the specified delay
     * @param command - the message command
     * @param data - the message data as JsonUtil
     * @param delayInMillisecs - the delay in milliseconds
     */
    public final void sendDelayedMessage(String command,JsonObject data,int delayInMillisecs){
        if(sessionClosed)
            return;
        if(data==null)
            data=EMPTY_JSON_OBJECT;
        delayedSendMesg("__" +command, data.toString(),delayInMillisecs);        
    }
    
    /**
     * Sends a message to the session's client after the specified delay
     * @param command - the message command
     * @param data - the message data as a String
     * @param delayInMillisecs - the delay in milliseconds
     */
    public final void sendDelayedMessage(String command,String data,int delayInMillisecs){
        if(sessionClosed)
            return;
        delayedSendMesg("--" +command, data,delayInMillisecs);        
    }
    
    /**
     * Sends a message to the session's client after the specified delay
     * @param command - the message command
     * @param data - the message data as a byte array
     * @param delayInMillisecs - the delay in milliseconds
     */
    public final void sendDelayedMessage(String command,byte[] data,int delayInMillisecs){
        if(sessionClosed)
            return;
        delayedSendMesg("--" +command, data,delayInMillisecs);        
    }
    
    final void sendMesg(String command,String data){
        socketLock.lock();
        try{
            if(socket!=null)
                socket.write(command, data);
        }finally{socketLock.unlock();}
    }
    
    final void sendMesg(String command,byte[] data){
        socketLock.lock();
        try{
            if(socket!=null)
                socket.write(command, data);
        }finally{socketLock.unlock();}
    }
    
    final static void sendError(CommandDataSocket socket,int errorCode, String desc){
        JsonObjectBuilder dataAsJson=JsonUtil.createObjectBuilder();
        dataAsJson.add("code", errorCode);
        dataAsJson.add("desc", desc);
        socket.write(Commands.error, dataAsJson.build().toString());
        
    }
    
    final void sendError(int errorCode, String desc){
        socketLock.lock();
        try{
            if(socket!=null)
                sendError(socket, errorCode, desc);
        }finally{socketLock.unlock();}     
    }
    
    final void delayedSendMesg(String command,String data,int delayInMillisecs){
        Executor.executeWait(new DelayedSendMesg(command, data), delayInMillisecs);
    }
    
    final void delayedSendMesg(String command,byte[] data,int delayInMillisecs){
        Executor.executeWait(new DelayedSendMesg(command, data), delayInMillisecs);
    }
    
    //delayed sendMesg
    private class DelayedSendMesg implements Runnable{
        String command,data;
        byte[] byteData;
        boolean isByteData=false;
        public DelayedSendMesg(String command, String data) {
            this.command = command;
            this.data = data;
        }

        public DelayedSendMesg(String command, byte[] data) {
            this.command = command;
            this.byteData = data;
            isByteData=true;
        }

        @Override
        public void run() {
            if(!isByteData)
                sendMesg(command, data);
            else
                sendMesg(command, byteData);
        }        
    }
    
    class DelayedClose implements Runnable{        
        @Override
        public void run() {
            close();
        }        
    }
    
    JsonObject getGameUserDataForClientAsJson(){
        return gameUserData==null?null:gameUserData.data==null?null:gameUserData.data.toJson();
    }

    private static AtomicBoolean shutDownStarted=new AtomicBoolean(false);
    static void doShutDown(){
        if(!shutDownStarted.compareAndSet(false, true))
            return;
        //send shutdown mesg to all active users and close all sessions        
        for(Session session:Session.allSessions){
            if(!session.sessionClosed){
                session.closeOnShutDown();
                if(!session.user.isBot)
                    session.gameDataModified();
            }            
        }        
        GameDB.destroyModifiedDataProcessor();
    }
    
    void onLoggedIn(boolean firstTime){
        try{
            sessionListener.onLoggedIn(this, firstTime);
        }catch(Exception ex){Utils.log(ex);}
    }
    
    void onGameSelected(boolean firstTime){
        try{
            if(gameObj!=null){
                gameObj.game.onEnterGame(this, firstTime);
                gameDataModified();
            }
        }catch(Exception ex){Utils.log(ex);}
    }
    
    void onLoggedOff(){
        try{
            sessionListener.onLoggedOff(this);
        }catch(Exception ex){Utils.log(ex);}        
    }
    
    void onGameDeselected(){
        try{
            if(gameObj!=null)
                gameObj.game.onExitGame(this);
        }catch(Exception ex){Utils.log(ex);}        
    }
    
    void onSessionTimedOut(){
        try{
            sessionListener.onSessionTimedOut(this);
        }catch(Exception ex){Utils.log(ex);}
        
    }
    
    void onShutDown(){
        try{
            sessionListener.onShutDown(this);
        }catch(Exception ex){Utils.log(ex);}
        
    }
    
    /**
     * Returns the room joined by the session or null if no room has been joined
     * @return Room - the room joined by the session
     */
    public final Room getRoom(){
        return room;
    }
    
    /**
     * REturns true if the session has joined a room
     * @return boolean - true if the session has joined a room
     */
    public boolean hasJoinedRoom(){
        return room!=null;
    }
    
    /**
     * Returns true if the session is a spectator - i.e. has joined a room and is not seated
     * @return boolean - true if session is a spectator
     */
    public final boolean isSpectator(){
        Room room=this.room;
        if(room==null)
            return false;
        return room.isSpectator(this);
    }
    
    /**
     * Returns true if the session is seated
     * @return boolean - true if the session is seated
     */
    public boolean isSeated(){
        Room room=this.room;
        if(room==null)
            return false;
        return room.isSeated(this);        
    }
    
    /**
     * returns true if session is taking part in a game being played in the room
     * @return boolean - true if session is taking part in a game being played in the room
     */
    public boolean isIngame(){
        Room room=this.room;
        if(room==null)
            return false;
        return room.isSeated(this);                
    }
    
    /**
     * Returns the player instance associated with this session
     * @return Player - the player instance associated with this session
     */
    public Player getPlayer(){
        Room room=this.room;
        if(room==null)
            return null;
        return room.getPlayerFromSession(this);
    }
    
    /**
     * Returns the user instance associated with this session
     * @return User - the user instance associated with this session 
     */
    public final User getUser(){
        return this.user;
    }
    
    private static class SessionsIterator implements Iterator<Session>{
        private Iterator<Map.Entry<String,Session>> sessions;
        public SessionsIterator() {
            if(Session.sessions!=null){
                sessions=Session.sessions.entrySet().iterator();
            }
            else
                sessions=Collections.emptyIterator();
        }
        
        @Override
        public boolean hasNext() {
            return sessions.hasNext();
        }

        @Override
        public Session next() {
            return sessions.next().getValue();
        }
        
    }
    
    private static class SessionsIterable implements Iterable<Session>{
        @Override
        public Iterator<Session> iterator() {
            return new SessionsIterator();
        }        
    }
    
    static Iterable<Session> allSessions=new SessionsIterable();
}
