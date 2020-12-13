/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import io.sockit.servertools.Utils;
import io.sockit.servertools.CommandDataReadListener;
import io.sockit.servertools.CommandDataSocket;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author h
 */
class NonSessionCommandListener implements CommandDataReadListener{

    int maxNoOfLoggedInUsers=Integer.MAX_VALUE;
    
    NonSessionCommandListener() {
//        maxNoOfLoggedInUsers=Functions.toInt(Functions.descrambleBytes(new byte[]{(byte)0x34,(byte)0x0,(byte)0x0,(byte)0x0}));        
    }

    @Override
    public void commandDataRead(CommandDataSocket socket,String command, String data) {
        processCommand(socket, command, data);
    }

    @Override
    public void commandDataRead(CommandDataSocket socket,String command, byte[] data) {
        processCommand(socket, command,data);
    }
    
    void processCommand(CommandDataSocket socket,String command, String data) {
        try{
            JsonObject dataAsJson=null;
            if(data!=null){
                dataAsJson=JsonUtil.readObject(data);
            }
            if(Server.shutDownStarted.get()){
                Session.sendError(socket, ErrorCodes.serverShutdownStarted, ErrorDescriptions.shutdownStarted);
                socket.close();
                return;
            }
            switch(Command.toEnum(command)){
                case register:
                    {                        
                        String registrationType=JsonUtil.getAsString(dataAsJson, "type");
                        String emailId=JsonUtil.getAsString(dataAsJson, "emailId");
                        String otherIdId=JsonUtil.getAsString(dataAsJson, "otherId");
                        String password=JsonUtil.getAsString(dataAsJson, "password");
                        String name=JsonUtil.getAsString(dataAsJson, "name");
                        GameObj gameObj=Games.getGame(JsonUtil.getAsString(dataAsJson, "gameName"));
                        if("email".equals(registrationType))
                            User.registerUserWithEmailId(emailId, password, name, socket, gameObj);
                        else if("other".equals(registrationType))
                            User.registerUserWithOtherId(otherIdId, password, name, socket, gameObj);
                        else
                            Session.sendError(socket, ErrorCodes.invalidRegistrationType, ErrorDescriptions.invalidRegistrationType);
                    }
                    break;
                case login:{
                    if(Session.nonBotSessionCount.get()>=maxNoOfLoggedInUsers){
                        Session.sendError(socket, ErrorCodes.tooManyUsersLoggedIn, ErrorDescriptions.tooManyUsersLoggedIn);//send error or close session
                        socket.close();
                        break;
                    }
                    //3 types of login email,other,google and bot
                    String loginType=JsonUtil.getAsString(dataAsJson, "type", "");
                    GameObj gameObj=Games.getGame(JsonUtil.getAsString(dataAsJson, "gameName"));
                    if(loginType.equals("google"))
                        User.loginWithGoogle(JsonUtil.getAsString(dataAsJson, "idToken"), socket,gameObj);
                    else if(loginType.equals("email"))
                       User.loginByEmail(JsonUtil.getAsString(dataAsJson, "emailId"), JsonUtil.getAsString(dataAsJson, "password"), socket,gameObj);
                    else if(loginType.equals("other"))
                        User.loginByOtherId(JsonUtil.getAsString(dataAsJson, "otherId"), JsonUtil.getAsString(dataAsJson, "password"), socket,gameObj);
                    else if(loginType.equals("bot"))
                        User.loginByBot(JsonUtil.getAsString(dataAsJson, "name"),JsonUtil.getAsInt(dataAsJson, "avtarId"), socket,gameObj);
                    else {
                        Session.sendError(socket, ErrorCodes.invalidLoginType, ErrorDescriptions.invalidLoginType);//send error or close session
                        socket.close();
                        break;
                    }
                    break;
                }
                case poll:{
                    String sessionId=JsonUtil.getAsString(dataAsJson, "sessionId");
                    Session session=Session.getSessionById(sessionId);
                    if(session!=null && session.sessionClosed==false){
                        session.setSocket(socket);
                        Room room=session.room;
                        if(room!=null){
                            session.sendUserData();
                            session.sendMesg(Commands.roomData, room.getRoomAsJson(session).toString());
                        }
                    }
                    else{
                        if(session!=null){
                            session.close();
                        }
                        socket.write(Commands.sessionTimedOut, (String)null);
                        socket.close();
                    }
                    break;
                }
                case rejoinSession:{
                    String sessionId=JsonUtil.getAsString(dataAsJson, "sessionId");
                    Session session=Session.getSessionById(sessionId);
                    if(session!=null && session.sessionClosed==false){
                        session.setSocket(socket);
                        Room room=session.room;
                        User user=session.user;
                        JsonObjectBuilder userJsonBuilder=JsonUtil.createObjectBuilder();
                        userJsonBuilder.add("userId", user.userId);
                        userJsonBuilder.add("sessionId", session.getSessionId());
                        userJsonBuilder.add("avtarId", user.avtarId);
                        userJsonBuilder.add("name", user.name);
                        userJsonBuilder.add("gameName", session.getGameName());
                        userJsonBuilder.add("gameUserData", session.getGameUserDataForClientAsJson());
                        JsonObjectBuilder mainJsonBuilder=JsonUtil.createObjectBuilder();
                        mainJsonBuilder.add("user", userJsonBuilder);
                        mainJsonBuilder.add("room", room==null?null:room.getRoomAsJson(session));
                        session.sendMesg(Commands.sessionRejoined, mainJsonBuilder.build().toString());
                    }
                    else{
                        socket.write(Commands.sessionTimedOut, (String)null);
                        socket.close();
                    }
                    break;                    
                }
            }
        }catch(Exception ex){
            Utils.log(ex);
            if(socket!=null) 
                socket.close();
        }
    }
    
    void processCommand(CommandDataSocket socket,String command, byte[] data) {
        
    }
    
}
