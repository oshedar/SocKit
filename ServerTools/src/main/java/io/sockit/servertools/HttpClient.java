/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.json.JsonObject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author 
 */
public final class HttpClient {
    private static final String COOKIE_RESPONSE_HEADER = "Set-Cookie";
    private static final String COOKIE_REQUEST_HEADER = "Cookie";
    private static int defaultConnectTimeOutInMillis=3000;
    private static int defaultReadTimeOutInMillis=8000;
    private static Proxy proxy;
    
    public static void setProxy(String ip,int port){
        proxy=new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip,port));
    }
    
    public static void removeProxy(){
        HttpClient.proxy=null;
    }
    
    
    public static void setDefaultTimeout(int connectTimeOutInMillis,int readTimeOutInMillis){
        defaultConnectTimeOutInMillis=connectTimeOutInMillis;
        defaultReadTimeOutInMillis=readTimeOutInMillis;
    }
    
    public static HttpClientResponse doPost(URL url, String postData,List<HttpHeader> headers) throws IOException{
        return doPost(url, postData, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,HttpClientResponseType.string,headers);
    }
    
    public static HttpClientResponse doPost(URL url, String postData,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doPost(url, postData, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,HttpClientResponseType.string,headers,acceptSelfSignedCertificate);
    }
    
    public static HttpClientResponse doPost(URL url, JsonObject postData,List<HttpHeader> headers) throws IOException{
        return doPost(url, postData,HttpClientResponseType.string,headers);
    }

    public static HttpClientResponse doPost(URL url, JsonObject postData,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doPost(url, postData, HttpClientResponseType.string,headers,acceptSelfSignedCertificate);
    }

    public static HttpClientResponse doPost(URL url, JsonObject postData,HttpClientResponseType responseType,List<HttpHeader> headers) throws IOException{
        return doPost(url, postData, responseType, headers, false);
    }
    
    public static HttpClientResponse doPost(URL url, JsonObject postData,HttpClientResponseType responseType,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        if(headers==null)
            headers=new ArrayList<HttpHeader>(2);
        byte[] requestBody=postData.toString().getBytes(Utils.utf8Charset);
        headers.add(new HttpHeader("Content-Type", "application/json"));
        return doPost(url, requestBody, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,responseType,headers,acceptSelfSignedCertificate);
    }

    public static HttpClientResponse doPost(URL url, String postData,HttpClientResponseType responseType,List<HttpHeader> headers) throws IOException{
        return doPost(url, postData, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,responseType,headers);
    }
    
    public static HttpClientResponse doPost(URL url, String postData,HttpClientResponseType responseType,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doPost(url, postData, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,responseType,headers,acceptSelfSignedCertificate);
    }
    
    public static HttpClientResponse doPost(URL url, String postData,int connectTimeOutInMillis,int readTimeOutInMillis,HttpClientResponseType responseType,List<HttpHeader> headers) throws IOException{
        return doPost(url, postData, connectTimeOutInMillis, readTimeOutInMillis, responseType, headers, false);
    }
    
    public static HttpClientResponse doPost(URL url, String postData,int connectTimeOutInMillis,int readTimeOutInMillis,HttpClientResponseType responseType,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        HttpURLConnection httpConn = (HttpURLConnection)(proxy==null?url.openConnection():url.openConnection(proxy));
        if(acceptSelfSignedCertificate && httpConn instanceof HttpsURLConnection)
            ((HttpsURLConnection)httpConn).setSSLSocketFactory(getTrustSelfSignedSocketFactory());
        OutputStream os=null;
        InputStream is=null;
        try {
            httpConn.setConnectTimeout(connectTimeOutInMillis);
            httpConn.setInstanceFollowRedirects(false);
            httpConn.setReadTimeout(readTimeOutInMillis);
            httpConn.setDoInput(true);
            httpConn.setRequestMethod("POST");
            httpConn.setUseCaches(false);
            if(headers!=null){
                for(HttpHeader header:headers){
                    if(header!=null)
                        httpConn.setRequestProperty(header.name, header.value);
                }
            }
            if(postData!=null && postData.length()>0){
                httpConn.setDoOutput(true);
                os = httpConn.getOutputStream();
                os.write(postData.getBytes(Utils.utf8Charset));
                os.close();
                os=null;
            }
            else
                httpConn.setDoOutput(false);
            int responseCode = httpConn.getResponseCode();
            if (responseCode < 400)
                is = httpConn.getInputStream();
            else {
                is = httpConn.getErrorStream();
                return new HttpClientResponse(responseCode, read(is),httpConn.getHeaderFields());
            }
            switch (responseType){
                case byteArray:
                    return new HttpClientResponse(responseCode, readBytes(is),httpConn.getHeaderFields());
                case string:
                    return new HttpClientResponse(responseCode, read(is),httpConn.getHeaderFields());
                case inputStream: {
                    HttpClientResponse httpResponse=new HttpClientResponse(responseCode, is,httpConn.getHeaderFields());
                    is=null;
                    return httpResponse;
                }
            }
            return new HttpClientResponse(0, (String) null,httpConn.getHeaderFields());
        }
        finally{
            if(os!=null)
                try{os.close();} catch(Exception ex){}
            if(is!=null)
                try{is.close();} catch(Exception ex){}

        }
    }
    
    public static HttpClientResponse doPost(URL url, byte[] postData,int connectTimeOutInMillis,int readTimeOutInMillis,HttpClientResponseType responseType,List<HttpHeader> headers) throws IOException{
        return doPost(url, postData, connectTimeOutInMillis, readTimeOutInMillis, responseType, headers, false);
    }
    
    public static HttpClientResponse doPost(URL url, byte[] postData,int connectTimeOutInMillis,int readTimeOutInMillis,HttpClientResponseType responseType,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        HttpURLConnection httpConn = (HttpURLConnection)(proxy==null?url.openConnection():url.openConnection(proxy));
        if(acceptSelfSignedCertificate && httpConn instanceof HttpsURLConnection)
            ((HttpsURLConnection)httpConn).setSSLSocketFactory(getTrustSelfSignedSocketFactory());
        OutputStream os=null;
        InputStream is=null;
        if(headers==null)
            headers=new ArrayList(1);
        headers.add(new HttpHeader("Content-Length", String.valueOf(postData.length)));
        try {
            httpConn.setConnectTimeout(connectTimeOutInMillis);
            httpConn.setReadTimeout(readTimeOutInMillis);
            httpConn.setInstanceFollowRedirects(false);
            httpConn.setDoInput(true);
            httpConn.setRequestMethod("POST");
            httpConn.setUseCaches(false);
            if(headers!=null){
                for(HttpHeader header:headers){
                    if(header!=null)
                        httpConn.setRequestProperty(header.name, header.value);
                }
            }
            if(postData!=null && postData.length>0){
                httpConn.setDoOutput(true);
                os = httpConn.getOutputStream();
                os.write(postData);
                os.close();
                os=null;
            }
            else
                httpConn.setDoOutput(false);
            int responseCode = httpConn.getResponseCode();
            if (responseCode < 400)
                is = httpConn.getInputStream();
            else {
                is = httpConn.getErrorStream();
                return new HttpClientResponse(responseCode, read(is),httpConn.getHeaderFields());
            }
            switch (responseType){
                case byteArray:
                    return new HttpClientResponse(responseCode, readBytes(is),httpConn.getHeaderFields());
                case string:
                    return new HttpClientResponse(responseCode, read(is),httpConn.getHeaderFields());
                case inputStream: {
                    HttpClientResponse httpResponse=new HttpClientResponse(responseCode, is,httpConn.getHeaderFields());
                    is=null;
                    return httpResponse;
                }
            }
            return new HttpClientResponse(0, (String) null,httpConn.getHeaderFields());
        }
        finally{
            if(os!=null)
                try{os.close();} catch(Exception ex){}
            if(is!=null)
                try{is.close();} catch(Exception ex){}

        }
    }
    
    public static HttpClientResponse doPost(URL url, byte[] postData,List<HttpHeader> headers) throws IOException{
        return doPost(url, postData, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,HttpClientResponseType.string,headers);
    }
    
    public static HttpClientResponse doPost(URL url, byte[] postData,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doPost(url, postData, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,HttpClientResponseType.string,headers,acceptSelfSignedCertificate);
    }
    
    public static HttpClientResponse doPost(URL url, byte[] postData,HttpClientResponseType responseType,List<HttpHeader> headers) throws IOException{
        return doPost(url, postData, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,responseType,headers);
    }
    
    public static HttpClientResponse doPost(URL url, byte[] postData,HttpClientResponseType responseType,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doPost(url, postData, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,responseType,headers,acceptSelfSignedCertificate);
    }
    
    public static HttpClientResponse doPost(URL url,List<HttpParam> params,List<HttpHeader> headers) throws IOException{
        return doPost(url, params, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,HttpClientResponseType.string,headers);
    }
    
    public static HttpClientResponse doPost(URL url,List<HttpParam> params,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doPost(url, params, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,HttpClientResponseType.string,headers,acceptSelfSignedCertificate);
    }
    
    public static HttpClientResponse doPost(URL url,List<HttpParam> params,HttpClientResponseType responseType, List<HttpHeader> headers) throws IOException{
        return doPost(url, params, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,responseType,headers);
    }
    
    public static HttpClientResponse doPost(URL url,List<HttpParam> params,HttpClientResponseType responseType, List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doPost(url, params, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,responseType,headers,acceptSelfSignedCertificate);
    }
    
    public static HttpClientResponse doPost(URL url,List<HttpParam> params,int connectTimeOutInMillis,int readTimeOutInMillis,HttpClientResponseType responseType, List<HttpHeader> headers) throws IOException{
        return doPost(url, createParamString(params), connectTimeOutInMillis, readTimeOutInMillis,responseType,headers,false);
    }
    
    public static HttpClientResponse doPost(URL url,List<HttpParam> params,int connectTimeOutInMillis,int readTimeOutInMillis,HttpClientResponseType responseType, List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doPost(url, createParamString(params), connectTimeOutInMillis, readTimeOutInMillis,responseType,headers,acceptSelfSignedCertificate);
    }
    
    private static String read(InputStream is) throws IOException{
        if(is==null)
            return null;
        byte[] bytes=readBytes(is);
        if(bytes==null || bytes.length<1)
            return null;
        return new String(bytes,Utils.utf8Charset);
    }

    private static byte[] readBytes(InputStream is) throws IOException{
        if(is==null)
            return null;
        FastGrowingOutputStream os=new FastGrowingOutputStream(64);
        byte[] bytes=new byte[64];
        int bytesRead;
        while (true){
            bytesRead = is.read(bytes);

            if(bytesRead==-1)
                break;
            os.write(bytes,0,bytesRead);
        }
        return os.toByteArray();
    }

    private static String createParamString(List<HttpParam> params) throws UnsupportedEncodingException {
        int paramCount=0;
        if(params!=null)
            paramCount=params.size();
        if(paramCount<1)
            return null;
        StringBuilder sb=new StringBuilder();
        HttpParam param;
        if(paramCount>0){
            param=params.get(0);
            sb.append(param.paramName).append('=').append(URLEncoder.encode(param.paramvalue==null?"":param.paramvalue, "UTF-8"));
        }
        for(int ctr=1;ctr<paramCount;ctr++){
            param=params.get(ctr);
            sb.append('&').append(param.paramName).append('=').append(URLEncoder.encode(param.paramvalue==null?"":param.paramvalue, "UTF-8"));            
        }
        return sb.toString();
    }
    
    public static HttpClientResponse doGet(URL url,List<HttpHeader> headers) throws IOException{
        return doGet(url, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,HttpClientResponseType.string,headers);
    }
    
    public static HttpClientResponse doGet(URL url,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doGet(url, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,HttpClientResponseType.string,headers,acceptSelfSignedCertificate);
    }
    
    public static HttpClientResponse doGet(URL url,HttpClientResponseType responseType,List<HttpHeader> headers) throws IOException{
        return doGet(url, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,responseType,headers);
    }
    
    public static HttpClientResponse doGet(URL url,HttpClientResponseType responseType,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doGet(url, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,responseType,headers,acceptSelfSignedCertificate);
    }
    
    public static HttpClientResponse doGet(URL url,int connectTimeOutInMillis,int readTimeOutInMillis,List<HttpHeader> headers) throws IOException{
        return doGet(url, connectTimeOutInMillis, readTimeOutInMillis, HttpClientResponseType.string,headers);
    }
    
    public static HttpClientResponse doGet(URL url,int connectTimeOutInMillis,int readTimeOutInMillis,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doGet(url, connectTimeOutInMillis, readTimeOutInMillis, HttpClientResponseType.string,headers,acceptSelfSignedCertificate);
    }
    
    public static HttpClientResponse doGet(URL url,int connectTimeOutInMillis,int readTimeOutInMillis,HttpClientResponseType responseType,List<HttpHeader> headers) throws IOException{
        return doGet(url, connectTimeOutInMillis, readTimeOutInMillis, responseType, headers, false);
    }
    
    public static HttpClientResponse doGet(URL url,int connectTimeOutInMillis,int readTimeOutInMillis,HttpClientResponseType responseType,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        HttpURLConnection httpConn = (HttpURLConnection)(proxy==null?url.openConnection():url.openConnection(proxy));
        if(acceptSelfSignedCertificate && httpConn instanceof HttpsURLConnection)
            ((HttpsURLConnection)httpConn).setSSLSocketFactory(getTrustSelfSignedSocketFactory());
        InputStream is=null;
        try {
            httpConn.setConnectTimeout(connectTimeOutInMillis);
            httpConn.setReadTimeout(readTimeOutInMillis);
            httpConn.setInstanceFollowRedirects(false);
            httpConn.setDoOutput(false);
            httpConn.setDoInput(true);
            httpConn.setUseCaches(false);
            if(headers!=null){
                for(HttpHeader header:headers){
                    if(header!=null)
                        httpConn.setRequestProperty(header.name, header.value);
                }
            }
            httpConn.setRequestMethod("GET");
            int responseCode = httpConn.getResponseCode();
            if (responseCode < 400)
                is = httpConn.getInputStream();
            else {
                is = httpConn.getErrorStream();
                return new HttpClientResponse(responseCode, read(is),httpConn.getHeaderFields());
            }
            switch (responseType){
                case byteArray:
                    return new HttpClientResponse(responseCode, readBytes(is),httpConn.getHeaderFields());
                case string:
                    return new HttpClientResponse(responseCode, read(is),httpConn.getHeaderFields());
                case inputStream: {
                    HttpClientResponse httpResponse=new HttpClientResponse(responseCode, is,httpConn.getHeaderFields());
                    is=null;
                    return httpResponse;
                }
            }
            return new HttpClientResponse(0,(String)null,httpConn.getHeaderFields());
        }
        finally{
            if(is!=null)
                try{is.close();} catch(Exception ex){}

        }
    }
    
    public static HttpClientResponse doGet(String url,List<HttpHeader> headers) throws IOException{
        return doGet(url, null, headers);
    }
    
    public static HttpClientResponse doGet(String url,List<HttpParam> params,List<HttpHeader> headers) throws IOException{
        return doGet(url, params, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,HttpClientResponseType.string,headers);
    }
    
    public static HttpClientResponse doGet(String url,List<HttpParam> params,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doGet(url, params, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,HttpClientResponseType.string,headers,acceptSelfSignedCertificate);
    }
    
    public static HttpClientResponse doGet(String url,List<HttpParam> params,HttpClientResponseType responseType,List<HttpHeader> headers) throws IOException{
        return doGet(url, params, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,responseType,headers);
    }
    
    public static HttpClientResponse doGet(String url,List<HttpParam> params,HttpClientResponseType responseType,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doGet(url, params, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,responseType,headers,acceptSelfSignedCertificate);
    }
    
    public static HttpClientResponse doGet(String url,List<HttpParam> params,int connectTimeOutInMillis,int readTimeOutInMillis,List<HttpHeader> headers) throws IOException{
        return doGet(url, params, connectTimeOutInMillis, readTimeOutInMillis, HttpClientResponseType.string,headers);
    }
    
    public static HttpClientResponse doGet(String url,List<HttpParam> params,int connectTimeOutInMillis,int readTimeOutInMillis,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doGet(url, params, connectTimeOutInMillis, readTimeOutInMillis, HttpClientResponseType.string,headers,acceptSelfSignedCertificate);
    }
    
    public static HttpClientResponse doGet(String url,List<HttpParam> params,int connectTimeOutInMillis,int readTimeOutInMillis,HttpClientResponseType responseType,List<HttpHeader> headers) throws IOException{
        return doGet(url, params, connectTimeOutInMillis, readTimeOutInMillis, responseType, headers, false);
    }
    
    public static HttpClientResponse doGet(String url,List<HttpParam> params,int connectTimeOutInMillis,int readTimeOutInMillis,HttpClientResponseType responseType,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        String queryString=createParamString(params);
        URL urlObj;
        if(queryString!=null){
            if(url.indexOf('?')>=0)
                urlObj=new URL(url+'&'+queryString);
            else
                urlObj=new URL(url+'?'+queryString);
        }
        else
            urlObj=new URL(url);
        return doGet(urlObj, connectTimeOutInMillis, readTimeOutInMillis,responseType,headers,acceptSelfSignedCertificate);
    }
    
    public static String addParamsToUrl(String url, List<HttpParam> params) throws UnsupportedEncodingException{
        String queryString=createParamString(params);
        if(queryString!=null){
            if(url.indexOf('?')>=0)
                return url+'&'+queryString;
            else
                return url+'?'+queryString;
        }
        else
            return url;
        
    }
    
    private static String LINE_FEED="\r\n";
    private static String charset="UTF-8";
    public static HttpClientResponse doMultiPartForm(URL url, List<HttpParam> params,String fieldName,String fileName,InputStream fileData,int connectTimeOutInMillis,int readTimeOutInMillis,HttpClientResponseType responseType,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        HttpURLConnection httpConn = (HttpURLConnection)(proxy==null?url.openConnection():url.openConnection(proxy));
        if(acceptSelfSignedCertificate && httpConn instanceof HttpsURLConnection)
            ((HttpsURLConnection)httpConn).setSSLSocketFactory(getTrustSelfSignedSocketFactory());
        Writer writer=null;
        OutputStream os=null;
        InputStream is=null;
        String boundary = "====" + System.currentTimeMillis() + "====";
        try {
            httpConn.setInstanceFollowRedirects(false);
            httpConn.setConnectTimeout(connectTimeOutInMillis);
            httpConn.setReadTimeout(readTimeOutInMillis);
            httpConn.setDoInput(true);
            httpConn.setRequestMethod("POST");
            httpConn.setUseCaches(false);
            if(headers!=null){
                for(HttpHeader header:headers){
                    if(header!=null)
                        httpConn.setRequestProperty(header.name, header.value);
                }
            }
            httpConn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
            httpConn.setDoOutput(true);
            os = httpConn.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(os, charset),true);
            //add form fields
            if(params!=null){
                for(HttpParam param:params) {
                    addMultipartFormField(writer, boundary, param.paramName, param.paramvalue);
                }
            }
            //add file
            addFilePart(writer, os, boundary, fieldName, fileName, fileData);

            writer.append("--").append(boundary).append("--").append(LINE_FEED);
            writer.close();
            writer=null;
            try{os.close();}catch(Exception ex){}
            os=null;
            int responseCode = httpConn.getResponseCode();
            if (responseCode < 400)
                is = httpConn.getInputStream();
            else {
                is = httpConn.getErrorStream();
                return new HttpClientResponse(responseCode, read(is),httpConn.getHeaderFields());
            }
            switch (responseType){
                case byteArray:
                    return new HttpClientResponse(responseCode, readBytes(is),httpConn.getHeaderFields());
                case string:
                    return new HttpClientResponse(responseCode, read(is),httpConn.getHeaderFields());
                case inputStream: {
                    HttpClientResponse httpResponse=new HttpClientResponse(responseCode, is,httpConn.getHeaderFields());
                    is=null;
                    return httpResponse;
                }
            }
            return new HttpClientResponse(0,(String)null,httpConn.getHeaderFields());
        }
        finally{
            if(writer!=null)
                try{writer.close();} catch(Exception ex){}
            if(os!=null)
                try{os.close();} catch(Exception ex){}
            if(is!=null)
                try{is.close();} catch(Exception ex){}
            try{fileData.close();}catch(Exception ex){}
        }
    }
    
    public static HttpClientResponse doMultiPartForm(URL url, List<HttpParam> params,String fieldName,String fileName,InputStream fileData,int connectTimeOutInMillis,int readTimeOutInMillis,HttpClientResponseType responseType,List<HttpHeader> headers) throws IOException{
        return doMultiPartForm(url, params, fieldName, fileName, fileData, connectTimeOutInMillis, readTimeOutInMillis, responseType, headers, false);
    }
    
    public static HttpClientResponse doMultiPartForm(URL url, List<HttpParam> params,String fieldName,String fileName,InputStream fileData,List<HttpHeader> headers) throws IOException{
        return doMultiPartForm(url, params, fieldName, fileName, fileData, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,HttpClientResponseType.string,headers);
    }
    
    public static HttpClientResponse doMultiPartForm(URL url, List<HttpParam> params,String fieldName,String fileName,InputStream fileData,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doMultiPartForm(url, params, fieldName, fileName, fileData, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,HttpClientResponseType.string,headers,acceptSelfSignedCertificate);
    }
    
    public static HttpClientResponse doMultiPartForm(URL url, List<HttpParam> params,String fieldName,String fileName,InputStream fileData,HttpClientResponseType responseType,List<HttpHeader> headers) throws IOException{
        return doMultiPartForm(url, params, fieldName, fileName, fileData, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,responseType,headers);
    }
    
    public static HttpClientResponse doMultiPartForm(URL url, List<HttpParam> params,String fieldName,String fileName,InputStream fileData,HttpClientResponseType responseType,List<HttpHeader> headers,boolean acceptSelfSignedCertificate) throws IOException{
        return doMultiPartForm(url, params, fieldName, fileName, fileData, defaultConnectTimeOutInMillis, defaultReadTimeOutInMillis,responseType,headers,acceptSelfSignedCertificate);
    }
    
    private static void addMultipartFormField(Writer writer, String boundary, String name, String value) throws IOException{
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"")
                .append(LINE_FEED);
//        writer.append("Content-Type: text/plain; charset=").append(charset)
//                .append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value==null?"":value).append(LINE_FEED);
        writer.flush();        
    }
    
    private static void addFilePart(Writer writer, OutputStream outputStream, String boundary, String fieldName, String fileName,InputStream fileData)throws IOException {
        if(fileData==null)
            fileName="";
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        writer.append(
                "Content-Type: "
                        + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
//        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();
        if(fileData!=null){
            byte[] buffer = new byte[256];
            int bytesRead = -1;
            while ((bytesRead = fileData.read(buffer)) >=0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
        writer.append(LINE_FEED);
        writer.flush();    
    }
     
    private static SSLSocketFactory trustSelfSignedSocketFactory;
    private static SSLSocketFactory getTrustSelfSignedSocketFactory(){
        if(trustSelfSignedSocketFactory!=null)
            return trustSelfSignedSocketFactory;
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        try {
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            trustSelfSignedSocketFactory=sc.getSocketFactory();
            return trustSelfSignedSocketFactory;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
    }
    
    
    public static final List<HttpCookie> emptyCookieList=Collections.unmodifiableList(new ArrayList<HttpCookie>(0));
    
    public static List<HttpCookie> getCookies(List<String> setCookieHeaderValues){
       if(setCookieHeaderValues==null || setCookieHeaderValues.isEmpty())
           return emptyCookieList;
       List<HttpCookie> cookies=new ArrayList(setCookieHeaderValues.size());
       for(String headerValue:setCookieHeaderValues)
           cookies.add(createCookie(headerValue));
       return cookies;
    }
    
    public static final void addCookiesFromResponse(HttpClientResponse response,List<HttpCookie> cookies){
        List<String> setCookieHeaderValues=response.getHeaderValues(COOKIE_RESPONSE_HEADER);
        if(setCookieHeaderValues==null || setCookieHeaderValues.isEmpty())
            return;
        HttpCookie cookie;
        int index;
        boolean isCookiesEmpty=cookies.isEmpty();
        for(String headerValue:setCookieHeaderValues){
            cookie=createCookie(headerValue);
            if(!isCookiesEmpty){
                index=indexOfCookie(cookies, cookie.name);
                if(index>=0)
                    cookies.remove(index);
            }
            cookies.add(cookie);
        }
       return;
    }
    
    private static int indexOfCookie(List<HttpCookie> cookies,String cookieName){
        int ctr=0;
        for(HttpCookie cookie:cookies){
            if(cookie.name.equals(cookieName))
                return ctr;
            ctr++;
        }
        return -1;
    }    
    private static final String cookieExpiresFormatString="dd-MMM-yyyy hh:mm:ss z";
    private static final ThreadLocal<SimpleDateFormat> cookieExpiresDateFormat =
           new ThreadLocal<SimpleDateFormat>() {
               @Override protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat(cookieExpiresFormatString);
           }
       };
        
    private static HttpCookie createCookie(String cookieHeader){
        String cookieName,cookieValue,cookieExpires=null;
        String[] cookieAttributes=Utils.split(cookieHeader, ';');
        String[] nameValue=Utils.split(cookieAttributes[0], '=');
        cookieName=nameValue[0].trim();
        cookieValue=nameValue[1];
        if(cookieAttributes.length>1){
            nameValue=Utils.split(cookieAttributes[1], '=');
            if(nameValue[0].trim().equalsIgnoreCase("expires")){
                cookieExpires=nameValue[1];
                cookieExpires=cookieExpires.substring(5);
            }
        }
        Date expiresDate=null;
        try{
            expiresDate=cookieExpires==null?null:cookieExpiresDateFormat.get().parse(cookieExpires);
        }catch(Exception ex){
            Utils.log(ex);
        }
        return new HttpCookie(cookieName,cookieValue,expiresDate);
    }
    
    public static HttpHeader createRequestCookieHeader(List<HttpCookie> cookies){
        if(cookies==null || cookies.isEmpty())
            return null;
        int cookieCount=cookies.size();
        StringBuilder sb=new StringBuilder(cookieCount*10);
        for(HttpCookie cookie:cookies)
            sb.append(cookie.name).append('=').append(cookie.value).append("; ");
        return new HttpHeader(COOKIE_REQUEST_HEADER, sb.substring(0, sb.length()-2));
    }
    
}
