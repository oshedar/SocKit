/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.gameclient;

import javax.json.JsonObject;
import java.util.List;

/**
 * {@inheritDoc}
 * <div class="hidejava">
 * An abstract adapter class for receiving client events. The methods in this class are empty.
 * This class exists as convenience for creating ClientEventListener objects.
 * </div>
 * <div class="javascript" style="display:none;">
 *  Events/callbacks triggered by the Client object. These functions should be coded in an EventHandler object which is then registered with the client by invoking the setClientEventListener method on the client.
 * </div>
 */
public abstract class ClientEventAdapter implements ClientEventListener{
    
    @Override
    public void beforeServerMessageProcessed(Client client, ServerMessageType serverMessageType,String customMessageCommand, boolean isBinary,Object data) {
    }

    @Override
    public void afterServerMessageProcessed(Client client, ServerMessageType serverMessageType,String customMessageCommand, boolean isBinary,Object data) {
    }

    @Override
    public void onError(Client client, int errorCode, String errorDesc) {
    }

    @Override
    public void onLoggedIn(Client client, boolean isGameSelected) {
    }

    @Override
    public void onLoggedOut(Client client) {
    }

    @Override
    public void onSessionTimedOut(Client client) {
    }

    @Override
    public void onServerShutdown(Client client) {
    }

    @Override
    public void onGetLocations(Client client, String gameName, List<String> locations) {
    }

    @Override
    public void onGetRooms(Client client, String gameName, String location, RoomType roomtype, List<RoomInfo> rooms) {
    }

    @Override
    public void onRoomJoined(Client client, Room room) {
    }

    @Override
    public void onRoomRefreshedFromServer(Client client, Room room) {
    }

    @Override
    public void onSeatTaken(Client client, Player playerSeated, Room room, boolean isSelf) {
    }

    @Override
    public void onSeatLeft(Client client, Player playerLeft, Room room, boolean isSelf) {
    }

    @Override
    public void onRoomLeft(Client client) {
    }

    @Override
    public void onMessageReceivedJson(Client client, String command, JsonObject data) {
    }

    @Override
    public void onMessageReceivedString(Client client, String command, String data) {
    }

    @Override
    public void onMessageReceivedBytes(Client client, String command, byte[] data) {
    }

    @Override
    public void onGamePlayStarted(Client client, Room room) {
    }

    @Override
    public void onNextTurn(Client client, Player turnPlayer, JsonObject turnData, Room room, boolean isSelfTurn) {
    }

    @Override
    public void onTurnPlayed(Client client, Player turnPlayer, String playerAction, JsonObject actionData, Room room, boolean isSelf) {
    }

    @Override
    public void onOutOfTurnPlayed(Client client, Player outOfTurnPlayer, String playerAction, JsonObject actionData, Room room, boolean isSelf) {
    }

    @Override
    public void onGameAction(Client client, String gameAction, JsonObject actionData, Room room) {
    }

    @Override
    public void onGamePlayEnded(Client client, Room room, JsonObject endGameData) {
    }

    @Override
    public void onRoomDestroyed(Client client, Room room) {
    }

    @Override
    public void onAvtarChangedOfSelf(Client client, int newAvtarId) {
    }

    @Override
    public void onAvtarChangedOfOtherPlayer(Client client, Player player, int newAvtarId, Room room) {
    }

    @Override
    public void onNotEligibleToPlay(Client client, Player player, Room room, String reason) {
    }

    @Override
    public void onSessionRejoined(Client client) {
    }

    @Override
    public void onConnecting(Client client) {
    }

    @Override
    public void onConnectionSuccess(Client client) {
    }

    @Override
    public void onConnectionFailure(Client client) {
    }

    @Override
    public void onConnectionDisconnected(Client client) {
    }

    @Override
    public void onConnectionClosed(Client client) {
    }

    @Override
    public void onInvalidAction(Client client, String action, String description, JsonObject errorData, Room room, boolean isOutOfTurn) {
    }

    @Override
    public void onEnterGame(Client client, String gameName) {
    }

    @Override
    public void onExitGame(Client client, String gameName) {
    }
    
}
