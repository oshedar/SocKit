/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.pokergame;

import io.sockit.sockitserver.JsonUtil;
import io.sockit.sockitserver.Player;
import io.sockit.sockitserver.Session;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author h
 */
public class PokerPlayer extends Player{
    List<Card> hand=new ArrayList<Card>(2);
    int chipsOnTable;
    int chipsInPot;
    int lastAmtBet;
    PlayerAction lastAction=PlayerAction.none;
    int raisedBy;
    int handEVal=0;
    int amtWonInGame;
    List<Card> winningCards;
    HandEval.HandCategory winningHandCategory;
    PokerRound lastBettingRound;
    
    public PokerPlayer(Session session,int seatNo,JsonObject data) {        
        super(session,seatNo);
        chipsOnTable=JsonUtil.getAsInt(data, "chipsOnTable", 0);
    }

    void setHoleCards(Card card1,Card card2){
        hand.add(card1);
        hand.add(card2);
    }

    @Override
    protected JsonObject prepareJsonForOtherClients() { //data same for others and self
        JsonObjectBuilder json=JsonUtil.createObjectBuilder();
        JsonArrayBuilder jsonHand=JsonUtil.createArrayBuilder();
        boolean gameEnding=room.isGamePlayEnding();
        PokerRoom sitNGoRoom=(PokerRoom)room;
        for(Card card:hand){
            if(room.isGamePlayEnding() && this.lastAction!=PlayerAction.folded && sitNGoRoom.playersNotFoldedCount()>1)
                jsonHand.add(card.toString());
            else
                jsonHand.add("-");
        }
        json.add("hand", jsonHand);
        json.add("chipsOnTable", chipsOnTable);
        json.add("chipsInPot", chipsInPot);
        json.add("lastAmtBet", lastAmtBet);
        json.add("lastAction", lastAction.toString());
        json.add("raisedBy", lastAction==PlayerAction.raised?raisedBy:0);
        json.add("amtWon", amtWonInGame>0?amtWonInGame-chipsInPot:0);
        return json.build();
    }
    
    @Override
    protected JsonObject prepareJsonForSelfClient() {       
        JsonObjectBuilder json=JsonUtil.createObjectBuilder();
        JsonArrayBuilder jsonHand=JsonUtil.createArrayBuilder();
        for(Card card:hand)
            jsonHand.add(card.toString());
        json.add("hand", jsonHand);
        json.add("chipsOnTable", chipsOnTable);
        json.add("chipsInPot", chipsInPot);
        json.add("lastAmtBet", lastAmtBet);
        json.add("lastAction", lastAction.toString());
        json.add("raisedBy", lastAction==PlayerAction.raised?raisedBy:0);
        json.add("amtWon", amtWonInGame>0?amtWonInGame-chipsInPot:0);        
//        if(!session.getUser().isBot){
//            room.debug(session, "chipsOnTabel=" + chipsOnTable + ", chipsInPot=" + chipsInPot + ", call value=" + (((PokerRoom)room).maxBetInPot-chipsInPot));
//        }
        return json.build();
    }

    @Override
    protected void onShutDown() {
        
    }

    @Override
    protected void onRoomDestroyed() {
        PokerUserData pokerUserData=(PokerUserData)this.session.getGameUserData();
        pokerUserData.chipsInHand+=this.chipsOnTable+this.chipsInPot;
    }

    @Override
    protected void resetData() {
        hand.clear();
        lastAction=PlayerAction.none;
        chipsInPot=0;
        lastAmtBet=0;
        raisedBy=0;
        amtWonInGame=0;
        winningCards=null;
        handEVal=0;
        winningHandCategory=null;
        lastBettingRound=null;
    }

    @Override
    protected boolean canJoinGamePlay() {
        PokerRoom playerRoom=(PokerRoom)this.room;        
        return chipsOnTable>=playerRoom.bigBlind;
    }

    @Override
    protected String onCantJoinGamePlay() {
        room.debug(session, "not elligible to play");
        leaveSeat();
        return "Chips on table too less to play";
    }

    @Override
    protected boolean canPlayTurn() {
        return this.lastAction!=PlayerAction.allIn && this.lastAction!=PlayerAction.folded;
    }
        
}
