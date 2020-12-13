/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.pokergame;

import javax.json.Json;
import javax.json.JsonObject;
import io.sockit.sockitserver.CompressibleData;
import io.sockit.sockitserver.JsonUtil;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author h
 */
public class PokerUserData implements CompressibleData{
    int chipsInHand;

    @Override
    public JsonObject toCompressedJson() {
        JsonObjectBuilder json=JsonUtil.createObjectBuilder();
        json.add("a", chipsInHand);
        return json.build();
    }

    @Override
    public void fromCompressedJson(JsonObject jsonObject) {
        chipsInHand=JsonUtil.getAsInt(jsonObject, "a", -1);
    }

    @Override
    public JsonObject toJson() {
        JsonObjectBuilder json=JsonUtil.createObjectBuilder();
        json.add("chipsInHand", chipsInHand);
        return json.build();
    }

}
