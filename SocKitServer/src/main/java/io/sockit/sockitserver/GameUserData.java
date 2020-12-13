/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import javax.json.JsonObject;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * The Super class for a User Data (specific to a Game) that should persist across sessions and needs be saved to the Game database. For example for a Game such as Poker The UserData could have a field to store the  user's Poker Chips value. Another Field could be Highest Score. These fields can be defined in the GameUserData class. 
 */
final class GameUserData extends Persistable {
    private AtomicBoolean modified=new AtomicBoolean(false);
    boolean isNew;
    String userId;
    String gameId;
    CompressibleData data;
   
    void modified(){
        if(!this.modified.compareAndSet(false, true))
            return;
        GameDB.modified(this);
    }    
    
    void save(){
        if(!modified.compareAndSet(true, false))
            return;
        GameDB.write(this);            
    }
            
    
}
