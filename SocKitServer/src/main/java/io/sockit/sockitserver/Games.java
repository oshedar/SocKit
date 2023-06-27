/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Games {
    
    private static volatile boolean serverStarted=false;
    private static final Lock staticLock=new ReentrantLock();
    
    private final static Map<String,GameObj> games=new ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER);
    private final static Map<String,GameObj> gameIdMap=new ConcurrentSkipListMap();
    
     static final void registerNewGame(Game game) throws DuplicateGameNameHashException{
        staticLock.lock();
        try{
            GameObj newGameObj=new GameObj(game);
            if(gameIdMap.containsKey(newGameObj.id))
                throw new DuplicateGameNameHashException(newGameObj.name);
            gameIdMap.put(newGameObj.id, newGameObj);
            games.put(newGameObj.name, newGameObj);
            if(!serverStarted)
                return;
            newGameObj.game.setUpGame();
        }finally{staticLock.unlock();}        
        
    }
    
    static void serverStarted(){        
        staticLock.lock();
        try{
            if(serverStarted)
                return;
            Collection<GameObj> games=Games.games.values();
            for(GameObj gameObj:games){
                gameObj.game.setUpGame();
            }
            serverStarted=true;
        }finally{
            staticLock.unlock();
        }
    }
    
    static GameObj getGame(String gameName) {
        if(gameName==null)
            return null;
        return games.get(gameName);
  }

  static final void unregisterGame(String gameName) {
    staticLock.lock();
    try {
      GameObj gameObj = games.remove(gameName);
      if (gameObj != null) {
        gameIdMap.remove(gameObj.id);
      }
    } finally {
      staticLock.unlock();
    }

  }
}
