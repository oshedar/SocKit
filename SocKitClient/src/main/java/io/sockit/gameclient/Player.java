/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.gameclient;

import javax.json.JsonObject;

/**
 * <div class="hidejava">
 * This class represents a Player seated in the room and can be used to render a player onto the UI
 * </div>
 * <div class="javascript" style="display:none;">
 * This object/function prototype represents a Player seated in the room and can be used to render a player onto the UI
 * </div>
 */
public class Player {

    /**
     * The userId of this player. The userId uniquely identifies a user registered on the GameServer
     */
    public final String userId;
    private String name;

    private int avtarId;
    private boolean useProfilePicture;

    /**
     * The seat number of the player. The first seat number is 1
     */
    public final int seatNo;
    private boolean inGame;
    private int turnTimeLeftMillis;
    private JsonObject gameData;
    private long whenTurnTimeLeftMillisWasSet;
    private String profilePic; 

    /**
     * The room in which the player is seated
     */
    public volatile Room room;
    
    private Player(String userId, int seatNo) {
        this.userId = userId;
        this.name = null;
        this.avtarId = 0;
        this.useProfilePicture = false;
        this.seatNo = seatNo;
        this.inGame = false;
        this.turnTimeLeftMillis = 0;
        this.whenTurnTimeLeftMillisWasSet=System.currentTimeMillis();
        this.gameData = null;
    }
    
    static Player newPlayer(JsonObject jsonObject,Client client,Room room){
        Player player=new Player(JsonUtil.getAsString(jsonObject,"userId"), JsonUtil.getAsInt(jsonObject,"seatNo"));
        player.refreshPlayerData(jsonObject,client,room);
        return player;
    }
    
    void refreshPlayerData(JsonObject jsonObject,Client client,Room room){
        this.name=JsonUtil.getAsString(jsonObject,"name");
        this.avtarId=JsonUtil.getAsInt(jsonObject,"avtarId");
        this.useProfilePicture=JsonUtil.getAsBoolean(jsonObject,"useProfilePicture");
        this.profilePic=JsonUtil.getAsString(jsonObject, "profilePic");
        this.inGame=JsonUtil.getAsBoolean(jsonObject,"inGame");
        this.turnTimeLeftMillis=JsonUtil.getAsInt(jsonObject,"turnTimeLeftMillis");
        this.whenTurnTimeLeftMillisWasSet=System.currentTimeMillis();
        this.gameData=JsonUtil.getAsJsonObject(jsonObject,"gameData");
        this.room=room;
    }
    
    /**
     * The name of the player
     * @return String - the name of the player
     */
    public String getName() {
        return name;
    }

    /**
     * The avatar Id of the player
     * @return int - the avatar Id of the player
     */
    public int getAvtarId() {
        return avtarId;
    }

    /**
     * Indicates whether to display the player's profile picture or the player's avatar
     * @return boolean - true if the player's profile picture should be displayed instead of the player's avatar
     */
    public boolean shouldUseProfilePicture() {
        return useProfilePicture;
    }

    /**
     * Returns the url of player's profile picture
     * @return String - the url of player's profile picture
     */
    public String getProfilePic() {
        return profilePic;
    }

    /**
     * Returns true if the player is an active player (taking part in the current game play)
     * @return boolean - true if the player is an active player 
     */
    public boolean isActive() {
        return inGame;
    }

    /**
     * Returns the turn time left in milliseconds
     * @return int - the turn time left in milliseconds
     */
    public int getTurnTimeLeftMillis() {
        long timeDiff=System.currentTimeMillis()-whenTurnTimeLeftMillisWasSet;
        if(timeDiff>turnTimeLeftMillis)
            return (int)0;
        return (int) (turnTimeLeftMillis-timeDiff);
//        return turnTimeLeftMillis;
    }

    /**
     * Returns the player's data as json
     * @return JsonObject - the player's data as json
     */
    public JsonObject getData() {
        return gameData;
    }
    
    /**
     * Returns true if the current turn is the players turn
     * @return boolean - true if the current turn is the players turn
     */
    public boolean isCurTurn(){
        return this.room.isCurTurn(this);
    }
    
    /**
     * Returns true if the player belongs to the specified client
     * @param client - the client to check
     * @return boolean - true if the player belongs to the specified client
     */
    public boolean isSameAsClient(Client client){
        return this.userId.equals(client.userId);
    }
}
