/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

/**
 * Exception thrown when <code>Game.reconfigurePrivateRoom()</code> is invoked with an attempt to change the totalNoOfSeats and the seats are not empty
 */
public class SeatsNotEmptyException extends Exception {

    SeatsNotEmptyException() {
    }

}
