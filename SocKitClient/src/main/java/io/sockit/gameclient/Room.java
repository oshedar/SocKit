/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.gameclient;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * <div class="hidejava">
 * This class represents a game room. An instance of this class is passed to many of the event/callback methods in the ClientListener interface and can be used to render the room and game play onto the UI
 * </div>
 * <div class="javascript" style="display:none;">
 * This object/function prototype represents a game room. An instance of this class is passed to many of the event/callback methods in the ClientListener interface and can be used to render the room and game play onto the UI
 * </div>
 */
public class Room implements Iterable<Player> {

    /**
     * The name of the game that this room belongs to
     */
    public final String gameName;

    /**
     * The location of this room
     */
    public final String location;

    /**
     * The roomID of this room
     */
    public final long roomId;

    /**
     * The name of this room
     */
    public final String roomName;

    /**
     * The total number of seats in this room
     */
    public final int totalNoOfSeats;

    /**
     * The duration of a turn in this room in milliseconds
     */
    public final int turnDurationMillis;

    /**
     * The type of this room (normal or fast)
     */
    public final RoomType roomType;
    private final String ownerUserId;
    boolean gameInProgress;
    boolean gameEnding;
    int gameNo;
    private JsonObject gameData;

    int curTurnSeatNo;
    final private Player[] seats;
    private int occupiedCount;

    private transient int modCount = 0;

    private Room(String ownerUserId, String location, RoomType roomType, String gameName, long roomId, String roomName, int totalNoOfSeats, int turnDurationMillis) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.totalNoOfSeats = totalNoOfSeats;
        this.turnDurationMillis = turnDurationMillis;
        this.gameName = gameName;
        this.roomType = roomType;
        this.location = location;
        this.ownerUserId = ownerUserId;
        this.seats = new Player[totalNoOfSeats];
        this.occupiedCount = 0;
    }

    static Room newRoom(JsonObject jsonObject, Client client) {
        String gameName = JsonUtil.getAsString(jsonObject,"gameName");
        String location = JsonUtil.getAsString(jsonObject, "location");
        String ownerUserId = JsonUtil.getAsString(jsonObject,"ownerUserId");
        RoomType roomType = RoomType.normal;
        if ("fast".equals(JsonUtil.getAsString(jsonObject,"roomType"))) {
            roomType = RoomType.fast;
        }
        long roomId = JsonUtil.getAsLong(jsonObject,"roomId");
        String roomName = JsonUtil.getAsString(jsonObject,"roomName");
        int totalNoOfSeats = JsonUtil.getAsInt(jsonObject,"totalNoOfSeats");
        int turnDurationMillis = JsonUtil.getAsInt(jsonObject,"turnDurationMillis");
        Room room = new Room(ownerUserId, location, roomType, gameName, roomId, roomName, totalNoOfSeats, turnDurationMillis);
        room.refreshRoomData(jsonObject, client);
        return room;
    }

    /**
     * Returns true if gamePlay is in progress in this room
     * @return boolean - true if gamePlay is in progress in this room
     */
    public boolean isGamePlayInProgress() {
        return this.gameInProgress;
    }

    /**
     * Returns true if gamePlay is ending in this room
     * @return - true if gamePlay is ending in this room
     */
    public boolean isGamePlayEnding() {
        return gameEnding;
    }

    /**
     * Returns true if this room is private
     * @return - true if this room is private
     */
    public boolean isPrivate() {
        return this.ownerUserId != null;
    }

    /**
     * Returns true if the specified user owns this room.
     * @param userId - the userId of the user to check for ownership of this room
     * @return - true if the specified user owns this room.
     */
    public boolean isOwner(String userId) {
        if (userId == null || this.ownerUserId == null) {
            return false;
        }
        return userId.equals(this.ownerUserId);
    }

    /**
     * Returns the room data as json
     * @return JsonObject - the room data as json
     */
    public JsonObject getData() {
        return this.gameData;
    }

    void refreshRoomData(JsonObject jsonObject, Client client) {
        JsonObject[] playersAsJson = new JsonObject[this.totalNoOfSeats + 1];        
        this.gameInProgress = JsonUtil.getAsBoolean(jsonObject,"gameInProgress");
        this.gameEnding = JsonUtil.getAsBoolean(jsonObject,"gameEnding");
        this.gameNo = JsonUtil.getAsInt(jsonObject,"gameNo");
        this.gameData = JsonUtil.getAsJsonObject(jsonObject,"data");
        this.curTurnSeatNo = JsonUtil.getAsInt(jsonObject,"curTurnSeatNo");
        JsonArray jsonArray = JsonUtil.getAsJsonArray(jsonObject,"players");
        Player player;
        JsonObject playerAsJson;
        int seatNo;
        for (JsonValue jsonValue : jsonArray) {
            playerAsJson = (JsonObject) jsonValue;
            seatNo = JsonUtil.getAsInt(playerAsJson,"seatNo");
            playersAsJson[seatNo] = playerAsJson;
        }
        for (int ctr = 1; ctr < playersAsJson.length; ctr++) {
            if (playersAsJson[ctr] == null) {
                this.remove(ctr);
                continue;
            }
            player = this.getPlayerBySeatNo(ctr);
            if (player == null) {
                this.set(ctr, Player.newPlayer(playersAsJson[ctr], client,this));
                continue;
            }
            if (player.userId.equals(playersAsJson[ctr].get("userId"))) {
                player.refreshPlayerData(playersAsJson[ctr], client,this);
            } else {
                this.set(ctr, Player.newPlayer(playersAsJson[ctr], client,this));
            }
        }
    }
    
    /**
     * Returns the number of the currently active game play in this room. When a new game play starts, the gamePlay number is incremented by 1. The number of the first game play is 1. This value is mainly for debugging purposes to identify which game play number the debug message belongs to
     * @return int - the number of the currently active game play in this room
     */
    public final int getGamePlayNo(){
        return gameNo;
    }

    void setData(JsonObject data){
        this.gameData=data;
    }
    
    boolean isSeated(String userId) {
        for (Player player : this.seats) {
            if (player != null && player.userId.equals(userId)) {
                return true;
            }
        }
        return false;
    }

    boolean isCurTurn(Player player) {
        return this.gameInProgress && this.curTurnSeatNo == player.seatNo && this.getPlayerBySeatNo(player.seatNo) == player;
    }

    /**
     * Returns the seat number of the player whose turn is the current turn 
     * @return int - the seat number of the player whose turn is the current turn 
     */
    public int getCurTurnSeatNo() {
        if (!this.gameInProgress) {
            return 0;
        }
        return this.curTurnSeatNo;
    }

    /**
     * Returns the player whose turn is the current turn
     * @return Player -  the player whose turn is the current turn
     */
    public Player getCurTurnPlayer() {
        if (!this.gameInProgress || this.curTurnSeatNo < 1) {
            return null;
        }
        return this.getPlayerBySeatNo(this.curTurnSeatNo);
    }

    /**
     * Returns the player whose userId matches the specified userId
     * @param userId - the userId to match
     * @return Player - the player whose userId matches the specified userId
     */
    public Player getPlayerByUserId(String userId) {
        for (Player player : this.seats) {
            if (player != null && player.userId.equals(userId)) {
                return player;
            }
        }
        return null;
    }

    /**
     * Returns the number of players seated in the room
     * @return - the number of players seated in the room
     */
    public int getPlayerCount() {
        return this.occupiedCount;
    }

    void clear() {
        for (int ctr = 0; ctr < this.seats.length; ctr++) {
            this.seats[ctr] = null;
        }
        this.modCount++;
        this.occupiedCount = 0;
    }

    Player set(int seatNo, Player player) {
        if (seatNo < 1 || seatNo > this.seats.length) {
            throw new ArrayIndexOutOfBoundsException(seatNo);
        }
        Player oldPlayer = this.seats[seatNo - 1];
        if (player == null) {
            if (oldPlayer != null) {
                this.occupiedCount--;
                this.modCount++;
            }
            this.seats[seatNo - 1] = null;
            return oldPlayer;
        }
        if (oldPlayer == null) {
            this.occupiedCount++;
        }
        this.seats[seatNo - 1] = player;
        this.modCount++;
        return oldPlayer;
    }

    Player remove(int seatNo) {
        if (seatNo < 1 || seatNo > this.seats.length) {
            throw new ArrayIndexOutOfBoundsException(seatNo);
        }
        Player player = this.seats[seatNo - 1];
        this.seats[seatNo - 1] = null;
        if (player != null) {
            this.modCount++;
            this.occupiedCount--;
        }
        return player;
    }

    Player remove(Player player) {
        if (player == null) {
            throw new NullPointerException();
        }
        for (int ctr = 0; ctr < this.seats.length; ctr++) {
            if (this.seats[ctr] == player) {
                this.seats[ctr] = null;
                this.modCount++;
                this.occupiedCount--;
                return player;
            }
        }
        return null;
    }

    /**
     * Returns the player seated at the specified seat number
     * @param seatNo - the specified seat number
     * @return Player - the player seated at the specified seat number
     */
    public Player getPlayerBySeatNo(int seatNo) {
        if (seatNo < 1 || seatNo > this.seats.length) {
            throw new ArrayIndexOutOfBoundsException(seatNo);
        }
        return this.seats[seatNo - 1];
    }

    /**
     * Returns the first free seat number
     * @return int - the first free seat number
     */
    public int getFreeSeatNo() {
        for (int ctr = 0; ctr < this.seats.length; ctr++) {
            if (this.seats[ctr] == null) {
                return ctr + 1;
            }
        }
        return 0;
    }

    /**
     * Returns true if the seat number is free
     * @param seatNo - the seat number to check
     * @return boolean - true if the seat number is free
     */
    public boolean isSeatFree(int seatNo) {
        if (seatNo < 1 || seatNo > this.seats.length) {
            throw new ArrayIndexOutOfBoundsException(seatNo);
        }
        return this.seats[seatNo - 1] == null;
    }

    /**
     * Returns the next player seated after the specified seat number
     * @param prevSeatNo - the previous seat number 
     * @return Player - the next player seated after the specified seat number
     */
    public Player getNextPlayer(int prevSeatNo) {
        if (prevSeatNo < 1 || prevSeatNo > this.seats.length) {
            throw new ArrayIndexOutOfBoundsException(prevSeatNo);
        }
        for (int ctr = prevSeatNo; ctr < this.seats.length; ctr++) {
            if (this.seats[ctr] != null) {
                return this.seats[ctr];
            }
        }
        for (int ctr = 0; ctr < prevSeatNo - 1; ctr++) {
            if (this.seats[ctr] != null) {
                return this.seats[ctr];
            }
        }
        return null;
    }

    /**
     * Returns the next player seated after the specified player
     * @param prevPlayer - the previous player
     * @return player - the next player seated after the specified player
     */
    public Player getNextPlayer(Player prevPlayer) {
        if (prevPlayer.seatNo > 0 && this.seats[prevPlayer.seatNo-1]==prevPlayer) {
            return this.getNextPlayer(prevPlayer.seatNo);
        }
        return null;
    }

    /**
     * Returns an iterator which iterates through all the players in the room
     * @return Iterator - an iterator which iterates through all the players in the room
     */
    @Override
    public Iterator<Player> iterator() {
        return new SeatIterator(this.modCount);
    }

    private class SeatIterator implements Iterator<Player> {

        int curIndex;
        int expectedModCount;

        SeatIterator(int modCount) {
            this.expectedModCount = modCount;
            for (this.curIndex = 0; this.curIndex < Room.this.seats.length; this.curIndex++) {
                if (Room.this.seats[this.curIndex] != null) {
                    break;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return this.curIndex < Room.this.seats.length;
        }

        @Override
        public Player next() {
            if (Room.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (this.curIndex >= Room.this.seats.length) {
                throw new NoSuchElementException();
            }
            Player player = Room.this.seats[this.curIndex++];
            for (; this.curIndex < Room.this.seats.length; this.curIndex++) {
                if (Room.this.seats[this.curIndex] != null) {
                    break;
                }
            }
            return player;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

}
