/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

/**
 *
 * @author Hoshedar Irani
 */
class ErrorDescriptions {
    static final String duplicateEmailId = "duplicate emailId";
    static final String invalidEmailId = "invalid emailId";
    static final String duplicateLogin = "duplicate login";
    static final String emailIdAndPasswordDoesNotMatch = "emailId and password does not match";
    static final String invalidLoginData = "invalid login data";
    static final String invalidGameName = "invalid game name";
    static final String invalidLocation = "invalid location";
    static final String seatNotFree = "Seat not free";
    static final String noRoomJoined = "no room joined";
    static final String roomIdDoesNotExist = "roomId does not exist";
    static final String shutdownStarted = "shutdown started";
    static final String roomDestroyed = "room destroyed";
    static final String invalidSeatNo = "invalid seat number";
    static final String inElligibleToTakeSeat = "inelligible to take seat";
    static final String gameNotSelected = "game not selected";
    static final String tooManySessions = "too many sessions";
    static final String tooManyUsersLoggedIn = "too many users logged in. This edition of SocKit server limits the number of concurrent users";
    static final String isStillInRoom = "Still in Room. Cannot select or deselect game while client still in room";
    static final String invalidLoginType = "invalid login type";
    static final String invalidRegistrationType = "invalid registration type";
    static final String emptyEmailId = "EmailId is empty";
    static final String emptyOtherId = "OtherId is empty";
    static final String duplicateOtherId = "Duplicate otherId";
    static final String otherIdAndPasswordDoesNotMatch = "otherId and password does not match";
}
