/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * A WebFilterChain is an object provided by the server to the developer giving a view into the invocation chain of a filtered request for a resource. WebFilters use the WebFilterChain to invoke the next filter in the chain, or if the calling filter is the last filter in the chain, to invoke the webhandler for the resource at the end of the chain. 
 */
public class WebFilterChain {

    private final List<Object> reuestProcessors=new LinkedList();
            
    WebFilterChain() {
    }
    
    void add(WebFilter filter){
        reuestProcessors.add(filter);
    }

    void add(WebHandler webHandler){
        reuestProcessors.add(webHandler);
    }
    
    Iterator<Object> iterator=null;

    /**
     * Causes the next webFilter in the chain to be invoked, or if the calling filter is the last filter in the chain, causes the webHandler at the end of the chain to be invoked. 
     * @param webRequest - the webRequest to pass along the chain.
     * @param webResponse - the webResponse to pass along the chain.
     */
    public void doFilter(WebRequest webRequest,WebResponse webResponse){
        if(iterator==null)
            iterator=reuestProcessors.iterator();
        if(iterator.hasNext()){
            Object requestProcessor=iterator.next();
            if(requestProcessor instanceof WebFilter){
                ((WebFilter)requestProcessor).doFilter(webRequest, webResponse, this);
                return;
            }
            ((WebHandler)requestProcessor).processWebRequest(webRequest, webResponse);
        }
    }
}
