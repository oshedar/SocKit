/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.clienttools;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author h
 */
public class AsyncMethodExecutor {
    
    private static class MethodWithParamTypes{
        Method method;
        Class[] paramTypes;

        MethodWithParamTypes(Method method) {
            this.method = method;
            this.paramTypes=method.getParameterTypes();;
        }
    }
    private final Class classObj;
    private Map<String, List<MethodWithParamTypes>> methodsMap;
    
    public AsyncMethodExecutor(Class classObj) {
        this.classObj = classObj;
        Method[] methods=classObj.getMethods();
        List<MethodWithParamTypes> methodList;
        String methodName;
        methodsMap=new HashMap<>(methods.length,0.8f);
        for(Method method:methods){
            methodName=method.getName();
            methodList=methodsMap.get(methodName);
            if(methodList==null){
                methodList=new ArrayList(2);
                methodsMap.put(methodName, methodList);
            }
            methodList.add(new MethodWithParamTypes(method));            
        }
    }
    
    private static Class[] getParameterTypes(Object... args){
        Class[] types=new Class[args.length];
        int ctr=0;
        for(Object arg:args){
            types[ctr]=arg.getClass();
            ctr++;
        }
        return types;
    }
    
    public void executeMethod(Object instance, String methodName,Object... args){
        List<MethodWithParamTypes> methodList=methodsMap.get(methodName);
        if(methodList==null)
            return;
        if(methodList.size()<2){
            Timer.execute(new Invoker(instance, methodList.get(0).method, args));
            return;
        }
        for(MethodWithParamTypes methodWithParamTypes:methodList){
            if(isAssignable(methodWithParamTypes.paramTypes, args)){   
                Timer.execute(new Invoker(instance, methodWithParamTypes.method, args));
                return;
            }
        }
    }
    
    private static boolean isAssignable(Class[] paramTypes,Object... args){
        if(paramTypes.length==args.length)
            return ClassUtils.isAssignable(getParameterTypes(args),paramTypes, true);
        return false;
    }
    
    private static class Invoker implements Runnable{
        Method method;
        Object[] args;
        Object instance;
        public Invoker(Object instance,Method method, Object[] args) {
            this.method = method;
            this.args = args;
            this.instance=instance;
        }

        @Override
        public void run() {
            try{
                method.invoke(instance, args);
            }catch(Exception ex){
                System.out.println("error executing method :" + method.getName());
                ex.printStackTrace();
            }
        }
        
    }
   
    public static void main(String[] args){
        System.out.println(int.class==Integer.TYPE);
        System.out.println(Integer.class.isAssignableFrom(int.class));
        System.out.println(Integer.TYPE.isAssignableFrom(Short.TYPE));        
    }
}
