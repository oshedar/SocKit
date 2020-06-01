/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.pokergame;

/**
 *
 * @author h
 */
public class InvalidBetException extends Exception{
    int minBet;
    int actualBet;

    public InvalidBetException(int minBet, int actualBet) {
        this.minBet = minBet;
        this.actualBet = actualBet;
    }
    
}
