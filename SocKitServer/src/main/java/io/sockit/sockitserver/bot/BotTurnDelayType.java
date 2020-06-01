/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver.bot;

/**
 * Enumeration of the possible Bot TurnDelay Types. Indicates how much time a bot should wait before it plays an action during its turn
 */
public enum BotTurnDelayType {

    /**
     * zero delay before turn is played
     */
    none,

    /**
     * plays turn very quickly with a slight delay
     */
    fast,

    /**
     * plays turn after a normal delay
     */
    normal,

    /**
     * plays turn very slowly after a long delay
     */
    slow
}
