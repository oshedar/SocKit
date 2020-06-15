/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import io.sockit.servertools.AbstractSocket;
import io.sockit.servertools.Utils;
import io.sockit.servertools.Console;
import io.sockit.servertools.Executor;
import io.sockit.servertools.WebSocketServer;
import io.sockit.servertools.CommandDataSocket;
import io.sockit.servertools.FileLogger;
import io.sockit.servertools.Logger;
import io.sockit.servertools.WebHandler;
import io.sockit.servertools.SocketConnectionListener;
import io.sockit.servertools.WebFilter;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is used to start and stop the Game Engine. It also has methods to set the dataStore where game data is saved and set the WebResourceGetter (for web requests such as .html and .css files. Below is example code for starting the game engine
 * <br>
 * <pre>{@code
       Server.setDataStore(new LevelDbStore("./gameDb")); //sets the Database
       Server.setLogger(new FileLogger("./serverLog.txt",true)); // Sets the Logger
       Server.setWebHandler(new DefaultWebHandler("/webRoot"));  //Sets the web provider       
       Server.startServerAsHttp(0); //0 will use default port of 80
 } </pre>
 */
public class Server{ 

    private static NonSessionCommandListener nonSessCommandDataReadListener;
            
    /**
     * Starts server as an http service (no SSL)
     * @param port - the port on which server will accept new client connections. If 0 is passed server will use the default http port of 80
     * @throws Exception - if unable to start server for reasons such as port is not available, etc
     */
    public final static void startServerAsHttp(int port) throws Exception{
        if(port<1)
            port=80;
        start(port, false, false);
        
    }
    
    /**
     * starts server as an https service (with ssl support).
     * @param port - the port on which server will accept new client connections. If 0 is passed server will use the default https port of 443
     * @param enableHttpRedirect - indicates whether to redirect client to https if client attempts to connect on http port.
     * @throws Exception - 
     */
    public final static void startServerAsHttps(int port,boolean enableHttpRedirect) throws Exception{
        if(port<1)
            port=443;
        start(port, true, enableHttpRedirect);
    }
    
    private final static void start(int port,boolean asHttps,boolean enableHttpRedirect) throws Exception{
        boolean gameDbOpened=false;
        try{
            WebSocketServer.initExecutor("gms", Runtime.getRuntime().availableProcessors()*2, Runtime.getRuntime().availableProcessors()*20);
            Utils.setLogger(logger);
            GameDB.setDataStore(dataStore);
            gameDbOpened=true;
            DataCache.init(cacheSize);
            nonSessCommandDataReadListener=new NonSessionCommandListener();
            if(asHttps)
                WebSocketServer.startAsHttps(port, new ConnectionHandler(),enableHttpRedirect);
            else
                WebSocketServer.startAsHttp(port, new ConnectionHandler());
            Runtime.getRuntime().addShutdownHook(new ShutDown());
            Games.serverStarted();
            String ports="";
            if(asHttps && enableHttpRedirect)
                ports+="80," + port;
            else
                ports+=port;            
            Utils.log("Server started on ports: " + ports);
        }catch(Exception ex){
            if(gameDbOpened)
                GameDB.close();
            throw ex;
        }
            
    }
    
    /**
     * This server setting allows user Login action to be combined with the user register action -  this way there is a single point of entry for both first time users as well as returning users. This is useful for testing code during development
     * @param combineLoginWithRegister - specifies whether to enable this setting or not
     */
    public static void setCombineLoginWithRegisterUser(boolean combineLoginWithRegister){
        User.setCombineLoginWithRegister(combineLoginWithRegister);
    }
    
    /**
     * Registers a WebHandler for processing a web request matching the specified urlPattern 
     * @param domainName - domain name for which this web handler will be invoked. If value is "*" then it will be invoked for any domain if a matching handler is not available for the domain
     * @param urlPattern - the url pattern for which this web handler will be invoked
     * @param webHandler - the WebHandler that will be invoked when a request matching the url pattern is received by the Server
     */
    public static void addWebHandler(String domainName,String urlPattern, WebHandler webHandler){
        WebSocketServer.addWebHandler(domainName,urlPattern, webHandler);
    }
    
    /**
     * Deregisters a WebHandler on the server
     * @param domainName - the domain name for which this web handler will be removed. 
     * @param urlPattern - the url pattern for which this web handler will be removed
     * @return WebHandler - the webHandler that was deRegistered or null if none was registered for the specified urlPattern
     */
    public static WebHandler removeHandler(String domainName,String urlPattern){
        return WebSocketServer.removeWebHandler(domainName,urlPattern);
    }

    /**
     * Registers a WebFilter with the server
     * @param domainName - the domain name for which this web filter will be added. If value is "*" then it will be used for all domains
     * @param urlPattern - the url pattern for which this web filter will be invoked
     * @param webFilter - the WebFilter that will be invoked when a request matching the url pattern is received by the Server
     */
    public static void addWebFilter(String domainName,String urlPattern, WebFilter webFilter){
        WebSocketServer.addWebFilter(domainName,urlPattern, webFilter);
    }
    
    /**
     * Adds the SSL certificate to use by the https service.
     * @param pfxFile - the keystore (PKCS#12 format) file where the private key and the public key certificate is stored
     * @param pswd - the keystore password
     * @param keyAlias - the key alias. pass null if there is no key alias
     * @throws Exception - 
     */
    public final static void addSslCertificate(File pfxFile,String pswd,String keyAlias) throws Exception{
        WebSocketServer.addSslCertificate(pfxFile, pswd, keyAlias);
    }
    
    /**
     * Adds the SSL certificate to use by the https service.
     * @param domainName - the domain name for which this certificate will be used
     * @param pfxFile - the keystore (PKCS#12 format) file where the private key and the public key certificate is stored
     * @param pswd - the keystore password
     * @param keyAlias - the key alias. pass null if there is no key alias
     * @throws Exception - 
     */
    public final static void addSslCertificate(String domainName,File pfxFile,String pswd,String keyAlias) throws Exception{
        WebSocketServer.addSslCertificate(domainName,pfxFile, pswd, keyAlias);
    }
    
    volatile static AtomicBoolean shutDownStarted=new AtomicBoolean(false);

    /**
     * Stops the server
     */
    public final static void stopServer(){
        if(!shutDownStarted.compareAndSet(false, true))
            return;
        long stopStartTime=System.currentTimeMillis();
        Utils.log("Stopping Server");
        long roomsShutDownStart=System.currentTimeMillis();
        Room.doShutDown();
        Utils.log("rooms shutdown. Time taken=" + (System.currentTimeMillis()-roomsShutDownStart));
        long usersShutDownStart=System.currentTimeMillis();
        Session.doShutDown();
        Utils.log("users shutdown. Time taken=" + (System.currentTimeMillis()-usersShutDownStart));       
        long dataStoreCloseStart=System.currentTimeMillis();
        GameDB.close();
        Utils.log("DataStore closed. Time taken=" + (System.currentTimeMillis()-dataStoreCloseStart));        
        long socketServerStopStart=System.currentTimeMillis();
        WebSocketServer.shutdown();
        Utils.log("SocketServer stoped. Time Taken=" + (System.currentTimeMillis()-socketServerStopStart));
        System.out.println("Server stopped. Time Taken=" + (System.currentTimeMillis()-stopStartTime));
    }
    
    static class ShutDown extends Thread{

        @Override
        public void run() {            
            Server.stopServer();
        }
        
    }
    
    /**
     * Sets the sessionListenerFactory which will be used to create a SessionListener instance for each new Session.
     * @param sessionListenerFactory - the sessionListenerFactory which will be used to create a SessionListener instance for each new Session.
     */
    public static void setSessionListenerFactory(SessionListenerFactory sessionListenerFactory){
        Session.sessionListenerFactory=sessionListenerFactory;
    }
    
    private static int cacheSize=2000;

    /**
     * Sets the initial size of the cache where user objects are kept in memory. Default is 2000
     * @param size - the size of the cache
     */
    public static void setInitialUsersCacheSize(int size){
        if(size<2000)
            return;
        cacheSize=size;
    }
    
    /**
     * This optimizes the system for the specified number of sessions
     * @param expectedMaxActiveSessions - the expected max number of active sessions.
     */
    public static void setExpectedMaxActiveSessions(int expectedMaxActiveSessions){
        Session.initSessionsMap(expectedMaxActiveSessions);
    }
    
    private static DataStore dataStore;

    /**
     * Sets the DataStore implementation which will be used to save the Game Engine data.
     * @param dataStore - An instance of a class which implements the DataStore Interface. 
     */
    public static void setDataStore(DataStore dataStore) {        
        Server.dataStore = dataStore;
    }
    
    private static Logger logger;

    /**
     * Sets the Logger implementation  which will be used to log the Game Engine log messages
     * @param logger - An instance of a class which implements the Logger Interface
     */
    static void setLogger(Logger logger) {        
        Server.logger = logger;
    }
    
    /**
     * Sets the Server log file where all the log messages will be saved
     * @param logFile - the log File
     */
    public static void setLogFile(String logFile){
        setLogger(new FileLogger(logFile, true));
    }
        
    
    static class ConnectionHandler implements SocketConnectionListener{
        @Override
        public void newConnection(AbstractSocket socket) {
            CommandDataSocket commandDataSocket=(CommandDataSocket)socket;
            commandDataSocket.setCommandDataReadListener(nonSessCommandDataReadListener);
        }
    }
    
    /**
     * Logs a message on the SocketServer
     * @param mesg - the message to be logged
     */
    public static void log(String mesg){
        Utils.log(mesg);
    }

    /**
     * Logs an exception on the SocketServer
     * @param ex - the exception to be logged
     */
    public static void log(Exception ex){
        Utils.log(ex);
    }
    
    /**
     * Registers a new Game on the Game engine
     * @param game - the game to be registered
     * @throws DuplicateGameNameHashException - 
     */
    public static final void registerGame(Game game) throws DuplicateGameNameHashException{
        Games.registerNewGame(game);
    }

    /**
     * Executes a task asynchronously on the server after the specified delay - if delay is 0 task will be executed immediately
     * @param task - the Runnable task to be executed
     * @param delayInMillisSec - the delay in milli seconds
     */
    public static void execute(Runnable task,int delayInMillisSec){
        if(delayInMillisSec<1)
            Executor.execute(task);
        else
            Executor.executeWait(task, delayInMillisSec);
    }
        
    /**
     * Logs a message to the Server's console. Use this method instead of System.out.println() if you want to print messages to the console in the correct order. 
     * @param mesg - the message to log
     */
    public static void logToConsole(String mesg){
        Console.log(mesg);
    }
}
