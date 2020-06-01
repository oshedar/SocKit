/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.gameclient;


/**
 *
 * Enumeration of the possible Server message types
 */
public enum ServerMessageType {

    /**
     * Message received when room state (data) is updated
     */
    roomData,

    /**
     * Message received when session times out
     */
    sessionTimedOut,

    /**
     * Message received when session is rejoined
     */
    sessionRejoined,

    /**
     * Message received when room is joined by a client
     */
    roomJoined,

    /**
     * Message received when a seat is taken
     */
    seatTaken,

    /**
     * Message received when player is not eligible to take part in the game play
     */
    notEligibeToPlay,

    /**
     * Message received when new game play starts
     */
    gamePlayStarted,

    /**
     * Message received when new turn begins
     */
    nextTurn,

    /**
     * Message received when there is a non player action in the game. For e.g. in poker this coupld be flop card dealt
     */
    gameAction,

    /**
     * Message received when a client plays an invalid action
     */
    invalidAction,

    /**
     * Message received when an action is played out of turn by the Client
     */
    outOfTurnPlayed,

    /**
     * Message received when a player plays his turn
     */
    turnPlayed,

    /**
     * Message received when Game Play ends
     */
    gamePlayEnded,

    /**
     * Message received when a player leaves a seat
     */
    seatLeft,

    /**
     * Message received when Client leaves the room
     */
    roomLeft,

    /**
     * Message received when room joined by the Client is destroyed
     */
    roomDestroyed,

    /**
     * Message received when Client logs out
     */
    loggedOut,

    /**
     * Message received when Server sends list of locations to the Client
     */
    locations,

    /**
     * Message received when server sends list of roomList to the Client
     */
    roomList,

    /**
     * Message received when when a player's avatar changes
     */
    avtarChanged,

    /**
     * Message received when Server shuts down
     */
    serverShutDown,

    /**
     * Message received when there is an error on the server
     */
    error,

    /**
     * Message received when the client logins to the server
     */
    loggedIn,

    /**
     * Message received when user data is updated
     */
    userData,

    /**
     * Message received when the Client enters a game
     */
    gameEntered,

    /**
     * Message received when the Client exists a game
     */
    gameExited,

    /**
     * Message received a custom message is sent by the server
     */
    customMessage;
    
    static ServerMessageType toEnum(String command){
        switch(command.hashCode()){
            case 619076827: return roomData;
            case -1824865639: return sessionTimedOut;
            case 1038042260: return sessionRejoined;
            case -646465380: return roomJoined;
            case -2134438140: return seatTaken;
            case 55469179: return notEligibeToPlay;
            case -1776760364: return gamePlayStarted;
            case -1375900848: return nextTurn;
            case -1072415656: return gameAction;
            case -255221351: return invalidAction;
            case 1511638929: return outOfTurnPlayed;
            case 428039280: return turnPlayed;
            case -1428539527: return gamePlayEnded;
            case -768862924: return seatLeft;
            case 626571294: return roomLeft;
            case -774702264: return roomDestroyed;
            case 2049409946: return loggedOut;
            case -1221917464: return locations;
            case 72539662: return roomList;
            case 872670236: return avtarChanged;
            case 727731114: return serverShutDown;
            case -296777474: return error;
            case -72443399: return loggedIn;
            case 2025998315: return userData;
            case -1713498669: return gameEntered;
            case -1315701838: return gameExited;
        }
        return customMessage;        
    }
    
    static String getCustomCommand(String command){
        if(command.startsWith("#"))
            return null;
        return command.substring(2, command.length()-1);
    }
    
}
