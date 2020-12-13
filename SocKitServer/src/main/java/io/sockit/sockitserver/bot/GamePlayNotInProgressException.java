/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver.bot;


/**
 * Exception thrown when GamePlay has not started and Bot invokes playAction() method
 */
public class GamePlayNotInProgressException extends RuntimeException{

    /**
     * Creates a GamePlayNotInProgressException
     */
    public GamePlayNotInProgressException() {
        super("Game not in progress.");
    }
    
}
