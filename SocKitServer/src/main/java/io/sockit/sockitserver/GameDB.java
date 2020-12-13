/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;
import io.sockit.servertools.Utils;
import io.sockit.servertools.BatchDataProcessor;
import io.sockit.servertools.DataProcessor;
import io.sockit.servertools.WebRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.json.JsonObject;
import static io.sockit.sockitserver.Functions.*;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
/**
 *
 * @author h
 */
class GameDB {
    private static final Charset utf_8=Charset.forName("UTF-8");
    private static DataStore db;
    private static final byte[] userPrefix="us".getBytes(utf_8);
    private static final byte[] otherIdPrefix="ot".getBytes(utf_8);
    private static final byte[] emailIdPrefix="em".getBytes(utf_8);
    private static final byte[] privateRoomPrefix="pr".getBytes(utf_8);
    private static final byte[] gameUserDataPrefix="ud".getBytes(utf_8);
    private static final byte[] webAnalyticsPrefix="wa".getBytes(utf_8);
    
    private static int saveInterval=5*60*1000;//4 mins
    private static int maxSaveQSize=500;

    private static class ModifiedDataProcessor implements DataProcessor{

        @Override
        public void process(Object data) {
            ((Persistable)data).save();
        }
        
    }
    private static BatchDataProcessor modifiedDataProcessor=new BatchDataProcessor(new ModifiedDataProcessor(), saveInterval, maxSaveQSize);
    
    static void modified(Persistable persistable){
        modifiedDataProcessor.process(persistable);    
    }
    
    static void destroyModifiedDataProcessor(){
        modifiedDataProcessor.destroy();
    }
    
    static void setDataStore(DataStore dataStore) throws IOException{
        dataStore.open();
        db=dataStore;
    }
    
    static void close(){
        try{
            db.close();
        }catch(Exception ex){Utils.log(ex);}
    }
    
    static void writeNew(User user){
        byte[] key=concat(userPrefix, user.userId.getBytes(utf_8));
        db.put(key, user.convertToString().getBytes(utf_8));
        String otherId=user.otherId;
        if(otherId!=null)
            db.put(concat(otherIdPrefix,otherId.getBytes(utf_8)), key);
        String emailId=user.emailId;
        if(emailId!=null)
            db.put(concat(emailIdPrefix,emailId.getBytes(utf_8)), key);        
    }
    
    static void write(User user){
        byte[] key=concat(userPrefix , user.userId.getBytes(utf_8));
        db.put(key, user.convertToString().getBytes(utf_8));        
    }
    
    static void write(GameUserData gameUserData){
        byte[] key=concat(gameUserDataPrefix , (gameUserData.gameId + gameUserData.userId).getBytes(utf_8));
        String data=gameUserData.data==null?" ":gameUserData.data.toCompressedJson().toString();
        db.put(key, data.getBytes(utf_8));
    }
    
    static String readGameUserData(String userId,String gameId){
        byte[] data=db.get(concat(gameUserDataPrefix ,( gameId + userId).getBytes(utf_8)));
        if(data!=null)
            return new String(data, utf_8);
        return null;
    }
    
    static boolean exists(String userId){
        return db.get(concat(userPrefix , userId.getBytes(utf_8)))!=null;
    }
    
    static void emailIdChanged(User user,String oldEmailId,String newEmailId){
        if(oldEmailId==newEmailId)
            return;
        if(oldEmailId==null){
            db.put(concat(emailIdPrefix,newEmailId.getBytes(utf_8)),concat(userPrefix,user.userId.getBytes(utf_8)));
            return;
        }
        if(oldEmailId.equals(newEmailId))
            return;
        db.delete(concat(emailIdPrefix,oldEmailId.getBytes(utf_8)));
        if(newEmailId!=null)
            db.put(concat(emailIdPrefix,newEmailId.getBytes(utf_8)), concat(userPrefix,user.userId.getBytes(utf_8)));
    }
    
    static void otherIdChanged(User user,String oldOtherId,String newOtherId){
        if(oldOtherId==newOtherId)
            return;
        if(oldOtherId==null){
            db.put(concat(otherIdPrefix,newOtherId.getBytes(utf_8)), concat(userPrefix,user.userId.getBytes(utf_8)));
            return;
        }
        if(oldOtherId.equals(newOtherId))
            return;
        db.delete(concat(otherIdPrefix,oldOtherId.getBytes(utf_8)));
        if(newOtherId!=null)
            db.put(concat(otherIdPrefix,newOtherId.getBytes(utf_8)), concat(userPrefix,user.userId.getBytes(utf_8)));
    }
    
    static User read(String userId){
        byte[] data=db.get(concat(userPrefix,userId.getBytes(utf_8)));
        if(data!=null)
            return User.fromString(new String(data, utf_8));
        return null;
    }
        
    static User readWithOtherId(String otherId){
        byte[] key=db.get(concat(otherIdPrefix,otherId.getBytes(utf_8)));
        if(key!=null){
            byte[] data=db.get(key);
            if(data!=null)
                return User.fromString(new String(data, utf_8));
        }
        return null;
    }
    
    static User readWithEmailId(String emailId){
        byte[] key=db.get(concat(emailIdPrefix,emailId.getBytes(utf_8)));
        if(key!=null){
            byte[] data=db.get(key);
            if(data!=null)
                return User.fromString(new String(data, utf_8));
        }
        return null;
    }
    
    static void savePrivateRoom(String userId,String gameName,JsonObject privateRoomJson){
        byte[] key=concat(privateRoomPrefix,(userId + gameName).getBytes(utf_8));
        db.put(key, privateRoomJson.toString().getBytes(utf_8));
    }
    
    static JsonObject getPrivateRoom(String userId,String gameName){
        byte[] key=concat(privateRoomPrefix,(userId+gameName).getBytes(utf_8));
        byte[] data=db.get(key);
        if(data==null)
            return null;
        return JsonUtil.readObject(new String(data, utf_8));
    }
            
    static void deletePrivateRoom(String userId,String gameName){
        byte[] key=concat(privateRoomPrefix,(userId+gameName).getBytes(utf_8));
        db.delete(key);
    }
     
    private static String today;
    private static long todayMillis;
    private static final int _24Hrs=24 * 60 * 60 * 1000;
    private static final int istDiff=(int)(5.5*60*60*1000);
    private static DateFormat dateFormat;
    private static String getTodayIST(){
        long time = System.currentTimeMillis();
        long istDate=(time - time % _24Hrs) + istDiff;
        if(todayMillis==istDate)
            return today;
        if(dateFormat==null){
            dateFormat=new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
        }
        today=dateFormat.format(new Date(istDate));
        todayMillis=istDate;
        return today;
    }
    static void writeWa(WebRequest webRequest){
        String ip=((InetSocketAddress)webRequest.getRemoteAddress()).getAddress().toString();
        String userAgent=webRequest.getHeaderValue("User-Agent");
        if(userAgent.contains("bot") || userAgent.contains("AWS") || ip.contains("103.69.240.68"))
            return;
        byte[] key=concat(webAnalyticsPrefix,Functions.longToBytes(Functions.hash64((ip+userAgent).getBytes(utf_8))));
        byte[] value=db.get(key);        
        String waData=value==null?getTodayIST() + '\t' + ip+'\t' + userAgent + '\t':new String(value, utf_8);
        waData+=webRequest.getResourcePath() + ";";
        db.put(key, waData.getBytes(utf_8));       
    }
        
    static Iterator<String> getWaRecords(){
        return new WARecordsIterator();
    }
    
    
    private static class WARecordsIterator implements Iterator<String>{
        DataIterator dataIterator;

        public WARecordsIterator() {
            dataIterator = db.iterator();
            dataIterator.seek(webAnalyticsPrefix);
        }
        
        @Override
        public boolean hasNext() {
            if(dataIterator.hasNext()){
                if(keyStartsWith(dataIterator.peekNext().getKey(),webAnalyticsPrefix))
                        return true;
            }
            return false;
        }

        @Override
        public String next() {
            return new String(dataIterator.next().getValue(),utf_8);
        }
        
    }
    
    static boolean keyStartsWith(byte[] key,byte[] prefix){
        if(prefix.length>key.length)
            return false;
        for(int ctr=0;ctr<prefix.length;ctr++){
            if(prefix[ctr]!=key[ctr])
                return false;
        }
        return true;
    }
}
