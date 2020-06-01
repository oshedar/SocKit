/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

/**
 *
 * @author Hoshedar Irani
 */
public interface FieldGetter {
    Object getValue(Object obj,String fieldName) throws Exception;    
}
