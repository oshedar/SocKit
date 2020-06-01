/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.gameclient;

import javax.json.JsonObject;
import io.sockit.clienttools.AsyncMethodExecutor;
import java.util.List;

/**
 *
 * @author h
 */
class AsyncClientListener implements ClientEventListener{
    final ClientEventListener clientListener;
    private static AsyncMethodExecutor asyncMethodExecutor=new AsyncMethodExecutor(ClientEventListener.class);
    
    public AsyncClientListener(ClientEventListener clientListener) {
        this.clientListener = clientListener;
    }

    @Override
    public void beforeServerMessageProcessed(Client client, ServerMessageType serverMessageType,String customMessageCommand, boolean isBinary,Object data) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "beforeReadProcessed", client,serverMessageType,customMessageCommand,isBinary,data);
    }
    
    
    @Override
    public void afterServerMessageProcessed(Client client, ServerMessageType serverMessageType,String customMessageCommand, boolean isBinary,Object data) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "afterReadProcessed", client,serverMessageType,customMessageCommand,isBinary,data);
    }

    @Override
    public void onError(Client client, int errorCode, String errorDesc) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onError", client,errorCode,errorDesc);
    }

    @Override
    public void onLoggedIn(Client client,boolean isGameSelected) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onLoggedIn", client,isGameSelected);
    }

    @Override
    public void onLoggedOut(Client client) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onLoggedOut", client);
    }

    @Override
    public void onSessionTimedOut(Client client) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onSessionTimedOut", client);
    }

    @Override
    public void onServerShutdown(Client client) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onServerShutdown", client);
    }

    @Override
    public void onGetLocations(Client client, String gameName, List<String> locations) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onGetLocations", client,gameName,locations);
    }

    @Override
    public void onGetRooms(Client client, String gameName, String location, RoomType roomtype, List<RoomInfo> rooms) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onGetRooms", client,gameName,location,roomtype,rooms);
    }

    @Override
    public void onRoomJoined(Client client, Room room) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onRoomJoined", client,room);
    }

    @Override
    public void onRoomRefreshedFromServer(Client client, Room room) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onRoomRefreshedFromServer", client,room);
    }

    @Override
    public void onSeatTaken(Client client, Player playerSeated, Room room,boolean isSelf) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onSeatTaken", client, playerSeated, room, isSelf);
    }

    @Override
    public void onSeatLeft(Client client, Player playerLeft, Room room, boolean isSelf) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onSeatLeft", client,playerLeft,room,isSelf);
    }

    @Override
    public void onRoomLeft(Client client) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onRoomLeft", client);
    }

    @Override
    public void onMessageReceivedJson(Client client, String command, JsonObject data) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onMessageReceived", client,command,data);
    }

    @Override
    public void onMessageReceivedString(Client client, String command, String data) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onMessageReceived", client,command,data);
    }

    @Override
    public void onMessageReceivedBytes(Client client, String command, byte[] data) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onMessageReceived", client,command,data);
    }

    @Override
    public void onGamePlayStarted(Client client, Room room) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onGameStarted", client,room);
    }

    @Override
    public void onNextTurn(Client client, Player turnPlayer, JsonObject turnData, Room room, boolean isSelfTurn) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onNextTurn", client,turnPlayer,turnData,room,isSelfTurn);
    }

    @Override
    public void onTurnPlayed(Client client, Player turnPlayer, String playerAction, JsonObject actionData, Room room, boolean isSelf) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onTurnPlayed", client,turnPlayer,playerAction,actionData,room,isSelf);
    }

    @Override
    public void onOutOfTurnPlayed(Client client, Player outOfTurnPlayer, String playerAction, JsonObject actionData, Room room, boolean isSelf) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onOutOfTurnPlayed", client,outOfTurnPlayer,playerAction,actionData,room,isSelf);
    }

    @Override
    public void onGameAction(Client client, String gameAction, JsonObject actionData, Room room) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onGameAction", client,gameAction,actionData,room);
    }

    @Override
    public void onGamePlayEnded(Client client, Room room, JsonObject endGameData) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onGameEnded", client,room,endGameData);
    }

    @Override
    public void onRoomDestroyed(Client client, Room room) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onRoomDestroyed", client,room);
    }

    @Override
    public void onAvtarChangedOfSelf(Client client, int newAvtarId) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onAvtarChangedOfSelf", client,newAvtarId);
    }

    @Override
    public void onAvtarChangedOfOtherPlayer(Client client, Player player, int newAvtarId, Room room) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onAvtarChangedOfOtherPlayer", client,player,newAvtarId,room);
    }
    
    @Override
    public void onNotEligibleToPlay(Client client,Player player,Room room,String reason){
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onNotElligibleToPlay", client,player,room,reason);        
    }
    
    @Override
    public void onSessionRejoined(Client client){
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onSessionRejoined", client);                
    }

    @Override
    public void onConnecting(Client client) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onConnecting", client);                
    }

    @Override
    public void onConnectionSuccess(Client client) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onConnectionSuccess", client);
    }

    @Override
    public void onConnectionFailure(Client client) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onConnectionFailure", client);
    }

    @Override
    public void onConnectionDisconnected(Client client) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onConnectionDisconnected", client);
    }

    @Override
    public void onConnectionClosed(Client client) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onConnectionClosed", client);                
    }

    @Override
    public void onInvalidAction(Client client, String action, String description, JsonObject errorData, Room room, boolean isOutOfTurn) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onInvalidAction", client,action,description,errorData,room,isOutOfTurn);                
    }

    @Override
    public void onEnterGame(Client client, String gameName) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onGameSelected", client,gameName);                
    }

    @Override
    public void onExitGame(Client client, String gameName) {
        if(clientListener!=null)
            asyncMethodExecutor.executeMethod(clientListener, "onGameDeselected", client,gameName);                
    }

}
