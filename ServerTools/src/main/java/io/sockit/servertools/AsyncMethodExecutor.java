/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
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
        }
        void setParamTypes(){
            paramTypes=method.getParameterTypes();
        }
    }
    private Class classObj;
    private Map<String, MethodWithParamTypes[]> methodsMap;
    
    public AsyncMethodExecutor(Class classObj) {
        this.classObj = classObj;
        Method[] methods=classObj.getMethods();
        MethodWithParamTypes[] methodArray;
        String methodName;
        methodsMap=new HashMap<>(methods.length,0.8f);
        for(Method method:methods){
            methodName=method.getName();
            methodArray=methodsMap.get(methodName);
            if(methodArray==null){
                methodArray=new MethodWithParamTypes[1];
                methodArray[0]=new MethodWithParamTypes(method);
                methodsMap.put(methodName, methodArray);
            }
            else{
                if(methodArray.length<2)
                    methodArray[0].setParamTypes();
                methodArray=Arrays.copyOf(methodArray, methodArray.length, MethodWithParamTypes[].class);
                methodArray[methodArray.length-1]=new MethodWithParamTypes(method);
                methodArray[methodArray.length-1].setParamTypes();
                methodsMap.put(methodName, methodArray);
            }
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
        MethodWithParamTypes[] methodArray=methodsMap.get(methodName);
        if(methodArray==null)
            return;
        if(methodArray.length<2){
            Executor.execute(new Invoker(instance, methodArray[0].method, args));
            return;
        }
        for(MethodWithParamTypes methodWithParamTypes:methodArray){
            if(isAssignable(methodWithParamTypes.paramTypes, args)){
                Executor.execute(new Invoker(instance, methodWithParamTypes.method, args));
                return;
            }
        }
    }
    
    private static boolean isAssignable(Class[] paramTypes,Object... args){
        if(paramTypes.length==args.length)
            return ClassUtils.isAssignable(paramTypes, getParameterTypes(args),true);
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
                ex.printStackTrace();
            }
        }
        
    }
   
}
