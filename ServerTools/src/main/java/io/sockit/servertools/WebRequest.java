/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.net.SocketAddress;
import java.util.List;

/**
 *
 * An instance of this interface represents an http request received by the web server
 */
public interface WebRequest {

    /**
     * The socket address of the web client sending the request
     * @return SocketAddress - the socket address of the web client sending the request
     */
    SocketAddress getRemoteAddress();

    /**
     * The http request headers
     * @return List&lt;HttpHeader&gt; - the http request headers
     */
    List<HttpHeader> getRequestHeaders();

    /**
     * Returns the host name in the request url. For e.g for url http://sockit.io/docs/index.html the hostname is sockit.io
     * @return String - The host name in the request url
     */
    String getHostName();

    /**
     * Returns the host port on the server to which the request arrived. It is possible that the port on which the request was made (external Host Port) is different from the actual port on which the request arrived because of load balancers or proxies
     * @return int - The host port on the server to which the request arrived
     */
    int getHostPort();

    /**
     * Returns the external host port in the request url i.e. the actual port to which the request was made. For e.g for url http://sockit.io:8443/docs/index.html the port is 8443
     * @return int - The external host port in the request url
     */
    int getExternalHostPort();

    /**
     * Returns whether request is over https. 
     * @return boolean - returns true if request is over https else false
     */
    boolean isHttps();

    /**
     * Returns whether the external request from client is over https. If there is no proxy or loadbalancer between client and server then isExtHttps() will be same as isHttps(). It is possible that external request made over https whereas the actual request received by server is over http because of load balancers or proxies
     * @return boolean - returns true if the external request is over https else false
     */
    boolean isExtHttps();

    /**
     * Returns the resource path of the web request excluding the query string. For e.g for url http://sockit.io/docs/index.html?user=hoshi the resourcePath will be /docs/index.html
     * @return String - The resource path of the web request excluding the query string. For e.g for url http://sockit.io/docs/index.html?user=hoshi the resourcePath will be /docs/index.html
     */
    String getResourcePath(); 

    /**
     * Returns the queryString portion of the web request
     * @return String - the queryString portion of the web request
     */
    String getQueryString();

    /**
     * Returns the header value of the specified header in this WebRequest object
     * @return String - the header value of the specified header
     */
    String getHeaderValue(String headerName);
}
