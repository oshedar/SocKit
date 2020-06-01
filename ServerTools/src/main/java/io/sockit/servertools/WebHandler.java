/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

/**
 * This interface allows you to implement your own web handler to handle web Requests on the websocket server. A web handler can be registered on the Server by calling WebSocketServer.addWebHandler() method.
 */
public interface WebHandler {
    /**
     * Returns the Web Resource for a specified web path.The web path is the path following the domain name in a web url. 
     * @param remoteAddress - the socket address of the http client which made the request
     * @param requestHeaders - the http request headers which are part of the http request
     * @param resourcePath - the resource path of the web request excluding the query string. For e.g for url http://sockit.io/docs/index.html?user=hoshi the resourcePath will be /docs/index.html
     * @param queryString - the querystring of the web request
     */
    public void processWebRequest(WebRequest webRequest,WebResponse webResponse);
}
