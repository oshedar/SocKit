/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.pokergame;

/**
 *
 * @author h
 */
public enum PlayerAction {
    none,//initial value when player has yet to play
    folded,
    called,
    raised,
    allIn,
    smallBlind,
    bigBlind,
    checked
}
