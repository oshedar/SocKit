/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

/**
 *
 * @author Hoshedar Irani
 */
class TooManySessionsException extends Exception {

    /**
     * Creates a new instance of <code>TooManySessionsException</code> without
     * detail message.
     */
    TooManySessionsException() {
        super("User Exceeded max No Of Sessions");
    }
}
