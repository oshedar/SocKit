/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import javax.json.JsonObject;

/**
 *
 * 
 */
class SessionListenerAdapter implements SessionListener{

    @Override
    public void onLoggedIn(Session session, boolean firstTime) {
    }

    @Override
    public void onLoggedOff(Session session) {
    }

    @Override
    public void onSessionTimedOut(Session session) {
    }

    @Override
    public void onShutDown(Session session) {
    }

    @Override
    public void onMesg(Session session, String command, JsonObject dataAsJson) {
    }

    @Override
    public void onMesg(Session session, String command, String data) {
    }

    @Override
    public void onMesg(Session session, String command, byte[] data) {
    }

}
