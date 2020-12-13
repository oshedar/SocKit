/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

/**
 *
 * @author h
 */
class ErrorCodes {
    static final int duplicateEmailId=1;
    static final int invalidEmailId=2;
    static final int emailIdAndPasswordDoesNotMatch=4;
    static final int invalidLoginData=5;
    static final int invalidGameName=6;
    static final int invalidLocation=7;
    static final int seatNotFree=8;
    static final int noRoomJoined=9;
    static final int roomIdDoesNotExist=10;
    static final int serverShutdownStarted=11;
    static final int roomDestroyed=12;
    static final int invalidSeatNo=13;
    static final int inElligibleToTakeSeat=14;
    static final int gameNotSelected=15;
    static final int tooManySessions=16;
    static final int tooManyUsersLoggedIn=17;
    static final int isStillInRoom=18;
    static final int invalidLoginType=19;
    static final int invalidRegistrationType=20;
    static final int emptyEmailId=21;
    static final int emptyOtherId=22;
    static final int duplicateOtherId=23;
    static final int otherIdAndPasswordDoesNotMatch=24;
}
