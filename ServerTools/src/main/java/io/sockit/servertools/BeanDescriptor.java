/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.sockit.servertools;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hoshi
 */
public final class BeanDescriptor implements Serializable {
    final String[] fieldNames;
    final Field[] fields;
    final Class[] fieldTypes;
    final Map<String,int[]> fieldIndexes;
    final String firstField;
    final static boolean isAndroid=false;
    private static Map<String,BeanDescriptor> beanNameToDescriptors=new HashMap(50);
    
    private BeanDescriptor(String[] actualFieldNames,Class beanClass) {
        Field[] tmpFields=getFields(beanClass);
        List<Field> fieldList=new ArrayList(tmpFields.length);
        for(Field field:tmpFields){
            if(!Modifier.isStatic(field.getModifiers()))
                fieldList.add(field);
        }
        fields=new Field[fieldList.size()];
        fieldNames=new String[fieldList.size()];
        fieldTypes=new Class[fieldList.size()];
        boolean fieldNamesMatchFields=true;
        for(int ctr=0;ctr<fields.length;ctr++){
            fields[ctr]=fieldList.get(ctr);
            fields[ctr].setAccessible(true);
            fieldNames[ctr]=actualFieldNames!=null?actualFieldNames[ctr]:fields[ctr].getName();
            if(!fieldNames[ctr].equals(fields[ctr].getName()))
                fieldNamesMatchFields=false;
            fieldTypes[ctr]=fields[ctr].getType();
        }
        firstField=fieldNames[0];
        if(isAndroid && actualFieldNames!=null && fieldNamesMatchFields==false)
            Arrays.sort(fieldNames);
        fieldIndexes=new HashMap((int)(fieldNames.length*1.4));
        int[] index;
        for(int ctr=0;ctr<fieldNames.length;ctr++){
            index=new int[1];
            index[0]=ctr;
            fieldIndexes.put(fieldNames[ctr], index);
        }
    }
    
     private static Field[] getFields(Class beanClass){
        Class parent=beanClass.getSuperclass();
        List<Field> fields=null;
        fields=getFieldsList(parent);
        if(fields==null)
            return beanClass.getDeclaredFields();
        Field[] fieldArray=beanClass.getDeclaredFields();
        for(Field field: fieldArray){
            fields.add(field);
        }
        return fields.toArray(new Field[fields.size()]);
    }
    
    private static List<Field> getFieldsList(Class beanClass){
        if(beanClass==Object.class)
            return null;
        Class parent=beanClass.getSuperclass();
        List<Field> fields=null;
        fields=getFieldsList(parent);
        if(fields==null)
            fields=new LinkedList<Field>();
        Field[] fieldArray=beanClass.getDeclaredFields();
        for(Field field: fieldArray){
            fields.add(field);
        }
        return fields;
    }
    
    public static BeanDescriptor newInstance(Class beanClass){
        return newInstance(null, beanClass);
    }
    
    public static BeanDescriptor newInstance(String[] fieldNames,Class beanClass){
        BeanDescriptor descriptor=new BeanDescriptor(fieldNames,beanClass);
        beanNameToDescriptors.put(beanClass.getName(), descriptor);
        return descriptor;
    }
    
    static BeanDescriptor getDataDescriptor(Class beanClass){
        return beanNameToDescriptors.get(beanClass.getName());
    } 
    
    static BeanDescriptor getDataDescriptor(String beanName){
        return beanNameToDescriptors.get(beanName);
    }
    
    public int getFieldIndex(String fieldName){
        int[] index=fieldIndexes.get(fieldName);
        if(index!=null)
            return index[0];
        return -1;        
    }
    
    Field getFirstField(){
        int index=getFieldIndex(firstField);
        if(index!=-1)
            return fields[index];
        return null;        
    }
    
    public final Field getField(String fieldName){
        int index=getFieldIndex(fieldName);
        if(index!=-1)
            return fields[index];
        return null;        
    }
    
    private String[] fieldNamesCopy;
    public String[] getFieldNames(){
        if(fieldNamesCopy==null){
            fieldNamesCopy=Arrays.copyOf(fieldNames, fieldNames.length);
        }
        return fieldNamesCopy;
        
    }
    
    public static void setNull(Object beanInstance,Field field,Class fieldType) throws IllegalAccessException{
        if(fieldType==String.class)
            field.set(beanInstance, null);
        else if(fieldType==boolean.class)
            field.set(beanInstance, false);
        else if(fieldType==int.class)
            field.setInt(beanInstance, 0);
        else if(fieldType==long.class)
            field.setLong(beanInstance, 0);
        else if(fieldType==double.class)
            field.setDouble(beanInstance, 0);
        else if(fieldType==float.class)
            field.setFloat(beanInstance, 0);
        else if(fieldType==short.class)
            field.setShort(beanInstance, (short)0);
        else if(fieldType==byte.class)
            field.setByte(beanInstance, (byte)0);
        else if(fieldType==char.class)
            field.setChar(beanInstance, '\0');
        else
            field.set(beanInstance, null);                
    }
    
    public void setValue(Object beanInstance, String fieldName, Object value) throws Exception {
        Field field=this.getField(fieldName);
        if(field!=null){
            if(value==null)
                setNull(beanInstance, field, field.getType());
            else
                field.set(beanInstance, value);
            return;
        }
        throw new NoSuchFieldException(fieldName + " does not exist in class User");
    }
    
    public Object getValue(Object beanInstance, String fieldName) throws Exception {
        Field field=this.getField(fieldName);
        if(field!=null)
            return field.get(beanInstance);
        return null;
    }
}
