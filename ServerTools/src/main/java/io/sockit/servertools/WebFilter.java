/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

/**
 * A webFilter is an object that performs filtering tasks on either the request to a web resource , or on the response from a web resource, or both.
 */
public interface WebFilter {

    /**
     * The doFilter method of the WebFilter is called by the server each time a request/response pair is passed through the chain due to a web client request for a resource at the end of the chain. The WebFilterChain passed in to this method allows the Filter to pass on the request and response to the next entity in the chain. 
     * @param webRequest - the webRequest passed along the chain to this filter.
     * @param webResponse - the webResponse passed along the chain to this filter.
     * @param filterChain - The WebFilterChain used to pass on the request and response to the next filter in the chain or if its the last filter then the webHandler
     */
    void doFilter(WebRequest webRequest,WebResponse webResponse,WebFilterChain filterChain);
}
