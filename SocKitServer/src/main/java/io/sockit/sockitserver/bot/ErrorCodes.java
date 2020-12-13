/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver.bot;

/**
 * List of Error codes sent by the server
 */
public class ErrorCodes {
    /**
     * ErrorCode sent by the server when the data sent by the Bot with the login message is not valid. For e.g. emailId or password is null
     */
    public static final int invalidLoginData=5;

    /**
     * ErrorCode sent by the server when Bot attempts to enter a game which does not exist on the server
     */
    public static final int invalidGameName=6;

    /**
     * Error code sent by the server when Bot attempts to getRooms from a location which does not exist on the server
     */
    public static final int invalidLocation=7;

    /**
     * ErrorCode sent by the server when Bot attempts to sit on a seat which is not free
     */
    public static final int seatNotFree=8;

    /**
     * ErrorCode sent by the server when Bot has not joined a room and attempts to leaveSeat or playAction or refresh room data
     */
    public static final int noRoomJoined=9;

    /**
     * ErrorCode sent by the server when Bot attempts to join or take seat in a room which does not exist on the server 
     */
    public static final int roomIdDoesNotExist=10;

    static final int serverShutdownStarted=11;

    /**
     * ErrorCode sent by the server when Bot attempts to join a room which is destroyed
     */
    public static final int roomDestroyed=12;

    /**
     * ErrorCode sent by the server when Bot attempts to sit a a seat which does not exist
     */
    public static final int invalidSeatNo=13;

    /**
     * ErrorCode sent by the server when Bot attempts to take seat and is not eligible to take seat
     */
    public static final int inElligibleToTakeSeat=14;

}
