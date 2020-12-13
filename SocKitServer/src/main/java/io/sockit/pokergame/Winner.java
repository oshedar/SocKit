/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.pokergame;

import io.sockit.sockitserver.JsonUtil;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author h
 */
public class Winner {
    PokerPlayer player;
    int amtWonInPot;
    
    public Winner(PokerPlayer player) {
        this.player = player;
    }
    
    JsonObject toJson(){
        JsonObjectBuilder json=JsonUtil.createObjectBuilder();
        json.add("seatNo", player.seatNo);
        json.add("userId", player.session.getUser().userId);
        json.add("amtWon", amtWonInPot);
        json.add("handCategory", player.winningHandCategory==null?null:Utils.tittleCase(player.winningHandCategory.toString().replace('_', ' ')));
        json.add("winningCards", JsonUtil.toJsonArray(player.winningCards));
        return json.build();
    }
    
    
}
