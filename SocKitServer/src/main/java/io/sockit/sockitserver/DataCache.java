/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import io.sockit.servertools.Cache;

/**
 *
 * @author h
 */
class DataCache {
    static private Cache<String, User> userCache;
    static private int cacheSize;
    static private Cache<String, GameUserData> gameDataCache;
    
    static void init(int cacheSize){
        userCache=Cache.createBoundedCache(cacheSize, true,User.userFieldGetter,"emailId","otherId");
        gameDataCache=Cache.createBoundedCache(cacheSize, true,null);
        DataCache.cacheSize=cacheSize;
    }
    
    static void putUser(User user){
        userCache.put(user.userId, user);
    }        
    
    static User getUserByUserId(String userId){
        return userCache.get(userId);
    }
    
    static boolean userExists(String userId){
        return userCache.containsKey(userId);
    }
    
    static User getUserByOtherId(String otherId){
            return userCache.getWithSecondaryKey("otherId", otherId);
    }
    
    static User getUserByEmailId(String emailId){
        return userCache.getWithSecondaryKey("emailId", emailId);
    }
        
    static void userOtherIdChanged(User user,String oldOtherId){
        userCache.secondaryKeyValueChanged(user.userId, "otherId", oldOtherId, user.otherId);            
    }

    static void userEmailIdChanged(User user,String oldEmailId){
        userCache.secondaryKeyValueChanged(user.userId, "emailId", oldEmailId, user.emailId);
    }
     
    static void putGameData(GameUserData gameUserData){
        gameDataCache.put(gameUserData.gameId+gameUserData.userId, gameUserData);
    }
    
    static GameUserData getGameData(String userId,String gameId){
        return gameDataCache.get(gameId+userId);
    }
}
