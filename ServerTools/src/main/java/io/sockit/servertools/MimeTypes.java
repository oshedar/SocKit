/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.util.HashMap;
import java.util.Map;

/**
 *  Maps file extensions to a mime type
 */
public class MimeTypes {
   private static Map<String, String> mimeTypes=null;

    /**
     *
     * @param fileExtension - the file extension (including the dot - example '.txt') for which mimeType should be returned. 
     * @return String - the mime type of specified file extension
     */
    public final static String getMimeType(String fileExtension){
       if(mimeTypes==null)
           createMimeTypesMap();
       return fileExtension!=null?mimeTypes.get(fileExtension):null;
   }
   
   private static void createMimeTypesMap(){
       String[] fileExtensions={".aac",".abw",".arc",".avi",".azw",".bin",".bmp",".bz",".bz2",".csh",".css",".csv",".doc",".docx",".eot",".epub",".gif",".htm",".html",".ico",".ics",".jar",".jpeg",".jpg",".js",".json",".jsonld",".mid",".midi",".mjs",".mp3",".mpeg",".mpkg",".odp",".ods",".odt",".oga",".ogv",".ogx",".otf",".png",".pdf",".ppt",".pptx",".rar",".rtf",".sh",".svg",".swf",".tar",".tif",".tiff",".ttf",".txt",".vsd",".wav",".weba",".webm",".webp",".woff",".woff2",".xhtml",".xls",".xlsx",".xml",".xul",".zip",".3gp",".3g2",".7z"};
       String[] mimeTypes={"audio/aac","application/x-abiword","application/x-freearc","video/x-msvideo","application/vnd.amazon.ebook","application/octet-stream","image/bmp","application/x-bzip","application/x-bzip2","application/x-csh","text/css","text/csv","application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document","application/vnd.ms-fontobject","application/epub+zip","image/gif","text/html","text/html","image/vnd.microsoft.icon","text/calendar","application/java-archive","image/jpeg","image/jpeg","text/javascript","application/json","application/ld+json","audio/midi audio/x-midi","audio/midi audio/x-midi","text/javascript","audio/mpeg","video/mpeg","application/vnd.apple.installer+xml","application/vnd.oasis.opendocument.presentation","application/vnd.oasis.opendocument.spreadsheet","application/vnd.oasis.opendocument.text","audio/ogg","video/ogg","application/ogg","font/otf","image/png","application/pdf","application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation","application/x-rar-compressed","application/rtf","application/x-sh","image/svg+xml","application/x-shockwave-flash","application/x-tar","image/tiff","image/tiff","font/ttf","text/plain","application/vnd.visio","audio/wav","audio/webm","video/webm","image/webp","font/woff","font/woff2","application/xhtml+xml","application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","application/xml","application/vnd.mozilla.xul+xml","application/zip","video/3gpp","video/3gpp2","application/x-7z-compressed"};
       Map<String, String> map=new HashMap((int)(fileExtensions.length*1.25));
       for(int ctr=0;ctr<fileExtensions.length;ctr++)
           map.put(fileExtensions[ctr], mimeTypes[ctr]);
       MimeTypes.mimeTypes=map;
   }

    
}
