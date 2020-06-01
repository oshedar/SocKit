package io.sockit.sockitserver;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * This class represents a player in a game room. When a client session joins a room, the session is considered to be a spectator. A player instance gets created when a session takes seat to start playing in the room. Each Game should provide an implementation of this class as follows
 * <pre>
 * {@code
 *    public class TicTacToePlayer extends Player{
 *
 *        CellState playerToken;// X or 0
 *        
 *        public TicTacToePlayer(Session session, int seatNo) {
 *                super(session, seatNo);
 *                playerToken = seatNo == 1 ? CellState.x : CellState.o;
 *        }
 *        
 *        {@literal @}Override
 *        protected JsonObject prepareJsonForSelfClient() {
 *                JsonObjectBuilder json = Json.createObjectBuilder();
 *                json.add("playerToken", playerToken.toString());                
 *                return json.build();
 *        }
 *
 *        {@literal @}Override
 *        protected boolean canJoinGamePlay() {
 *                return true;
 *        }
 *       //..........
 *    }
 * }
 * </pre>
 */
public abstract class Player {

    /**
     * The client session of the player.
     */
    public final Session session;
    /**
     * The seat number of the player
     */
    public final int seatNo;
    /**
     * the room of the player
     */
    public final Room room;
    
    long turnEndTimeInMillis;
    
    /**
     * Creates a Player whose session is the specified session and whose seat number is the specified seat number
     * @param session - the player's client session
     * @param seatNo - the seat number of the player
     */
    public Player(Session session,int seatNo) {
        this.session = session;
        this.seatNo=seatNo;
        this.room=session.room;
    }
    
    private JsonObject tmpJsonForSelf;
    private JsonObject tmpJsonForOthers;
    
    final void createTmpJson(){
        tmpJsonForSelf=createJson(false);
        tmpJsonForOthers=createJson(true);
    }
    
    final void clearTmpJson(){
        tmpJsonForSelf=null;
        tmpJsonForOthers=null;
    }
    
    private static final JsonObject EMPTY_JSON_OBJECT=JsonObject.EMPTY_JSON_OBJECT;

    private JsonObject createJson(boolean forOthers){
        long turnTimeleftInMillis=0;
        if(isCurTurnPlayer()){
            turnTimeleftInMillis=turnEndTimeInMillis-System.currentTimeMillis();                
            if(turnTimeleftInMillis<0)
                turnTimeleftInMillis=0;            
        }
        JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
        jsonBuilder.add("userId", session.user.userId);
        jsonBuilder.add("name", session.user.name);
        jsonBuilder.add("avtarId", session.user.avtarId);
        jsonBuilder.add("useProfilePicture", session.user.enableProfilePicture);
        jsonBuilder.add("profilePic", session.user.profilePic);
        jsonBuilder.add("seatNo", seatNo);
        jsonBuilder.add("inGame", room==session.room?room.isActive(this):false);
        jsonBuilder.add("turnTimeLeftMillis", turnTimeleftInMillis);
        JsonObject dataAsJson;
        if(forOthers){
            dataAsJson=prepareJsonForOtherClients();
        }
        else{
            dataAsJson=prepareJsonForSelfClient();
        }
        jsonBuilder.add("gameData", dataAsJson==null?EMPTY_JSON_OBJECT:dataAsJson);
        return jsonBuilder.build();        
    }
    
    final JsonObject toJson(boolean forOthers){
        if(tmpJsonForOthers!=null){
            if(forOthers)
                return tmpJsonForOthers;
            else
                return tmpJsonForSelf;
        }
        return createJson(forOthers);
    }
    
    /**
     * Causes player to leave seat. After calling this method the client session becomes a spectator and the player instance is destroyed.
     */
    public final void leaveSeat(){
        room.leaveSeat(this);
    }
    
    /**
     * Causes player to exit the current game play. <b>Note</b> player still remains seated and will take part/play in the next game play after the current game play ends. In a game like poker exitGamePlay() can be called when the player folds 
     */
    public final void exitGamePlay(){ 
        if(room!=session.room)
            return;
        room.exitGamePlay(this);
    }
    
    /**
     * Checks if the player is still active in the current game play. For example in a game like poker player will remain active till the player folds or the game ends
     * @return boolean - true if the player is active in the current game play
     */
    public final boolean isActive(){
        return room.isActive(this);
    }
    
    /**
     * checks whether player was or is active in the current game play - returns true if a player was active when the game play began.
     * @return boolean - true if the player was active when the game play began
     */
    public final boolean wasActive(){
        return room.wasActive(this);
    }
        
    /**
     * Checks if the current turn is the player's or not
     * @return boolean - true if the current turn is the player's
     */
    public final boolean isCurTurnPlayer(){
        return room.getCurTurnPlayer()==this;
    }
    
    /**
     * Converts playerData to json that will be sent to other clients. This method is called whenever a player's data has to sent to other clients. This method should be overridden in the actual Player class
     * @return JsonObject - the player data as json
     */
    abstract protected JsonObject prepareJsonForOtherClients();
   
    /**
     * Converts playerData to json that will be sent to own client. This method is called whenever a player's data has to sent to own client. This method should be overridden in the actual Player class
     * @return JsonObject - the player data as json
     */
    abstract protected JsonObject prepareJsonForSelfClient();
       
    /**
     * EventHandler/CallBack called when the Game Engine is shutdown. 
     */
    protected void onShutDown(){        
    }
    
    /**
     * EventHandler/CallBack called when Room is destroyed. In a game like poker you may restore the player's chips in hand from the chips on table in this event
     */
    protected void onRoomDestroyed(){        
    }
    
    /**
     * Callback called when game play has ended to reset player data
     */
    abstract protected void resetData();//called in end game play to reset player data
    
    /**
     * EventHandler/CallBack when player can't join the game play. You may force player to leave seat in this event. Should return the reason why player cant join if you want to send the cantJoinGamePlay message to the client else return null
     * @return String - the reason why the player can't join the game play or null if no message should be sent to the client
     */
    protected String onCantJoinGamePlay(){
        return null;
    }
    
    /**
     * called in startGamePlay() to check if player is allowed to play (takePart) in the new game play
     * @return boolean - true if the player is allowed to join the game play
     */
    abstract protected boolean canJoinGamePlay();
    
    /**
     * Called to check if player can play turn or not. For example in poker if player is all in or folded then player cant play turn and the turn should go to the next player.
     * @return boolean - true if the player can play the turn.
     */
    abstract protected boolean canPlayTurn();
}
