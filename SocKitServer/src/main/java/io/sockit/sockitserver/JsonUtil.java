/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * This class contains convenience/utility methods for the java Json processing api.
 */
public class JsonUtil {
    
    static final JsonObject EmptyJsonObject=JsonObject.EMPTY_JSON_OBJECT;
    static final JsonBuilderFactory BUILDER_FACTORY=Json.createBuilderFactory(null);
    
    /**
     * Returns the json value as a String
     * @param jsonValue - the json value
     * @return String - the json value as a String
     */
    public static String getValueAsString(JsonValue jsonValue){
        if(jsonValue==null)
            return null;
        if(jsonValue.getValueType()==JsonValue.ValueType.NULL)
            return null;
        if(jsonValue.getValueType()==JsonValue.ValueType.STRING)
            return ((JsonString)jsonValue).getString();
        return jsonValue.toString();
            
    }
    
    /**
     * Returns the value of the specified property in the jsonObject as a String or null if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @return String - the value of the specified property in the jsonObject 
     */
    public static String getAsString(JsonObject jsonObject,String property){
        return getAsString(jsonObject, property, null);
    }

    /**
     * Returns the value of the specified property in the jsonObject as a String or defaultValue if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @param defaultValue - the value to be returned if the property does not exist
     * @return String - the value of the specified property in the jsonObject 
     */
    public static String getAsString(JsonObject jsonObject,String property,String defaultValue){
        JsonValue jsonValue=jsonObject.get(property);
        if(jsonValue==null)
            return defaultValue;
        if(jsonValue.getValueType()==JsonValue.ValueType.NULL)
            return null;
        if(jsonValue.getValueType()==JsonValue.ValueType.STRING)
            return ((JsonString)jsonValue).getString();
        return jsonValue.toString();
    }
    
    /**
     * Returns the value of the specified property in the jsonObject as an int or 0 if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @return int - the value of the specified property in the jsonObject as an int
     */
    public static int getAsInt(JsonObject jsonObject,String property){
        return getAsInt(jsonObject, property, 0);
    }
    
    /**
     * Returns the value of the specified property in the jsonObject as an int or defaultValue if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @param defaultValue - the value to be returned if the property does not exist
     * @return int - the value of the specified property in the jsonObject as an int
     */
    public static int getAsInt(JsonObject jsonObject,String property,int defaultValue){
        JsonValue jsonValue=jsonObject.get(property);
        if(jsonValue==null || jsonValue.getValueType()==JsonValue.ValueType.NULL)
            return defaultValue;
        if(jsonValue.getValueType()==JsonValue.ValueType.NUMBER)
            return ((JsonNumber)jsonValue).intValue();
        if(jsonValue.getValueType()==JsonValue.ValueType.STRING)
            return Integer.parseInt(((JsonString)jsonValue).getString());
        return defaultValue;
    }
    
    /**
     * Returns the value of the specified property in the jsonObject as a long or 0 if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @return long - the value of the specified property in the jsonObject as a long
     */
    public static long getAsLong(JsonObject jsonObject,String property){
        return getAsLong(jsonObject, property, 0l);
    }
    
    /**
     * Returns the value of the specified property in the jsonObject as a long or defaultValue if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @param defaultValue - the value to be returned if the property does not exist
     * @return long - the value of the specified property in the jsonObject as a long
     */
    public static long getAsLong(JsonObject jsonObject,String property,long defaultValue){
        JsonValue jsonValue=jsonObject.get(property);
        if(jsonValue==null || jsonValue.getValueType()==JsonValue.ValueType.NULL)
            return defaultValue;
        if(jsonValue.getValueType()==JsonValue.ValueType.NUMBER)
            return ((JsonNumber)jsonValue).longValue();
        if(jsonValue.getValueType()==JsonValue.ValueType.STRING)
            return Long.parseLong(((JsonString)jsonValue).getString());
        return defaultValue;
    }
    
    /**
     * Returns the value of the specified property in the jsonObject as a float or 0 if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @return float - the value of the specified property in the jsonObject as a float
     */
    public static float getAsFloat(JsonObject jsonObject,String property){
        return getAsFloat(jsonObject, property, 0f);
    }
    
    /**
     * Returns the value of the specified property in the jsonObject as a float or defaultValue if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @param defaultValue - the value to be returned if the property does not exist
     * @return float - the value of the specified property in the jsonObject as a float
     */
    public static float getAsFloat(JsonObject jsonObject,String property,float defaultValue){
        JsonValue jsonValue=jsonObject.get(property);
        if(jsonValue==null || jsonValue.getValueType()==JsonValue.ValueType.NULL)
            return defaultValue;
        if(jsonValue.getValueType()==JsonValue.ValueType.NUMBER)
            return (float)((JsonNumber)jsonValue).doubleValue();
        if(jsonValue.getValueType()==JsonValue.ValueType.STRING)
            return Float.parseFloat(((JsonString)jsonValue).getString());
        return defaultValue;
    }
    
    /**
     * Returns the value of the specified property in the jsonObject as a double or 0 if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @return double - the value of the specified property in the jsonObject as a double
     */
    public static double getAsDouble(JsonObject jsonObject,String property){
        return getAsDouble(jsonObject, property, 0.0);
    }
    
    /**
     * Returns the value of the specified property in the jsonObject as a double or defaultValue if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @param defaultValue - the value to be returned if the property does not exist
     * @return double - the value of the specified property in the jsonObject as a double
     */
    public static double getAsDouble(JsonObject jsonObject,String property,double defaultValue){
        JsonValue jsonValue=jsonObject.get(property);
        if(jsonValue==null || jsonValue.getValueType()==JsonValue.ValueType.NULL)
            return defaultValue;
        if(jsonValue.getValueType()==JsonValue.ValueType.NUMBER)
            return ((JsonNumber)jsonValue).doubleValue();
        if(jsonValue.getValueType()==JsonValue.ValueType.STRING)
            return Double.parseDouble(((JsonString)jsonValue).getString());
        return defaultValue;
    }
    
    /**
     * Returns the value of the specified property in the jsonObject as a boolean or false if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @return boolean - the value of the specified property in the jsonObject as a boolean
     */
    public static boolean getAsBoolean(JsonObject jsonObject,String property){
        return getAsBoolean(jsonObject, property, false);
    }
    
    /**
     * Returns the value of the specified property in the jsonObject as a boolean or defaultValue if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @param defaultValue - the value to be returned if the property does not exist
     * @return boolean - the value of the specified property in the jsonObject as a boolean
     */
    public static boolean getAsBoolean(JsonObject jsonObject,String property,boolean defaultValue){
        JsonValue jsonValue=jsonObject.get(property);
        if(jsonValue==null || jsonValue.getValueType()==JsonValue.ValueType.NULL)
            return defaultValue;
        if(jsonValue.getValueType()==JsonValue.ValueType.FALSE)
            return false;
        if(jsonValue.getValueType()==JsonValue.ValueType.TRUE)
            return true;        
        if(jsonValue.getValueType()==JsonValue.ValueType.STRING)
            return Boolean.parseBoolean(((JsonString)jsonValue).getString());
        return defaultValue;
    }
    
    /**
     * Returns the value of the specified property in the jsonObject as a short or 0 if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @return short - the value of the specified property in the jsonObject as a short
     */
    public static short getAsShort(JsonObject jsonObject,String property){
        return getAsShort(jsonObject, property, (short)0);
    }
    
    /**
     * Returns the value of the specified property in the jsonObject as a short or defaultValue if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @param defaultValue - the value to be returned if the property does not exist
     * @return short - the value of the specified property in the jsonObject as a short
     */
    public static short getAsShort(JsonObject jsonObject,String property,short defaultValue){
        JsonValue jsonValue=jsonObject.get(property);
        if(jsonValue==null || jsonValue.getValueType()==JsonValue.ValueType.NULL)
            return defaultValue;
        if(jsonValue.getValueType()==JsonValue.ValueType.NUMBER)
            return (short)((JsonNumber)jsonValue).intValue();;
        if(jsonValue.getValueType()==JsonValue.ValueType.STRING)
            return Short.parseShort(((JsonString)jsonValue).getString());
        return defaultValue;
    }
            
    /**
     * Returns the value of the specified property in the jsonObject as a jsonObject or null if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @return JsonObject - the value of the specified property in the jsonObject as a JsonObject
     */
    public static JsonObject getAsJsonObject(JsonObject jsonObject,String property){
        JsonValue jsonValue=jsonObject.get(property);
        if(jsonValue==null || jsonValue.getValueType()==JsonValue.ValueType.NULL || jsonValue.getValueType()!=JsonValue.ValueType.OBJECT)
            return null;
        return (JsonObject)jsonValue;
    }
    
    /**
     * Returns the value of the specified property in the jsonObject as a jsonArray or null if the property does not exist.
     * @param jsonObject - the jsonObject whose property value is to be returned
     * @param property - the property whose value is to be returned
     * @return JsonArray - the value of the specified property in the jsonObject as a JsonArray
     */
    public static JsonArray getAsJsonArray(JsonObject jsonObject,String property){
        JsonValue jsonValue=jsonObject.get(property);
        if(jsonValue==null || jsonValue.getValueType()==JsonValue.ValueType.NULL || jsonValue.getValueType()!=JsonValue.ValueType.ARRAY)
            return null;
        return (JsonArray)jsonValue;
    }
    
    /**
     * Creates a JsonArray from the specified Collection 
     * @param collection - the collection from which the JsonArray will be created
     * @return JsonArray - a JsonArray based on the specified Collection
     */
    public static JsonArray toJsonArray(Collection collection){
        if(collection==null)
            return null;
        JsonArrayBuilder jsonArrayBuilder=Json.createArrayBuilder();
        for(Object o:collection){
            if(o==null)
                jsonArrayBuilder.addNull();
            else
                jsonArrayBuilder.add(o.toString());
        }
        return jsonArrayBuilder.build();
    }
    
    static boolean isBoolean(JsonValue.ValueType valueType){
        return valueType==JsonValue.ValueType.FALSE || valueType==JsonValue.ValueType.TRUE;
    }
    
    /**
     * Parses the specified json String and creates a JsonObject
     * @param jsonString - the json string to be parsed
     * @return JsonObject - the parsed json String as a JsonObject
     */
    public static JsonObject readObject(String jsonString){
        return Json.createReader(new StringReader(jsonString)).readObject();
    }
    
    /**
     * Parses the specified json String and creates a JsonArray
     * @param jsonString - the json string to be parsed
     * @return JsonArray - the parsed json String as a JsonArray
     */
    public static JsonArray readArray(String jsonString){
        return Json.createReader(new StringReader(jsonString)).readArray();
    }

    private static class ObjectBuilder implements JsonObjectBuilder{
        JsonObjectBuilder json;

        ObjectBuilder() {
            json=BUILDER_FACTORY.createObjectBuilder();
        }

        ObjectBuilder(JsonObject jsonObject) {
            json=BUILDER_FACTORY.createObjectBuilder(jsonObject);
        }

        @Override
        public JsonObjectBuilder add(String name, JsonValue value) {
            if(value==null)
                json.addNull(name);
            else
                json.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, String value) {
            if(value==null)
                json.addNull(name);
            else
                json.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, BigInteger value) {
            if(value==null)
                json.addNull(name);
            else
                json.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, BigDecimal value) {
            if(value==null)
                json.addNull(name);
            else
                json.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, int value) {
            json.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, long value) {
            json.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, double value) {
            json.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, boolean value) {
            json.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder addNull(String name) {
            json.addNull(name);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, JsonObjectBuilder builder) {
            json.add(name, builder);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, JsonArrayBuilder builder) {
            json.add(name, builder);
            return this;
        }

        @Override
        public JsonObject build() {
            return json.build();
        }        
    }
    
    private static class ArrayBuilder implements JsonArrayBuilder{
        JsonArrayBuilder json;

        ArrayBuilder() {
            json=BUILDER_FACTORY.createArrayBuilder();
        }
        
        ArrayBuilder(JsonArray array) {
            json=BUILDER_FACTORY.createArrayBuilder(array);
        }
        
        ArrayBuilder(Collection<?> collection) {
            json=BUILDER_FACTORY.createArrayBuilder(collection);
        }
        
        @Override
        public JsonArrayBuilder add(JsonValue value) {
            if(value==null)
                json.addNull();
            else
                json.add(value);
            return this;
        }

        @Override
        public JsonArrayBuilder add(String value) {
            if(value==null)
                json.addNull();
            else
                json.add(value);
            return this;
        }

        @Override
        public JsonArrayBuilder add(BigDecimal value) {
            if(value==null)
                json.addNull();
            else
                json.add(value);
            return this;
        }

        @Override
        public JsonArrayBuilder add(BigInteger value) {
            if(value==null)
                json.addNull();
            else
                json.add(value);
            return this;
        }

        @Override
        public JsonArrayBuilder add(int value) {
            json.add(value);
            return this;
        }

        @Override
        public JsonArrayBuilder add(long value) {
            json.add(value);
            return this;
        }

        @Override
        public JsonArrayBuilder add(double value) {
            json.add(value);
            return this;
        }

        @Override
        public JsonArrayBuilder add(boolean value) {
            json.add(value);
            return this;
        }

        @Override
        public JsonArrayBuilder addNull() {
            json.addNull();
            return this;
        }

        @Override
        public JsonArrayBuilder add(JsonObjectBuilder builder) {
            if(builder==null)
                json.addNull();
            else
                json.add(builder);
            return this;
        }

        @Override
        public JsonArrayBuilder add(JsonArrayBuilder builder) {
            if(builder==null)
                json.addNull();
            else
                json.add(builder);
            return this;
        }

        @Override
        public JsonArray build() {
            return json.build();
        }        
    }
    
    /**
     * Creates a JsonObjectBuilder that handles nulls gracefully
     * @return JsonObjectBuilder - a JsonObjectBuilder that handles nulls gracefully
     */
    public static JsonObjectBuilder createObjectBuilder(){
        return new ObjectBuilder();
    }
    
    /**
     * Creates a JsonObjectBuilder that handles nulls gracefully
     * @param jsonObject - the initial object in the builder
     * @return JsonObjectBuilder - a JsonObjectBuilder that handles nulls gracefully
     */
   public static JsonObjectBuilder createObjectBuilder(JsonObject jsonObject){
        return new ObjectBuilder(jsonObject);
    }
    
    /**
     * Creates a JsonArrayBuilder  that handles nulls gracefully
     * @return JsonArrayBuilder - a JsonArrayBuilder that handles nulls gracefully
     */
    public static JsonArrayBuilder createArrayBuilder(){
        return new ArrayBuilder();
    }

    /**
     * Creates a JsonArrayBuilder  that handles nulls gracefully
     * @param array - the initial array in the builder
     * @return JsonArrayBuilder - a JsonArrayBuilder that handles nulls gracefully
     */
    public static JsonArrayBuilder createArrayBuilder(JsonArray array){
        return new ArrayBuilder(array);
    }

    /**
     * Creates a JsonArrayBuilder  that handles nulls gracefully
     * @param collection - the initial data for the builder
     * @return JsonArrayBuilder - a JsonArrayBuilder that handles nulls gracefully
     */
    public static JsonArrayBuilder createArrayBuilder(Collection<?> collection){
        return new ArrayBuilder(collection);
    }
}
