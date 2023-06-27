/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.pokergame;

import io.sockit.servertools.BasicWebHandler;
import io.sockit.servertools.Console;
import io.sockit.sockitserver.Server;
import io.sockit.sockitserver.LevelDbStore;
import io.sockit.sockitserver.WebAnalyticsFilter;
import io.sockit.sockitserver.bot.BotTurnDelayType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
//import org.apache.commons.daemon.Daemon;
//import org.apache.commons.daemon.DaemonContext;
//import org.apache.commons.daemon.DaemonInitException;

/**
 *
 * @author Hoshedar Irani
 */
public class Main { //implements Daemon{
    
    public static void main(String[] args) throws Exception{
//        System.out.println(new File("../..").getCanonicalPath());
//        if(1==1) return;
        String dbPath=getArgValue(args, "-dbPath");
        if(dbPath==null)
            dbPath="../../gameDb";

        String logFile=getArgValue(args, "-logFile");
        if(logFile==null)
            logFile="../../serverLog.txt";
                
        String webPort=getArgValue(args, "-port");
        int port=0;
        if(webPort!=null)
            port=Integer.parseInt(webPort);
        
        String extWebPort=getArgValue(args, "-extPort");
        int extPort=-1;
        if(extWebPort!=null)
            extPort=Integer.parseInt(extWebPort);
        
        String isExtWebHttps=getArgValue(args, "-isExtHttps");
        boolean isExtHttps=false;
        if(isExtWebHttps!=null)
            isExtHttps=Boolean.parseBoolean(isExtWebHttps);
        
        String hideShutdownPromptStr=getArgValue(args, "hideShutdownPrompt");
        boolean hideShutdownPrompt=false;
        if(hideShutdownPromptStr!=null)
            hideShutdownPrompt=Boolean.parseBoolean(hideShutdownPromptStr);
        
//        System.out.println("web root folder=" + webRootFolder.getAbsolutePath());
//        System.out.println("web root exists=" + webRootFolder.exists());
//        port=8443;
//        addSite("*", new File("/home/hoshi/sites/sockit/GameWebClient/public_html"), "/home/hoshi/sites/sockit/keys/sockit.pfx", "test@123", null);
//        addSite("funwithcode.in", new File("/home/hoshi/sites/funwithcode/webroot"), "/home/hoshi/sites/funwithcode/key.pfx", "fun@123", null);

        String pfxFile=getArgValue(args, "-pfxFile");
        String pfxPswd=getArgValue(args, "-pfxPswd");
        String domainName=getArgValue(args, "-domainName");
        String webPath=getArgValue(args, "-webPath");
        if(domainName==null)
            domainName="*";
        File webRootFolder;
        if(webPath!=null)
            webRootFolder=new File(webPath);
        else        
            webRootFolder=new File(new File(Main.class.getResource("Main.class").toURI()).getParentFile(),"web");
        if(pfxFile==null)
            pfxPswd=null;
        if(sites.isEmpty())
            addSite(domainName, webRootFolder, pfxFile, pfxPswd, null);

        pfxFile=getArgValue(args, "-pfxFile2");
        pfxPswd=getArgValue(args, "-pfxPswd2");
        domainName=getArgValue(args, "-domainName2");
        webPath=getArgValue(args, "-webPath2");
        if(pfxFile==null)
            pfxPswd=null;
        if(domainName!=null){
            webRootFolder=new File(webPath);
            addSite(domainName, webRootFolder, pfxFile, pfxPswd, null);
        }
        pfxFile=getArgValue(args, "-pfxFile3");
        pfxPswd=getArgValue(args, "-pfxPswd3");
        domainName=getArgValue(args, "-domainName3");
        webPath=getArgValue(args, "-webPath3");
        if(pfxFile==null)
            pfxPswd=null;
        if(domainName!=null){
            webRootFolder=new File(webPath);
            addSite(domainName, webRootFolder, pfxFile, pfxPswd, null);
        }
        Main.startServer(dbPath, logFile,port,extPort,isExtHttps,hideShutdownPrompt);
    }
    
    private static class SiteConfig{
        String domainName;
        File webPath;
        String pfxFile;
        String pfxPswd;
        String pvtKeyAlias;

        public SiteConfig(String domainName, File webPath, String pfxFile, String pfxPswd, String pvtKeyAlias) {
            this.domainName = domainName;
            this.webPath = webPath;
            this.pfxFile = pfxFile;
            this.pfxPswd = pfxPswd;
            this.pvtKeyAlias = pvtKeyAlias;
        }
        
    }
    private static List<SiteConfig> sites=new ArrayList(2);

    private static void addSite(String domainName, File webPath, String pfxFile, String pfxPswd, String pvtKeyAlias){
        sites.add(new SiteConfig(domainName, webPath, pfxFile, pfxPswd, pvtKeyAlias));
    }
    
    private static void startServer(String dbPath,String logFile,int port,int extPort,boolean isExtHttps,boolean hideShutdownPrompt) throws Exception{
        Server.registerGame(new PokerGame(20,BotTurnDelayType.fast,8));
        Server.setInitialUsersCacheSize(2000);
        Server.setDataStore(new LevelDbStore(dbPath));
        Server.setCombineLoginWithRegisterUser(true);
        if(logFile!=null)
            Server.setLogFile(logFile);
        Server.addWebFilter("*",".*", new WebAnalyticsFilter());
        boolean isSsl=false;
        for(SiteConfig site:sites){
            if(site.pfxFile!=null){
                Server.addSslCertificate(site.domainName,new File(site.pfxFile), site.pfxPswd, site.pvtKeyAlias);
                isSsl=true;
            }
            Server.addWebHandler(site.domainName,".*",new BasicWebHandler(site.webPath));
        }
        if(isSsl)
            Server.startServerAsHttps(port,extPort, true);
        else
            Server.startServerAsHttp(port,extPort,isExtHttps);
        Server.logToConsole("Server started on port: " + port);
        Server.logToConsole("Press Q + Enter to Quit");
        int charRead;
        if(!hideShutdownPrompt){
            while(true){
                charRead=(char)System.in.read();
                if(charRead=='Q' || charRead=='q')
                    break;
            }
            Server.stopServer();
        }
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
//        Main.startServer(dbPath, webPath, logFile);
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
