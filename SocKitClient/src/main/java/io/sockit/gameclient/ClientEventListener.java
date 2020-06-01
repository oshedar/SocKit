package io.sockit.gameclient;

import javax.json.JsonObject;
import java.util.List;

/**
 * Interface for Client events/callbacks. An instance of this interface should be registered with the client object to handle async events received from the server
 * <br>
 * <pre>{@code
      public class TicTacToeClientListener implements ClientEventListener {
          {@literal @}Override
          public void onLoggedIn(Client client, boolean isGameSelected) {
              if(isGameSelected)
                  client.getRooms( "Mumbai", RoomType.normal); //fetches room list from Mumbai location
          }
 
          {@literal @}Override
          public void onGetRooms(Client client, String gameName, String location, RoomType roomtype, List<RoomInfo> rooms) {
              client.joinRoom(rooms.get(0).roomId); //joins the first room in the list
          }
          //....
      }
 } </pre>
 */
public interface ClientEventListener {

    /**
     * Event-Handler/Callback called when a message is received from the Server but before it is processed
     * @param client - the Client which received the message
     * @param serverMessageType - the messageType received from Server e.g. ServerMessageType.roomJoined or ServerMessageType.gamePlayStarted or ...
     * @param customMessageCommand - the custom message command if the serverMessageType is a custom message
     * @param isBinary - whether message data is binary or not
     * @param data - the message data
     */
    void beforeServerMessageProcessed(Client client,ServerMessageType serverMessageType,String customMessageCommand, boolean isBinary,Object data);

    /**
     * Event-Handler/Callback called when a message is received from the Server but after it is processed
     * @param client - the Client which received the message
     * @param serverMessageType - the messageType received from Server e.g. ServerMessageType.roomJoined or ServerMessageType.gamePlayStarted or ...
     * @param customMessageCommand - the custom message command if the serverMessageType is a custom message
     * @param isBinary - whether message data is binary or not
     * @param data - the message data
     */
    void afterServerMessageProcessed(Client client,ServerMessageType serverMessageType,String customMessageCommand,boolean isBinary,Object data);

    /**
     * Event-Handler/Callback called when an error is triggered on the server.
     * @param client - the Client which triggered the error
     * @param errorCode - the errorCode e.g. ErrorCodes.emailIdAndPasswordDoesNotMatch or ErrorCodes.seatNotFree or ...
     * @param errorDesc - the error description
     */
    void onError(Client client,int errorCode,String errorDesc);

    /**
     * Event-Handler/Callback called when the Client successfully logins a user into the server
     * @param client - the Client which loggedIn
     * @param isGameSelected - whether a Game is linked with this user session or not
     */
    void onLoggedIn(Client client,boolean isGameSelected);

    /**
     * Event-Handler/Callback called when the Client successfully logouts a user from the server
     * @param client - the client which loggedOut
     */
    void onLoggedOut(Client client);

    /**
     * Event-Handler/Callback called when the Client session times out on the server
     * @param client - the client whose session timed out
     */
    void onSessionTimedOut(Client client);

    /**
     * Event-Handler/Callback called when the server shutsdown
     * @param client - the Client which triggered this event
     */
    void onServerShutdown(Client client);

    /**
     * Event-Handler/Callback called when the Client receives the location list from the Server
     * @param client - the Client which triggered this event
     * @param gameName - the name of the game whose locations were requested by the Client
     * @param locations - the list of locations
     */
    void onGetLocations(Client client,String gameName,List<String> locations);

    /**
     * Event-Handler/Callback called when the Client receives the room list from the Server
     * @param client - the Client which triggered this event
     * @param gameName - the name of the game whose room list was requested by the Client
     * @param location - the location whose room list was requested
     * @param roomtype - the type of room list   e.g. RoomType.normal or RoomType.fast
     * @param rooms - the room list as a collection of RoomInfo objects
     */
    void onGetRooms(Client client,String gameName,String location,RoomType roomtype,List<RoomInfo> rooms);

    /**
     * Event-Handler/Callback called when the Client joins a room
     * @param client - the Client which triggered this event
     * @param room - the room joined by this Client
     */
    void onRoomJoined(Client client,Room room);

    /**
     * Event-Handler/Callback called when the room state is refreshed from the server. Gets triggered when Client receives a ServerMessageType.roomData message from the Server
     * @param client - the Client which triggered this event
     * @param room - the room joined by the client
     */
    void onRoomRefreshedFromServer(Client client,Room room);

    /**
     * Event-Handler/Callback called when any user takes a seat in the room.
     * @param client - the Client which receives this event
     * @param playerSeated - the player which took seat
     * @param room - the room joined by this client
     * @param isSelf - if the user seated is the same as this Client
     */
    void onSeatTaken(Client client,Player playerSeated,Room room,boolean isSelf);

    /**
     * Event-Handler/Callback called when any user leaves a seat in the room.
     * @param client - the Client which receives this event
     * @param playerLeft - the player which left the seat
     * @param room - the room joined by this client
     * @param isSelf - if the user which left is the same as this Client
     */
    void onSeatLeft(Client client,Player playerLeft,Room room,boolean isSelf);

    /**
     * Event-Handler/Callback called when the Client leaves the room
     * @param client - the Client which triggered this event
     */
    void onRoomLeft(Client client);

    /**
     * Event-Handler/Callback called when the Client receives a custom Json message from the server
     * @param client - the Client which receives the message
     * @param command - the message command
     * @param data - the message data as json
     */
    void onMessageReceivedJson(Client client,String command,JsonObject data);

    /**
     * Event-Handler/Callback called when the Client receives a custom text message from the server
     * @param client - the Client which receives the message
     * @param command - the message command
     * @param data - the message data as String
     */
    void onMessageReceivedString(Client client,String command,String data);

    /**
     * Event-Handler/Callback called when the Client receives a custom binary message from the server
     * @param client - the Client which receives the message
     * @param command - the message command
     * @param data - the message data as a byte array
     */
    void onMessageReceivedBytes(Client client,String command,byte[] data);

    /**
     * Event-Handler/Callback called when game play starts in the room
     * @param client - the Client which receives this event
     * @param room - the room joined by this client
     */
    void onGamePlayStarted(Client client,Room room);

    /**
     * Event-Handler/Callback called when a new turn begins
     * @param client - the Client which receives this event
     * @param turnPlayer - the player whose turn it is
     * @param turnData - the additional data received with the event. For e.g. in a game like Poker it could be {"callValue":20}
     * @param room - the room joined by this client
     * @param isSelfTurn - whether its the turn of the client which received this event
     */
    void onNextTurn(Client client,Player turnPlayer,JsonObject turnData,Room room,boolean isSelfTurn);

    /**
     * Event-Handler/Callback called when a player plays his/her turn
     * @param client - the Client which receives this event
     * @param turnPlayer - the player who played the action
     * @param playerAction - the action played by the player
     * @param actionData - the action data if any. For e.g. in a game like Poker it could be {"amtBet":20}
     * @param room - the room joined by this client
     * @param isSelf - whether the action was played by the client which received this event
     */
    void onTurnPlayed(Client client,Player turnPlayer,String playerAction,JsonObject actionData,Room room,boolean isSelf);

    /**
     * Event-Handler/Callback called when an action is played out of turn by a player
     * @param client - the Client which receives this event
     * @param outOfTurnPlayer - the player who played the action
     * @param playerAction - the action played by the player
     * @param actionData - the action data if any. 
     * @param room - the room joined by this client
     * @param isSelf - whether the action was played by the client which received this event
     */
    void onOutOfTurnPlayed(Client client,Player outOfTurnPlayer,String playerAction, JsonObject actionData,Room room,boolean isSelf);

    /**
     * Event-Handler/Callback called when a game action occurs in the game. For e.f. in a game like poker this could be flop cards dealt
     * @param client - the Client which receives this event
     * @param gameAction - the game action 
     * @param actionData - the action data. For e.g. in a game like poker this could be the value of the flop cards 
     * @param room - the room joined by this client
     */
    void onGameAction(Client client,String gameAction,JsonObject actionData,Room room);

    /**
     * Event-Handler/Callback called when game play ends in the room
     * @param client - the Client which receives this event
     * @param room - the room joined by this client
     * @param endGameData - the data received with the endGame event. For e.g this could be the winning seat no and the amount won
     */
    void onGamePlayEnded(Client client,Room room,JsonObject endGameData);

    /**
     * Event-Handler/Callback called when the room joined by this client is destroyed
     * @param client - the Client which receives this event
     * @param room - the room which was destroyed
     */
    void onRoomDestroyed(Client client,Room room);

    /**
     * Event-Handler/Callback called when the avtar of the user loggedIn via this client changes
     * @param client - the Client which receives this event
     * @param newAvtarId - the new avtarId of the user
     */
    void onAvtarChangedOfSelf(Client client,int newAvtarId);

    /**
     * Event-Handler/Callback called when the avtar of another player is changed
     * @param client - the Client which receives this event
     * @param player - the player whose avtar is changed
     * @param newAvtarId - the new avtarID
     * @param room - the room joined by this client
     */
    void onAvtarChangedOfOtherPlayer(Client client,Player player,int newAvtarId,Room room);

    /**
     * Event-Handler/Callback called when the Client is not eligible to play the new game play that is starting
     * @param client - the Client which receives this event
     * @param player - the player linked to this client
     * @param room - the room joined by this client
     * @param reason - the reason why player is not eligible
     */
    void onNotEligibleToPlay(Client client,Player player,Room room,String reason);

    /**
     * Event-Handler/Callback called when session is rejoined by this client. This event is triggered by the method Client.rejoinSession()
     * @param client - the Client which receives this event
     */
    void onSessionRejoined(Client client);

    /**
     * Event-Handler/Callback called when Client is attempting to connect to the server
     * @param client - the Client which triggers this event
     */
    void onConnecting(Client client);

    /**
     * Event-Handler/Callback called when Client is successfully connected to the server
     * @param client - the Client which triggers this event
     */
    void onConnectionSuccess(Client client);

    /**
     * Event-Handler/Callback called when Client is fails to connect to the server
     * @param client - the Client which triggers this event
     */
    void onConnectionFailure(Client client);

    /**
     * Event-Handler/Callback called when Client is disconnected from the server i.e. When the Server initiates the connection close.
     * @param client - the Client which triggers this event
     */
    void onConnectionDisconnected(Client client);

    /**
     * Event-Handler/Callback called when the connection to the server is closed i.e. When the Client initiates the connection close.
     * @param client - the Client which triggers this event
     */
    void onConnectionClosed(Client client);

    /**
     * Event-Handler/Callback called when an action played by the Client is not valid
     * @param client - the Client which triggers this event
     * @param action - the action played
     * @param description - the error description
     * @param errorData - the error data containing more info about the error
     * @param room - the room joined by the client
     * @param isOutOfTurn - true if the action was played out of turn
     */
    void onInvalidAction(Client client,String action,String description,JsonObject errorData,Room room,boolean isOutOfTurn);

    /**
     * Event-Handler/Callback called when Client enters/selects a game.
     * @param client - the Client which triggers this event
     * @param gameName - the name of the game entered/selected
     */
    void onEnterGame(Client client,String gameName);

    /**
     * Event-Handler/Callback called when Client exits/deselects a game.
     * @param client - the Client which triggers this event
     * @param gameName - the name of the game exited/deselected
     */
    void onExitGame(Client client,String gameName);
}
