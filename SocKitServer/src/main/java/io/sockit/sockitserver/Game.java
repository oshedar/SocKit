/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import io.sockit.servertools.Utils;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

/**
 * An instance of this class represents a New Game. The class has factory methods to create rooms. Every Game class has to extend Game and override the abstract factory methods and event/callback methods
 * 
 */
public abstract class Game {

    /**
     * the game Name
     */
    public final String gameName;
    private final HashMap<String, Location> locations=new HashMap(16, 1.0f);
    private final Set<String> locationNames=new TreeSet(String.CASE_INSENSITIVE_ORDER);
    private List<Location> locationList=null;
    private String locationsAsJsonString;
    private final Lock instanceLock=new ReentrantLock();
    private final Map<String,Room> privateRooms=new ConcurrentHashMap(32, 1.0f);
    Lock publicRoomsLock=new ReentrantLock();
    final boolean isPlayerDataSameForAllClients;
    /**
     * Creates a new Game. isPlayerDataSameForAllClients should be true if the player data is same for player's client as well as other clients. For example in poker you will not send the player's hole cards to other clients. But in games like chess or tic tac toe the player data will be the same. If the data is same this method should return true. When this method returns true the game engine can do some optimizations and speed up the sending of room state to clients. <b>Note </b> if you are not sure then this value should be false.
     * @param gameName - the game Name
     * @param isPlayerDataSameForAllClients - whether the player data is same for player's client as well as other clients.  if you are not sure then this value should be false.
     */
    public Game(String gameName,boolean isPlayerDataSameForAllClients) {
        this.gameName = gameName;
        this.isPlayerDataSameForAllClients=isPlayerDataSameForAllClients;
    }
    
    /**
     *  This method is called to initialize and set up the Game - here create the locations and public rooms of the game and start bots.
     */
    protected abstract void setUpGame();
        
    /**
     * This method adds a new location to the Game
     * @param locationName - the location name. eg "Mumbai" or "Las Vegas" 
     */
    public final void addNewLocation(String locationName){
        instanceLock.lock();
        try{
            locationName=Utils.tittleCase(locationName);
            if(locations.containsKey(locationName))
                return;
            locations.put(locationName, new Location(locationName, this));
            locationNames.add(locationName);
            locationsAsJsonString=null;
            locationList=null;
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Deletes the specified location from the game
     * @param location - the location to be deleted
     */
    public void deleteLocation(Location location){
        instanceLock.lock();
        try{
            location.destroy();
            locations.remove(location.name);
            locationNames.remove(location.name);
            locationsAsJsonString=null;
            locationList=null;            
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Deletes a location of the specified name from the game
     * @param locationName - the name of the location to be deleted
     */
    public void deleteLocation(String locationName){
        Location location=getLocation(locationName);
        if(location==null)
            return;
        deleteLocation(location);
    }
            
    /**
     * gets Location by Name
     * @param locationName - the name of the location
     * @return Location - the location with the given Name
     */
    public Location getLocation(String locationName){
        instanceLock.lock();
        try{
            return locations.get(Utils.tittleCase(locationName));
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Returms the room with the specified room ID
     * @param roomId - the room Id
     * @return Room - the room with the specified roomId
     */
    public static Room getRoom(Long roomId){
        return Room.getRoom(roomId);
    }
    
    /**
     * This method returns returns all the locations of the Game.
     * @return List&lt;Location&gt; - list of all the locations of the Game
     */
    public List<Location> getLocations(){
        instanceLock.lock();
        try{
            if(this.locationList!=null)
                return this.locationList;
            List<Location> locationList=new ArrayList(locations.size());
            for(String locationName:this.locationNames)
                locationList.add(locations.get(locationName));
            this.locationList=Collections.unmodifiableList(locationList);
            return this.locationList;
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Creates a private room for a user if one does not exist. Returns the new room. If a private room already exists then the method will return the existing room.
     * @param user - the user for whom the private room is created
     * @param totalNoOfSeats - the max number of players who can play in the room
     * @param turnDurationInSecs - the duration of 1 turn in seconds
     * @param additionalRoomConfig - additional configuration data for the room in json format
     * @return Room - the new private room that was created or old private room if one already exists for the user
     */
    public Room createPrivateRoom(User user,int totalNoOfSeats, int turnDurationInSecs,JsonObject additionalRoomConfig){
        Room room;
        RoomType roomType=RoomType.normal;
        //check if privateRooms already contains this user's room - if so then return the old room
        room=privateRooms.get(user.userId);
        String roomName="";
        if(room==null){
            //check if room exists in privateRoomDB if so then deserialize and return the same room
            JsonObject roomAsJson=GameDB.getPrivateRoom(user.userId, gameName);
            if(roomAsJson!=null){
                roomName=JsonUtil.getAsString(roomAsJson,"roomName");
                roomType=RoomType.valueOf(JsonUtil.getAsString(roomAsJson,"roomType"));
                totalNoOfSeats=JsonUtil.getAsInt(roomAsJson,"totalNoOfSeats");
                turnDurationInSecs=JsonUtil.getAsInt(roomAsJson,"turnDurationSecs")*1000;
                additionalRoomConfig=JsonUtil.getAsJsonObject(roomAsJson, "additionalConfig");
            }
            room=this.newRoom(roomName, roomType, totalNoOfSeats, turnDurationInSecs,additionalRoomConfig);
            room.ownerUserId=user.userId;
            Room oldRoom=privateRooms.put(user.userId, room);
            if(oldRoom!=null){
                privateRooms.put(user.userId, oldRoom);
                return oldRoom;
            }
            if(roomAsJson==null)
                GameDB.savePrivateRoom(user.userId, gameName, room.toJsonForPrivateRoomSave());            
            Room.register(room);
        }
        return room;                            
    }
    
    /**
     * The method returns a the private room of the user if one exists
     * @param user - the user who owns the private room
     * @return - the private room of the user if one exists or null.
     */
    public Room getPrivateRoom(User user){
        Room room;
        room =privateRooms.get(user.userId);
        if(room==null){
            //retrieve from db
            JsonObject roomData=GameDB.getPrivateRoom(user.userId, gameName);
            if(roomData==null)
                return null;
            String roomName=JsonUtil.getAsString(roomData,"roomName");
            RoomType roomType=RoomType.valueOf(JsonUtil.getAsString(roomData,"roomType"));
            int totalNoOfSeats=JsonUtil.getAsInt(roomData, "totalNoOfSeats");
            int turnDurationSecs=JsonUtil.getAsInt(roomData, "turnDurationSecs");
            JsonObject additionalConfig=JsonUtil.getAsJsonObject(roomData, "additionalConfig");
            room=this.newRoom(roomName, RoomType.normal, totalNoOfSeats, turnDurationSecs,additionalConfig);
            room.ownerUserId=user.userId;
            Room oldRoom=privateRooms.put(user.userId, room);
            if(oldRoom!=null){
                privateRooms.put(user.userId, oldRoom);
                return oldRoom;
            }
            Room.register(room);
        }
        return room;                            
    }
    
    /**
     * Reconfigures a private room for a user if one exists. 
     * @param user - the user for whom the private room is created
     * @param totalNoOfSeats - the max number of players who can play in the room
     * @param turnDurationInSecs - the duration of 1 turn in seconds
     * @param additionalRoomConfig - additional configuration data for the room in json format
     * @throws GamePlayInProgressException - if this method is invoked while gamePlay is in progres
     * @throws SeatsNotEmptyException - if this method tries to change the totalNoOfSeats and the seats are not empty
     */
    public final void reconfigurePrivateRoom(User user,int totalNoOfSeats, int turnDurationInSecs,JsonObject additionalRoomConfig) throws SeatsNotEmptyException,GamePlayInProgressException{
        Room room=getPrivateRoom(user);
        if(room==null)
            return;
        room.configureRoom(totalNoOfSeats,turnDurationInSecs, additionalRoomConfig);
    }
    
    /**
     * This method destroys the private room of the user if one exists
     * @param user - the user who owns the private room
     */
    public final void destroyPrivateRoom(User user){
        Room room=getPrivateRoom(user);
        if(room!=null)
            room.destroy();
    }
    
    void removePrivateRoom(String userId,boolean destroy){
        Room room;
        room=privateRooms.remove(userId);
        if(room==null)
            return;
        if(destroy)
            GameDB.deletePrivateRoom(userId, gameName);
        else
            GameDB.savePrivateRoom(userId, gameName, room.toJsonForPrivateRoomSave());                
    }
        
    /**
     * Checks if user has a private room
     * @param user - the user whose private room existence is to be checked
     * @return boolean - true if the user has a private room else false 
     */
    public boolean hasPrivateRoom(User user){
        if(privateRooms.get(user.userId)!=null)
            return true;
        return GameDB.getPrivateRoom(user.userId, gameName)!=null;
    }
            
    /**
     * Creates a public room. 
     * @param locationName - the name of the location where the room will be created
     * @param roomName - the name of the room
     * @param roomType - the type of the room (normal or fast)
     * @param totalNoOfSeats - the max number of players who can play in the room
     * @param turnDurationInSecs - duration of a turn in seconds
     * @param additionalRoomConfig - additional configuration actionData for the room in json format
     * @return - the room that is created
     */
    public Room createPublicRoom(String locationName,String roomName,RoomType roomType,int totalNoOfSeats, int turnDurationInSecs,JsonObject additionalRoomConfig){
        Room room;
        Location location;
        instanceLock.lock();
        try{
            location=locations.get(Utils.tittleCase(locationName));            
            if(location==null)
                return null;
        }finally{instanceLock.unlock();}
        publicRoomsLock.lock();
        try{
            room=this.newRoom(roomName, roomType, totalNoOfSeats, turnDurationInSecs,additionalRoomConfig);
            if(location.addRoom(room))
                return room;
        }finally{publicRoomsLock.unlock();}
        return null;
    }
            
    String getLocationsAsString() {
        instanceLock.lock();
        try{
            if(locationsAsJsonString==null){
                JsonArrayBuilder jsonLocations=JsonUtil.createArrayBuilder();
                for(String locationName:locationNames)
                    jsonLocations.add(locationName);
                JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
                jsonBuilder.add("gameName", gameName);
                jsonBuilder.add("locations", jsonLocations);
                locationsAsJsonString= jsonBuilder.build().toString();                
            }
            return locationsAsJsonString;
        }finally{instanceLock.unlock();}
    }
    
    JsonObject getRoomsAsJson(String locationName,RoomType roomType) throws InvalidLocationNameException{
        Location location=null;
        instanceLock.lock();
        try{
            location=locations.get(Utils.tittleCase(locationName));
        }finally{instanceLock.unlock();}
        if(location==null)
            throw new InvalidLocationNameException();
        
        JsonArray jsonArray=location.getRoomsAsJson(roomType);
        JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
        jsonBuilder.add("gameName", gameName);
        jsonBuilder.add("location", location.name);
        jsonBuilder.add("roomType", roomType.toString());
        jsonBuilder.add("rooms", jsonArray);
        return jsonBuilder.build();
    }
    
    /**
     * Factory method which is called to create a new room for the Game at runtime. This method should be overridden in the Game class. Can return null
     * @param roomName - the name of the room
     * @param roomType - the type of the room
     * @param totalNoOfSeats - the max number of players who can play in the room
     * @param turnDurationInSecs - duration of a turn in seconds
     * @param additionalRoomConfig - additional configuration data for the room in json format
     * @return Room - the new room that is created
     */
    protected abstract Room newRoom(String roomName,RoomType roomType,int totalNoOfSeats, int turnDurationInSecs,JsonObject additionalRoomConfig);
    
    /**
     * Factory method for creating GameUserData for a first time player of the Game. This method should be overridden in the Game class
     * @return CompressibleData - the new instance of GameUserData
     */
    protected abstract CompressibleData newGameUserData();
    
    /**
     * Event-Handler/Callback - called when a client session enters the Game (i.e. selects the game)
     * @param session - the client session
     * @param firstTime - if the user selected the game for the first time
     */
    protected abstract void onEnterGame(Session session,boolean firstTime);

    /**
     * Event-Handler/Callback - called when a client session exits the Game (i.e. deselects the game)
     * @param session - the client session
     */
    protected abstract void onExitGame(Session session);
    
}
