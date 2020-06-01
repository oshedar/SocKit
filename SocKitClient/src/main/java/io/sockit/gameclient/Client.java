package io.sockit.gameclient;

import io.sockit.clienttools.CommandDataReadListener;
import io.sockit.clienttools.ClientCommandDataSocket;
import io.sockit.clienttools.SocketConnectionListener;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import io.sockit.clienttools.CommandDataSocket;
import io.sockit.clienttools.Timer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.json.JsonObjectBuilder;

/**
 * <div class="hidejava">
 * This class represents a Client interacting with the server. It has methods to login a User to the Server, enter a game, fetch list of rooms, join a room, etc. See startup code example below 
 * <br>
 * <pre>{@code
 *     // instantiate a client with the server url
 *      Client client = new Client("ws://localhost"); 
 *     // register a ClientEventListener (event/callback handler) with the client
 *      client.setClientEventListener(new TicTacToeClientListener());
 *     // register user and login to the server with emailId, password, user name and game name as parameters
 *      client.registerWithEmailId("a@a.com", "123", "Rohan", "TicTacToe");
 * } </pre>
 * </div>
 * <div class="javascript" style="display:none;">
 * This object/function prototype represents a Client interacting with the server. It has methods to login a User to the Server, enter a game, fetch list of rooms, join a room, etc. See startup code example below 
 * <br>
 * <pre>{@code
 *     // instantiate a client with the server url
 *      var client = new Client("ws://"+document.location.hostname);
 *     // register a ClientEventListener (event/callback handler) with the client
 *      client.setClientEventListener(new TicTacToeClientListener());
 *     // register user and login to the server with emailId, password, user name and game name as parameters
 *      client.registerWithEmailId("a@a.com", "123", "Rohan", "TicTacToe");
 * } </pre>
 * </div>
 */
public class Client {
    final String wsUrl;
    String userId;
    String sessionId;
    String name;
    int avtarId;
    String emailId;
    String password;
    String otherId;
    LoginType loginType;
    String profilePic;
    String gameName;
    JsonObject gameUserData;
    CommandDataSocket socket;
    private boolean loggedIn;
    private ClientEventListener clientEventListener;
    private long lastSentTime;
    private Poller poller;
    
    private Room joinedRoom;
    
    private String lastPlayAction=null;
    private JsonObject lastPLayActionData=null;

    private static int pollingInterval=6000;

    static final Charset Utf8Charset=Charset.forName("UTF-8");
    
    final Lock instanceLock=new ReentrantLock();
        
    private ErrorLogger errorLogger;
    private void logError(Exception ex){
        if(errorLogger==null)
            ex.printStackTrace();
        else
            errorLogger.logError(ex);
    }
    
    
    /**
     * Sets the Error logger that will be used to log errors by this Client
     * @param errorLogger - the error logger used for logging errors 
     */
    public void setErrorLogger(ErrorLogger errorLogger){
        this.errorLogger=errorLogger;
    }
    
    private final SocketConnectionListener socketConnectionListener;
    private final CommandDataReadListener commandDataReadListener;
    
    /**
     * Creates a new Client instance
     * @param url - the URL of the server that the client will connect to
     */
    public Client(String url) {
        this.wsUrl=url;
        this.socketConnectionListener=new ClientSocketConnectionListener();
        this.commandDataReadListener=new ClientCommandDataReadListener();
    }
    
    /**
     * Attempts to Register a new user with emailId on the Game Server
     * @param emailId - the user emailId
     * @param password - the user password
     * @param name - the user name
     * @param gameName - the game that will be linked with the client session on successful user registration - can be null
     */
    public void registerWithEmailId(String emailId,String password,String name,String gameName){
        instanceLock.lock();
        try{
            if(loggedIn)
                logOut();            
            reset();
            if(emailId!=null)
                emailId=emailId.trim();
            if(emailId==null || emailId.length()<1)
                throw new IllegalArgumentException("EmailId is null or 0 length String");
            if(password==null || password.length()<1)
                throw new IllegalArgumentException("Password is null or 0 length String");
            if(name!=null)
                name=name.trim();
            if(name==null || name.length()<1)
                throw new IllegalArgumentException("Name is null or 0 length String");
            this.emailId=emailId;
            this.password=password;
            this.name=name;
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            json.add("type","email");
            json.add("emailId", emailId);
            json.add("password", password);
            json.add("name", name);
            json.add("gameName", gameName);
            sendMesg(Commands.register,json.build().toString());
        }finally{instanceLock.unlock();}
    }
    
    /**
     *
     * Attempts to Register a new user with otherId on the Game Server
     * @param otherId - the user otherId (which is not an email), such as otherId or mobileNo 
     * @param password - the user password
     * @param name - the user name
     * @param gameName - the game that will be linked with the client session on successful user registration - can be null
     */
    public void registerWithOtherId(String otherId,String password,String name,String gameName){
        instanceLock.lock();
        try{
            if(loggedIn)
                logOut();            
            reset();
            if(otherId!=null)
                otherId=otherId.trim();
            if(otherId==null || otherId.length()<1)
                throw new IllegalArgumentException("OtherId is null or 0 length String");
            if(password==null || password.length()<1)
                throw new IllegalArgumentException("Password is null or 0 length String");
            if(name!=null)
                name=name.trim();
            if(name==null || name.length()<1)
                throw new IllegalArgumentException("Name is null or 0 length String");
            this.otherId=otherId;
            this.password=password;
            this.name=name;
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            json.add("type","other");
            json.add("otherId", otherId);
            json.add("password", password);
            json.add("name", name);
            json.add("gameName", gameName);
            sendMesg(Commands.register,json.build().toString());
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Attempts to Login a user onto server with emailId and starts a new session
     * @param emailId - the user emailId
     * @param password - the user password
     * @param gameName - the game which should be linked with the client session on successful login - can be null
     */
    public void logInWithEmailId(String emailId,String password,String gameName){
        instanceLock.lock();
        try{
            if(loggedIn)
                logOut();            
            reset();
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            json.add("type", "email");
            json.add("emailId", emailId);
            json.add("password", password);
            json.add("gameName", gameName);
            this.emailId=emailId;
            this.password=password;
            this.loginType=loginType.email;
            sendMesg(Commands.login, json.build().toString());
        }finally{instanceLock.unlock();}
    }

    /**
     * Attempts to Login a user onto server with google's oAuth JWT (json web token) and starts a client session. Note it is the job of the game developer to add google oAuth capability to the game client.
     * @param idToken - The jsonWebToken sent by google on successful authentication by google
     * @param gameName - the game which should be linked with the client session on successful login - can be null
     */
    public void logInWithGoogle(String idToken,String gameName){
        instanceLock.lock();
        try{
            if(loggedIn)
                logOut();            
            reset();
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            json.add("type", "google");
            json.add("idToken", idToken);
            json.add("gameName", gameName);
            this.loginType=LoginType.google;
            sendMesg(Commands.login, json.build().toString());
        }finally{instanceLock.unlock();}
    }

    /**
     * Attempts to Login a user onto server with otherId and starts a client session
     * @param otherId - the user otherId
     * @param password - the user password
     * @param gameName - the game which should be linked with the client session on successful login - can be null
     */
    public void logInWithOtherId(String otherId,String password,String gameName){
        instanceLock.lock();
        try{
            if(loggedIn)
                logOut();            
            reset();
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            json.add("type", "other");
            json.add("otherId", otherId);
            json.add("password", password);
            json.add("gameName", gameName);
            this.otherId=otherId;
            this.password=password;
            this.loginType=loginType.other;
            sendMesg(Commands.login, json.build().toString());
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Attempts to Link a Game with the client session
     * @param gameName - the name of the game to which the session should be linked
     */
    public void enterGame(String gameName){
        instanceLock.lock();
        try{
            if(!loggedIn)
                return;
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            json.add("gameName", gameName);
            sendMesg(Commands.selectGame, json.build().toString());
        }finally{instanceLock.unlock();}
        
    }
    
    /**
     * Attempts to Delink the currently linked game from the client session
     */
    public void exitGame(){
        instanceLock.lock();
        try{
            if(!loggedIn)
                return;
            sendMesg(Commands.deselectGame, (String)null);
        }finally{instanceLock.unlock();}
        
    }
    
    //resets client data before registerWithEmailId or login
    private void reset(){        
        if(socket!=null){
            socket.close();
            socket=null;
        }
        userId=null;
        sessionId=null;
        name=null;
        avtarId=0;
        emailId=null;
        password=null;
        otherId=null;
        loginType=null;
        loggedIn=false;
        poller=null;
        joinedRoom=null;
        gameName=null;
        gameUserData=null;
    }
    
    /**
     * Checks if this Client is connected to the game server or not
     * @return - true if this Client is connected to the game server
     */
    public boolean isConnected(){
        return socket!=null && socket.isClosed()==false;
    }
    
    /**
     * Disconnects this Client from the server
     */
    public void disconnect(){
        if(this.socket!=null){
            if(!this.socket.isClosed())
                this.socket.close();
            this.socket=null;
        }
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
    
    private String pollData=null;

    String getPollData(){
        String pollData=this.pollData;
        if(pollData!=null)
            return pollData;
        JsonObjectBuilder json=JsonUtil.createObjectBuilder();
        json.add("sessionId", this.sessionId);
        pollData=json.build().toString();
        this.pollData=pollData;
        return pollData;
    }
        
    void sendMesg(String command,String data) {
        instanceLock.lock();
        try{
            try{
                if(this.socket==null || this.socket.isClosed()){
                    this.socket=ClientCommandDataSocket.newInstance(this.wsUrl,this.socketConnectionListener,this.commandDataReadListener);
                    //if command is not registerWithEmailId or login or poll then send poll mesg first
                    if(!(Commands.poll.equals(command) || Commands.login.equals(command) || Commands.register.equals(command) || Commands.rejoinSession.equals(command)))
                        this.socket.write(Commands.poll, this.getPollData());

                }
                this.socket.write(command, data);
                this.lastSentTime=System.currentTimeMillis();
            }
            catch(Exception ex){
                logError(ex);           
            }
        }finally{instanceLock.unlock();}
    }
    
    void sendMesg(String command,byte[] data) {
        instanceLock.lock();
        try{
            try{
                if(this.socket==null || this.socket.isClosed()){
                    this.socket=ClientCommandDataSocket.newInstance(this.wsUrl,this.socketConnectionListener,this.commandDataReadListener);
                    //if command is not registerWithEmailId or login or poll then send poll mesg first
                    if(!(Commands.poll.equals(command) || Commands.login.equals(command) || Commands.register.equals(command) || Commands.rejoinSession.equals(command)))
                        this.socket.write(Commands.poll, this.getPollData());

                }
                this.socket.write(command, data);
                this.lastSentTime=System.currentTimeMillis();
            }
            catch(Exception ex){
                logError(ex);           
            }
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Attempts to Change the avtarId of user loggedin to the session
     * @param avtarId - the new avtar ID
     */
    public void changeAvtarId(int avtarId){
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
     * Attempts to Logout the user from the server and end the client session
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
     * Attempts to Make the client session join the specified room. 
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
     * Attempts to take a seat in the room. Until the client is seated he/she remains a spectator and cannot take part in the game play. The seat numbers are numbered 1 to n. 
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
        instanceLock.lock();
        try{
            if(!loggedIn || joinedRoom==null)
                return;
            sendMesg(Commands.leaveRoom, (String)null);
        }finally{instanceLock.unlock();}        
        
    }
    
    private class ClientCommandDataReadListener implements CommandDataReadListener{
        
        @Override
        public void commandDataRead(CommandDataSocket socket,String command, String data) {
            instanceLock.lock();
            try{
                if(clientEventListener!=null)
                    clientEventListener.beforeServerMessageProcessed(Client.this,ServerMessageType.toEnum(command),ServerMessageType.getCustomCommand(command),false,data);

                JsonObject dataAsJson=null;
                if(data!=null){
                    try{
                        dataAsJson=JsonUtil.readObject(data);
                    }catch(Exception ex){}
                }
                switch(Command.toEnum(command)){
                    case loggedIn:
                        Client.this.userId=JsonUtil.getAsString(dataAsJson,"userId");
                        Client.this.sessionId=JsonUtil.getAsString(dataAsJson,"sessionId");
                        Client.this.name=JsonUtil.getAsString(dataAsJson,"name");
                        Client.this.avtarId=JsonUtil.getAsInt(dataAsJson,"avtarId");
                        Client.this.profilePic=JsonUtil.getAsString(dataAsJson, "profilePic");
                        Client.this.gameName=JsonUtil.getAsString(dataAsJson, "gameName");
                        Client.this.gameUserData=JsonUtil.getAsJsonObject(dataAsJson, "gameUserData");
                        Client.this.loggedIn=true;
                        startPoller();
                        if(clientEventListener!=null)
                            clientEventListener.onLoggedIn(Client.this,Client.this.gameName!=null);
                        break;
                    case sessionRejoined:
                        JsonObject userJson=JsonUtil.getAsJsonObject(dataAsJson, "user");
                        JsonObject roomJson=JsonUtil.getAsJsonObject(dataAsJson, "room");
                        Client.this.userId=JsonUtil.getAsString(userJson,"userId");
                        Client.this.sessionId=JsonUtil.getAsString(userJson,"sessionId");
                        Client.this.name=JsonUtil.getAsString(userJson,"name");
                        Client.this.avtarId=JsonUtil.getAsInt(userJson,"avtarId");
                        Client.this.profilePic=JsonUtil.getAsString(userJson, "profilePic");
                        Client.this.gameName=JsonUtil.getAsString(userJson, "gameName");
                        Client.this.gameUserData=JsonUtil.getAsJsonObject(userJson, "gameUserData");
                        Client.this.loggedIn=true;
                        Client.this.joinedRoom=roomJson==null?null:Room.newRoom(roomJson,Client.this);
                        startPoller();
                        if(clientEventListener!=null)
                            clientEventListener.onSessionRejoined(Client.this);
                        break;
                    case error:
                        int errorCode=JsonUtil.getAsInt(dataAsJson,"code");
                        if(clientEventListener!=null){
                            if(errorCode==ErrorCodes.serverShutdownStarted)
                                clientEventListener.onServerShutdown(Client.this);
                            else
                                clientEventListener.onError(Client.this,errorCode, JsonUtil.getAsString(dataAsJson,"desc"));
                        }
                        break;
                    case loggedOut:
                        Client.this.loggedIn=false;
                        Client.this.reset();
                        if(clientEventListener!=null)
                            clientEventListener.onLoggedOut(Client.this);
                        break;
                    case sessionTimedOut:
                        Client.this.loggedIn=false;
                        Client.this.reset();
                        if(clientEventListener!=null)
                            clientEventListener.onSessionTimedOut(Client.this);
                        break;
                    case shutDown:
                        Client.this.loggedIn=false;
                        Client.this.reset();
                        if(clientEventListener!=null)
                            clientEventListener.onServerShutdown(Client.this);
                        break;
                    case gameSelected:
                        {
                            Client.this.gameName=JsonUtil.getAsString(dataAsJson, "gameName");
                            Client.this.gameUserData=JsonUtil.getAsJsonObject(dataAsJson, "gameUserData");
                            if(clientEventListener!=null)
                                clientEventListener.onEnterGame(Client.this, Client.this.gameName);
                        }
                        break;
                    case gameDeselected:
                        {
                            String oldGameName=Client.this.gameName;
                            Client.this.gameName=null;
                            Client.this.gameUserData=null;
                            if(oldGameName!=null && clientEventListener!=null)
                                clientEventListener.onExitGame(Client.this, oldGameName);
                        }
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
                            if(clientEventListener!=null)
                                clientEventListener.onGetLocations(Client.this,gameName,locations);
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
                            if(clientEventListener!=null)
                                clientEventListener.onGetRooms(Client.this, gameName, location, roomType, rooms);                            
                        }
                        break;
                    case roomJoined:
                        Client.this.lastPlayAction=null;
                        if(!loggedIn)
                            return;
                        Client.this.gameUserData=JsonUtil.getAsJsonObject(dataAsJson, "gameUserData");
                        joinedRoom=Room.newRoom(JsonUtil.getAsJsonObject(dataAsJson,"room"),Client.this);
                        if(clientEventListener!=null)
                            clientEventListener.onRoomJoined(Client.this, joinedRoom);                        
                        break;
                    case roomData:
                        if(!loggedIn){
                            Client.this.lastPlayAction=null;
                            return;
                        }
                        {
                            long roomId=JsonUtil.getAsLong(dataAsJson,"roomId");
                            if(joinedRoom==null || joinedRoom.roomId!=roomId)
                                joinedRoom=Room.newRoom(dataAsJson,Client.this);
                            else
                                joinedRoom.refreshRoomData(dataAsJson,Client.this);
                            if(Client.this.isCurTurn() && Client.this.lastPlayAction!=null)
                                Client.this.playAction(Client.this.lastPlayAction, Client.this.lastPLayActionData);
                            else
                                Client.this.lastPlayAction=null;
                            if(clientEventListener!=null)
                                clientEventListener.onRoomRefreshedFromServer(Client.this, joinedRoom);
                        }
                        break;
                    case seatTaken:
                        if(!loggedIn || joinedRoom==null){
                            Client.this.lastPlayAction=null;
                            return;
                        }
                        Player playerSeated=Player.newPlayer(JsonUtil.getAsJsonObject(dataAsJson,"player"),Client.this,Client.this.joinedRoom);
                        joinedRoom.set(playerSeated.seatNo,playerSeated);
                        if(Client.this.userId.equals(playerSeated.userId)){
                            Client.this.lastPlayAction=null;
                            Client.this.gameUserData=JsonUtil.getAsJsonObject(dataAsJson, "gameUserData");
                        }
                        if(clientEventListener!=null)
                            clientEventListener.onSeatTaken(Client.this,playerSeated, joinedRoom,Client.this.userId.equals(playerSeated.userId));
                        break;
                    case userData:
                        Client.this.gameUserData=dataAsJson;
                        break;
                    case seatLeft:
                        if(!loggedIn || joinedRoom==null){
                            Client.this.lastPlayAction=null;
                            return;
                        }
                        {
                            int seatNo=JsonUtil.getAsInt(dataAsJson,"seatNo");
                            String userId=JsonUtil.getAsString(dataAsJson,"userId");                        
                            Player playerLeft=joinedRoom.remove(seatNo);
                            if(Client.this.userId.equals(userId))
                                Client.this.gameUserData=JsonUtil.getAsJsonObject(dataAsJson, "gameUserData");
                            if(Client.this.userId.equals(userId))
                                Client.this.lastPlayAction=null;
                            else if(playerLeft!=null && Client.this.userId.equals(playerLeft.userId))
                                Client.this.lastPlayAction=null;
                            if(playerLeft!=null && clientEventListener!=null)
                                clientEventListener.onSeatLeft(Client.this,playerLeft, joinedRoom,Client.this.userId.equals(playerLeft.userId));
                        }
                        break;
                    case roomLeft:
                        Client.this.lastPlayAction=null;
                        if(!loggedIn || joinedRoom==null)
                            return;
                        joinedRoom=null;
                        Client.this.gameUserData=JsonUtil.getAsJsonObject(dataAsJson, "gameUserData");
                        if(clientEventListener!=null)
                            clientEventListener.onRoomLeft(Client.this);
                        break;
                    case newGame:
                        Client.this.lastPlayAction=null;
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            long roomId=JsonUtil.getAsLong(dataAsJson,"roomId");
                            if(joinedRoom==null || joinedRoom.roomId!=roomId)
                                joinedRoom=Room.newRoom(dataAsJson,Client.this);
                            else
                                joinedRoom.refreshRoomData(dataAsJson,Client.this);                            
                            if(clientEventListener!=null)
                                clientEventListener.onGamePlayStarted(Client.this, joinedRoom);
                        }
                        break;
                    case nextTurn:
                        Client.this.lastPlayAction=null;
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            JsonObject turnPlayerAsJson=JsonUtil.getAsJsonObject(dataAsJson,"turnPlayer");
                            int seatNo=JsonUtil.getAsInt(turnPlayerAsJson,"seatNo");
                            String userId=JsonUtil.getAsString(turnPlayerAsJson,"userId");                        
                            Player turnPlayer=joinedRoom.getPlayerBySeatNo(seatNo);
                            if(turnPlayer==null){
                                turnPlayer=Player.newPlayer(turnPlayerAsJson,Client.this,Client.this.joinedRoom);
                                joinedRoom.set(seatNo, turnPlayer);
                            }
                            else if(!turnPlayer.userId.equals(userId)){
                                turnPlayer=Player.newPlayer(turnPlayerAsJson,Client.this,Client.this.joinedRoom);
                                joinedRoom.set(seatNo, turnPlayer);
                            }
                            else
                                turnPlayer.refreshPlayerData(turnPlayerAsJson,Client.this,Client.this.joinedRoom);
                            joinedRoom.setData(JsonUtil.getAsJsonObject(dataAsJson,"data"));                            
                            joinedRoom.curTurnSeatNo=turnPlayer.seatNo;
                            if(clientEventListener!=null)
                                clientEventListener.onNextTurn(Client.this,turnPlayer,JsonUtil.getAsJsonObject(dataAsJson,"turnData") , joinedRoom,Client.this.userId.equals(turnPlayer.userId));
                        }
                        break;
                    case turnPlayed:
                        Client.this.lastPlayAction=null;
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            String playerAction=JsonUtil.getAsString(dataAsJson, "playerAction");
                            JsonObject actionData=JsonUtil.getAsJsonObject(dataAsJson, "actionData");
                            int turnSeatNo=JsonUtil.getAsInt(dataAsJson,"turnSeatNo");
                            JsonObject roomData=JsonUtil.getAsJsonObject(dataAsJson,"room");
                            long roomId=JsonUtil.getAsLong(roomData,"roomId");
                            if(joinedRoom==null || joinedRoom.roomId!=roomId)
                                joinedRoom=Room.newRoom(roomData,Client.this);
                            else
                                joinedRoom.refreshRoomData(roomData,Client.this);
                            Player turnPlayer=joinedRoom.getPlayerBySeatNo(turnSeatNo);
                            if(clientEventListener!=null)
                                clientEventListener.onTurnPlayed(Client.this, turnPlayer, playerAction, actionData, joinedRoom, turnPlayer==null?false:Client.this.userId.equals(turnPlayer.userId));
                        }
                        break;
                    case invalidAction:
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            String action=JsonUtil.getAsString(dataAsJson, "action");
                            String desc=JsonUtil.getAsString(dataAsJson, "desc");
                            JsonObject errorData=JsonUtil.getAsJsonObject(dataAsJson, "data");
                            if(clientEventListener!=null)
                                clientEventListener.onInvalidAction(Client.this, action, desc, errorData, joinedRoom, !Client.this.isCurTurn());
                        }
                        break;
                    case gameAction:
                        Client.this.lastPlayAction=null;
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            String gameAction=JsonUtil.getAsString(dataAsJson, "action");
                            JsonObject actionData=JsonUtil.getAsJsonObject(dataAsJson, "data");
                            JsonObject roomData=JsonUtil.getAsJsonObject(dataAsJson,"room");
                            long roomId=JsonUtil.getAsLong(roomData,"roomId");
                            if(joinedRoom==null || joinedRoom.roomId!=roomId)
                                joinedRoom=Room.newRoom(roomData,Client.this);
                            else
                                joinedRoom.refreshRoomData(roomData,Client.this);
                            if(clientEventListener!=null)
                                clientEventListener.onGameAction(Client.this, gameAction, actionData, joinedRoom);
                        }
                        break;
                    case outOfTurnPlayed:
                        if(!loggedIn || joinedRoom==null){
                            Client.this.lastPlayAction=null;
                            return;
                        }
                        {
                            String playerAction=JsonUtil.getAsString(dataAsJson, "playerAction");
                            JsonObject actionData=JsonUtil.getAsJsonObject(dataAsJson, "actionData");
                            JsonObject playerAsJson=JsonUtil.getAsJsonObject(dataAsJson, "player");
                            int playerSeatNo=JsonUtil.getAsInt(dataAsJson, "playerSeatNo");
                            JsonObject roomData=JsonUtil.getAsJsonObject(dataAsJson, "room");
                            Player outOfTurnPlayer=null;
                            if(playerAsJson!=null){
                                int seatNo=JsonUtil.getAsInt(playerAsJson,"seatNo");
                                String userId=JsonUtil.getAsString(playerAsJson,"userId");                        
                                outOfTurnPlayer=joinedRoom.getPlayerBySeatNo(seatNo);
                                if(outOfTurnPlayer==null){
                                    outOfTurnPlayer=Player.newPlayer(playerAsJson,Client.this,Client.this.joinedRoom);
                                    joinedRoom.set(seatNo, outOfTurnPlayer);
                                }
                                else if(!outOfTurnPlayer.userId.equals(userId)){
                                    outOfTurnPlayer=Player.newPlayer(playerAsJson,Client.this,Client.this.joinedRoom);
                                    joinedRoom.set(seatNo, outOfTurnPlayer);
                                }
                                else
                                    outOfTurnPlayer.refreshPlayerData(playerAsJson,Client.this,Client.this.joinedRoom);

                            }
                            else{
                                long roomId=JsonUtil.getAsLong(roomData,"roomId");
                                if(joinedRoom==null || joinedRoom.roomId!=roomId)
                                    joinedRoom=Room.newRoom(roomData,Client.this);
                                else
                                    joinedRoom.refreshRoomData(roomData,Client.this);
                                outOfTurnPlayer=joinedRoom.getPlayerBySeatNo(playerSeatNo);
                            }
                            if(outOfTurnPlayer!=null && Client.this.userId.equals(outOfTurnPlayer.userId))
                                Client.this.lastPlayAction=null;
                            if(clientEventListener!=null)
                                clientEventListener.onOutOfTurnPlayed(Client.this, outOfTurnPlayer, playerAction, actionData, joinedRoom, outOfTurnPlayer==null?false:Client.this.userId.equals(outOfTurnPlayer.userId));
                        }
                        break;
                    case endGame:
                        Client.this.lastPlayAction=null;
                        if(!loggedIn || joinedRoom==null)
                            return;
                        {
                            JsonObject endGameData=JsonUtil.getAsJsonObject(dataAsJson, "endGameData");
                            JsonObject roomData=JsonUtil.getAsJsonObject(dataAsJson,"room");
                            long roomId=JsonUtil.getAsLong(roomData,"roomId");
                            if(joinedRoom==null || joinedRoom.roomId!=roomId)
                                joinedRoom=Room.newRoom(roomData,Client.this);
                            else
                                joinedRoom.refreshRoomData(roomData,Client.this);
                            if(clientEventListener!=null)
                                clientEventListener.onGamePlayEnded(Client.this, joinedRoom, endGameData);
                        }
                        break;
                    case avtarChanged:
                        if(!loggedIn)
                            return;
                        {
                            String userId=JsonUtil.getAsString(dataAsJson,"userId");
                            int avtarId=JsonUtil.getAsInt(dataAsJson,"avtarId");
                            if(Client.this.userId.equals(userId)){
                                Client.this.avtarId=avtarId;
                                if(clientEventListener!=null)
                                    clientEventListener.onAvtarChangedOfSelf(Client.this, avtarId);
                                break;
                            }
                            Room room=Client.this.joinedRoom;
                            if(room==null)
                                break;
                            Player player=room.getPlayerByUserId(userId);
                            if(player==null)
                                break;
                            if(clientEventListener!=null)
                                clientEventListener.onAvtarChangedOfOtherPlayer(Client.this, player, avtarId, room);                                
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
                                Client.this.gameUserData=gameUserData;
                            Room roomDestroyed=joinedRoom;
                            joinedRoom=null;
                            if(clientEventListener!=null)
                                clientEventListener.onRoomDestroyed(Client.this, roomDestroyed);
                        }
                        break;
                    case notElligibeToPlay:
                            if(clientEventListener!=null)
                                clientEventListener.onNotEligibleToPlay(Client.this, Client.this.getPlayer(), Client.this.joinedRoom, JsonUtil.getAsString(dataAsJson, "reason"));
                            break;
                    default:
                        if(command.startsWith("__") && command.endsWith("__")){
                            if(clientEventListener!=null){
                                clientEventListener.onMessageReceivedJson(Client.this,command.substring(2, command.length()-2), dataAsJson);
                            }
                        }
                        else if(command.startsWith("--") && command.endsWith("--")){
                            if(clientEventListener!=null){
                                clientEventListener.onMessageReceivedString(Client.this,command.substring(2, command.length()-2), data);
                            }
                        }
                }
                if(clientEventListener!=null)
                    clientEventListener.afterServerMessageProcessed(Client.this,ServerMessageType.toEnum(command),ServerMessageType.getCustomCommand(command),false,data);
            }catch(Exception ex){
                logError(ex);
            }
            finally{instanceLock.unlock();}
        }

        @Override
        public void commandDataReadBytes(CommandDataSocket socket,String command, byte[] data) {
            instanceLock.lock();
            try{
                if(clientEventListener!=null)
                    clientEventListener.beforeServerMessageProcessed(Client.this,ServerMessageType.toEnum(command),ServerMessageType.getCustomCommand(command),true,data);
                if(command.startsWith("--") && command.endsWith("--")){
                    if(clientEventListener!=null)
                        clientEventListener.onMessageReceivedBytes(Client.this,command.substring(2, command.length()-2), data);
                }
                if(clientEventListener!=null)
                    clientEventListener.afterServerMessageProcessed(Client.this,ServerMessageType.toEnum(command),ServerMessageType.getCustomCommand(command),true,data);
            }catch(Exception ex){
                logError(ex);
            }
            finally{instanceLock.unlock();}
        }
        
    }
    
    /**
     * Sets the ClientEventListener to which the client's events/callbacks will be forwarded
     * @param clientEventListener - the ClientEventListener that will handle the client's events/callbacks
     */
    public void setClientEventListener(ClientEventListener clientEventListener) {
        instanceLock.lock();
        try{
            this.clientEventListener=clientEventListener;
        }finally{instanceLock.unlock();}
    }
        
    /**
     * Returns the user ID of the user loggedIn to the client session. The userId uniquely identifies a user registered on the GameServer
     * @return - the user ID of the user loggedIn to the client session
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the Client sessionId
     * @return - Client sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns the name of the User loggedIn to the client session
     * @return - the name of the User loggedIn to the client session
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the avtarId of the User loggedIn to the client session
     * @return - the avtarId of the User loggedIn to the client session
     */
    public int getAvtarId() {
        return avtarId;
    }

    /**
     * Returns the URL of the profile picture of the User loggedIn to the client session
     * @return - the URL of the profile picture of the User loggedIn to the client session
     */
    public String getProfilePic() {
        return profilePic;
    }

    /**
     * Returns the emailId of the User loggedIn to the client session
     * @return - the emailId of the User loggedIn to the client session
     */
    public String getEmailId() {
        return emailId;
    }

    /**
     * Returns the otherId of the User loggedIn to the client session
     * @return the otherId of the User loggedIn to the client session
     */
    public String getOtherId() {
        return otherId;
    }

    /**
     * returns the Name of the game that this client session is linked to
     * @return - the Name of the game that this client session is linked to
     */
    public String getGameName() {
        return gameName;
    }

    /**
     * Returns the Game Specific User Data of the User loggedIn to the client session 
     * @return - the Game Specific User Data of the User loggedIn to the client session 
     */
    public JsonObject getGameUserData() {
        return gameUserData;
    }

    /**
     * Returns whether this Client is loggedIn to the server or not
     * @return - true if this Client is loggedIn to the server
     */
    public boolean isLoggedIn() {
        instanceLock.lock();
        try{
            return loggedIn;
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Returns the LoginType of the Client session - i.e. whether Client loddeIn with emailId or otherId or google
     * @return - the LoginType of the Client session - i.e. whether Client loddeIn with emailId or otherId or google
     */
    public LoginType getLoginType() {
        return loginType;
    }

    /**
     * Returns the room joined by this client or null if no room has been joined
     * @return - the room joined by this client
     */
    public Room getJoinedRoom() {
        return joinedRoom;
    }
    
    /**
     * Checks if Client has joined a room. Returns true if this Client has joined a room
     * @return - true if this Client has joined a room
     */
    public boolean hasJoinedRoom(){
        return joinedRoom!=null;
    }
    
    /**
     * Checks if Client is a spectator. Returns true if this Client has joined a room and is a spectator
     * @return - true if this Client has joined a room and is a spectator
     */
    public boolean isSpectator(){
        Room joinedRoom=this.joinedRoom;
        return joinedRoom!=null && !joinedRoom.isSeated(userId);
    }
    
    /**
     * Checks if this Client is seated. Returns true if this Client is seated in a room.
     * @return - true if this Client is seated in a room.
     */
    public boolean isSeated(){
        Room joinedRoom=this.joinedRoom;
        return joinedRoom!=null && joinedRoom.isSeated(userId);
    }
    
    /**
     * Checks if Game play is in progress. Returns true if this Client has joined a room and Game play is in progress in the room.
     * @return - true if this Client has joined a room and Game play is in progress in the room.
     */
    public boolean isGamePlayInProgress(){
        Room joinedRoom=this.joinedRoom;
        return joinedRoom!=null && joinedRoom.isGamePlayInProgress();        
    }
    
    /**
     * Checks if this Client is an active player. Returns true if this Client is seated in a room and is taking part in the current game play in the room..
     * @return - true if this Client is seated in a room and is taking part in the current game play in the room..
     */
    public boolean isActivePlayer(){
        Player player=getPlayer();
        return player!=null && player.isActive();
    }
    
    /**
     * Returns the player object associated with this Client or null if the Client is not seated
     * @return - the player object associated with this Client or null if the Client is not seated
     */
    public Player getPlayer(){
        Room joinedRoom=this.joinedRoom;
        if(joinedRoom==null)
            return null;
        return joinedRoom.getPlayerByUserId(userId);                    
    }
    
    /**
     * Returns true if the current turn belongs to this Client.
     * @return - true if the current turn belongs to this Client.
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
            if(this.joinedRoom==null)
                return;
            if(!this.joinedRoom.gameInProgress)
                throw new GamePlayNotInProgressException();
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            json.add("action", action);
            json.add("data", actionData);
            this.lastPlayAction=action;
            this.lastPLayActionData=actionData;
            sendMesg(Commands.playAction, json.build().toString());
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Checks if the User loggedIn via this Client owns the joined room or not. Returns true if the User loggedIn via this Client owns the joined room
     * @return - true if the User loggedIn via this Client owns the joined room
     */
    public boolean isOwnerOfJoinedRoom(){
        if(this.joinedRoom==null)
            return false;
        return joinedRoom.isOwner(userId);
    }
    
    /**
     * Attempts to rejoin this Client's session if the session is still alive on the server
     */
    public void rejoinSession(){
        instanceLock.lock();
        try{
            if(this.loggedIn || this.sessionId==null)
                return;            
            String sessionId=this.sessionId;
            reset();
            JsonObjectBuilder json=JsonUtil.createObjectBuilder();
            json.add("sessionId", sessionId);
            this.sendMesg(Commands.rejoinSession, json.build().toString());
        }finally{instanceLock.unlock();}        
    }

    private class Poller implements Runnable{
        @Override
        public void run() {
            instanceLock.lock();
            try{
                if(Client.this.poller!=this || !Client.this.isLoggedIn())
                    return;
                if(Client.this.sessionId==null)
                    return;
                long curTime=System.currentTimeMillis();
                if(curTime-Client.this.lastSentTime>=Client.pollingInterval){
                    Client.this.sendMesg(Commands.poll,Client.this.getPollData());
                }
                Timer.setTimeOut(this, Client.pollingInterval);
            }finally{instanceLock.unlock();}
        }        
    } 

    private void startPoller(){
        this.poller=new Poller();
        Timer.setTimeOut(this.poller, Client.pollingInterval);
    }
    
    private void stopPoller(){
        this.poller=null;
    }
    
    
    private class ClientSocketConnectionListener implements SocketConnectionListener{
        @Override
        public void socketConnecting(CommandDataSocket socket) {
            instanceLock.lock();
            try{
                if(clientEventListener!=null)
                    clientEventListener.onConnecting(Client.this);
            }finally{
                instanceLock.unlock();
            }
        }

        @Override
        public void socketConnected(CommandDataSocket socket) {
            instanceLock.lock();
            try{
                if(clientEventListener!=null)
                    clientEventListener.onConnectionSuccess(Client.this);
            }finally{
                instanceLock.unlock();
            }
        }

        @Override
        public void connectionFailed(CommandDataSocket socket, Exception exception) {
            instanceLock.lock();
            try{
                if(clientEventListener!=null)
                    clientEventListener.onConnectionFailure(Client.this);
            }finally{
                instanceLock.unlock();
            }
        }        

        @Override
        public void socketDisconnected(CommandDataSocket socket) {
            instanceLock.lock();
            try{
                if(clientEventListener!=null)
                    clientEventListener.onConnectionDisconnected(Client.this);
            }finally{
                instanceLock.unlock();
            }
        }

        @Override
        public void socketClosed(CommandDataSocket socket) {
            instanceLock.lock();
            try{
                Client.this.stopPoller();
                if(clientEventListener!=null)
                    clientEventListener.onConnectionClosed(Client.this);
            }finally{
                instanceLock.unlock();
            }
        }
    }
}
