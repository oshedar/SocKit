/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver.bot;

import javax.json.JsonObject;

/**
 * This class represents the key info for each room in the room list
 */
public class RoomInfo {

    /**
     * the roomId of the room
     */
    public final long roomId;

    /**
     * The name of the room
     */
    public final String roomName;

    /**
     * The total number of seats in the room
     */
    public final int totalNoOfSeats;

    /**
     * The total number of players in the room
     */
    public final int noOfPlayers;

    /**
     * The room data sent with the roomList
     */
    public final JsonObject data;

    RoomInfo(long roomId,String roomName,int totalNoOfSeats, int noOfPlayers, JsonObject data) {
        this.roomId=roomId;
        this.roomName = roomName;
        this.totalNoOfSeats=totalNoOfSeats;
        this.noOfPlayers = noOfPlayers;
        this.data = data;
    }
        
}
