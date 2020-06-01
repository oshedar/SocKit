/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 *
 * @author h
 */
public class Utils {
    public static final Charset utf8Charset=Charset.forName("UTF-8");
    private static Logger logger;
    private static Throwable lastLoggedException=null; 
    public static void log(String mesg){
        try{
            if(logger==null)
                Console.log(mesg);
            else
                logger.log(mesg);                
        }catch(Exception ex){ex.printStackTrace();}
    }
    
    public static void log(Throwable ex){
        try{
            if(ex==null || lastLoggedException==ex)
                return;
            if(logger==null)
                Console.log(ex);
            else
                logger.log(ex);
            lastLoggedException=ex;
        }catch(Exception ex2){ex2.printStackTrace();}
}
    
    public static void setLogger(Logger logger) {
        Utils.logger = logger;
    }
    
    public static void destroyLogger(){
        Logger logger=Utils.logger;
        if(logger!=null)
            logger.destroy();
    }
    
    public static int toInt(byte[] bytes){ // converts 4 bytes to int. big endian - for c# (little endian) reverse the bytes
        return (((bytes[0]&0xFF)<<24) |  ((bytes[1]&0xFF)<<16) | ((bytes[2]&0xFF)<<8) | ((bytes[3]&0xFF)) ) ;
    }
    
    public static byte[] intToBytes(int value){// converts int to 4 bytes. big endian - for c# (little endian) reverse the bytes
        byte[] bytes=new byte[4];
        bytes[3]=(byte)(value & 0xFF);
        bytes[2]=(byte)((value>>>8) & 0xFF);
        bytes[1]=(byte)((value>>>16) & 0xFF);
        bytes[0]=(byte)((value>>>24) & 0xFF);
        return bytes;
    }

    public static long toLong(byte[] bytes){ // converts 8 bytes to long. big endian - for c# (little endian) reverse the bytes
        return (((bytes[0]&0xFFl)<<56) |  ((bytes[1]&0xFFl)<<48) | ((bytes[2]&0xFFl)<<40) | ((bytes[3]&0xFFl)<<32) | ((bytes[4]&0xFFl)<<24) |  ((bytes[5]&0xFFl)<<16) | ((bytes[6]&0xFFl)<<8) | ((bytes[7]&0xFFl)) ) ;
    }
    
    public static byte[] longToBytes(long value){// converts long to 8 bytes. big endian - for c# (little endian) reverse the bytes
        byte[] bytes=new byte[8];
        bytes[7]=(byte)(value & 0xFF);
        bytes[6]=(byte)((value>>>8) & 0xFF);
        bytes[5]=(byte)((value>>>16) & 0xFF);
        bytes[4]=(byte)((value>>>24) & 0xFF);
        bytes[3]=(byte)((value>>>32) & 0xFF);
        bytes[2]=(byte)((value>>>40) & 0xFF);
        bytes[1]=(byte)((value>>>48) & 0xFF);
        bytes[0]=(byte)((value>>>56) & 0xFF);
        return bytes;
    }

    public static boolean contains(Object[] array,Object value){
        return indexOf(array, value)!=-1;
    }
    
    public static int indexOf(Object[] array,Object value){
        for(int ctr=0;ctr<array.length;ctr++){
            if(array[ctr]==value)
                return ctr;
        }
        if(value==null)
            return -1;
        for(int ctr=0;ctr<array.length;ctr++){
            if(value.equals(array[ctr]))
                return ctr;
        }
        return -1;
    }
    
    public static Object[] removeElement(Object[] array,Object value){
        int index=indexOf(array, value);
        if(index==-1)
            return Arrays.copyOf(array, array.length);
        Object[] array2;
        array2=new Object[array.length-1];
        for(int ctr=0;ctr<index;ctr++)
            array2[ctr]=array[ctr];
        for(int ctr=index;ctr<array2.length;ctr++)
            array2[ctr]=array[ctr+1];
        return array2;        
    }

    public static String[] removeElement(String[] array,String value){
        int index=indexOf(array, value);
        if(index==-1)
            return Arrays.copyOf(array, array.length);
        String[] array2;
        array2=new String[array.length-1];
        for(int ctr=0;ctr<index;ctr++)
            array2[ctr]=array[ctr];
        for(int ctr=index;ctr<array2.length;ctr++)
            array2[ctr]=array[ctr+1];
        return array2;        
    }
    
    public static String toCSV(Object[] array){
        if(array.length<1)
            return "";
        StringBuilder sb=new StringBuilder();
        int ctr;
        for(ctr=0;ctr<array.length-1;ctr++)
            sb.append(array[ctr]).append(',');
        sb.append(array[ctr]);
        return sb.toString();
        
    }
    
    public static String toLines(Object[] array){
        if(array.length<1)
            return "";
        StringBuilder sb=new StringBuilder();
        int ctr;
        for(ctr=0;ctr<array.length-1;ctr++)
            sb.append(array[ctr]).append('\n');
        sb.append(array[ctr]);
        return sb.toString();
        
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
    
    private static boolean isTittleCase(String string){
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
    
    private static Pattern emailPattern=Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    
    public static boolean isValidEmailId(String emailId){
        return emailPattern.matcher(emailId).matches();
    }

    
    public static String stringz(String s){
        if(s!=null && s.length()<1)
            return null;
        return s;
    }
    
    private static final SimpleDateFormat dateTimeFormat=new SimpleDateFormat("d MMM yyyy h:m:s:S a");
    public static String stringz(java.util.Date date,TimeZone timeZone){
        if(date==null)
            return null;
        dateTimeFormat.setTimeZone(timeZone);
        return dateTimeFormat.format(date);
    }
    
    private static final TimeZone utcTimeZone=TimeZone.getTimeZone("UTC");

    public static String stringz(java.util.Date date){
        return stringz(date, utcTimeZone);
    }

    public static String stringz(java.util.Date date,String pattern,TimeZone timeZone){
        if(date==null)
            return null;
        SimpleDateFormat dateFormat=new SimpleDateFormat(pattern);
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

    public static String stringz(java.util.Date date,String pattern){
        return stringz(date, pattern, (TimeZone)null);
    }
    public static String stringz(Calendar calendar,TimeZone timeZone){
        if(calendar==null)
            return null;
        return stringz(calendar.getTime(),timeZone);
    }
    
    public static String stringz(Calendar calendar){
        return stringz(calendar, (TimeZone)null);
    }
    
    public static String stringz(Calendar calendar, String pattern,TimeZone timeZone){
        if(calendar==null)
            return null;
        return stringz(calendar.getTime(),pattern,timeZone);
    }
    
    public static String stringz(Calendar calendar, String pattern){
        return stringz(calendar, pattern, (TimeZone)null);
    }
    
    public static String stringz(Object obj){
        if(obj==null)
            return null;
        if(obj instanceof String)
            return (String)obj;
        if(obj instanceof java.util.Date)
            return stringz((java.util.Date)obj);
        if(obj instanceof java.util.Calendar)
            return stringz((java.util.Calendar)obj);
        return obj.toString();
    }
    
    public static String[] split(String str, String delim) {
        FastGrowingIntList delimIndexes = new FastGrowingIntList(5);
        int index = 0;
        int fromIndex = 0;
        int delimSize = delim.length();
        while (true) {
            index = str.indexOf(delim, fromIndex);
            if (index < 0) {
                break;
            }
            delimIndexes.add(index);
            fromIndex = index + delimSize;
        }
        int delimCount = delimIndexes.size();
        String[] values = new String[delimCount + 1];
        int toIndex;
        int ctr = 0;
        fromIndex = 0;
        for (; ctr < delimCount; ctr++) {
            toIndex = delimIndexes.get(ctr);
            values[ctr] = str.substring(fromIndex, toIndex);
            fromIndex = toIndex + delimSize;
        }
        values[ctr] = str.substring(fromIndex);
        return values;
    }

    public static String[] split(String str, char delim) {
        FastGrowingIntList delimIndexes = new FastGrowingIntList(5);
        int index = 0;
        int fromIndex = 0;
        int delimSize = 1;
        while (true) {
            index = str.indexOf(delim, fromIndex);
            if (index < 0) {
                break;
            }
            delimIndexes.add(index);
            fromIndex = index + delimSize;
        }
        int delimCount = delimIndexes.size();
        String[] values = new String[delimCount + 1];
        int toIndex;
        int ctr = 0;
        fromIndex = 0;
        for (; ctr < delimCount; ctr++) {
            toIndex = delimIndexes.get(ctr);
            values[ctr] = str.substring(fromIndex, toIndex);
            fromIndex = toIndex + delimSize;
        }
        values[ctr] = str.substring(fromIndex);
        return values;
    }

    public static int randomInt(int min,int max){
        if(max<min)
            throw new IllegalArgumentException("max should be >= min");        
        return max==min?max:(int)(Math.random()*(max-min+1)) + min;
    }
    
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToBytes(final String hexString) throws IllegalArgumentException {
        final char[] data=hexString.toCharArray();
        final int len = data.length;

        if ((len & 0x01) != 0) {
            throw new IllegalArgumentException("Odd number of characters.");
        }

        final byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = Character.digit(data[j],16) << 4;
            j++;
            f = f | Character.digit(data[j],16);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    public static int getRandom(int[] values){
        return values[(int)(Math.random()*values.length)];
    }
    
    public static RuntimeException toRuntimeException(Throwable ex){
        if(ex instanceof RuntimeException)
            return (RuntimeException)ex;
        return new RuntimeException(ex);
    }
    
}
