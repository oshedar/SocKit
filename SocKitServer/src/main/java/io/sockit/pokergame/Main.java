/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.pokergame;

import io.sockit.servertools.BasicWebHandler;
import io.sockit.sockitserver.Server;
import io.sockit.sockitserver.LevelDbStore;
import io.sockit.sockitserver.WebAnalyticsFilter;
import io.sockit.sockitserver.bot.BotTurnDelayType;
import java.io.File;
//import org.apache.commons.daemon.Daemon;
//import org.apache.commons.daemon.DaemonContext;
//import org.apache.commons.daemon.DaemonInitException;

/**
 *
 * @author Hoshedar Irani
 */
public class Main { //implements Daemon{
    
    public static void main(String[] args) throws Exception{
        String dbPath=getArgValue(args, "-dbPath");
        if(dbPath==null)
            dbPath="../gameDb";

        String logFile=getArgValue(args, "-logFile");
        if(logFile==null)
            logFile="../serverLog.txt";
        
        String webPath=getArgValue(args, "-webPath");
        if(webPath==null)
            webPath="../GameWebClient/public_html";

        File webRootFolder=new File(webPath);
        
//        webRootFolder=new File(new File(Main.class.getResource("Main.class").toURI()).getParentFile(),"web");        
        
        System.out.println("web root folder=" + webRootFolder.getAbsolutePath());
        System.out.println("web root exists=" + webRootFolder.exists());
        
        String webPort=getArgValue(args, "-port");
        int port=0;
        if(webPort!=null)
            port=Integer.parseInt(webPort);
        
        String pfxFile=getArgValue(args, "-pfxFile");
        String pfxPswd=getArgValue(args, "-pfxPswd");
        String pvtKeyAlias=getArgValue(args, "-pvtKeyAlias");
        
        Main main=new Main();
        main.startServer(dbPath, webRootFolder, logFile,port,pfxFile,pfxPswd,pvtKeyAlias);
    }
    
    private void startServer(String dbPath,File webPath,String logFile,int port,String pfxFile,String pfxPswd,String pvtKeyAlias) throws Exception{
        Server.registerGame(new PokerGame(20,BotTurnDelayType.fast,8));
        if(pfxFile!=null)
            Server.setSslCertificate(new File(pfxFile), pfxPswd, pvtKeyAlias);
        Server.setInitialUsersCacheSize(2000);
        Server.setDataStore(new LevelDbStore(dbPath));
        Server.setCombineLoginWithRegisterUser(true);
        if(logFile!=null)
            Server.setLogFile(logFile);
        Server.addWebHandler(".*",new BasicWebHandler(webPath));
        Server.addWebFilter(".*", new WebAnalyticsFilter());
        if(pfxFile!=null)
            Server.startServerAsHttps(port, true);
        else
            Server.startServerAsHttp(port);
        Server.logToConsole("Server started on ports: 80, 443");
        Server.logToConsole("Press Q + Enter to Quit");
        int charRead;
        while(true){
            charRead=(char)System.in.read();
            if(charRead=='Q' || charRead=='q')
                break;
        }
        Server.stopServer();        
    }
    
    static String getArgValue(String[] args,String argName){
        for(int ctr=0;ctr<args.length;ctr++ ){
            if(args[ctr].equalsIgnoreCase(argName)){
                if(args.length>ctr+1)
                    return args[ctr+1];
                return null;
            }            
        }
        return null;
    }

//    @Override
//    public void init(DaemonContext dc) throws DaemonInitException, Exception {
//    }
//
//    @Override
//    public void start() throws Exception {
//        String dbPath="../gamedb";
//        String logFile="../serverLog.txt";
//        String webPath="C:/projects/GameWebClient/public_html";
//        this.startServer(dbPath, webPath, logFile);
//    }
//
//    @Override
//    public void stop() throws Exception {
//        Server.stopServer();
//    }
//
//    @Override
//    public void destroy() {
//    }
    
}
