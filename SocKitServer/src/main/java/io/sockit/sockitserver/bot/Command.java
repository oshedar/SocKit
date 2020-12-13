/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver.bot;

/**
 *
 * @author Hoshedar Irani
 */
enum Command {
    //in commands
    logout,
    poll,
    getLocations,
    getRooms,
    joinRoom,
    getRoomData,
    takeSeat,
    leaveSeat,
    leaveRoom,
    playAction,
    changeAvtar,
    register,
    login,
    rejoinSession,
    selectGame,
    deselectGame,

    //out commands
    roomData,
    sessionTimedOut,
    sessionRejoined,
    roomJoined,
    seatTaken,
    notElligibeToPlay,
    newGame,
    nextTurn,
    gameAction,
    invalidAction,
    outOfTurnPlayed,
    turnPlayed,
    endGame,
    seatLeft,
    roomLeft,
    roomDestroyed,
    loggedOut,
    locations,
    rooms,
    avtarChanged,
    shutDown,
    error,
    loggedIn,
    userData,
    gameSelected,
    gameDeselected,
    invalidCommand;
    
    static Command toEnum(String command){
        switch(command.hashCode()){
            case 1211350614: return logout;
            case 1108868609: return poll;
            case 729406776: return getLocations;
            case -1812308386: return getRooms;
            case 1205726939: return joinRoom;
            case 1513306763: return getRoomData;
            case -1095499340: return takeSeat;
            case 1489747626: return leaveSeat;
            case 1489135252: return leaveRoom;
            case -1515671850: return playAction;
            case 2010086982: return changeAvtar;
            case 1790188829: return register;
            case -99477571: return login;
            case -179427283: return rejoinSession;
            case 415655634: return selectGame;
            case 554968177: return deselectGame;
            case 619076827: return roomData;
            case -1824865639: return sessionTimedOut;
            case 1038042260: return sessionRejoined;
            case -646465380: return roomJoined;
            case -2134438140: return seatTaken;
            case 55469179: return notElligibeToPlay;
            case -1776760364: return newGame;
            case -1375900848: return nextTurn;
            case -1072415656: return gameAction;
            case -255221351: return invalidAction;
            case 1511638929: return outOfTurnPlayed;
            case 428039280: return turnPlayed;
            case -1428539527: return endGame;
            case -768862924: return seatLeft;
            case 626571294: return roomLeft;
            case -774702264: return roomDestroyed;
            case 2049409946: return loggedOut;
            case -1221917464: return locations;
            case 72539662: return rooms;
            case 872670236: return avtarChanged;
            case 727731114: return shutDown;
            case -296777474: return error;
            case -72443399: return loggedIn;
            case 2025998315: return userData;
            case -1713498669: return gameSelected;
            case -1315701838: return gameDeselected;
        }
        return invalidCommand;
    }    
}
