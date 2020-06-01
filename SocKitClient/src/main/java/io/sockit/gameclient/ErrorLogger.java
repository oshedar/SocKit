/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.gameclient;

/**
 * Interface for logging errors
 */
public interface ErrorLogger {

    /**
     * Callback called when an Exception/error occurs to log errors
     * @param error - the error to log
     */
    void logError(Exception error);

}
