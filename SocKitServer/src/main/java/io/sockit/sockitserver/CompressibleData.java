/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import javax.json.JsonObject;

/**
 * This is the super interface for User Data that can be saved in the Server database
 */
public interface CompressibleData {
    /**
     * Converts the Data to compressed Json. Called when the Data object is saved to the Game database. For example instead of long descriptive attribute names use short name. Eg. instead of {"firstName":"ashok","lastName":"patil"} you could use {"a":"ashok","b":"patil"}
     * @return JsonObject - the data as compressed Json
     */
    JsonObject toCompressedJson();

    /**
     * Initializes Data from compressed Json - converts compressed Json to Data. Called when the Data object is retrieved from the Game database
     * @param jsonObject - The compressed Json from which the data will be initialized
     */
    void fromCompressedJson(JsonObject jsonObject);
        
    /**
     * Coverts the Data to Json. Called when data needs to be passed to client
     * @return JsonObject - data as Json
     */
    JsonObject toJson();

}
