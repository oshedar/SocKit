/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

/**
 * Exception thrown by <code>registerGame()</code> method attempts to register a Game where the hash of the Game's name is equal to the hash of another Game's name 
 */
public class DuplicateGameNameHashException extends Exception {

    DuplicateGameNameHashException(String gameName) {
        super("Duplicate Game Name hash for game " + gameName);
    }

}
