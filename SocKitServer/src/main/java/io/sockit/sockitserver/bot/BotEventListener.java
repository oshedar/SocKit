  /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver.bot;

import javax.json.JsonObject;
import java.util.List;

/**
 * Interface for Bot events/callbacks. An instance of this interface should be registered with the bot object to handle async events received from the server
 * <br>
 * <pre>{@code
      public class TicTacToeBotListener implements BotEventListener {
          {@literal @}Override
          public void onLoggedIn(Bot bot) {
              bot.getRooms( "Mumbai", RoomType.normal); //fetches room list from Mumbai location
          }
 
          {@literal @}Override
          public void onGetRooms(Bot bot, String gameName, String location, RoomType roomtype, List&lt;RoomInfo&gt; rooms) {
              bot.joinRoom(rooms.get(0).roomId); //joins the first room in the list
          }
          ....
      }
 } </pre>
 */
public interface BotEventListener {

    /**
     * Event-Handler/Callback called when an error is triggered on the server.
     * @param bot - the Bot which triggered the error
     * @param errorCode - the errorCode e.g. ErrorCodes.emailIdAndPasswordDoesNotMatch or ErrorCodes.seatNotFree or ...
     * @param errorDesc - the error description
     */
    void onError(Bot bot,int errorCode,String errorDesc);

    /**
     * Event-Handler/Callback called when the Bot successfully logins into the server
     * @param bot - the Bot which loggedIn
     */
    void onLoggedIn(Bot bot);

    /**
     * Event-Handler/Callback called when the Bot successfully logouts from the server
     * @param bot - the bot which loggedOut
     */
    void onLoggedOut(Bot bot);

    /**
     * Event-Handler/Callback called when the Bot's session times out on the server
     * @param bot - the bot whose session timed out
     */
    void onSessionTimedOut(Bot bot);

    /**
     * Event-Handler/Callback called when the server shutsdown
     * @param bot - the Bot which triggered this event
     */
    void onServerShutdown(Bot bot);

    /**
     * Event-Handler/Callback called when the Bot receives the location list from the Server
     * @param bot - the Bot which triggered this event
     * @param gameName - the name of the game whose locations were requested by the Bot
     * @param locations - the list of locations
     */
    void onGetLocations(Bot bot,String gameName,List<String> locations);

    /**
     * Event-Handler/Callback called when the Bot receives the room list from the Server
     * @param bot - the Bot which triggered this event
     * @param gameName - the name of the game whose room list was requested by the Bot
     * @param location - the location whose room list was requested
     * @param roomtype - the type of room list   e.g. RoomType.normal or RoomType.fast
     * @param rooms - the room list as a collection of RoomInfo objects
     */
    void onGetRooms(Bot bot,String gameName,String location,RoomType roomtype,List<RoomInfo> rooms);

    /**
     * Event-Handler/Callback called when the Bot joins a room
     * @param bot - the Bot which triggered this event
     * @param room - the room joined by this Bot
     */
    void onRoomJoined(Bot bot,Room room);

    /**
     * Event-Handler/Callback called when the room state is refreshed from the server. Gets triggered when Bot receives a ServerMessageType.roomData message from the Server
     * @param bot - the Bot which triggered this event
     * @param room - the room joined by the bot
     */
    void onRoomRefreshedFromServer(Bot bot,Room room);

    /**
     * Event-Handler/Callback called when any user takes a seat in the room.
     * @param bot - the Bot which receives this event
     * @param playerSeated - the player which took seat
     * @param room - the room joined by this bot
     * @param isSelf - if the user seated is the same as this Bot
     */
    void onSeatTaken(Bot bot,Player playerSeated,Room room,boolean isSelf);

    /**
     * Event-Handler/Callback called when any user leaves a seat in the room.
     * @param bot - the Bot which receives this event
     * @param playerLeft - the player which left the seat
     * @param room - the room joined by this bot
     * @param isSelf - if the user which left is the same as this Bot
     */
    void onSeatLeft(Bot bot,Player playerLeft,Room room,boolean isSelf);

    /**
     * Event-Handler/Callback called when the Bot leaves the room
     * @param bot - the Bot which triggered this event
     */
    void onRoomLeft(Bot bot);

    /**
     * Event-Handler/Callback called when the Bot receives a custom Json message from the server
     * @param bot - the Bot which receives the message
     * @param command - the message command
     * @param data - the message data as json
     */
    void onMessageReceivedJson(Bot bot,String command,JsonObject data);

    /**
     * Event-Handler/Callback called when the Bot receives a custom text message from the server
     * @param bot - the Bot which receives the message
     * @param command - the message command
     * @param data - the message data as String
     */
    void onMessageReceivedString(Bot bot,String command,String data);

    /**
     * Event-Handler/Callback called when the Bot receives a custom binary message from the server
     * @param bot - the Bot which receives the message
     * @param command - the message command
     * @param data - the message data as a byte array
     */
    void onMessageReceivedBytes(Bot bot,String command,byte[] data);

    /**
     * Event-Handler/Callback called when game play starts in the room
     * @param bot - the Bot which receives this event
     * @param room - the room joined by this bot
     */
    void onGamePlayStarted(Bot bot,Room room);

    /**
     * Event-Handler/Callback called when a new turn begins
     * @param bot - the Bot which receives this event
     * @param turnPlayer - the player whose turn it is
     * @param turnData - the additional data received with the event. For e.g. in a game like Poker it could be {"callValue":20}
     * @param room - the room joined by this bot
     * @param isSelfTurn - whether its the turn of the bot which received this event
     */
    void onNextTurn(Bot bot,Player turnPlayer,JsonObject turnData,Room room,boolean isSelfTurn);

    /**
     * Event-Handler/Callback called when a player plays his/her turn
     * @param bot - the Bot which receives this event
     * @param turnPlayer - the player who played the action
     * @param playerAction - the action played by the player
     * @param actionData - the action data if any. For e.g. in a game like Poker it could be {"amtBet":20}
     * @param room - the room joined by this bot
     * @param isSelf - whether the action was played by the bot which received this event
     */
    void onTurnPlayed(Bot bot,Player turnPlayer,String playerAction,JsonObject actionData,Room room,boolean isSelf);

    /**
     * Event-Handler/Callback called when an action is played out of turn by a player
     * @param bot - the Bot which receives this event
     * @param outOfTurnPlayer - the player who played the action
     * @param playerAction - the action played by the player
     * @param actionData - the action data if any. 
     * @param room - the room joined by this bot
     * @param isSelf - whether the action was played by the bot which received this event
     */
    void onOutOfTurnPlayed(Bot bot,Player outOfTurnPlayer,String playerAction, JsonObject actionData,Room room,boolean isSelf);

    /**
     * Event-Handler/Callback called when a game action occurs in the game. For e.f. in a game like poker this could be flop cards dealt
     * @param bot - the Bot which receives this event
     * @param gameAction - the game action 
     * @param actionData - the action data. For e.g. in a game like poker this could be the value of the flop cards 
     * @param room - the room joined by this bot
     */
    void onGameAction(Bot bot,String gameAction,JsonObject actionData,Room room);

    /**
     * Event-Handler/Callback called when game play ends in the room
     * @param bot - the Bot which receives this event
     * @param room - the room joined by this bot
     * @param endGameData - the data received with the endGame event. For e.g this could be the winning seat no and the amount won
     */
    void onGamePlayEnded(Bot bot,Room room,JsonObject endGameData);

    /**
     * Event-Handler/Callback called when the room joined by this bot is destroyed
     * @param bot - the Bot which receives this event
     * @param room - the room which was destroyed
     */
    void onRoomDestroyed(Bot bot,Room room);

    /**
     * Event-Handler/Callback called when the avtar of the user loggedIn via this bot changes
     * @param bot - the Bot which receives this event
     * @param newAvtarId - the new avtarId of the user
     */
    void onAvtarChangedOfSelf(Bot bot,int newAvtarId);

    /**
     * Event-Handler/Callback called when the avtar of another player is changed
     * @param bot - the Bot which receives this event
     * @param player - the player whose avtar is changed
     * @param newAvtarId - the new avtarID
     * @param room - the room joined by this bot
     */
    void onAvtarChangedOfOtherPlayer(Bot bot,Player player,int newAvtarId,Room room);

    /**
     * Event-Handler/Callback called when the Bot is not eligible to play the new game play that is starting
     * @param bot - the Bot which receives this event
     * @param player - the player linked to this bot
     * @param room - the room joined by this bot
     * @param reason - the reason why player is not eligible
     */
    void onNotEligibleToPlay(Bot bot,Player player,Room room,String reason);

    /**
     * Event-Handler/Callback called when an action played by the Bot is not valid
     * @param bot - the Bot which triggers this event
     * @param action - the action played
     * @param description - the error description
     * @param errorData - the error data containing more info about the error
     * @param room - the room joined by the bot
     * @param isOutOfTurn - true if the action was played out of turn
     */
    void onInvalidAction(Bot bot,String action,String description,JsonObject errorData,Room room,boolean isOutOfTurn);

}
	