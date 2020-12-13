/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import io.sockit.servertools.Utils;
import javax.json.JsonArray;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.json.Json;
import javax.json.JsonArrayBuilder;

/**
 *
 * A container for Game rooms. A set of rooms can be grouped into a location
 */
public class Location {

    /**
     * The name of the location
     */
    public final String name;

    /**
     * The game to which the location belongs
     */
    public final Game game;
    private ConcurrentSkipListSet<Room> fastRooms=new ConcurrentSkipListSet(Room.roomComparator);
    private ConcurrentSkipListSet<Room> normalRooms=new ConcurrentSkipListSet(Room.roomComparator);
    private Collection<Room> unmodifiableNormalRooms;
    private Collection<Room> unmodifiableFastRooms;
    private boolean destroyed=false;
    private boolean beingDestroyed=false;
    private int noOfRooms;    
    final Lock instanceLock=new ReentrantLock();
    
    Location(String name,Game roomFactory) {
        this.name = Utils.tittleCase(name);
        this.game=roomFactory;
        this.unmodifiableFastRooms=Collections.unmodifiableCollection(fastRooms);
        this.unmodifiableNormalRooms=Collections.unmodifiableCollection(normalRooms);
    }
    
    /**
     * returns the first room of the given type in this location
     * @param type - the type of the room 
     * @return Room - the first room of the given type in this location
     */
    public final Room getFirstRoom(RoomType type){
        if(type==RoomType.normal)
            return normalRooms.first();
        return fastRooms.first();
    }
    
    final void destroy(){
        Collection<Room> fastRooms;
        Collection<Room> normalRooms;
        instanceLock.lock();
        try{
            if(destroyed)
                return;
            beingDestroyed=true;
            fastRooms=getRooms(RoomType.fast);
            normalRooms=getRooms(RoomType.normal);
        }finally{instanceLock.unlock();}
        for(Room room:fastRooms)
            room.destroy();
        for(Room room:normalRooms)
            room.destroy();
        instanceLock.lock();
        try{
            this.normalRooms.clear();
            this.fastRooms.clear();
            this.fastRooms=null;
            this.normalRooms=null;
            noOfRooms=0;
            destroyed=true;
        }finally{instanceLock.unlock();}        
    }
    
    /**
     * adds a room to this location
     * @param room - the room to add
     * @return boolean - true if room added successfully else false
     */
    public final boolean addRoom(Room room){
        instanceLock.lock();
        try{
            if(beingDestroyed)
                return false;
            List<Room> roomsInMaster;
            room.location=this;
            if(room.roomType==RoomType.fast){
                fastRooms.add(room);
            }
            else{
                normalRooms.add(room);
            }
            Room.register(room);
            noOfRooms++;
            return true;
        }finally{instanceLock.unlock();}        
    }
    
    final boolean removeRoom(Room room){
        instanceLock.lock();
        try{
            if(destroyed)
                return false;
            boolean result;
            if(room.roomType==RoomType.fast){
                result=fastRooms.remove(room);
            }
            else{
                result=normalRooms.remove(room);
            }
            if(result)
                noOfRooms--;
            return result;
        }finally{instanceLock.unlock();}
    }
    
    JsonArray getRoomsAsJson(RoomType roomType){
        Collection<Room> rooms=null;
        instanceLock.lock();
        try{
            if(destroyed)
                return null;
            if(roomType==RoomType.normal){
                rooms=unmodifiableNormalRooms;
            }
            else{
                rooms=unmodifiableFastRooms;
            }
        }finally{instanceLock.unlock();}
        JsonArrayBuilder jsonRooms=JsonUtil.createArrayBuilder();
        for(Room room:rooms)
            jsonRooms.add(room.toShortJson());
        return jsonRooms.build();
    }
    
    /**
     * returns all the rooms of the given type in this location. Override the compareTo method of the Room class to set the order of the rooms
     * @param roomType - the room type
     * @return Collection&lt;Room&gt; -  all the rooms of the given type in this location
     */
    public final Collection<Room> getRooms(RoomType roomType){
        instanceLock.lock();
        try{
            if(destroyed)
                return null;
            if(roomType==RoomType.normal){
                return unmodifiableNormalRooms;
            }
            else{
                return unmodifiableFastRooms;
            }
        }finally{instanceLock.unlock();}
    }
    
    /**
     * returns the total number of rooms in this location
     * @return int - the total number of rooms in this location
     */
    public final int noOfRooms(){
        instanceLock.lock();
        try{
            return noOfRooms;
        }finally{instanceLock.unlock();}
    }
    
    /**
     * returns the total number of rooms of the given type in this location
     * @param roomType - the room type
     * @return int - the total number of rooms of the given type in this location
     */
    public final int noOfRooms(RoomType roomType){
        instanceLock.lock();
        try{
            if(destroyed)
                return 0; 
            if(roomType==null)
                return noOfRooms;
            switch(roomType){
                case normal: return normalRooms.size();
                case fast: return fastRooms.size();                    
            }
            return 0;
        }finally{instanceLock.unlock();}        
    }
    
    /**
     * returns the total number of spectators and players in the location
     * @return int - the total number of spectators and players in the location
     */
    public final int noOfSpectatorsAndPlayers(){
        int sum=0;
        instanceLock.lock();
        try{
            if(destroyed)
                return 0;
            for(Room room:fastRooms)
                sum+=room.playerCount() + room.spectatorCount();            
            for(Room room:normalRooms)
                sum+=room.playerCount() + room.spectatorCount();            
        }finally{instanceLock.unlock();}
        return sum;    
    }
    
    /**
     * returns the total number of players in the location
     * @return int - the total number of players in the location
     */
    public final int noOfPlayers(){
        int sum=0;
        instanceLock.lock();
        try{
            if(destroyed)
                return 0;
            for(Room room:fastRooms)
                sum+=room.playerCount();           
            for(Room room:normalRooms)
                sum+=room.playerCount();            
        }finally{instanceLock.unlock();}
        return sum;    
    }

    /**
     * returns the total number of spectators in the location
     * @return int - the total number of spectators in the location
     */
    public final int noOfSpectators(){
        int sum=0;
        instanceLock.lock();
        try{
            if(destroyed)
                return 0;
            for(Room room:fastRooms)
                sum+=room.spectatorCount();            
            for(Room room:normalRooms)
                sum+=room.spectatorCount();            
        }finally{instanceLock.unlock();}
        return sum;    
    }

    /**
     * returns the total number of spectators and players of the given room type in the location
     * @param roomtype - the room type
     * @return int - the total number of spectators and players of the given room type in the location
     */
    public final int noOfSpectatorsAndPlayers(RoomType roomtype){
        int sum=0;
        instanceLock.lock();
        try{
            if(destroyed)
                return 0;
            if(roomtype==RoomType.fast)
                for(Room room:fastRooms)
                    sum+=room.playerCount() + room.spectatorCount();
            else
                for(Room room:normalRooms)
                    sum+=room.playerCount() + room.spectatorCount();            
        }finally{instanceLock.unlock();}
        return sum;    
    }
    
    /**
     * returns the total number of players of the given room type in the location
     * @param roomtype - the room type
     * @return int - the total number of players of the given room type in the location
     */
    public final int noOfPlayers(RoomType roomtype){
        int sum=0;
        instanceLock.lock();
        try{
            if(destroyed)
                return 0;
            if(roomtype==RoomType.fast)
                for(Room room:fastRooms)
                    sum+=room.playerCount();           
            else
                for(Room room:normalRooms)
                    sum+=room.playerCount();            
        }finally{instanceLock.unlock();}
        return sum;    
    }

    /**
     * returns the total number of spectators of the given room type in the location
     * @param roomtype - the room type
     * @return int - the total number of spectators of the given room type in the location
     */
    public final int noOfSpectators(RoomType roomtype){
        int sum=0;
        instanceLock.lock();
        try{
            if(destroyed)
                return 0;
            if(roomtype==RoomType.fast)
                for(Room room:fastRooms)
                    sum+=room.spectatorCount();            
            else
                for(Room room:normalRooms)
                    sum+=room.spectatorCount();            
        }finally{instanceLock.unlock();}
        return sum;    
    }
    
    /**
     * returns whether room is destroyed or not
     * @return boolean - true if room is destroyed else false
     */
    public boolean isDestroyed(){
        instanceLock.lock();
        try{
            return destroyed;
        }finally{instanceLock.unlock();}
    }
}
