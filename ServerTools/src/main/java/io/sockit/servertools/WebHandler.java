/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

/**
 * This interface allows you to implement your own web handler to handle web Requests on the websocket server. A web handler can be registered on the Server by calling WebSocketServer.addWebHandler() method.
 */
public interface WebHandler {

    /**
     * 
     * @param webRequest
     * @param webResponse
     */
    public void processWebRequest(WebRequest webRequest,WebResponse webResponse);
}
