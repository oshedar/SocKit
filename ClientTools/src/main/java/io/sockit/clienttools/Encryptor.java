/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.sockit.clienttools;

/**
 *
 * @author hoshi2
 */
public interface Encryptor {
    public byte[] encrypt(byte[] bytes);
    public String encrypt(String txt);
}
