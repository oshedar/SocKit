/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import javax.json.JsonObject;

/**
 *
 * Interface for Session event callbacks
 */
public interface SessionListener {

    /**
     * Event-Handler/Callback called when user logins
     * @param session - the user's session
     * @param firstTime - whether its the first time that the user has logged in
     */
    void onLoggedIn(Session session,boolean firstTime);

    /**
     * Event-Handler/Callback called when user logffs
     * @param session - the user's session
     */
    void onLoggedOff(Session session);

    /**
     * Event-Handler/Callback called when session times out
     * @param session - the user's session
     */
    void onSessionTimedOut(Session session);

    /**
     * Event-Handler/Callback called when server begins shuts down but before shutdown message is sent to client
     * @param session - the user's session
     */
    void onShutDown(Session session);

    /**
     * Event-Handler/Callback called when server receives a custom json message from the client
     * @param session - the user's session
     * @param command - the message command
     * @param dataAsJson - the message data
     */
    void onMesg(Session session,String command, JsonObject dataAsJson);

    /**
     * Event-Handler/Callback called when server receives a custom text message from the client
     * @param session - the user's session
     * @param command - the message command
     * @param data - the message data
     */
    void onMesg(Session session,String command, String data);

    /**
     * Event-Handler/Callback called when server receives a custom binary message from the client
     * @param session - the user's session
     * @param command - the message command
     * @param data - the message data
     */
    void onMesg(Session session,String command, byte[] data);
}
