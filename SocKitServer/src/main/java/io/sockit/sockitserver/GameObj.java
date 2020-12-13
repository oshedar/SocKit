/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import io.sockit.servertools.Utils;
import io.sockit.servertools.Base64;
import javax.json.JsonObject;

/**
 *
 * @author Hoshedar Irani
 */
class GameObj {
    final String id;
    final String name;
    final Game game;

    GameObj(Game game) {
        this.game = game;
        this.name=game.gameName;
        this.id=Base64.encodeBase64String(Utils.intToBytes(name.toUpperCase().hashCode()));
    }
    
    GameUserData getGameUserData(String userId){
        GameUserData gameUserData=DataCache.getGameData(userId, id);
        if(gameUserData!=null){
            gameUserData.isNew=false;
            return gameUserData;
        }
        String data=GameDB.readGameUserData(userId, id);
        if(data!=null){
            gameUserData=new GameUserData();
            if(data.equals(" "))
                gameUserData.data=null;
            else {
                gameUserData.data=game.newGameUserData();
                gameUserData.data.fromCompressedJson(JsonUtil.readObject(data));
            }
            gameUserData.gameId=this.id;
            gameUserData.userId=userId;
            gameUserData.isNew=false;
            DataCache.putGameData(gameUserData);
            return gameUserData;
        }
        gameUserData=new GameUserData();
        gameUserData.data=game.newGameUserData();
        gameUserData.gameId=this.id;
        gameUserData.userId=userId;
        gameUserData.save();
        DataCache.putGameData(gameUserData);
        gameUserData.isNew=true;        
        return gameUserData;
    }
    
}
