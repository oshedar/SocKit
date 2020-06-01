/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.gameclient;

/**
 * <div class="hidejava">
 * This class defines the list of Error codes sent by the server
 * </div>
 * <div class="javascript" style="display:none;">
 * This object/function prototype defines the list of Error codes sent by the server
 * </div>
 */
public class ErrorCodes {

    /**
     * ErrorCode sent by the server when Client attempts to register a User with an emailId which is already registered on the server
     */
    public static final int duplicateEmailId=1;

    /**
     * ErrorCode sent by the server when Client attempts to register a User with an invalid emailId String
     */
    public static final int invalidEmailId=2;

    /**
     * ErrorCode sent by the server when Client attempts to login with an emailId and password that does not match
     */
    public static final int emailIdAndPasswordDoesNotMatch=4;

    /**
     * ErrorCode sent by the server when the data sent by the Client with the login message is not valid. For e.g. emailId or password is null
     */
    public static final int invalidLoginData=5;

    /**
     * ErrorCode sent by the server when Client attempts to enter a game which does not exist on the server
     */
    public static final int invalidGameName=6;

    /**
     * Error code sent by the server when Client attempts to getRooms from a location which does not exist on the server
     */
    public static final int invalidLocation=7;

    /**
     * ErrorCode sent by the server when Client attempts to sit on a seat which is not free
     */
    public static final int seatNotFree=8;

    /**
     * ErrorCode sent by the server when Client has not joined a room and attempts to leaveSeat or playAction or refresh room data
     */
    public static final int noRoomJoined=9;

    /**
     * ErrorCode sent by the server when Client attempts to join or take seat in a room which does not exist on the server 
     */
    public static final int roomIdDoesNotExist=10;

    static final int serverShutdownStarted=11;

    /**
     * ErrorCode sent by the server when Client attempts to join a room which is destroyed
     */
    public static final int roomDestroyed=12;

    /**
     * ErrorCode sent by the server when Client attempts to sit a a seat which does not exist
     */
    public static final int invalidSeatNo=13;

    /**
     * ErrorCode sent by the server when Client attempts to take seat and is not eligible to take seat
     */
    public static final int inElligibleToTakeSeat=14;

    /**
     * ErrorCode sent by the server when Client attempts to get locations or room list or join room before entering a game
     */
    public static final int noGameEntered=15;    

    /**
     * ErrorCode sent by the server when the User has exceeded the maximum number of live sessions allowed per user.
     */
    public static final int tooManySessions=16;

    /**
     * ErrorCode sent by the server when Client tries to login on a server which has exceeded the maximum number simultaneous Logins allowed
     */
    public static final int tooManyUsersLoggedIn=17;

    /**
     * ErrorCode sent by the server when Client attempts to enter a game when a room is already joined
     */
    public static final int isStillInRoom=18;

    /**
     * ErrorCode sent by the server when the loginType is not supported by the Server
     */
    public static final int invalidLoginType=19;

    /**
     * ErrorCode sent by the server when the registrationType is not supported by the Server
     */
    public static final int invalidRegistrationType=20;

    /**
     * ErrorCode sent by the server when the Client invokes registerUserWithEmailId and the emailId is null or empty
     */
    public static final int emptyEmailId=21;

    /**
     * ErrorCode sent by the server when the Client invokes registerUserWithOtherId and the otherId is null or empty
     */
    public static final int emptyOtherId=22;

    /**
     * ErrorCode sent by the server when Client attempts to register a User with an otherId which is already registered on the server
     */
    public static final int duplicateOtherId=23;

    /**
     * ErrorCode sent by the server when Client attempts to login with an otherId and password that does not match
     */
    public static final int otherIdAndPasswordDoesNotMatch=24;
}
