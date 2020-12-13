/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.pokergame;

import java.util.List;

/**
 *
 * @author Hoshedar Irani
 */
public class Utils {
    public static String tittleCase(String string) {             
        if(isTittleCase(string))
            return string;
        boolean precededBySpace = true;  
        StringBuilder properCase = new StringBuilder();      
        int length=string.length();
        for(int ctr=0;ctr<length;ctr++){        
            int i = string.charAt(ctr);  
              if (i == -1)  break;        
                char c = (char)i;  
                if (c == ' ' || c == '"' || c == '(' || c == '.' || c == '/' || c == '\\' || c == ',') {  
                  properCase.append(c);  
                  precededBySpace = true;  
               } else {  
                  if (precededBySpace) {   
                     properCase.append(Character.toUpperCase(c));  
               } else {   
                     properCase.append(Character.toLowerCase(c));   
               }  
               precededBySpace = false;  
            }  
        }  
           
        return properCase.toString();                 
    }  
    
    public static boolean isTittleCase(String string){
        boolean precededBySpace = true;  
        int length=string.length();
        for(int ctr=0;ctr<length;ctr++){        
            int i = string.charAt(ctr);  
              if (i == -1)  break;        
                char c = (char)i;  
                if (c == ' ' || c == '"' || c == '(' || c == '.' || c == '/' || c == '\\' || c == ',') {  
                  precededBySpace = true;  
                } else {  
                    if (precededBySpace) {   
                        if(Character.isLowerCase(c))
                            return false;
                    }
                    else if(Character.isUpperCase(c))
                        return false;
               precededBySpace = false;  
            }  
        }             
        return true;                         
    }
    
    public static int randomInt(int min,int max){
        if(max<min)
            throw new IllegalArgumentException("max should be >= min");        
        return max==min?max:(int)(Math.random()*(max-min+1)) + min;
    }
    
    public static String toCSV(List list){
        int size=list.size();
        if(size<1)
            return "";
        StringBuilder sb=new StringBuilder();
        int ctr=1;        
        for(Object o:list){
            sb.append(o);
            if(ctr<size)
                sb.append(',');
            ctr++;
        }
        return sb.toString();        
    }
    
    public static String toCSV(Object[] array,int from, int to){
        if(to>=array.length)
            to=array.length-1;
        if(from>to || array.length<1)
            return "";
        StringBuilder sb=new StringBuilder();
        int ctr;
        for(ctr=from;ctr<to;ctr++)
            sb.append(array[ctr]).append(',');
        sb.append(array[ctr]);
        return sb.toString();        
    }

    public static String toCSV(Object[] array,int from){
        return toCSV(array, from, array.length-1);
    }
    
    public static String toCSV(int[] array){
        if(array.length<1)
            return "";
        StringBuilder sb=new StringBuilder();
        int ctr;
        for(ctr=0;ctr<array.length-1;ctr++)
            sb.append(array[ctr]).append(',');
        sb.append(array[ctr]);
        return sb.toString();        
    }

    public static int getRandom(int[] values){
        return values[(int)(Math.random()*values.length)];
    }
    
}
