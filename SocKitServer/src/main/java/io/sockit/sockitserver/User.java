/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import io.sockit.servertools.Utils;
import io.sockit.servertools.RandomStringGenerator;
import io.sockit.servertools.FieldGetter;
import io.sockit.servertools.Base64;
import io.sockit.servertools.CommandDataSocket;
import io.sockit.servertools.Crypto;
import io.sockit.servertools.HttpClient;
import io.sockit.servertools.HttpClientResponse;
import io.sockit.servertools.BeanDescriptor;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.json.JsonObjectBuilder;

/**
 * This class represents a user who can login to the server.
 */
public class User extends Persistable {

    /**
     * a unique identifier for each User
     */
    public final String userId;

    /**
     * determines if this User is a bot
     */
    public final boolean isBot;
    String otherId;
    String emailId;
    String password;
    String name;
    int avtarId;
    boolean enableProfilePicture;
    String profilePic;
    private static boolean combineLoginWithRegister=false;
    AtomicBoolean modified=new AtomicBoolean(false);
    private final static Lock staticLock=new ReentrantLock();
    final Lock instanceLock=new ReentrantLock();
    
    private User(String userId, boolean isBot, String otherId, String emailId,String name,String password,String profilePic) {
        this.userId = userId;
        this.isBot = isBot;
        this.otherId = otherId;
        this.emailId = emailId;
        this.name=name;
        this.password=password;
        this.profilePic=profilePic;
   }
    
    private User(String userId,boolean isBot){
        this.userId=userId;
        this.isBot=isBot;
    }
    
    static User create(String otherId,String emailId,boolean isBot,String name,String password,String profilePic){
        staticLock.lock();
        try{
            String userId;
            if(isBot){
                userId="bt_" + RandomStringGenerator.randomAlphanumeric(14);
            }
            else {
                do{
                    userId=RandomStringGenerator.randomAlphanumeric(16);
                }while(DataCache.userExists(userId) || GameDB.exists(userId));
            }
            User user=new User(userId, isBot, otherId, emailId,name,password,profilePic);
            if(!isBot){
                GameDB.writeNew(user);
                DataCache.putUser(user);
            }            
            return user;
        }finally{staticLock.unlock();}
    }
    
    void save(){
        save(false);
    }
    
    void save(boolean forceSave){
        if(!modified.get() && !forceSave)
            return;
        if(!modified.compareAndSet(true, false))
            return;
        GameDB.write(this);            
    }
            
    static User getByUserId(String userId){
        //check datacache if not exists then check Data
        User user=DataCache.getUserByUserId(userId);
        if(user==null){
            user=GameDB.read(userId);
            if(user==null)
                return null;
            DataCache.putUser(user);
            return user;
        }
        return user;
    }
    
    static User getByOtherId(String otherId){
        //check datacache if not exists then check Data
        User user=DataCache.getUserByOtherId(otherId);
        if(user==null){
            user=GameDB.readWithOtherId(otherId);
            if(user==null)
                return null;
            DataCache.putUser(user);
            return user;
        }
        return user;
    }
    
    static User getByEmailId(String emailId){
        //check datacache if not exists then check Data
        User user=DataCache.getUserByEmailId(emailId);
        if(user==null){
            user=GameDB.readWithEmailId(emailId);
            if(user==null)
                return null;
            DataCache.putUser(user);
            return user;
        }
        return user;
    }
    
    String convertToString(){
        JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
        jsonBuilder.add("a", userId);
        jsonBuilder.add("b", otherId);
        jsonBuilder.add("c", emailId);
        jsonBuilder.add("d", name);
        jsonBuilder.add("e", avtarId);
        jsonBuilder.add("g", enableProfilePicture);
        jsonBuilder.add("h", password);
        jsonBuilder.add("j", profilePic);
        return jsonBuilder.build().toString();
    }
    
    static User fromString(String s){
        JsonObject jsonObject=JsonUtil.readObject(s);
        User user=new User(JsonUtil.getAsString(jsonObject,"a"),false);
        user.otherId=JsonUtil.getAsString(jsonObject,"b");
        user.emailId=JsonUtil.getAsString(jsonObject,"c");
        user.name=JsonUtil.getAsString(jsonObject,"d");
        user.avtarId=JsonUtil.getAsInt(jsonObject,"e");
        user.enableProfilePicture=JsonUtil.getAsBoolean(jsonObject,"g");
        user.password=JsonUtil.getAsString(jsonObject,"h");
        user.profilePic=JsonUtil.getAsString(jsonObject, "j");
        return user;
    }
    
    /**
     * Changes the user's email id
     * @param newEmailId - the new email id
     */
    public void changeEmailId(String newEmailId){
        String oldEmailId=this.emailId;
        instanceLock.lock();
        try{
            this.emailId=newEmailId;
            this.modified.set(false);
            this.save(true);
            DataCache.userEmailIdChanged(this, oldEmailId);
            GameDB.emailIdChanged(this, oldEmailId, newEmailId);        
        }finally{instanceLock.unlock();}
    }
    
    /**
     * Chanes a user's other id
     * @param newOtherId - the new other id
     */
    public void changeOtherId(String newOtherId){
        String oldOtherId=this.otherId;
        instanceLock.lock();
        try{
            this.otherId=newOtherId;
            this.modified.set(false);
            this.save(true);
            DataCache.userOtherIdChanged(this, oldOtherId);
            GameDB.otherIdChanged(this, oldOtherId, newOtherId);                
        }finally{instanceLock.unlock();}
    }
    
    void modified(){
        if(this.isBot || this.modified.compareAndSet(false, true)==false)
            return;
        GameDB.modified(this);
    }    
    
    static void setCombineLoginWithRegister(boolean value){
        combineLoginWithRegister=value;
    }
    
    static void registerUserWithOtherId(String otherId,String password,String name,CommandDataSocket socket,GameObj gameObj){
        User user;
        if(otherId==null || otherId.length()<1){
            Session.sendError(socket, ErrorCodes.emptyOtherId, ErrorDescriptions.emptyOtherId);
            return;
        }
            
        staticLock.lock();
        try{
            if(User.getByOtherId(otherId)!=null){
                if(combineLoginWithRegister){
                    loginByOtherId(otherId, password, socket,gameObj);
                    return;
                }
                Session.sendError(socket, ErrorCodes.duplicateOtherId, ErrorDescriptions.duplicateOtherId);
                socket.close();
                return;
            }
            user=User.create(otherId, null, false, name, password,null);
        }finally{
            staticLock.unlock();
        }
        Session session;
        try{            
            session=Session.newSession(user, socket, gameObj);
        }catch(TooManySessionsException ex){
            Session.sendError(socket, ErrorCodes.tooManySessions, ErrorDescriptions.tooManySessions);
            socket.close();
            return;           
        }
        session.onLoggedIn(false);
        if(gameObj!=null)
            session.onGameSelected(true);
        JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
        jsonBuilder.add("userId", user.userId);
        jsonBuilder.add("sessionId", session.getSessionId());
        jsonBuilder.add("avtarId", user.avtarId);
        jsonBuilder.add("name", user.name);
        jsonBuilder.add("gameName", session.getGameName());
        jsonBuilder.add("gameUserData", session.getGameUserDataForClientAsJson());
        session.sendMesg(Commands.loggedIn, jsonBuilder.build().toString());
        
    }
    
    static void registerUserWithEmailId(String emailId,String password,String name,CommandDataSocket socket,GameObj gameObj){
        User user;
        if(emailId==null || emailId.length()<1){
            Session.sendError(socket, ErrorCodes.emptyEmailId, ErrorDescriptions.emptyEmailId);
            return;
        }
            
        if(!Utils.isValidEmailId(emailId)){
            Session.sendError(socket, ErrorCodes.invalidEmailId, ErrorDescriptions.invalidEmailId);
            return;
        }
        staticLock.lock();
        try{
            if(User.getByEmailId(emailId)!=null){
                if(combineLoginWithRegister){
                    loginByEmail(emailId, password, socket,gameObj);
                    return;
                }
                Session.sendError(socket, ErrorCodes.duplicateEmailId, ErrorDescriptions.duplicateEmailId);
                socket.close();
                return;
            }
            user=User.create(null, emailId, false, name, password,null);
        }finally{
            staticLock.unlock();
        }
        Session session;
        try{            
            session=Session.newSession(user, socket, gameObj);
        }catch(TooManySessionsException ex){
            Session.sendError(socket, ErrorCodes.tooManySessions, ErrorDescriptions.tooManySessions);
            socket.close();
            return;           
        }
        session.onLoggedIn(false);
        if(gameObj!=null)
            session.onGameSelected(true);
        JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
        jsonBuilder.add("userId", user.userId);
        jsonBuilder.add("sessionId", session.getSessionId());
        jsonBuilder.add("avtarId", user.avtarId);
        jsonBuilder.add("name", user.name);
        jsonBuilder.add("gameName", session.getGameName());
        jsonBuilder.add("gameUserData", session.getGameUserDataForClientAsJson());
        session.sendMesg(Commands.loggedIn, jsonBuilder.build().toString());
    }
    
    static void loginByEmail(String emailId,String password,CommandDataSocket socket,GameObj gameObj){
        if(emailId==null || password==null){
            Session.sendError(socket, ErrorCodes.invalidLoginData, ErrorDescriptions.invalidLoginData);
            socket.close();
            return;
        }        
        //get user by emailId
        User user=getByEmailId(emailId);
        //if null send error and exit
        if(user==null){
            Session.sendError(socket, ErrorCodes.emailIdAndPasswordDoesNotMatch, ErrorDescriptions.emailIdAndPasswordDoesNotMatch);
            socket.close();
            return;
        }
        //if password does not match then send error and exit
        if(!(user.password!=null && user.password.equals(password))){
            Session.sendError(socket, ErrorCodes.emailIdAndPasswordDoesNotMatch, ErrorDescriptions.emailIdAndPasswordDoesNotMatch);
            socket.close();
            return;           
        }       
        Session session;
        try{            
            session=Session.newSession(user, socket, gameObj);
        }catch(TooManySessionsException ex){
            Session.sendError(socket, ErrorCodes.tooManySessions, ErrorDescriptions.tooManySessions);
            socket.close();
            return;           
        }
        session.onLoggedIn(false);
        if(gameObj!=null)
            session.onGameSelected(session.isGameDataNew());
        JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
        jsonBuilder.add("userId", user.userId);
        jsonBuilder.add("sessionId", session.getSessionId());
        jsonBuilder.add("avtarId", user.avtarId);
        jsonBuilder.add("name", user.name);
        jsonBuilder.add("gameName", session.getGameName());
        jsonBuilder.add("profilePic", user.profilePic);
        jsonBuilder.add("gameUserData", session.getGameUserDataForClientAsJson());
        session.sendMesg(Commands.loggedIn, jsonBuilder.build().toString());
        Room room=session.room;
        if(room!=null)
            room.sendRoomDataToSession(session);
    }
       
    private static PublicKey googlePublicKey1;
    private static PublicKey googlePublicKey2;
    
    static boolean verifyGoogleIdToken(String jwHeader,String jwPayload,String jwSignature) throws Exception{
        if(googlePublicKey2==null)
            fetchGoogleKeys();
        byte[] dataToVerify=(jwHeader + '.' + jwPayload).getBytes();
        byte[] signatureBytes=Base64.decodeBase64(jwSignature);
        boolean result=Crypto.verifySignature(googlePublicKey1, dataToVerify, signatureBytes) || Crypto.verifySignature(googlePublicKey2, dataToVerify, signatureBytes);
        if(result==false)
            fetchGoogleKeys();
        return Crypto.verifySignature(googlePublicKey1, dataToVerify, signatureBytes) || Crypto.verifySignature(googlePublicKey2, dataToVerify, signatureBytes);
    }
    
    static void loginWithGoogle(String jwToken,CommandDataSocket socket,GameObj gameObj) throws Exception{
        //verify hwToken
        String[] splitToken=Utils.split(jwToken, '.');
        if(!verifyGoogleIdToken(splitToken[0],splitToken[1],splitToken[2])){
            Session.sendError(socket, ErrorCodes.invalidLoginData, "idToken not valid");//send error or close socket
            socket.close();
            return;
        }
        JsonObject payloadAsJson=JsonUtil.readObject(new String(Base64.decodeBase64(splitToken[1]),Utils.utf8Charset));
        String profilePic=JsonUtil.getAsString(payloadAsJson, "picture");
        if(profilePic.endsWith("photo.jpg"))
            profilePic=null;
            
        String emailId=JsonUtil.getAsString(payloadAsJson, "email");
        String name=JsonUtil.getAsString(payloadAsJson, "name");
        //get user by emailId
        User user;
        boolean isNew=false;
        staticLock.lock();
        try{
            user=getByEmailId(emailId);
            if(user==null){
                //create User
                user=User.create(null, emailId, false, name, "",profilePic);
                isNew=true;
            }
            else{
                if(profilePic!=null && profilePic.equals(user.profilePic)==false){
                    user.profilePic=profilePic;                    
                    user.modified();
                }                    
            }
        }finally{
            staticLock.unlock();
        }
        Session session;
        try{            
            session=Session.newSession(user, socket, gameObj);
        }catch(TooManySessionsException ex){
            Session.sendError(socket, ErrorCodes.tooManySessions, ErrorDescriptions.tooManySessions);
            socket.close();
            return;           
        }
        session.onLoggedIn(isNew);
        if(gameObj!=null)
            session.onGameSelected(session.isGameDataNew());        
        JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
        jsonBuilder.add("userId", user.userId);
        jsonBuilder.add("sessionId", session.getSessionId());
        jsonBuilder.add("avtarId", user.avtarId);
        jsonBuilder.add("name", user.name);
        jsonBuilder.add("gameName", session.getGameName());
        jsonBuilder.add("profilePic", user.profilePic);
        jsonBuilder.add("gameUserData", session.getGameUserDataForClientAsJson());
        session.sendMesg(Commands.loggedIn, jsonBuilder.build().toString());
        Room room=session.room;
        if(room!=null)
            room.sendRoomDataToSession(session);
    }
       
    static void loginByOtherId(String otherId,String password,CommandDataSocket socket,GameObj gameObj){
        if(otherId==null){
           Session.sendError(socket, ErrorCodes.invalidLoginData, ErrorDescriptions.invalidLoginData);
           socket.close();
           return;            
        }
        User user=getByOtherId(otherId);
        //if null send error and exit
        if(user==null){
            Session.sendError(socket, ErrorCodes.otherIdAndPasswordDoesNotMatch, ErrorDescriptions.otherIdAndPasswordDoesNotMatch);
            socket.close();
            return;
        }
        //if password does not match then send error and exit
        if(!(user.password!=null && user.password.equals(password))){
            Session.sendError(socket, ErrorCodes.otherIdAndPasswordDoesNotMatch, ErrorDescriptions.otherIdAndPasswordDoesNotMatch);
            socket.close();
            return;           
        }       
        Session session;
        try{            
            session=Session.newSession(user, socket, gameObj);
        }catch(TooManySessionsException ex){
            Session.sendError(socket, ErrorCodes.tooManySessions, ErrorDescriptions.tooManySessions);
            socket.close();
            return;           
        }
        session.onLoggedIn(false);
        if(gameObj!=null)
            session.onGameSelected(session.isGameDataNew());
        JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
        jsonBuilder.add("userId", user.userId);
        jsonBuilder.add("sessionId", session.getSessionId());
        jsonBuilder.add("avtarId", user.avtarId);
        jsonBuilder.add("name", user.name);
        jsonBuilder.add("gameName", session.getGameName());
        jsonBuilder.add("profilePic", user.profilePic);
        jsonBuilder.add("gameUserData", session.getGameUserDataForClientAsJson());
        session.sendMesg(Commands.loggedIn, jsonBuilder.build().toString());        
        Room room=session.room;
        if(room!=null)
            room.sendRoomDataToSession(session);
    }
    
    static void loginByBot(String name,int avtarId,CommandDataSocket socket,GameObj gameObj){
        //create new User
        User user=create(null, null, true, name, null,null);
        user.avtarId=avtarId;
        Session session;
        try{            
            session=Session.newSession(user, socket, gameObj);
        }catch(TooManySessionsException ex){
            Session.sendError(socket, ErrorCodes.tooManySessions, ErrorDescriptions.tooManySessions);
            socket.close();
            return;           
        }
        session.onLoggedIn(false);
        JsonObjectBuilder jsonBuilder=JsonUtil.createObjectBuilder();
        jsonBuilder.add("userId", user.userId);
        jsonBuilder.add("sessionId", session.getSessionId());
        jsonBuilder.add("avtarId", user.avtarId);
        jsonBuilder.add("name", user.name);
        jsonBuilder.add("gameName", session.getGameName());
        jsonBuilder.add("profilePic", user.profilePic);
        jsonBuilder.add("gameUserData", session.getGameUserDataForClientAsJson());
        session.sendMesg(Commands.loggedIn, jsonBuilder.build().toString());        
    }
    
    /**
     * Returns the other id of the user
     * @return String - the other id of the user
     */
    public String getOtherId() {
        return otherId;
    }

    /**
     * Returns the email id of the user
     * @return String - the email id of the user
     */
    public String getEmailId() {
        return emailId;
    }

    /**
     * returns the name of the User
     * @return String - the name of the User
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the id of avatar selected the user. 
     * @return String - the avatar id of the user
     */
    public int getAvtarId() {
        return avtarId;
    }

    /**
     * Determines if profile picture is enabled
     * @return boolean - true if the profile picture is enabled for this user
     */
    public boolean isProfilePictureEnabled() {
        return enableProfilePicture;
    }

    /**
     * Returns the user's password
     * @return String - the user's password
     */
    public String getPassword() {
        return password;
    }
    
    static BeanDescriptor descriptor;
    static{
        descriptor=BeanDescriptor.newInstance(User.class);        
    }
    
    Object getValue(String fieldName) throws Exception {
        return descriptor.getValue(this, fieldName);
    }
    
    private static void fetchGoogleKeys() throws Exception{
        HttpClientResponse response=HttpClient.doGet(new URL("https://www.googleapis.com/oauth2/v1/certs"), null);
        JsonObject jsonObject=JsonUtil.readObject(response.body);
        String certificate1=null;
        String certificate2=null;
        for(JsonValue member:jsonObject.values()){
            if(certificate1==null)
                certificate1=JsonUtil.getValueAsString(member);
            else {
                certificate2=JsonUtil.getValueAsString(member);
                break;
            }
        }
        if(certificate1!=null)
            googlePublicKey1=Crypto.loadCertificate(new ByteArrayInputStream(certificate1.getBytes()), "X.509").getPublicKey();
        if(certificate2!=null)
            googlePublicKey2=Crypto.loadCertificate(new ByteArrayInputStream(certificate2.getBytes()), "X.509").getPublicKey();
    }
    
    static final UserFieldGetter userFieldGetter=new UserFieldGetter();
    private static class UserFieldGetter implements FieldGetter{

        @Override
        public Object getValue(Object obj, String fieldName) throws Exception {
            return ((User)obj).getValue(fieldName);
        }
        
    }
}
