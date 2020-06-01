/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.pokergame;

/**
 *
 * A convenience class to represent name-value pairs
 */
final class Pair<F, S> {
    public final F first; //first member of pair
    public final S second; //second member of pair

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

}

