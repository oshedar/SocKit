/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

/**
 * Exception thrown when <code>Game.reconfigurePrivateRoom()</code> is invoked while gamePlay is in progress
 */
public class GamePlayInProgressException extends Exception {

    GamePlayInProgressException() {
    }

}
