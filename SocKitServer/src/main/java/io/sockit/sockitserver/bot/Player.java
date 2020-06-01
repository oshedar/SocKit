/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver.bot;

import io.sockit.sockitserver.JsonUtil;
import javax.json.JsonObject;

/**
 * This class represents a Player seated in the room for a Bot
 */
public class Player {

    /**
     * The userId of this player.
     */
    public final String userId;
    private String name;

//    private int avtarId;
//    private boolean useProfilePicture;

    /**
     * The seat number of the player. The first seat number is 1
     */
    public final int seatNo;
    private boolean inGame;
    private int turnTimeLeftMillis;
    private JsonObject gameData;
    private long whenTurnTimeLeftMillisWasSet;
//    private String profilePic;
    
    /**
     * The room in which the player is seated
     */
    public volatile Room room;

    private Player(String userId, int seatNo) {
        this.userId = userId;
        this.name = null;
//        this.avtarId = 0;
//        this.useProfilePicture = false;
        this.seatNo = seatNo;
        this.inGame = false;
        this.turnTimeLeftMillis = 0;
        this.whenTurnTimeLeftMillisWasSet=System.currentTimeMillis();
        this.gameData = null;
    }
    
    static Player newPlayer(JsonObject jsonObject,Bot bot,Room room){
        Player player=new Player(JsonUtil.getAsString(jsonObject,"userId"), JsonUtil.getAsInt(jsonObject,"seatNo"));
        player.refreshPlayerData(jsonObject,bot,room);
        return player;
    }
    
    void refreshPlayerData(JsonObject jsonObject,Bot bot,Room room){
        this.name=JsonUtil.getAsString(jsonObject,"name");
//        this.avtarId=JsonUtil.getAsInt(jsonObject,"avtarId");
//        this.useProfilePicture=JsonUtil.getAsBoolean(jsonObject,"useProfilePicture");
//        this.profilePic=Json.getAsString(jsonObject, "profilePic");
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

//    public int getAvtarId() {
//        return avtarId;
//    }

//    public boolean useProfilePicture() {
//        return useProfilePicture;
//    }
//
//    public String getProfilePic() {
//        return profilePic;
//    }


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
     * Returns true if the player belongs to the specified bot
     * @param bot - the bot to check
     * @return boolean - true if the player belongs to the specified bot
     */
    public boolean isSameAsBot(Bot bot){
        return this.userId.equals(bot.userId);
    }
}
