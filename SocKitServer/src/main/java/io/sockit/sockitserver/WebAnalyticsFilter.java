/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import io.sockit.servertools.MimeTypes;
import io.sockit.servertools.WebFilter;
import io.sockit.servertools.WebFilterChain;
import io.sockit.servertools.WebRequest;
import io.sockit.servertools.WebResponse;
import java.io.Writer;
import java.util.Iterator;

/**
 *
 * @author oshedar
 */
public class WebAnalyticsFilter implements WebFilter{

    @Override
    public void doFilter(WebRequest webRequest, WebResponse webResponse, WebFilterChain filterChain) {
        String path=webRequest.getResourcePath();
        if(path.equals("/webanalytics.db")){
            Iterator<String> iterator=GameDB.getWaRecords();
            Writer writer=webResponse.getWriter();
            String line;
            try{
                while(iterator.hasNext()){
                    line=iterator.next();
                    if(line!=null && line.length()>0)
                        writer.append(line).append('\n');
                }
            }catch(Exception ex){
                throw new RuntimeException(ex);
            }finally{
                try{writer.close();}catch(Exception ex){throw new RuntimeException(ex);}
            }
            webResponse.setStatus(200);
            webResponse.setMimeType(MimeTypes.getMimeType(".txt"));
            return;
        }
        filterChain.doFilter(webRequest, webResponse);
        if(webResponse.getStatus()==200){
            if(path.equals("/") || path.endsWith(".htm") || path.endsWith(".html") || path.endsWith(".zip")){
                GameDB.writeWa(webRequest);
            }
        }        
    }
    
}
