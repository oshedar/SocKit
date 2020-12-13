/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver.bot;

import java.util.List;
import javax.json.JsonObject;

/**
 * {@inheritDoc}
 * An abstract adapter class for receiving bot events. The methods in this class are empty.
 * This class exists as convenience for creating BotEventListener objects.
 */
public abstract class BotEventAdapter implements BotEventListener{

    @Override
    public void onError(Bot bot, int errorCode, String errorDesc) {
        
    }

    @Override
    public void onLoggedIn(Bot bot) {
        
    }

    @Override
    public void onLoggedOut(Bot bot) {
        
    }

    @Override
    public void onSessionTimedOut(Bot bot) {
        
    }

    @Override
    public void onServerShutdown(Bot bot) {
        
    }

    @Override
    public void onGetLocations(Bot bot, String gameName, List<String> locations) {
        
    }

    @Override
    public void onGetRooms(Bot bot, String gameName, String location, RoomType roomtype, List<RoomInfo> rooms) {
        
    }

    @Override
    public void onRoomJoined(Bot bot, Room room) {
        
    }

    @Override
    public void onRoomRefreshedFromServer(Bot bot, Room room) {
        
    }

    @Override
    public void onSeatTaken(Bot bot, Player playerSeated, Room room, boolean isSelf) {
        
    }

    @Override
    public void onSeatLeft(Bot bot, Player playerLeft, Room room, boolean isSelf) {
        
    }

    @Override
    public void onRoomLeft(Bot bot) {
        
    }

    @Override
    public void onMessageReceivedJson(Bot bot, String command, JsonObject data) {
        
    }

    @Override
    public void onMessageReceivedString(Bot bot, String command, String data) {
        
    }

    @Override
    public void onMessageReceivedBytes(Bot bot, String command, byte[] data) {
        
    }

    @Override
    public void onGamePlayStarted(Bot bot, Room room) {
        
    }

    @Override
    public void onNextTurn(Bot bot, Player turnPlayer, JsonObject turnData, Room room, boolean isSelfTurn) {
        
    }

    @Override
    public void onTurnPlayed(Bot bot, Player turnPlayer, String playerAction, JsonObject actionData, Room room, boolean isSelf) {
        
    }

    @Override
    public void onOutOfTurnPlayed(Bot bot, Player outOfTurnPlayer, String playerAction, JsonObject actionData, Room room, boolean isSelf) {
        
    }

    @Override
    public void onGameAction(Bot bot, String gameAction, JsonObject actionData, Room room) {
        
    }

    @Override
    public void onGamePlayEnded(Bot bot, Room room, JsonObject endGameData) {
        
    }

    @Override
    public void onRoomDestroyed(Bot bot, Room room) {
        
    }

    @Override
    public void onAvtarChangedOfSelf(Bot bot, int newAvtarId) {
        
    }

    @Override
    public void onAvtarChangedOfOtherPlayer(Bot bot, Player player, int newAvtarId, Room room) {
        
    }

    @Override
    public void onNotEligibleToPlay(Bot bot, Player player, Room room, String reason) {
        
    }

    @Override
    public void onInvalidAction(Bot bot, String action, String description, JsonObject errorData, Room room, boolean isOutOfTurn) {
        
    }
    
}
