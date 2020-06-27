/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.DomainNameMapping;
import io.netty.util.DomainNameMappingBuilder;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;

/**
 *
 * @author Hoshedar Irani
 */
public class WebSocketServer {
    private static SocketConnectionListener connectionListener;
    private static int httpPort;
    private static int httpsPort;
    private static int extPort;
    private static boolean isExtHttps;
    
    private static Channel listeningChannel;
    
    private static class UrlHandler implements Comparable<UrlHandler>{
        String urlPattern;
        WebHandler webHandler;
        Pattern pattern;

        UrlHandler(String urlPattern, WebHandler webHandler) {
            this.urlPattern = urlPattern;
            this.webHandler = webHandler;
            pattern=Pattern.compile(urlPattern);            
        }

        @Override
        public int compareTo(UrlHandler o) {
            return o.urlPattern.compareTo(this.urlPattern);
        }
    }    
    
    private static final Map<String,NavigableSet<UrlHandler>> handlersMap=new ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER); //new ConcurrentSkipListSet();
    
    private static class FilterHandler{
        String urlPattern;
        WebFilter webFilter;
        Pattern pattern;

        FilterHandler(String urlPattern, WebFilter webFilter) {
            this.urlPattern = urlPattern;
            this.webFilter = webFilter;
            pattern=Pattern.compile(urlPattern);
        }
        
    }
    
    private static final Collection<FilterHandler> globalFilters=new ConcurrentLinkedQueue();
    
    private static final Map<String,Collection<FilterHandler>> filtersMap=new ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER);
    
    public static void startAsHttp(int port, int extPort, boolean isExtHttps, SocketConnectionListener connectionListener) {     
        if(port<1)
            throw new IllegalArgumentException("invalid port " + port);
        if(extPort<1)
            extPort=port;
        start(port, -1, extPort, isExtHttps, connectionListener);
    }
    
    public static void startAsHttps(int port,int extPort, SocketConnectionListener connectionListener,boolean enableHttpRedirect) {
        if(port<1)
            throw new IllegalArgumentException("invalid port " + port);
        if(extPort<1)
            extPort=port;
        int httpPort=-1;
        if(enableHttpRedirect){
            if(port<1000){
                httpPort=80;
            }
            else{
                httpPort=(port/1000)*1000+80;
            }
        }
        start(httpPort, port, extPort, true, connectionListener);
    }
    
    private static void start(int httpPort, int httpsPort, int extPort, boolean isExtHttps, SocketConnectionListener connectionListener) {
        if(listeningChannel!=null)
            return;
        if(!Executor.isInitialized())
            Executor.init("server", Runtime.getRuntime().availableProcessors()*2, Runtime.getRuntime().availableProcessors()*20);
        final NioEventLoopGroup eventLoopGroup=new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());        
        WebSocketServer.httpPort=httpPort;
        WebSocketServer.httpsPort=httpsPort;
        WebSocketServer.extPort=extPort;
        WebSocketServer.isExtHttps=isExtHttps;
        WebSocketServer.connectionListener=connectionListener;
        LocalCommandDataSocket.setConnectionListener(connectionListener);
        ServerBootstrap b=new ServerBootstrap();
        b.group(eventLoopGroup);
        b.channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() { 
                @Override
                public void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    int localPort=socketChannel.localAddress().getPort();
                    if(localPort==httpsPort)
                        pipeline.addLast(new SniHandler(domainNameMappingBuilder.build()));//pipeline.addLast(sslContext.newHandler(socketChannel.alloc()));//
                    boolean addWebSocket=httpsPort<1 || (localPort==httpsPort);
                    pipeline.addLast(new HttpServerCodec());
                    pipeline.addLast(new ChunkedWriteHandler());
                    pipeline.addLast(new HttpObjectAggregator(16*1024));
                    pipeline.addLast(new HttpRequestHandler(!addWebSocket,localPort==httpsPort,localPort));
                    if(addWebSocket){
                        pipeline.addLast(new WebSocketServerProtocolHandler("/"));
                        pipeline.addLast(new BinaryWebSocketFrameHandler());
                    }
                }
            });
        ChannelFuture future=null;
        if(httpPort>0){
            future=null;
            try{
                future=b.bind(httpPort).sync();
            }catch(Exception ex){
                eventLoopGroup.shutdownGracefully();
                Executor.shutDown();
                throw new RuntimeException("Port binding error", ex);
            }
            if(future==null || future.isSuccess()==false){
                eventLoopGroup.shutdownGracefully();
                Executor.shutDown();
                return;
            }
        }
        if(httpsPort>0){
            future=null;
            try{
                 future=b.bind(httpsPort).sync();
            }catch(Exception ex){
                eventLoopGroup.shutdownGracefully();
                Executor.shutDown();
                throw new RuntimeException("Port binding error", ex);
            } 
            if(future==null || future.isSuccess()==false){
                eventLoopGroup.shutdownGracefully();
                Executor.shutDown();
                return;
            }
        }
        listeningChannel = future.channel();
        listeningChannel.closeFuture().addListener(new GenericFutureListener(){
            @Override
            public void operationComplete(Future f) throws Exception {
                eventLoopGroup.shutdownGracefully();
            }        
            });
    }
        
    public static void shutdown(){
        shutdown(true, true);
    }
    
    public static void shutdown(boolean shutDownExecutor,boolean destroyLogger){
        long serverShutDownStart=System.currentTimeMillis();       
        Channel listeningeChannel=WebSocketServer.listeningChannel;
        if(listeningeChannel!=null){
            listeningeChannel.close();
            listeningChannel=null;
        }
        Utils.log("server shutdown. Time taken=" + (System.currentTimeMillis()-serverShutDownStart));
        if(destroyLogger)
            Utils.destroyLogger();
        if(shutDownExecutor){
            long executorShutDownStart=System.currentTimeMillis();       
            Executor.shutDown();
            System.out.println("Executor shutDown. Time taken " + (System.currentTimeMillis()-executorShutDownStart));
        }        
    }
    
    /**
     * Registers a WebHandler for processing a web request matching the specified urlPattern 
     * @param domainName - the domain name for which this web handler will be invoked. If value is "*" then it will be invoked for any domain if a matching handler is not available for the domain
     * @param urlPattern - the url pattern for which this web handler will be invoked
     * @param webHandler - the WebHandler that will be invoked when a request matching the url pattern is received by the Server
     */
    public static void addWebHandler(String domainName,String urlPattern, WebHandler webHandler){        
        if(domainName==null)
            throw new NullPointerException("hostName cannot be null");
        NavigableSet<UrlHandler> handlers=handlersMap.get(domainName);
        if(handlers==null){
            handlers=new ConcurrentSkipListSet();
            handlersMap.put(domainName, handlers);
        }
        handlers=handlersMap.get(domainName);
        handlers.add(new UrlHandler(urlPattern, webHandler));        
    }
    
    /**
     * Deregisters a WebHandler on the server
     * @param domainName - the domain name for which this web handler will be removed. 
     * @param urlPattern - the url pattern for which this web handler will be removed
     * @return WebHandler - the webHandler that was deRegistered or null if none was registered for the specified urlPattern
     */
    public static WebHandler removeWebHandler(String domainName,String urlPattern){
        NavigableSet<UrlHandler> handlers=handlersMap.get(domainName);
        if(handlers==null)
            return null;
        Iterator<UrlHandler> iterator=handlers.iterator();
        UrlHandler handler;
        while(iterator.hasNext()){
            handler=iterator.next();
            if(handler.urlPattern.equals(urlPattern)){
                iterator.remove();
                return handler.webHandler;
            }
        }
        return null;
    }

    /**
     * Registers a WebFilter with the server
     * @param domainName - the domain name for which this web filter will be added. If value is "*" then it will be used for all domains
     * @param urlPattern - the url pattern for which this web filter will be invoked
     * @param webFilter - the WebFilter that will be invoked when a request matching the url pattern is received by the Server
     */
    public static void addWebFilter(String domainName,String urlPattern, WebFilter webFilter){
        FilterHandler filterHandler;
        Collection<FilterHandler> filters;
        //if hostName is * add to globalFilters and all other filters and return        
        if(domainName.equals("*")){
            filterHandler=new FilterHandler(urlPattern, webFilter);
            globalFilters.add(filterHandler);
            Set<String> domains=filtersMap.keySet();
            for(String domain:domains){
                filters=filtersMap.get(domain);
                if(filters!=null)
                    filters.add(filterHandler);
            }
            return;
        }
        
        //if hostName is not * the get filters collection for host
        filters=filtersMap.get(domainName);//new ConcurrentLinkedQueue();
        // if null then create filters collection and add all the filters in global filters collection 
        if(filters==null){
            filters=new ConcurrentLinkedQueue(globalFilters);
            filtersMap.put(domainName, filters);
        }
        // add filter to the host's filter collection
        filters.add(new FilterHandler(urlPattern, webFilter));
    }
    
    public static void initExecutor(String threadNamePrefix,int coreThreadPoolSize, int maximumThreadPoolSize) {
        Executor.init(threadNamePrefix, coreThreadPoolSize, maximumThreadPoolSize);
    }
    
    public static int getHttpPort(){
        return httpPort;
    }

    public static int getHttpsPort() {
        return httpsPort;
    }
    
    public static void enableAdvanceLeakDetection(){
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
    }
    
    static class BinaryWebSocketFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {
        private volatile RemoteCommandDataSocket remoteCommandDataSocket;

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                ctx.pipeline().remove(HttpRequestHandler.class);
                remoteCommandDataSocket=RemoteCommandDataSocket.newInstance(ctx.pipeline());
                SocketConnectionListener connectionListener=WebSocketServer.connectionListener;
                connectionListener.newConnection(remoteCommandDataSocket);
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws Exception {
            byte[] bytes=null;
            try{
                ByteBuf byteBuf=frame.content();
                bytes=new byte[byteBuf.readableBytes()];                
                byteBuf.readBytes(bytes,0,bytes.length);
                remoteCommandDataSocket.bytesRead(bytes);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
           Utils.log("Exception in BinaryWebSocketFrameHandler");
           Utils.log(cause);
           ctx.close();
        }
    }
    
    static class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        private final boolean redirectToHttps;
        private final boolean https;
        private final int port;
        public HttpRequestHandler(boolean redirectToHttps,boolean isHttps,int port) {
            this.redirectToHttps= redirectToHttps;
            this.https=isHttps;
            this.port=port;
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx,FullHttpRequest request) throws Exception {           
            String host=request.headers().get("Host");
            String hostName;
            int index=host.indexOf(':');
            if(index<0){
                hostName=host;
            }
            else {
                hostName=host.substring(0, index);
            }
            if(redirectToHttps){               
                String redirectUri;
                redirectUri="https://" + hostName + (extPort==443?"":":"+extPort) + request.uri();
                HttpResponse response=new DefaultFullHttpResponse(request.getProtocolVersion(),HttpResponseStatus.TEMPORARY_REDIRECT);
                response.headers().set(HttpHeaders.Names.LOCATION,redirectUri);
                 ctx.write(response);
                 ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                 future.addListener(ChannelFutureListener.CLOSE);
                 return;
            }
            String upgradeHeader=request.headers().get("Upgrade");
            if (upgradeHeader!=null && upgradeHeader.equals("websocket")) {
                ctx.fireChannelRead(request.retain());
            } else {
                Executor.execute(new HttpRequestProcessor(ctx, request,hostName,this.port,this.https,extPort,isExtHttps));
            }
        }
       
        private static WebHandler getWebHandler(String hostName,String webPath){            
            NavigableSet<UrlHandler> handlers=handlersMap.get(hostName);
            if(handlers==null){                
                handlers=handlersMap.get("*");
                hostName="*";
            }
            if(handlers==null)
                return null;
            for(UrlHandler handler:handlers){
                if(handler.pattern.matcher(webPath).matches())
                    return handler.webHandler;
            }
            if(hostName.equals("*"))
                return null;
            handlers=handlersMap.get("*");
            if(handlers==null)
                return null;
            for(UrlHandler handler:handlers){
                if(handler.pattern.matcher(webPath).matches())
                    return handler.webHandler;
            }
            return null;
        }
       
       private static class HttpRequestProcessor implements Runnable{
           private final ChannelHandlerContext ctx;
           private final FullHttpRequest request;
           private final String hostName;
           private final int port;
           private final boolean https;
           private final int extPort;
           private final boolean isExtHttps;

            public HttpRequestProcessor(ChannelHandlerContext ctx, FullHttpRequest request, String hostName,int port,boolean isHttps, int extPort, boolean  isExtHttps) {
                this.ctx = ctx;
                this.request = request;
                this.hostName=hostName;
                this.port=port;
                this.https=isHttps;
                this.extPort=extPort;
                this.isExtHttps=isExtHttps;
            }

            @Override
            public void run() {
                try{
                    if (HttpHeaders.is100ContinueExpected(request)) {
                        send100Continue(ctx);
                    }
                    boolean keepAlive = HttpHeaders.isKeepAlive(request);
                    String uri=request.uri();
                    String webPath=uri,queryString="";
                    int index=uri.indexOf('?');
                    if(index>=0){
                        webPath=uri.substring(0, index);
                        queryString=uri.substring(index+1);
                    }
                    WebHandler webHandler=getWebHandler(hostName,uri);
                    Collection<FilterHandler> filters=filtersMap.get(hostName);
                    if(filters==null){
                        filters=globalFilters;
                    }
                    //create filter chain
                    WebFilterChain chain=new WebFilterChain();
                    for(FilterHandler filterHandler:filters){
                        if(filterHandler.pattern.matcher(webPath).matches())
                            chain.add(filterHandler.webFilter);
                    }
                    //if webhandler is not null set responseStatus to 200  and add webHandler to chain else set to 0
                    BasicWebResponse webResponse=new BasicWebResponse();
                    webResponse.setStatus(0);
                    if(webHandler!=null){
                        webResponse.setStatus(200);
                        chain.add(webHandler);
                    }
                    //invoke filter chain
                    List<Map.Entry<String,String>> entries=request.headers().entries();
                    List<HttpHeader> requestHeaders=new LinkedList();
                    HttpHeaders reqHeaders=request.headers();
                    if(reqHeaders!=null){
                        for(Map.Entry<String,String> entry:reqHeaders)
                            requestHeaders.add(new HttpHeader(entry.getKey(), entry.getValue()));
                    }
                    BasicWebRequest webRequest=new BasicWebRequest(ctx.channel().remoteAddress(), requestHeaders,hostName, webPath,queryString,this.port,this.https,this.extPort,this.isExtHttps);
                    try{
                        chain.doFilter(webRequest, webResponse);
                    }finally{webResponse.closeOutputStream();}
                    //if response status is 0 then set status to 200 and if headers are not null or responsesize is > 0 then set statuscode to not_found
                    List<HttpHeader> responseHeaders=webResponse.getHttpResponseHeaders();
                    if(webResponse.getStatus()==0){
                        webResponse.setStatus(200);
                        if(responseHeaders==null && webResponse.getByteCount()<1){
                            webResponse.setStatus(404);
                        }
                    }
                    HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.valueOf(webResponse.getStatus()));
                    if(responseHeaders!=null){
                        HttpHeaders resHeaders=response.headers();
                        for(HttpHeader header:responseHeaders)
                            resHeaders.set(header.name, header.value);
                    }
                    String mimeType=webResponse.getMimeType();
                    if(mimeType!=null)
                        response.headers().set(HttpHeaders.Names.CONTENT_TYPE,mimeType);
                    response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, webResponse.getByteCount());
                    if (keepAlive) {
                        response.headers().set( HttpHeaders.Names.CONNECTION,HttpHeaders.Values.KEEP_ALIVE);
                    }
                    ctx.write(response);
                    ChunkedInput<ByteBuf> resourceBody=null;
                    if(webResponse.isFile()){
                        File file=webResponse.getFile();
                        if(file!=null){
                            RandomAccessFile srcFile = new RandomAccessFile(file, "r");
                            resourceBody=new ChunkedNioFile(srcFile.getChannel());
                        }
                    }
                    else{
                        InputStream inputStream=webResponse.getStream();
                        if(inputStream!=null)
                            resourceBody=new ChunkedStream(inputStream);
                    }
                    if(resourceBody!=null)
                        ctx.write(resourceBody);
                    ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                    if (!keepAlive) {
                        future.addListener(ChannelFutureListener.CLOSE);
                    }
                }catch(Exception ex){
                    Utils.log(ex);
                    ctx.close();
                }
            }
       }

       private static void send100Continue(ChannelHandlerContext ctx) {
           FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
           ctx.writeAndFlush(response);
       }
       
       @Override
       public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
           Utils.log("Exception in HttpRequestHandler");
           Utils.log(cause);
           ctx.close();
       }
   }
   
    private static DomainNameMappingBuilder<SslContext> domainNameMappingBuilder;
    public static final void addSslCertificate(String domainName,File pfxFile,String pswd,String keyAlias) throws Exception{
        KeyStore keyStore=Crypto.loadKeyStore(pfxFile, "PKCS12", pswd);
        KeyStore.PrivateKeyEntry keyEntry=Crypto.getEntry(keyStore, keyAlias, pswd);
        Certificate[] certificates=keyEntry.getCertificateChain();   
        X509Certificate[] x509Certificates=new X509Certificate[certificates.length];
        for(int ctr=0;ctr<certificates.length;ctr++)
            x509Certificates[ctr]=(X509Certificate)certificates[ctr];
        SslContextBuilder sslContextBuilder=SslContextBuilder.forServer(keyEntry.getPrivateKey(), x509Certificates);
        SslContext sslContext=sslContextBuilder.build();
        if(domainNameMappingBuilder==null){
            domainNameMappingBuilder=new DomainNameMappingBuilder(sslContext);
        }
        else{
            domainNameMappingBuilder.add(domainName, sslContext);
        }
    }
    
    public static final void addSslCertificate(File pfxFile,String pswd,String keyAlias) throws Exception{
        addSslCertificate(null, pfxFile, pswd, keyAlias);
    }
    
    private static void main2(String[] args) throws Exception{
        File file=new File("C:\\projects\\GameWebClient\\public_html\\.well-known\\acme-challenge\\notes.txt");
        RandomAccessFile racFile=new RandomAccessFile(file, "r");
        FileChannel fileChannel=racFile.getChannel();
        ByteBuffer byteBuffer=ByteBuffer.allocate((int)file.length());
        int bytesRead=fileChannel.read(byteBuffer);
        System.out.println("bytesRead=" + bytesRead);
        System.out.println(new String(byteBuffer.array()));
        fileChannel.close();
        racFile.close();
        Thread.sleep(1000);
        if(1==1)
            return;
        try{
            WebSocketServer.addSslCertificate(new File("c:/temp/server.pfx"), "test123", null);
            WebSocketServer.start(80, 443,-1,true, null);
            java.net.URL url=new java.net.URL("http://localhost");
            HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.getResponseCode();
            Thread.sleep(600);
        }finally{
            WebSocketServer.shutdown();
        }
    }
}
